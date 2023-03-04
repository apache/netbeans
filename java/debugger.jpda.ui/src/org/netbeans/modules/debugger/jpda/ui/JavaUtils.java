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

    private abstract static class ScanRunnable <E extends Throwable> implements Runnable {
        
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
