pipeline {
  agent any

  environment {
    REGISTRY = "host.docker.internal:5001"               
    VERSION  = "${env.BUILD_NUMBER}"           
    JAVA_OPTS = "-Dorg.gradle.jvmargs=-Xmx2g" 
  }

  options {
    skipDefaultCheckout(true)
    timestamps()
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'chmod +x gradlew'   
      }
    }

    stage('Gradle build & test') {
      steps {
        sh './gradlew --no-daemon clean test bootJar'
      }
      post {
        always {
          junit 'build/test-results/test/*.xml'         
          archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
        }
      }
    }

    stage('Build Docker image') {
      steps {
        sh '''
          docker build -t ${IMAGE}:${VERSION} .
          docker tag ${IMAGE}:${VERSION} ${REGISTRY}/${IMAGE}:${VERSION}
          docker tag ${IMAGE}:${VERSION} ${REGISTRY}/${IMAGE}:latest
        '''
      }
    }

    stage('Login & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'nexus-docker', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
          sh '''
            echo "$PASS" | docker login ${REGISTRY} -u "$USER" --password-stdin
            docker push ${REGISTRY}/${IMAGE}:${VERSION}
            docker push ${REGISTRY}/${IMAGE}:latest
          '''
        }
      }
    }
  }

  post {
    always {
      sh 'docker logout ${REGISTRY} || true'
      sh 'docker image prune -f || true'
    }
  }
}