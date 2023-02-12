def call(Map config) {
  withAnt(installation: "${config.antTool}", jdk: "${config.jdkTool}") {
    sh """
              javac -version
              ant ${config.buildCmd} 
            """
  }
}