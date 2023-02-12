/**
 * Метод springbootBuildAndPushDockerImage выполняет сборку Docker образа и сохранение его в Docker Registry.
 *
 * @param projectName
 * @param ProjectVersion
 * @param nexusCredential
 * @param package
 * @param projectVersionOrig
 *
 */
def call(Map config) {
  String baseImageRegistryUrl = config.baseImageRegistryUrl != null ? config.baseImageRegistryUrl : ""
  String baseImageRegistryCredentials = config.baseImageRegistryCredentials != null ? config.baseImageRegistryCredentials : ""
  String dockerBaseImage = config.dockerBaseImage != null ? config.dockerBaseImage : "openjdk:11.0.15-slim-buster"
  JAR_FILE = "${env.PROJECT_NAME}-${env.PROJECT_VERSION}.${env.EXT}"
  withCredentials([usernamePassword(credentialsId: "${config.nexusCredential}", passwordVariable: 'NEXUS_PASSWD', usernameVariable: 'NEXUS_USER')]) {
    sh """
                wget --http-user=${NEXUS_USER} --http-password=${NEXUS_PASSWD} ${NEXUS_URL_MDI}/repository/XXX-maven-snapshots-hosted/${env.PACKAGE}/${env.PROJECT_NAME}/${env.PROJECT_VERSION_ORIG}/${JAR_FILE}
            """
  }
  writeFile file: 'Dockerfile', text: """
            FROM ${dockerBaseImage}
            LABEL maintainer="ianashkin@mdi.ru"
            ADD ${JAR_FILE} /app.jar
        """

  docker.withRegistry(baseImageRegistryUrl, baseImageRegistryCredentials) {
    image = docker.build("${env.PROJECT_NAME}:${env.PROJECT_VERSION}", ".")
  }

  docker.withRegistry("${DOCKER_REGISTRY}", "${config.nexusCredential}") {
    image.push()
  }
}
