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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.exceptions.ExceptionsSettings;
import org.netbeans.modules.exceptions.ReportPanel;

/**
 *
 * @author Jindrich Sedek
 */
public class ExceptionsTest extends NbTestCase {
    
    public ExceptionsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        UIHandler.flushImmediatelly();
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        Installer.findObject(Installer.class, true).uninstalled();
    }
    
    
    public void testSetReportPanelSummary(){
        String str = "RETEZEC SUMMARY";
        ExceptionsSettings s = new ExceptionsSettings();
        ReportPanel panel = new ReportPanel(false, s);
        panel.setSummary(str);
        assertEquals(str, panel.getSummary());
    }
    
    public void testExceptionThrown() throws Exception{
        Logger uiLogger = Logger.getLogger(Installer.UI_LOGGER_NAME);
        LogRecord log1 = new LogRecord(Level.SEVERE, "TESTING MESSAGE");
        LogRecord log2 = new LogRecord(Level.SEVERE, "TESTING MESSAGE");
        LogRecord log3 = new LogRecord(Level.SEVERE, "NO EXCEPTION LOG");
        LogRecord log4 = new LogRecord(Level.INFO, "INFO");
        Throwable t1 = new NullPointerException("TESTING THROWABLE");
        Throwable t2 = new UnknownError("TESTING ERROR");
        log1.setThrown(t1);
        log2.setThrown(t2);
        log4.setThrown(t2);
        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);
        installer.restored();
        uiLogger.log(log1);
        uiLogger.log(log2);
        uiLogger.log(log3);
        UIHandler.waitFlushed();
        assertEquals(3, InstallerTest.getLogsSize());
        if (Installer.getThrown().getMessage().indexOf("TESTING ERROR") == -1) {
            fail("Wrong message " + Installer.getThrown().getMessage());
        }
        log1 = new LogRecord(Level.SEVERE, "TESTING 2");
        log1.setThrown(t1);
        uiLogger.log(log1);
        assertEquals(4, InstallerTest.getLogsSize());
        if (Installer.getThrown().getMessage().indexOf("TESTING THROWABLE") == -1) {
            fail("Wrong message " + Installer.getThrown().getMessage());
        }
        for (int i= 0; i < 10; i++){
            uiLogger.warning("MESSAGE "+Integer.toString(i));
        }
        assertEquals(14, InstallerTest.getLogsSize());
        if (Installer.getThrown().getMessage().indexOf("TESTING THROWABLE") == -1) {
            fail("Wrong message " + Installer.getThrown().getMessage());
        }
        uiLogger.log(log4);
        assertEquals(15, InstallerTest.getLogsSize());
        if (Installer.getThrown().getMessage().indexOf("TESTING THROWABLE") == -1){
            fail("Wrong message " + Installer.getThrown().getMessage());
        }
        if (Installer.getThrown().getMessage().contains("WARNING")){
            fail("Message should not contain warnings" + Installer.getThrown().getMessage());
        }
        StackTraceElement elem = t1.getStackTrace()[0];
        String mess = elem.getClassName() + "." + elem.getMethodName();
        Object[] params = {t1.getClass().getName() + ": " + t1.getMessage(), mess}; // NOI18N
        Installer.setSelectedExcParams(params);
        if (Installer.getThrown().getMessage().indexOf("TESTING THROWABLE") == -1){
            fail("Wrong message - selected by user " + Installer.getThrown().getMessage());
        }
        if (Installer.getThrown().getMessage().indexOf("TESTING ERROR") != -1){
            fail("Wrong message - selected by user" + Installer.getThrown().getMessage());
        }

        elem = t2.getStackTrace()[0];
        mess = elem.getClassName() + "." + elem.getMethodName();
        Object[] params2 = {t2.getClass().getName() + ": " + t2.getMessage(), mess}; // NOI18N
        Installer.setSelectedExcParams(params2);
        if (Installer.getThrown().getMessage().indexOf("TESTING THROWABLE") != -1){
            fail("Wrong message - selected by user" + Installer.getThrown().getMessage());
        }
        if (Installer.getThrown().getMessage().indexOf("TESTING ERROR") == -1){
            fail("Wrong message - selected by user" + Installer.getThrown().getMessage());
        }
        
    }
}
