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

package org.netbeans.modules.tasklist.impl;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A hack into Java Parser to get notifications when classpath scanning started/finished
 * so that we can pause task scanning at that time.
 *
 * @author S. Aubrecht
 */
class ScanMonitor {

    private static ScanMonitor INSTANCE;

    private final Object LOCK = new Object();
    private boolean locked = false;

    private ScanMonitor() {
        Logger logger = Logger.getLogger("org.netbeans.modules.java.source.usages.RepositoryUpdater.activity"); //NOI18N
        logger.setLevel(Level.FINEST);
        logger.setUseParentHandlers(false);
        logger.addHandler( new Handler() { //NOI18N

            @Override
            public void publish(LogRecord record) {
                if( Level.FINEST.equals( record.getLevel() )
                        && "START".equals(record.getMessage()) ) { //NOI18N
                    lock();
                    return;
                }

                if( Level.FINEST.equals( record.getLevel() )
                        && "FINISHED".equals(record.getMessage()) ) { //NOI18N
                    unlock();
                    return;
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
    }

    public static ScanMonitor getDefault() {
        if( null == INSTANCE ) {
            INSTANCE = new ScanMonitor();
        }
        return INSTANCE;
    }

    /**
     * The method is blocking as long as classpath scanning is in progress.
     */
    public void waitEnabled() {
        synchronized( LOCK ) {
            if( locked ) {
                try {
                    LOCK.wait();
                } catch (InterruptedException ex) {
                   //ignore
                }
            }
        }
    }

    private void lock() {
        synchronized( LOCK ) {
            locked = true;
        }
    }

    private void unlock() {
        synchronized( LOCK ) {
            locked = false;
            LOCK.notifyAll();
        }
    }
}
