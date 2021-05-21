package test;

public class FieldByThis2 {
    
    private String s;
    
    public void test1() {
        this.s = "";
    }
    
    public void test2() {
        System.err.println(s);
    }
}
