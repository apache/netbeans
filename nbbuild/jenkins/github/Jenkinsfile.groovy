/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
pipeline {
    agent { 
        node { 
            label 'ubuntu' 
        }
    }
    
    tools {
      jdk 'jdk_1.8_latest'
      ant 'ant_latest'
    }

    triggers {
        issueCommentTrigger('.*test this please.*')
    }
 
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
 
    environment { 
        ANT_OPTS = '-Dmetabuild.jsonurl=https://raw.githubusercontent.com/apache/netbeans-jenkins-lib/master/meta/netbeansrelease.json'
    }

    stages {
        stage('Check line endings') {
            steps {
                sh 'nbbuild/travis/check-line-endings.sh'
            }
        }
        
        stage('Verify RAT report') {
            steps {
                withAnt {
                    sh 'ant rat'
                }
            }
        }
     
        stage('Verify libs and licenses') {
            steps {
                withAnt {
                    sh 'ant verify-libs-and-licenses'
                }
            }
        }
        
        stage('Build') {
            steps {
                withAnt {
                    sh 'ant build'
                }
            }
        }
     
        stage('Verify sigtests') {
            steps {
                withAnt {
                    sh 'ant check-sigtests-release'
                }
            }
        }
     
        stage('Commit validation') {
            steps {
                wrap([$class: 'Xvfb']) {
                    withAnt {
                        sh 'ant commit-validation'
                    }
                }
            }
        }
    }
 
    post { 
        always { 
            junit 'nbbuild/build/rat/*.xml,nbbuild/build/verifylibsandlicenses.xml,**/test/*/results/TEST*.xml,**/sigtest/check-release/*.xml'
        }
    }
 
}
