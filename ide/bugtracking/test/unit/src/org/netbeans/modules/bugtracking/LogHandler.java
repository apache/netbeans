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

package org.netbeans.modules.bugtracking;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author tomas
 */
public class LogHandler extends Handler {
    private static final long TIMEOUT = 60 * 1000;
    private final String msg;
    private boolean done = false;
    private final Compare compare;
    public enum Compare {
        STARTS_WITH,
        ENDS_WITH
    }

    public LogHandler(String msg, Compare compare) {
        this.msg = msg;
        this.compare = compare;
        BugtrackingManager.LOG.addHandler(this);
    }

    @Override
    public void publish(LogRecord record) {
        if(!done) {
            switch (compare) {
                case STARTS_WITH :
                    done = record.getMessage().startsWith(msg);
                    break;
                case ENDS_WITH :
                    done = record.getMessage().endsWith(msg);
                    break;
                default:
                    throw new IllegalStateException("LogHandler - wrong value " + compare);
            }
            if(done) {
                System.out.println("LogHandler - DONE [" + record.getMessage() + "]");
            }
        }
    }

    public void reset() {
        done = false;
        System.out.println("LogHandler - RESET [" + msg + "]");
    }

    public boolean isDone() {
        return done;
    }
    
    @Override
    public void flush() { }
    @Override
    public void close() throws SecurityException { }

    public void waitUntilDone() throws InterruptedException {
        long t = System.currentTimeMillis();
        while(!done) {
            System.out.println("LogHandler - SLEEP [" + msg + "]");
            Thread.sleep(200);
            if(System.currentTimeMillis() - t > TIMEOUT) {
                throw new IllegalStateException("LogHandler timeout > " + TIMEOUT);
            }
        }
    }
}
