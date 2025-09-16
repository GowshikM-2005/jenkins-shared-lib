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
            stage('Terraform Init & Plan & Apply') {
            steps {
                    withCredentials([file(credentialsId: 'kubeconfig-secret', variable: 'KUBECONFIG')]) {
                        sh '''
                          terraform init
                          terraform plan -out=tfplan
                          terraform apply -auto-approve tfplan
                        '''
              } 
          }
       }

            
        }
    }
}
