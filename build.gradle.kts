plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient")
    maven("https://mvnrepository.com/artifact/commons-io/commons-io")
}

dependencies {
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.spigot)
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.sparkjava:spark-core:2.9.4")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.runicrealms.plugin"
            artifactId = "filepull"
            version = "1.0-SNAPSHOT"
            from(components["java"])
        }
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
//    build {
//        dependsOn(shadowJar)
//    }
}

tasks.register("wrapper")
tasks.register("prepareKotlinBuildScriptModel")