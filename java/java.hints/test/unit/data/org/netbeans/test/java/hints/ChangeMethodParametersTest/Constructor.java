package org.netbeans.test.java.hints;

public class Constructor {
    
    private Constructor(int i) {
        System.out.println(i);
    }
    
    public void test() {
        new Constructor(3, "Hello World!");
        new Constructor("Hello World!", 3);
        new Constructor("Hello World!", 3, "");
        new Constructor(3, 3, 4);
    }
}
