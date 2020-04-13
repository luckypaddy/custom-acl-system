val kotlinVersion: String by project
val ktorVersion: String by project
val exposedVersion: String by project
val junitVersion: String by project

group = "com.custom.auth"
version = "1.0-SNAPSHOT"

plugins {
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
                entry("ktor-jackson")
            }
            dependencySet("org.jetbrains.exposed:$exposedVersion") {
                entry("exposed-core")
                entry("exposed-dao")
                entry("exposed-jdbc")
            }
            dependencySet("org.junit.jupiter:$junitVersion") {
                entry("junit-jupiter-api")
                entry("junit-jupiter-engine")
            }
            dependency("com.zaxxer:HikariCP:3.4.2")
            dependency("ch.qos.logback:logback-classic:1.2.3")
            dependency("com.natpryce:konfig:1.6.10.0")
            dependency("org.postgresql:postgresql:42.2.12")
            dependency("io.github.microutils:kotlin-logging:1.7.9")
        }
    }
}
