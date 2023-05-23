val artifactName = "filepull"
val rrGroup: String by rootProject.extra
val rrVersion: String by rootProject.extra

plugins {
    `java-library`
    `maven-publish`
}

group = rrGroup
version = rrVersion

repositories {
    maven("https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient")
    maven("https://mvnrepository.com/artifact/commons-io/commons-io")
}

dependencies {
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.httpclient)
    compileOnly(commonLibs.commonsio)
    compileOnly(commonLibs.spark)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rrGroup
            artifactId = artifactName
            version = rrVersion
            from(components["java"])
        }
    }
}