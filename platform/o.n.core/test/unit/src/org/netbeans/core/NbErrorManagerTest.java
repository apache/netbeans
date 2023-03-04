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

package org.netbeans.core;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.startup.TopLogging;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.xml.sax.SAXParseException;

/**
 * Test the core error manager impl.
 * @author Jesse Glick
 * @see "#18141"
 */
public final class NbErrorManagerTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NbErrorManagerTest.class);
    }

    public NbErrorManagerTest(String s) {
        super(s);
    }

    private ErrorManager err;
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        MockServices.setServices(MockDD.class);
        
        System.setProperty("netbeans.user", getWorkDirPath());
        // init the whole system
        TopLogging.initializeQuietly();


        err = ErrorManager.getDefault();
        assertNotNull("One Error manager found", err);
    }

    @Override
    protected int timeOut() {
        return 10000;
    }
    
    public void testIsLoggable() {
        assertFalse(ErrorManager.getDefault ().isLoggable(ErrorManager.INFORMATIONAL));
        assertFalse(ErrorManager.getDefault ().isLoggable(ErrorManager.INFORMATIONAL + 1));
        assertTrue(ErrorManager.getDefault ().isLoggable(ErrorManager.WARNING + 1));
    }
    
    public void testBasicNotify() throws Exception {
        assertTrue(err.isNotifiable(ErrorManager.EXCEPTION));
        NullPointerException npe = new NullPointerException("unloc msg");
        err.notify(ErrorManager.INFORMATIONAL, npe);
        String s = readLog();
        assertTrue(s.indexOf("java.lang.NullPointerException: unloc msg") != -1);
        assertTrue(s.indexOf("testBasicNotify") != -1);
    }
    
    public void testLog() throws Exception {
        assertFalse(err.isLoggable(ErrorManager.INFORMATIONAL));
        err.log("some msg");
        String s = readLog();
        assertTrue(s.indexOf("some msg") == -1);
        assertTrue(err.isLoggable(ErrorManager.WARNING));
        err.log(ErrorManager.WARNING, "another msg");
        s = readLog();
        assertTrue(s.indexOf("another msg") != -1);
        ErrorManager err2 = err.getInstance("foo.bar.baz");
        assertFalse(err2.isLoggable(ErrorManager.INFORMATIONAL));
        err2.log("sub msg #1");
        s = readLog();
        assertTrue(s.indexOf("sub msg #1") == -1);
        System.setProperty("quux.hoho.level", "0");

        LogManager.getLogManager().readConfiguration();

        err2 = err.getInstance("quux.hoho.yaya");
        assertTrue(err2.isLoggable(ErrorManager.INFORMATIONAL));
        err2.log("sub msg #2");
        s = readLog();
        assertTrue(s, s.indexOf("sub msg #2") != -1);
        assertTrue(s, s.indexOf("quux.hoho.yaya") != -1);
    }
    
    /** @see "#15611" */
    public void testNestedThrowables() throws Exception {
        NullPointerException npe = new NullPointerException("unloc msg");
        ClassNotFoundException cnfe = new ClassNotFoundException("other msg", npe);
        err.notify(ErrorManager.INFORMATIONAL, cnfe);
        String s = readLog();
        assertTrue(s.indexOf("java.lang.NullPointerException: unloc msg") != -1);
        assertTrue(s.indexOf("java.lang.ClassNotFoundException") != -1);
        npe = new NullPointerException("msg1");
        IOException ioe = new IOException("msg2");
        ioe.initCause(npe); // only works in right order with initCause, not ErrorManager.annotate
        InvocationTargetException ite = new InvocationTargetException(ioe, "msg3");
        IllegalStateException ise = new IllegalStateException("msg4");
        ise.initCause(ite);
        err.notify(ErrorManager.INFORMATIONAL, ise);
        s = readLog();
        assertTrue(s, s.indexOf("java.lang.NullPointerException: msg1") != -1);
        assertTrue(s, s.indexOf("java.io.IOException: msg2") != -1);
        assertTrue(s.indexOf("msg3") != -1);
        assertTrue(s, s.indexOf("java.lang.IllegalStateException: msg4") != -1);
        // #91541: check that stack traces are printed in a pleasant order.
        assertTrue(s, s.indexOf("java.lang.NullPointerException: msg1") < s.indexOf("java.io.IOException: msg2"));
        assertTrue(s, s.indexOf("java.io.IOException: msg2") < s.indexOf("msg3"));
        assertTrue(s, s.indexOf("msg3") < s.indexOf("java.lang.IllegalStateException: msg4"));
    }
    
    public void testNotifyWithAnnotations() throws Exception {
        NullPointerException npe = new NullPointerException("unloc msg");
        err.annotate(npe, "loc msg #1");
        err.notify(ErrorManager.INFORMATIONAL, npe);
        String s = readLog();
        assertTrue(s.indexOf("java.lang.NullPointerException: unloc msg") != -1);
        assertTrue(s, s.indexOf("loc msg #1") != -1);
        npe = new NullPointerException("unloc msg");
        err.annotate(npe, ErrorManager.UNKNOWN, "extra unloc msg", null, null, null);
        err.notify(ErrorManager.INFORMATIONAL, npe);
        s = readLog();
        assertTrue(s.indexOf("extra unloc msg") != -1);
        npe = new NullPointerException("new unloc msg");
        IOException ioe = new IOException("something bad");
        err.annotate(ioe, npe);
        err.notify(ErrorManager.INFORMATIONAL, ioe);
        s = readLog();
        assertTrue(s.indexOf("java.lang.NullPointerException: new unloc msg") != -1);
        assertTrue(s.indexOf("java.io.IOException: something bad") != -1);
    }
    
    public void testDeepAnnotations() throws Exception {
        Exception e1 = new Exception("msg1");
        // #19114: deeply nested loc msgs should be used
        err.annotate(e1, "some loc msg");
        Exception e2 = new Exception("msg2");
        err.annotate(e2, e1);
        Exception e3 = new Exception("msg3");
        err.annotate(e3, e2);
        Exception e4 = new Exception("msg4");
        err.annotate(e3, e4);
        err.notify(ErrorManager.INFORMATIONAL, e3);
        String s = readLog();
        assertTrue(s, s.indexOf("java.lang.Exception: msg1") != -1);
        assertTrue(s.indexOf("java.lang.Exception: msg2") != -1);
        assertTrue(s.indexOf("java.lang.Exception: msg3") != -1);
        assertTrue(s.indexOf("java.lang.Exception: msg4") != -1);
        assertTrue(s.indexOf("some loc msg") != -1);
    }
    
    /** @see "#19487" */
    public void testLoops() throws Exception {
        Exception e1 = new Exception("msg1");
        Exception e2 = new Exception("msg2");
        err.annotate(e2, e1);
        Exception e3 = new Exception("msg3");
        err.annotate(e3, e2);
        err.annotate(e1, e3);
        err.notify(ErrorManager.INFORMATIONAL, e1);
        String s = readLog();
        assertTrue(s.indexOf("java.lang.Exception: msg1") != -1);
        assertTrue(s, s.indexOf("java.lang.Exception: msg2") != -1);
        assertTrue(s.indexOf("java.lang.Exception: msg3") != -1);
        // warning from NBEM itself:
        assertTrue(s.indexOf("cyclic") != -1);
    }
    
    public void testAddedInfo() throws Exception {
        MissingResourceException mre = new MissingResourceException("msg1", "the.class.Name", "the-key");
        err.notify(ErrorManager.INFORMATIONAL, mre);
        String s = readLog();
        assertTrue(s.indexOf("java.util.MissingResourceException: msg1") != -1);
        assertTrue(s.indexOf("the.class.Name") != -1);
        assertTrue(s.indexOf("the-key") != -1);
        SAXParseException saxpe = new SAXParseException("msg2", "pub-id", "sys-id", 313, 424);
        err.notify(ErrorManager.INFORMATIONAL, saxpe);
        s = readLog();
        assertTrue(s.indexOf("org.xml.sax.SAXParseException") != -1);
        assertTrue(s.indexOf("msg2") != -1);
        assertTrue(s.indexOf("pub-id") != -1);
        assertTrue(s.indexOf("sys-id") != -1);
        assertTrue(s.indexOf("313") != -1);
        assertTrue(s.indexOf("424") != -1);
    }
    
    /**
     * Actually just tests the same code used when running NE.
     */
    public void testNotifyException() throws Exception {
        IOException ioe = new IOException("unloc msg");
        err.annotate(ioe, "loc msg");
        NbErrorManager.Exc x = NbErrorManager.createExc(ioe, Level.INFO, null);
        assertEquals(Level.INFO, x.getSeverity());
        assertEquals("loc msg", x.getLocalizedMessage());
        assertTrue(x.isLocalized());
        // could do more here...
    }
    
    /**
     * Check that UNKNOWN works.
     * @see "#30947"
     */
    public void testUnknownSeverity() throws Exception {
        
        // Simple exception is EXCEPTION.
        Throwable t = new IOException("unloc msg");
        NbErrorManager.Exc x = NbErrorManager.createExc(t, null, null);
        assertEquals(Level.WARNING, x.getSeverity());
        assertEquals("unloc msg", x.getMessage());
        assertEquals("unloc msg", x.getLocalizedMessage());
        assertFalse(x.isLocalized());
        
        // Same when there is unloc debug info attached.
        t = new IOException("unloc msg");
        err.annotate(t, ErrorManager.UNKNOWN, "some debug info", null, null, null);
        x = NbErrorManager.createExc(t, null, null);
        assertEquals(Level.WARNING, x.getSeverity());
        assertEquals("unloc msg", x.getMessage());
        assertEquals("unloc msg", x.getLocalizedMessage());
        assertFalse(x.isLocalized());
        
        // Nested exceptions don't necessarily change anything severity-wise.
        t = new IOException("unloc msg");
        Throwable t2 = new IOException("unloc msg #2");
        err.annotate(t, ErrorManager.UNKNOWN, null, null, t2, null);
        x = NbErrorManager.createExc(t, null, null);
        assertEquals(Level.WARNING, x.getSeverity());
        assertEquals("unloc msg", x.getMessage());
        assertEquals("unloc msg", x.getLocalizedMessage());
        assertFalse(x.isLocalized());
        
        // But annotations at a particular severity level (usually localized) do
        // set the severity for the exception.
        t = new IOException("unloc msg");
        err.annotate(t, ErrorManager.USER, null, "loc msg", null, null);
        x = NbErrorManager.createExc(t, null, null);
        assertEquals(1973, x.getSeverity().intValue());
        assertEquals("unloc msg", x.getMessage());
        assertEquals("loc msg", x.getLocalizedMessage());
        assertTrue(x.isLocalized());
        
        // And that works even if you are just rethrowing someone else's exception.
        t = new IOException("unloc msg");
        t2 = new IOException("unloc msg #2");
        err.annotate(t2, ErrorManager.USER, null, "loc msg", null, null);
        err.annotate(t, ErrorManager.UNKNOWN, null, null, t2, null);
        x = NbErrorManager.createExc(t, null, null);
        assertEquals(1973, x.getSeverity().intValue());
        assertEquals("unloc msg", x.getMessage());
        assertEquals("loc msg", x.getLocalizedMessage());
        assertTrue(x.isLocalized());

        // Almost the same test, but to mimic #31254 message == localizedMessage:
        t2 = new IOException("loc msg");
        err.annotate(t2, ErrorManager.USER, null, "loc msg", null, null);
        t = new IOException("loc msg");
        err.annotate(t, ErrorManager.USER, null, null, t2, null);
        x = NbErrorManager.createExc(t, null, null);
        assertEquals(1973, x.getSeverity().intValue());
        assertEquals("loc msg", x.getMessage());
        assertEquals("loc msg", x.getLocalizedMessage());
        // Note that it is stil considered localized even though the messages
        // are equals: there is a localized annotation.
        assertTrue(x.isLocalized());
        
    }

    public void testPerPetrKuzelsRequestInIssue62836() throws Exception {
        class My extends Exception {
            public My() {
                super("Ahoj");
            }
        }
        
        My my = new My();
        
        err.notify(ErrorManager.INFORMATIONAL, my);
        err.notify(ErrorManager.USER, my);
        
        String output = readLog();
        // wait for a dialog to be shown
        waitEQ();
        
        int report = output.indexOf("My: Ahoj");
        assertTrue("There is one exception reported: " + output, report > 0);
        int next = output.indexOf("My: Ahoj", report + 1);
        assertEquals("No next exceptions there (after " + report + "):\n" + output, -1, next);
    }
    
    private void waitEQ() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
            }
        });
    }

    public void testErrorManagerCompatibilityAsDescribedInIssue79227() throws Exception {
        MockDD.lastDescriptor = null;

        Exception ex = new ClassNotFoundException();
        ErrorManager em = ErrorManager.getDefault();
        String msg = "LocMsg";
        em.annotate(ex, msg);
        em.notify(ErrorManager.USER, ex); // Issue 65116 - don't show the exception to the user

        waitEQ();
        assertNotNull("Mock descriptor called", MockDD.lastDescriptor);
        assertEquals("Info msg", NotifyDescriptor.INFORMATION_MESSAGE, MockDD.lastDescriptor.getMessageType());
    }

    public void testUIExceptionsTriggersTheDialog() throws Exception {
        MockDD.lastDescriptor = null;

        Exception ex = new IOException();
        ErrorManager em = ErrorManager.getDefault();
        em.annotate(ex, ErrorManager.USER, "bla", "blaLoc", null, null);
        Exceptions.printStackTrace(ex);

        waitEQ();
        assertNotNull("Mock descriptor called", MockDD.lastDescriptor);
        assertEquals("Info msg", NotifyDescriptor.INFORMATION_MESSAGE, MockDD.lastDescriptor.getMessageType());
    }
    public void testUIExceptionsTriggersNoDialogIssue118408() throws Exception {
        MockDD.lastDescriptor = null;

        Exception iex = new IllegalStateException();
        String msg = "Thread '";
        ErrorManager.getDefault().notify(ErrorManager.getDefault().annotate(iex, msg));

        waitEQ();
        assertNotNull("Mock descriptor called", MockDD.lastDescriptor);
        assertEquals("Warning msg as the exception has been localized", NotifyDescriptor.WARNING_MESSAGE, MockDD.lastDescriptor.getMessageType());
    }
    public void testUIExceptionsTriggersTheDialogWithWarningPlus1() throws Exception {
        MockDD.lastDescriptor = null;

        Exception ex = new IOException();
        ErrorManager em = ErrorManager.getDefault();
        em.annotate(ex, ErrorManager.USER, "bla", "blaLoc", null, null);
        Logger.global.log(OwnLevel.UNKNOWN, "someerror", ex);

        waitEQ();
        assertNotNull("Mock descriptor called", MockDD.lastDescriptor);
        assertEquals("Info msg", NotifyDescriptor.INFORMATION_MESSAGE, MockDD.lastDescriptor.getMessageType());
    }
    
    // Noticed as part of analysis of #59807 stack trace: Throwable.initCause tricky!
    public void testCatchMarker() throws Exception {
        try {
            m1();
            fail();
        } catch (IOException e) {
            err.notify(ErrorManager.INFORMATIONAL, e);
            String s = readLog();
            assertTrue("added [catch] marker in simple cases: " + s, s.indexOf("[catch] at " + NbErrorManagerTest.class.getName() + ".testCatchMarker") != -1);
        }
        try {
            m3();
            fail();
        } catch (IOException e) {
            err.notify(ErrorManager.INFORMATIONAL, e);
            String s = readLog();
            assertTrue("added [catch] marker in compound exception: " + s, s.indexOf("[catch] at " + NbErrorManagerTest.class.getName() + ".testCatchMarker") != -1);
        }
        try {
            m5();
            fail();
        } catch (InterruptedException e) {
            err.notify(ErrorManager.INFORMATIONAL, e);
            String s = readLog();
            assertTrue("added [catch] marker in multiply compound exception: " + s, s.indexOf("[catch] at " + NbErrorManagerTest.class.getName() + ".testCatchMarker") != -1);
        }
        try {
            throw new IOException("main line\ndata 1\ndata 2\ndata 3\ndata 4");
        } catch (IOException e) {
            err.notify(ErrorManager.INFORMATIONAL, e);
            String s = readLog();
            assertTrue("added [catch] marker in an actual stack trace line: " + s, s.indexOf("[catch] at " + NbErrorManagerTest.class.getName() + ".testCatchMarker") != -1);
        }
    }
    private static void m1() throws IOException {
        m2();
    }
    private static void m2() throws IOException {
        throw new IOException();
    }
    private static void m3() throws IOException {
        try {
            m4();
        } catch (ClassNotFoundException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
    private static void m4() throws ClassNotFoundException {
        throw new ClassNotFoundException();
    }
    private static void m5() throws InterruptedException {
        try {
            m3();
        } catch (IOException e) {
            throw (InterruptedException) new InterruptedException().initCause(e);
        }
    }

    private String readLog() throws IOException {
        LogManager.getLogManager().readConfiguration();

        File log = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log");
        assertTrue("Log file exists: " + log, log.canRead());

        FileInputStream is = new FileInputStream(log);

        byte[] arr = new byte[(int)log.length()];
        int r = is.read(arr);
        assertEquals("all read", arr.length, r);
        is.close();

        new FileOutputStream(log).close(); // truncate

        return "---%<--- [start log of " + getName() + "]\n" + new String(arr).replaceFirst("\0+", "") + "\n---%<--- [end log of " + getName() + "]\n";
    }

    public static final class MockDD extends DialogDisplayer {
        static NotifyDescriptor lastDescriptor;
        
        public Object notify(NotifyDescriptor descriptor) {
            lastDescriptor = descriptor;
            return null;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            lastDescriptor = descriptor;
            return new JDialog() {
                @SuppressWarnings("deprecation")
                @Override
                public void show() {}
            };
        }
        
    }
    private static final class OwnLevel extends Level {
        public static final Level UNKNOWN = new OwnLevel("UNKNOWN", Level.WARNING.intValue() + 1); // NOI18N

        private OwnLevel(String s, int i) {
            super(s, i);
        }
    } // end of UserLevel
}
