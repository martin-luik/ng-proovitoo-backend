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

    stage('Deploy (Helm)') {
      steps {
        script {
          if (!env.TAG_SHA?.trim()) {
            env.TAG_SHA = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
          }
        }
        withCredentials([file(credentialsId: 'kubeconfig-ng-events', variable: 'KCFG')]) {
          sh '''
            CHART_DIR="helm"

            cp "$KCFG" "$WORKSPACE/kubeconfig"
            chmod 600 "$WORKSPACE/kubeconfig"

            docker run --rm \
              -e KUBECONFIG=/kube/config \
              -v "$WORKSPACE/kubeconfig":/kube/config:ro \
              -v "$PWD/${CHART_DIR}":/chart \
              dtzar/helm-kubectl:3.14.4 \
              sh -c 'set -euo pipefail; \
                     ls -l /kube; echo "---"; \
                     kubectl config view --minify || true; echo "---"; \
                     cd /chart && \
                     helm upgrade --install ng-events . \
                       --namespace ng-events --create-namespace \
                       --set image.repository=host.docker.internal:5001/ng-proovitoo-backend \
                       --set image.tag='${TAG_SHA:-latest}' \
                       --wait --atomic --timeout 5m'
          '''
        }
      }
    }
  }

  post {
    always { sh 'docker image prune -f || true' }
  }
}