/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.project;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;
import org.junit.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * @author Jaroslav Tulach
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.windows.IOProvider.class)
public class InputOutputProviderImpl extends IOProvider {

    static NbTestCase running;
    
    /** Creates a new instance of InputOutputProviderImpl */
    public InputOutputProviderImpl() {}
    
    public static void registerCase(NbTestCase r) {
        running = r;
    }
    
    public InputOutput getIO(String name, boolean newIO) {
        return new IO(name);
    }
    
    public OutputWriter getStdOut() {
        Assert.assertNotNull("A test case must be registered", running);
        return new OW("stdout");
    }
    
    private static class OW extends OutputWriter {
        
        private Logger err;
        
        public OW(String prefix) {
            super(new StringWriter());
            err = Logger.getLogger("test.output." + prefix);
            err.info("create output " + prefix);
        }
        
        public void println(String s, OutputListener l) throws IOException {
            write("println: " + s + " listener: " + l);
            flush();
        }
        
        public void reset() throws IOException {
            write("Internal reset");
            flush();
        }
        
        public void write(char[] buf, int off, int len) {
            write(new String(buf, off, len));
        }
        
        public void write(int c) {
            write(String.valueOf((char)c));
        }
        
        public void write(char[] buf) {
            write(buf, 0, buf.length);
        }
        
        public void write(String s, int off, int len) {
            write(s.substring(off, off + len));
        }
        public void write(String s) {
            err.info(s);
        }
    }
    
    @SuppressWarnings("deprecation") // for flushReader
    private static class IO implements InputOutput {
        
        private OW w;
        private boolean closed;
        
        public IO(String n) {
            w = new OW(n);
            w.write("Created IO named '" + n + "'");
            w.flush();
        }
        
        public OutputWriter getOut() {
            return w;
        }
        
        public Reader getIn() {
            w.write("Creating reader");
            return new StringReader("");
        }
        
        public OutputWriter getErr() {
            return w;
        }
        
        public void closeInputOutput() {
            w.write("closeInputOutput");
            closed = true;
        }
        
        public boolean isClosed() {
            w.write("isClosed");
            return closed;
        }
        
        public void setOutputVisible(boolean value) {
            w.write("setOutputVisible: " + value);
        }
        
        public void setErrVisible(boolean value) {
            w.write("setErrVisible: " + value);
        }
        
        public void setInputVisible(boolean value) {
            w.write("setInputVisible: " + value);
        }
        
        public void select() {
            w.write("select");
        }
        
        public boolean isErrSeparated() {
            return false;
        }
        
        public void setErrSeparated(boolean value) {}
        
        public boolean isFocusTaken() {
            return false;
        }
        
        public void setFocusTaken(boolean value) {}
        
        public Reader flushReader() {
            return getIn();
        }
        
    }
}
