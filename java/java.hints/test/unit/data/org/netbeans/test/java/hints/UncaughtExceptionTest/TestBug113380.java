package org.netbeans.test.java.hints;

import java.io.FileReader;

public class TestBug113380<T extends Throwable> {
    void f() throws T  {
    }
    
    static <T extends Throwable> void g() throws T {
    }
    
    public static void main(String[] args) {
	new TestBug113380<InstantiationException>().f();
	TestBug113380.<IllegalAccessException>g();
    }

}

class aa {
    aa() {
        new FileReader("").read();
    }
}
