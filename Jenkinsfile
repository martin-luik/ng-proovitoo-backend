pipeline {
  agent any
  options { timestamps() }

  environment {
    REGISTRY = "localhost:5000"
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
  }

  post {
    always { sh 'docker image prune -f || true' }
  }
}