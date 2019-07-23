package completion

class T(val testParam : Int) {
  val x : Int get() {
      val test = 12
      return te<caret>
  }
}