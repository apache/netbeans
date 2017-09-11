/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.apisupport.project;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
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
