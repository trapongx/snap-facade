import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.collections.plus

val javaSdkVersion: String by project

plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.runninglane"
version = "1.7.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))

}


tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaSdkVersion.toInt()))
    }

    // Configure JSR-305 strict mode for proper nullability handling
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            // Enable JSR-305 strict mode for proper nullability with Java interop
            freeCompilerArgs += "-Xjsr305=strict"
        }
    }
}

// Configure Java plugin first to properly enable withSourcesJar
java {
    withSourcesJar()
}

// Add publishing configuration
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}