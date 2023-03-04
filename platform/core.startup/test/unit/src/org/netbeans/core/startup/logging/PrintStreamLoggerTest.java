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
package org.netbeans.core.startup.logging;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static org.netbeans.core.startup.logging.PrintStreamLogger.BUFFER_SHRINK_TIME;
import static org.netbeans.core.startup.logging.PrintStreamLogger.BUFFER_THRESHOLD;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author sdedic
 */
public class PrintStreamLoggerTest extends NbTestCase {
    
    private static class LH extends Handler {
        List<LogRecord> records = new ArrayList<>();
        
        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
    
    private LH handler = new LH();
    
    private Logger logger = Logger.getLogger("test.pslogger");
    
    private PrintStreamLogger ps = (PrintStreamLogger)PrintStreamLogger.create("test.pslogger");
    
    private Object psLock;

    public PrintStreamLoggerTest(String name) {
        super(name);
    }
    
    public void setUp() throws Exception {
        logger.addHandler(handler);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
        Field f = ps.getClass().getDeclaredField("lock");
        f.setAccessible(true);
        psLock = f.get(ps);
    }
    
    public void tearDown() {
        LogManager.getLogManager().getLogger("test.pslogger").removeHandler(handler);
    }
    
    public void testContinousLogingFlushes() throws Exception {
        int count = 0;
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            data.append("12345678901234567890123456789012345678901234567890\n");
        }
        do {
            Thread.sleep(50);
            if (count < (BUFFER_THRESHOLD / data.length())) {
                assertEquals("Not buffering properly", 0, handler.records.size());
            }
            // 50k chars + newline
            ps.println(data);
        } while (++count < ((BUFFER_THRESHOLD / data.length()) * 2));
        assertFalse(handler.records.isEmpty());
   }

    public void testMemoryReclaimed() throws Exception {
        int count = 0;
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            data.append("12345678901234567890123456789012345678901234567890");
        }
        
        do {
            Thread.sleep(50);
            if (count < (BUFFER_THRESHOLD / data.length())) {
                assertEquals("Not buffering properly", 0, handler.records.size());
            }
            // 50k chars + newline
            ps.println(data);
        } while (++count < ((BUFFER_THRESHOLD / data.length()) * 2));
        // check that the logger has started to flush already
        assertFalse(handler.records.isEmpty());

        // wait until flush, check that the buffer is still large.
        synchronized (psLock) {
            ps.println(data);
            
            // wait & release the lock:
            psLock.wait(1000);

            int[] capacity = ps.bufferSizes();
            assertTrue(capacity[1] >= BUFFER_THRESHOLD);
        }
        Thread.sleep(5 * BUFFER_SHRINK_TIME / 2);
        synchronized (psLock) {
            ps.println(data);
            
            // wait & release the lock:
            psLock.wait(1000);
        }
        int[] capacity = ps.bufferSizes();
        assertTrue(capacity[1] < BUFFER_THRESHOLD);
   }
}
