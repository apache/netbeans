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
package org.netbeans.modules.nativeexecution.api.execution;

import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.terminal.api.IOTerm;
import org.openide.util.Cancellable;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.WeakListeners;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOSelect;
import org.openide.windows.IOSelect.AdditionalOperation;
import org.openide.windows.OutputWriter;

/**
 * This is a wrapper over an <tt>Executionservice</tt> that handles running
 * NativeProcesses in a terminal output window.
 *
 * It also can be used for running in an output windows - in this case it just
 * delegates execution to the <tt>ExecutionService</tt>
 *
 * @see ExecutionService
 * @see NativeExecutionDescriptor
 *
 * @author ak119685
 */
public final class NativeExecutionService {

    private final NativeProcessBuilder processBuilder;
    private final String displayName;
    private final NativeExecutionDescriptor descriptor;
    private static final Charset execCharset;
    private Runnable postExecutable;
    private final AtomicReference<NativeProcess> processRef = new AtomicReference<>();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private ProcessChangeListener listener;
    private long startTimeMillis;

    static {
        String charsetName = System.getProperty("org.netbeans.modules.nativeexecution.execcharset", "UTF-8"); // NOI18N
        Charset cs = null;
        try {
            cs = Charset.forName(charsetName);
        } catch (Exception ex) {
            cs = Charset.defaultCharset();
        } finally {
            execCharset = cs;
        }
    }

    private NativeExecutionService(NativeProcessBuilder processBuilder, String displayName, NativeExecutionDescriptor descriptor) {
        this.processBuilder = processBuilder;
        this.displayName = displayName;
        this.descriptor = descriptor;
    }

    public static NativeExecutionService newService(NativeProcessBuilder processBuilder,
            NativeExecutionDescriptor descriptor, String displayName) {
        return new NativeExecutionService(processBuilder, displayName, descriptor);
    }

    public Future<Integer> run() {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("NativeExecutionService state error - cannot be called more than once!"); // NOI18N
        }

        // FIXME: processBuilder has no removeProcessListener method...
        // So have to add a weak listener...
        listener = new ProcessChangeListener();
        processBuilder.addNativeProcessListener(WeakListeners.create(ChangeListener.class, listener, processBuilder));

        postExecutable = descriptor.postExecution;
        descriptor.postExecution(new PostRunnable());

