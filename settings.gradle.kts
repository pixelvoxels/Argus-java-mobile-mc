pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    mavenCentral()
    google()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    google()
  }
}

rootProject.name = "argus-java-monorepo"

include(":shim:argus_input_shim_fabric")