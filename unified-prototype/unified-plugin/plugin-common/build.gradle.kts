plugins {
    `kotlin-dsl`
    id("build-logic.publishing")
}

description = "Common APIs and implementation classes shared by the ecosystem specific declarative prototypes"

dependencies {
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        create("common") {
            id = "org.gradle.experimental.declarative-common"
            displayName = "Common Experimental Declarative Plugin"
            description = "Experimental declarative plugin containing common code shared by the Android, JVM, and KMP prototype plugins"
            implementationClass = "org.gradle.api.experimental.common.CommonPlugin"
            tags = setOf("declarative-gradle")
        }
    }
}
