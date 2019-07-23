package folding


// Some class
class SomeClass {

	init {

	}

	/* comment  */
	fun doNothing() {

	}

	fun someFun() {
		
		for (i in 0..10) {
			for (j in 10..20) {
				doNothing()		
			}
		}

	}

}
