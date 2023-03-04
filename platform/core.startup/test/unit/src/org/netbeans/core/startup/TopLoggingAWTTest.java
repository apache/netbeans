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

import java.awt.EventQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

public class TopLoggingAWTTest extends NbTestCase 
implements Thread.UncaughtExceptionHandler {
    static {
        assertNull("No handler yet", Thread.getDefaultUncaughtExceptionHandler());
        Thread.setDefaultUncaughtExceptionHandler(new TopLoggingAWTTest("handler"));
    }
    private static Throwable uncaught;
    private static LogRecord published;
    
    public TopLoggingAWTTest(String name) {
        super(name);
    }

    public void testAWTErrorReported() throws Exception {
        TopLogging.initialize();
        MockServices.setServices(MyHandler.class);
        
        final IllegalStateException ex = new IllegalStateException("I am broken");
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                throw ex;
            }
        });
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
            }
        });
        assertEquals("Exception provided", ex, getPublished().getThrown());
        assertNotNull("Our handler called", uncaught);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        uncaught = e;
    }
    
    public static synchronized LogRecord getPublished() throws InterruptedException {
        while (published == null) {
            TopLoggingAWTTest.class.wait();
        }
        return published;
    }
    
    public static synchronized void setPublished(LogRecord r) {
        published = r;
        TopLoggingAWTTest.class.notifyAll();
    }
    
    public static final class MyHandler extends Handler {
        
        @Override
        public void publish(LogRecord record) {
            setPublished(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
        
    }
}
