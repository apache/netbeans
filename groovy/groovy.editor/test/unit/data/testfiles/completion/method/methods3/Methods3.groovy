class GroovyClass1 {
    def method1() {}
    def method2() {}
    def method3() {}
}

class GroovyClass2 {
    def m2() {
        GroovyClass1 localClass = new GroovyClass1()
        localClass.m

    }
}

class GroovyClass3 {
    def m3() {
        println "Hi"
    }
}