

//def conf = [path: "codebase/us-east-1-eks",aws_region: params.AWS_REGION, action: params.action, instance_type: params.instance_type , desired_number:params.desired_number]

def call(Map conf) {

properties([
    parameters([
        [$class: 'ChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: 'Select a Region',
            filterLength: 1,
            filterable: false,
            name: 'AWS_REGION',
            randomName: 'choice-parameter-7601235200970',
            script: [$class: 'GroovyScript',
                fallbackScript: [classpath: [], sandbox: true, script: 'return ["ERROR"]'],
                script: [classpath: [], sandbox: true, script: 'return ["us-east-1","us-east-2","ap-south-1"]']
                ]],
        [$class: 'ChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: 'Select Workspace',
            filterLength: 1,
            filterable: false,
            name: 'terraform_workspace',
            randomName: 'choice-parameter-7601235200970',
            script: [$class: 'GroovyScript',
                fallbackScript: [classpath: [], sandbox: true, script: 'return ["ERROR"]'],
                script: [classpath: [], sandbox: true, script: 'return ["dev","qa","prod"]']
                ]],
        [$class: 'ChoiceParameter',
            choiceType: 'PT_SINGLE_SELECT',
            description: 'Terraform Operation',
            filterLength: 1,
            filterable: false,
            name: 'operation',
            randomName: 'choice-parameter-7601235200970',
            script: [$class: 'GroovyScript',
                fallbackScript: [classpath: [], sandbox: true, script: 'return ["ERROR"]'],
                script: [classpath: [], sandbox: true, script: 'return ["init","plan","apply","destroy"]']
                ]],

        string (defaultValue: 't2.micro', name: 'instance_type', trim: true),
        string(name: "desired_number", defaultValue: "1", trim: true, description: "")
        
    ])
    
        
])


pipeline {
    agent {
      label 'linux'
    }
    options {
        ansiColor('xterm')
    }
    
    environment {
        AWS_ACCESS_KEY_ID     = credentials('EKS_AWS_ACCESS_KEY_ID')
        AWS_SECRET_ACCESS_KEY = credentials('EKS_AWS_SECRET_ACCESS_KEY')
        TF_IN_AUTOMATION      = 'true'
    }
    
    stages{
        stage('Git Checkout') {
            steps{
                checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: '*/jenkins-parameterized']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/kloudseekar/infra-as-code.git']]]
                conf.aws_region=params.AWS_REGION
                conf.action=params.action
                conf.instance_type=params.instance_type
                conf.desired_number=params.desired_number
                println conf
            }
        }
        stage('Terraform Init') {
            steps{
                terraformInit()
                }
        }
        stage('Terraform test') {
            steps{
                terraformTest(conf)
                }
            }

        stage('Terraform Plan') {

            steps{
                 terraformPlan(conf)
                }
            }

        stage('Approval'){
            steps{
                input(message: 'Apply Terraform ?')
            }
        }

        stage('Terraform Apply') {

            steps{
                 terraformApply(params.operation)
                }
            }


        stage('Clean WS')
        {
            steps{
            cleanWs()
            }
        }
        }
    }


}










            
        