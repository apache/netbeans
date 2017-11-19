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

package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.lib.uihandler.LogRecords;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Jaroslav Tulach
 */
public class LogFileIsKeptAtSizeOf1000Test extends NbTestCase {
    private Installer installer;
    
    static {
        MemoryURL.initialize();
    }
    
    public LogFileIsKeptAtSizeOf1000Test(String testName) {
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
        UIHandler.flushImmediatelly();
        System.setProperty("netbeans.user", getWorkDirPath());
        InstallerTest.assureInstallFileLocatorUserDir(getWorkDirPath());
        clearWorkDir();
        
        installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);

        DD.d = null;
        MockServices.setServices(DD.class);

        // setup the listing
        installer.restored();

        assertNull("No dialog as there are no records", DD.d);
    }

    @Override
    protected void tearDown() throws Exception {
        assertNotNull(installer);
        installer.doClose();
    }

    public void testGenerateEnoughLogsAndExit() throws Exception {
        doGenerateALotOfLogs(true);
    }

    @RandomlyFails // NB-Core-Build #7949: full buffer expected:<1000> but was:<0>
    public void testGenerateEnoughLogsInOneRun() throws Exception {
        doGenerateALotOfLogs(false);
    }
    
    private void doGenerateALotOfLogs(boolean exitMeanwhile) throws Exception {
        
        for (int repeat = 0; repeat < 10; repeat++) {
            LogRecord r = new LogRecord(Level.INFO, "MSG_SOMETHING");
            r.setLoggerName(Installer.UI_LOGGER_NAME + ".anything");

            for (int i = 0; i < 1500; i++) {
                Logger.getLogger(Installer.UI_LOGGER_NAME + ".anything").log(r);
            }
            assertEquals("full buffer", 1000, InstallerTest.getLogsSize());

            File logs = new File(new File(getWorkDir(), "var"), "log");
            assertEquals("Two log files: " + Arrays.asList(logs.list()), 2, logs.list().length);
            
            class Cnt extends Handler {
                int cnt;
                
                public Cnt(File f) throws IOException {
                    FileInputStream is = new FileInputStream(f);
                    LogRecords.scan(is, this);
                    is.close();
                }
                
                @Override
                public void publish(LogRecord record) {
                    cnt++;
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() throws SecurityException {
                }

                final void assert1000() {
                    if (cnt > 1001) {
                        fail("Too many logs in this file: " + cnt);
                    }
                }
            }

            Cnt one = new Cnt(logs.listFiles()[0]);
            one.assert1000();
            Cnt two = new Cnt(logs.listFiles()[1]);
            two.assert1000();
            
            assertNull("No dialogs so far", DD.d);

            if (exitMeanwhile) {
                installer.doClose();
                waitForGestures();

                assertNull("No dialogs at close", DD.d);
            }

            assertNull("No dialog shown at begining", DD.d);
        }
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
