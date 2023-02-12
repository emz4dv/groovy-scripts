def call(Map config) {

  String baseImageRegistryUrl = config.baseImageRegistryUrl != null ? config.baseImageRegistryUrl : ""
  String baseImageRegistryCredentials = config.baseImageRegistryCredentials != null ? config.baseImageRegistryCredentials : ""
  String dockerBaseImage = config.dockerBaseImage != null ? config.dockerBaseImage : "tomcat:7.0.57-jre8"
  def JAR_FILE = "${env.PROJECT_NAME}-${env.PROJECT_VERSION}.${env.EXT}"

  PACKAGE = sh (
          script: "echo ${env.PROJECT_GROUP} | sed 's|\\.|/|g'",
          returnStdout: true
  ).trim()
  withCredentials([usernamePassword(credentialsId: "${config.nexusCredential}", passwordVariable: 'NEXUS_PASSWD', usernameVariable: 'NEXUS_USER')]) {
    sh """
                wget --http-user=${NEXUS_USER} --http-password=${NEXUS_PASSWD} ${NEXUS_URL_MDI}/repository/${config.nexusRepo}/${PACKAGE}/${env.PROJECT_NAME}/${env.PROJECT_VERSION_ORIG}/${JAR_FILE}
            """
  }
  if (config.pgSqlDriverVersion == null) {
    writeFile file: 'Dockerfile', text: """
            FROM ${dockerBaseImage}
            LABEL maintainer="ianashkin@mdi.ru"
            ADD ${JAR_FILE} /usr/local/tomcat/webapps/${env.APP_NAME}.${env.EXT}
        """
  } else {
    writeFile file: 'Dockerfile', text: """
            FROM ${dockerBaseImage}
            LABEL maintainer="ianashkin@mdi.ru"
            ADD postgresql/postgresql-${config.pgSqlDriverVersion}.jar /usr/local/tomcat/lib/
            ADD ${JAR_FILE} /usr/local/tomcat/webapps/${env.APP_NAME}.${env.EXT}
        """
  }
  docker.withRegistry("", "") {
    image = docker.build("${env.PROJECT_NAME}:${env.PROJECT_VERSION}", ".")
  }

  docker.withRegistry("${DOCKER_REGISTRY}", "${config.nexusCredential}") {
    image.push()
  }
}