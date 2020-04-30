plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.custom.auth"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    testImplementation("org.junit.jupiter:junit-jupiter-api")
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

tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
    }
    useJUnitPlatform {
        exclude("**/ScenariosTest.class")
        systemProperty("zerocode.junit", "gen-smart-charts-csv-reports")
        systemProperty("hostname", project.properties["load.test.host"]!!.toString())
        systemProperty("admin_user", project.properties["load.test.admin.user"]!!.toString())
        systemProperty("admin_password", project.properties["load.test.admin.password"]!!.toString())
    }
}
