def call(Map config) {
  deleteDir()
  currentBuild.displayName = "#${BUILD_NUMBER} ${env.VERSION}"
  def LINK = ''
  env.CHART_VERSION = env.VERSION
  withCredentials([string(credentialsId: "${config.nexusCredential}", variable: 'NEXUS_NPM_USER')]) {
    sh """
                    echo "" > .npmrc
                    echo "registry=${config.nexusUrl}/repository/${config.npmRepositoryHosted}/" >> .npmrc
                    echo "email=any@email.com" >> .npmrc
                    echo "_auth=\\"$NEXUS_NPM_USER\\"" >> .npmrc
                    echo "always-auth=true" >> .npmrc
                """

    nodejs(nodeJSInstallationName: "${env.NODE_JS}") {
      script {
        LINK = sh (
                script: "npm view ${env.APP_NAME}@${env.VERSION} dist.tarball",
                returnStdout: true
        ).trim()
      }
    }

    sh """
                   wget --header="Authorization: Basic ${NEXUS_NPM_USER}" ${LINK}
                """
  }
  sh """
                tar -xf *.tgz
                tar -czvf app.tgz -C package .
                ls -al package
  """
}