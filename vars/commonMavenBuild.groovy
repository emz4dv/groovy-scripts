/**
 * Метод commonHelmInstall выполняет установку Helm чарта на удаленном сервере, подключаясь по ssh
 *
 * @param chartVersion  Версия чарта. Пример: 1.0.6
 * @param settingsPath Путь(папка) к файлу конфигурации. Пример: keycloak
 * @param NAMESPACE Глобальный параметр(t_namespace). Наименование namespace для установки в k8s. Пример: cdp-dev-demo-sys
 * @param ARTIFACT_NAME Глобальный параметр(t_artifact). Имя чарта. Пример: keycloak
 * @param APP_NAME Глобальный параметр(t_appName). Имя приложения. Пример: keycloak
 * @param CTX Глобальный параметр(t_ctx). Контекст. Пример: dev
 * @param CHARTMUSEUM_NAME Глобальный параметр(t_chartmuseumName). Имя репозитория в Helm. Пример: cdp-chartmuseum
 * @param HELM_EXTRA Глобальный параметр(t_helm_extra_CTX). Дополнительные переметры для установки Helm чарта. Пример: --timeout
 *
 */
def call(Map config) {
  println env.Q
  env.PROJECT_VERSION_ORIG = readMavenPom(file: 'pom.xml').getVersion()
  env.PROJECT_VERSION = env.PROJECT_VERSION_ORIG
  env.PROJECT_GROUP = readMavenPom(file: 'pom.xml').getGroupId()
  env.PACKAGE = sh (
          script: "echo ${env.PROJECT_GROUP} | sed 's|\\.|/|g'",
          returnStdout: true
  ).trim()
  //currentBuild.displayName = "#${BUILD_NUMBER} ${env.PROJECT_VERSION}"
  withEnv( ["PATH+MAVEN=${tool config.mavenTool}/bin"] ) {
    sh "mvn --version"
    sh "mvn clean deploy -DskipTests=true -U -P maven-jenkins -DaltDeploymentRepository=XXX-maven-snapshots-hosted::default::http://devops-tools.XXX.XX/nexus/repository/XXX-maven-snapshots-hosted/ | tee logMaven.txt"
  }

  def logMaven = new File("${WORKSPACE}/logMaven.txt").text.trim()
  if (env.PROJECT_VERSION.contains('SNAPSHOT')) {
    def m = "${logMaven}" =~ /Uploading to .*\/${env.PROJECT_NAME}-([\d.-]+)\.${env.EXT}/

    if (!logMaven.contains('BUILD SUCCESS')) {
      error('Task failed')
    }

    if (m.find()) {
      env.PROJECT_VERSION = m.group(1)
      env.CHART_VERSION = env.PROJECT_VERSION
    }
  }
  else {
    def m = "${env.PROJECT_VERSION}" =~ /(\d+\.\d+\.\d+)[\.-](.*)/
    if (m.find()) {
      env.CHART_VERSION = m.group(1) + '-' + m.group(2)
    }
    else {
      env.CHART_VERSION = PROJECT_VERSION
    }
  }
}