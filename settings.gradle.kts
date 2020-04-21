rootProject.name = "custom-acl-system"
include("user-management")
include("web-app-example")

pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("io.spring.dependency-management") version "1.0.9.RELEASE" apply false
    }
}