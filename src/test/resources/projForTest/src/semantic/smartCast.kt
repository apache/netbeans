package semantic

private fun getNumber(bool: Boolean): Int? = if (bool) 42 else null

fun testSmartCastHighlighting() {
    val value = getNumber(false)
    if (value != null) value + 1
}
