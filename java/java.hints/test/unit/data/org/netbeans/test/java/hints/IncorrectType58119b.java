package org.netbeans.test.java.hints;

/**
 * @author leon chiver
 */
public class IncorrectType58119b {
    
    public void doStuff2() {
        d = IncorrectType58119b.buffer();
    }
    
    private static StringBuffer buffer() {
        return null;
    }
}
