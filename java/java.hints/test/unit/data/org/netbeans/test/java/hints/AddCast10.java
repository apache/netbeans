package org.netbeans.test.java.hints;

import java.util.Collection;

public class AddCast10 {
    
    public AddCast10() {
    }
    
    private Collection<String> keys;
    
    public void getString() {
        String s =  keys.toArray()[1];
    }
    
}
