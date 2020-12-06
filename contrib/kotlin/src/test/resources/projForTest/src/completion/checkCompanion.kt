package completion

class ClassWithCompanion {
    companion object {
        val companionVal = "companion"

        fun companionFun = companionVal
    }

    fun checkCompanion() {
        val check = co<caret>
    }
}