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

package org.netbeans.modules.project.indexingbridge;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Allows parser indexing to be temporarily suppressed.
 * Unlike {@code org.netbeans.modules.parsing.api.indexing.IndexingManager}
 * this is not block-scoped. Every call to {@link #protectedMode} must
 * eventually be matched by exactly one call to {@link Lock#release}.
 * It is irrelevant which thread makes each call. It is permissible to make
 * multiple enter calls so long as each lock is released.
 */
public abstract class IndexingBridge {


    /**
     * IndexingBridge which allows a caller {@link IndexingBridge#protectedMode(boolean)}
     * to wait for not yet processed indexing tasks.
     * @since 1.5
     */
    public abstract static class Ordering extends IndexingBridge {
        /**
         * Waits until the non processes indexing tasks are done.
         * @throws InterruptedException when the waiting thread is interrupted.
         */
        protected abstract void await() throws InterruptedException;
    }

    private static final Logger LOG = Logger.getLogger(IndexingBridge.class.getName());

    protected IndexingBridge() {}

    /**
     * Begin suppression of indexing.
     * @return a lock indicating when to resume indexing
     */
    public final Lock protectedMode() {
        return protectedMode(false);
    }

    /**
     * Begin suppression of indexing.
     * @return a lock indicating when to resume indexing
     * @param waitForScan if ture and if the implementation of {@link IndexingBridge}
     * supports waits for not yet processed indexing tasks before entering to protected mode.
     * @return a lock indicating when to resume indexing
     * @since 1.5
     */
    public final Lock protectedMode(final boolean waitForScan) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, null, new Throwable("IndexingBridge.protectedMode"));
        }
        if (waitForScan && (this instanceof Ordering)) {
            try {
                ((Ordering)this).await();
            } catch (InterruptedException ex) {
                //pass: cancel of running task.
            }
        }
        enterProtectedMode();
        return new Lock();
    }

    /**
     * @see #protectedMode
     */
    public final class Lock {

        private final Stack creationStack = new Stack("locked here");
        private Stack releaseStack;

        /**
         * End suppression of indexing.
         * Indexing may resume if this is the last matching call.
         */
        public void release() {
            synchronized (IndexingBridge.this) {
                if (releaseStack != null) {
                    LOG.log(Level.WARNING, null, new IllegalStateException("Attempted to release lock twice", releaseStack));
                    return;
                }
                releaseStack = new Stack("released here", creationStack);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, null, new Throwable("IndexingBridge.Lock.release"));
                }
            }
            exitProtectedMode();
        }

        @SuppressWarnings("FinalizeDeclaration")
        @Override protected void finalize() throws Throwable {
            super.finalize();
            synchronized (IndexingBridge.this) {
                if (releaseStack != null) {
                    return;
                }
                LOG.log(Level.WARNING, "Unreleased lock", creationStack);
                releaseStack = new Stack("released here", creationStack);
            }
            exitProtectedMode();
        }
        
    }

    /**
     * SPI to enter protected mode semaphore. Will be matched eventually by one call to {@link #exitProtectedMode}.
     */
    protected abstract void enterProtectedMode();

    /**
     * SPI to exit protected mode semaphore. Will follow one call to {@link #enterProtectedMode}.
     */
    protected abstract void exitProtectedMode();

    /**
     * Gets the registered singleton of the bridge.
     * If none is registered, a dummy implementation is produced which tracks lock usage but does nothing else.
     */
    public static IndexingBridge getDefault() {
        IndexingBridge b = Lookup.getDefault().lookup(IndexingBridge.class);
        return b != null ? b : new IndexingBridge() {
            @Override protected void enterProtectedMode() {}
            @Override protected void exitProtectedMode() {}
        };
    }

    private static final class Stack extends Throwable {
        Stack(String msg) {
            super(msg);
        }
        Stack(String msg, Stack prior) {
            super(msg, prior);
        }
        @Override public synchronized Throwable fillInStackTrace() {
            boolean asserts = false;
            assert asserts = true;
            return asserts ? super.fillInStackTrace() : this;
        }
    }

}
