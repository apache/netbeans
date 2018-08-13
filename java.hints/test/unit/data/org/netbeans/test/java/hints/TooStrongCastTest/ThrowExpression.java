/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.test.java.hints.TooStrongCastTest;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ThrowExpression {
    public void castInThrow() throws IOException {
        Exception ex = null;
        if (true) {
            throw (FileNotFoundException)ex;
        } else { 
            throw (IllegalArgumentException)ex;
        }
    }
}
