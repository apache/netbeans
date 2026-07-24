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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.nativeexecution.NbRemoteNativeProcess;
import org.netbeans.modules.nativeexecution.RemoteNativeProcess;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.support.MiscUtils;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

public final class ProcessUtils {

    private static final RequestProcessor RP = new RequestProcessor("ProcessUtils", 1); // NOI18N
    private static final String REMOTE_CHAR_SET = System.getProperty("cnd.remote.charset", "UTF-8"); // NOI18N

    private ProcessUtils() {
    }

    public static String getRemoteCharSet() {
        return REMOTE_CHAR_SET;
    }

    public static boolean isAlive(Process p) {
        if (p == null) {
            return false;
        } else if (p instanceof RemoteNativeProcess rnp) {
            return rnp.isAlive();
        } else if (p instanceof NbRemoteNativeProcess rnp) {
            return rnp.isAlive();
        } else {
            try {
                p.exitValue();
                return false;
            } catch (IllegalThreadStateException x) {
                return true;
            }
        }
    }

    public static BufferedReader getReader(final InputStream is, boolean remote) {
        if (remote) {
            // set charset
            try {
                return new BufferedReader(new InputStreamReader(is, getRemoteCharSet()));
            } catch (UnsupportedEncodingException ex) {
                String msg = getRemoteCharSet() + " encoding is not supported, try to override it with cnd.remote.charset"; //NOI18N
                Exceptions.printStackTrace(new IllegalStateException(msg, ex));
            }
        }
        return new BufferedReader(new InputStreamReader(is));
    }

    public static PrintWriter getWriter(final OutputStream os, boolean remote) {
        if (remote) {
            // set charset
            try {
                return new PrintWriter(new OutputStreamWriter(os, getRemoteCharSet()));
            } catch (UnsupportedEncodingException ex) {
                String msg = getRemoteCharSet() + " encoding is not supported, try to override it with cnd.remote.charset"; //NOI18N
                Exceptions.printStackTrace(new IllegalStateException(msg, ex));
            }
        }
        return new PrintWriter(os);
    }

    public static List<String> readProcessError(final Process p) throws IOException {
        if (p == null) {
            return Collections.<String>emptyList();
        }

        return readProcessStreamImpl(p.getErrorStream(), isRemote(p));
    }

    public static String readProcessErrorLine(final Process p) throws IOException {
        if (p == null) {
            return ""; // NOI18N
        }

        return readProcessStreamLine(p.getErrorStream(), isRemote(p));
    }

    public static List<String> readProcessOutput(final Process p) throws IOException {
        if (p == null) {
            return Collections.<String>emptyList();
        }

        return readProcessStreamImpl(p.getInputStream(), isRemote(p));
    }

    public static String readProcessOutputLine(final Process p) throws IOException {
        if (p == null) {
            return ""; // NOI18N
        }

        return readProcessStreamLine(p.getInputStream(), isRemote(p));
    }

    private static boolean isRemote(Process process) {
        if (process instanceof NativeProcess nativeProcess) {
            return nativeProcess.getExecutionEnvironment().isRemote();
        }
        return false;
    }

    public static void logError(final Level logLevel, final Logger log, final ExitStatus exitStatus) throws IOException {
        if (log != null && log.isLoggable(logLevel)) {
            logErrorImpl(logLevel, log, exitStatus.getErrorLines());
        }
    }

    public static void logError(final Level logLevel, final Logger log, final Process p) throws IOException {
        if (log != null && log.isLoggable(logLevel)) {
            logErrorImpl(logLevel, log, readProcessError(p));
        } else {
            readAndIgnoreProcessStream(p.getErrorStream());
        }
    }

    private static void logErrorImpl(final Level logLevel, final Logger log, List<String> err) throws IOException {
        for (String line : err) {
            log.log(logLevel, "ERROR: {0}", line); // NOI18N
        }
    }

