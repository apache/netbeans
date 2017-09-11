/*
 * AbstractClass.java
 *
 * Created on March 12, 2005, 7:22 PM
 */

package org.netbeans.test.java.hints;
import java.io.File;
import java.io.FileInputStream;

/**
 *
 * @author lahvac
 */
public abstract class TryWrapper5 {
    
    /** Creates a new instance of AbstractClass */
    public TryWrapper5() {
        FileInputStream a, b, c, fis = new FileInputStream(new File("")), d, e, f;
        
        fis.read();
    }
    
}
