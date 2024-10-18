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
package org.netbeans.modules.nativeexecution.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.nativeexecution.AbstractNativeProcess;
import org.netbeans.modules.nativeexecution.LocalNativeProcess;
import org.netbeans.modules.nativeexecution.NativeProcessInfo;
import org.netbeans.modules.nativeexecution.NbLocalNativeProcess;
import org.netbeans.modules.nativeexecution.NbRemoteNativeProcess;
import org.netbeans.modules.nativeexecution.PtyNativeProcess;
import org.netbeans.modules.nativeexecution.RemoteNativeProcess;
import org.netbeans.modules.nativeexecution.TerminalLocalNativeProcess;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport;
import org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport.ShellValidationStatus;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.pty.NbStartUtility;
import org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.util.Utilities;

/**
 * Utility class for the {@link NativeProcess external native process} creation.
 * <p>
 * Depending on {@link ExecutionEnvironment} it creates either local process or
 * remote one. This class was designed to be usable with {@link ExecutionService}
 * provided by the
 * <a href="http://bits.netbeans.org/dev/javadoc/org-netbeans-modules-extexecution/index.html?overview-summary.html" target="_blank">External Execution Support</a>
 * NetBeans module.
 * <p>
 * Builder handles command, working directory, environment, task's listeners and
 * execution in an external terminal.
 * <p>
 */
// @NotThreadSafe
public final class NativeProcessBuilder implements Callable<Process> {

    private final NativeProcessInfo info;
    private ExternalTerminal externalTerminal = null;

    private NativeProcessBuilder(final ExecutionEnvironment execEnv) {
        info = new NativeProcessInfo(execEnv);
    }

    /**
     * Creates a new instance of the builder that will create a {@link NativeProcess}
     * in the specified execution environment.
     * @param execEnv execution environment that defines <b>where</b> a native
     *        process will be started.
     * @return new instance of process builder
     */
    public static NativeProcessBuilder newProcessBuilder(ExecutionEnvironment execEnv) {
        return new NativeProcessBuilder(execEnv);
    }

    /**
     * Creates a new instance of the builder that will create a {@link NativeProcess}
     * on the localhost.
     * @return new instance of process builder
     */
    public static NativeProcessBuilder newLocalProcessBuilder() {
        return new NativeProcessBuilder(ExecutionEnvironmentFactory.getLocal());
    }

    public NativeProcessBuilder redirectError() {
        info.redirectError(true);
        return this;
    }

    public boolean redirectErrorStream() {
        return info.isRedirectError();
    }

    /**
     * Specif
     * @param executable
     * @return
     */
    public NativeProcessBuilder setExecutable(String executable) {
        info.setExecutable(executable);
        return this;
    }

    /**
     * NB! no arguments can be set after that.
     * command line it not escaped before execution.
     * @param commandLine
     * @return
     */
    @SuppressWarnings("deprecation")
    public NativeProcessBuilder setCommandLine(String commandLine) {
        info.setCommandLine(commandLine);
        return this;
    }

    /**
     * Register passed <tt>NativeProcess.Listener</tt>.
     *
     * @param listener NativeProcess.Listener to be registered to receive
     * process's state change events.
     *
     * @return this
     */
    public NativeProcessBuilder addNativeProcessListener(ChangeListener listener) {
        info.addChangeListener(listener);
        return this;
    }

    public NativeProcessBuilder removeNativeProcessListener(ChangeListener listener) {
        info.removeChangeListener(listener);
        return this;
    }

    public MacroMap getEnvironment() {
        return info.getEnvironment();
    }

