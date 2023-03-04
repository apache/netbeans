/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.netbeans.junit.Log;
import org.openide.util.Exceptions;

/**
 * @author Jaroslav Tulach
 */
public class ExceptionsTest extends TestCase {

    public ExceptionsTest(String testName) {
        super(testName);
    }

    private void assertCleanStackTrace(Throwable t) {
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        t.printStackTrace(pw);
        pw.flush();
        String m = w.toString();
        assertFalse(m.replace("\n", "\\n").replace("\t", "\\t"), m.contains("AnnException"));
        assertFalse(m.replace("\n", "\\n").replace("\t", "\\t"), m.contains("msg"));
    }

    public void testAttachMessage() {
        Exception e = new Exception("Help");
        String msg = "me please";
        
        Exception result = Exceptions.attachMessage(e, msg);

        assertSame(result, e);

        StringWriter w = new StringWriter();
        result.printStackTrace(new PrintWriter(w));

        String m = w.toString();

        if (m.indexOf(msg) == -1) {
            fail(msg + " shall be part of output:\n" + m);
        }

        assertCleanStackTrace(e);
    }
    
    public void testAttachMessageForClassNotFound() {
        Exception e = new ClassNotFoundException("Help");
        String msg = "me please";
        
        Exception result = Exceptions.attachMessage(e, msg);

        assertSame(result, e);

        CharSequence log = Log.enable("", Level.WARNING);
        Exceptions.printStackTrace(e);

        String m = log.toString();

        if (m.indexOf(msg) == -1) {
            fail(msg + " shall be part of output:\n" + m);
        }

        assertCleanStackTrace(e);
    }
    
    public void testLogLevel() {
        Exception e = new IOException("Help");
        
        Exception result = Exceptions.attachSeverity(e, Level.FINE);
        assertSame(e, result);

        class H extends Handler {
            int cnt;
            
            @Override
            public void publish(LogRecord record) {
                assertEquals("Fine is the level", Level.FINE, record.getLevel());
                cnt++;
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
            
        }
        
        H h = new H();
        h.setLevel(Level.ALL);
        Logger L = Logger.getLogger("");
        L.setLevel(Level.ALL);
        L.addHandler(h);
        
        Exceptions.printStackTrace(e);
        L.removeHandler(h);
        assertEquals("Called once", 1, h.cnt);
    }

    public void testAttachLocalizedMessage() {
        Exception e = new Exception("Help");
        String msg = "me please";
        
        Exception expResult = e;
        Exception result = Exceptions.attachLocalizedMessage(e, msg);
        assertEquals(expResult, result);

        String fnd = Exceptions.findLocalizedMessage(e);

        assertEquals("The same msg", msg, fnd);

        assertCleanStackTrace(e);
    }

    public void testAttachLocalizedMessageForClassNFE() {
        Exception e = new ClassNotFoundException("Help");
        String msg = "me please";
        
        Exception expResult = e;
        Exception result = Exceptions.attachLocalizedMessage(e, msg);
        assertEquals(expResult, result);

        String fnd = Exceptions.findLocalizedMessage(e);

        assertEquals("The same msg", msg, fnd);

        assertCleanStackTrace(e);
    }

    public void testAttachLocalizedMessageForClassNFEIfNoMsg() {
        Exception e = new ClassNotFoundException("Help");
        String msg = "me please";
        
        Exception expResult = e;
        Exception result = Exceptions.attachMessage(e, msg);
        assertEquals(expResult, result);

        String fnd = Exceptions.findLocalizedMessage(e);

        assertEquals("No localized msg found", null, fnd);

        assertCleanStackTrace(e);
    }

    public void testAttachLocalizedMessageForWeirdException() {
        class WeirdEx extends Exception {
            public WeirdEx(String message) {
                super(message);
            }

            @Override
            public Throwable getCause() {
                return null;
            }
        }

        Exception e = new WeirdEx("Help");
        String msg = "me please";

        Exception expResult = e;
        Exception result = Exceptions.attachMessage(e, msg);
        assertEquals(expResult, result);

        String fnd = Exceptions.findLocalizedMessage(e);

        assertEquals("No localized msg found", null, fnd);

        assertCleanStackTrace(e);
    }

    public void testAnnotateCNFE() {
        Exception e = new ClassNotFoundException();
        String msg = "text of annotation";

        MyHandler mh = new MyHandler();
        Exceptions.LOG.addHandler(mh);
        
        Exceptions.attachMessage(e, msg);
        Exceptions.printStackTrace(e);

        assertTrue(MyHandler.lastThrowable == e || MyHandler.lastThrowable.getCause() == e);
        
        StringWriter w = new StringWriter();
        MyHandler.lastThrowable.printStackTrace(new PrintWriter(w));
        String stackTrace = w.toString();

        if (!stackTrace.contains(msg)) fail("\'"+msg+"\' not found: "+stackTrace);
    }
    
    public void testAnnotateExceptionWithCNFECause() {
        MyHandler mh = new MyHandler();
        Exceptions.LOG.addHandler(mh);
        
        
        Throwable e = new NoClassDefFoundError();
        e.initCause(new ClassNotFoundException());
        String msg = "some annotation";
    
        Exceptions.attachMessage(e, msg);
        Exceptions.printStackTrace(e);

        assertTrue(MyHandler.lastThrowable == e || MyHandler.lastThrowable.getCause() == e);
        
        StringWriter w = new StringWriter();
        MyHandler.lastThrowable.printStackTrace(new PrintWriter(w));
        String stackTrace = w.toString();

        if (!stackTrace.contains(msg)) fail("\'"+msg+"\' not found: "+stackTrace);
    }    
    
    public void testToStringWithNoLogRecords() {
        Throwable t = new Throwable();
        Exceptions.AnnException ann = Exceptions.AnnException.findOrCreate(t, true);
        assertNotNull(ann.toString());
    }
    
    public static final class MyHandler extends Handler {
        public static final StringBuffer messages = new StringBuffer ();
        
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
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }
        
    } 
    
}
