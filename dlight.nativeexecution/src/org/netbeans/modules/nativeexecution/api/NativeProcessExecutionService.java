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

        private final static Logger log = org.netbeans.modules.nativeexecution.support.Logger.getInstance();
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
