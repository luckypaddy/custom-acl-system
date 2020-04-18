plugins {
    kotlin("jvm")
    application
}

group = "com.custom.auth"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":user-management"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-jackson")
    implementation("io.ktor:ktor-server-netty")


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