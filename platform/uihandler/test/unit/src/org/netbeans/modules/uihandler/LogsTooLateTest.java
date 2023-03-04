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

import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class LogsTooLateTest extends NbTestCase {
    private Installer installer;
    
    public LogsTooLateTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
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
 
    public void testLogsReceivedOnExit() throws Exception {
        installer.restored();
        assertEquals("no logs", 0, InstallerTest.getLogsSize());
        installer.closing();
        installer.close();
        Logger.getLogger(Installer.UI_LOGGER_NAME + ".anything").warning("Ahoj");
        
        assertEquals("one logger received", 1, InstallerTest.getLogsSize());
    }
}
