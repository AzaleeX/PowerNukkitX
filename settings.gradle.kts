pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io") {
            content {
                includeGroup("com.github.AllayMC")
                includeGroup("com.github.stateless4j")
            }
        }
        maven("https://repo.opencollab.dev/maven-releases/")
        maven("https://repo.opencollab.dev/maven-snapshots/") {
            mavenContent {
                snapshotsOnly()
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.maven.apache.org/maven2/") // Explicit mirror for reliability
        maven("https://repo.powernukkitx.org/releases") {
            content {
                includeGroup("cn.powernukkitx")
            }
        }
        maven("https://jitpack.io") {
            content {
                includeGroup("com.github.AllayMC")
                includeGroup("com.github.stateless4j")
            }
        }
        maven("https://repo.opencollab.dev/maven-releases/") {
            content {
                includeGroup("com.hivemc.leveldb")
                includeGroup("org.cloudburstmc.netty")
                includeGroup("org.powernukkitx")
            }
        }
        maven("https://repo.opencollab.dev/maven-snapshots/") {
            mavenContent {
                snapshotsOnly()
            }
            content {
                includeGroup("org.cloudburstmc.netty")
                includeGroup("org.powernukkitx")
            }
        }
    }
}

rootProject.name = "powernukkitx"

// Enable Gradle enterprise features for better build insights
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
