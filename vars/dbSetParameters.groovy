def call(Map config) {
  def values = readYaml file: "${config.ctx}/${config.appName}.yaml"
  withCredentials([string(credentialsId: "${config.dbCredential}", variable: 'DB_PASSWORD')]) {
    values.db = values.db != null ? values.db : [:]
    values.db.password = "${DB_PASSWORD}"
  }
  writeYaml file: "${config.ctx}/${config.appName}.yaml", overwrite: "true", data: values
}