package org.netbeans.test.java.hints.BroadCatchBlockTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;

public class TwoExceptions {
    public void t() {
        Class c = Collections.class;
        try {
            Method m = c.getDeclaredMethod("foobar");
            m.invoke(null);
        } /* cmt */ catch (IllegalArgumentException | SecurityException ex) {
            /* cmt */
        } catch (ReflectiveOperationException ex) {
        }
    }

    public void u() throws Exception {
        try {
            FileInputStream istm = new FileInputStream("foobar");
            URL url = new URL("foobar");
        } catch (IOException parent) {
             
        }
    }
}
