pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.21"
        id("com.google.devtools.ksp") version "2.2.21-2.0.4"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "upstox-java-sdk"
include("sdk")
include("builder-annotations")
include("builder-processor")
