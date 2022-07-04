
def call(String action){
                dir("${env.WORKSPACE}/codebase/us-east-1-eks"){

                    if(action == 'destroy') {
                        env.DESTROY = '-destroy'
                    } else {
                        env.DESTROY = ""
                    }

                        unstash "terraform-plan"
                        sh "terraform apply ${env.DESTROY} -state=terraform.tfplan -lock=false -auto-approve"

                    }
                }

