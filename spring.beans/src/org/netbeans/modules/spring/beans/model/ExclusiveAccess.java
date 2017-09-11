/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.spring.beans.model;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;


/**
 * An utility for managing exclusive access.
 *
 * @author Andrei Badea
 */
public final class ExclusiveAccess {

    // TODO improve the priority of runSyncTask() tasks.

    private final static ExclusiveAccess INSTANCE = new ExclusiveAccess();

    private final RequestProcessor rp = new RequestProcessor("Spring config file access thread", 1, false); // NOI18N
    private final ReentrantLock lock = new ReentrantLock();

    public static ExclusiveAccess getInstance() {
        return INSTANCE;
    }

    /**
     * Posts a task which will be run with exclusive access at some time
     * in the future and returns immediately.
     *
     * @param  run the task.
     */
    public AsyncTask createAsyncTask(Runnable run) {
        return new AsyncTask(rp.create(new TaskWrapper(run), true));
    }

    /**
     * Runs a priority task synchronously (the method returns after the task
     * has run).
     *
     * @param  run the task.
     */
    public <V> V runSyncTask(Callable<V> task) throws Exception {
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks whether the current thread has exclusive access (that is,
     * it is running a task posted through {@link #postTask} or {@link #runPriorityTask}.
     *
     * @return true if the current thread has exclusive access, false otherwise.
     */
    public boolean isCurrentThreadAccess() {
        return lock.isHeldByCurrentThread();
    }

    public static final class AsyncTask {

        private final Task task;

        AsyncTask(RequestProcessor.Task task) {
            this.task = task;
        }

        public void schedule(int delay) {
            task.schedule(delay);
        }

        public boolean cancel() {
            if (task.cancel()) {
                return true;
            }
            return false;
        }

        public boolean isFinished() {
            return task.isFinished();
        }
    }

    /**
     * Wraps a runnable inside the lock to make sure it has exclusive access.
     */
    private final class TaskWrapper implements Runnable {

        private final Runnable delegate;

        public TaskWrapper(Runnable delegate) {
            this.delegate = delegate;
        }

        public void run() {
            lock.lock();
            try {
                delegate.run();
            } finally {
                lock.unlock();
            }
        }
    }
}
