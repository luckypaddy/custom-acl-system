plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
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



    testCompileOnly("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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