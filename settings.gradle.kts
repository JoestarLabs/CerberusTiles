pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.mikepenz.aboutlibraries.plugin.android") version "14.0.0-b03" apply false
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
