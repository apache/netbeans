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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import junit.framework.*;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class LogsTooEarlyTest extends NbTestCase {
    static {
        Logger.getLogger("").addHandler(new Delegating());
    }
    
    private Installer installer;
    
    public LogsTooEarlyTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        UIHandler.flushImmediatelly();
        clearWorkDir();
        
        Installer.clearLogs();
        
        installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);

    }

    @Override
    protected void tearDown() throws Exception {
        assertNotNull(installer);
        installer.doClose();
    }

    public void testLogsReceivedOnStartup() throws Exception {
        Logger.getLogger(Installer.UI_LOGGER_NAME + ".anything").warning("Ahoj");

        installer.restored();
        
        assertEquals("one logger received", 1, InstallerTest.getLogsSize());
    }
 
    /** This simulates the standard core's handler
     */
    private static final class Delegating extends Handler {

        @Override
        public void publish(LogRecord record) {
            for (Handler h : Lookup.getDefault().lookupAll(Handler.class)) {
                h.publish(record);
            }
        }

        @Override
        public void flush() {
            for (Handler h : Lookup.getDefault().lookupAll(Handler.class)) {
                h.flush();
            }
        }

        @Override
        public void close() throws SecurityException {
            for (Handler h : Lookup.getDefault().lookupAll(Handler.class)) {
                h.close();
            }
        }
        
    }
}
