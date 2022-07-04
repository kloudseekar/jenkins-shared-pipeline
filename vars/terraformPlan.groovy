
def call(String action){
                dir("${env.WORKSPACE}/codebase/us-east-1-eks"){
                        sh "terraform plan -out terraform.tfplan -lock=false;echo \$? > status"
                        echo ${action}
                        stash name: "terraform-plan", includes: "terraform.tfplan"
                    }
                }