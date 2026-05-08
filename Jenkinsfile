pipeline {
    agent any
    
    environment {
        APP_IMAGE = "todo-app:${env.BUILD_ID}"
        APP_CONTAINER = "todo-app-container-${env.BUILD_ID}"
        NETWORK = "todo-network-${env.BUILD_ID}"
    }

    stages {
        stage('Setup Environment') {
            steps {
                script {
                    sh "docker network create ${NETWORK} || true"
                }
            }
        }

        stage('Build & Deploy App') {
            steps {
                dir('app') {
                    script {
                        sh "docker build -t ${APP_IMAGE} ."
                        sh "docker run -d --rm --name ${APP_CONTAINER} --network ${NETWORK} ${APP_IMAGE}"
                        // Wait for the app to initialize
                        sleep 10
                    }
                }
            }
        }

        stage('Run Selenium Tests') {
            agent {
                docker {
                    image 'markhobson/maven-chrome:jdk-11'
                    args "--network ${NETWORK} -v /dev/shm:/dev/shm"
                    reuseNode true
                }
            }
            steps {
                dir('tests') {
                    script {
                        // Pass the container name as APP_URL to the test suite
                        sh "APP_URL=http://${APP_CONTAINER}:5000 mvn clean test"
                    }
                }
            }
            post {
                always {
                    junit 'tests/target/surefire-reports/*.xml'
                }
            }
        }
    }

    post {
        always {
            script {
                // Cleanup Docker resources
                sh "docker stop ${APP_CONTAINER} || true"
                sh "docker network rm ${NETWORK} || true"
            }
        }
        success {
            emailext (
                subject: "SUCCESS: Build '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>Build SUCCESS</p>
                         <p>Check the test results and console output at: <a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
                recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
        failure {
            emailext (
                subject: "FAILED: Build '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>Build FAILED</p>
                         <p>Check the test results and console output at: <a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
                recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
    }
}
