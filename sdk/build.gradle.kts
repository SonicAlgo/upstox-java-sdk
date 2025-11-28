plugins {
    kotlin("jvm") version "2.2.21"
    id("com.google.protobuf") version "0.9.5"
    `maven-publish`
    signing
}

group = "io.github.sonicalgo"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.google.protobuf:protobuf-java:4.33.1")
    implementation("com.google.protobuf:protobuf-kotlin:4.33.1")
}

kotlin {
    jvmToolchain(11)
}

java {
    withSourcesJar()
    withJavadocJar()
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Upstox Java SDK")
                description.set("Unofficial Kotlin/Java SDK for the Upstox trading platform. Supports REST APIs and real-time WebSocket streaming.")
                url.set("https://github.com/SonicAlgo/upstox-java-sdk")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
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
                    connection.set("scm:git:git://github.com/SonicAlgo/upstox-java-sdk.git")
                    developerConnection.set("scm:git:ssh://github.com:SonicAlgo/upstox-java-sdk.git")
                    url.set("https://github.com/SonicAlgo/upstox-java-sdk")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("ossrhUsername") as String? ?: ""
                password = findProperty("ossrhPassword") as String? ?: ""
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

// Make signing optional for local builds (required for publishing)
tasks.withType<Sign>().configureEach {
    onlyIf { project.hasProperty("signing.keyId") }
}
