def call(Map config) {
  println env.Q
  env.PROJECT_VERSION_ORIG = VERSION
  env.PROJECT_VERSION = env.PROJECT_VERSION_ORIG

  //currentBuild.displayName = "#${BUILD_NUMBER} ${env.PROJECT_VERSION}"
  withEnv( ["PATH+MAVEN=${tool config.mavenTool}/bin"] ) {
    sh "mvn --version"
    sh "mvn deploy:deploy-file -DgroupId=${env.PROJECT_GROUP} -DartifactId=${env.APP_NAME} -Dversion=${env.VERSION} -DgeneratePom=true -Dpackaging=war -DrepositoryId=XXX-maven-snapshots-hosted -Durl=http://devops-tools.XXX.XX/nexus/repository/XXX-maven-snapshots-hosted/ -Dfile=build/${env.APP_NAME}-${env.VERSION}.war | tee logMaven.txt"
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
      env.CHART_VERSION = env.PROJECT_VERSION
    }
  }
}