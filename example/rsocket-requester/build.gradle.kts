plugins {
    val kotlinVersion: String by System.getProperties()
    val micronautPluginVersion: String by System.getProperties()
    val dokkaVersion: String by System.getProperties()

    kotlin("kapt")
    kotlin("plugin.allopen") version kotlinVersion
    id("org.jetbrains.dokka") version dokkaVersion
    id("io.micronaut.application") version "3.0.1"

    id("groovy")
}

micronaut {
    runtime("netty")
    testRuntime("spock2")
    processing {
        incremental(true)
        annotations("com.example.*")
    }
}


val micronautDocsVersion: String by project
val spockVersion: String by project
val micronautTestVersion: String by project
val alibabaRsocketVersion: String by project
val zipkinVersion: String by project
val micronautVersion: String by project


dependencies {
    annotationProcessor("io.micronaut:micronaut-inject-java")
    annotationProcessor("io.micronaut.docs:micronaut-docs-asciidoc-config-props:$micronautDocsVersion")
    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-inject-java:$micronautVersion")
    testAnnotationProcessor("io.micronaut", "micronaut-inject-java")


    implementation(project(":alibaba-rsocket"))
    api(project(":example:user-service-api"))

    implementation("io.micronaut:micronaut-context")
    implementation("io.micronaut:micronaut-management")

    implementation("io.micrometer", "micrometer-registry-prometheus", "1.8.0")
    implementation("com.github.javafaker:javafaker:1.0.2")


    implementation("com.alibaba.rsocket", "alibaba-rsocket-core", alibabaRsocketVersion)
    implementation("io.zipkin.brave", "brave", zipkinVersion)
    testImplementation("org.spockframework:spock-core") {
        exclude(module = "groovy-all")
    }
    testImplementation("io.micronaut.test:micronaut-test-spock")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClass.set("org.microjservice.rsocket.example.RSocketRequesterApp.kt")
}