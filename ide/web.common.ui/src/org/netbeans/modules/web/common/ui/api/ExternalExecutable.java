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
package org.netbeans.modules.web.common.ui.api;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.base.ProcessBuilder;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.modules.web.common.spi.ExternalExecutableUserWarning;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.windows.InputOutput;

/**
 * Class usable for running any external executable (program or script).
 * @since 1.74
 */
public final class ExternalExecutable {

    private static final Logger LOGGER = Logger.getLogger(ExternalExecutable.class.getName());

    /**
     * The default {@link ExecutionDescriptor execution descriptor}. This descriptor is:
     * <ul>
     *   <li>{@link ExecutionDescriptor#isControllable() controllable}</li>
     *   <li>{@link ExecutionDescriptor#isFrontWindow() displays the Output window}</li>
     *   <li>{@link ExecutionDescriptor#isFrontWindowOnError()  displays the Output window on error (since 1.62)}</li>
     *   <li>{@link ExecutionDescriptor#isInputVisible() has visible user input}</li>
     *   <li>{@link ExecutionDescriptor#showProgress() shows progress}</li>
     *   <li>{@link ExecutionDescriptor#charset(java.nio.charset.Charset) charset set to UTF-8}</li>
     * </ul>
     */
    public static final ExecutionDescriptor DEFAULT_EXECUTION_DESCRIPTOR = new ExecutionDescriptor()
            .controllable(true)
            .frontWindow(true)
            .frontWindowOnError(true)
            .inputVisible(true)
            .showProgress(true)
            .charset(StandardCharsets.UTF_8);

    private final String executable;
    private final List<String> parameters;
    private final String command;
    final List<String> fullCommand = new CopyOnWriteArrayList<>();

    private String executableName = null;
    private String displayName = null;
    private String optionsPath = null;
    private boolean redirectErrorStream = false;
    private File workDir = null;
    private boolean warnUser = true;
    private List<String> additionalParameters = Collections.<String>emptyList();
    private Map<String, String> environmentVariables = Collections.<String, String>emptyMap();
    private File fileOutput = null;
    private boolean fileOutputOnly = false;
    private boolean noInfo = false;
    private boolean noOutput = false;


    /**
     * Parse command which can be just binary or binary with parameters.
     * As a parameter separator, "-" or "/" is used.
     * @param command command to parse, can be {@code null}.
     */
    public ExternalExecutable(String command) {
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
        Pair<String, List<String>> parsedCommand = Pair.of(tokens[0].trim(), Arrays.asList(BaseUtilities.parseParameters(tokens[1].trim())));
        LOGGER.log(Level.FINE, "Parameters parsed: {0} {1}", new Object[] {parsedCommand.first(), parsedCommand.second()});
        return parsedCommand;
    }

    /**
     * Get external executable, never {@code null}.
     * @return external executable, never {@code null}.
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
     * Set name of the executable. This name is used for {@link ExternalExecutableValidator validation} only (before running).
     * <p>
     * The default value is {@code null} (it means "File").
     * @param executableName name of the executable
     * @return the external executable instance itself
     */
    public ExternalExecutable executableName(@NonNull String executableName) {
        Parameters.notEmpty("executableName", executableName); // NOI18N
        this.executableName = executableName;
        return this;
    }

    /**
     * Set display name that is used for executable running (as a title of the Output window).
     * <p>
     * The default value is {@link #getExecutable() executable} with {@link #getParameters() parameters}.
     * @param displayName display name that is used for executable running
     * @return the external executable instance itself
     */
    public ExternalExecutable displayName(String displayName) {
        Parameters.notEmpty("displayName", displayName); // NOI18N
        this.displayName = displayName;
        return this;
    }

    /**
     * Set IDE Options path which is used if executable is {@link ExternalExecutableValidator#validateCommand(java.lang.String, java.lang.String) invalid}.
     * Please notice that IDE Options are opened only if {@link #warnUser(boolean) is set}.
     * @param optionsPath IDE Options path used in case of invalid executable
     * @return the external executable instance itself
     */
    public ExternalExecutable optionsPath(String optionsPath) {
        Parameters.notEmpty("optionsPath", optionsPath); // NOI18N
        this.optionsPath = optionsPath;
        return this;
    }

