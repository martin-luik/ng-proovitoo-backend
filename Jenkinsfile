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
      }
      steps {
        sh 'chmod +x gradlew || true'
        sh './gradlew --no-daemon clean test bootJar'
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

    stage('E2E: DB + BE + FE + Playwright') {
      agent { docker { image 'host.docker.internal:5001/devops/kubectl-helm:3.19.0' } }
      environment {
        E2E_NS          = "e2e-ng-events-${env.BUILD_NUMBER}"    
        REGISTRY_PULL   = "host.docker.internal:5001"
        CFG_SRC_NS      = "ng-events"                          
        CFG_SRC_NAME    = "pg-init-src"
        HELM_REPO_URL   = "http://host.docker.internal:8081/repository/helm-hosted/"
        HELM_REPO_NAME  = "company-helm"

        CHART_BE        = "ng-backend"
        CHART_FE        = "ng-frontend"
        PG_CHART_VER    = "18.0.8"                                 

        BE_IMAGE        = "ng-proovitoo-backend"
        FE_IMAGE        = "ng-proovitoo-frontend"
        BE_TAG          = "${params.BACKEND_TAG}"                  
        FE_TAG          = "${env.BUILD_NUMBER}"                 

        DB_USER         = "admin"
        DB_PASS         = "admin"
        DB_NAME         = "events_db"

        LB_SCHEMA       = "liquibase"
        APP_SCHEMA      = "event_mgmt"
      }
      steps {
        withCredentials([
          file(credentialsId: 'kubeconfig-ng-events', variable: 'KCFG'),
          usernamePassword(credentialsId: 'nexus-helm',   usernameVariable: 'USER', passwordVariable: 'PASS'),
          usernamePassword(credentialsId: 'nexus-docker', usernameVariable: 'USER', passwordVariable: 'PASS')
        ]) {
          sh '''#!/usr/bin/env bash
            set -euxo pipefail

            cp "$KCFG" ./kubeconfig
            chmod 600 ./kubeconfig
            if grep -q "https://127.0.0.1:6443" ./kubeconfig; then
              sed -i 's#https://127.0.0.1:6443#https://kubernetes.docker.internal:6443#g' ./kubeconfig
            fi
            export KUBECONFIG="$PWD/kubeconfig"

            kubectl -n "${CFG_SRC_NS}" get configmap "${CFG_SRC_NAME}" -o jsonpath='{.data.initdb\.sql}' > /tmp/initdb.sql
            kubectl -n "${E2E_NS}" create configmap pg-init \
              --from-file=initdb.sql=/tmp/initdb.sql \
              --dry-run=client -o yaml | kubectl apply -f -

            helm repo add bitnami https://charts.bitnami.com/bitnami || true
            helm upgrade --install pg oci://registry-1.docker.io/bitnamicharts/postgresql \
              --version "${PG_CHART_VER}" \
              -n "${E2E_NS}" \
              --set auth.username="${DB_USER}" \
              --set auth.password="${DB_PASS}" \
              --set auth.database="${DB_NAME}" \
              --set primary.initdb.user="${DB_USER}" \
              --set primary.initdb.scriptsConfigMap=pg-init \
              --wait --timeout 5m

            helm repo add "${HELM_REPO_NAME}" "${HELM_REPO_URL}" --username "${USER}" --password "${PASS}" || true
            helm repo update

            helm upgrade --install e2e-ng-backend "${HELM_REPO_NAME}/${CHART_BE}" \
              -n "${E2E_NS}" \
              --set image.repository="${REGISTRY_PULL}/${BE_IMAGE}" \
              --set-string image.tag="${BE_TAG}" \
              --set ingress.enabled=true \
              --set ingress.host="" \
              --set-string extraEnv.SPRING_DATASOURCE_URL="jdbc:postgresql://pg-postgresql.${E2E_NS}.svc.cluster.local:5432/${DB_NAME}" \
              --set        extraEnv.SPRING_DATASOURCE_USERNAME=events_adm_user \
              --set        extraEnv.SPRING_DATASOURCE_PASSWORD=events_admin \
              --set        extraEnv.SPRING_LIQUIBASE_DEFAULT_SCHEMA=${LB_SCHEMA} \
              --set        extraEnv."SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA"=${APP_SCHEMA} \
              --wait --atomic --timeout 10m

            kubectl -n "${E2E_NS}" rollout status deploy -l app=ng-events-backend --timeout=180s
            kubectl -n "${E2E_NS}" run curl-be --rm -i --restart=Never --image=curlimages/curl:8.10.1 -- \
              sh -lc 'curl -fsS http://events-backend-service:80/actuator/health | grep -q "\"status\":\"UP\""'

            helm upgrade --install e2e-ng-frontend "${HELM_REPO_NAME}/${CHART_FE}" \
              -n "${E2E_NS}" \
              --set image.repository="${REGISTRY_PULL}/${FE_IMAGE}" \
              --set-string image.tag="${FE_TAG}" \
              --set ingress.enabled=true \
              --set ingress.host="" \
              --wait --atomic --timeout 10m

            kubectl -n "${E2E_NS}" rollout status deploy -l app=ng-events-frontend --timeout=180s

            PF_POD="$(kubectl -n ingress-nginx get po -l app.kubernetes.io/component=controller -o jsonpath='{.items[0].metadata.name}')"
            kubectl -n ingress-nginx port-forward "$PF_POD" 8088:80 >/tmp/pf8088.log 2>&1 &
            PF_PID=$!
            sleep 2


            for i in $(seq 1 60); do
              curl -fsS http://localhost:8088/ && break
              sleep 2
            done


            docker run --rm --network=host -v "$PWD":/work -w /work node:24-bookworm bash -lc '
              set -eux
              npm ci || npm install
              npx playwright install --with-deps
              # BASE_URL näitab ingressi kaudu FE-le; FE proxy.conf suunab /v1 ja /auth backendi ingressile
              BASE_URL=http://localhost:8088/ PW_USE_WEBSERVER=false npx playwright test
            '

            kill ${PF_PID} || true
          '''
        }
      }
      post {
        always {
          // koristus – jäta ära kui tahad jäävaid jälgi
          sh '''#!/usr/bin/env bash
            set -euxo pipefail
            export KUBECONFIG="$PWD/kubeconfig" || true
            helm uninstall e2e-ng-frontend -n "${E2E_NS}" || true
            helm uninstall e2e-ng-backend  -n "${E2E_NS}" || true
            helm uninstall pg              -n "${E2E_NS}" || true
            kubectl delete ns "${E2E_NS}" --ignore-not-found=true || true
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