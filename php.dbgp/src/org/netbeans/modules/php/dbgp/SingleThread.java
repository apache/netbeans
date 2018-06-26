/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.dbgp;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

/**
 * @author Radek Matous
 */
public abstract class SingleThread extends ThreadPoolExecutor implements Runnable, Cancellable {
    final Object sync = new Object();
    FutureTask task;

    public SingleThread() {
        super(1, 1, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        setThreadFactory(getDaemonThreadFactory());
        setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                super.rejectedExecution(r, e);
                Logger.getLogger(getClass().getName()).info("rejectedExecution"); //NOI18N
            }
        });
    }

    public static ThreadFactory getDaemonThreadFactory() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();
                Thread newThread = defaultThreadFactory.newThread(r);
                newThread.setDaemon(true);
                return newThread;
            }
        };
    }

    public Object getSync() {
        return sync;
    }

    public final FutureTask invokeLater() {
        synchronized (sync) {
            task = new FutureTask(this, null);
            execute(task);
            return task;
        }
    }

    public final void invokeAndWait() throws InterruptedException, ExecutionException {
        synchronized (sync) {
            task = new FutureTask(this, null);
            execute(task);
            task.get();
        }
    }

    protected final void waitFinished() {
        synchronized (sync) {
            if (task != null) {
                try {
                    task.get(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    Thread.interrupted();
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (TimeoutException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    @Override
    public abstract boolean cancel();

}
