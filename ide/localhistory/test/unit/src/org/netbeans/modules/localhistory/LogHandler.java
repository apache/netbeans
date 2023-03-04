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

package org.netbeans.modules.localhistory;

import java.text.MessageFormat;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.openide.util.Exceptions;

/**
 *
 * @author tomas
 */
public class LogHandler extends Handler {
    private long TIMEOUT = 30 * 1000;
    private final String messageToWaitFor;
    private String interceptedMessage;
    private int hits = 0;
    private final Compare compare;
    private long blockTO = -1;

    public enum Compare {
        STARTS_WITH,
        ENDS_WITH
    }

    public LogHandler(String msg, Compare compare) {
        this(msg, compare, -1);
    }

    public LogHandler(String msg, Compare compare, int timeout) {
        this.messageToWaitFor = msg;
        this.compare = compare;
        LocalHistory.LOG.addHandler(this);
        if(timeout > -1) {
            TIMEOUT = timeout * 1000;
        }
    }

    @Override
    public void publish(LogRecord record) {
        if(hits < 1) {
            String message = record.getMessage();
            if(message == null) {
                return;
            }
            message = MessageFormat.format(message, record.getParameters());
            switch (compare) {
                case STARTS_WITH :
                    if(message.startsWith(messageToWaitFor)) {
                        hits++;
                    }
                    break;
                case ENDS_WITH :
                    if(message.endsWith(messageToWaitFor)) {
                        hits++;
                    }
                    break;
                default:
                    throw new IllegalStateException("wrong value " + compare);
            }
            if(hits > 0) {
                long t = System.currentTimeMillis();
                while(blockTO > -1 && System.currentTimeMillis() - t < blockTO) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                interceptedMessage = message;
            }
        }
    }

    public void reset() {
        hits = 0;
    }

    public void block(long blockTO) {
        this.blockTO = blockTO;
    }
    
    public void unblock() {
        this.blockTO = -1;
    }
    
    public boolean isDone() {
        return hits > 0;
    }

    public int getHits() {
        return hits;
    }

    public String getInterceptedMessage() {
        return interceptedMessage;
    }

    @Override
    public void flush() { }
    @Override
    public void close() throws SecurityException { }

    public void waitUntilDone() throws InterruptedException, TimeoutException {        
        long t = System.currentTimeMillis();
        while(hits < 1) {
            Thread.sleep(100);
            if(System.currentTimeMillis() - t > TIMEOUT) {
                throw new TimeoutException("timout while waiting for log message containing '" + messageToWaitFor + "'");
            }
        }
    }
    
    public void waitForHits(int hits, long timeout) throws InterruptedException, TimeoutException {        
        if(timeout < 0) {
            timeout = TIMEOUT;
        }
        long t = System.currentTimeMillis();
        while(this.hits < hits) {
            Thread.sleep(100);
            if(System.currentTimeMillis() - t > timeout) {
                throw new TimeoutException("timout while waiting for log message containing '" + messageToWaitFor + "'");
            }
        }
    }
}
