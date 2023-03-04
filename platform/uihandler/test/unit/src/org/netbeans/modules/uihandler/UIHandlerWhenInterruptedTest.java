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

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class UIHandlerWhenInterruptedTest extends NbTestCase {
    private static Logger UILOG = Logger.getLogger(Installer.UI_LOGGER_NAME + ".actions");

    
    public UIHandlerWhenInterruptedTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        Installer o = Installer.findObject(Installer.class, true);
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        assertNotNull("Installer created", o);
        o.restored();
    }

    protected void tearDown() throws Exception {
        Installer o = Installer.findObject(Installer.class, true);
        o.uninstalled();
    }

    public void testPublishWhenInterupted() {
        
        for (int i = 0; i < 800; i++) {
            LogRecord rec2 = new LogRecord(Level.FINER, "" + i); // NOI18N
            Thread.currentThread().interrupt();
            UILOG.log(rec2);        
        }

        int cnt = 50;
        while (cnt-- > 0 && Installer.getLogsSize() < 800) {
            // ok, repeat
        }
        List<LogRecord> logs = InstallerTest.getLogs();
        assertEquals("One log: " + logs, 800, logs.size());
        
        for (int i = 1; i < 800; i++) {
            assertEquals("" + i, logs.get(i).getMessage());
        }
        
    }
    
    private static final class MyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
        }
    }
}
