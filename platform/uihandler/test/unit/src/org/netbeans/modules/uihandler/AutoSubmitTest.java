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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.zip.GZIPInputStream;
import javax.swing.SwingUtilities;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.uihandler.api.Activated;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
public class AutoSubmitTest extends NbTestCase {
    
    private Installer installer;
    
    static {
        MemoryURL.initialize();
    }
    
    public AutoSubmitTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected void setUp() throws Exception {
        UIHandler.flushImmediatelly();
        MetricsHandler.flushImmediatelly();
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        Locale.setDefault(new Locale("ts", "AU"));
        NbPreferences.root().node("org/netbeans/core").putBoolean("usageStatisticsEnabled", true);
        
        installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);
        //checkHandlers("After Installer find object", Logger.getLogger(Installer.UI_LOGGER_NAME));
        MockServices.setServices(A.class, D.class);
        //checkHandlers("After mock services set up", Logger.getLogger(Installer.UI_LOGGER_NAME));
        // Initialize the module system:
        Lookup.getDefault().lookupAll(ModuleInfo.class);
        //checkHandlers("After initialization of module system", Logger.getLogger(Installer.UI_LOGGER_NAME));
        MemoryURL.initialize();
        //checkHandlers("After all set up", Logger.getLogger(Installer.UI_LOGGER_NAME));
        installer.restored();
    }

    @Override
    protected void tearDown() throws Exception {
        installer.uninstalled();
    }
    
    /**
     * Tests auto-submission of UI logs.
     * 
     * @throws Exception 
     */
    @RandomlyFails
    public void testAutoSubmitUILogs() throws Exception {
        // Needs to be call in the thread's context class loader, due to mockup services
        Installer.logDeactivated();
        //checkHandlers("After log deactivate", Logger.getLogger(Installer.UI_LOGGER_NAME));
        // setup the listing
        //installer.restored();
        File logs = File.createTempFile("autoSubmitLogs", ".xml");
        logs.deleteOnExit();
        String utf8 = 
            "<html><head>" +
            "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'></meta>" +
            "</head>" +
            "<body>" +
            "<form action='file://"+logs.getAbsolutePath()+"' method='post'>" +
            "  <input name='submit' value='auto' type='hidden'> </input>" +
            "</form>" +
            "</body></html>";
        //ByteArrayInputStream is = new ByteArrayInputStream(utf8.getBytes("UTF-8"));
        MemoryURL.registerURL("memory://auto.html", utf8);
        //checkHandlers("After memory registry", Logger.getLogger(Installer.UI_LOGGER_NAME));
        
        Preferences prefs = NbPreferences.forModule(Installer.class);
        //checkHandlers("After Installer preferences loaded", Logger.getLogger(Installer.UI_LOGGER_NAME));
        prefs.putBoolean("autoSubmitWhenFull", true);
        //LogRecord r = new LogRecord(Level.INFO, "MSG_SOMETHING");
        //r.setLoggerName("org.netbeans.ui.anything");
        /*
        {
            Logger logger = Logger.getLogger("org.netbeans.ui.anything");
            while (logger != null) {
                Handler[] handlers = logger.getHandlers();
                System.err.println("Logger "+logger.getName()+" handlers = "+Arrays.asList(handlers));
                if (!logger.getUseParentHandlers()) {
                    break;
                }
                logger = logger.getParent();
            }
        }
        */
        //checkHandlers("Before getLogsSizeTest()", Logger.getLogger(Installer.UI_LOGGER_NAME));
        int n1 = Installer.getLogsSizeTest();
        //checkHandlers("After getLogsSizeTest()", Logger.getLogger(Installer.UI_LOGGER_NAME));
        int n = UIHandler.MAX_LOGS + 1 - n1;
        //checkHandlers("Before logging loop", Logger.getLogger(Installer.UI_LOGGER_NAME));
        for (int i = 0; i < n; i++) {
            //Installer.writeOut(r);
            LogRecord r = new LogRecord(Level.INFO, "MSG_SOMETHING"+i);
            r.setLoggerName(Installer.UI_LOGGER_NAME + ".anything");
            Logger logger = Logger.getLogger(Installer.UI_LOGGER_NAME + ".anything");
            //System.err.println("handlers = "+Arrays.toString(handlers));
            //if (i == 0) {
            //    checkHandlers("Right before log", logger.getParent());
            //    checkHandlers("Begore log with main UI logger", Logger.getLogger(Installer.UI_LOGGER_NAME));
            //}
            logger.log(r);
        }
        UIHandler.waitFlushed();
        
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                // Empty just to get over the last scheduled task invoked in AWT.
            }
        });
        waitTillTasksInRPFinish(Installer.RP_SUBMIT);
        
        checkMessagesIn(logs, n);
        logs.delete();
    }
    
    private static void waitTillTasksInRPFinish(RequestProcessor rp) throws Exception {
        // Perhaps there is a better way to wait until all scheduled tasks are processed...?
        do {
            Field runningFiled = RequestProcessor.class.getDeclaredField("processors");
            runningFiled.setAccessible(true);
            Set<?> running = (Set<?>) runningFiled.get(rp);
            if (running.isEmpty()) {
                break;
            } else {
                Thread.sleep(500);
            }
        } while (true);
    }
    
    private static boolean checkHandlers(String msg, Logger logger) {
        Handler[] handlers = logger.getHandlers();
        int n = 0;
        for (Handler h : handlers) {
            if (h instanceof UIHandler && !((UIHandler) h).isExceptionOnly()) {
                n++;
            }
        }
        System.err.println(msg+" Handlers with not exception only = "+n+", logger = "+logger);
        return n == 1;
    }
    
    private static void checkMessagesIn(File logs, int n) throws Exception {
        InputStream in = new FileInputStream(logs);
        byte[] bytes = new byte[(int) logs.length()];
        int from = 0;
        int l;
        while ((l = in.read(bytes, from, bytes.length - from)) > 0) {
            from += l;
        }
        in.close();
        l = from;
        //System.err.println("Read file length = "+l);
        //System.err.println("File size = "+logs.length());
        // Skip the header:
        // GZIP header magic number.
        byte b0 = GZIPInputStream.GZIP_MAGIC & 255;
        byte b1 = (byte) (GZIPInputStream.GZIP_MAGIC >> 8);
        
        int begin = 0;
        for (int i = 0; i < (l - 1); i++) {
            if (bytes[i] == b0 && bytes[i + 1] == b1) {
                begin = i;
                break;
            }
        }
        
        //System.err.println("Begin = "+begin);
        
        GZIPInputStream gin;
        try {
            gin = new GZIPInputStream(new ByteArrayInputStream(bytes, begin, l - begin));
        } catch (EOFException eofex) {
            IOException ioex = new IOException("checkMessagesIn("+n+"), bytes.length = "+bytes.length+", begin = "+begin+", l = "+l, eofex);
            throw ioex;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(gin));
        //StringBuilder sb = new StringBuilder();
        String line;
        String lastMessage = "<message>MSG_SOMETHING"+(n - 1)+"</message>";
        String afterLastMessage = "<message>MSG_SOMETHING"+n+"</message>";
        boolean isLastMessage = false;
        boolean isAfterLastMessage = false;
        while ((line = br.readLine()) != null) {
            //sb.append(line);
            //sb.append('\n');
            line = line.trim();
            if (lastMessage.equals(line)) {
                isLastMessage = true;
            }
            if (afterLastMessage.equals(line)) {
                isAfterLastMessage = true;
            }
        }
        br.close();
        //System.err.println("AUTO FILE CONTENT = "+sb);
        //System.err.println("isLastMessage("+lastMessage+") = "+isLastMessage);
        //System.err.println("isAfterLastMessage("+afterLastMessage+") = "+isAfterLastMessage);
        
        assertTrue("Last message '"+lastMessage+"' not found in the log.", isLastMessage);
        assertFalse("Messages following the last message '"+lastMessage+"' are present in the log.", isAfterLastMessage);
    }

    public static final class A implements Activated {
        @Override
        public void activated(Logger uiLogger) {
            //uiLogger.config("A started");
        }
    }
    public static final class D implements Deactivated {
        @Override
        public void deactivated(Logger uiLogger) {
            //uiLogger.config("D stopped");
        }
    }
}
