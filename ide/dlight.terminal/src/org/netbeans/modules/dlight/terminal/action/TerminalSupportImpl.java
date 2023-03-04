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
package org.netbeans.modules.dlight.terminal.action;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.PathUtils;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.ui.IOVisibility;
import org.netbeans.modules.terminal.support.TerminalPinSupport;
import org.netbeans.modules.terminal.support.TerminalPinSupport.TerminalCreationDetails;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

import static org.netbeans.lib.terminalemulator.Term.ExternalCommandsConstants.*;
import org.netbeans.modules.terminal.api.ui.IOTerm;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class TerminalSupportImpl {

    private static final RequestProcessor RP = new RequestProcessor("Terminal Action RP", 100); // NOI18N

    private TerminalSupportImpl() {
    }

    public static Component getToolbarPresenter(Action action) {
        JButton button = new JButton(action);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setText(null);
        button.putClientProperty("hideActionText", Boolean.TRUE); // NOI18N
        Object icon = action.getValue(Action.SMALL_ICON);
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/dlight/terminal/action/local_term.png", false);// NOI18N
        }
        if (!(icon instanceof Icon)) {
            throw new IllegalStateException("No icon provided for " + action); // NOI18N
        }
        button.setDisabledIcon(ImageUtilities.createDisabledIcon((Icon) icon));
        return button;
    }
    
    
    /**
     * Creates new Terminal tab. Method must be called from UI Thread.
     * @param ioContainer parent tabbed container
     * @param io io will be reused if this value is not null, if null the new one will be created 
     * (<code>ioProvider.getIO(tabTitle, null, ioContainer)</code>)
     * @param tabTitle tab title
     * @param env execution environment
     * @param dir Terminal tries to cd into this dir when connected
     * @param silentMode produces output on errors if true
     * @param pwdFlag try to set title to 'user@host - ${PWD}' every time ${PWD} changes.
     */
    public static void openTerminalImpl(
            final IOContainer ioContainer,
            final String tabTitle,
            final ExecutionEnvironment env,
            final String dir,
            final boolean silentMode,
            final boolean pwdFlag,
            final long termId) {
        final IOProvider ioProvider = IOProvider.get("Terminal"); // NOI18N
        if (ioProvider != null) {
            final AtomicReference<InputOutput> ioRef = new AtomicReference<InputOutput>();
            // Create a tab in EDT right after we call the method, don't let this 
            // work to be done in RP in asynchronous manner. We need this to
            // save tab order 
            InputOutput io = ioProvider.getIO(tabTitle, null, ioContainer);
            ioRef.set(io);
            final AtomicBoolean destroyed = new AtomicBoolean(false);
            
            final Runnable runnable = new Runnable() {
                private final Runnable delegate = new Runnable() {
                    @Override
                    public void run() {
                        if (SwingUtilities.isEventDispatchThread()) {
                            ioContainer.requestActive();
                        } else {
                            doWork();
                        }
                    }
                };

                RequestProcessor.Task task = RP.create(delegate);

                private final HyperlinkAdapter retryLink = new HyperlinkAdapter() {
                    @Override
                    public void outputLineAction(OutputEvent ev) {
                        task.schedule(0);
                    }
                };

                @Override
                public void run() {
                    delegate.run();
                }

                private void doWork() {
                    boolean verbose = env.isRemote(); // can use silentMode instead
                    OutputWriter out = ioRef.get().getOut();

                    long id = TerminalPinSupport.getDefault().createPinDetails(TerminalCreationDetails.create(IOTerm.term(ioRef.get()), termId, env.getDisplayName(), pwdFlag));

                    if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                        try {
                            if (verbose) {
                                out.println(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_ConnectingTo", env.getDisplayName() ));
                            }
                            ConnectionManager.getInstance().connectTo(env);
                        } catch (IOException ex) {
                            if (!destroyed.get()) {
                                if (verbose) {
                                    try {
                                        out.print(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_ConnectionFailed"));
                                        out.println(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_Retry"), retryLink);
                                    } catch (IOException ignored) {
                                    }
                                }
                                String error = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
                                String msg = NbBundle.getMessage(TerminalSupportImpl.class, "TerminalAction.FailedToStart.text", error); // NOI18N
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                            }
                            return;
                        } catch (CancellationException ex) {
                            if (verbose) {
                                try {
                                    out.print(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_Canceled"));
                                    out.println(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_Retry"), retryLink);
                                } catch (IOException ignored) {
                                }
                            }
                            return;
                        }
                    }

                    final HostInfo hostInfo;
                    String expandedDir = null;
                    try {
                        // There is still a chance of env being disconnected
                        // (exception supressed in FetchHostInfoTask.compute)
                        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                            return;
                        }

                        try {
                            if (dir != null) {
                                try {
                                    expandedDir = PathUtils.expandPath(dir, env);
                                } catch (ParseException ex) {
                                    // If path, for some reason, was not expanded, we won't create Terminal
                                }
                                if (expandedDir == null || !HostInfoUtils.directoryExists(env, expandedDir)) {
                                    // Displaying this message always, not just for remote envs.
                                    // Logging dir instead of expandedDir, so user can understand the possible problem is macro.
                                    out.print(NbBundle.getMessage(TerminalSupportImpl.class, "LOG_DirNotExist", dir, env.getDisplayName()));
                                    return;
                                }
                            }
                        } catch (ConnectException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        hostInfo = HostInfoUtils.getHostInfo(env);
                        boolean isSupported = PtySupport.isSupportedFor(env);
                        if (!isSupported) {
                            if (!silentMode) {
                                String message;

                                if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                                    message = NbBundle.getMessage(TerminalSupportImpl.class, "LocalTerminalNotSupported.error.nocygwin"); // NOI18N
                                } else {
                                    message = NbBundle.getMessage(TerminalSupportImpl.class, "LocalTerminalNotSupported.error"); // NOI18N
                                }

                                NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
                                DialogDisplayer.getDefault().notify(nd);
                            }
                            return;
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        return;
                    } catch (CancellationException ex) {
                        Exceptions.printStackTrace(ex);
                        return;
                    }

                    if (verbose) {
                        try {
                            // Erase "log" in case we successfully connected to host
                            out.reset();
                        } catch (IOException ex) {
                            // never thrown from TermOutputWriter
                        }
                    }

                    try {
                        final Term term = IOTerm.term(ioRef.get());
                        // TODO: this is a temporary solution.

                        // Right now xterm emulation is not fully supported. (NB7.4)
                        // Still it has a very desired functionality - is recognises
                        // \ESC]%d;%sBEL escape sequences.
                        // Although \ESC]0;%sBEL is not implemented yet and window title
                        // is not set, it, at least, can skip the whole %s.
                        // This makes command prompt look better when this sequence is used
                        // in PS1 (ex. cygwin set this by default).
                        //
                        term.setEmulation("xterm"); // NOI18N

                        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                        // clear env modified by NB. Let it be initialized by started shell process
                        npb.getEnvironment().put("LD_LIBRARY_PATH", "");// NOI18N
                        npb.getEnvironment().put("DYLD_LIBRARY_PATH", "");// NOI18N

                        if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                            // /etc/profile changes directory to ${HOME} if this
                            // variable is not set.
                            npb.getEnvironment().put("CHERE_INVOKING", "1");// NOI18N
                        }

                        final TerminalPinSupport support = TerminalPinSupport.getDefault();
                        String envId = ExecutionEnvironmentFactory.toUniqueID(env);

                        npb.addNativeProcessListener(new NativeProcessListener(ioRef.get(), destroyed));

                        /*
                         * Was: echo -n \"\033]0;" + tabTitle + " `pwd`\007\""
                         * Why changed:
                         *  1. Flag "-n" is not always supported by different shells,
                         *     so use printf instead. (Actually, if "-n" is not supported,
                         *     almost always PROMPT_COMMAND won't be supported too. (ksh, for example).
                         *  2. Now we use "033]3" (op_cwd) instead of "033]0" (op_win_title) 
                         *     and let listeners decide what to do when cwd is changed.
                         *  3. Removed a useless `pwd` call because cd has already updated
                         *     $PWD and $OLDPWD variables.
                         */
                        if (pwdFlag) {
                            /**
                             * http://wiki.bash-hackers.org/scripting/posparams
                             *
                             *  $*      $1 $2 $3 ... ${N}
                             *  $@      $1 $2 $3 ... ${N}
                             *  "$*"    "$1c$2c$3c...c${N}" where 'c' is the first character of IFS
                             *  "$@"    "$1" "$2" "$3" ... "${N}"
                             */
                            final String promptCommand = "printf \"\033]3;${PWD}\007\"; " // NOI18N
                                    + IDE_OPEN + "() { printf \"\033]10;" + COMMAND_PREFIX + IDE_OPEN + " $*;\007\"; printf \"Opening $# file(s) ...\n\";}";   // NOI18N
                            final String commandName = "PROMPT_COMMAND";                                    // NOI18N
                            String usrPrompt = npb.getEnvironment().get(commandName);
                            npb.getEnvironment().put(commandName,
                                    (usrPrompt == null)
                                            ? promptCommand
                                            : promptCommand + ';' + usrPrompt
                            );
                            term.putClientProperty(EXECUTION_ENV_PROPERTY_KEY, ExecutionEnvironmentFactory.toUniqueID(env));
                        }

                        String shell = hostInfo.getLoginShell();
                        if (expandedDir != null) {
                            npb.setWorkingDirectory(expandedDir);
                        }
//                            npb.setWorkingDirectory("${HOME}");
                        npb.setExecutable(shell);
                        if (shell.endsWith("bash") || shell.endsWith("bash.exe")) { // NOI18N
                            npb.setArguments("--login"); // NOI18N
                        }
                        
                        NativeExecutionDescriptor descr;
                        descr = new NativeExecutionDescriptor().controllable(true).frontWindow(true).inputVisible(true).inputOutput(ioRef.get());
                        descr.postExecution(new Runnable() {

                            @Override
                            public void run() {
                                ioRef.get().closeInputOutput();
                                support.close(term);
                            }
                        });
                        NativeExecutionService es = NativeExecutionService.newService(npb, descr, "Terminal Emulator"); // NOI18N
                        Future<Integer> result = es.run();
                        // ask terminal to become active
                        SwingUtilities.invokeLater(this);

                        try {
                            // if terminal can not be started then ExecutionException should be thrown
                            // wait one second to see if terminal can not be started. otherwise it's OK to exit by TimeOut

                            // IG: I've increased the timeout from 1 to 10 seconds.
                            // On slow hosts 1 sec was not enougth to get an error code from the pty
                            // No work is done after this call, so this change should be safe.
                            Integer rc = result.get(10, TimeUnit.SECONDS);
                            if (rc != 0) {
                                Logger.getLogger(TerminalSupportImpl.class.getName())
                                        .log(Level.INFO, "{0}{1}", new Object[]{NbBundle.getMessage(TerminalSupportImpl.class, "LOG_ReturnCode"), rc});
                            }
                        } catch (TimeoutException ex) {
                            // we should be there
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (ExecutionException ex) {
                            if (!destroyed.get()) {
                                String error = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
                                String msg = NbBundle.getMessage(TerminalSupportImpl.class, "TerminalAction.FailedToStart.text", error); // NOI18N
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                            }
                        }
                    } catch (java.util.concurrent.CancellationException ex) { // VK: don't quite understand who can throw it?
                        Exceptions.printStackTrace(ex);
                        reportInIO(ioRef.get(), ex);
                    }
                }

                private void reportInIO(InputOutput io, Exception ex) {
                    if (io != null && ex != null) {
                        io.getErr().print(ex.getLocalizedMessage());
                    }
                }
            };
            RP.post(runnable);
        }
    }
    
    private static final class NativeProcessListener implements ChangeListener, PropertyChangeListener {

        private final AtomicReference<NativeProcess> processRef;
        private final AtomicBoolean destroyed;

        public NativeProcessListener(InputOutput io, AtomicBoolean destroyed) {
            assert destroyed != null;
            this.destroyed = destroyed;
            this.processRef = new AtomicReference<NativeProcess>();
            IONotifier.addPropertyChangeListener(io, WeakListeners.propertyChange(NativeProcessListener.this, io));
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            NativeProcess process = processRef.get();
            if (process == null && e.getSource() instanceof NativeProcess) {
                processRef.compareAndSet(null, (NativeProcess) e.getSource());
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (IOVisibility.PROP_VISIBILITY.equals(evt.getPropertyName()) && Boolean.FALSE.equals(evt.getNewValue())) {
                if (destroyed.compareAndSet(false, true)) {
                    // term is closing => destroy process
                    final NativeProcess proc = processRef.get();
                    if (proc != null) {
                        RP.submit(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    proc.destroy();
                                } catch (Throwable th) {
                                }
                            }
                        });
                    }
                }
            }
        }
    }
    
    private static class HyperlinkAdapter implements OutputListener{

        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
    }
}
