package completion

val valExternal = "external"

class Some() {
  private val myVal : Int

  init {
    val valInternal = 12
    myVal = va<caret>
  }
}