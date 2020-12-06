package completion

open class MySecondClass() {
}

open class MyFirstClass<T> {

}

class A() : My<caret> {
    public fun test() {
    }
}