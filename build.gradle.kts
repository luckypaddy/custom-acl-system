val kotlinVersion: String by project
val ktorVersion: String by project
val exposedVersion: String by project
val junitVersion: String by project
val kodeinVersion: String by project

group = "com.custom.auth"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
    id("io.spring.dependency-management")
}

configure(subprojects) {
    apply(plugin = "io.spring.dependency-management")

    repositories {
        jcenter()
        mavenCentral()
    }

    dependencyManagement {
        dependencies {
            dependencySet("io.ktor:$ktorVersion") {
                entry("ktor-server-netty")
                entry("ktor-auth")
                entry("ktor-locations")
                entry("ktor-server-sessions")
                entry("ktor-serialization")
            }
            dependencySet("org.jetbrains.exposed:$exposedVersion") {
                entry("exposed-core")
                entry("exposed-dao")
                entry("exposed-jdbc")
                entry("exposed-java-time")
            }
            dependencySet("org.kodein.di:$kodeinVersion") {
                entry("kodein-di-framework-ktor-server-controller-jvm")
                entry("kodein-di-generic-jvm")
            }
            dependencySet("org.junit.jupiter:$junitVersion") {
                entry("junit-jupiter-api")
                entry("junit-jupiter-engine")
            }

            dependency("org.jsmart:zerocode-tdd-jupiter:1.3.18")
            dependency("org.jsmart:zerocode-tdd:1.3.18")
            dependency("com.zaxxer:HikariCP:3.4.2")
            dependency("ch.qos.logback:logback-classic:1.2.3")
            dependency("com.natpryce:konfig:1.6.10.0")
            dependency("org.postgresql:postgresql:42.2.12")
            dependency("io.github.microutils:kotlin-logging:1.7.9")
        }
    }
}
