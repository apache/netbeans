package completion

class SomeClass {
    public fun test() {}
}

fun checkLocalLambda() {
    val lambda = { int : Int -> SomeClass() }
    lambda(0).<caret>
}