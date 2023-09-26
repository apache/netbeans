class A {
}

/**
 * This class definition should generate a "missing semicolon" hint, as the
 * explicit constructor really has not semicolon
 */
class B extends A {
    constructor() {
        super()
    }
}

