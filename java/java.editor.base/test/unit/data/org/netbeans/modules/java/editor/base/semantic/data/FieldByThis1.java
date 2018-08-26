package test;

public class FieldByThis1 {
    
    private String s;
    
    public void test1() {
        s = "";
    }
    
    public void test2() {
        System.err.println(this.s);
    }
}
