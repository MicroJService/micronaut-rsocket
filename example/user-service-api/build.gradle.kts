//import com.google.protobuf.gradle.protobuf
//import com.google.protobuf.gradle.protoc
import com.google.protobuf.gradle.*

plugins {
    val kotlinVersion: String by System.getProperties()
    val micronautPluginVersion: String by System.getProperties()
    val dokkaVersion: String by System.getProperties()
    java
    idea
    id("com.google.protobuf") version "0.8.18"


}

repositories {
    maven("https://plugins.gradle.org/m2/")
}


val micronautDocsVersion: String by project
val spockVersion: String by project
val micronautTestVersion: String by project
val alibabaRsocketVersion: String by project
val zipkinVersion: String by project
val micronautVersion: String by project
val grpcVersion = "1.42.1"
val protobufJavaVersion = "3.17.2"

dependencies {
//    implementation("io.micronaut.grpc", "micronaut-protobuff-support")
    annotationProcessor("io.micronaut:micronaut-inject-java")
    implementation("com.google.protobuf:protobuf-java")
    implementation("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-stub")

    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation(project(":rsocket-core"))
    implementation("com.alibaba.rsocket", "alibaba-rsocket-service-common", alibabaRsocketVersion)
    implementation("io.cloudevents", "cloudevents-api", "2.2.0")
    implementation("io.micronaut.rxjava2:micronaut-rxjava2")
    implementation("io.micronaut.rxjava3:micronaut-rxjava3")
    // https://mvnrepository.com/artifact/javax.cache/cache-api
    implementation("javax.cache:cache-api:1.1.1")

//    implementation ("io.micronaut.grpc:micronaut-grpc-server-runtime")


}


protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:$protobufJavaVersion"
    }
    plugins {
        // Optional: an artifact spec for a protoc plugin, with "grpc" as
        // the identifier, which can be referred to in the "plugins"
        // container of the "generateProtoTasks" closure.
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.15.1"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
            }
        }
    }
}