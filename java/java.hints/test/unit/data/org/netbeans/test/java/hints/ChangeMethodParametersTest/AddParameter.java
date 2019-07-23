package org.netbeans.test.java.hints;

public class AddParameter {
    
    private void method(int i) {
        System.out.println(i);
    }
    
    public void test() {
        method(3, "Hello World!");
        method("Hello World!", 3);
        method("Hello World!", 3, "");
        method(3, 3, 4);
    }
}