    /**
     * Reads process stream asynchronously. As soon as all stream is read (i. e. process is finished),
     * all lines are added to listToAdd.
     *
     * @param process
     * @param lineProcessor
     */
    @SuppressWarnings("deprecation") // org.netbeans.api.extexecution.input.LineProcessor is API
    public static void readProcessOutputAsync(final Process process, final LineProcessor lineProcessor) {
        NativeTaskExecutorService.submit(() -> {
            try {
                readProcessStreamImpl(process.getInputStream(), isRemote(process), lineProcessor);
            } catch (IOException ex) {
                // nothing
            }
        }, "reading process output"); // NOI18N
    }

    public static Future<List<String>> readProcessOutputAsync(final Process process) {
        return NativeTaskExecutorService.submit(() -> {
            return readProcessStreamImpl(process.getInputStream(), isRemote(process));
        }, "reading process output"); // NOI18N
    }

    @SuppressWarnings("deprecation") // org.netbeans.api.extexecution.input.LineProcessor is API
    public static void readProcessErrorAsync(final Process process,  final LineProcessor lineProcessor) {
        NativeTaskExecutorService.submit(() -> {
            try {
                readProcessStreamImpl(process.getErrorStream(), isRemote(process), lineProcessor);
            } catch (IOException ex) {
                // nothing
            }
        }, "reading process error"); // NOI18N
    }

    public static Future<List<String>> readProcessErrorAsync(final Process process) {
        return NativeTaskExecutorService.submit(() -> {
            return readProcessStreamImpl(process.getErrorStream(), isRemote(process));
        }, "reading process error"); // NOI18N
    }

    private static void readAndIgnoreProcessStream(final InputStream stream) throws IOException {
        if (stream != null) {
            // remote or local does not matter when ignoring
            final BufferedReader br = getReader(stream, true);
            try {
                while (br.readLine() != null) {
                    // nothing
                }
            } finally {
                if (br != null) {
                    br.close();
                }
            }
        }
    }

