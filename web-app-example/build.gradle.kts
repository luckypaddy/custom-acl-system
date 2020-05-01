import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "5.2.0"
    application
}

group = "com.custom.auth"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":user-management"))

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-serialization")
    implementation("io.ktor:ktor-auth")
    implementation("io.ktor:ktor-locations")
    implementation("io.ktor:ktor-server-sessions")

    implementation("org.kodein.di:kodein-di-framework-ktor-server-controller-jvm")
    implementation("org.kodein.di:kodein-di-generic-jvm")

    implementation( "com.zaxxer:HikariCP")
    implementation( "org.jetbrains.exposed:exposed-core")
    implementation( "org.jetbrains.exposed:exposed-dao")
    implementation( "org.jetbrains.exposed:exposed-jdbc")
    implementation( "org.jetbrains.exposed:exposed-java-time")
    implementation("io.github.microutils:kotlin-logging")

    runtimeOnly("com.h2database", "h2", "1.4.200")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("io.mockk:mockk")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

application {
    mainClassName = "com.custom.acl.web.demo.MainKt"
}

tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
    }
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClassName
            )
        )
    }
}

tasks.getByName<ProcessResources>("processResources") {
    val jar: Jar by tasks
    val filterTokens = mapOf(
        "postgres.user" to "postgres",
        "postgres.password" to "pwdpwdpwd",
        "postgres.db" to "acl",
        "postgres.hostname" to "testpostgres", //service name of postgres from docker-compose.yml file
        "default.admin.name" to "Admin",
        "default.admin.password" to "securedpwd",
        "artifact.jar.name" to jar.archiveFileName.get(),
        "name" to project.name,
        "version" to project.version)
    from("docker") {
        filter<ReplaceTokens>("tokens" to filterTokens)
    }
    filter<ReplaceTokens>("tokens" to filterTokens)
}

tasks.register<Copy>("copyProcessedResources") {
    include("*.jar", "*docker*","*Docker*")
    from(
        "$buildDir/resources/main/",
        "$buildDir/libs"
    )
    into("$buildDir/artifacts")
}
