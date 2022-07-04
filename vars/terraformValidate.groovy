def call(){
    dir("${env.WORKSPACE}/codebase/us-east-1-eks"){
            sh("""
             terraform validate
            """)
    
}
}