// def call(Map config = [:]) {
//     pipeline {
//         agent any 

//         stages {
//             stage('Checkout Code') {
//                 steps {
//                     git branch: config.get('gitBranch', 'main'),
//                         url: config.get('gitRepo', 'https://github.com/GowshikM-2005/Terraform.git')
//                 }
//             }

//             stage('Terraform Init & Plan') {
//                 steps {
//                     withCredentials([file(credentialsId: 'kubeconfig-secret', variable: 'KUBECONFIG')]) {
//                         sh '''
//                           # Export kubeconfig path for Terraform variable
//                           export TF_VAR_kubeconfig_path=$KUBECONFIG

//                           echo ">>> Initializing Terraform"
//                           terraform init -input=false

//                           echo ">>> Validating Terraform"
//                           terraform validate

//                           echo ">>> Running Terraform Plan"
//                           terraform plan -out=tfplan
//                         '''
//                     }
//                 }
//             }

//         //     stage('Terraform Apply') {
//         //         when {
//         //             expression { return config.get('autoApprove', true) }
//         //         }
//         //         steps {
//         //             withCredentials([file(credentialsId: 'kubeconfig-secret', variable: 'KUBECONFIG')]) {
//         //                 sh '''
//         //                   # Export kubeconfig path again to ensure it's available
//         //                   export TF_VAR_kubeconfig_path=$KUBECONFIG

//         //                   echo ">>> Applying Terraform Changes"
//         //                   terraform apply -auto-approve tfplan
//         //                 '''
//         //             }
//         //         }
//         //     }
//         // }

//         post {
//             success {
//                 echo "✅ Terraform pipeline completed successfully"
//             }
//             failure {
//                 echo "❌ Terraform pipeline failed"
//             }
//         }    
//     }
// }

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

            stage('Setup Kubeconfig') {
                steps {
                    withCredentials([file(credentialsId: 'kubeconfig-secret', variable: 'KUBECONFIG_FILE')]) {
                        sh '''
                          echo ">>> Setting up kubeconfig"
                          mkdir -p $WORKSPACE/.kube
                          cp $KUBECONFIG_FILE $WORKSPACE/.kube/config
                          export KUBECONFIG=$WORKSPACE/.kube/config
                          echo "KUBECONFIG set to $KUBECONFIG"

                          # Pass kubeconfig path to Terraform variable
                          export TF_VAR_kubeconfig_path=$KUBECONFIG
                        '''
                    }
                }
            }

            stage('Terraform Init & Plan') {
                steps {
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

            stage('Terraform Apply') {
                when {
                    expression { return config.get('autoApprove', true) }
                }
                steps {
                    sh '''
                      echo ">>> Applying Terraform Changes"
                      terraform apply -auto-approve tfplan
                    '''
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
            always {
                sh '''
                  echo ">>> Cleaning up kubeconfig"
                  rm -rf $WORKSPACE/.kube
                '''
            }
        }
    }
}