    private static List<String> readProcessStreamImpl(final InputStream stream, boolean remoteStream) throws IOException {
        if (stream == null) {
            return Collections.<String>emptyList();
        }

        final List<String> result = new LinkedList<>();
        final BufferedReader br = getReader(stream, remoteStream);

        try {
            for(String line = br.readLine(); line != null; line = br.readLine()) {
                result.add(line);
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return result;
    }

    private static void readProcessStreamImpl(final InputStream stream, boolean remoteStream,  @SuppressWarnings("deprecation") final LineProcessor lineProcessor) throws IOException {
        if (stream == null) {
            try (lineProcessor) {
                lineProcessor.reset();
            }
        } else {
            lineProcessor.reset();
            try (lineProcessor;
                    BufferedReader br = getReader(stream, remoteStream)) {
                for(String line = br.readLine(); line != null; line = br.readLine()) {
                    lineProcessor.processLine(line);
                }
            }
        }
    }


    @SuppressWarnings("NestedAssignment")
    private static String readProcessStreamLine(final InputStream stream, boolean remoteStream) throws IOException {
        if (stream == null) {
            return ""; // NOI18N
        }

        final StringBuilder result = new StringBuilder();
        final BufferedReader br = getReader(stream, remoteStream);

        try {
            boolean first = true;

            for(String line = br.readLine(); line != null; line = br.readLine()) {
                if (!first) {
                    result.append('\n');
                }
                result.append(line);
                first = false;
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return result.toString();
    }

    /**
     * Just a conveniency shortcut for calling both methods
     * ignoreProcessOutput()
     * ignoreProcessError()
     * @param <T>
     * @param p
     * @return
     */
    public static <T extends Process> T  ignoreProcessOutputAndError(final T p) {
        ignoreProcessOutput(p);
        ignoreProcessError(p);
        return p;
    }

    /**
     * Even if you ignore process error stream, it should be read,
     * otherwise other processes can hang - this is a jsch related issue.
     * This method reads and ignores process error stream
     * @param <T>
     * @param p process
     * @return
     */
    public static <T extends Process> T ignoreProcessError(final T p) {
        if (p != null) {
            NativeTaskExecutorService.submit(() -> {
                try {
                    readAndIgnoreProcessStream(p.getErrorStream());
                } catch (IOException ex) {
                }
            }, "Reading process error " + p); // NOI18N
        }
        return p;
    }

    /**
     * Even if you ignore process output stream, it should be read,
     * otherwise other processes can hang - this is a jsch related issue.
     * This method reads and ignores process output stream
     * @param <T>
     * @param p process
     * @return
     */
    public static <T extends Process> T ignoreProcessOutput(final T p) {
        if (p != null) {
            NativeTaskExecutorService.submit(() -> {
                try {
                    readAndIgnoreProcessStream(p.getInputStream());
                } catch (IOException ex) {
                }
            }, "Reading process output " + p); // NOI18N
        }
        return p;
    }

    @SuppressWarnings("deprecation") // org.netbeans.api.extexecution.input.LineProcessor is API
    public static void writeError(final Writer error, Process p) throws IOException {
        class MyLineProcessor implements LineProcessor {
            private IOException writeEx = null;
            @Override
            public void processLine(String line) {
                if (writeEx == null) {
                    try {
                        error.write(line);
                    } catch (IOException ex) {
                        writeEx = ex;
                    }
                }
            }

            @Override
            public void reset() {}

            @Override
            public void close() {}
        }
        MyLineProcessor myLProcessor = new MyLineProcessor();
        readProcessStreamImpl(p.getErrorStream(), isRemote(p), myLProcessor);
        if (myLProcessor.writeEx != null) {
            throw myLProcessor.writeEx;
        }
    }

    /**
     * This method tries to destroy the process in two attempts. First attempt
     * is simply calling process' destroy() method. But in some cases this could
     * fail to terminate the process - so in case first attempt fails, send
     * SIGKILL to the process.
     *
     * @param process - process to terminate (not necessarily NativeProcess)
     */
    public static void destroy(Process process) {
        // First attempt is just call destroy() on the process
        process.destroy();

        // But in case the process is in system call (sleep, read, for example)
        // this will not have a desired effect - in this case
        // will send SIGTERM signal..

        try {
            process.exitValue();
            // No exception means successful termination
            return;
        } catch (java.lang.IllegalThreadStateException ex) {
        }

        ExecutionEnvironment execEnv;

        if (process instanceof NativeProcess nativeProcess) {
            execEnv = nativeProcess.getExecutionEnvironment();
        } else {
            execEnv = ExecutionEnvironmentFactory.getLocal();
        }

        int pid = getPID(process);

        if (pid > 0) {
            try {
                CommonTasksSupport.sendSignal(execEnv, pid, Signal.SIGKILL, null).get();
            } catch (InterruptedException | ExecutionException ex) {
            }
        }
    }

    private static int getPID(Process process) {
        int pid = -1;

        try {
            if (process instanceof NativeProcess nativeProcess) {
                pid = nativeProcess.getPID();
            } else if (process != null) {
                String className = process.getClass().getName();
                // TODO: windows?...
                if ("java.lang.UNIXProcess".equals(className)) { // NOI18N
                    Field f = process.getClass().getDeclaredField("pid"); // NOI18N
                    f.setAccessible(true);
                    pid = f.getInt(process);
                }
            }
        } catch (Throwable e) {
            org.netbeans.modules.nativeexecution.support.Logger.getInstance().log(Level.FINE, e.getMessage(), e);
        }

        return pid;
    }

    /**
     * Starts executable and returns immediately.
     * @param execEnv - target execution environment
     * @param rp - RequestProcessor that is used for running the task.
     * Could be NULL. In this case default (private) processor is used.
     * Note that default (private) processor has throughput == 1
     * @param postExecutor - once process is done, passed postExecutor will be
     * notified. Call of postExecutor's method is performed in the same thread
     * as invocation of the executable (i.e. in rp (see above)).
     * @param executable - full path to executable to run.
     * @param args - list of arguments to pass to executable
     * @return Future ExitStatus
     */
    public static Future<ExitStatus> execute(final ExecutionEnvironment execEnv, final RequestProcessor rp, final PostExecutor postExecutor, final String executable, final String... args) {
        final RequestProcessor processor = (rp == null) ? RP : rp;
        return processor.submit(() -> {
            ExitStatus status = null;
            String error = null;

            try {
                status = execute(execEnv, executable, args);
            } catch (Throwable t) {
                error = t.getMessage();
            } finally {
                if (postExecutor != null) {
                    postExecutor.processFinished(error == null ? status : new ExitStatus(1, null, Arrays.asList(error.split("\n")))); // NOI18N
                }
            }

            return status;
        });
    }

    public static ExitStatus execute(final ExecutionEnvironment execEnv, final String executable, final String... args) {
        return execute(NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable(executable).setArguments(args));
    }

    public static ExitStatus executeInDir(final String workingDir, final ExecutionEnvironment execEnv, final String executable, final String... args) {
        return execute(NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable(executable).setArguments(args).setWorkingDirectory(workingDir));
    }

    public static ExitStatus executeWithoutMacroExpansion(final String workingDir, final ExecutionEnvironment execEnv, final String executable, final String... args) {
        if (workingDir != null) {
            return execute(NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable(executable).setArguments(args).setMacroExpansion(false));
        } else {
            return execute(NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable(executable).setArguments(args).setWorkingDirectory(workingDir).setMacroExpansion(false));
        }
    }

    /**
     * This method can be used to start a process without additional handling
     * of exceptions/streams reading, etc.
     *
     * Usage pattern:
     *        ExitStatus status = ProcessUtils.execute(
     *            NativeProcessBuilder.newProcessBuilder(execEnv).
     *            setExecutable("/bin/ls").setArguments("/home"));
     *
     *        if (status.isOK()) {
     *            do something...
     *        } else {
     *            System.out.println("Error! " + status.error);
     *        }
     *
     * This method WILL modify passed ProcessBuilder:
     *   - X11 forwarding will be switched off
     *   - initial suspend will be switched off
     *   - unbuffering will be switched off
     *   - usage of external terminal will be switched off
     *
     * @param processBuilder
     * @since 1.13.0
     * @return
     */
    public static ExitStatus execute(final NativeProcessBuilder processBuilder) {
        return execute(processBuilder, null);
    }

    public static ExitStatus execute(final NativeProcessBuilder processBuilder, byte[] input) {
        ExitStatus result;
        Future<List<String>> error;
        Future<List<String>> output;

        if (processBuilder == null) {
            throw new NullPointerException("NULL process builder!"); // NOI18N
        }

        processBuilder.setX11Forwarding(false);
        processBuilder.setInitialSuspend(false);
        processBuilder.unbufferOutput(false);
        processBuilder.useExternalTerminal(null);

        try {
            final Process process = processBuilder.call();
            if (processBuilder.redirectErrorStream()) {
                error = null;
            } else {
                error = NativeTaskExecutorService.submit(() -> {
                    return readProcessError(process);
                }, "e"); // NOI18N
            }
            output = NativeTaskExecutorService.submit(() -> {
                return readProcessOutput(process);
            }, "o"); // NOI18N
            if (input != null && input.length > 0) {
                process.getOutputStream().write(input);
                process.getOutputStream().close();
            }
            result = new ExitStatus(process.waitFor(), output.get(), (error == null) ? null : error.get());
        } catch (InterruptedException ex) {
            result = new ExitStatus(-100, null, MiscUtils.getMessageAsList(ex));
        } catch (Throwable th) {
            org.netbeans.modules.nativeexecution.support.Logger.getInstance().log(Level.INFO, th.getMessage(), th);
            result = new ExitStatus(-200, null, MiscUtils.getMessageAsList(th));
        }

        return result;
    }

    /**
     * This method can be used to start a process without additional handling
     * of exceptions/streams reading, etc.
     *
     * Usage pattern:
     *        ExitStatus status = ProcessUtils.execute(
     *            new ProcessBuilder("/bin/ls"));
     *
     *        if (status.isOK()) {
     *            do something...
     *        } else {
     *            System.out.println("Error! " + status.error);
     *        }
     *
     * @param processBuilder
     * @since 1.13.0
     * @return ExitStatus
     */
    public static ExitStatus execute(final ProcessBuilder processBuilder) {
        return execute(processBuilder, null);
    }

    public static ExitStatus execute(final ProcessBuilder processBuilder, byte[] input) {
        ExitStatus result;
        Future<List<String>> error;
        Future<List<String>> output;

        if (processBuilder == null) {
            throw new NullPointerException("NULL process builder!"); // NOI18N
        }

        try {
            final Process process = processBuilder.start();
            if (processBuilder.redirectErrorStream() || processBuilder.redirectError() != ProcessBuilder.Redirect.PIPE) {
                error = null;
            } else {
                error = NativeTaskExecutorService.submit(() -> {
                    return readProcessError(process);
                }, "e"); // NOI18N
            }
            output = NativeTaskExecutorService.submit(() -> {
                return readProcessOutput(process);
            }, "o"); // NOI18N
            if (input != null && input.length > 0) {
                process.getOutputStream().write(input);
                process.getOutputStream().close();
            }
            result = new ExitStatus(process.waitFor(), output.get(), (error == null) ? null : error.get());
        } catch (InterruptedException ex) {
            result = new ExitStatus(-100, null, MiscUtils.getMessageAsList(ex));
        } catch (Throwable th) {
            org.netbeans.modules.nativeexecution.support.Logger.getInstance().log(Level.INFO, th.getMessage(), th);
            result = new ExitStatus(-200, null, MiscUtils.getMessageAsList(th));
        }

        return result;
    }

    public static final class ExitStatus {

        public final int exitCode;

        private final String error;

        private final String output;

        private final List<String> outputLines;
        private final List<String> errorLines;

        public ExitStatus(int exitCode, List<String> outputLines, List<String> errorLines) {
            this.exitCode = exitCode;
            this.outputLines = (outputLines == null) ?
                    Collections.<String>emptyList() : Collections.unmodifiableList(outputLines);
            this.output = (outputLines == null || outputLines.isEmpty()) ?
                    "" : merge(outputLines); //NOI18N
            this.errorLines = (errorLines == null) ?
                    Collections.<String>emptyList() : Collections.unmodifiableList(errorLines);
            this.error = (errorLines == null || errorLines.isEmpty()) ?
                    "" : merge(errorLines); //NOI18N
        }

        private String merge(List<String> outputLines) {
            StringBuilder sb = new StringBuilder();
            if (outputLines != null) {
                for (String line : outputLines) {
                    if (sb.length() > 0) {
                        sb.append('\n'); //NOI18N
                    }
                    sb.append(line);
                }
            }
            return sb.toString();
        }

        public boolean isOK() {
            return exitCode == 0;
        }

        @Override
        public String toString() {
            return "ExitStatus " + "exitCode=" + exitCode + "\nerror=" + getErrorString() + "\noutput=" + getOutputString(); // NOI18N
        }

        /**
         * This method may be ineffective. Consider using getOutputLines()
         *
         * @return
         */
        public String getOutputString() {
            return output;
        }

        public List<String> getOutputLines() {
            return outputLines;
        }

        /**
         * This method may be ineffective. Consider using getErrorLines()
         *
         * @return
         */
        public String getErrorString() {
            return error;
        }

        public List<String> getErrorLines() {
            return errorLines;
        }
    }

    public static interface PostExecutor {

        public void processFinished(ExitStatus status);
    }
}
