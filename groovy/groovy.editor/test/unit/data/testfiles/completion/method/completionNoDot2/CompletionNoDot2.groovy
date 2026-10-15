class NoCCTest {
    private String method1() {
        return "Ahoj"
    }
    
    public void method2() {
        String a = meth
    }
        
    private String case1() {
        String a = this.method1()
    }
    
    private String case2() {
        return case1()
    }
    
    private String case3() {
        return case2() + case1();
    }
    
    private void case4() {
        if(case1()) {
            case2()
        } else {
            case3();
        }
    }
    
    def m() {
        new File("something").c
        println "Hi"
    }
}

