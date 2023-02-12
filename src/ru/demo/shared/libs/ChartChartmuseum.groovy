package ru.demo.shared.libs

class ChartChartmuseum extends AbstractArtifact {

    ChartChartmuseum(String registryUrl, String registryAuth, String artifactVersion, String artifactName) {
        this.registryUrl = registryUrl
        this.registryAuth = registryAuth
        this.artifactVersion = artifactVersion
        this.artifactName = artifactName
    }

    @Override
    void init() {
        RegistryUtils.init(registryUrl + '/api/charts/', registryAuth)
    }

    @Override
    void recursiveGetArtifactVersions(String continuationToken) {
    }

    @Override
    String getMaxVersion() {
        def chartsJson = RegistryUtils.sendGet("${artifactName}")

        chartsJson.each {
            artifactVersions.add(it.version)
        }

        return getMaxVersionFromList(artifactVersions, artifactVersion)
    }
}
