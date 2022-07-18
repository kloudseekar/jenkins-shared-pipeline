
// def call(String action , String instance_type , String asg_desired_capacity){
//                 dir("${env.WORKSPACE}/codebase/us-east-1-eks"){

//                     if(action == 'destroy') {
//                         env.DESTROY = '-destroy'
//                     } else {
//                         env.DESTROY = ""
//                     }
//                         sh "terraform plan ${env.DESTROY} -out terraform.tfplan -lock=false -var instance_type=${instance_type}  -var asg_desired_capacity=${asg_desired_capacity}"
//                         stash name: "terraform-plan", includes: "terraform.tfplan"
//                     }
//                 }


def call(Map conf){
                dir("${env.WORKSPACE}/${conf.path}"){

                    if(action == 'destroy') {
                        env.DESTROY = '-destroy'
                    } else {
                        env.DESTROY = ""
                    }
                        sh "terraform plan ${env.DESTROY} -out terraform.tfplan -lock=false -var instance_type=${conf.instance_type}  -var asg_desired_capacity=${conf.asg_desired_capacity}"
                        stash name: "terraform-plan", includes: "terraform.tfplan"
                    }
                }