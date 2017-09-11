/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.ui;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.openide.util.RequestProcessor;

/**
 * Really lazy scanning of Java sources. Use when it needs to be called from AWT.
 * 
 * @author Martin Entlicher
 */
public class JavaUtils {
    
    private static final int ASYNC_WAIT_TIME = 500;
    
    private static final RequestProcessor scanningProcessor = new RequestProcessor("Debugger Context Scanning", 1);   // NOI18N

    public static Future<Void> runWhenScanFinishedReallyLazy(final JavaSource js,
                                                              final Task<CompilationController> task,
                                                              final boolean shared) throws IOException {
        return scanReallyLazy(new ScanRunnable<IOException>(IOException.class) {
            @Override
            public void run(Future<Void>[] resultPtr, IOException[] excPtr) {
                try {
                    js.runUserActionTask(task, shared);
                } catch (IOException ex) {
                    synchronized (resultPtr) {
                        excPtr[0] = ex;
                    }
                }
            }
        });
    }

    /*
    private static Future<Void> parseWhenScanFinishedReallyLazy(final Collection<Source> sources,
                                                                final UserTask userTask) throws ParseException {
        return scanReallyLazy(new ScanRunnable<ParseException> (ParseException.class) {
            @Override
            public void run(Future<Void>[] resultPtr, ParseException[] excPtr) {
                try {
                    ParserManager.parse(sources, userTask);
                } catch (ParseException ex) {
                    synchronized (resultPtr) {
                        excPtr[0] = ex;
                    }
                }
            }
        });
    }
    */

    private static <E extends Throwable> Future<Void> scanReallyLazy(ScanRunnable<E> run) throws E {
        final Future<Void>[] resultPtr = new Future[] { null };
        final E[] excPtr = (E[]) java.lang.reflect.Array.newInstance(run.exceptionType, 1);//new E[] { null };
        run.setParam(resultPtr, excPtr);
        final RequestProcessor.Task scanning = scanningProcessor.post(run);
        try {
            scanning.waitFinished(ASYNC_WAIT_TIME);
        } catch (InterruptedException ex) {
        }
        synchronized (resultPtr) {
            if (excPtr[0] != null) {
                throw excPtr[0];
            }
            if (resultPtr[0] != null) {
                return resultPtr[0];
            }
        }
        return new Future<Void>() {
            boolean cancelled = false;
            
            private Future<Void> getDelegate() {
                synchronized (resultPtr) {
                    return resultPtr[0];
                }
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return cancelled = scanning.cancel();
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return scanning.isFinished();
            }

            @Override
            public Void get() throws InterruptedException, ExecutionException {
                scanning.waitFinished();
                if (excPtr[0] != null) {
                    throw new ExecutionException(excPtr[0]);
                }
                return null;
            }

            @Override
            public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                long mstimeout = unit.toMillis(timeout);
                if (mstimeout == 0) {
                    if (!scanning.isFinished()) {
                        throw new TimeoutException("Task timeout");
                    }
                } else {
                    long s1 = System.nanoTime();
                    boolean finished = scanning.waitFinished(mstimeout);
                    if (!finished) {
                        throw new TimeoutException("Task timeout");
                    }
                    long s2 = System.nanoTime();
                    timeout -= unit.convert(s2 - s1, TimeUnit.NANOSECONDS);
                    if (timeout < 0) {
                        timeout = 1;
                    }
                }
                if (excPtr[0] != null) {
                    throw new ExecutionException(excPtr[0]);
                }
                return null;
            }
        };
    }

    private static abstract class ScanRunnable <E extends Throwable> implements Runnable {
        
        private Future<Void>[] resultPtr;
        private E[] excPtr;
        private Class<E> exceptionType;

        public ScanRunnable(Class<E> exceptionType) {
            this.exceptionType = exceptionType;
        }

        private void setParam(Future<Void>[] resultPtr, E[] excPtr) {
            this.resultPtr = resultPtr;
            this.excPtr = excPtr;
        }

        @Override
        public final void run() {
            run(resultPtr, excPtr);
        }

        public abstract void run(Future<Void>[] resultPtr, E[] excPtr);

    }


}
