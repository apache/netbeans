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
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.RequestProcessor;


/**
 * Checks that it is possible to log to console.
 */
public class TopLoggingNbLoggerConsoleTest extends TopLoggingTest {
    private static ByteArrayOutputStream w;
    private static PrintStream ps;
    static {
        final PrintStream OLD = System.err;
        System.setProperty("netbeans.logger.console", "true");
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
                TopLogging.flush(false);
                OLD.flush();

                String retValue;                
                retValue = super.toString();
                return retValue;
            }
        };

        ps = new PrintStream(w);
        System.setErr(ps);
    }


    public TopLoggingNbLoggerConsoleTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        System.setProperty("netbeans.user", getWorkDirPath());

        // initialize logging
        TopLogging.initialize();

        ps.flush();
        w.reset();
    }

    @Override
    protected ByteArrayOutputStream getStream() {
        return w;
    }

    @RandomlyFails
    public void testFlushHappensQuickly() throws Exception {
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

        Pattern p = Pattern.compile("INFO.*First visible message");

        Matcher d = null;
        String disk = null;
        // console gets flushed at 500ms
        for (int i = 0; i < 4; i++) {
            disk = w.toString("utf-8"); // this one is not flushing
            d = p.matcher(disk);
            if (!d.find()) {
                Thread.sleep(300);
            } else {
                return;
            }
        }

        fail("msg shall be logged to file: " + disk);
    }

    @RandomlyFails // NB-Core-Build #8225: "msg shall be logged to file: "
    public void testCycleWithConsoleLogger() throws Exception {
        ConsoleHandler h = new ConsoleHandler();

        try {
            Logger.getLogger("").addHandler(h);


            w.reset();
            Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

            Pattern p = Pattern.compile("INFO.*First visible message");
            Matcher m = p.matcher(getStream().toString("utf-8"));

            Matcher d = null;
            String disk = null;
            // console gets flushed at 500ms
            for (int i = 0; i < 4; i++) {
                disk = w.toString("utf-8"); // this one is not flushing
                d = p.matcher(disk);
                if (!d.find()) {
                    Thread.sleep(300);
                } else {
                    if (w.size() > d.end() + 300) {
                        fail("File is too big\n" + w + "\nsize: " + w.size() + " end: " + d.end());
                    }

                    return;
                }
            }

            fail("msg shall be logged to file: " + disk);
        } finally {
            Logger.getLogger("").removeHandler(h);
            
        }
    }


    public void testDeadlockConsoleAndStdErr() throws Exception {
        ConsoleHandler ch = new ConsoleHandler();
        
        Logger root = Logger.getLogger("");
        root.addHandler(ch);
        try {
            doDeadlockConsoleAndStdErr(ch);
        } finally {
            root.removeHandler(ch);
        }
    }
    
    private void doDeadlockConsoleAndStdErr(final ConsoleHandler ch) {
        class H extends Handler implements Runnable {
            public void publish(LogRecord record) {
                try {
                    RequestProcessor.getDefault().post(this).waitFinished(100);
                } catch (InterruptedException ex) {
                    // ex.printStackTrace();
                }
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
            
            public void run() {
                ch.publish(new LogRecord(Level.WARNING, "run"));
            }
        }
        H handler = new H();
        Logger.getLogger("stderr").addHandler(handler);
        
        System.err.println("Ahoj");
    }
    
}
