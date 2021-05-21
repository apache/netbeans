package org.netbeans.test.java.hints;

import java.util.ArrayList;
import java.util.List;

public class AddCast11 {
    
    public AddCast11() {
    }
    
    public <T extends CharSequence> void doSomething() {
        List<CharSequence> l2 = new ArrayList<T>();
    }
    
}
