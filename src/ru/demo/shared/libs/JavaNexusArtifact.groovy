package ru.demo.shared.libs

import static ru.demo.shared.libs.AbstractArtifact.ArtifactBuilder

class JavaNexusArtifact extends AbstractArtifact{
    String artifactGroup

    private JavaNexusArtifact(JavaNexusArtifact artifact) {
        super(artifact)
        this.artifactGroup = artifact.artifactGroup
    }

    JavaNexusArtifact() {}

    static final class Builder extends ArtifactBuilder<JavaNexusArtifact, Builder> {
        public Builder() {
            super(new JavaNexusArtifact())
        }

        Builder(JavaNexusArtifact artifact) {
            super(new JavaNexusArtifact(artifact))
        }

        final Builder artifactGroup(String artifactGroup) {
            artifact.artifactGroup = artifactGroup
            return self()
        }

        JavaNexusArtifact build() {
            return new JavaNexusArtifact(artifact);
        }
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
        RegistryUtils.init(registryUrl, registryApiUrl, registryAuth)
        recursiveGetArtifactVersions(null)
        return getMaxVersionFromList(artifactVersions, artifactVersion)
    }

}
