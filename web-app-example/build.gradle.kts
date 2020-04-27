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
    implementation( "org.jetbrains.exposed:exposed-java-time")
    implementation("io.github.microutils:kotlin-logging")

    runtimeOnly("com.h2database", "h2", "1.4.200")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
//    testImplementation("com.google.guava:guava:29.0-jre")
//    testImplementation("com.google.code.gson:gson:2.8.6")

    testImplementation("org.jsmart:zerocode-tdd-jupiter")
    testImplementation("org.jsmart:zerocode-tdd")
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
    useJUnitPlatform() {
        systemProperty("zerocode.junit", "gen-smart-charts-csv-reports")
        systemProperty("hostname", "http://localhost:8080")
    }
}