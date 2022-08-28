plugins {
    val kotlinVersion: String by System.getProperties()
    val micronautPluginVersion: String by System.getProperties()
    val dokkaVersion: String by System.getProperties()

//    id("io.micronaut.library") version micronautPluginVersion


//    kotlin("jvm") version kotlinVersion
    kotlin("kapt")
    kotlin("plugin.allopen") version kotlinVersion

}


val micronautVersion: String by project
val alibabaRsocketVersion: String by project



dependencies {
    kapt("io.micronaut:micronaut-inject-java:$micronautVersion")

    api( "io.micronaut:micronaut-inject")
    api( "io.micronaut:micronaut-runtime")
    api( "io.micronaut:micronaut-aop")

    implementation("jakarta.inject:jakarta.inject-api")

    implementation("com.alibaba.rsocket", "alibaba-rsocket-core", alibabaRsocketVersion)

}

