/*
 * AbstractClass.java
 *
 * Created on March 12, 2005, 7:22 PM
 */

package org.netbeans.test.java.hints;

import java.io.IOException;
import javax.swing.text.BadLocationException;

/**
 *
 * @author lahvac
 */
public abstract class AbstractClass {
    
    /** Creates a new instance of AbstractClass */
    public AbstractClass() {
    }
    
    public abstract void test(int x, String y, String ... args) throws IOException, BadLocationException;
    
}
