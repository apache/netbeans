package org.netbeans.test.java.hints;

import java.io.File;

public class AddCast8 {
    
    public AddCast8() {
    }
    
    public void getString() {
        Object o = null;
        File f;
        Object t = (f = o);
    }
    
}
