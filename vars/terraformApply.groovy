
def call(String action){
                dir("${env.WORKSPACE}/codebase/us-east-1-eks"){

                    if(params.Action == 'Destroy') {
                        env.DESTROY = '-destroy'
                    } else {
                        env.DESTROY = ""
                    }

                        unstash "terraform-plan"
                        sh "terraform apply ${env.DESTROY} terraform.tfplan -lock=false -auto-approve ;echo \$? > status"

                    }
                }

