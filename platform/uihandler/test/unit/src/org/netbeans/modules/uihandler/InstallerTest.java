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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JButton;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.lib.uihandler.LogRecords;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.xml.sax.SAXException;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstallerTest extends NbTestCase {
    
    public InstallerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return false;
    }
    
    private void runTestInEQ(final String testName) throws Exception {
        final Method method = getClass().getMethod(testName);
        final Object obj = this;
        SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                try {
                    method.invoke(obj);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalArgumentException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof RuntimeException) {
                        throw (RuntimeException) cause;
                    } else {
                        throw new RuntimeException(cause);
                    }
                } catch (SecurityException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @Override
    protected void setUp() throws Exception {
        UIHandler.flushImmediatelly();
        System.setProperty("netbeans.user", getWorkDirPath());
        assureInstallFileLocatorUserDir(getWorkDirPath());
        clearWorkDir();
        
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Installer installer = Installer.findObject(Installer.class, true);
                assertNotNull(installer);

                // setup the listing
                installer.restored();
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Installer installer = Installer.findObject(Installer.class, true);
                assertNotNull(installer);
                installer.doClose();
            }
        });
    }
    
    public void testEmptyLog() throws Exception {
        List<LogRecord> list = ScreenSizeTest.removeExtraLogs(Installer.getLogs());
        assertEquals("Empty", 0, list.size());
        list.add(null);
        assertEquals("One", 1, list.size());
    }
    
    @RandomlyFails // #9267, #9435, #9436, #9437, #9949
    public void testLogsRereadOnStartup() throws Exception {
        Logger log = Logger.getLogger(Installer.UI_LOGGER_NAME);
        log.warning("Something happened");

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Installer installer = Installer.findObject(Installer.class, true);
                assertNotNull(installer);
                installer.doClose();

                installer.restored();
            }
        });
        UIHandler.waitFlushed();
        assertEquals("One log is available: " + getLogs(), 1, getLogsSize());
        
        assertEquals("The right message is there", 
            "Something happened", getLogs().get(0).getMessage()
        );
        log.warning("Something happened");
        log.warning("Something happened");
        log.warning("Something happened");
        assertEquals("Four logs available: " + getLogs(), 4, getLogsSize());
        
        // upload done
        Installer.clearLogs();
        
        log.warning("Something happened");
        assertEquals("One log available: " + getLogs(), 1, getLogsSize());
        
    }

    public void testWeCanGetLast1000If1500Logged() throws Exception {
        Logger log = Logger.getLogger(Installer.UI_LOGGER_NAME);
        
        for (int i = 0; i < 1500; i++) {
            log.warning("" + i);
        }

        List<LogRecord> arr = Installer.getLogs();
        assertEquals("Has 1000 records", 1000, arr.size());
        
        Iterator<LogRecord> it = arr.iterator();
        for (int i = 500; i < 1500; i++) {
            LogRecord r = it.next();
            assertEquals("The right name", i, Integer.parseInt(r.getMessage()));
        }
    }

    public void testReadListOfSubmitButtons() throws Exception {
        runTestInEQ("inEQtestReadListOfSubmitButtons");
    }

    public void inEQtestReadListOfSubmitButtons() throws Exception {
        String page = "<html><body><form action='http://xyz.cz' method='POST'>" +
            "<input type='hidden' name='submit' value=\"Send Feedback\"/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        Object[] buttons = parseButtons(is, def);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("Second is default", def, buttons[1]);
        assertEquals("There is one button", 2, buttons.length);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        JButton b = (JButton)buttons[0];
        assertEquals("It is named", "Send Feedback", b.getText());
        assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
    }
    public void testAutosubmitButtonIsAlsoSubmitButton() throws Exception {
        runTestInEQ("inEQtestAutosubmitButtonIsAlsoSubmitButton");
    }
    public void inEQtestAutosubmitButtonIsAlsoSubmitButton() throws Exception {
        String page = "<html><body><form action='http://xyz.cz' method='POST'>" +
            "<input type='hidden' name='auto-submit' value=\"Send Feedback\"/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        Object[] buttons = parseButtons(is, def);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("Second is default", def, buttons[1]);
        assertEquals("There is one button", 2, buttons.length);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        JButton b = (JButton)buttons[0];
        assertEquals("It is named", "Send Feedback", b.getText());
        assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
    }
    public void testDisabledButton() throws Exception {
        runTestInEQ("inEQtestDisabledButton");
    }
    public void inEQtestDisabledButton() throws Exception {
        String page = "<html><body><form action='http://xyz.cz' method='POST'>" +
            "<input type='hidden' name='submit' disabled='true' value=\"Send Feedback\"/>" +
            "<input type='hidden' name='cancel' value=\"Cancel\"></input>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        Object[] buttons = parseButtons(is, def);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("There are 3 buttons", 3, buttons.length);
        assertEquals("3rd is default", def, buttons[2]);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        assertEquals("It is a button2", JButton.class, buttons[0].getClass());
        JButton b = (JButton)buttons[0];
        assertEquals("It is named", "Send Feedback", b.getText());
        assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
        assertFalse("disabled", b.isEnabled());
    }
    public void testTitle() throws Exception {
        runTestInEQ("inEQtestTitle");
    }
    public void inEQtestTitle() throws Exception {
        String page = "<html><head><title>Ahoj</title></head><body><form action='http://xyz.cz' method='POST'>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        DialogDescriptor dd = new DialogDescriptor(null, "MyTit");
        Installer.parseButtons(is, def, dd);
        is.close();
        Object[] buttons = dd.getOptions();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("There is the default", 1, buttons.length);
        assertEquals("3rd is default", def, buttons[0]);
        assertEquals("Ahoj", dd.getTitle());
    }
    public void testParseEmptyAction() throws Exception {
        runTestInEQ("inEQtestParseEmptyAction");
    }
    public void inEQtestParseEmptyAction() throws Exception {
        String page = "<html><head><title>Ahoj</title></head><body><form action='' method='POST'>" +
                "\n" +
                "</form></body></html>";
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        DialogDescriptor dd = new DialogDescriptor(null, "MyTit");
        boolean ok = false;
        try{
            Installer.parseButtons(is, def, dd);
            assertTrue(false);
        }catch(IllegalStateException e){
            ok = true;
            assertTrue (e.getMessage().contains("Action"));
            assertTrue (e.getMessage().contains("empty"));
        }finally{
            is.close();
        }
        assertTrue(ok);
    }
    public void testNoTitle() throws Exception {
        runTestInEQ("inEQtestNoTitle");
    }
    public void inEQtestNoTitle() throws Exception {
        String page = "<html><head></head><body><form action='http://xyz.cz' method='POST'>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        DialogDescriptor dd = new DialogDescriptor(null, "MyTit");
        Installer.parseButtons(is, def, dd);
        is.close();
        Object[] buttons = dd.getOptions();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("There is the default", 1, buttons.length);
        assertEquals("3rd is default", def, buttons[0]);
        assertEquals("MyTit", dd.getTitle());
    }

    public void testUnknownButton() throws Exception {
        runTestInEQ("inEQtestUnknownButton");
    }
    public void inEQtestUnknownButton() throws Exception {
        String page = "<html><body><form action='http://xyz.cz' method='POST'>" +
            "<input type='hidden' name='nevim.co.s.nim' value=\"&amp;Unknown\"/>" +
            "<input type='hidden' name='cancel' value=\"Cancel\"></input>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        Object[] buttons = parseButtons(is, def);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("There are 3 buttons", 3, buttons.length);
        assertEquals("3rd is default", def, buttons[2]);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        assertEquals("It is a button2", JButton.class, buttons[0].getClass());
        JButton b = (JButton)buttons[0];
        assertEquals("It is named", "Unknown", b.getText());
        assertEquals("No url attribute is set", null, b.getClientProperty("url"));
        assertFalse("disabled", b.isEnabled());
    }
    
    public void testReadListOfSubmitButtonsWithAmpersand() throws Exception {
        runTestInEQ("inEQtestReadListOfSubmitButtonsWithAmpersand");
    }
    public void inEQtestReadListOfSubmitButtonsWithAmpersand() throws Exception {
        String page = "<html><body><form action='http://xyz.cz' method='POST'>" +
            "<input type='hidden' name='submit' value=\"&amp;Send Feedback\"/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        Object[] buttons = parseButtons(is, def);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("There is one button", 2, buttons.length);
        assertEquals("Second is default", def, buttons[1]);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        JButton b = (JButton)buttons[0];
        assertEquals("It is named", "Send Feedback", b.getText());
        assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
        if (Utilities.isMac()) {
            assertEquals("Mnemonics", 0, b.getMnemonic());
            assertEquals("Mnemonic index", -1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals("Mnemonics", 'S', b.getMnemonic());
            assertEquals("Mnemonic index", 0, b.getDisplayedMnemonicIndex());
        }
    }

    public void testCanDefineExitButton() throws Exception {
        runTestInEQ("inEQtestCanDefineExitButton");
    }
    public void inEQtestCanDefineExitButton() throws Exception {
        String page = "<html><body><form action='http://xyz.cz' method='POST'>" +
            "\n" +
            "<input type='hidden' name='submit' value=\"&amp;Send Feedback\"/>" +
            "\n" +
            "<input type='hidden' name='exit' value=\"&amp;Cancel\"/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        JButton def = new JButton("Default");
        Object[] buttons = parseButtons(is, def);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("There are two buttons", 2, buttons.length);
        if (def == buttons[1]) {
            fail("Second is default: " + buttons[1]);
        }
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        JButton b = (JButton)buttons[0];
        assertEquals("It is named", "Send Feedback", b.getText());
        assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
        if (Utilities.isMac()) {
            assertEquals("Mnemonics", 0, b.getMnemonic());
            assertEquals("Mnemonic index", -1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals("Mnemonics", 'S', b.getMnemonic());
            assertEquals("Mnemonic index", 0, b.getDisplayedMnemonicIndex());
        }

        assertEquals("It is a button", JButton.class, buttons[1].getClass());
        b = (JButton)buttons[1];
        assertEquals("It is named", "Cancel", b.getText());
        assertNull("No url", b.getClientProperty("url"));
        if (Utilities.isMac()) {
            assertEquals("Mnemonics", 0, b.getMnemonic());
            assertEquals("Mnemonic index", -1, b.getDisplayedMnemonicIndex());
        } else {
            assertEquals("Mnemonics", 'C', b.getMnemonic());
            assertEquals("Mnemonic index", 0, b.getDisplayedMnemonicIndex());
        }
    }

    public void testReadAllButtons() throws Exception {
        runTestInEQ("inEQtestReadAllButtons");
    }
    public void inEQtestReadAllButtons() throws Exception {
        String page = 
            "<html><body><form action='http://xyz.cz' method='POST'>" +
            "  <input type='hidden' name='submit' value=\"&amp;Send Feedback\"/>" +
            "  <input type='hidden' name='never-again' value=\"&amp;No and do not Bother Again\"/>" +
            "\n" +
            "  <input type='hidden' name='view-data' value=\"View &amp;Data\"/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        Object[] buttons = parseButtons(is, null);
        is.close();
        
        assertNotNull("buttons parsed", buttons);
        assertEquals("There are 3 button", 3, buttons.length);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        assertEquals("It is a button2", JButton.class, buttons[1].getClass());
        assertEquals("It is a button3", JButton.class, buttons[2].getClass());
        
        {
            JButton b = (JButton)buttons[0];
            assertEquals("It is named", "Send Feedback", b.getText());
            assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
            if (Utilities.isMac()) {
                assertEquals("Mnemonics", 0, b.getMnemonic());
                assertEquals("Mnemonic index", -1, b.getDisplayedMnemonicIndex());
            } else {
                assertEquals("Mnemonics", 'S', b.getMnemonic());
                assertEquals("Mnemonic index", 0, b.getDisplayedMnemonicIndex());
            }
            assertEquals("submit", b.getActionCommand());
            URL[] url = new URL[1];
            String r = Installer.decodeButtons(b, url);
            assertEquals("action is ", "submit", r);
            assertEquals("no url", new URL("http://xyz.cz"), url[0]);
        }
        {
            JButton b = (JButton)buttons[1];
            assertEquals("It is named", "No and do not Bother Again", b.getText());
            assertEquals("It url attribute is not set", null, b.getClientProperty("url"));
            if (Utilities.isMac()) {
                assertEquals("Mnemonics", 0, b.getMnemonic());
                assertEquals("Mnemonic index", -1, b.getDisplayedMnemonicIndex());
            } else {
                assertEquals("Mnemonics", 'N', b.getMnemonic());
                assertEquals("Mnemonic index", 0, b.getDisplayedMnemonicIndex());
            }
            assertEquals("never-again", b.getActionCommand());
        }
        {
            JButton b = (JButton)buttons[2];
            assertEquals("It is named", "View Data", b.getText());
            assertEquals("It url attribute is not set", null, b.getClientProperty("url"));
            if (Utilities.isMac()) {
                assertEquals("Mnemonics", 0, b.getMnemonic());
                assertEquals("Mnemonic index", -1, b.getDisplayedMnemonicIndex());
            } else {
                assertEquals("Mnemonics", 'D', b.getMnemonic());
                assertEquals("Mnemonic index", 5, b.getDisplayedMnemonicIndex());
            }
            assertEquals("view-data", b.getActionCommand());
            
            URL[] url = new URL[1];
            String r = Installer.decodeButtons(b, url);
            assertEquals("action is ", "view-data", r);
            assertEquals("no url", null, url[0]);
        }
    }

    /*
    <input type="radio" name="group1" value="Milk"> Milk<br>
      */
    
    public void testNoFormMeansNoButtons() throws Exception {
        runTestInEQ("inEQtestNoFormMeansNoButtons");
    }
    public void inEQtestNoFormMeansNoButtons() throws Exception {
        String page = 
            "<html><body></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        DialogDescriptor dd = new DialogDescriptor(null, null);
        Installer.parseButtons(is, null, dd);
        is.close();
        
        Object[] buttons = dd.getOptions();
        assertNotNull("buttons not parsed: ", buttons);
        assertEquals("But empty", 0, buttons.length);
    }
        
    public void testLeftAligned() throws Exception {
        runTestInEQ("inEQtestLeftAligned");
    }
    public void inEQtestLeftAligned() throws Exception {
        String page = 
            "<html><body><form action='http://xyz.cz' method='POST'>" +
            "  <input type='hidden' name='submit' value=\"&amp;Send Feedback\"/>" +
            "  <input type='hidden' name='never-again' value=\"&amp;No and do not Bother Again\"/>" +
            "\n" +
            "  <input type='hidden' name='view-data' align='left' value=\"&amp;View Data\" alt='Hide'/>" +
            "\n" +
            "</form></body></html>";
        
        InputStream is = new ByteArrayInputStream(page.getBytes());
        DialogDescriptor dd = new DialogDescriptor(null, null);
        Installer.parseButtons(is, null, dd);
        is.close();
        
        Object[] buttons = dd.getOptions();
        assertNotNull("buttons parsed", buttons);
        assertEquals("There are 2 buttons", 2, buttons.length);
        assertEquals("It is a button", JButton.class, buttons[0].getClass());
        assertEquals("It is a button2", JButton.class, buttons[1].getClass());
        
        {
            JButton b = (JButton)buttons[0];
            assertEquals("It is named", "Send Feedback", b.getText());
            assertEquals("It url attribute is set", "http://xyz.cz", b.getClientProperty("url"));
            assertEquals("Mnemonics", 'S', b.getMnemonic());
            assertEquals("submit", b.getActionCommand());
            URL[] url = new URL[1];
            String r = Installer.decodeButtons(b, url);
            assertEquals("action is ", "submit", r);
            assertEquals("no url", new URL("http://xyz.cz"), url[0]);
        }
        {
            JButton b = (JButton)buttons[1];
            assertEquals("It is named", "No and do not Bother Again", b.getText());
            assertEquals("It url attribute is not set", null, b.getClientProperty("url"));
            assertEquals("Mnemonics", 'N', b.getMnemonic());
            assertEquals("never-again", b.getActionCommand());
        }
        
        buttons = dd.getAdditionalOptions();
        assertNotNull("There are some additionals", buttons);
        assertEquals("One is there", 1, buttons.length);
        {
            JButton b = (JButton)buttons[0];
            assertEquals("It is named", "View Data", b.getText());
            assertEquals("It url attribute is not set", null, b.getClientProperty("url"));
            assertEquals("Mnemonics", 'V', b.getMnemonic());
            assertEquals("view-data", b.getActionCommand());
            
            URL[] url = new URL[1];
            String r = Installer.decodeButtons(b, url);
            assertEquals("action is ", "view-data", r);
            assertEquals("no url", null, url[0]);
            assertEquals("alt is there", "Hide", b.getClientProperty("alt"));
        }
    }
    
    private static Object[] parseButtons(InputStream is, Object def) throws IOException, ParserConfigurationException, SAXException, InterruptedException, InvocationTargetException {
        DialogDescriptor dd = new DialogDescriptor(null, null);
        Installer.parseButtons(is, def, dd);
        return dd.getOptions();
    }
    
    public void testCreateMessage(){
        String message = Installer.createMessage(new NullPointerException());
        assertEquals("NullPointerException at org.netbeans.modules.uihandler.InstallerTest.testCreateMessage", message);
        message = Installer.createMessage(new AssertionError());
        assertEquals("AssertionError at org.netbeans.modules.uihandler.InstallerTest.testCreateMessage", message);
        message = Installer.createMessage(new AssertionError("TEST"));
        assertEquals("AssertionError: TEST", message);
        message = Installer.createMessage(new Throwable());
        assertEquals("Throwable at org.netbeans.modules.uihandler.InstallerTest.testCreateMessage", message);
        message = Installer.createMessage(new Error("PROBLEM"));
        assertEquals("Error: PROBLEM", message);
    }

    public void testCreateMessageIssue157715(){
        Exception rootExc = new NullPointerException("root");
        IllegalStateException ise = new IllegalStateException("ae", rootExc);
        String message = Installer.createMessage(ise);
        assertEquals("use nested exception", "NullPointerException: root", message);
    }

    public void testCreateMessageIssue160019() throws IOException {//ignore annotations
        final List<LogRecord> logs = new ArrayList<LogRecord>();
        ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        Exception rootExc = new NullPointerException("root");
        Exceptions.attachMessage(rootExc, "annotation message");
        LogRecord rec = new LogRecord(Level.SEVERE, "test");
        rec.setThrown(rootExc);
        LogRecords.write(bos, rec);
        bos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        LogRecords.scan(bis, new Handler() {

            @Override
            public void publish(LogRecord record) {
                logs.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
        bis.close();

        assertEquals(1, logs.size());
        Throwable thrown = logs.get(0).getThrown();
        assertEquals("thrown is annotated", "annotation message", thrown.getCause().getMessage());
        String message = Installer.createMessage(thrown);
        assertEquals("annontation should be ignored", "NullPointerException: root", message);
    }

    static int getLogsSize(){
        List<LogRecord> logs = Installer.getLogs();
        logs = ScreenSizeTest.removeExtraLogs(logs);
        return logs.size();
    }

    static List<LogRecord> getLogs() {
        List<LogRecord> logs = Installer.getLogs();
        logs = ScreenSizeTest.removeExtraLogs(logs);
        return logs;
    }

    /**
     * Assures that the InstalledFileLocator uses the current user dir.
     * @param workDirPath
     */
    static void assureInstallFileLocatorUserDir(String workDirPath) {
        File f = new File(workDirPath).getAbsoluteFile();
        if (!f.isDirectory()) {
            return ;
        }
        File userDir = FileUtil.normalizeFile(f);
        try {
            Field instancesField = InstalledFileLocator.class.getDeclaredField("instances");
            instancesField.setAccessible(true);
            InstalledFileLocator[] instances = (InstalledFileLocator[]) instancesField.get(null);
            if (instances == null) {
                return ; // Not initialized yet
            }
            Class implClass = instances[0].getClass(); // Expecting org.netbeans.core.startup.InstalledFileLocatorImpl
            Field dirsField = implClass.getDeclaredField("dirs");
            dirsField.setAccessible(true);
            File[] dirs = (File[]) dirsField.get(instances[0]);
            if (dirs == null) {
                return ; // Not initialized yet
            }
            if (!dirs[0].equals(userDir)) {
                dirs[0] = userDir;  // Update
            }
        } catch (NoSuchFieldException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException("FIX ME.");
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException("FIX ME.");
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException("FIX ME.");
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
            throw new IllegalStateException("FIX ME.");
        }
    }

}
