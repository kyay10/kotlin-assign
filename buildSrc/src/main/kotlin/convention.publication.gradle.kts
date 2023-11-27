plugins {
  `maven-publish`
  signing
}

publishing {
  publications.withType<MavenPublication> {
    pom {
      name = Publication.displayName
      description = Publication.description
      url = Publication.url

      licenses {
        license {
          name = License.name
          url = License.url
        }
      }
      developers {
        developer {
          id = Developer.id
          name = Developer.name
          email = Developer.email
        }
      }
      scm { url = Publication.vcs }
    }
  }
}

signing { sign(publishing.publications) }
