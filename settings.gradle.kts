pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

stonecutter {
    create(rootProject) {
        fun mc(version: String, vararg loaders: String) = loaders
            .forEach { vers("$version-$it", version).buildscript = "build.$it.gradle.kts" }

        //mc("1.21.11", "fabric")
        mc("1.21.9", "fabric")
        mc("1.21.4", "fabric", "neoforge")
        mc("1.21.1", "fabric", "neoforge")
        mc("1.20.1", "fabric", "forge")

        vcsVersion = "1.20.1-fabric"
    }
}