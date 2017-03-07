package quickfixes

fun removeUselessCast(integer: Int) {
    println((integer as Int).toString())
}

fun useRemoveUselessCast() = removeUselessCast(1)