        if (IOTerm.isSupported(descriptor.inputOutput)) {
            return runTerm();
        } else {
            return runRegular();
        }
    }

    private Future<Integer> runTerm() {
        final AtomicReference<FutureTask<Integer>> runTaskRef = new AtomicReference<>(null);

        processBuilder.setUsePty(true);

        if (IOEmulation.isSupported(descriptor.inputOutput)) {
            // TODO: this is a temporary solution.
            //
            // Currently only ansi emulation is supported (NB7.4).
            // xterm/dtterm cannot be considered as fully supported yet..
            // So will set TERM environment to 'ansi'
            String termType = processBuilder.getEnvironment().get("TERM"); //NOI18N
            if (termType != null && termType.startsWith("xterm")) { //NOI18N
                // $TERM is one of xterm-color, xterm-16color, xterm-88color, or xterm-256color.
                // Allows user to use his favorite one.
                processBuilder.getEnvironment().put("TERM", termType); // NOI18N
            } else {
                processBuilder.getEnvironment().put("TERM", "xterm" /*IOEmulation.getEmulation(descriptor.inputOutput)*/); // NOI18N
            }
        } else {
            processBuilder.getEnvironment().put("TERM", "dumb"); // NOI18N
        }

        processBuilder.setCharset(descriptor.charset);

        Callable<Integer> callable = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                ProgressHandle progressHandle = null;

                try {
                    if (descriptor.showProgress) {
                        Cancellable c = null;
                        if (descriptor.controllable) {
                            c = new Cancellable() {

                                @Override
                                public boolean cancel() {
                                    FutureTask<Integer> task = runTaskRef.get();
                                    if (task != null) {
                                        return task.cancel(true);
                                    }
                                    return false;
                                }
                            };
                        }

                        progressHandle = ProgressHandle.createHandle(displayName, c);//, new AbstractAction() {

//                            @Override
//                            public void actionPerformed(ActionEvent e) {
//                                if (descriptor.inputOutput != null) {
//                                    descriptor.inputOutput.select();
//                                }
//                            }
//                        });
                        progressHandle.start();
                    }

                    final NativeProcess process = processBuilder.call();

                    /**
                     * As IO could be re-used we need to 'unlock' it because it
                     * could be 'locked' by previous run... (It is always set to
                     * read-only mode on run finish)
                     */
                    IOTerm.setReadOnly(descriptor.inputOutput, !descriptor.inputVisible);
                    //IOTerm.term(descriptor.inputOutput).setReadOnly(!descriptor.inputVisible);

                    PtySupport.connect(descriptor.inputOutput, process, new PreExecution());

                    /**
                     * We call invokeAndWait here to be sure that connect is
                     * done. This is because connection is asynchronous
                     * operation that occurs in EDT. return from connect()
                     * guarantees that event was posted to EDT. So our event is
                     * queued after that one. So as soon as our is processed we
                     * are sure that connection is done.
                     */
                    /**
                     * comment above is true only in case of NetBeans implementation
                     * As only in NB implementation the task of connecting to the IOTerm 
                     * is invoked asynchronous in EDT.
                     * In other case it can be done differently,
                     * that's why we will use IOTerm itself to provide service for 
                     * running pre-execution task
                     */
                  //  SwingUtilities.invokeAndWait(new PreExecution());

                    if (process.getState() == State.ERROR) {
                        out(true, ProcessUtils.readProcessErrorLine(process), "\r"); // NOI18N
                        return 1;
                    }

                    return process.waitFor();
                } finally {
                    if (progressHandle != null) {
                        progressHandle.finish();
                    }

                    final CountDownLatch latch = new CountDownLatch(1);
                    IOTerm.disconnect(descriptor.inputOutput, new Runnable() {

                        @Override
                        public void run() {
                            latch.countDown();
                        }
                    });
                    try {
                        // The problem here is that continuation passed to the
                        // disconnect() method could be ignored (if, for some
                        // reasons terminal is already disconnected.
                        // In this case we may deadlock on wait.
                        // It is not possible to detect if continuation was not
                        // called because of some error or because terminal is
                        // still waiting for IO drain..
                        // To avoid deadlocks assume that 5 seconds is enough
                        // for drain... and assume that if runnable was not
                        // called in 5 seconds then it makes no sense to wait
                        // any more.
                        // The worst situation that this approach could lead to
                        // is that postExecution is called before all IO was
                        // drained....
                        latch.await(5, TimeUnit.SECONDS);
                    } finally {
                        try {
                            if (descriptor.postExecution != null) {
                                descriptor.postExecution.run();
                            }
                        } finally {
                            IOTerm.setReadOnly(descriptor.inputOutput, true);
                            //IOTerm.term(descriptor.inputOutput).setReadOnly(true);
                        }
                    }
                }
            }
        };

        FutureTask<Integer> runTask = new FutureTask<Integer>(callable) {

            @Override
            /**
             * @return <tt>true</tt>
             */
            public boolean cancel(boolean mayInterruptIfRunning) {
                synchronized (processRef) {
                    /*
                     * *** Bug 186172 ***
                     *
                     * Do NOT call super.cancel() here as it will interrupt
                     * waiting thread (see callable's that this task is created
                     * from) and will initiate a postExecutionTask BEFORE the
                     * process is terminated. Just need to terminate the
                     * process. The process.waitFor() will naturally return
                     * then.
                     *
                     */
//                    boolean ret = super.cancel(mayInterruptIfRunning);

                    NativeProcess process = processRef.get();

                    if (process != null) {
                        process.destroy();
                    }

                    return true;
                }
            }
        };

        runTaskRef.set(runTask);
        NativeTaskExecutorService.submit(runTask, "start process in term"); // NOI18N

        return runTask;
    }

    private Future<Integer> runRegular() {
        Charset charset = descriptor.charset;

        if (charset == null) {
            charset = execCharset;
        }

        Logger.getInstance().log(Level.FINE, "Input stream charset: {0}", charset);

        ExecutionDescriptor descr =
                new ExecutionDescriptor().controllable(descriptor.controllable).
                frontWindow(descriptor.frontWindow).
                preExecution(new PreExecution()).
                inputVisible(descriptor.inputVisible).
                inputOutput(descriptor.inputOutput).
                outLineBased(descriptor.outLineBased).
                showProgress(descriptor.showProgress).
                postExecution(descriptor.postExecution).
                noReset(!descriptor.resetInputOutputOnFinish).
                errConvertorFactory(descriptor.errConvertorFactory).
                outConvertorFactory(descriptor.outConvertorFactory).
                charset(charset);

        return ExecutionService.newService(processBuilder, descr, displayName).run();
    }

    // copy-paste from CndUtils
    private static boolean isUnitTestMode() {
        return Boolean.getBoolean("cnd.mode.unittest"); // NOI18N
    }

    private void out(final boolean toError, final CharSequence... cs) {
        final Action<Void> action = new Action<Void>() {
            
            @Override
            public Void run() {
                if (descriptor.inputOutput != null) {
                    OutputWriter w = toError
                            ? descriptor.inputOutput.getErr()
                            : descriptor.inputOutput.getOut();
                    if (w != null) {
                        for (CharSequence c : cs) {
                            w.append(c);
                        }
                    }
                }
                return null;
            }
        };
        if (isUnitTestMode()) {
            action.run();
        } else {
            Mutex.EVENT.writeAccess(action);
        }
    }

    private void closeIO() {
        Mutex.EVENT.writeAccess(new Action<Void>() {

            @Override
            public Void run() {
                final OutputWriter out = (descriptor.inputOutput == null) ? null : descriptor.inputOutput.getOut();
                final OutputWriter err = (descriptor.inputOutput == null) ? null : descriptor.inputOutput.getErr();
                if (err != null) {
                    try {
                        err.close();
                    } catch (Throwable th) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (Throwable th) {
                    }
                }
                return null;
            }
        });
    }

    private final class PostRunnable implements Runnable {

        @Override
        public void run() {
            NativeProcess process = processRef.get();

            if (process == null) {
                return;
            }

            int rc = -1;

            try {
                rc = process.waitFor();
            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
            } finally {
                 org.netbeans.modules.nativeexecution.support.Logger.getInstance().fine("NativeExecutionService FINISHING");
                final long time = System.currentTimeMillis() - startTimeMillis;
                if (IOColorLines.isSupported(descriptor.inputOutput)
                        && descriptor.postMessageDisplayer instanceof PostMessageDisplayer2) {
                    PostMessageDisplayer2 pmd = (PostMessageDisplayer2) descriptor.postMessageDisplayer;
                    pmd.outPostMessage(descriptor.inputOutput, process, time);
                    NativeExecutionUserNotification.getDefault().notifyStatus(pmd.getPostStatusString(process));
                    org.netbeans.modules.nativeexecution.support.Logger.getInstance().fine("NativeExecutionService FINISHING have sent colored notification");
                    //StatusDisplayer.getDefault().setStatusText(pmd.getPostStatusString(process));
                } else {
                    if (descriptor.postMessageDisplayer != null) {
                        String postMsg = descriptor.postMessageDisplayer.getPostMessage(process, time);
                        out(rc != 0, "\n\r", postMsg, "\n\r"); // NOI18N
                        if (!isUnitTestMode()) {
                            NativeExecutionUserNotification.getDefault().notifyStatus(descriptor.postMessageDisplayer.getPostStatusString(process));
                        }
                        org.netbeans.modules.nativeexecution.support.Logger.getInstance().log(Level.FINE, "NativeExecutionService FINISHING have sent notification {0}", postMsg);
                        //StatusDisplayer.getDefault().setStatusText(descriptor.postMessageDisplayer.getPostStatusString(process));
                    }
                }

                try {
                    // Finally, if there was some post executable set before - call it
                    if (postExecutable != null) {
                        org.netbeans.modules.nativeexecution.support.Logger.getInstance().fine("NativeExecutionService FINISHING start post executable");
                        postExecutable.run();
                        org.netbeans.modules.nativeexecution.support.Logger.getInstance().fine("NativeExecutionService FINISHING have been executed post executable");
                    }
                } finally {
                    if (descriptor.closeInputOutputOnFinish) {
                        closeIO();
                    }
                    org.netbeans.modules.nativeexecution.support.Logger.getInstance().fine("NativeExecutionService FINISHED");
                }
            }
        }
    }

    private final class ProcessChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (!(e instanceof NativeProcessChangeEvent)) {
                return;
            }

            final NativeProcessChangeEvent event = (NativeProcessChangeEvent) e;
            processRef.compareAndSet(null, (NativeProcess) event.getSource());

            switch (event.state) {
                case RUNNING:
                case ERROR:
                    startTimeMillis = System.currentTimeMillis();
                    break;
            }
        }
    }

    private class PreExecution implements Runnable {

        @Override
        public void run() {
            if (descriptor.frontWindow) {
                if (IOSelect.isSupported(descriptor.inputOutput)) {
                    IOSelect.select(descriptor.inputOutput,
                            EnumSet.<AdditionalOperation>of(
                            AdditionalOperation.OPEN,
                            AdditionalOperation.REQUEST_VISIBLE));
                } else {
                    if (descriptor.inputOutput != null) { // avoid random NPE in tests
                        descriptor.inputOutput.select();
                    }
                }
            }
            if (descriptor.requestFocus) {
                IOTerm.requestFocus(descriptor.inputOutput);
//                Term term = IOTerm.term(descriptor.inputOutput);
//
//                if (term != null) {
//                    JComponent screen = term.getScreen();
//                    if (screen != null) {
//                        screen.requestFocusInWindow();
//                    }
//                } else {
//                    if (descriptor.inputOutput != null) { // avoid random NPE in tests
//                        descriptor.inputOutput.setFocusTaken(true);
//                    }
//                }
            }
        }
    }
}
