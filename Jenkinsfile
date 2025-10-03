pipeline {
  agent any
  options { timestamps() }

  environment {
    // Pushime Nexusesse hosti porti...
    REGISTRY_PUSH = "localhost:5001"
    // ...aga K8s tõmbab Docker Desktopis selle alt:
    REGISTRY_PULL = "host.docker.internal:5001"

    IMAGE = "ng-proovitoo-backend"
  }

  stages {
    stage('Checkout & Tag') {
      steps {
        checkout scm
        script {
          env.TAG_SHA = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
          echo "Using image tag: ${env.TAG_SHA}"
        }
      }
    }

    stage('Build & Test (Gradle JDK21)') {
      agent { docker { image 'gradle:8.10.2-jdk21' } }
      environment { GRADLE_USER_HOME = "${WORKSPACE}/.gradle" }
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

    stage('Docker build & push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'nexus-docker', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
          sh '''
            set -e
            docker build -t ${IMAGE}:${TAG_SHA} .
            docker tag ${IMAGE}:${TAG_SHA} ${REGISTRY_PUSH}/${IMAGE}:${TAG_SHA}
            docker tag ${IMAGE}:${TAG_SHA} ${REGISTRY_PUSH}/${IMAGE}:latest

            echo "$PASS" | docker login ${REGISTRY_PUSH} -u "$USER" --password-stdin
            docker push ${REGISTRY_PUSH}/${IMAGE}:${TAG_SHA}
            docker push ${REGISTRY_PUSH}/${IMAGE}:latest

            # Kiirkontroll, et tag on registris olemas
            curl -s http://${REGISTRY_PUSH}/v2/${IMAGE}/tags/list | grep -q ${TAG_SHA}
          '''
        }
      }
      post { always { sh 'docker logout ${REGISTRY_PUSH} || true' } }
    }

    stage('Deploy (Helm)') {
      agent { docker { image 'dtzar/helm-kubectl:3.14.4' } }
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-ng-events', variable: 'KCFG')]) {
          sh '''
            set -euo pipefail

            # kubeconfig tööle
            cp "$KCFG" ./kubeconfig && chmod 600 ./kubeconfig
            # Docker Desktop K8s API fix konteinerist:
            if grep -q "https://127.0.0.1:6443" ./kubeconfig; then
              sed -i 's#https://127.0.0.1:6443#https://kubernetes.docker.internal:6443#g' ./kubeconfig
            fi
            export KUBECONFIG="$PWD/kubeconfig"

            kubectl config view --minify || true
            kubectl cluster-info || true

            CHART_DIR="helm"
            [ -f "$CHART_DIR/Chart.yaml" ] || { echo "Chart not found in $CHART_DIR"; exit 2; }

            helm upgrade --install ng-events "$CHART_DIR" \
              --namespace ng-events --create-namespace \
              --set image.repository=${REGISTRY_PULL}/${IMAGE} \
              --set image.tag=${TAG_SHA} \
              --wait --atomic --timeout 5m
          '''
        }
      }
    }
  }

  post { always { sh 'docker image prune -f || true' } }
}