/*
 * AbstractClass.java
 *
 * Created on March 12, 2005, 7:22 PM
 */

package org.netbeans.test.java.hints;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;

/**
 *
 * @author lahvac
 */
public abstract class AbstractClass3 {
    
    /** Creates a new instance of AbstractClass */
    public AbstractClass3() {
    }
    
    public abstract String[] test(Map<String, List<String>> l) throws IOException, BadLocationException;
    
}
