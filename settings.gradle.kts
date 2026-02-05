pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/facebook/meta-wearables-dat-android")
            credentials {
                username = "facebook"
                password = providers.gradleProperty("github_token").orElse(
                    providers.environmentVariable("GITHUB_TOKEN")
                ).orNull
            }
        }
    }
}

rootProject.name = "Meta RayBan Assistant"
include(":app")
