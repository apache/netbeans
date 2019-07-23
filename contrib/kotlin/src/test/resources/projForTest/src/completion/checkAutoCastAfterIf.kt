package completion

class Num(val value : Int)

fun checkAutoCastAfterIf(obj : Any) {
    if (obj is Num) {
        return obj.<caret>
    }
}