package completion

class Sum(val left : Any, val right : Any)

fun checkAutoCastInWhen(obj : Any) : Any = when (obj) {
    is Sum -> obj.<caret>
    else -> 0
}