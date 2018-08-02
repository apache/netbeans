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
public abstract class TryWrapper7 {
    
    /** Creates a new instance of AbstractClass */
    public TryWrapper7() {
        FileInputStream a = null, b, c = new FileInputStream(new File("")), fis = new FileInputStream(new File("")), d, e = new FileInputStream(new File("")), f;
        
        fis.read();
    }
    
}
