def call( region ,workingDirectory) {
    dir("${env.WORKSPACE}/${workingDirectory}"){
            sh("""
          echo ""
          echo "*************** TERRAFOM INIT ******************"
          echo "******* At environment: ${params.terraform_workspace} ********"
          echo "*************************************************"
            terraform init -backend-config=${region}-config.s3.tfbackend
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

