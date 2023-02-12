def call(Map config) {
  nodejs(nodeJSInstallationName: "${env.NODE_JS}") {
    script {
      LINK = sh (
              script: """
                      cd "${WORKSPACE}"
        
                      cmdBuild='build'
                      npm cache clean --force
                      npm i ${config.extraCmd}
        
                      npm run \$cmdBuild --prod
                      ncp ./package.json ./dist/${env.APP_NAME}/package.json && ncp ./.npmrc ./dist/.npmrc && ncp ./package-lock.json ./dist/${env.APP_NAME}/lock.json
                      npm publish ./dist/${env.APP_NAME}   
                    """,
              returnStdout: true
      ).trim()
    }
  }
}