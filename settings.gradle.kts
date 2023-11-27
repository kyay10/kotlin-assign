@file:Suppress("UnstableApiUsage")

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}
rootProject.name = "kotlin-compiler-plugin"

include(":gradle-plugin")

include(":kotlin-plugin")

include("prelude")

include("maven-plugin")

dependencyResolutionManagement { repositories { mavenCentral() } }
