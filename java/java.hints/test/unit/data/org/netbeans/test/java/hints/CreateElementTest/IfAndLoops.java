package org.netbeans.test.java.hints;

public class IfAndLoops {
    
    public IfAndLoops() {
    }
    
    public static void main(String[] args) {
        IfAndLoops i = null;

        if (a) {}
        if (i.a[0]) {}
        
        while (a) {}
        while (i.a[0]) {}
        
        do {} while (a);
        do {} while (i.a[0]);
        
        for ( ; a ; ) {}
        for ( ; i.a[0] ; ) {}
    }
}
