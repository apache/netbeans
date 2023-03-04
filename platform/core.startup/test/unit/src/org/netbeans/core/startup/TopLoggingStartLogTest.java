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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;


/**
 * Checks the behaviour of NetBeans logging support.
 */
public class TopLoggingStartLogTest extends NbTestCase {
    private ByteArrayOutputStream w;
    private Handler handler;
    private Logger logger;
    
    static {
        System.setProperty("org.netbeans.log.startup", "print"); // NOI18N
    }
    
    public TopLoggingStartLogTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

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
                TopLogging.flush(false);
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


    protected ByteArrayOutputStream getStream() {
        return w;
    }

    @RandomlyFails // NB-Core-Build #1659
    public void testProgress() throws Exception {
        StartLog.logProgress("First visible message");

        Pattern p = Pattern.compile("@[0-9]+.*First visible message");
        Matcher m = p.matcher(getStream().toString());

        if (!m.find()) {
            fail("msg shall be logged: " + getStream().toString());
        }

        String disk = readLog(true);
        Matcher d = p.matcher(disk);

        if (!d.find()) {
            fail("msg shall be logged to file: " + disk);
        }

    }
    
    public void testStartEnd() throws Exception {
        StartLog.logStart("run");
        StartLog.logEnd("run");

        {
            Pattern p = Pattern.compile("@[0-9]+.*run.*started");
            Matcher m = p.matcher(getStream().toString());

            if (!m.find()) {
                fail("msg shall be logged: " + getStream().toString());
            }

            String disk = readLog(true);
            Matcher d = p.matcher(disk);

            if (!d.find()) {
                fail("msg shall be logged to file: " + disk);
            }
        }
        
        {
            Pattern p = Pattern.compile("@[0-9]+.*run.*finished");
            Matcher m = p.matcher(getStream().toString());

            if (!m.find()) {
                fail("msg shall be logged: " + getStream().toString());
            }

            String disk = readLog(true);
            Matcher d = p.matcher(disk);

            if (!d.find()) {
                fail("msg shall be logged to file: " + disk);
            }
        }

    }
    
    public void testStartEndToLogger() throws Exception {
        Logger LOG = Logger.getLogger("org.netbeans.log.startup");
        LOG.log(Level.FINE, "start", "run");
        LOG.log(Level.FINE, "end", "run");

        {
            Pattern p = Pattern.compile("@[0-9]+.*run.*started");
            Matcher m = p.matcher(getStream().toString());

            if (!m.find()) {
                fail("msg shall be logged: " + getStream().toString());
            }

            String disk = readLog(true);
            Matcher d = p.matcher(disk);

            if (!d.find()) {
                fail("msg shall be logged to file: " + disk);
            }
        }
        
        {
            Pattern p = Pattern.compile("@[0-9]+.*run.*finished");
            Matcher m = p.matcher(getStream().toString());

            if (!m.find()) {
                fail("msg shall be logged: " + getStream().toString());
            }

            String disk = readLog(true);
            Matcher d = p.matcher(disk);

            if (!d.find()) {
                fail("msg shall be logged to file: " + disk);
            }
        }

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

}
