pluginManagement {
    plugins {
        val micronautPluginVersion: String by System.getProperties()

        id("io.micronaut.application") version micronautPluginVersion
        id("io.micronaut.library") version micronautPluginVersion
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    val kotlinVersion: String by System.getProperties()

//    kotlin("jvm") version kotlinVersion

    id("io.micronaut.build.shared.settings") version "4.2.3"
}

rootProject.name = "micronaut-rsocket"

include("alibaba-rsocket")
include("rsocket-core")
include("example:rsocket-responder")
include("example:user-service-api")
