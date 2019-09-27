package org.netbeans.test.java.hints;

import java.io.File;

public class ArrayInitializer {
    
    public ArrayInitializer() {
    }
    
    public void main(String[] args) {
        Object o = new File[] {f};
        Object x = new File[][] {{f}};
        Object y = new File[i];
    }
}
