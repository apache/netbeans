package org.netbeans.test.java.hints.BroadCatchBlockTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

public class TwoExceptions {
    private static class StableMethod {
        public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {return null;}
    }
    private static class StableClass {
        public StableMethod getDeclaredMethod(String name, Class<?>... types) throws NoSuchMethodException, SecurityException {return null;}
    }
    public void t() {
        StableClass c = new StableClass();
        try {
            StableMethod m = c.getDeclaredMethod("foobar");
            m.invoke(null);
        } /* cmt */ catch (IllegalArgumentException ex) {
            /* cmt */
        } catch (SecurityException ex) {
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
