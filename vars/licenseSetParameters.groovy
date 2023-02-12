def call(Map config) {
  def values = readYaml file: "${config.ctx}/${config.appName}.yaml"
  withCredentials([string(credentialsId: "${config.serialNumber}", variable: 'License_Serial_Number')]) {
    values.app.license.serial = "${License_Serial_Number}"
  }
  withCredentials([string(credentialsId: "${config.password}", variable: 'License_Password')]) {
    values.app.license.password = "${License_Password}"
  }
  writeYaml file: "${config.ctx}/${config.appName}.yaml", overwrite: "true", data: values
}