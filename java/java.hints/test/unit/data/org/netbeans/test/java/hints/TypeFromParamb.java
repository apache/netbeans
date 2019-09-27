package org.netbeans.test.java.hints;

import javax.swing.SwingUtilities;

/**
 * @author leon chiver
 */
public class TypeFromParamb {
    
    /** Creates a new instance of TypeFromParama */
    public TypeFromParamb() {
        doStuff(1, SwingUtilities.calculateInnerArea(null, f), 2);
    }
    
    public void doStuff(int i1, Object o, int i2) {
        
    }
    
}
