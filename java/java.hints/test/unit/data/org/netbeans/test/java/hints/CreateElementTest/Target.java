package org.netbeans.test.java.hints;

public class Target {
    
    public Target() {
    }
    
    public static void main(String[] args) {
        Target i = null;
        
        a = 0;
        i.a = 0;
        get().a = 0;
        new Target().a = 0;
    }
    
    private static Target get() {return null;}
}
