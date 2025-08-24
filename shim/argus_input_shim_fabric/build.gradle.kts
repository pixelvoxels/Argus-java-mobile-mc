plugins {
  id("fabric-loom") version "1.6-SNAPSHOT"
  java
}

group = "com.argus"
version = "0.0.2-alpha"

repositories {
  mavenCentral()
  maven("https://maven.fabricmc.net/")
}

dependencies {
  minecraft("com.mojang:minecraft:1.20.1")
  mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
  modImplementation("net.fabricmc:fabric-loader:0.14.23")
  modImplementation("net.fabricmc.fabric-api:fabric-api:0.90.4+1.20.1")
}

java {
  toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
  withSourcesJar()
}

tasks.processResources {
  inputs.property("version", version)
  filesMatching("fabric.mod.json") {
    expand(mapOf("version" to version))
  }
}