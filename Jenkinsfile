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
      agent {
        docker { image 'dtzar/helm-kubectl:3.14.4' }
      }
      steps {
        script {
          if (!env.TAG_SHA?.trim()) {
            env.TAG_SHA = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
          }
        }
        withCredentials([file(credentialsId: 'kubeconfig-ng-events', variable: 'KCFG')]) {
          sh '''
            set -euo pipefail

            # kubeconfig tööle
            cp "$KCFG" ./kubeconfig
            chmod 600 ./kubeconfig

            if grep -q "https://127.0.0.1:6443" ./kubeconfig; then
              sed -i 's#https://127.0.0.1:6443#https://kubernetes.docker.internal:6443#g' ./kubeconfig
            fi

            export KUBECONFIG="$PWD/kubeconfig"

            kubectl config view --minify
            kubectl cluster-info

            CHART_DIR="helm"
            [ -f "$CHART_DIR/Chart.yaml" ] || { echo "Chart not found in $CHART_DIR"; exit 2; }

            # helm deploy
            helm upgrade --install ng-events "$CHART_DIR" \
              --namespace ng-events --create-namespace \
              --set image.repository=host.docker.internal:5001/ng-proovitoo-backend \
              --set image.tag=${VERSION} \
              --wait --atomic --timeout 5m
          '''
        }
      }
    }
  }

  post {
    always { sh 'docker image prune -f || true' }
  }
}