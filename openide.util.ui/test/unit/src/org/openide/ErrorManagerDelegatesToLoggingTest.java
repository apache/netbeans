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

package org.openide;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/** Test for general ErrorManager functionality.
 *
 * @author Jaroslav Tulach
 */
public class ErrorManagerDelegatesToLoggingTest extends NbTestCase {

    public ErrorManagerDelegatesToLoggingTest(String testName) {
        super(testName);
    }
    
    protected void setUp () throws IOException {
        assertNull("No ErrorManager in lookup", Lookup.getDefault().lookup(ErrorManager.class));
        
        
        String config = 
            "handlers=" + MyHandler.class.getName() + "\n" +
            ".level=50\n";
        LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(config.getBytes()));

        MyHandler.messages.setLength(0);
    }
    
    /** Test of getDefault method, of class org.openide.ErrorManager. */
    public void testGetDefault() {
        assertNotNull("There has to be a manager", ErrorManager.getDefault ());
    }
    
    /** Test of notify method, of class org.openide.ErrorManager. */
    public void testNotify() {
        Throwable t = new Throwable ();
        ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, t);
        MyHandler.assertNotify (ErrorManager.INFORMATIONAL, t);
        t = new Throwable ();
        ErrorManager.getDefault ().notify (t);
        MyHandler.assertNotify (ErrorManager.EXCEPTION, t);
    }
    
    /** Test of log method, of class org.openide.ErrorManager. */
    public void testLog() {
        ErrorManager.getDefault ().log (ErrorManager.INFORMATIONAL, "A text");
        MyHandler.assertLog (ErrorManager.INFORMATIONAL, "A text");
        ErrorManager.getDefault ().log ("Another text");
        MyHandler.assertLog (ErrorManager.INFORMATIONAL, "Another text");
    }
    
    /** Test of isLoggable method, of class org.openide.ErrorManager. */
    public void testIsLoggable() {
        ErrorManager.getDefault ().isLoggable(ErrorManager.INFORMATIONAL);
        ErrorManager.getDefault ().isLoggable(ErrorManager.INFORMATIONAL + 1);
    }
    
    /** Test of annotate method, of class org.openide.ErrorManager. */
    public void testReturnValues () {
        Throwable t = new Throwable ();
        Throwable value = ErrorManager.getDefault ().annotate(t, ErrorManager.INFORMATIONAL, null, null, null, null);
        assertEquals ("Annotate must return the same exception", t, value);
        
        @SuppressWarnings("deprecation") Throwable _value = ErrorManager.getDefault().copyAnnotation(t, new Throwable());
        assertEquals ("copyAnnotation must return the same exception", t, _value);
        
        value = ErrorManager.getDefault ().attachAnnotations(t, new ErrorManager.Annotation[0]);
        assertEquals ("attachAnnotations must return the same exception", t, value);
        
    }

    public void testThatWeNotifyAnException() throws Exception {
        Throwable t = new NullPointerException("npe");
        Throwable v = new ClassNotFoundException("cnf", t);

        Throwable a = new IOException();
        ErrorManager.getDefault().annotate(a, v);
        ErrorManager.getDefault().notify(a);

        assertEquals("Last throwable is a", a, MyHandler.lastThrowable);
        assertNotNull("And it has a cause", MyHandler.lastThrowable.getCause());

        StringWriter w = new StringWriter();
        MyHandler.lastThrowable.getCause().printStackTrace(new PrintWriter(w));
        String msg = w.toString();

        if (msg.indexOf("npe") == -1) fail("there should be npe: " + msg);
        if (msg.indexOf("cnf") == -1) fail("there should be cnf: " + msg);

    }

    public void testAnnotatedMessagesShallBePresentInPrintStackTrace() throws Exception {
        Throwable t = new NullPointerException("npe");

        ErrorManager.getDefault().annotate(t, ErrorManager.UNKNOWN, "Ahoj Null!", null, null, null);

        StringWriter w = new StringWriter();
        t.printStackTrace(new PrintWriter(w));
        String msg = w.toString();

        if (msg.indexOf("npe") == -1) fail("there should be npe: " + msg);
        if (msg.indexOf("Ahoj Null") == -1) fail("there should be Ahoj Null: " + msg);

    }

    public void testAnnotateCNFE() {
        Exception e = new ClassNotFoundException();
        String msg = "text of annotation";
        
        ErrorManager.getDefault().annotate(e, ErrorManager.EXCEPTION, msg, null, null, null);
        ErrorManager.getDefault().notify(e);

        assertTrue(MyHandler.lastThrowable == e || MyHandler.lastThrowable.getCause() == e);
        
        StringWriter w = new StringWriter();
        MyHandler.lastThrowable.printStackTrace(new PrintWriter(w));
        String stackTrace = w.toString();

        if (!stackTrace.contains(msg)) fail("\'"+msg+"\' not found: "+stackTrace);
    }
    
    public void testAnnotateExceptionWithCNFECause() {
        Throwable e = new NoClassDefFoundError();
        e.initCause(new ClassNotFoundException());
        String msg = "some annotation";
    
        ErrorManager.getDefault().annotate(e, ErrorManager.EXCEPTION, msg, null, null, null);
        ErrorManager.getDefault().notify(e);

        assertTrue(MyHandler.lastThrowable == e || MyHandler.lastThrowable.getCause() == e);
        
        StringWriter w = new StringWriter();
        MyHandler.lastThrowable.printStackTrace(new PrintWriter(w));
        String stackTrace = w.toString();

        if (!stackTrace.contains(msg)) fail("\'"+msg+"\' not found: "+stackTrace);
    }
    
    
    //
    // Manager to delegate to
    //
    public static final class MyHandler extends Handler {
        public static final StringBuffer messages = new StringBuffer ();
        
        private String prefix;
        
        private static int lastSeverity;
        private static Throwable lastThrowable;
        private static String lastText;

        public static void assertNotify (int sev, Throwable t) {
            assertEquals ("Severity is same", sev, lastSeverity);
            assertSame ("Throwable is the same", t, lastThrowable);
            lastThrowable = null;
            lastSeverity = -1;
        }
        
        public static void assertLog (int sev, String t) {
            assertEquals ("Severity is same", sev, lastSeverity);
            assertEquals ("Text is the same", t, lastText);
            lastText = null;
            lastSeverity = -1;
        }

        public void publish(LogRecord record) {
            messages.append(record.getMessage());
            
            lastText = record.getMessage();
            lastThrowable = record.getThrown();

            Level l = record.getLevel();
            if (l.intValue() >= Level.FINEST.intValue()) {
                lastSeverity = ErrorManager.INFORMATIONAL;
            }
            if (l.intValue() >= Level.WARNING.intValue()) {
                lastSeverity = ErrorManager.WARNING;
            }
            if (l.intValue() >= Level.SEVERE.intValue()) {
                lastSeverity = ErrorManager.EXCEPTION;
            }
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }
        
    } 
    
}
