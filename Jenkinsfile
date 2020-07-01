/*
 * Copyright 2017-2020 Brambolt ehf.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

node {

  stage('checkout') {
    checkout scm
  }

  stage('build') {

    withCredentials(
      [usernamePassword(
        credentialsId: env.JENKINS_MAVEN_CREDS,
        usernameVariable: 'ORG_GRADLE_PROJECT_artifactoryUser',
        passwordVariable: 'ORG_GRADLE_PROJECT_artifactoryToken'),
       usernamePassword(
        credentialsId: env.JENKINS_VCS_CREDS,
        usernameVariable: 'ORG_GRADLE_PROJECT_vcsUser',
        passwordVariable: 'ORG_GRADLE_PROJECT_vcsToken')]) {

      withEnv([ "GRADLE_OPTS=-Dgradle.user.home=${env.HOME}/.gradle "
      ]) {
        sh 'echo ${GRADLE_OPTS}'
        sh './gradlew clean all -PbuildNumber=${BUILD_NUMBER} --info --stacktrace --no-daemon --refresh-dependencies'
      }
    }
  }
}

