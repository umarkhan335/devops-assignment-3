pipeline {
    agent any
    
    environment {
        APP_IMAGE = "todo-app:latest"
        APP_CONTAINER = "todo-app-container"
        NETWORK = "todo-network"
    }

    stages {
        stage('Setup Environment') {
            steps {
                script {
                    // Create network if it doesn't exist
                    sh "docker network create ${NETWORK} || true"
                }
            }
        }

        stage('Build & Deploy App') {
            steps {
                dir('app') {
                    script {
                        // Build the new image
                        sh "docker build -t ${APP_IMAGE} ."
                        
                        // Stop and remove any old running container
                        sh "docker stop ${APP_CONTAINER} || true"
                        sh "docker rm ${APP_CONTAINER} || true"
                        
                        // Run the new container, mapping port 5000 to the host so the instructor can view it
                        sh "docker run -d --name ${APP_CONTAINER} --network ${NETWORK} -p 5000:5000 ${APP_IMAGE}"
                        
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
                    args "--network ${NETWORK} -v /dev/shm:/dev/shm --entrypoint='' -u root"
                    reuseNode true
                }
            }
            steps {
                dir('tests') {
                    script {
                        // Print versions for debugging
                        sh "ls -la /usr/bin/ | grep chrome || true"
                        sh "chromedriver --version || true"
                        // Pass the container name as APP_URL to the test suite
                        sh "APP_URL=http://todo-app-container:5000 mvn clean test -Dwebdriver.chrome.driver=/usr/bin/chromedriver"
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
        // NOTICE: We are NOT stopping the app container here anymore.
        // It will remain running so you can provide the Deployment URL!
        
        success {
            emailext (
                to: "umarkhan335@gmail.com",
                subject: "SUCCESS: Build '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>Build SUCCESS</p>
                         <p>Check the test results and console output at: <a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
                recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
        failure {
            emailext (
                to: "umarkhan335@gmail.com",
                subject: "FAILED: Build '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """<p>Build FAILED</p>
                         <p>Check the test results and console output at: <a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a></p>""",
                recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
            )
        }
    }
}
