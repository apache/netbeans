/*
   This is to demonstrate missing OccurencyFinder features:

 1) method usage (method1()      : OK
 2) Class usage (TestCase)       : MISSING
 3) Local vars (localvar[1|2])   : OK
 4) Member vars (membervar1)     : OK
 5) Parameter (param1)           : OK
 */

package foo

println "Starting testcase"
new TestCase().method1()
new TestCase().method1(1)
TestCase c = new TestCase()
c.method1()
c.method1(1)
TestCase.create().method1()
TestCase.create().method1(1)

class TestCase extends java.lang.Object {
    int membervar1 = 2

    TestCase() {

    }

    def method1 (int param1){
        int localvar1 = 3
        int localvar2 = 4

        def localvar3 = membervar1 + param1 + localvar1 + localvar2
        println "Result: " + localvar3
    }

    def method2(){
        method1()
        method1(1)
        this.method1()
        this.method1(1)
        new TestCase()
    }

    static TestCase create() {
        new TestCase()
    }

    def test (Object xy = 0 ) {
        xy = 5

        [1,2,3].each { value ->
            println value+" "+(value*value)
        }
    }

}