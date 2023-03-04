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
package org.netbeans.modules.parsing.impl.indexing;

import org.netbeans.modules.parsing.spi.indexing.SuspendStatus;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Tomas Zezula
 */
public final class SuspendSupport {

    private static final Logger LOG = Logger.getLogger(SuspendSupport.class.getName());
    private static final boolean NO_SUSPEND = Boolean.getBoolean("SuspendSupport.disabled");    //NOI18N

    private final RequestProcessor worker;
    private final Object lock = new Object();
    private final ThreadLocal<Boolean> ignoreSuspend = new ThreadLocal<Boolean>();
    private final SuspendStatus suspendStatus = SPIAccessor.getInstance().createSuspendStatus(new DefaultImpl());
    //@GuardedBy("lock")
    private int suspedDepth;
    
    public static final SuspendStatus NOP = SPIAccessor.getInstance().createSuspendStatus(new NopImpl());

    
    @NonNull
    public SuspendStatus getSuspendStatus() {
        return suspendStatus;
    }
    
    public static interface SuspendStatusImpl {
        public boolean isSuspendSupported();
        public boolean isSuspended();
        public void parkWhileSuspended() throws InterruptedException;
    }
    
    
    
//-- Package private --
    
    SuspendSupport(@NonNull final RequestProcessor worker) {
        Parameters.notNull("worker", worker);   //NOI18N
        this.worker = worker;
    }

    void suspend() {
        if (NO_SUSPEND) {
            return;
        }
        if (worker.isRequestProcessorThread()) {
            return;
        }
        synchronized(lock) {
            suspedDepth++;
            if (LOG.isLoggable(Level.FINE) && suspedDepth == 1) {
                LOG.log(
                    Level.FINE,
                    "SUSPEND: {0}", //NOI18N
                    Arrays.toString(Thread.currentThread().getStackTrace()));
            }
        }
    }

    void resume() {
        if (NO_SUSPEND) {
            return;
        }
        if (worker.isRequestProcessorThread()) {
            return;
        }
        synchronized(lock) {
            assert suspedDepth > 0;
            suspedDepth--;
            if (suspedDepth == 0) {
                lock.notifyAll();
                LOG.fine("RESUME"); //NOI18N
            }
        }
    }
    
    void runWithNoSuspend(final Runnable work) {
        ignoreSuspend.set(Boolean.TRUE);
        try {
            work.run();
        } finally {
            ignoreSuspend.remove();
        }
    }
    
    private static final class NopImpl implements SuspendStatusImpl {
        @Override
        public boolean isSuspendSupported() {
            return true;
        }
        @Override
        public boolean isSuspended() {
            return false;
        }
        @Override
        public void parkWhileSuspended() throws InterruptedException {
        }
    }
    
    private final class DefaultImpl implements SuspendStatusImpl {
        @Override
        public boolean isSuspendSupported() {
            return ignoreSuspend.get() != Boolean.TRUE;
        }

        @Override
        public boolean isSuspended() {
            if (ignoreSuspend.get() == Boolean.TRUE) {
                return false;
            }
            synchronized(lock) {
                return suspedDepth > 0;
            }
        }

        @Override
        public void parkWhileSuspended() throws InterruptedException {
            if (ignoreSuspend.get() == Boolean.TRUE) {
                return;
            }
            synchronized(lock) {
                boolean parked = false;
                while (suspedDepth > 0) {
                    LOG.fine("PARK");   //NOI18N
                    lock.wait();
                    parked = true;
                }
                if (LOG.isLoggable(Level.FINE) && parked) {
                    LOG.fine("UNPARK");   //NOI18N
                }
            }
        }
    }

}
