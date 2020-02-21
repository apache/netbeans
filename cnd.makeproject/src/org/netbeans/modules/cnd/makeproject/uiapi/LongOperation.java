/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
