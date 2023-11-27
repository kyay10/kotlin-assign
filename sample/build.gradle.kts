plugins {
  kotlin("multiplatform") version "2.0.0-Beta1"
  id("io.github.kyay10.kotlin-compiler-plugin") version "0.1.0"
}

group = "io.github.kyay10.kotlin-compiler-plugin"

version = "1.0"

repositories {
  mavenLocal()
  mavenCentral()
}

kotlin {
  jvm()
  jvmToolchain(8)
  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  when {
    hostOs == "Mac OS X" -> macosX64()
    hostOs == "Linux" -> linuxX64()
    isMingwX64 -> mingwX64()
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }
  js(IR)
}
