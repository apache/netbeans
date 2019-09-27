package org.netbeans.test.java.hints;

import java.io.IOException;

public class AddThrowsClause4 {
    
    public AddThrowsClause4() {
    }
    
    public void test() {
        throw exc();
        System.out.println("");
    }
    
    public IOException exc() {
        return new IOException();
    }
}
