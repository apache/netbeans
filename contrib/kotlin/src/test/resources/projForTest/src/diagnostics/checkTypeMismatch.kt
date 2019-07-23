package diagnostics

interface Base1<T> {}

class Proxy1<T>(t: T) : Base1<T>{
  val value = t
}

fun checkTypeMismatch() {
    val base : Base1<Int> = Proxy1<String>("str")
    base.toString()
}