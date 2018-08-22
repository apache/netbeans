package org.netbeans.test.java.hints;

public class Simple {
    
    public Simple() {
    }
    
    public static void main(String[] args) {
        Simple s = null;
        
        throw e;
        throw s.e;
        
        synchronized (e) {}
        synchronized (s.e) {}
        
        if (e instanceof Runnable) {}
        if (s.e instanceof Runnable) {}
	
	int f = -s.i;
	byte h = -s.b;
	h = -s.b;
	s.l++;
    }
    
}
