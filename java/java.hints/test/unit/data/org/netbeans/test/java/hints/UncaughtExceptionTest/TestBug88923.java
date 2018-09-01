package org.netbeans.test.java.hints;

import java.io.FileNotFoundException;

public class TestBug88923 {
    
    public static void main(String[] args) throws Exc1 {
	Foo.f();
    }

}

class Exc1 extends Exception {}
class Exc2 extends Exception {}
class Foo {
    static void f() throws Exc1,Exc2,FileNotFoundException, IllegalThreadStateException {
    }
}
