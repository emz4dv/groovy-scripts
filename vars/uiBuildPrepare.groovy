def call(Map config) {

  def packageJSON = readJSON file: 'package.json'
  env.APP_NAME = packageJSON.name
  env.VERSION = packageJSON.version
  echo "APP_NAME = ${env.APP_NAME}, VERSION = ${env.VERSION}"
  currentBuild.displayName = "#${BUILD_NUMBER} ${env.VERSION}"
  withCredentials([string(credentialsId: "${config.nexusCredential}", variable: 'NEXUS_NPM_USER')]) {
    sh """
                    echo "" > .npmrc
                    echo "registry=${config.nexusUrl}/repository/${config.npmRepositoryGroup}/" >> .npmrc
                    echo "email=any@email.com" >> .npmrc
                    echo "_auth=${NEXUS_NPM_USER}" >> .npmrc
                    echo "always-auth=true" >> .npmrc
                  """
  }
  // env.NODEJS_HOME = "${tool 'node_12_12'}"
  // env.PATH="${env.NODEJS_HOME}/bin:${env.PATH}"
  nodejs(nodeJSInstallationName: "${env.NODE_JS}") {
    script {
      LINK = sh(
              script: """
                      npm --version
                      node --version
                      cd "${WORKSPACE}"
                      url=`npm view ${env.APP_NAME}@${env.VERSION} dist.tarball || echo ''`
  
                      if [ ! -z "\$url" ]
                      then
                          echo "Version ${env.VERSION} already exists"
                          #exit 1
                      fi
              
                      find "${WORKSPACE}/." -type f -name package-lock.json -delete
              
                      nexusUrl="${config.nexusUrl}/repository/${config.npmRepositoryHosted}/" && find "${WORKSPACE}/." -type f -name package.json | grep -v node_modules | xargs sed -i "s#\\"registry.*\\"#\\"registry\\": \\"\$nexusUrl\\"#"
                      """,
              returnStdout: true
      ).trim()
    }
  }
}