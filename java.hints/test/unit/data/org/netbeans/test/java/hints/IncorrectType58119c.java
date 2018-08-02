package org.netbeans.test.java.hints;

/**
 * @author leon chiver
 */
public class IncorrectType58119c {

    public void doStuff3() {
        e = looong();
    }
    
    private static Long looong() {
        return null;
    }
    
}
