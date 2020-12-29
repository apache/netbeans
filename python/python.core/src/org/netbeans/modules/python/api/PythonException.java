/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.api;

public class PythonException extends Exception {

    public PythonException(Throwable arg0) {
        super(arg0);
    }

    public PythonException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public PythonException(String arg0) {
        super(arg0);
    }

    public PythonException() {
    }

}
