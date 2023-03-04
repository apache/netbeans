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

package org.netbeans.modules.bugzilla;

import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author tomas
 */
public class LogHandler extends Handler {
    public static long DEFAULT_TIMEOUT = 30 * 1000;
    private long TIMEOUT = DEFAULT_TIMEOUT;
    private final String messageToWaitFor;
    private String interceptedMessage;
    private boolean done = false;
    private final Compare compare;
    private final int expectedCount;
    private int interceptedCount = 0;
    private boolean reset = false;
    public enum Compare {
        STARTS_WITH,
        ENDS_WITH
    }

    public LogHandler(String msg, Compare compare) {
        this(msg, compare, DEFAULT_TIMEOUT);
    }
    
    public LogHandler(String msg, Compare compare, long timeout) {
        this(msg, compare, timeout, 1);
    }

    public LogHandler(String msg, Compare compare, long timeout, int count) {
        this.expectedCount = count;
        this.messageToWaitFor = msg;
        this.compare = compare;
        Bugzilla.LOG.addHandler(this);
        if(timeout > -1) {
            TIMEOUT = timeout * 1000;
        }
    }

    @Override
    public void publish(LogRecord record) {
        if(!done) {
            String message = record.getMessage();
            if(message == null) {
                return;
            }
            message = MessageFormat.format(message, record.getParameters());
            boolean intercepted = false;
            switch (compare) {
                case STARTS_WITH :
                    intercepted = message.startsWith(messageToWaitFor);
                    break;
                case ENDS_WITH :
                    intercepted = message.endsWith(messageToWaitFor);
                    break;
                default:
                    throw new IllegalStateException("wrong value " + compare);
            }
            if(intercepted) {
                interceptedCount++;
                interceptedMessage = message;
            }
            done = intercepted && interceptedCount >= expectedCount;
        }
    }

    public int getInterceptedCount() {
        return interceptedCount;
    }
    
    public boolean isDone() {
        return done;
    }

    public void reset() {
        reset = true;
    }

    public String getInterceptedMessage() {
        return interceptedMessage;
    }

    @Override
    public void flush() { }
    @Override
    public void close() throws SecurityException { }

    public void waitUntilDone() throws InterruptedException {
        reset = false;
        long t = System.currentTimeMillis();
        while(!done && !reset && interceptedCount < expectedCount) {
            Thread.sleep(200);
            if(System.currentTimeMillis() - t > TIMEOUT) {
                throw new IllegalStateException("Timeout > " + TIMEOUT + " for " + messageToWaitFor);
            }
        }
    }
}
