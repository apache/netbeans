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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class DispatchingHandlerTest extends NbTestCase {
    
    public DispatchingHandlerTest(String s) {
        super(s);
    }
    
    @RandomlyFails // NB-Core-Build #9138, #9370: Unstable
    public void testContinuousMessagesShouldNotPreventOutput() throws InterruptedException {
        class MyHandler extends Handler {
            final List<LogRecord> records = new CopyOnWriteArrayList<LogRecord>();

            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
                records.clear();
            }
            
        }
        MyHandler mh = new MyHandler();
        DispatchingHandler dh = new DispatchingHandler(mh, 100);
        
        for (int i = 0; i < 100; i++) {
            dh.publish(new LogRecord(Level.INFO, "" + i));
            Thread.sleep(10);
            if (i > 50 && mh.records.isEmpty()) {
                fail("There should be some records when we are at round " + i);
            }
        }
        dh.flush();
        
        assertEquals("One hundered records now", 100, mh.records.size());
    }
    
    public void testOwnFormatter() throws UnsupportedEncodingException {
        class MyFrmtr extends Formatter {
            private int cnt;
            @Override
            public String format(LogRecord record) {
                cnt++;
                return record.getMessage();
            }
        }
        MyFrmtr my = new MyFrmtr();
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamHandler sh = new StreamHandler(os, NbFormatter.FORMATTER);
        DispatchingHandler dh = new DispatchingHandler(sh, 10);
        dh.setFormatter(my);
        dh.publish(new LogRecord(Level.WARNING, "Ahoj"));
        dh.flush();
        String res = new String(os.toByteArray(), StandardCharsets.UTF_8);
        assertEquals("Only the message is written", "Ahoj", res);
        assertEquals("Called once", 1, my.cnt);
    }
}
