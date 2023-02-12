/**
 * Метод commonHelmInstall выполняет установку Helm чарта на удаленном сервере, подключаясь по ssh
 *
 * @param appName  Имя чарта. Пример: autoform
 * @param chartVersion Версия чарта. Пример: 1.0.6
 * @param namespace Глобальный параметр(NAMESPACE). Наименование namespace для установки в k8s. Пример: XXX-pg-dev
 * @param ctx
 *
 */

def call(Map config) {

  sh """
          helm repo update
          helm upgrade --install ${config.appName} ${config.helmRepo}/${config.appName} --version ${config.chartVersion} -n ${config.namespace} -f ${config.ctx}/${config.appName}.yaml
        """

}

@Library('shared-lib@dev')
import ru.mdi.shared.lib.AbstractArtifact
import ru.mdi.shared.lib.ChartNexusHelm
node {
  properties([
          parameters([
                  string(
                          defaultValue: '1.0-SNAPSHOT',
                          name: 'VERSION'
                  )
          ])
  ])
  CTX                 = 'dev'
  env.APP_NAME        = 'autoform-webapp'
  GIT_PATH            = 'PROJECT/GGE/RESOURCES/k8s-configs.git'
  NAMESPACE           = 'XXX-pg-dev'

  stage("Git"){
    deleteDir()
    git url: "${GIT_URL_MDI}/${GIT_PATH}", branch: "dev", credentialsId: 'GIT-MDI-GitLab'
  }
  stage('Get max version') {

    // deleteDir()

    ChartNexusHelm artifact = new ChartNexusHelm(
            (String) "http://devops-tools.XXX.XX/nexus",
            null,
            VERSION,
            (String) APP_NAME,
            (String) "XXX-helm-hosted"
    )
    artifact.init()
    VERSION = artifact.getMaxVersion()


  }
  stage("Helm"){
    currentBuild.displayName = "#${BUILD_NUMBER} ${VERSION}"

    dbSetParameters(
            ctx: CTX,
            appName: APP_NAME,
            dbCredential: 'PG_DEV_PASSWORD'
    )

    commonHelmInstall(
            appName: APP_NAME,
            chartVersion: VERSION,
            ctx: CTX,
            namespace: NAMESPACE,
            helmRepo: 'XXX-helm-hosted'
    )
  }
}