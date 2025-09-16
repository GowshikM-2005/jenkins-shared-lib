def call(Map config = [:]){
    pipeline {
        agent any 
       stages {
               stage('Checkout Code') {
                steps {
                    git branch: config.get('gitBranch', 'main'),
                        url: config.get('gitRepo', 'https://github.com/GowshikM-2005/jenkins-shared-lib.git')
                }
            }
         // stage('Terraform Setup'){
         //    steps {
         //        sh """
         //              curl -fsSL https://releases.hashicorp.com/terraform/\${TF_VERSION}/terraform_\${TF_VERSION}_linux_amd64.zip -o terraform.zip
         //              unzip terraform.zip
         //              sudo mv terraform /usr/local/bin/
         //              terraform --version
         //            """
         //    }
         // }
        stage('Terraform Init'){
            steps {
                sh 'terraform init'
            }
        }
        stage('Terraform Plan'){
            steps {
                sh 'terraform plan -out=tfplan'
            }
        }
        stage('Terraform Apply'){
            steps {
                when {
                    expression { return config.get('autoApprove', false) }
                }
                sh 'terraform apply -auto-approve tfplan'
            }    
       }
    }
}
}
