
package org.netbeans.test.java.editor.codegeneration;

import java.util.List;

/**
 *
 * @author jp159440
 */
public class CreateGetterSetter {    
    
    public int num;
    
    public List<? extends Thread> threads;
    
    public static int statField;

    private boolean bool;

    protected int hasGetter;
    
    public int hasSetter;

    public int getHasGetter() {
        return hasGetter;
    }

    public void setHasSetter(int hasSetter) {
        this.hasSetter = hasSetter;
    }
    
    class Inner {
        String innerFiled;
        
    }
    
    
}
