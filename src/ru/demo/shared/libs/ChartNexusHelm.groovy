package ru.demo.shared.libs

class ChartNexusHelm extends AbstractArtifact {
    private String helmRepoName

    ChartNexusHelm(String registryUrl, String registryAuth, String artifactVersion, String artifactName, String helmRepoName) {
        this.registryUrl = registryUrl
        this.registryAuth = registryAuth
        this.artifactVersion = artifactVersion
        this.artifactName = artifactName
        this.helmRepoName = helmRepoName
    }

    @Override
    void init() {
        RegistryUtils.init(registryUrl + '/service/rest/v1/', registryAuth)
    }

    @Override
    void recursiveGetArtifactVersions(String continuationToken) {
        def listJson = RegistryUtils.sendGet("search?name=$artifactName&repository=$helmRepoName"
                + (continuationToken != null ? "&continuationToken=$continuationToken" : ''))

        listJson.items.each {
            artifactVersions.add(it.version)
        }

        if (listJson.continuationToken?.trim()) {
            recursiveGetArtifactVersions(listJson.continuationToken)
        }
    }

    @Override
    String getMaxVersion() {
        recursiveGetArtifactVersions(null)
        return getMaxVersionFromList(artifactVersions, artifactVersion)
    }
}
