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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 * Checks the top logging delegates to handlers in lookup.
 */
public class TopLoggingLookupTest extends NbTestCase {
    private MyHandler handler;
    
    public TopLoggingLookupTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());

        MockServices.setServices();

        // initialize logging
        TopLogging.initialize();
    }


    protected void tearDown() throws Exception {
    }

    public void testLogOneLine() throws Exception {
        MockServices.setServices(MyHandler.class);
        handler = Lookup.getDefault().lookup(MyHandler.class);
        assertNotNull("Handler found", handler);

        
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

        assertEquals("[First visible message]", handler.logs.toString());
    }

    public void testDeadlock78865() throws Exception {
        MockServices.setServices(AnotherThreadLoggingHandler.class);
        handler = Lookup.getDefault().lookup(MyHandler.class);
        assertNotNull("Handler found", handler);

        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

        assertEquals("[First visible message]", handler.logs.toString());
    }

    public static class MyHandler extends Handler {
        public List<String> logs = new ArrayList<String>();

        public void publish(LogRecord record) {
            logs.add(record.getMessage());
        }

        public void flush() {
            logs.add("flush");
        }

        public void close() throws SecurityException {
            logs.add("close");
        }

    }
    public static final class AnotherThreadLoggingHandler extends MyHandler
    implements Runnable {
        public AnotherThreadLoggingHandler() {
            Logger.global.info("in constructor before");
            RequestProcessor.getDefault().post(this).waitFinished();
            Logger.global.info("in constructor after");
        }
        public void run() {
            Logger.global.warning("running in parael");
        }

    }
}
