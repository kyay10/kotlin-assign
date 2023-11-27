object Publication {
  val name = "kotlin-assign"
  val id = "${Developer.groupId}.$name"
  val packageName = id.replace("-", "")
  val displayName = "Kotlin Assign Plugin"
  val description = "A Kotlin compiler plugin that relaxes the assignment operator"
  val url = "https://github.com/${Developer.id}/$name"
  val vcs = "$url.git"
  val tags = listOf("kotlin")
  val version = "0.1.0"
}

object License {
  val name = "Apache-2.0"
  val url = "http://www.apache.org/licenses/LICENSE-2.0"
}

object Developer {
  val id = "kyay10"
  val groupId = "io.github.$id"
  val name = "Youssef Shoaib"
  val email = "canonballt@gmail.com"
}