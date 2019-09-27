package org.netbeans.test.java.hints.BroadCatchBlockTest;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OneException {
    public void test1() {
        Class c = Collections.class;
        try {
            Method m = c.getDeclaredMethod("foobar");
        } /* cmt */ catch (NoSuchMethodException ex) {
            Logger.getLogger(OneException.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
