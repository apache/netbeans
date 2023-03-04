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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 *
 * @author Martin
 */
public class EarlyHandlerTest extends NbTestCase {
    
    public EarlyHandlerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        Installer installer = Installer.findObject(Installer.class, true);
        installer.uninstalled();
    }
    
    public void testEarlyPublish() throws Exception {
        EarlyHandler eh = Lookup.getDefault().lookup(EarlyHandler.class);
        Logger allLogger = Logger.getLogger("org.myapplication.ui.test_early"); // Copied Installer.UI_LOGGER_NAME, not to initialize Installer class.
        allLogger.setLevel(Level.ALL);
        allLogger.addHandler(eh);
        
        allLogger.fine("Test Message 1");
        allLogger.info("Test Message 2");
        allLogger.finest("Test Message 3");
        
        Installer installer = Installer.findObject(Installer.class, true);
        installer.restored();
        assertEquals("EarlyHandler turned off", Level.OFF, eh.getLevel());
        
        allLogger.finer("Test Message 4");
        
        List<LogRecord> logs = InstallerTest.getLogs();
        assertEquals("Number of messages logged: ", 4, logs.size());
        for (int i = 0; i < logs.size(); i++) {
            assertEquals("Test Message "+(i+1), logs.get(i).getMessage());
        }
    }
}
