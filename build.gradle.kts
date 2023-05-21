plugins {
    `java-library`
    `maven-publish`
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"
val artifactName = "filepull"

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
            groupId = "com.runicrealms.plugin"
            artifactId = artifactName
            version = "1.0-SNAPSHOT"
            from(components["java"])
        }
    }
}