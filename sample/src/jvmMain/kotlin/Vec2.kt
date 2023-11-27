data class Vec2(var x: Float, var y: Float)

fun Vec2.assign(other: Vec2) {
  x = other.x
  y = other.y
}
fun main() {
  val v = Vec2(1f, 2f)
  v = Vec2(3f, 4f)
  println(v)
}