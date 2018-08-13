/*
 * AddThrowsClause.java
 *
 * Created on March 12, 2005, 8:29 PM
 */

package org.netbeans.test.java.hints;

import javax.swing.text.BadLocationException;

/**
 *
 * @author lahvac
 */
public class AddThrowsClause1 {
    
    /** Creates a new instance of AddThrowsClause */
    public AddThrowsClause1() {
        throw new BadLocationException("text", 0);
    }
    
}