    /**
     * Creates a new {@link NativeProcess} based on the properties configured
     * in this builder.
     * @return new {@link NativeProcess} based on the properties configured
     *             in this builder
     * @throws IOException if the process could not be created
     * @throws UserQuestionException in case the system is not yet connected
     */
    @Messages({
        "# {0} - display name of execution environment",
        "EXC_NotConnectedQuestion=No connection to {0}. Connect now?"
    })
    @Override
    public NativeProcess call() throws IOException {
        AbstractNativeProcess process = null;

        final ExecutionEnvironment execEnv = info.getExecutionEnvironment();

        if (info.getCommand() == null) {
            throw new IllegalStateException("No executable nor command line is specified"); // NOI18N
        }

        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            throw new UserQuestionException("No connection to " + execEnv.getDisplayName()) {// NOI18N
                @Override
                public void confirmed() throws IOException {
                    RequestProcessor.getDefault().post(new Runnable() {
                        @Override
                        public void run() {
                            ConnectionManager.getInstance().connect(execEnv);
                        }
                    });
                }

                @Override
                public String getLocalizedMessage() {
                    return Bundle.EXC_NotConnectedQuestion(execEnv.getDisplayName());
                }
            };
        }

        if (externalTerminal == null && NbStartUtility.getInstance(info.getExecutionEnvironment().isLocal()).isSupported(info.getExecutionEnvironment())) {
            if (info.getExecutionEnvironment().isLocal()) {
                process = new NbLocalNativeProcess(info);
            } else {
                process = new NbRemoteNativeProcess(info);
            }
        } else {
            if (info.isPtyMode() && PtySupport.isSupportedFor(info.getExecutionEnvironment())) {
                process = new PtyNativeProcess(info);
            } else {
                if (info.getExecutionEnvironment().isRemote()) {
                    process = new RemoteNativeProcess(info);
                } else {
                    if (externalTerminal != null) {
                        boolean canProceed = true;
                        boolean available = externalTerminal.isAvailable(info.getExecutionEnvironment());

                        if (!available) {
                            if (Boolean.getBoolean("nativeexecution.mode.unittest") || "true".equals(System.getProperty("cnd.command.line.utility"))) { // NOI18N
                                System.err.println(loc("NativeProcessBuilder.processCreation.NoTermianl.text"));
                            } else {
                                        NativeExecutionUserNotification.getDefault().notify(loc("NativeProcessBuilder.processCreation.NoTermianl.text"), // NOI18N
                                                NativeExecutionUserNotification.Descriptor.WARNING);                                
//                                DialogDisplayer.getDefault().notify(
//                                        new NotifyDescriptor.Message(loc("NativeProcessBuilder.processCreation.NoTermianl.text"), // NOI18N
//                                        NotifyDescriptor.WARNING_MESSAGE));
                            }
                            canProceed = false;
                        } else {
                            if (Utilities.isWindows()) {
                                Shell shell = WindowsSupport.getInstance().getActiveShell();
                                if (shell == null) {
                                    if (Boolean.getBoolean("nativeexecution.mode.unittest") || "true".equals(System.getProperty("cnd.command.line.utility"))) { // NOI18N
                                        System.err.println(loc("NativeProcessBuilder.processCreation.NoShell.text"));
                                    } else {
                                        NativeExecutionUserNotification.getDefault().notify(loc("NativeProcessBuilder.processCreation.NoShell.text"), // NOI18N
                                                NativeExecutionUserNotification.Descriptor.WARNING);
//                                        DialogDisplayer.getDefault().notify(
//                                                new NotifyDescriptor.Message(loc("NativeProcessBuilder.processCreation.NoShell.text"), // NOI18N
//                                                NotifyDescriptor.WARNING_MESSAGE));
                                    }
                                    canProceed = false;
                                } else {
                                    ShellValidationStatus validationStatus = ShellValidationSupport.getValidationStatus(shell);

                                    if (!validationStatus.isValid()) {
                                        canProceed = ShellValidationSupport.confirm(
                                                loc("NativeProcessBuilder.processCreation.BrokenShellConfirmationHeader.text"), // NOI18N
                                                loc("NativeProcessBuilder.processCreation.BrokenShellConfirmationFooter.text"), // NOI18N
                                                validationStatus);
                                    }
                                }
                            }

                            if (canProceed) {
                                process = new TerminalLocalNativeProcess(info, externalTerminal);
                            }
                        }
                    }
                }

                if (process == null) {
                    // Either externalTerminal is null or there are some problems with it
                    process = new LocalNativeProcess(info);
                }
            }
        }