    /**
     * Set error stream redirection.
     * <p>
     * The default value is {@code false} (it means that the error stream is not redirected to the standard output).
     * @param redirectErrorStream {@code true} if error stream should be redirected, {@code false} otherwise
     * @return the external executable instance itself
     */
    public ExternalExecutable redirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    /**
     * Set working directory for {@link #run() running} this executable.
     * <p>
     * The default value is {@code null} ("unknown" directory).
     * @param workDir working directory for {@link #run() running} this executable
     * @return the external executable instance itself
     */
    public ExternalExecutable workDir(@NonNull File workDir) {
        Parameters.notNull("workDir", workDir); // NOI18N
        this.workDir = workDir;
        return this;
    }

    /**
     * Set whether user should be warned before {@link #run() running} in case of invalid command.
     * <p>
     * The default value is {@code true} (it means that the user is informed).
     * @param warnUser {@code true} if user should be warned, {@code false} otherwise
     * @return the external executable instance itself
     */
    public ExternalExecutable warnUser(boolean warnUser) {
        this.warnUser = warnUser;
        return this;
    }

    /**
     * Set addition parameters for {@link #run() running}.
     * <p>
     * The default value is empty list (it means no additional parameters).
     * @param additionalParameters addition parameters for {@link #run() running}.
     * @return the external executable instance itself
     */
    public ExternalExecutable additionalParameters(@NonNull List<String> additionalParameters) {
        Parameters.notNull("additionalParameters", additionalParameters); // NOI18N
        this.additionalParameters = additionalParameters;
        return this;
    }

    /**
     * Set environment variables for {@link #run() running}.
     * <p>
     * The default value is empty list (it means no environment variables).
     * @param environmentVariables addition parameters for {@link #run() running}.
     * @return the external executable instance itself
     */
    public ExternalExecutable environmentVariables(Map<String, String> environmentVariables) {
        Parameters.notNull("environmentVariables", environmentVariables); // NOI18N
        this.environmentVariables = environmentVariables;
        return this;
    }

    /**
     * Set file for executable output; also set whether only output to file should be used (no Output window).
     * <p>
     * The default value is {@code null} and {@code false} (it means no output is stored to any file
     * and info is printed in Output window).
     * @param fileOutput file for executable output
     * @param fileOutputOnly {@code true} for only file output, {@code false} otherwise
     * @return the external executable instance itself
     * @see #noInfo(boolean)
     */
    public ExternalExecutable fileOutput(@NonNull File fileOutput, boolean fileOutputOnly) {
        Parameters.notNull("fileOutput", fileOutput); // NOI18N
        this.fileOutput = fileOutput;
        this.fileOutputOnly = fileOutputOnly;
        return this;
    }

    /**
     * Set no information. If Output window is used, no info about this executable is printed.
     * <p>
     * The default value is {@code false} (it means print info about this executable).
     * @param noInfo {@code true} for pure output only (no info about executable)
     * @return the external executable instance itself
     */
    public ExternalExecutable noInfo(boolean noInfo) {
        this.noInfo = noInfo;
        return this;
    }

