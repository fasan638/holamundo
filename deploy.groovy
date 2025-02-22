pipeline{
    agent {
         label 'master'
    }
    /*
    tools {
         maven 'maven 3.6'
         jdk 'java'
    }
    */
    environment {
        // This can be nexus3 or nexus2
        NEXUS_VERSION = "nexus3"
        // This can be http or https
        NEXUS_PROTOCOL = "http"
        // Where your Nexus is running. 'nexus-3' is defined in the docker-compose file
        NEXUS_URL = "192.168.0.200:8081"
        // Repository where we will upload the artifact
        NEXUS_REPOSITORY = "maven-releases"
        // Jenkins credential id to authenticate to Nexus OSS
        NEXUS_CREDENTIAL_ID = "nexus"
        DOCKER_VERSION = "0.2.2"
        
        // Workfolder
        //WORKFOLDER = "/usr/jenkins/node_agent/workspace"
    }

    stages{
        stage('Checkout'){
            steps{
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'github', url: 'https://github.com/fasan638/holamundo.git']]])
            }
        }
        stage('Download artifact from nexus'){
            agent {
                label 'maven'
            }
            steps{
                sh '''
                    pwd 
                    curl -v -u admin:Curso322 -o app.jar http://192.168.0.200:8081/repository/maven-releases/org/springframework/Jenkins-holamundo/0.2.2/Jenkins-holamundo-0.2.2.jar
                '''
            }
        }
        stage('Build container'){
            agent {
                label 'maven'
            }
            steps{
                sh '''
                    docker build -t holamundo .
                    docker tag holamundo:latest 192.168.0.200:8082/holamundo:latest
                '''

            }
        } //fin stage build container
        
        //user y pass de nexus dado que sube al contenedor docker
        stage('Upload container'){
            agent {
                label 'maven'
            }
            steps{
                sh '''
                    docker login -u admin -p Curso322 192.168.0.200:8082   
                    docker push 192.168.0.200:8082/holamundo:latest
                '''

            }
        } //fin stage build container
        stage('Deploy container'){
            agent {
                label 'maven'
            }
            steps{
                sh '''
                    docker run -d -p 8085:80 holamundo
                '''

            }
        } //fin stage build container
        
        stage("Post") {
            agent {
                label 'maven'
            }
            steps {
                sh '''
                    pwd
                    echo "Clean up workfolder"
                    rm -Rf *
                '''
            }
        } //fin stage post
        
    }
}
