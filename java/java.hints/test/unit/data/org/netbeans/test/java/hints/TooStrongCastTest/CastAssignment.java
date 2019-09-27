package org.netbeans.test.java.hints.TooStrongCastTest;

import java.util.Collection;
import java.util.List;

public class CastAssignment {
    public void unnecessaryCast() {
        Collection a;
        Collection b = null;
        a = (List)b;
    }
    
    public void strongCast() {
        Collection a;
        Object b = null;
        
        a = (List)b;
    }
    
    public void primitiveCast() {
        long a;
        int b = 2;
        
        a = (long)b;
    }
    
    public void primitiveCastOK() {
        int a;
        long b = 2;
        a = (int)b;
    }
}
