/*
 * InnerOutter.java
 *
 * Created on December 13, 2000, 5:30 PM
 */

package org.netbeans.test.editor.completion;

/**
 *
 * @author  jlahoda
 * @version 
 */
public class InnerOutter extends Object {
    
    public class Innerer {
        
        public class Innerest {
            public int test() {
Innerer.this.
            }
        }
        
        public native int yyy();
        void te() {
            
        }
    }

    /** Creates new InnerOutter */
    public InnerOutter() {
    }
    
    public native int xxx();

}