        return process.createAndStart();
    }

    /**
     * Configures a working directory.
     * Process subsequently created by the call() method on this builder
     * will be executed with this directory as a current working dir.
     * <p>
     * The default value is undefined.
     * <p>
     * @param workingDirectory working directory to start process in.
     * @return this
     */
    public NativeProcessBuilder setWorkingDirectory(String workingDirectory) {
        info.setWorkingDirectory(workingDirectory);
        return this;
    }

    /**
     * Configure arguments of the command.
     *
     * <p>
     * By default executable is started without any arguments.
     * <p>
     * Previously configured arguments are cleared. 
     * <p>
     * If there is a need to parse arguments already provided as one big string
     * the method that can help is
     * {@link org.openide.util.Utilitiesies#parseParameters(java.lang.String)}.
     *
     * @param arguments command arguments
     * @return this
     */
    public NativeProcessBuilder setArguments(String... arguments) {
        info.setArguments(arguments);
        return this;
    }

    /**
     * Configure external terminal to be used to execute configured process.
     * 
     * <p>
     * @param terminal terminal specification
     * @return this
     *
     * @see ExternalTerminalProvider
     */
    public NativeProcessBuilder useExternalTerminal(/*@NullAllowed*/ExternalTerminal terminal) {
        externalTerminal = terminal;
        return this;
    }

    /**
     * Configure whether to use output unbuffering or not.
     * @param unbuffer - if true, native unbuffer library will be preloaded.
     * @return this
     */
    public NativeProcessBuilder unbufferOutput(boolean unbuffer) {
        info.setUnbuffer(unbuffer);
        return this;
    }

    /**
     * Configure X11 forwarding.
     *
     * @param x11forwarding  pass <code>true</code> to enable forwarding,
     *      or <code>false</code> to disable
     * @return this
     */
    public NativeProcessBuilder setX11Forwarding(boolean x11forwarding) {
        if (Boolean.getBoolean("cnd.remote.noX11")) {
            return this; //
        }
        info.setX11Forwarding(x11forwarding);
        return this;
    }

    /**
     * Configure whether process starts normally or suspended.
     * Suspended process can be resumed by sending it SIGCONT signal.
     * Note that suspended process is also in RUNNING state.
     *
     * @param suspend  pass <code>true</code> to start process suspended,
     *      or <code>false</code> to start process normally
     * @return this
     */
    public NativeProcessBuilder setInitialSuspend(boolean suspend) {
        info.setInitialSuspend(suspend);
        return this;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(NativeProcessBuilder.class, key, params);
    }

    /**
     * Configure whether process starts in a prseudo-terminal or not.
     * 
     * @param usePty - if true, process builder will start the process in
     * a pty mode
     * @return this
     */
    public NativeProcessBuilder setUsePty(boolean usePty) {
        info.setPtyMode(usePty);
        return this;
    }

    /**
     * Process builder try to expand, escape, quote command line according to subset of shell man.
     * By default builder do this. This method allows to forbid  preprocessing of command line.
     *
     * @param expandMacros - if false, process builder do not preprocess command line
     * @return this
     */
    public NativeProcessBuilder setMacroExpansion(boolean expandMacros) {
        info.setExpandMacros(expandMacros);
        return this;
    }

    public NativeProcessBuilder setCharset(Charset charset) {
        info.setCharset(charset);
        return this;
    }

    public NativeProcessBuilder setStatusEx(boolean b) {
        info.setStatusEx(b);
        return this;
    }
}
