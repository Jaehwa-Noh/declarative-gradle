@file:Suppress("UnstableApiUsage")

plugins {
    id("org.gradle.playground-kmp")
}

repositories {
    mavenCentral()
    google()
}

kmpApplication {
    // Common dependencies
    dependencies {
        implementation(libs.kotlinx.datetime)
    }

    sourceSets {
        jvmMain {
            // Dependencies used only by the JVM target
            dependencies {
                implementation(libs.kotlinx.coroutines)
            }
        }
    }

    // TODO: think not in terms of SOURCESETS, but in terms of TARGET PLATFORMS?
    // Perhaps try to separate sourceSets from targets and join them up with "compilations" that do the mapping?
}