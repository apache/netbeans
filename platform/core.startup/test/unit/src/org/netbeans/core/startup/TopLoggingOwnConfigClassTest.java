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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.logging.XMLFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;


/**
 * Checks the behaviour of NetBeans logging support.
 */
public class TopLoggingOwnConfigClassTest extends NbTestCase {
    static File log;
    
    public TopLoggingOwnConfigClassTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();

        System.setProperty("netbeans.user", getWorkDirPath());

        log = new File(getWorkDir(), "own.log");

        System.setProperty("java.util.logging.config.class", Cfg.class.getName());

        // initialize logging
        TopLogging.initialize();
    }

    protected void tearDown() throws Exception {
    }


    public void testLogOneLine() throws Exception {
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.FINER, "First visible message");

        String content = readLog();
        if (content.indexOf("<!DOCTYPE") == -1) {
            fail("Content must be XML based: " + content);
        }

        if (content.indexOf("First vis") == -1) {
            fail("It must contain our log message: " + content);
        }
    }

    private String readLog() throws IOException {
        Handler[] ha = Logger.getLogger("").getHandlers();
        assertEquals("There is one handler", 1, ha.length);
        ha[0].flush();

        assertTrue("Log file exists: " + log, log.canRead());

        FileInputStream is = new FileInputStream(log);

        byte[] arr = new byte[(int)log.length()];
        int r = is.read(arr);
        assertEquals("all read", arr.length, r);
        is.close();

        return new String(arr);
    }

    public static final class Cfg extends Object {
        public Cfg() throws IOException {

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            OutputStreamWriter w = new OutputStreamWriter(os);
            w.write("handlers=java.util.logging.FileHandler\n");
            w.write(".level=100\n");
            w.write("java.util.logging.FileHandler.pattern=" + log.toString().replace('\\', '/') +"\n");
            w.close();

            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(os.toByteArray()));
            
        }
    }
}
