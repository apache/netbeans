package org.netbeans.test.java.hints;

import java.util.Collection;

public class AddCast9 {
    
    public AddCast9() {
    }
    
    public void getString() {
        Collection<? extends XXX> x = new Y().test(XXX.class);
    }
    
    private static class Y {
        public <T> Collection<? extends T> test(Class<T> c) {}
    }
    
}
