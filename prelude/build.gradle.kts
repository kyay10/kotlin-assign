import org.jetbrains.dokka.gradle.DokkaTask

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.dokka")
  id("convention.publication")
}

val dokkaHtml: DokkaTask by tasks

val javadocJar by
    tasks.registering(Jar::class) {
      dependsOn(dokkaHtml)
      archiveClassifier.set("javadoc")
      from(dokkaHtml.outputDirectory)
    }

kotlin {
  jvm()
  jvmToolchain(8)
  js(IR)
  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  when {
    hostOs == "Mac OS X" -> macosX64()
    hostOs == "Linux" -> linuxX64()
    isMingwX64 -> mingwX64()
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }
}

publishing { publications { publications.withType<MavenPublication> { artifact(javadocJar) } } }

//region Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://github.com/gradle/gradle/issues/26091
tasks.withType<AbstractPublishToMaven>().configureEach {
  val signingTasks = tasks.withType<Sign>()
  mustRunAfter(signingTasks)
}
//endregion