pipeline {
  agent any
  options { timestamps() }

  environment {
    REGISTRY = "localhost:5001"
    IMAGE    = "ng-proovitoo-backend"
    VERSION  = "${env.BUILD_NUMBER}"
  }

  stages {
    stage('Build & Test (Gradle JDK21)') {
      agent {
        docker {
          image 'gradle:8.10.2-jdk21'
        }
      }
      environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        NEXUS_URL = 'http://host.docker.internal:8081/repository/maven-public/'
      }
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'nexus-maven',
          usernameVariable: 'NEXUS_USER',
          passwordVariable: 'NEXUS_PASS'
        )]) {
          sh '''#!/usr/bin/env bash
            set -euxo pipefail
            chmod +x gradlew || true
    
            ./gradlew --no-daemon \
              -PnexusUser="$NEXUS_USER" \
              -PnexusPass="$NEXUS_PASS" \
              -PnexusUrl="$NEXUS_URL" \
              clean test bootJar
          '''
        }
      }
      post {
        always {
          junit 'build/test-results/test/*.xml'
          archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
        }
      }
    }

    stage('Docker build & push (host)') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'nexus-docker', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
          sh '''
            docker build -t ${IMAGE}:${VERSION} .
            docker tag ${IMAGE}:${VERSION} ${REGISTRY}/${IMAGE}:${VERSION}
            docker tag ${IMAGE}:${VERSION} ${REGISTRY}/${IMAGE}:latest
            echo "$PASS" | docker login ${REGISTRY} -u "$USER" --password-stdin
            docker push ${REGISTRY}/${IMAGE}:${VERSION}
            docker push ${REGISTRY}/${IMAGE}:latest
          '''
        }
      }
      post {
        always { sh 'docker logout ${REGISTRY} || true' }
      }
    }

    stage('Helm package & upload (hosted)') {
      agent { docker { image 'host.docker.internal:5001/devops/kubectl-helm:3.19.0' } }
      environment {
        CHART_DIR     = 'helm'
        CHART_VERSION = "0.1.${env.BUILD_NUMBER}"
        APP_VERSION   = "${env.VERSION}"
        HELM_REPO_URL = "http://host.docker.internal:8081/repository/helm-hosted/"
      }
      steps {
        withCredentials([usernamePassword(credentialsId: 'nexus-helm', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
          sh '''
            set -euo pipefail
            set -x

            rm -f ./*.tgz || true

            sed -i "s/^version:.*/version: ${CHART_VERSION}/" ${CHART_DIR}/Chart.yaml || true
            sed -i "s/^appVersion:.*/appVersion: \\"${APP_VERSION}\\"/" ${CHART_DIR}/Chart.yaml || true

            if grep -q "^dependencies:" ${CHART_DIR}/Chart.yaml; then
              helm dependency build ${CHART_DIR}
            fi

            helm package ${CHART_DIR} --version ${CHART_VERSION} --app-version ${APP_VERSION}

            TGZ=$(ls -1 *.tgz)
            BASENAME=$(basename "$TGZ")

            curl -f -L -u "${USER}:${PASS}" \
              --upload-file "${TGZ}" \
              "${HELM_REPO_URL}${BASENAME}"
          '''
        }
      }
    }

    stage('Publish DB init (E2E ConfigMap)') {
      agent { docker { image 'host.docker.internal:5001/devops/kubectl-helm:3.19.0' } }
      environment {
        NAMESPACE = "ng-events" // või ng-config, kui tahad püsivat “publish” NS-i
        SQL_PATH  = "src/main/resources/db/changelog/initdb.sql"
      }
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-ng-events', variable: 'KCFG')]) {
          sh '''#!/usr/bin/env bash
            set -euo pipefail

            cp "$KCFG" ./kubeconfig
            chmod 600 ./kubeconfig
            if grep -q "https://127.0.0.1:6443" ./kubeconfig; then
              sed -i 's#https://127.0.0.1:6443#https://kubernetes.docker.internal:6443#g' ./kubeconfig
            fi
            export KUBECONFIG="$PWD/kubeconfig"

            kubectl version --client
            kubectl get nodes
            kubectl cluster-info || true

            kubectl create ns "${NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f -

            SQL_PATH="src/main/resources/db/changelog/initdb.sql"
            test -f "${SQL_PATH}" || { echo "missing ${SQL_PATH}"; exit 2; }

            kubectl -n "${NAMESPACE}" create configmap pg-init-src \
              --from-file=initdb.sql="${SQL_PATH}" \
              --dry-run=client -o yaml | kubectl apply -f -
          '''
        }
      }
    }

    stage('Deploy (Helm)') {
      agent {
        docker {
          image 'host.docker.internal:5001/devops/kubectl-helm:3.19.0'
        }
      }
      environment {
        HELM_REPO_NAME = "company-helm"
        HELM_REPO_URL  = "http://host.docker.internal:8081/repository/helm-hosted/"
        CHART_NAME     = "ng-backend"
        CHART_VERSION  = "0.1.${env.BUILD_NUMBER}"
    
        REGISTRY_PULL  = "host.docker.internal:5001"
    
        RELEASE_NAME   = "ng-events-backend"
        NAMESPACE      = "ng-events"
      }
      steps {
        withCredentials([
          file(credentialsId: 'kubeconfig-ng-events', variable: 'KCFG'),
          usernamePassword(credentialsId: 'nexus-helm',   usernameVariable: 'HUSER', passwordVariable: 'HPASS'),
          usernamePassword(credentialsId: 'nexus-docker', usernameVariable: 'DUSER', passwordVariable: 'DPASS')
        ]) {
          sh '''
            set -euo pipefail
            set -x
    
            cp "$KCFG" ./kubeconfig
            chmod 600 ./kubeconfig
            if grep -q "https://127.0.0.1:6443" ./kubeconfig; then
              sed -i 's#https://127.0.0.1:6443#https://kubernetes.docker.internal:6443#g' ./kubeconfig
            fi
            export KUBECONFIG="$PWD/kubeconfig"
    
            kubectl config view --minify
            kubectl cluster-info || true
    
            kubectl create namespace "${NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f - || true
            kubectl -n "${NAMESPACE}" create secret docker-registry nexus-regcred \
              --docker-server="${REGISTRY_PULL}" \
              --docker-username="${DUSER}" \
              --docker-password="${DPASS}" \
              --dry-run=client -o yaml | kubectl apply -f -
            kubectl -n "${NAMESPACE}" patch serviceaccount default \
              -p '{"imagePullSecrets":[{"name":"nexus-regcred"}]}' || true
    
            helm repo add "${HELM_REPO_NAME}" "${HELM_REPO_URL}" --username "${HUSER}" --password "${HPASS}"
            helm repo update
    
            helm upgrade --install "${RELEASE_NAME}" "${HELM_REPO_NAME}/${CHART_NAME}" \
              --version "${CHART_VERSION}" \
              --namespace "${NAMESPACE}" --create-namespace \
              --set image.repository="${REGISTRY_PULL}/${IMAGE}" \
              --set-string image.tag="${VERSION}" \
              --wait --atomic --timeout 10m --history-max 10
    
            kubectl -n "${NAMESPACE}" get deploy,po,svc
          '''
        }
      }
    }
  }

  post {
    always { sh 'docker image prune -f || true' }
  }
}