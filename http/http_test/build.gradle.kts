
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Test cases for HTTP client and server adapters."

dependencies {
    val junitVersion = properties["junitVersion"]
    val gatlingVersion = properties["gatlingVersion"]
    val slf4jVersion = properties["slf4jVersion"]

    "api"(project(":serialization:serialization"))
    "api"(project(":http:http_client"))
    "api"(project(":http:http_server"))
    "api"("org.jetbrains.kotlin:kotlin-test")
    "api"("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    "api"("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    "api"("org.slf4j:slf4j-jdk14:$slf4jVersion")
    "api"("org.junit.jupiter:junit-jupiter:$junitVersion")
    "api"("io.gatling.highcharts:gatling-charts-highcharts:$gatlingVersion") {
        exclude("ch.qos.logback")
    }
}
