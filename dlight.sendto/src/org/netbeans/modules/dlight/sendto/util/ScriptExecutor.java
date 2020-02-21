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
package org.netbeans.modules.dlight.sendto.util;

import org.netbeans.modules.dlight.sendto.api.OutputMode;
import org.netbeans.modules.dlight.sendto.config.ConfigureAction;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.IOColorLines;
import org.openide.windows.InputOutput;

/**
 *
 */
public final class ScriptExecutor {

    private final ExecutionEnvironment env;
    private final ArrayList<String> argslist;
    private String stringToWrite;

    public ScriptExecutor(List<String> cmd) {
        this(null, cmd);
    }

    public ScriptExecutor(ExecutionEnvironment env, List<String> cmd) {
        argslist = new ArrayList<String>(cmd);
        this.env = env;
    }

    public ScriptExecutor writeToProcessOnRun(String string) {
        stringToWrite = string;
        return this;
    }

    private void outHeader(final InputOutput io, final String tabName) {
        Format formatter;
        Date date = new Date();

        // Time formate 01:12:53 AM
        formatter = new SimpleDateFormat("hh:mm:ss"); // NOI18N

        String msg = formatter.format(date) + " - " + tabName + "\r"; // NOI18N

        try {
            if (IOColorLines.isSupported(io)) {
                IOColorLines.println(io, msg, null, true, Color.BLUE);
            } else {
                io.getOut().println(msg, null, true);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void execute(String name, OutputMode outputMode) {
        execute(name, outputMode, null);
    }

    public void execute(final String name, final OutputMode outputMode, final ExecutionDescriptor.LineConvertorFactory lineConvertorFactory) {
        if (argslist.isEmpty()) {
            return;
        }

        final AtomicReference<Process> procRef = new AtomicReference<Process>();
        final AtomicReference<Throwable> xRef = new AtomicReference<Throwable>();

        final String tabName = NbBundle.getMessage(ScriptExecutor.class, "OutputWindow.SendTo.name", name); // NOI18N
        final StopAction stopAction = new StopAction(tabName, procRef);

        final InputOutput io = CachedIOProvider.getIO(
                tabName,
                new Action[]{stopAction, ConfigureAction.getInstance()},
                procRef,
                outputMode);

        final ProgressHandle progressHandle = ProgressHandleFactory.createHandle(tabName, new Cancellable() {
            @Override
            public boolean cancel() {
                stopAction.actionPerformed(null);
                return true;
            }
        }, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                io.select();
            }
        });

        try {
            progressHandle.start();
            PostExecution postExecution = new PostExecution(io, tabName, procRef, xRef);

            if (OutputMode.INTERNAL_TERMINAL == outputMode || (env != null && !env.isLocal())) {
                final String exec = argslist.get(0);
                final String[] args = Arrays.copyOfRange(argslist.toArray(new String[0]), 1, argslist.size());
                NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                npb.setExecutable(exec).setArguments(args);

                npb.addNativeProcessListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        Object src = e == null ? null : e.getSource();
                        if (src instanceof Process) {
                            if (procRef.compareAndSet(null, (Process) src)) {
                                stopAction.setEnabled(true);
                            }
                        }
                    }
                });

                NativeExecutionDescriptor descr = new NativeExecutionDescriptor();

                descr = descr.frontWindow(true).
                        controllable(true).
                        noReset(true).
                        inputOutput(io).
                        inputVisible(true).
                        postExecution(postExecution);

                final NativeExecutionService es = NativeExecutionService.newService(
                        npb,
                        descr,
                        tabName);

                outHeader(io, tabName);

                es.run().get();
            } else {
                ProcessBuilder pb = new ProcessBuilder(argslist);

                Runnable preExecution = new Runnable() {
                    @Override
                    public void run() {
                        outHeader(io, tabName);
                    }
                };

                ExecutionDescriptor descr = new ExecutionDescriptor();

                descr = descr.frontWindow(true).
                        controllable(true).
                        noReset(true).
                        inputOutput(io).
                        inputVisible(stringToWrite == null).
                        preExecution(preExecution).
                        postExecution(postExecution).
                        outConvertorFactory(lineConvertorFactory).
                        errConvertorFactory(lineConvertorFactory);

                final ExecutionService es = ExecutionService.newService(
                        new ProcessCreator(pb, procRef, xRef, stringToWrite),
                        descr,
                        name);

                es.run().get();
            }
        } catch (Throwable th) {
            xRef.set(th);
        } finally {
            stopAction.setEnabled(false);
            progressHandle.finish();
        }
    }

    private static class ProcessCreator implements Callable<Process> {

        private final ProcessBuilder pb;
        private final AtomicReference<Process> pref;
        private final AtomicReference<Throwable> xref;
        private final String selection;

        public ProcessCreator(ProcessBuilder pb, AtomicReference<Process> pref, AtomicReference<Throwable> xref, String selection) {
            this.pb = pb;
            this.pref = pref;
            this.xref = xref;
            this.selection = selection;
        }

        @Override
        public Process call() throws Exception {
            Process p = null;
            try {
                p = pb.start();

                if (selection != null) {
                    p.getOutputStream().write(selection.getBytes(Charset.forName("UTF-8"))); // NOI18N
                    p.getOutputStream().close();
                }
            } catch (Exception ex) {
                xref.set(ex);
                throw ex;
            }

            pref.set(p);
            return p;
        }
    }

    private static class StopAction extends AbstractAction {

        private final AtomicReference<Process> procRef;

        private StopAction(String tabName, AtomicReference<Process> procRef) {
            super(NbBundle.getMessage(ScriptExecutor.class, "Execution.StopAction.name", tabName), // NOI18N
                    ImageUtilities.loadImageIcon("org/netbeans/modules/dlight/sendto/resources/stop.png", false)); // NOI18N
            this.procRef = procRef;

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final Process proc = procRef.get();
            if (proc != null) {
                proc.destroy();
            }
        }
    }

    private static class PostExecution implements Runnable {

        private final InputOutput io;
        private final AtomicReference<Process> procRef;
        private final AtomicReference<Throwable> xRef;
        private final String scriptName;

        private PostExecution(InputOutput io, String scriptName, AtomicReference<Process> procRef, AtomicReference<Throwable> xRef) {
            this.io = io;
            this.scriptName = scriptName;
            this.procRef = procRef;
            this.xRef = xRef;
        }

        @Override
        public void run() {
            try {
                Process p = procRef.get();
                io.getOut().println('\r');

                if (p != null) {
                    IOColorLines.println(io, NbBundle.getMessage(ScriptExecutor.class, "Execution.ExitStatus.text", p.waitFor()), Color.BLUE); // NOI18N
                } else {
                    if (xRef.get() != null) {
                        IOColorLines.println(io, xRef.get().getLocalizedMessage(), Color.RED);
                    }

                    IOColorLines.println(io, NbBundle.getMessage(ScriptExecutor.class, "Execution.StartFailed.text", scriptName), Color.RED); // NOI18N
                }

                io.getOut().println('\r');
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                io.getOut().close();
            }
        }
    }
}
