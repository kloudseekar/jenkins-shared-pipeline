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
        string(name: 'BRANCH_NAME', defaultValue: 'main', trim: true, description: 'Git  Branch or  Tag to be dpeloyed')

    ])

])

    pipeline {
        agent {
            label 'linux'
        }
        options {
            ansiColor('xterm')
        }
        tools {
            maven 'Maven-3.8.6'
        }

        environment {
            AWS_ACCESS_KEY_ID     = credentials('EKS_AWS_ACCESS_KEY_ID')
            AWS_SECRET_ACCESS_KEY = credentials('EKS_AWS_SECRET_ACCESS_KEY')
            TF_IN_AUTOMATION      = 'true'
        }

        stages {
            stage('Git Checkout & Setup') {
                steps {
                    checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: params.BRANCH_NAME]], extensions: [], userRemoteConfigs: [[url: conf.giturl]]]
                    script  {
                        conf.aws_region = params.AWS_REGION
                        conf.action = params.operation
                        conf.instance_type = params.instance_type
                        conf.desired_number = params.desired_number
                        conf.branch_name = params.BRANCH_NAME
                        println conf
                    }
                }
            }
            stage('Build  Artifact') {
                environment {
                    // SCANNER_HOME = tool 'SonarQubeScanner'
                    ORGANIZATION = 'ac-maninder'
                    PROJECT_NAME = 'petclinic'
                }
                steps {
                    withSonarQubeEnv('SonarCloud-AC') {
                        sh '''mvn clean package'''
                    }
                }
            }

            stage('SonarCloud') {
                environment {
                    // SCANNER_HOME = tool 'SonarQubeScanner'
                    ORGANIZATION = 'ac-maninder'
                    PROJECT_NAME = 'petclinic-service'
                }
                steps {
                    withSonarQubeEnv('SonarCloud-AC') {
                        sh '''mvn sonar:sonar -Dsonar.organization=$ORGANIZATION \
                                  -Dsonar.java.binaries=target/classes/ \
                                  -Dsonar.projectKey=$PROJECT_NAME \
                                  -Dsonar.sources=. \
                                  -Dsonar.exclusions=src/test/java/****/*.java'''
                    }
                }
            }

            stage('Quality Gate') {
                steps {
                    timeout(time: 1, unit: 'HOURS') {
                        // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                        // true = set pipeline to UNSTABLE, false = don't
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
        }
    }
}

