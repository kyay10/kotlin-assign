import io.github.kyay10.kotlinassign.assign

var foo = 1
fun main() {
  val bar = ::foo
  bar.assign(2)
  bar = 42
  println(foo)
}