plugins {
    kotlin("jvm")
}

group = "io.github.sonicalgo"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":builder-annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.2.21-2.0.4")
    implementation("com.squareup:kotlinpoet:2.2.0")
    implementation("com.squareup:kotlinpoet-ksp:2.2.0")
}

kotlin {
    jvmToolchain(11)
}
