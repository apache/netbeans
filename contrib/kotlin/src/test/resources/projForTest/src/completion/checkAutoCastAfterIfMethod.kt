package completion

class Smth {
    fun test() {}
}

fun eval(obj : Any) {
    if (obj is Smth) {
        return obj.<caret>()
    }
}