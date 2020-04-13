plugins {
    kotlin("jvm")
}

group = "com.custom.auth"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation( "org.jetbrains.exposed:exposed-core")
    implementation( "org.jetbrains.exposed:exposed-jdbc")
    implementation( "com.zaxxer:HikariCP")
    implementation( "ch.qos.logback:logback-classic")
//    implementation("com.natpryce:konfig")
//    runtimeOnly("org.postgresql:postgresql")


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