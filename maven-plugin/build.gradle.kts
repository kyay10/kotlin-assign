@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  kotlin("kapt")
  id("com.github.gmazzo.buildconfig")
  id("convention.publication")
}
dependencies {
  implementation(kotlin("maven-plugin"))
  api(project(":kotlin-plugin"))
  implementation("org.apache.maven:maven-core:3.9.5")
}

// A bit of a hack to copy over the META-INF services information
val servicesDirectory = "META-INF/services"
val copyServices =
    tasks.register<Copy>("copyServices") {
      dependsOn(":kotlin-plugin:kaptKotlin")
      val kotlinPlugin = project(":kotlin-plugin")
      from(kotlinPlugin.kaptGeneratedServicesDir)
      into(kaptGeneratedServicesDir)
    }

buildConfig {
  packageName(Publication.packageName)
  buildConfigField(
      "String",
      "KOTLIN_PLUGIN_ID",
      "\"${Publication.packageName}\"")
}

tasks.withType<KotlinCompile> {
  dependsOn(copyServices)
}

java {
  withSourcesJar()
  withJavadocJar()
}

publishing { publications { create<MavenPublication>("maven") { from(components["java"]) } } }

val Project.kaptGeneratedServicesDir: File
  get() =
      Kapt3GradleSubplugin.getKaptGeneratedClassesDir(this, sourceSets.main.get().name)
          .resolve(servicesDirectory)
