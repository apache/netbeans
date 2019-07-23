package quickfixes

fun removeUselessElvis(a: Int) {
    a ?: return
}

fun useRemoveUselessElvis() = removeUselessElvis(1)
