package org.netbeans.test.java.hints;

public class Bug180111 {
    
    public Bug180111() {
    }
    
    public void get(Object o) {
        ((Bug180111) o).create();
    }
    
}
