plugins {
    val kotlinVersion: String by System.getProperties()
    val micronautPluginVersion: String by System.getProperties()
    val dokkaVersion: String by System.getProperties()

    id("io.micronaut.library") version micronautPluginVersion

    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    id("org.jetbrains.dokka") version dokkaVersion

    id("groovy")
    id("maven-publish")

}


val micronautDocsVersion: String by project
val spockVersion: String by project
val micronautTestVersion: String by project
val alibabaRsocketVersion: String by project
val zipkinVersion: String by project

dependencies {
    annotationProcessor("io.micronaut:micronaut-inject-java")
    annotationProcessor("io.micronaut.docs:micronaut-docs-asciidoc-config-props:$micronautDocsVersion")

    api("io.micronaut:micronaut-inject")

    implementation("com.alibaba.rsocket", "alibaba-rsocket-core", alibabaRsocketVersion)
    implementation("io.zipkin.brave","brave",zipkinVersion)
    testImplementation("org.spockframework:spock-core:${spockVersion}") {
        exclude(module = "groovy-all")
    }
    testImplementation("io.micronaut.test:micronaut-test-spock:$micronautTestVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
