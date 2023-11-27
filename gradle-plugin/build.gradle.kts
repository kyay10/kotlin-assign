@file:Suppress("UnstableApiUsage")

plugins {
  kotlin("jvm")
  id("com.github.gmazzo.buildconfig")
  id("com.gradle.plugin-publish")
  id("convention.publication")
}

dependencies {
  implementation(kotlin("gradle-plugin-api"))
}

buildConfig {
  val project = project(":kotlin-plugin")
  packageName(Publication.packageName)
  buildConfigField(
      "String",
      "KOTLIN_PLUGIN_ID",
      "\"${Publication.packageName}\"")
  buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
  buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project.name}\"")
  buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
  val preludeProject = project(":prelude")
  buildConfigField(
      "String",
      "PRELUDE_LIBRARY",
      "\"${preludeProject.group}:${preludeProject.name}:${preludeProject.version}\"")
}

kotlin {
  jvmToolchain(8)
}

java {
  withSourcesJar()
  withJavadocJar()
}

gradlePlugin {
  website = Publication.url
  vcsUrl = Publication.vcs
  plugins {
    create(Publication.name) {
      id = Publication.id
      displayName = Publication.displayName
      description = Publication.description
      tags = Publication.tags
      implementationClass = "io.github.kyay10.kotlincompilerplugin.MyGradlePlugin"
    }
  }
}