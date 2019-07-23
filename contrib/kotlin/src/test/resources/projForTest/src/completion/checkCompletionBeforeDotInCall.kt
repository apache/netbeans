package completion

fun testTop() {

}

class TestSample() {
    fun main(args : Array<String>) {
        val testVar = ""
        test<caret>.testFun()
    }

    fun testFun() {

    }
}