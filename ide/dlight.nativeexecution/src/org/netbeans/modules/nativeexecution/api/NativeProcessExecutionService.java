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
package org.netbeans.modules.nativeexecution.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;

/**
 * This is a very light-weigth version of ExecutionService from org.netbeans.api.extexecution
 * @author ak119685
 */
public final class NativeProcessExecutionService {

    private final ExecutionTask task;
    private final String descr;

    private NativeProcessExecutionService(final ExecutionTask task, String descr) {
        this.task = task;
        this.descr = descr;
    }

    public static NativeProcessExecutionService newService(NativeProcessBuilder npb, LineProcessor outProcessor, LineProcessor errProcessor, String descr) {
        ExecutionTask task = new ExecutionTask(npb, outProcessor, errProcessor, descr);
        return new NativeProcessExecutionService(task, descr);
    }

    public Future<Integer> start() {
        return NativeTaskExecutorService.submit(task, descr);
    }

    public ProcessInfo getProcessInfo() {
        synchronized (task) {
            if (task.process == null) {
                throw new IllegalThreadStateException("Not started yet"); // NOI18N
            }

            return task.process.getProcessInfo();
        }
    }

    private static class ExecutionTask implements Callable<Integer> {

        private static final Logger log = org.netbeans.modules.nativeexecution.support.Logger.getInstance();
        private final NativeProcessBuilder npb;
        private final LineProcessor outProcessor;
        private final LineProcessor errProcessor;
        private final String descr;
        private NativeProcess process = null;

        public ExecutionTask(NativeProcessBuilder npb, LineProcessor outProcessor, LineProcessor errProcessor, String descr) {
            this.npb = npb;
            this.outProcessor = outProcessor;
            this.errProcessor = errProcessor;
            this.descr = descr;
        }

        public synchronized Integer call() throws Exception {
            if (process != null) {
                throw new IllegalThreadStateException("Already started!"); // NOI18N
            }

            int result = -1;

            InputStream is = null;
            BufferedReader br = null;
            Future<Boolean> errorProcessingDone = null;

            try {
                if (outProcessor != null) {
                    outProcessor.reset();
                }

                if (errProcessor != null) {
                    errProcessor.reset();
                }

                process = npb.call();

                errorProcessingDone = NativeTaskExecutorService.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        InputStream is = process.getErrorStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (errProcessor != null) {
                                errProcessor.processLine(line);
                            } else {
                                ProcessUtils.logError(Level.FINE, log, process);
                            }
                        }
                        return true;
                    }
                }, "reading process err"); //NOI18N

                is = process.getInputStream();

                if (is != null) {
                    // LATER: shouldn't it use ProcessUtils.getReader?
                    br = new BufferedReader(new InputStreamReader(is));
                    String line;

                    try {
                        while ((line = br.readLine()) != null) {
                            if (outProcessor != null) {
                                outProcessor.processLine(line);
                            }
                        }
                    } catch (InterruptedIOException ex) {
                        // Clean interrupted status
                        Thread.interrupted();
                    }
                }
                errorProcessingDone.get(); // just to wait until error processing is done
            } catch (Throwable th) {
                log.log(Level.FINE, descr, th.getMessage());
            } finally {
                try {
                    if (outProcessor != null) {
                        outProcessor.close();
                    }
                } catch (Throwable th) {
                    log.log(Level.FINE, descr, th.getMessage());
                }

                if (br != null) {
                    br.close();
                }

                if (process != null) {
                    try {
                        result = process.exitValue();
                    } catch (Throwable th) {
                        // Not exited yet...
                    }

                    process.destroy();
                }
            }

            return result;
        }
    }
}
