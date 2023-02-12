package ru.demo.shared.libs

abstract class AbstractArtifact {
    protected String registryUrl
    protected String registryAuth

    protected String artifactVersion
    protected String artifactName

    protected List<String> artifactVersions = new ArrayList<>()

//инициализация хранилища артефактов
    abstract void init()

    // сравнение версий
    protected static boolean compareVersions(String ver1, String ver2) {
        int dotsCount1 = 0
        ver1.findAll('\\.', { dotsCount1++ })

        int dotsCount2 = 0
        ver2.findAll('\\.', { dotsCount2++ })

        int maxDots = Math.max(dotsCount1, dotsCount2)

        while (dotsCount1 < maxDots) {
            ver1 += '.0'
            dotsCount1++
        }

        while (dotsCount2 < maxDots) {
            ver2 += '.0'
            dotsCount2++
        }

        List<String> arrayListVer1 = new ArrayList<>()
        Collections.addAll(arrayListVer1, ver1.split('\\.'))

        List<String> arrayListVer2 = new ArrayList<>()
        Collections.addAll(arrayListVer2, ver2.split('\\.'))


        for (int i = 0; i <= maxDots; i++) {

            try {
                int intSubVer1 = Integer.valueOf(arrayListVer1[i])
                int intSubVer2 = Integer.valueOf(arrayListVer2[i])


                if (intSubVer1 != intSubVer2) {
                    return intSubVer1 > intSubVer2
                }
            }catch (NumberFormatException e) {
                println "ERROR: Failed compareVersions \"$ver1\" and \"$ver2\". Cause: " + e.getMessage()
                throw e
            }
        }
        throw new Exception("Version comparison error")

    }

    // получение максимальной версии из списка версий
    protected static String getMaxVersionFromList(List<String> artVersions, String version) {
        if (version.contains('max') || version.contains('-SNAPSHOT')) {

            String maxVersion = '0'
            int snapNumber = 0
            String cropVersion = version.replace('.max', '').replace('-SNAPSHOT', '')

            artVersions.each {
                if (!(it.matches("(\\d+\\.?)+\\d+"))) {
                    //println "Bad version! ($ver)"
                }
                // нахождение максимальной релизной версии
                if ((it.startsWith(cropVersion) && version.contains('max') && !it.contains('-')) || version == 'max') {
                    if (maxVersion == '0' || compareVersions(it, maxVersion)) {
                        maxVersion = it
                    }
                }
                // нахождение максимальной версии snapshot (формат новой версии: релиз-дата.время-номер, старой версии:релиз-дата.время)
                else if (it.startsWith("${cropVersion}-") && version.contains('-SNAPSHOT')) {
                    // убираем релизную часть
                    String snapVersion = it.replace("${cropVersion}-",'')
                    if (maxVersion == '0') {
                        maxVersion = it
                    }
                    //новый формат версии
                    if (snapVersion.split('-').length  == 2) {
                        //пределение макс. номера
                        if (Integer.compare(Integer.parseInt(snapVersion.split('-')[1]), snapNumber) > 0) {
                            snapNumber = Integer.parseInt(snapVersion.split('-')[1])
                        }
                    }
                    // определение последней даты+время
                    if (new BigInteger(it.split('-')[1].replace('.', '')).compareTo(new BigInteger(maxVersion.split('-')[1].replace('.', ''))) > 0) {
                        maxVersion = it
                    }
                }
            }
            if (snapNumber != 0) {
                assert snapNumber == Integer.parseInt(maxVersion.split('-')[2]) : "последний номер отличается от последней даты в версии snapshot"
            }
            return maxVersion
        }

        return version
    }

    // получение списка версий
    abstract void recursiveGetArtifactVersions(String continuationToken)

    // получение максимальной версии
    abstract String getMaxVersion()
}

