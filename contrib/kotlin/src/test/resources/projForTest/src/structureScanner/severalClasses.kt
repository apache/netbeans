package structureScanner

class FirstClass {

	private val prop = 42

	private val prop2 = 1

	private fun method() = prop + prop2

	class InnerClass {
		
		fun hi() = "hi"

		class InnerInnerClass {

			private val INNER = 42
		
		}

	}

}

object SecondObject {

	fun ft() = 42

}
