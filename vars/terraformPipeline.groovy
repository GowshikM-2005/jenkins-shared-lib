def call(Map config = [:]) {
    pipeline {
        agent any 

        stages {
            stage('Checkout Code') {
                steps {
                    git branch: config.get('gitBranch', 'main'),
                        url: config.get('gitRepo', 'https://github.com/GowshikM-2005/Terraform.git')
                }
            }

            stage('Terraform Init & Plan') {
                steps {
                    withCredentials([file(credentialsId: 'kubeconfig-secret', variable: 'KUBECONFIG')]) {
                        sh '''
                          echo ">>> Initializing Terraform"
                          terraform init -input=false

                          echo ">>> Validating Terraform"
                          terraform validate

                          echo ">>> Running Terraform Plan"
                          terraform plan -out=tfplan
                        '''
                    }
                }
            }

            stage('Terraform Apply') {
                when {
                    expression { return config.get('autoApprove', true) }
                }
                steps {
                    withCredentials([file(credentialsId: 'kubeconfig-secret', variable: 'KUBECONFIG')]) {
                        sh '''
                          echo ">>> Applying Terraform Changes"
                          export TF_VAR_KUBECONFIG=$KUBECONFIG
                          echo ">>> Applying Terraform Changes"
                          terraform apply -auto-approve tfplan
                        '''
                    }
                }
            }
        }

        post {
            success {
                echo "✅ Terraform pipeline completed successfully"
            }
            failure {
                echo "❌ Terraform pipeline failed"
            }
        }
    }
}
