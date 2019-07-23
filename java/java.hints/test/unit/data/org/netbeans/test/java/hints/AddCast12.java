package org.netbeans.test.java.hints;

public class AddCast12 {
    
    public AddCast12() {
    }
    
    public char doSomething() {
        Object o = null;
        char   c = 0;
        
        c = o;
        
        char c2 = o;
        
        return o;
    }
    
}
