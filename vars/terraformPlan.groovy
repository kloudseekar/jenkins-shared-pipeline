
def call(String action){
                dir("${env.WORKSPACE}/codebase/us-east-1-eks"){

                    if(params.Action == 'Destroy') {
                        env.DESTROY = '-destroy'
                    } else {
                        env.DESTROY = ""
                    }
                        sh "terraform plan ${env.DESTROY} -out terraform.tfplan -lock=false"
                        stash name: "terraform-plan", includes: "terraform.tfplan"
                    }
                }

