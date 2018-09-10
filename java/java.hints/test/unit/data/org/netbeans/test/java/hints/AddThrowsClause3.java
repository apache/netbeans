package org.netbeans.test.java.hints;

import java.io.IOException;

public class AddThrowsClause3 {
    
    public AddThrowsClause3() {
    }
    
    public void test() {
        throw (IOException) new IOException().initCause(null);
    }
}
