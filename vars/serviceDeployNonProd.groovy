def call(Map conf) {
    properties([
    parameters([
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
            dockerRegistryUrl     = 'https://hub.docker.com/'
            dockerRegistryCred    = 'dockerhubcred'
            artifactoryRegistryUrl = 'https://myenvpractise.jfrog.io/'
            artifactoryRegistryCred = 'artifactorycred'
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
                        currentBuild.description = 'My custom build description'
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
                    sleep(10)
                    /* groovylint-disable-next-line DuplicateNumberLiteral */
                    timeout(time: 10, unit: 'MINUTES') {
                        // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                        // true = set pipeline to UNSTABLE, false = don't
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
            stage('Build Image') {
                steps {
                        script {
                        dockerImage = docker.build("mnarang2/docker-jenkins-pipeline:${env.BUILD_NUMBER}")
                        }
                }
            }
            stage('Deploy') {
                steps {
                    script {
                        /* groovylint-disable-next-line NestedBlockDepth */
                        docker.withRegistry('', dockerRegistryCred) {
                            dockerImage.push("${env.BUILD_NUMBER}")
                            dockerImage.push('latest')
                        }
                    }
                }
            }
            stage('Deploy Jfrog Artifactory') {
                steps {
                    script {
                        /* groovylint-disable-next-line NestedBlockDepth */
                        docker.withRegistry(artifactoryRegistryUrl, artifactorycred) {
                            dockerImage.push("${env.BUILD_NUMBER}")
                            /* groovylint-disable-next-line DuplicateStringLiteral */
                            dockerImage.push('jfrog')
                        }
                    }
                }
            }
        }
            // post {
            //     always {
            //         steps {
            //             script {
            //             echo 'One way or another, I have finished'
            //             deleteDir() /* clean up our workspace */
            //             }
            //         }
            //     }
            //     success {
            //         steps {
            //             script {
            //                 echo 'I succeeded!'
            //                 // currentBuild.currentResult = 'SUCCESS'
            //                 currentBuild.displayName = conf.appName + currentBuild.currentResult
            //             }
            //         }
            //     }
            //     unstable {
            //         steps {
            //             script {
            //                 echo 'I am unstable :/'
            //                 // currentBuild.currentResult = 'UNSTABLE'
            //                 currentBuild.displayName = conf.appName + currentBuild.currentResult
            //             }
            //         }
            //     }
            //     failure {
            //         steps {
            //             /* groovylint-disable-next-line NestedBlockDepth */
            //             script {
            //                 echo 'I failed :('
            //                 // currentBuild.currentResult = 'FAILED'
            //                 currentBuild.displayName = conf.appName + currentBuild.currentResult
            //             }
            //         }
            //     }
            //     changed {
            //         echo 'Things were different before...'
            //     }
            // }
    }
}

