package javahints;

public class TestShortErrorsNewClass {
    
    public TestShortErrorsNewClass(String s) {
    }
    
    public static void test() {
        Object o = 1;
        
        new TestShortErrorsNewClass(o);
    }
    
}
