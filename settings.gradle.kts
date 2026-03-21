pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
    id("com.mikepenz.aboutlibraries.plugin") version "11.6.3" apply false
}
/*'getRepositoriesMode()' is marked unstable with @Incubating*/
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Custom Tiles"
include(":app")
