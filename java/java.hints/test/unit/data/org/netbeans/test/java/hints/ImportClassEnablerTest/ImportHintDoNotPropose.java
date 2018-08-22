package org.netbeans.test.java.hints;

public class ImportHintDoNotPropose {
    
    /** Creates a new instance of ImportTest */
    public ImportHintDoNotPropose() {
    }
    
    public static void test() {
        NullPointerException();
        throws new NullPointerException();
    }
}
