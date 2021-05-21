package foo;

class Hello1 {

    int field1 = 1;
    public publicField = 1
    protected protectedField = 2
    private privateField = 3
    def name = 'World'

    Hello1(int inputval) {
	field1 = inputval
    }

    static void main(args) {
        String s = 'aaa'
        println 'Hello, world'
    }

    void dynamicmethod() {
        field1 = 2
        this.field1 = 77
    }

    def greeting = {
        println "Hello, ${name}!"
    }

}

class SecondTestClass {

    SecondTestClass (int f) {
    }

    SecondTestClass (String str) {
    }
}

class ThirdTestClass {

    ThirdTestClass (int f) {
    }

    ThirdTestClass (String str) {
    }
}

class FourthClass {}

Hello1 hello = new Hello1()
hello.field1 = 9

println "End."

