pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // <- añade esta línea
    repositories {
        google()
        mavenCentral()
    }
}


rootProject.name = "App"
include(":app")
