package ru.demo.shared.libs

import groovy.json.JsonSlurper

class RegistryUtils {
    private static String apiUrl
    private static String auth = null

    static void init(String apiUrl) {
        this.apiUrl = apiUrl
    }

    static void init(String apiUrl, String auth) {
        this.apiUrl = apiUrl
        this.auth = auth
    }

//    @NonCPS
    static Object sendGet(String uri) {
//        if (!uri.contains('?')) {
//            uri += '?'
//        }

        def url = new URL("${apiUrl}${uri}")
        HttpURLConnection connection = url.openConnection()
        String result

        try {
            connection.setDoOutput(true)
            connection.setRequestMethod("GET")
            if (auth != null) {
                connection.setRequestProperty("Authorization", "Basic " + auth)
            }

            result = connection.getContent().getText()

            return new JsonSlurper().parse(result.getBytes('UTF-8'), 'UTF-8')
        } catch (IOException e) {
            throw new IOException(e.getMessage() + "; error message: " + connection.getErrorStream().getText("UTF-8"))
        }
    }

}