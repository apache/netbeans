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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/** Checks that we can do proper logging.
 *
 * @author Jaroslav Tulach
 */
public class LoggingTest extends NbTestCase {
    private Throwable toThrow;
    private Throwable toMsg;
    
    public LoggingTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    /** Used in testIOExceptionIsWrappedWithLogMsg. */
    public void throwIOThrowable() throws Throwable {
        Logger log = Logger.getLogger(getName());
        if (toMsg != null) {
            log.log(Level.WARNING, "Msg", toMsg);
        }
        log.warning("Going to throw: " + toThrow);
        throw toThrow;
    }
    
    /** Used in testMyExceptionIsWrappedWithLogMsg. It has to have different
     *  name because it cannot delete log file on 64 bit machine. */
    public void throwMyThrowable() throws Throwable {
        throwIOThrowable();
    }
    
    public void testLogFileName() throws Exception {
        PrintStream ps = getLog("ahoj");
        File f = new File(getWorkDir(), "ahoj");
        assertEquals("Log file exists", true, f.exists());
    }
    
    public void testLogFileNameEqualsToNameOfTest() throws Exception {
        PrintStream ps = getLog();
        File f = new File(getWorkDir(), getName() + ".log");
        assertEquals("Log file exists", true, f.exists());
    }
    
    /** Test of NbTestCase#trimLogFiles method. It should trim size of all files
     * in test case workdir to 1 MB. The method is called at the end of
     * NbTestCase#run method.
     */
    public void testTrimLogFiles() throws IOException {
        StringBuffer buff = new StringBuffer(1024);
        for(int i=0;i<1024;i++) {
            buff.append('A');
        }
        String string1kB = buff.toString();
        for(int i=0;i<2024;i++) {
            log(string1kB);
            log("myLog", string1kB);
            ref(string1kB);
        }
        
        File trimmedDir = getWorkDir();
        String[] filenames = {"testTrimLogFiles.log", "testTrimLogFiles.ref", "myLog" };
        for(int i=0;i<filenames.length;i++) {
            File file = new File(trimmedDir, "TRIMMED_"+filenames[i]);
            assertTrue(file.getName()+" not exists.", file.exists());
            assertTrue(file.getName()+" not trimmed to 1 MB.", file.length() < 2097152L);
            file = new File(trimmedDir, filenames[i]);
            if(file.exists()) {
                // original file exists only if cannot be deleted. Then it has minimal size.
                assertTrue(file.getName()+" not trimmed." + file.length(), file.length() < 1024 * 1024);
            }
        }
    }


    protected Level logLevel() {
        return Level.WARNING;
    }

    public void testLoggingUtil() throws Exception {
        doLoggingUtil(false);
    }

    public void testLoggingUtilWithCleanOfUserDir() throws Exception {
        doLoggingUtil(true);
    }
    
    private void doLoggingUtil(boolean clean) throws Exception {
        Logger log = Logger.getLogger(getName());
        
        log.log(Level.SEVERE, "Ahoj");

        if (clean) {
            clearWorkDir();
            log.log(Level.SEVERE, "Ahojky");
        }
        
        log.log(Level.FINE, "Jardo");
 
        File f = new File(getWorkDir(), getName() + ".log");
        assertEquals("Log file exists", true, f.exists());

        String s = readFile(f);
        if (s.indexOf("Ahoj") == -1) {
            fail("There should be Ahoj\n" + s);
        }
        assertEquals("Not logged for FINE: " + s, -1, s.indexOf("Jardo"));
    }
    
    static String readFile(File f) throws IOException {
        byte[] arr = new byte[(int)f.length()];
        FileInputStream is = new FileInputStream(f);
        int l = is.read(arr);
        assertEquals(l, arr.length);
        
        String s = new String(arr);
        return s;
    }
    public void testFmting() throws Exception {
        Logger log = Logger.getLogger(getName());
        
        LogRecord rec = new LogRecord(Level.SEVERE, "LOG_SevereMsg");
        rec.setResourceBundle(ResourceBundle.getBundle("org.netbeans.junit.TestBundle"));
        rec.setParameters(new Object[] { "Very" });
        log.log(rec);

        File f = new File(getWorkDir(), getName() + ".log");
        assertEquals("Log file exists", true, f.exists());

        byte[] arr = new byte[(int)f.length()];
        FileInputStream is = new FileInputStream(f);
        int l = is.read(arr);
        assertEquals(l, arr.length);

        String s = new String(arr);
        if (s.indexOf("Important message Very") == -1) {
            fail("There should the message\n" + s);
        }
    }
    public void testIOExceptionIsWrappedWithLogMsg() throws Exception {
        
        LoggingTest inner = new LoggingTest("throwIOThrowable");
        inner.toThrow = new IOException("Ahoj");
        
        TestResult res = inner.run();
        assertEquals("One error", 1, res.errorCount());
        assertEquals("No failure", 0, res.failureCount());
        
        TestFailure f = (TestFailure)res.errors().nextElement();
        
        if (f.exceptionMessage().indexOf("Going to throw") == -1) {
            fail("There should be output of the log:\n" + f.exceptionMessage());
        }
    }
    public void testMyExceptionIsWrappedWithLogMsg() throws Exception {
        LoggingTest inner = new LoggingTest("throwMyThrowable");
        
        class MyEx extends Exception {
        }
        
        inner.toThrow = new MyEx();
        
        TestResult res = inner.run();
        assertEquals("One error", 1, res.errorCount());
        assertEquals("No failure", 0, res.failureCount());
        
        TestFailure f = (TestFailure)res.errors().nextElement();
        
        if (f.exceptionMessage() == null || f.exceptionMessage().indexOf("Going to throw") == -1) {
            fail("There should be output of the log:\n" + f.exceptionMessage());
        }
    }
    public void testMyExceptionWithStackTrace() throws Exception {
        LoggingTest inner = new LoggingTest("throwMyThrowable");
        
        class MyEx extends Exception {
        }
        
        inner.toThrow = new MyEx();
        inner.toMsg = new MyEx();
        
        TestResult res = inner.run();
        assertEquals("One error", 1, res.errorCount());
        assertEquals("No failure", 0, res.failureCount());
        
        TestFailure f = (TestFailure)res.errors().nextElement();
        
        if (
            f.exceptionMessage() == null || 
            f.exceptionMessage().indexOf("Going to throw") == -1 ||
            f.exceptionMessage().indexOf("testMyExceptionWithStackTrace") == -1
       ) {
            fail("There should be output of the log:\n" + f.exceptionMessage());
        }
    }
}
