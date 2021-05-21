package org.netbeans.test.java.hints;

public class TestDuplicate {
public static void main(String[] args) {
        new Foo().fooMethod();
    }
}

class Exc1 extends Exception {}
class Exc2 extends Exception {}

class Foo {
    public Foo() throws Exc1 {
        
    }
    
    public void fooMethod() throws Exc2 {
        
    }
}
