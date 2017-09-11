/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
