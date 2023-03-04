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
package org.netbeans.core.startup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;


/**
 * Checks the behaviour of NetBeans logging support.
 */
public class TopLoggingTest extends NbTestCase {
    private ByteArrayOutputStream w;
    private Handler handler;
    private Logger logger;
    
    public TopLoggingTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        Enumeration<?> en = System.getProperties().propertyNames();
        while (en.hasMoreElements()) {
            String n = en.nextElement().toString();
            if (n.endsWith(".level")) {
                System.getProperties().remove(n);
            }
        }

        System.setProperty("netbeans.user", getWorkDirPath());

        // initialize logging
        TopLogging.initialize();

        w = new ByteArrayOutputStream() {
            @Override
            public void write(byte[] b, int off, int len) {
                super.write(b, off, len);
            }

            @Override
            public void write(byte[] b) throws IOException {
                super.write(b);
            }

            @Override
            public void write(int b) {
                super.write(b);
            }

            @Override
            public String toString() {
                handler.flush();

                String retValue;                
                retValue = super.toString();
                return retValue;
            }

        };

        handler = TopLogging.createStreamHandler(new PrintStream(getStream()));
        logger = Logger.getLogger("");
        Handler[] old = logger.getHandlers();
// do not remove default handlers from CLIOptions.initialize():
//        for (int i = 0; i < old.length; i++) {
//            logger.removeHandler(old[i]);
//        }
        logger.addHandler(handler);

