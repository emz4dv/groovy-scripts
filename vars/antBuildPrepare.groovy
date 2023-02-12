def call(Map config) {
  env.JAVA_HOME="${tool config.jdkTool}"
  env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"

  def buildPath = "${WORKSPACE}/build.xml"
  def buildXml = new XmlSlurper().parse(buildPath)

  env.APP_NAME = buildXml.'@name'.toString()

  buildXml.property.each {
    if (it.'@name' == 'version') {
      env.VERSION = it.'@value'.toString()
    } else if (it.'@name' == 'groupId') {
      env.PROJECT_GROUP = it.'@value'.toString()
    }
  }
}