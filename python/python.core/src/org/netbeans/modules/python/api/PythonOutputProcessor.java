/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.api;

import java.io.IOException;
import org.netbeans.api.extexecution.input.InputProcessor;

public class PythonOutputProcessor implements InputProcessor {
    StringBuilder builder = new StringBuilder();
    @Override
    public void processInput(char[] input) throws IOException {
        builder.append(input);
    }

    @Override
    public void reset() throws IOException {
        //builder = new StringBuilder();
    }

    @Override
    public void close() throws IOException {

    }
    public String getData(){
        return builder.toString();
    }

}