        w.reset();

    }

    @Override
    protected void tearDown() throws Exception {
        logger = null;
    }

    protected ByteArrayOutputStream getStream() {
        return w;
    }



    public void testLoggingAnnotateException() throws Exception {
        Exception e = new Exception("One");
        Exceptions.attachMessage(e, "Two");

        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "Three", e);

        String disk = readLog(true);
        if (!disk.contains("One") || !disk.contains("Two") || !disk.contains("Three")) {
            fail("There shall be One, Two, Three text in the log:\n" + disk);
        }
    }
    public void testLoggingLocalizedAnnotateException() throws Exception {
        Exception e = new Exception("One");
        Exceptions.attachLocalizedMessage(e, "Two");

        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "Three", e);

        String disk = readLog(true);
        if (!disk.contains("One") || !disk.contains("Two") || !disk.contains("Three")) {
            fail("There shall be One, Two, Three text in the log:\n" + disk);
        }
    }
    
    public void testLogLoggingMessagesEndsUpInMultipleFiles() throws Exception {
        StringBuilder sb = new StringBuilder();
        while(sb.length() < 1024) {
            sb.append("0123456789");
        }

        Logger l = Logger.getLogger(TopLoggingTest.class.getName());
        for (int i = 0; i < 2048; i++) {
            l.log(Level.WARNING, sb.toString() + " index: " + i);
            getStream().reset();
        }

        TopLogging.flush(false);

        File log = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log");
        assertTrue("Log file exists: " + log, log.canRead());


        File log2 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.1");
        assertTrue("Currently we rotate just after each 1 meg: " + log2, log2.canRead());

        TopLogging.close();
    }

    public void testFileRotationByDefault() throws Exception {
        System.clearProperty("org.netbeans.log.numberOfFiles");
        for (int i = 0; i < 5; i++) {
            // simulate shutdown
            TopLogging.flush(false);
            TopLogging.close();
            // simulate restart
            TopLogging.flush(true);
            TopLogging.initialize();
        }

        File log = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log");
        assertTrue("Log file exists: " + log, log.canRead());

        File log1 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.1");
        assertTrue("Backup file exists: " + log1, log1.canRead());

        File log2 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.2");
        assertTrue("Backup file exists: " + log2, log2.canRead());

        File log3 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.3");
        assertFalse("Backup file does not exist: " + log3, log3.canRead());
    }

    public void testFileRotationWithSystemProperty() throws Exception {
        try {
        for (int i = 0; i < 6; i++) {
            // simulate shutdown
            TopLogging.flush(false);
            TopLogging.close();
            // set system property
            System.setProperty("org.netbeans.log.numberOfFiles", "4");
            // simulate restart
            TopLogging.flush(true);
            TopLogging.initialize();
        }
        } finally {
            System.clearProperty("org.netbeans.log.numberOfFiles");
        }

        File log = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log");
        assertTrue("Log file exists: " + log, log.canRead());

        File log1 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.1");
        assertTrue("Backup file exists: " + log1, log1.canRead());

        File log2 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.2");
        assertTrue("Backup file exists: " + log2, log2.canRead());

        File log3 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.3");
        assertTrue("Backup file exists: " + log3, log3.canRead());

        File log4 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.4");
        assertFalse("Backup file does not exist: " + log4, log4.canRead());
    }

    public void testFileRotationAtLeastThreeFiles() throws Exception {
        try {
        for (int i = 0; i < 5; i++) {
            // simulate shutdown
            TopLogging.flush(false);
            TopLogging.close();
            // set system property
            System.setProperty("org.netbeans.log.numberOfFiles", "2");
            // simulate restart
            TopLogging.flush(true);
            TopLogging.initialize();
        }
        } finally {
            System.clearProperty("org.netbeans.log.numberOfFiles");
        }

        File log = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log");
        assertTrue("Log file exists: " + log, log.canRead());

        File log1 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.1");
        assertTrue("Backup file exists: " + log1, log1.canRead());

        File log2 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.2");
        assertTrue("Backup file exists: " + log2, log2.canRead());

        File log3 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.3");
        assertFalse("Backup file does not exist: " + log3, log3.canRead());
    }

    public void testCanInfluenceBehaviourBySettingALevelProperty() throws Exception {
        System.setProperty(TopLoggingTest.class.getName() + ".level", "100");
        LogManager.getLogManager().readConfiguration();

        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.FINER, "Finer level msg");

        Pattern p = Pattern.compile("FINER.*Finer level msg");
        String disk = readLog(true);
        Matcher d = p.matcher(disk);

        if (!d.find()) {
            fail("msg shall be logged to file: " + disk);
        }
    }

    public void testCanInfluenceBehaviourBySettingALevelPropertyOnParent() throws Exception {
        System.setProperty("ha.nu.level", "100");
        Reference<?> ref = new WeakReference<Object>(Logger.getLogger("ha.nu.wirta"));
        assertGC("ha.nu.wirta should not exist after this line", ref);
        
        LogManager.getLogManager().readConfiguration();

        final Logger log = Logger.getLogger("ha.nu.wirta");
        log.log(Level.FINER, "Finer level msg");

        Pattern p = Pattern.compile("FINER.*Finer level msg");
        String disk = readLog(true);
        Matcher d = p.matcher(disk);

        if (!d.find()) {
            fail("msg shall be logged to file: " + disk);
        }
    }

    public void testCanInfluenceBehaviourBySettingALevelPropertyOnExistingParent() throws Exception {
        System.setProperty("ha.nu.level", "100");

        Logger l = Logger.getLogger("ha.nu.wirta");

        LogManager.getLogManager().readConfiguration();

        l.log(Level.FINER, "Finer level msg");

        Pattern p = Pattern.compile("FINER.*Finer level msg");
        String disk = readLog(true);
        Matcher d = p.matcher(disk);

        if (!d.find()) {
            fail("msg shall be logged to file: " + disk);
        }
    }

    public void testSystemErrIsSentToLog() throws Exception {
        System.err.println("Ahoj");
        System.err.println("Jardo");
        new IllegalStateException("Hi").printStackTrace();
        System.err.flush();

        if (handler != null) {
            handler.flush();
        }

        String disk = readLog(true);

        Matcher m = Pattern.compile("^Ahoj(.*)Jardo", Pattern.MULTILINE | Pattern.DOTALL).matcher(disk);
        assertTrue(disk, m.find());
        assertEquals("One group found", 1, m.groupCount());
        assertTrue("Non empty group: " + m.group(1) + "\n" + disk, m.group(1).length() > 0);
        char next = m.group(1).charAt(0);
        if (next != 10 && next != 13) {
            fail("Expecting 'Ahoj': index: " + 0 + " next char: " + (int)next + "text:\n" + disk);
        }
        
        Pattern p = Pattern.compile("IllegalStateException.*Hi");
        Matcher d = p.matcher(disk);
        if (!d.find()) {
            fail("Expecting exception: " + disk);
        }
    }

    
    public void testSystemErrPrintLnIsSentToLog() throws Exception {
        System.err.println("BEGIN");
        System.err.println("");
        System.err.println("END");
        System.err.flush();

        if (handler != null) {
            handler.flush();
        }

        String disk = readLog(true);
        Matcher m = Pattern.compile("BEGIN.*END", Pattern.MULTILINE | Pattern.DOTALL).matcher(disk);
        assertTrue("There is text between BEGINandEND\n" + disk, m.find());
        disk = m.group(0);
        disk = disk.replace('\n', 'n');
        disk = disk.replace('\r', 'r');

        if (org.openide.util.Utilities.isWindows()) {
            assertEquals("BEGINrnrnEND", disk);
        } else {
            assertEquals("BEGINnnEND", disk);
        }
    }

    
    public void testLoggingFromRequestProcessor() throws Exception {
        Logger.getLogger("org.openide.util.RequestProcessor").setLevel(Level.ALL);

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                
            }
        }).waitFinished();
        
    }

    private String readLog(boolean doFlush) throws IOException {
        if (doFlush) {
            TopLogging.flush(false);
        }

        File log = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log");
        assertTrue("Log file exists: " + log, log.canRead());

        FileInputStream is = new FileInputStream(log);

        byte[] arr = new byte[(int)log.length()];
        int r = is.read(arr);
        assertEquals("all read", arr.length, r);
        is.close();

        return new String(arr);
    }

    public void testAttachMessage() throws Exception { // #158906
        Exception e = new Exception("Help");
        String msg = "me please";
        Exception result = Exceptions.attachMessage(e, msg);
        assertSame(result, e);
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "background", e);
        String disk = readLog(true);
        assertTrue(disk, disk.contains("background"));
        assertTrue(disk, disk.contains("java.lang.Exception"));
        assertTrue(disk, disk.contains("Help"));
        assertTrue(disk, disk.contains("me please"));
    }

    public void testThreadDeath() throws Exception { // #203171
        Thread t = new Thread(new Runnable() {
            @Override public void run() {
                throw new ThreadDeath();
            }
        });
        t.start();
        t.join();
        String disk = readLog(true);
        assertFalse(disk, disk.contains("java.lang.ThreadDeath"));
    }

}
