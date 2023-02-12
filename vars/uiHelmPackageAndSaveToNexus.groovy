/**
 * Метод commonHelmInstall выполняет установку Helm чарта на удаленном сервере, подключаясь по ssh
 *
 * @param GIT_URL Глобальный параметр(t_git). Адрес сервера Git. Пример: '${REINFORM_CDP_COMMON_GIT_URL}'
 * @param gitCharts Путь к Git репозиторию с общими helm чартами. Пример: 'DEVOPS/DIT/charts-lib.git'
 * @param chartType Тип используемого helm чарта. Пример: ui
 * @param chartName Имя helm чарта. Пример: 'ui-${ARTIFACT_NAME}'
 * @param chartVersion Версия helm чарта. Пример: 1.0.0
 * @param nexusHelmCredentials Credentials для подключения к серверу Nexus. Пример: 'Nexus-Reinform'
 * @param nexusHelmUrl Адрес для подключения к серверу Nexus. Пример: '${REINFORM_CDP_COMMON_NEXUS_URL}'
 * @param dockerImageName Имя docker образа. Пример: '${DOCKER_REGISTRY}/ui-${ARTIFACT_NAME}'
 * @param dockerImageTag Версия(tag) docker образа. Пример: '${ARTIFACT_VERSION}'
 * @param helm_ver Версия helm. Пример: 3
 * @param sshServer Имя конфигурации PublishOverSSH в Jenkins. Пример: k8s-master
 *
 */

def call(Map config) {
  //debug
  dir ('charts-lib') {
    git url: "${config.gitUrl}/${config.gitCharts}", branch: "dev", credentialsId: "${config.gitCredential}"
    //update Chart.yaml
    def chartYaml = readYaml file: "${config.chartType}/Chart.yaml"
    chartYaml.version = env.CHART_VERSION
    chartYaml.appVersion = env.CHART_VERSION
    chartYaml.name = "ui-${env.APP_NAME}"
    writeYaml file: "${config.chartType}/Chart.yaml", overwrite: "true", data: chartYaml

    //update Values.yaml
    def valuesYaml = readYaml file: "${config.chartType}/values.yaml"
    valuesYaml.image.registry = (new URL("${config.dockerRegistry}")).getHost()
    valuesYaml.image.name = "ui-${env.APP_NAME}"
    valuesYaml.image.tag = env.CHART_VERSION
    writeYaml file: "${config.chartType}/values.yaml", overwrite: "true", data: valuesYaml

    withCredentials([usernamePassword(credentialsId: "${config.nexusCredential}", passwordVariable: 'NEXUS_PASSWD', usernameVariable: 'NEXUS_USER')]) {
      sh """
                  rm -rf target
                  mkdir target
                  helm package ${config.chartType} --dependency-update -d target/
                  cd target
                  curl  --upload-file ui-${env.APP_NAME}-${env.CHART_VERSION}.tgz ${config.nexusUrl}/repository/${config.npmRepositoryHosted}/ -u ${NEXUS_USER}:${NEXUS_PASSWD} -i > upload.log
        
                  #проверка результата
                  cat upload.log
                  if  grep -E "HTTP.* 200 OK" "upload.log" ; then
                           echo 'Chart saved sucsessfully' ;
                  else
                           echo 'Error during save chart to Nexus' ;
                           exit 1
                  fi
                """
    }
  }
  sh('rm -rf charts-lib@tmp')

}