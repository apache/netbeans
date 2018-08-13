package javahints;

public class TestShortErrorsMethodInvocation1 {
    
    public TestShortErrorsMethodInvocation1() {
        Object o = 1;
        
        test(o);
    }
    
    public void test(Integer i) {}
}
