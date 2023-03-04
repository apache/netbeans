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
import javax.swing.JButton;
import java.util.logging.LogRecord;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class UIHandlerTest extends NbTestCase {
    private static Logger UILOG = Logger.getLogger(Installer.UI_LOGGER_NAME + ".actions");

    
    public UIHandlerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        Installer o = Installer.findObject(Installer.class, true);
        System.setProperty("netbeans.user", getWorkDirPath());
        UIHandler.flushImmediatelly();
        clearWorkDir();
        assertNotNull("Installer created", o);
        o.restored();
    }

    @Override
    protected void tearDown() throws Exception {
        Installer o = Installer.findObject(Installer.class, true);
        o.uninstalled();
    }
    
    public void testPublish() {
        
        MyAction a = new MyAction();
        a.putValue(Action.NAME, "Tmp &Action");
        JButton b = new JButton(a);
        
        LogRecord rec = new LogRecord(Level.FINER, "UI_ACTION_BUTTON_PRESS"); // NOI18N
        rec.setParameters(new Object[] { 
            b, 
            b.getClass().getName(), 
            a, 
            a.getClass().getName(), 
            a.getValue(Action.NAME) }
        );
        UILOG.log(rec);        
        
        List<LogRecord> logs = InstallerTest.getLogs();
        assertEquals("One log: " + logs, 1, logs.size());
        LogRecord first = logs.get(0);
        
        assertEquals("This is the logged record", rec.getMessage(), first.getMessage());
        
        
    }
    
    private static final class MyAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
        }
    }
}
