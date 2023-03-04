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

package org.netbeans.modules.php.api.executable;

import java.awt.EventQueue;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.base.ProcessBuilder;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.api.PhpOptions;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.spi.executable.DebugStarter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * Class usable for running any PHP executable (program or script).
 */
public final class PhpExecutable {

    private static final Logger LOGGER = Logger.getLogger(PhpExecutable.class.getName());

    private static final Project DUMMY_PROJECT = new DummyProject();

    /**
     * The {@link ExecutionDescriptor.InputProcessorFactory2 input processor factory} that strips any
     * <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">ANSI escape sequences</a>.
     * <p>
     * <b>In fact, it is not needed anymore since the Output window understands ANSI escape sequences.</b>
     * @see InputProcessors#ansiStripping(InputProcessor)
     * @since 0.28
     */
    public static final ExecutionDescriptor.InputProcessorFactory2 ANSI_STRIPPING_FACTORY = new ExecutionDescriptor.InputProcessorFactory2() {
        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.ansiStripping(defaultProcessor);
        }
    };

    /**
     * This descriptor is:
     * <ul>
     *   <li>{@link ExecutionDescriptor#isControllable() controllable}</li>
     *   <li>{@link ExecutionDescriptor#isFrontWindow() displays the Output window}</li>
     *   <li>{@link ExecutionDescriptor#isFrontWindowOnError()  displays the Output window on error (since 1.62)}</li>
     *   <li>{@link ExecutionDescriptor#isInputVisible() has visible user input}</li>
     *   <li>{@link ExecutionDescriptor#showProgress() shows progress}</li>
     * </ul>
     */
    public static final ExecutionDescriptor DEFAULT_EXECUTION_DESCRIPTOR = new ExecutionDescriptor()
            .controllable(true)
            .frontWindow(true)
            .frontWindowOnError(true)
            .inputVisible(true)
            .showProgress(true);

    private final String executable;
    private final List<String> parameters;
    private final String command;
    final List<String> fullCommand = new CopyOnWriteArrayList<>();

    private String executableName = null;
    private String displayName = null;
    private String optionsSubcategory = null;
    private boolean viaPhpInterpreter = false;
    private boolean viaAutodetection = true;
    private boolean redirectErrorStream = false;
    private File workDir = null;
    private boolean warnUser = true;
    private List<String> additionalParameters = Collections.<String>emptyList();
    private Map<String, String> environmentVariables = Collections.<String, String>emptyMap();
    private PhpExecutableValidator.ValidationHandler validationHandler = null;
    private File fileOutput = null;
    private Charset outputCharset = null;
    private boolean fileOutputOnly = false;
    private boolean noInfo = false;
    private boolean noDebugConfig = false;


    /**
     * Parse command which can be just binary or binary with parameters.
     * As a parameter separator, "-" or "/" is used.
     * @param command command to parse, can be {@code null}.
     */
    public PhpExecutable(String command) {
        Pair<String, List<String>> parsedCommand = parseCommand(command);
        executable = parsedCommand.first();
        parameters = parsedCommand.second();
        this.command = command.trim();
    }

    static Pair<String, List<String>> parseCommand(String command) {
        if (command == null) {
            // avoid NPE
            command = ""; // NOI18N
        }
        // try to find program (search for " -" or " /" after space)
        String[] tokens = command.split(" * (?=\\-|/)", 2); // NOI18N
        if (tokens.length == 1) {
            LOGGER.log(Level.FINE, "Only program given (no parameters): {0}", command);
            return Pair.of(tokens[0].trim(), Collections.<String>emptyList());
        }
        Pair<String, List<String>> parsedCommand = Pair.of(tokens[0].trim(), Arrays.asList(Utilities.parseParameters(tokens[1].trim())));
        LOGGER.log(Level.FINE, "Parameters parsed: {0} {1}", new Object[] {parsedCommand.first(), parsedCommand.second()});
        return parsedCommand;
    }

    /**
     * Get PHP executable, never {@code null}.
     * @return PHP program, never {@code null}.
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Get parameters, can be an empty array but never {@code null}.
     * @return parameters, can be an empty array but never {@code null}.
     */
    public List<String> getParameters() {
        return new ArrayList<>(parameters);
    }

    /**
     * Get the command, in the original form (just without leading and trailing whitespaces).
     * @return the command, in the original form (just without leading and trailing whitespaces).
     */
    public String getCommand() {
        return command;
    }

    /**
     * Set name of the executable. This name is used for {@link PhpExecutableValidator validation} only (before running).
     * <p>
     * The default value is {@code null} (it means "File").
     * @param executableName name of the executable
     * @return the PHP Executable instance itself
     */
    public PhpExecutable executableName(@NonNull String executableName) {
        Parameters.notEmpty("executableName", executableName); // NOI18N
        this.executableName = executableName;
        return this;
    }

    /**
     * Set display name that is used for executable running (as a title of the Output window).
     * <p>
     * The default value is {@link #getExecutable() executable} with {@link #getParameters() parameters}.
     * @param displayName display name that is used for executable running
     * @return the PHP Executable instance itself
     */
    public PhpExecutable displayName(String displayName) {
        Parameters.notEmpty("displayName", displayName); // NOI18N
        this.displayName = displayName;
        return this;
    }

    /**
     * Set identifier of the IDE PHP Options. If the executable is not {@link PhpExecutableValidator valid} and user should be
     * {@link #warnUser(boolean) warned} about it, IDE Options are opened with this category selected.
     * <p>
     * The default value is {@code null} (the General PHP category).
     * @param optionsSubcategory identifier of the IDE PHP Options
     * @return the PHP Executable instance itself
     */
    public PhpExecutable optionsSubcategory(String optionsSubcategory) {
        Parameters.notEmpty("optionsSubcategory", optionsSubcategory); // NOI18N
        this.optionsSubcategory = optionsSubcategory;
        return this;
    }

    /**
     * Set running this executable via {@link PhpInterpreter}.
     * <p>
     * The default value is {@code false} (it means use {@link #viaAutodetection(boolean) autodetection}).
     * @param viaPhpInterpreter {@code true} if running via {@link PhpInterpreter} should be forced, {@code false} otherwise
     * @return the PHP Executable instance itself
     */
    public PhpExecutable viaPhpInterpreter(boolean viaPhpInterpreter) {
        this.viaPhpInterpreter = viaPhpInterpreter;
        return this;
    }

    /**
     * Set running this executable via autodetection.
     * <p>
     * The default value is {@code true} (it means only <i>*.bat</i> files are run directly; the others are run via {@link PhpInterpreter}).
     * @param viaAutodetection {@code true} if running via autodetection should be forced, {@code false} otherwise
     * @return the PHP Executable instance itself
     */
    public PhpExecutable viaAutodetection(boolean viaAutodetection) {
        this.viaAutodetection = viaAutodetection;
        return this;
    }

    /**
     * Set error stream redirection.
     * <p>
     * The default value is {@code false} (it means that the error stream is not redirected to the standard output).
     * @param redirectErrorStream {@code true} if error stream should be redirected, {@code false} otherwise
     * @return the PHP Executable instance itself
     */
    public PhpExecutable redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    /**
     * Set working directory for {@link #run() running} this executable.
     * <p>
     * The default value is {@code null} ("unknown" directory).
     * @param workDir working directory for {@link #run() running} this executable
     * @return the PHP Executable instance itself
     */
    public PhpExecutable workDir(@NonNull File workDir) {
        Parameters.notNull("workDir", workDir); // NOI18N
        this.workDir = workDir;
        return this;
    }

    /**
     * Set whether user should be warned before {@link #run() running} in case of invalid command.
     * <p>
     * The default value is {@code true} (it means that the user is informed).
     * @param warnUser {@code true} if user should be warned, {@code false} otherwise
     * @return the PHP Executable instance itself
     */
    public PhpExecutable warnUser(boolean warnUser) {
        this.warnUser = warnUser;
        return this;
    }

    /**
     * Set addition parameters for {@link #run() running}.
     * <p>
     * The default value is empty list (it means no additional parameters).
     * @param additionalParameters addition parameters for {@link #run() running}.
     * @return the PHP Executable instance itself
     */
    public PhpExecutable additionalParameters(@NonNull List<String> additionalParameters) {
        Parameters.notNull("additionalParameters", additionalParameters); // NOI18N
        this.additionalParameters = additionalParameters;
        return this;
    }

    /**
     * Set addition parameters for {@link #run() running}.
     * <p>
     * The default value is empty list (it means no additional parameters).
     * @param additionalParameters addition parameters for {@link #run() running}.
     * @return the PHP Executable instance itself
     */
    public PhpExecutable environmentVariables(Map<String, String> environmentVariables) {
        Parameters.notNull("environmentVariables", environmentVariables); // NOI18N
        this.environmentVariables = environmentVariables;
        return this;
    }

    /**
     * Set {@link PhpExecutableValidator.ValidationHandler validation handler} for executable {@link PhpExecutableValidator validator}.
     * <p>
     * The default value is {@code null} (it means use {@link PhpExecutableValidator#validateCommand(String, String) default validation}).
     * @param validationHandler validation handler for executable validation
     * @return the PHP Executable instance itself
     */
    public PhpExecutable validationHandler(@NonNull PhpExecutableValidator.ValidationHandler validationHandler) {
        Parameters.notNull("validationHandler", validationHandler); // NOI18N
        this.validationHandler = validationHandler;
        return this;
    }

    /**
     * Set file for executable output; also set whether only output to file should be used (no Output window).
     * <p>
     * The default value is {@code null} and {@code false} (it means no output is stored to any file
     * and info is printed in Output window).
     * @param fileOutput file for executable output
     * @param outputCharset charset to be used for the output
     * @param fileOutputOnly {@code true} for only file output, {@code false} otherwise
     * @return the PHP Executable instance itself
     * @see #noInfo(boolean)
     * @since 0.17
     */
    public PhpExecutable fileOutput(@NonNull File fileOutput, @NonNull String outputCharset, boolean fileOutputOnly) {
        Parameters.notNull("fileOutput", fileOutput); // NOI18N
        Parameters.notNull("outputCharset", outputCharset); // NOI18N
        this.fileOutput = fileOutput;
        this.outputCharset = Charset.forName(outputCharset);
        this.fileOutputOnly = fileOutputOnly;
        return this;
    }

    /**
     * Set no information. If Output window is used, no info about this executable is printed.
     * <p>
     * The default value is {@code false} (it means print info about this executable).
     * @param noInfo {@code true} for pure output only (no info about executable)
     * @return the PHP Executable instance itself
     * @since 0.3
     */
    public PhpExecutable noInfo(boolean noInfo) {
        this.noInfo = noInfo;
        return this;
    }

    /**
     * Set no debug config. Normally, debug config is done automatically (set XDEBUG_CONFIG environemnt variable).
     * Set it to {@code false} if the debug config is done some other way (e.g. using command line parameter).
     * <p>
     * The default value is {@code false} (it means debug is configured).
     * @param noDebugConfig {@code true} for no debug config
     * @return the PHP Executable instance itself
     * @since 0.19
     */
    public PhpExecutable noDebugConfig(boolean noDebugConfig) {
        this.noDebugConfig = noDebugConfig;
        return this;
    }

    /**
     * Run this executable with the {@link #DEFAULT_EXECUTION_DESCRIPTOR default execution descriptor}.
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     * or {@code null} if the executable cannot be run
     * @see #run(ExecutionDescriptor)
     * @see #run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2)
     * @see ExecutionService#run()
     */
    @CheckForNull
    public Future<Integer> run() {
        return run(DEFAULT_EXECUTION_DESCRIPTOR);
    }

    /**
     * Run this executable with the given execution descriptor.
     * <p>
     * <b>WARNING:</b> If any {@link ExecutionDescriptor.InputProcessorFactory2 output processor factory} should be used, use
     * {@link PhpExecutable#run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2) run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2)} instead.
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     * or {@code null} if the executable cannot be run
     * @see #run()
     * @see #run(ExecutionDescriptor)
     * @see ExecutionService#run()
     */
    @CheckForNull
    public Future<Integer> run(@NonNull ExecutionDescriptor executionDescriptor) {
        return run(executionDescriptor, (ExecutionDescriptor.InputProcessorFactory2) null);
    }

    /**
     * Run this executable with the given execution descriptor and optional output processor factory.
     * <p>
     * @param executionDescriptor execution descriptor to be used
     * @param outProcessorFactory output processor factory to be used, can be {@code null}
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     * or {@code null} if the executable cannot be run
     * @see #run()
     * @see #run(ExecutionDescriptor)
     * @see #run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2)
     * @see ExecutionService#run()
     * @since 0.28
     */
    @CheckForNull
    public Future<Integer> run(@NonNull ExecutionDescriptor executionDescriptor, @NullAllowed ExecutionDescriptor.InputProcessorFactory2 outProcessorFactory) {
        Parameters.notNull("executionDescriptor", executionDescriptor); // NOI18N
        return runInternal(executionDescriptor, outProcessorFactory, false);
    }

    /**
     * Run this executable with the {@link #DEFAULT_EXECUTION_DESCRIPTOR default execution descriptor}, <b>blocking but not blocking the UI thread</b>
     * (it displays progress dialog if it is running in it).
     * @param progressMessage message displayed if the task is running in the UI thread
     * @return exit code of the process or {@code null} if any error occured
     * @throws ExecutionException if any error occurs
     * @see #runAndWait(ExecutionDescriptor, String)
     * @see #runAndWait(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2, String)
     */
    @CheckForNull
    public Integer runAndWait(@NonNull String progressMessage) throws ExecutionException {
        return runAndWait(DEFAULT_EXECUTION_DESCRIPTOR, progressMessage);
    }

    /**
     * Run this executable with the given execution descriptor, <b>blocking but not blocking the UI thread</b>
     * (it displays progress dialog if it is running in it).
     * <p>
     * <b>WARNING:</b> If any {@link ExecutionDescriptor.InputProcessorFactory2 output processor factory} should be used, use
     * {@link PhpExecutable#runAndWait(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2, String) run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2, String)} instead.
     * @param executionDescriptor execution descriptor to be used
     * @param progressMessage message displayed if the task is running in the UI thread
     * @return exit code of the process or {@code null} if any error occured
     * @throws ExecutionException if any error occurs
     * @see #runAndWait(String)
     * @see #runAndWait(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2, String)
     */
    @CheckForNull
    public Integer runAndWait(@NonNull ExecutionDescriptor executionDescriptor, @NonNull String progressMessage) throws ExecutionException {
        return runAndWait(executionDescriptor, null, progressMessage);
    }

    /**
     * Run this executable with the given execution descriptor and optional output processor factory, <b>blocking but not blocking the UI thread</b>
     * (it displays progress dialog if it is running in it).
     * @param executionDescriptor execution descriptor to be used
     * @param outProcessorFactory output processor factory to be used, can be {@code null}
     * @param progressMessage message displayed if the task is running in the UI thread
     * @return exit code of the process or {@code null} if any error occured
     * @throws ExecutionException if any error occurs
     * @see #runAndWait(String)
     * @see #runAndWait(ExecutionDescriptor, String)
     * @since 0.28
     */
    @CheckForNull
    public Integer runAndWait(@NonNull ExecutionDescriptor executionDescriptor, @NullAllowed ExecutionDescriptor.InputProcessorFactory2 outProcessorFactory,
            @NonNull final String progressMessage) throws ExecutionException {
        Parameters.notNull("progressMessage", progressMessage); // NOI18N
        final Future<Integer> result = run(executionDescriptor, outProcessorFactory);
        if (result == null) {
            return null;
        }
        final AtomicReference<ExecutionException> executionException = new AtomicReference<>();
        if (SwingUtilities.isEventDispatchThread()) {
            if (!result.isDone()) {
                try {
                    // let's wait in EDT to avoid flashing dialogs
                    getResult(result, 90L);
                } catch (TimeoutException ex) {
                    BaseProgressUtils.showProgressDialogAndRun(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getResult(result);
                            } catch (ExecutionException extEx) {
                                executionException.set(extEx);
                            }
                        }
                    }, progressMessage);
                }
            }
        }
        if (executionException.get() != null) {
            throw executionException.get();
        }
        return getResult(result);
    }

    /**
     * Debug this executable with the {@link #DEFAULT_EXECUTION_DESCRIPTOR default execution descriptor}, <b>blocking but not blocking the UI thread</b>
     * (it displays progress dialog if it is running in it).
     * @param startFile the start file
     * @return exit code of the process or {@code null} if any error occured
     * @throws ExecutionException if any error occurs
     * @see #debug(FileObject, ExecutionDescriptor)
     * @see #debug(FileObject, ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2)
     */
    @CheckForNull
    public Integer debug(@NonNull FileObject startFile) throws ExecutionException {
        return debug(startFile, DEFAULT_EXECUTION_DESCRIPTOR);
    }

    /**
     * Debug this executable with the given execution descriptor, <b>blocking but not blocking the UI thread</b>
     * (it displays progress dialog if it is running in it).
     * @param startFile the start file
     * @param executionDescriptor execution descriptor to be used (never controllable)
     * @return exit code of the process or {@code null} if any error occured
     * @throws ExecutionException if any error occurs
     * @see #debug(FileObject)
     * @see #debug(FileObject, ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2)
     */
    @CheckForNull
    public Integer debug(@NonNull FileObject startFile, @NonNull ExecutionDescriptor executionDescriptor) throws ExecutionException {
        return debug(startFile, executionDescriptor, null);
    }

    /**
     * Debug this executable with the given execution descriptor and optional output processor factory, <b>blocking but not blocking the UI thread</b>
     * (it displays progress dialog if it is running in it).
     * @param startFile the start file
     * @param executionDescriptor execution descriptor to be used (never controllable)
     * @param outProcessorFactory output processor factory to be used, can be {@code null}
     * @return exit code of the process or {@code null} if any error occured
     * @throws ExecutionException if any error occurs
     * @see #debug(FileObject)
     * @see #debug(FileObject, ExecutionDescriptor)
     * @since 0.28
     */
    @NbBundle.Messages("PhpExecutable.debug.progress=Debugging...")
    @CheckForNull
    public Integer debug(@NonNull final FileObject startFile, @NonNull final ExecutionDescriptor executionDescriptor,
            @NullAllowed final ExecutionDescriptor.InputProcessorFactory2 outProcessorFactory) throws ExecutionException {
        if (!EventQueue.isDispatchThread()) {
            return debugInternal(startFile, executionDescriptor, outProcessorFactory);
        }
        // ui thread
        final AtomicReference<Integer> executionResult = new AtomicReference<>();
        final AtomicReference<ExecutionException> executionException = new AtomicReference<>();
        BaseProgressUtils.showProgressDialogAndRun(new Runnable() {
            @Override
            public void run() {
                try {
                    executionResult.set(debugInternal(startFile, executionDescriptor, outProcessorFactory));
                } catch (ExecutionException ex) {
                    executionException.set(ex);
                }
            }
        }, Bundle.PhpExecutable_debug_progress());
        if (executionException.get() != null) {
            throw executionException.get();
        }
        return executionResult.get();
    }

    @CheckForNull
    Integer debugInternal(@NonNull FileObject startFile, @NonNull ExecutionDescriptor executionDescriptor,
            @NullAllowed final ExecutionDescriptor.InputProcessorFactory2 outProcessorFactory) throws ExecutionException {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Debugging cannot be called from the UI thread");
        }
        DebugStarter dbgStarter =  Lookup.getDefault().lookup(DebugStarter.class);
        assert dbgStarter != null;
        if (dbgStarter.isAlreadyRunning()) {
            if (warnNoMoreDebugSession()) {
                dbgStarter.stop();
                return debug(startFile, executionDescriptor, outProcessorFactory);
            }
        }
        // never controllable for debugging
        final ExecutionDescriptor notControllableExecutionDescriptor = executionDescriptor
                .controllable(false);
        final AtomicReference<Future<Integer>> result = new AtomicReference<>();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Callable<Cancellable> task = new Callable<Cancellable>() {
            @Override
            public Cancellable call() throws Exception {
                try {
                    result.set(PhpExecutable.this.runInternal(notControllableExecutionDescriptor, outProcessorFactory, true));
                } finally {
                    countDownLatch.countDown();
                }
                return new Cancellable() {
                    @Override
                    public boolean cancel() {
                        // debugger calls us, simply return true
                        // do NOT call cancel on the result otherwise no more process output is written/processed!
                        return true;
                    }
                };
            }
        };
        Project project = FileOwnerQuery.getOwner(startFile);
        DebugStarter.Properties props = new DebugStarter.Properties.Builder()
                .setStartFile(startFile)
                .setCloseSession(true)
                .setEncoding(FileEncodingQuery.getEncoding(startFile).name())
                .build();
        dbgStarter.start(project != null ? project : DUMMY_PROJECT, task, props);
        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        Future<Integer> res = result.get();
        if (res == null) {
            return null;
        }
        // wait for debugger to finish
        try {
            res.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (CancellationException ex) {
            // debugger ends correctly
            return 0;
        }
        return 1;
    }

    @CheckForNull
    private Future<Integer> runInternal(ExecutionDescriptor executionDescriptor, ExecutionDescriptor.InputProcessorFactory2 outProcessorFactory, boolean debug) {
        Parameters.notNull("executionDescriptor", executionDescriptor); // NOI18N
        String error;
        if (validationHandler == null) {
            error = PhpExecutableValidator.validateCommand(executable, executableName);
        } else {
            error = PhpExecutableValidator.validateCommand(executable, validationHandler);
        }
        if (error != null) {
            if (warnUser) {
                // optionsSubcategory should be taken from executionDescriptor (unfortunately not possible)
                UiUtils.invalidScriptProvided(error, optionsSubcategory);
            }
            return null;
        }
        ProcessBuilder processBuilder = getProcessBuilder(debug);
        if (processBuilder == null) {
            return null;
        }
        executionDescriptor = getExecutionDescriptor(executionDescriptor, outProcessorFactory);
        return ExecutionService.newService(processBuilder, executionDescriptor, getDisplayName()).run();
    }

    @CheckForNull
    private ProcessBuilder getProcessBuilder(boolean debug) {
        Pair<ProcessBuilder, List<String>> processBuilderInfo = createProcessBuilder();
        if (processBuilderInfo == null) {
            return null;
        }
        ProcessBuilder processBuilder = processBuilderInfo.first();
        List<String> arguments = processBuilderInfo.second();
        for (String param : parameters) {
            fullCommand.add(param);
            arguments.add(param);
        }
        for (String param : additionalParameters) {
            fullCommand.add(param);
            arguments.add(param);
        }
        processBuilder.setArguments(arguments);
        if (workDir != null) {
            processBuilder.setWorkingDirectory(workDir.getAbsolutePath());
        }
        for (Map.Entry<String, String> variable : environmentVariables.entrySet()) {
            processBuilder.getEnvironment().setVariable(variable.getKey(), variable.getValue());
        }
        processBuilder.setRedirectErrorStream(redirectErrorStream);
        if (debug
                && !noDebugConfig) {
            processBuilder.getEnvironment().setVariable("XDEBUG_CONFIG", "idekey=" + Lookup.getDefault().lookup(PhpOptions.class).getDebuggerSessionId()); // NOI18N
        }
        return processBuilder;
    }

    @CheckForNull
    private Pair<ProcessBuilder, List<String>> createProcessBuilder() {
        List<String> arguments = new ArrayList<>();
        fullCommand.clear();
        boolean useInterpreter = viaPhpInterpreter;
        if (viaAutodetection) {
            String exec = executable.toLowerCase();
            if (exec.endsWith(".phar")) { // NOI18N
                useInterpreter = true;
            } else {
                FileObject fo = FileUtil.toFileObject(new File(executable));
                assert fo != null : executable;
                if (FileUtils.isPhpFile(fo)) {
                    useInterpreter = true;
                } else if (Utilities.isWindows()) {
                    if (exec.endsWith(".bat") // NOI18N
                            || exec.endsWith(".cmd")) { // NOI18N
                        useInterpreter = false;
                    }
                } else {
                    if (exec.endsWith(".sh")) { // NOI18N
                        useInterpreter = false;
                    }
                }
            }
        }
        if (!useInterpreter) {
            fullCommand.add(executable);
            ProcessBuilder processBuilder = ProcessBuilder.getLocal();
            processBuilder.setExecutable(executable);
            return Pair.of(processBuilder, arguments);
        }
        PhpInterpreter phpInterpreter;
        try {
            phpInterpreter = PhpInterpreter.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            if (warnUser) {
                UiUtils.invalidScriptProvided(ex.getLocalizedMessage());
            }
            return null;
        }
        fullCommand.add(phpInterpreter.getInterpreter());
        ProcessBuilder processBuilder = ProcessBuilder.getLocal();
        processBuilder.setExecutable(phpInterpreter.getInterpreter());
        for (String param : phpInterpreter.getParameters()) {
            fullCommand.add(param);
            arguments.add(param);
        }
        fullCommand.add(executable);
        arguments.add(executable);
        return Pair.of(processBuilder, arguments);
    }

    private String getDisplayName() {
        if (displayName != null) {
            return displayName;
        }
        return getDefaultDisplayName();
    }

    private String getDefaultDisplayName() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append(executable);
        for (String param : parameters) {
            buffer.append(" "); // NOI18N
            buffer.append(param);
        }
        return buffer.toString();
    }

    static Integer getResult(Future<Integer> result) throws ExecutionException {
        try {
            return getResult(result, null);
        } catch (TimeoutException ex) {
            // in fact, cannot happen since we don't use timeout
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

    private static Integer getResult(Future<Integer> result, Long timeout) throws TimeoutException, ExecutionException {
        try {
            if (timeout != null) {
                return result.get(timeout, TimeUnit.MILLISECONDS);
            }
            return result.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private ExecutionDescriptor getExecutionDescriptor(ExecutionDescriptor executionDescriptor, ExecutionDescriptor.InputProcessorFactory2 outProcessorFactory) {
        final List<ExecutionDescriptor.InputProcessorFactory2> inputProcessors = new CopyOnWriteArrayList<>();
        // colors
        ExecutionDescriptor.InputProcessorFactory2 infoOutProcessorFactory = getInfoOutputProcessorFactory();
        if (infoOutProcessorFactory != null) {
            inputProcessors.add(infoOutProcessorFactory);
        }
        // file output
        ExecutionDescriptor.InputProcessorFactory2 fileOutProcessorFactory = getFileOutputProcessorFactory();
        if (fileOutProcessorFactory != null) {
            inputProcessors.add(fileOutProcessorFactory);
            if (fileOutputOnly) {
                executionDescriptor = executionDescriptor
                        .inputOutput(InputOutput.NULL)
                        .frontWindow(false)
                        .frontWindowOnError(false);
            }
        }
        if (outProcessorFactory != null) {
            inputProcessors.add(outProcessorFactory);
        }
        if (!inputProcessors.isEmpty()) {
            executionDescriptor = executionDescriptor.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory2() {
                @Override
                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    InputProcessor[] processors = new InputProcessor[inputProcessors.size()];
                    for (int i = 0; i < inputProcessors.size(); ++i) {
                        processors[i] = inputProcessors.get(i).newInputProcessor(defaultProcessor);
                    }
                    return InputProcessors.proxy(processors);
                }
            });
        }
        return executionDescriptor;
    }

    private ExecutionDescriptor.InputProcessorFactory2 getInfoOutputProcessorFactory() {
        if (noInfo) {
            // no info
            return null;
        }
        return new ExecutionDescriptor.InputProcessorFactory2() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.proxy(new InfoInputProcessor(defaultProcessor, fullCommand), defaultProcessor);
            }
        };
    }

    private ExecutionDescriptor.InputProcessorFactory2 getFileOutputProcessorFactory() {
        if (fileOutput == null) {
            return null;
        }
        return new ExecutionDescriptor.InputProcessorFactory2() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return new RedirectOutputProcessor(fileOutput, outputCharset);
            }
        };
    }

    @NbBundle.Messages("PhpExecutable.debug.noMoreSessions=Debugger session is already running. Restart?")
    private static boolean warnNoMoreDebugSession() {
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(Bundle.PhpExecutable_debug_noMoreSessions(), NotifyDescriptor.OK_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [executable: "); // NOI18N
        sb.append(executable);
        sb.append(", parameters: "); // NOI18N
        sb.append(parameters);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    //~ Inner classes

    static final class InfoInputProcessor implements InputProcessor {

        private final InputProcessor defaultProcessor;
        private char lastChar;


        public InfoInputProcessor(InputProcessor defaultProcessor, List<String> fullCommand) {
            this.defaultProcessor = defaultProcessor;
            String infoCommand = colorize(getInfoCommand(fullCommand)) + "\n"; // NOI18N
            try {
                defaultProcessor.processInput(infoCommand.toCharArray());
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            if (chars.length > 0) {
                lastChar = chars[chars.length - 1];
            }
        }

        @Override
        public void reset() throws IOException {
            // noop
        }

        @NbBundle.Messages("InfoInputProcessor.done=Done.")
        @Override
        public void close() throws IOException {
            StringBuilder msg = new StringBuilder(Bundle.InfoInputProcessor_done().length() + 2);
            if (!isNewLine(lastChar)) {
                msg.append("\n"); // NOI18N
            }
            msg.append(colorize(Bundle.InfoInputProcessor_done()));
            msg.append("\n"); // NOI18N
            defaultProcessor.processInput(msg.toString().toCharArray());
        }

        public static String getInfoCommand(List<String> fullCommand) {
            List<String> escapedCommand = new ArrayList<>(fullCommand.size());
            for (String command : fullCommand) {
                escapedCommand.add("\"" + command.replace("\"", "\\\"") + "\""); // NOI18N
            }
            return StringUtils.implode(escapedCommand, " "); // NOI18N
        }

        private static String colorize(String msg) {
            return "\033[1;30m" + msg + "\033[0m"; // NOI18N
        }

        private static boolean isNewLine(char ch) {
            return ch == '\n' || ch == '\r' || ch == '\u0000'; // NOI18N
        }

    }

    static final class RedirectOutputProcessor implements InputProcessor {

        private final File fileOuput;
        private final Charset outputCharset;

        private OutputStream outputStream;


        public RedirectOutputProcessor(File fileOuput, Charset outputCharset) {
            assert fileOuput != null;
            assert outputCharset != null;
            this.fileOuput = fileOuput;
            this.outputCharset = outputCharset;
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            if (outputStream == null) {
                outputStream = new BufferedOutputStream(new FileOutputStream(fileOuput));
            }
            ByteBuffer byteBuffer = outputCharset.encode(CharBuffer.wrap(chars));
            byte[] bytes = byteBuffer.array();
            byte[] compactedBytes = new byte[byteBuffer.limit()];
            System.arraycopy(bytes, 0, compactedBytes, 0, compactedBytes.length);
            outputStream.write(compactedBytes);
        }

        @Override
        public void reset() {
            // noop
        }

        @Override
        public void close() throws IOException {
            if (outputStream != null) {
                outputStream.close();
            }
        }

    }

    // needed for php debugger, used as a key in session map
    static final class DummyProject implements Project {

        @Override
        public FileObject getProjectDirectory() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

    }

}
