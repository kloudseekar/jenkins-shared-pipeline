

def call() {
    dir("${env.WORKSPACE}/codebase/us-east-1-eks"){
            sh("""
          echo ""
          echo "*************** TERRAFOM INIT ******************"
          echo "******* At environment: ${params.terraform_workspace} ********"
          echo "*************************************************"
            terraform init -backend-config=config.s3.tfbackend
            echo '\033[Hello colorful world!\033'
            """)
                    script {
                        try {
                            sh "terraform workspace new ${params.terraform_workspace}"
                        } catch (err) {
                            sh "terraform workspace select ${params.terraform_workspace}"
                        }
                    }
        }

}

