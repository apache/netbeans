package diagnostics

interface Base<T> {}

class Proxy<T>(t: T) : Base<T>{
  val value = t
}

fun checkNoTypeMismatch() {
    val base : Base<Int> = Proxy<Int>(5)
    base.toString()
}