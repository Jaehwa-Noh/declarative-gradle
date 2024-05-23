pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("unified-plugin")
}

plugins {
    id("org.gradle.experimental.android-ecosystem")
    id("org.gradle.experimental.jvm-ecosystem")
    id("org.gradle.experimental.kmp-ecosystem")
    id("org.gradle.experimental.swift-ecosystem")
    id("org.gradle.experimental.cpp-ecosystem")
}

// Not currently supported by declarative model
//rootProject.name = "unified-prototype"

include("android-util")
include("java-util")
include("kotlin-jvm-util")
include("kotlin-util")
include("swift-util")
include("testbed-android-library")
include("testbed-android-application")
include("testbed-kotlin-library")
include("testbed-kotlin-application")
include("testbed-kotlin-jvm-application")
include("testbed-kotlin-jvm-library")
include("testbed-jvm-library")
include("testbed-jvm-application")
include("testbed-java-application")
include("testbed-java-library")
include("testbed-swift-library")
include("testbed-swift-application")
include("testbed-cpp-library")
include("testbed-cpp-application")
