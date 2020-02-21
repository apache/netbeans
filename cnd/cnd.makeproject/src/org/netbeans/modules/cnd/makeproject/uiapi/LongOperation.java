/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.makeproject.uiapi;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.spi.project.ActionProgress;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class LongOperation {
    public static final class CanceledState {
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicBoolean interruptable = new AtomicBoolean(true);
        private final ActionProgress actionProgress;
        
        private CanceledState(ActionProgress actionProgress) {
            this.actionProgress = actionProgress;
        }
        
        public synchronized void cancel() {
            cancelled.set(true);
            if (actionProgress != null) {
                actionProgress.finished(false);
            }
        }
        
        public synchronized boolean isCanceled() {
            return cancelled.get();
        }

        public synchronized void setInterruptable(boolean interruptable){
            this.interruptable.set(interruptable);
        }

        public synchronized boolean isInterruptable(){
            return interruptable.get();
        }

        public ActionProgress getActionProgress() {
            return actionProgress;
        }
    }
    
    public abstract static class CancellableTask implements Runnable, Cancellable {
        private volatile Thread thread;
        private final CanceledState cancelled;

        public CancellableTask(ActionProgress actionProgress) {
            cancelled = new CanceledState(actionProgress);
        }
 
        protected abstract void runImpl();
        
        @Override
        public final void run() {
            thread = Thread.currentThread();
            if (!cancelled.isCanceled()) {
                runImpl();
            }
        }

        @Override
        public boolean cancel() {
            cancelled.cancel();
            if (thread != null && cancelled.isInterruptable()) { // we never set it back to null => no sync
                thread.interrupt();
            }
            return true;
        }

        public CanceledState getCancelled() {
            return cancelled;
        }
    }
    
    public abstract void executeLongOperation(CancellableTask task, String title, String message);
    public abstract void executeLongOperation2(Runnable task, String title, String message);
    
    private static final Default DEFAULT = new Default();

    public static LongOperation getLongOperation() {
        LongOperation defaultFactory = Lookup.getDefault().lookup(LongOperation.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    private static final class Default extends LongOperation {

        @Override
        public void executeLongOperation(CancellableTask task, String title, String message) {
            task.run();
        }

        @Override
        public void executeLongOperation2(Runnable task, String title, String message) {
            task.run();
        }
    }
}
