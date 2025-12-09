plugins {
    kotlin("jvm")
    id("com.google.protobuf") version "0.9.5"
    id("com.vanniktech.maven.publish") version "0.35.0"
    signing
}

group = "io.github.sonicalgo"
version = "1.2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.1")
    implementation("com.google.protobuf:protobuf-java:4.33.1")
    implementation("com.google.protobuf:protobuf-kotlin:4.33.1")
}

kotlin {
    jvmToolchain(11)
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

tasks.matching { it.name == "generateMetadataFileForMavenPublication" }.configureEach {
    dependsOn(tasks.matching { it.name == "plainJavadocJar" })
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.33.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("kotlin")
            }
        }
    }
}

signing {
    useGpgCmd()
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        project.extra["signing.gnupg.executable"] = "/opt/homebrew/bin/gpg"
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.sonicalgo",
        artifactId = "upstox-java-sdk",
        version = "1.2.0",
    )

    pom {
        name.set("Upstox Java SDK")
        description.set(
            "Unofficial Kotlin/Java SDK for the Upstox trading platform. " +
                    "Supports REST APIs and real-time WebSocket streaming."
        )
        url.set("https://github.com/SonicAlgo/upstox-java-sdk")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("SonicAlgo")
                name.set("SonicAlgo")
                url.set("https://github.com/SonicAlgo")
            }
        }

        scm {
            url.set("https://github.com/SonicAlgo/upstox-java-sdk")
            connection.set("scm:git:git://github.com/SonicAlgo/upstox-java-sdk.git")
            developerConnection.set("scm:git:ssh://git@github.com/SonicAlgo/upstox-java-sdk.git")
        }
    }

    // Use Maven Central via the plugin's defaults (Central Portal)
    publishToMavenCentral()

    // Enable GPG signing for all publications
    signAllPublications()
}
