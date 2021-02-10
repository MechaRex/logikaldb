val kotlinxVersion: String by project
val jacksonVersion: String by project
val foundationDBVersion: String by project
val kotestVersion: String by project

plugins {
    kotlin("jvm") version "1.4.10"
    id("org.jmailen.kotlinter") version "3.2.0"
    id("org.jetbrains.dokka") version "1.4.20"
    idea
}

group = "com.logikaldb"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("script-runtime"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:$jacksonVersion")
    implementation("org.foundationdb:fdb-java:$foundationDBVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
}

kotlin {
    explicitApiWarning()
}

idea.module {
    isDownloadJavadoc = true
    isDownloadSources = true
}

tasks.withType<Test> {
    useJUnitPlatform()
}
