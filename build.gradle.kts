import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.9.24" apply false
    `maven-publish`
    id("com.diffplug.spotless") version "6.25.0" apply false
}

allprojects {
    group = "io.kovertx"
    version = findProperty("kovertx.version") as String

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "maven-publish")
//    apply(plugin = "com.diffplug.spotless")

    publishing {
        repositories {
            maven {
                name = "KovertxReleases"
                url = uri("https://mvn.kovertx.io/kovertx-releases")
                credentials {
                    username = "kovertx_deploy"
                    password = System.getenv("MVN_KOVERTX_IO_TOKEN")
                }
            }
        }
    }

    if (!project.name.endsWith("-bom") && !project.name.endsWith("-gradle-plugin")) {
        apply(plugin = "java")
        apply(plugin = "kotlin")

//        configure<SpotlessExtension> {
//            kotlin {
//                ktfmt().kotlinlangStyle()
//            }
//        }

        dependencies {
            implementation(platform("io.vertx:vertx-stack-depchain:${findProperty("vertx.version")}"))

            testImplementation(platform("org.junit:junit-bom:5.10.2"))
            testImplementation("org.junit.jupiter:junit-jupiter")
            testRuntimeOnly("org.junit.platform:junit-platform-launcher")
            testImplementation("io.vertx:vertx-junit5")
        }

        tasks.test {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
        }

        publishing {
            publications {
                register<MavenPublication>("mavenJava") {
                    from(components["java"])
                }
            }
        }

        java {
            withJavadocJar()
            withSourcesJar()
        }

        tasks.withType<JavaCompile> {
            sourceCompatibility = "17"
            targetCompatibility = "17"
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
}
