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
package org.openide.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.Test;
import org.openide.util.RequestProcessor;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class RequestProcessorLoggingTest extends Handler {
    private Thread thread;
    private static RequestProcessor rp = new RequestProcessor("Does logging");
    private volatile boolean isRP = true;
    private volatile int cnt;

    @Override
    public void publish(LogRecord record) {
        if (Thread.currentThread() == thread) {
            cnt++;
            isRP &= rp.isRequestProcessorThread();
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    @Test public void isRequestProcessorThreadProperlySetDuringLogging() {
        Logger l = Logger.getLogger(RequestProcessor.class.getName());
        l.addHandler(this);
        l.setLevel(Level.ALL);
        setLevel(Level.ALL);
        
        class R implements Runnable {
            @Override
            public void run() {
                thread = Thread.currentThread();
            }
        }
        rp.post(new R()).waitFinished();
        
        assertNotNull("Thread remembered", thread);
        
        class Empty implements Runnable {
            @Override
            public void run() {
            }
        }
        rp.post(new Empty()).waitFinished();
        
        if (cnt == 0) {
            fail("At least some logging should occur");
        }
        assertTrue("isRequestProcessorThread worked OK", isRP);
    }
}
