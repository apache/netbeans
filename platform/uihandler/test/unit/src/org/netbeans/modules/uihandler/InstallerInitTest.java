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

package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstallerInitTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(InstallerInitTest.class);
    }

    private Installer installer;
    
    static {
        MemoryURL.initialize();
    }
    
    public InstallerInitTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        UIHandler.flushImmediatelly();
        
        installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);

        Installer.dontWaitForUserInputInTests();
        DD.d = null;
        MockServices.setServices(DD.class);

        // setup the listing
        installer.restored();

        assertNull("No dialog as there are no records", DD.d);
        
        Locale.setDefault(new Locale("in", "IT"));
        
    }

    @Override
    protected void tearDown() throws Exception {
        assertNotNull(installer);
        installer.doClose();
    }

    public void testGenerateEnoughLogsExit() throws Exception {
        LogRecord r = new LogRecord(Level.INFO, "MSG_SOMETHING");
        r.setLoggerName(Installer.UI_LOGGER_NAME + ".anything");

        String utf8 = 
            "<html><head>" +
            "</head>" +
            "<body>" +
            "<form action='http://anna.nbextras.org/analytics/upload.jsp' method='post'>" +
            "  <input name='submit' value='&amp;Fill Survey' type='hidden'> </input>" +
            "</form>" +
            "</body></html>";
        ByteArrayInputStream is = new ByteArrayInputStream(utf8.getBytes("utf-8"));
        
        MemoryURL.registerURL("memory://start.html", is);
        
        for (int i = 0; i < 1500; i++) {
            Logger.getLogger(Installer.UI_LOGGER_NAME + ".anything").log(r);
        }
        assertEquals("full buffer", 1000, InstallerTest.getLogsSize());
        
        assertNull("No dialogs so far", DD.d);
        
        installer.doClose();
        waitForGestures();
        
        assertNull("No dialogs at close", DD.d);

        Preferences prefs = NbPreferences.forModule(Installer.class);
        prefs.putInt("count", UIHandler.MAX_LOGS );
        
        installer.restored();
        
        waitForGestures();

//        assertNotNull("A dialog shown at begining", DD.d);
    }

    public void testDontSubmitTwiceIssue128306(){
        final AtomicBoolean submittingTwiceStopped = new AtomicBoolean(false);
        final Installer.SubmitInteractive interactive = new Installer.SubmitInteractive("hallo", true);
        final ActionEvent evt = new ActionEvent("submit", 1, "submit");
        Installer.LOG.setLevel(Level.FINEST);
        Installer.LOG.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                if ("posting upload UIGESTURES".equals(record.getMessage())){
                    interactive.actionPerformed(evt);
                }
                if ("ALREADY SUBMITTING".equals(record.getMessage())){
                    submittingTwiceStopped.set(true);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
        interactive.createDialog();
        interactive.actionPerformed(evt);
        Installer.RP_SUBMIT.post(new Runnable() {

            public void run() {
                // block RP executor
                synchronized(this){
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        Installer.RP_SUBMIT.post(new Runnable() {

            public void run() {
                // just wait for processing
            }
        }).waitFinished();
        assertTrue(submittingTwiceStopped.get());
    }

    public static final class DD extends DialogDisplayer {
        static NotifyDescriptor d;
        
        public Object notify(NotifyDescriptor descriptor) {
            assertNull(d);
            d = descriptor;
            return NotifyDescriptor.CLOSED_OPTION;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            assertNull(d);
            d = descriptor;
            
            return new DialogImpl(d, new Frame());
        }

        private static class DialogImpl extends Dialog 
        implements PropertyChangeListener {
            NotifyDescriptor d;
            
            private DialogImpl(NotifyDescriptor d, Frame owner) {
                super(owner);
                this.d = d;
            }

            @java.lang.Override
            public synchronized void setVisible(boolean b) {
                assertFalse(isModal());
            }

            public synchronized void propertyChange(PropertyChangeEvent evt) {
                if (d != null && d.getOptions().length == 2) {
                    d.setValue(NotifyDescriptor.CLOSED_OPTION);
                    d = null;
                    notifyAll();
                }
            }
        }
        
    }

    private void waitForGestures() throws InterruptedException, InvocationTargetException {
        class RunnableImpl implements Runnable {
            
            private RunnableImpl() {
            }
            
            public void run() {
            }
        }
        SwingUtilities.invokeAndWait(new RunnableImpl());
        Installer.RP.post(new RunnableImpl()).waitFinished();
    }

}
