def call(Map config) {
  def WAR_FILE = "${env.APP_NAME}-${env.PROJECT_VERSION}.war"
  PACKAGE = sh (
          script: "echo ${env.PROJECT_GROUP} | sed 's|\\.|/|g'",
          returnStdout: true
  ).trim()
  withCredentials([usernamePassword(credentialsId: "${config.nexusCredential}", passwordVariable: 'NEXUS_PASSWD', usernameVariable: 'NEXUS_USER')]) {
    sh """
                wget --http-user=${NEXUS_USER} --http-password=${NEXUS_PASSWD} ${NEXUS_URL_MDI}/repository/XXX-maven-snapshots-hosted/${PACKAGE}/${PROJECT_NAME}/${PROJECT_VERSION_ORIG}/${WAR_FILE}
            """
  }
  writeFile file: 'Dockerfile', text: """
            FROM tomcat:7.0.57-jre8
            LABEL maintainer="ianashkin@mdi.ru"
            ADD postgresql/postgresql-${config.pgSqlDriverVersion}.jar /usr/local/tomcat/lib/
            ADD ${WAR_FILE} /usr/local/tomcat/webapps/${env.APP_NAME}.war
        """

  docker.withRegistry("", "") {
    image = docker.build("${env.PROJECT_NAME}:${env.PROJECT_VERSION}", ".")
  }

  docker.withRegistry("${DOCKER_REGISTRY}", "${config.nexusCredential}") {
    image.push()
  }
}