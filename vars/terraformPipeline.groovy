def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Checkout Code') {
                steps {
                    git branch: config.get('gitBranch', 'main'),
                        url: config.get('gitRepo', 'https://github.com/GowshikM-2005/jenkins-shared-lib.git')
                }
            }

            stage('Terraform Init') {
                steps {
                    withCredentials([file(credentialsId: 'kubeconfig-secret', variable: 'KUBECONFIG')]) {
                        sh 'terraform init'
                    }
                }
            }

            stage('Terraform Plan') {
                steps {
                    sh 'terraform plan -out=tfplan'
                }
            }

            stage('Terraform Apply') {
                when {
                    expression { return config.get('autoApprove', false) }
                }
                steps {
                    withCredentials([file(credentialsId: 'kubeconfig-secret', variable: 'KUBECONFIG')]) {
                        sh 'terraform apply -auto-approve'
                    }
                }
            }
        }
    }
}
