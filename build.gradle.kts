plugins {
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-1"
  kotlin("jvm") version libs.versions.kotlin apply false
  id("org.jetbrains.dokka") version "1.9.10" apply false
  id("com.gradle.plugin-publish") version "1.2.1" apply false
  id("com.github.gmazzo.buildconfig") version "4.2.0" apply false
}

group = Developer.groupId

version = Publication.version

subprojects {
  group = Publication.id
  version = Publication.version
}

nexusPublishing {
  repositories {
    sonatype {
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
    }
  }
}
