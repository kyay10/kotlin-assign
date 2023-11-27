@file:Suppress("UnstableApiUsage")

plugins {
  kotlin("jvm")
  kotlin("kapt")
  id("com.github.gmazzo.buildconfig")
  id("convention.publication")
}

dependencies {
  compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  kapt("com.google.auto.service:auto-service:1.1.1")
  compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")

  // Needed for running tests since the tests inherit out classpath
  implementation(project(":prelude"))

  testImplementation(kotlin("test-junit5"))
  testImplementation(platform("org.junit:junit-bom:5.10.1"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
  testImplementation("dev.zacsweers.kctfork:core:0.4.0")
}

buildConfig {
  packageName(Publication.packageName)
  buildConfigField(
      "String",
      "KOTLIN_PLUGIN_ID",
      "\"${Publication.packageName}\"")
  buildConfigField(
      "String",
      "SAMPLE_JVM_MAIN_PATH",
      "\"${rootProject.projectDir.absolutePath}/sample/src/jvmMain/\"")
}

kotlin {
  jvmToolchain(8)
}

java {
  withSourcesJar()
  withJavadocJar()
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
      version = rootProject.version.toString()
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging { events("passed", "skipped", "failed") }
}
