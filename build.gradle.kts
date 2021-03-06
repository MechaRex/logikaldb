val kotlinxVersion: String by project
val jacksonVersion: String by project
val foundationDBVersion: String by project
val kotestVersion: String by project

val logikaldbGroupId: String by project
val logikaldbArtifactId: String by project
val logikaldbVersion: String by project

plugins {
    kotlin("jvm") version "1.5.20"
    id("org.jmailen.kotlinter") version "3.4.5"
    id("org.jetbrains.dokka") version "1.5.0"
    id("maven-publish")
    id("signing")
    id("java-library")
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    idea
}

group = logikaldbGroupId
version = logikaldbVersion

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxVersion")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("script-runtime"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:$jacksonVersion")
    implementation("org.foundationdb:fdb-java:$foundationDBVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.get().outputDirectory.get())
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("logikaldb") {
            groupId = logikaldbGroupId
            artifactId = logikaldbArtifactId
            version = logikaldbVersion

            from(components["java"])

            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name.set("logikaldb")
                description.set("Foundational reactive logical database ")
                url.set("https://github.com/MechaRex/logikaldb")
                licenses {
                    license {
                        name.set("GNU Lesser General Public License v3.0")
                        url.set("https://github.com/MechaRex/logikaldb/blob/master/COPYING.LESSER")
                    }
                }
                developers {
                    developer {
                        id.set("robert-toth-mecharex")
                        name.set("Robert Toth")
                        email.set("robert.toth@mecharex.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com:MechaRex/logikaldb.git")
                    developerConnection.set("scm:git:ssh://github.com:MechaRex/logikaldb.git")
                    url.set("https://github.com/MechaRex/logikaldb/tree/master")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

signing {
    sign(publishing.publications)
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
