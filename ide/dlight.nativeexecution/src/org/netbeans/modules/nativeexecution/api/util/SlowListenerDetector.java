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
package org.netbeans.modules.nativeexecution.api.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

/**
 *
 * @author vkvashin
 */
class SlowListenerDetector {
    
    private final Logger logger;
    private final Level level;
    private final Timer timer;
    private final int timeout;
    
    private final Object lock = new Object();
    private Thread thread;
    private StackTraceElement[] stackTrace;
    private long startTime;
    private String methodName;

    public SlowListenerDetector(int timeout, Logger log, Level level) {
        this.logger = log;
        this.level = level;
        this.timeout = timeout;
        timer = new Timer(timeout, new TimerListener());
    }

    private void clear() {
        synchronized (lock) {
            thread = null;
            stackTrace = null;
            startTime = 0;
        }
    }
    
    public void start(String methodName) {
        synchronized (lock) {
            clear();
            this.methodName = methodName;
            thread = Thread.currentThread();
            startTime = System.currentTimeMillis();
        }
        timer.start();
    }
    
    public void stop() {
        timer.stop();
        Exception ex = null;
        synchronized (lock) {
            long time = System.currentTimeMillis() - startTime;
            if (time > timeout && stackTrace != null) {
                ex = new SlowListenerException("Too much time spent in " +  //NOI18N
                        methodName + ": " + time + " ms", stackTrace); //NOI18N
            }
            clear();
        }
        if (ex != null) {
            logger.log(level, ex.getMessage(), ex);
        }
    }

    private class TimerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            timer.stop();
            synchronized(lock) {
                stackTrace = thread.getStackTrace();
            }
        }
    }
    
    private static class SlowListenerException extends RuntimeException {
        public SlowListenerException(String message, StackTraceElement[] stackTrace) {
            super(message);
            setStackTrace(stackTrace);
        }        
    }
}
