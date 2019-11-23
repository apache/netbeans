/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.test.java.hints.BroadCatchBlockTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 *
 * @author sdedic
 */
public class TwoExceptionsCommon {
    public void t(Method m) {
        try {
            m.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            /* comment */
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
