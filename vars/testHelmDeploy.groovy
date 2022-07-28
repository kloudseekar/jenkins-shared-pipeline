def call(Map conf) {
    properties([
    parameters([
        string(name: 'BRANCH_NAME', defaultValue: 'JenkinsFlow', trim: true, description: 'Git  Branch or  Tag to be dpeloyed')
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
            dockerhubInitial       = 'mnarang2'
            artifactoryRegistryUrl = 'https://myenvpractise.jfrog.io/'
            artifactoryRegistryCred = 'artifactorycred'
            artifactoryInitial      = 'myenvpractise.jfrog.io/default-docker-virtual'
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

            stage('Deploy Helm Chart') {
                steps {
                    script {
                        sh 'helm install pet-clinic kubernetes/pet-clinic'
                    }
                }
            }
        }
    }
}
