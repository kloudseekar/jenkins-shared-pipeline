
def call(action,workingDirectory){
                dir("${env.WORKSPACE}/${workingDirectory}"){

                    if(action == 'destroy') {
                        env.DESTROY = '-destroy'
                    } else {
                        env.DESTROY = ""
                    }

                        unstash "terraform-plan"
                        sh "terraform apply ${env.DESTROY} -state=terraform.tfplan -lock=false -auto-approve"

                    }
                }

