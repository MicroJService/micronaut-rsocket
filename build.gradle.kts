buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0")
        classpath("io.micronaut.build.internal:micronaut-gradle-plugins:4.0.0")
//        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.18")
    }
}

//repositories {
//    mavenCentral()
//    gradlePluginPortal()
//}

//plugins {
//    val kotlinVersion: String by System.getProperties()
//
////    kotlin("jvm") version kotlinVersion
//
//    id("io.micronaut.build.internal.dependency-updates")
//    id("io.micronaut.build.internal.docs")
//}

val micronautVersion: String by project



//allprojects {
//    repositories {
//        mavenCentral()
//        gradlePluginPortal()
//    }
//}

subprojects {
//    repositories {
//        mavenCentral()
//        gradlePluginPortal()
//    }

    group = "org.microjservice.rsocket"

    apply(plugin = "io.micronaut.build.internal.common")
    apply(plugin = "io.micronaut.build.internal.dependency-updates")

    apply(plugin = "kotlin")
//    apply(plugin = "org.jetbrains.kotlin.jvm")


//    kotlin("jvm") version kotlinVersion
//    app

    apply(plugin = "io.micronaut.build.internal.publishing")

    dependencies {
        val implementation by configurations
        val annotationProcessor by configurations
        val testAnnotationProcessor by configurations

        implementation(kotlin("stdlib"))
        implementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
        annotationProcessor(platform("io.micronaut:micronaut-bom:$micronautVersion"))
        testAnnotationProcessor(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    }


}

apply(plugin = "io.micronaut.build.internal.docs")
apply(plugin = "io.micronaut.build.internal.dependency-updates")