
def call(region,action,workingDirectory){
                dir("${env.WORKSPACE}/${workingDirectory}"){

                    if(action == 'destroy') {
                        env.DESTROY = '-destroy'
                    } else {
                        env.DESTROY = ""
                    }
                        sh "terraform plan ${env.DESTROY} -out terraform.tfplan -lock=false -var region_id=${region}"
                        stash name: "terraform-plan", includes: "terraform.tfplan"
                    }
                }