    /**
     * Set no output. If Output window is used, no output is printed.
     * <p>
     * The default value is {@code false} (it means print output of this executable).
     * @param noOutput {@code true} if no output should be printed
     * @return the external executable instance itself
     */
    public ExternalExecutable noOutput(boolean noOutput) {
        this.noOutput = noOutput;
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
     * {@link ExternalExecutable#run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2) run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2)} instead.
     * @param executionDescriptor execution descriptor
     * @return task representing the actual run, value representing result of the {@link Future} is exit code of the process
     * or {@code null} if the executable cannot be run
     * @see #run()
     * @see #run(ExecutionDescriptor)
     * @see ExecutionService#run()
     */
    @CheckForNull
    public Future<Integer> run(@NonNull ExecutionDescriptor executionDescriptor) {
        return run(executionDescriptor, null);
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
     * @since 0.2
     */
    @CheckForNull
    public Future<Integer> run(@NonNull ExecutionDescriptor executionDescriptor, @NullAllowed ExecutionDescriptor.InputProcessorFactory2 outProcessorFactory) {
        Parameters.notNull("executionDescriptor", executionDescriptor); // NOI18N
        return runInternal(executionDescriptor, outProcessorFactory);
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
     * {@link ExternalExecutable#runAndWait(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2, String) run(ExecutionDescriptor, ExecutionDescriptor.InputProcessorFactory2, String)} instead.
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
        if (Mutex.EVENT.isReadAccess()) {   // Is EDT
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

    @CheckForNull
    private Future<Integer> runInternal(ExecutionDescriptor executionDescriptor, ExecutionDescriptor.InputProcessorFactory2 outProcessorFactory) {
        Parameters.notNull("executionDescriptor", executionDescriptor); // NOI18N
        final String error = ExternalExecutableValidator.validateCommand(executable, executableName);
        if (error != null) {
            if (warnUser) {
                ExternalExecutableUserWarning euw = Lookup.getDefault().lookup(ExternalExecutableUserWarning.class);
                if (euw == null) {
                    LOGGER.info("No implementation of "+ExternalExecutableUserWarning.class);
                } else {
                    euw.displayError(error, optionsPath);
                }
            }
            return null;
        }
        ProcessBuilder processBuilder = getProcessBuilder();
        executionDescriptor = getExecutionDescriptor(executionDescriptor, outProcessorFactory);
        return ExecutionService.newService(processBuilder, executionDescriptor, getDisplayName()).run();
    }

    private ProcessBuilder getProcessBuilder() {
        ProcessBuilder processBuilder = createProcessBuilder();
        List<String> arguments = new ArrayList<>();
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
        return processBuilder;
    }

    private ProcessBuilder createProcessBuilder() {
        fullCommand.clear();
        fullCommand.add(executable);
        ProcessBuilder processBuilder = ProcessBuilder.getLocal();
        processBuilder.setExecutable(executable);
        return processBuilder;
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
        // output
        ExecutionDescriptor.InputProcessorFactory2 outputOutProcessorFactory = getOutputProcessorFactory();
        if (outputOutProcessorFactory != null) {
            inputProcessors.add(outputOutProcessorFactory);
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
                return new InfoInputProcessor(defaultProcessor, fullCommand);
            }
        };
    }

    private ExecutionDescriptor.InputProcessorFactory2 getOutputProcessorFactory() {
        if (noOutput) {
            // no output
            return null;
        }
        return new ExecutionDescriptor.InputProcessorFactory2() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return defaultProcessor;
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
                return new RedirectOutputProcessor(fileOutput);
            }
        };
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
            return implode(escapedCommand, " "); // NOI18N
        }

        private static String colorize(String msg) {
            return "\033[1;30m" + msg + "\033[0m"; // NOI18N
        }

        private static boolean isNewLine(char ch) {
            return ch == '\n' || ch == '\r' || ch == '\u0000'; // NOI18N
        }

        private static String implode(List<String> items, String delimiter) {
            if (items.isEmpty()) {
                return ""; // NOI18N
            }

            StringBuilder buffer = new StringBuilder(200);
            boolean first = true;
            for (String s : items) {
                if (!first) {
                    buffer.append(delimiter);
                }
                buffer.append(s);
                first = false;
            }
            return buffer.toString();
        }

    }

    static final class RedirectOutputProcessor implements InputProcessor {

        private final File fileOuput;

        private OutputStream outputStream;


        public RedirectOutputProcessor(File fileOuput) {
            this.fileOuput = fileOuput;
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            if (outputStream == null) {
                outputStream = new BufferedOutputStream(new FileOutputStream(fileOuput));
            }
            for (char c : chars) {
                outputStream.write((byte) c);
            }
        }

        @Override
        public void reset() {
            // noop
        }

        @Override
        public void close() throws IOException {
            outputStream.close();
        }

    }

}
