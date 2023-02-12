package ru.demo.shared.libs

class CommonNexusArtifact extends AbstractArtifact {
    private String artifactGroup

    CommonNexusArtifact(String registryUrl, String registryAuth, String artifactVersion, String artifactName, String artifactGroup) {
        this.registryUrl = registryUrl
        this.registryAuth = registryAuth
        this.artifactVersion = artifactVersion
        this.artifactName = artifactName
        this.artifactGroup = artifactGroup
    }

    @Override
    void init() {
        RegistryUtils.init(registryUrl + '/service/rest/v1/', registryAuth)
    }

    @Override
    void recursiveGetArtifactVersions(String continuationToken) {
        def listJson = RegistryUtils.sendGet("search?group=$artifactGroup&name=$artifactName"
                + (continuationToken != null ? "&continuationToken=$continuationToken" : ''))

        listJson.items.each {
            this.artifactVersions.add(it.version)
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
