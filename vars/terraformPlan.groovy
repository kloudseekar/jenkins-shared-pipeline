
def call(action,workingDirectory){
                dir("${env.WORKSPACE}/${workingDirectory}"){

                    if(action == 'destroy') {
                        env.DESTROY = '-destroy'
                    } else {
                        env.DESTROY = ""
                    }
                        sh "terraform plan ${env.DESTROY} -out terraform.tfplan -lock=false"
                        stash name: "terraform-plan", includes: "terraform.tfplan"
                    }
                }

