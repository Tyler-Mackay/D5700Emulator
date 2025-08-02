plugins {
    kotlin("jvm") version "2.0.20"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("D5700EmulatorKt")
}

// Configure the run task to properly handle console input
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }
}