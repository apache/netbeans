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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ak119685
 */
@SuppressWarnings("deprecation") // API
public final class ShellScriptRunner {

    private static final Logger log = org.netbeans.modules.nativeexecution.support.Logger.getInstance();
    private Charset scriptCS;
    private Charset outputCS;
    private final ExecutionEnvironment env;
    private final String script;
    private final URI scriptURI;
    private final LineProcessor outputProcessor;
    private LineProcessor errorProcessor;
    private CountDownLatch countdown;
    private NativeProcess shellProcess;
    private final List<String> args = new ArrayList<>();

    public ShellScriptRunner(ExecutionEnvironment env, String script, LineProcessor outputProcessor) {
        this.env = env;
        this.script = script;
        this.scriptURI = null;
        this.outputProcessor = outputProcessor;
    }

    public ShellScriptRunner(ExecutionEnvironment env, URI scriptURI, LineProcessor outputProcessor) {
        this.env = env;
        this.script = null;
        this.scriptURI = scriptURI;
        this.outputProcessor = outputProcessor;
    }

    public void setScriptCharset(Charset scriptCS) {
        this.scriptCS = scriptCS;
    }

    public void setOutputCharset(Charset outputCS) {
        this.outputCS = outputCS;
    }

    public void setErrorProcessor(LineProcessor errorProcessor) {
        this.errorProcessor = errorProcessor;
    }

    public synchronized int execute() throws IOException, CancellationException {
        if (scriptURI == null && script == null) {
            return 0;
        }

        HostInfo info = HostInfoUtils.getHostInfo(env);

        if (info == null || info.getShell() == null) {
            throw new IOException("Unable to get shell for " + env.getDisplayName()); // NOI18N
        }

        if (scriptCS == null) {
            if (env.isLocal()) {
                if (info.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                    scriptCS = WindowsSupport.getInstance().getShellCharset();
                } else {
                    scriptCS = Charset.defaultCharset(); // (UTF-8 ??)
                }
            } else {
                scriptCS = Charset.forName(ProcessUtils.getRemoteCharSet());
            }
        }

        if (outputCS == null) {
            outputCS = scriptCS;
        }

        NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(env);
        List<String> finalArgs = new ArrayList<>();
        finalArgs.add("-s"); // NOI18N
        if (!args.isEmpty()) {
            finalArgs.add("--"); // NOI18N
            finalArgs.addAll(args);
        }
        pb.setExecutable(info.getShell()).setArguments(finalArgs.<String>toArray(new String[finalArgs.size()]));

        shellProcess = pb.call();

        if (shellProcess.getState() == State.ERROR) {
            ProcessUtils.readProcessError(shellProcess); // just in case
            throw new IOException("Cannot start " + info.getShell() + " -s"); // NOI18N
        }

        RequestProcessor rp = new RequestProcessor("Shell runner", 3); // NOI18N
        countdown = new CountDownLatch(3);

        Callable<Integer> scriptWriter = () -> {
            try (BufferedWriter scriptWriter1 = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream(), scriptCS))) {
                if (script != null) {
                    scriptWriter1.write(script);
                    scriptWriter1.write('\n');
                } else {
                    try (BufferedReader scriptReader = new BufferedReader(new InputStreamReader(scriptURI.toURL().openStream(), scriptCS))) {
                        for(    String scriptLine = scriptReader.readLine();
                                scriptLine != null;
                                scriptLine = scriptReader.readLine()) {
                            scriptWriter1.write(scriptLine);
                            scriptWriter1.write('\n');
                        }
                    }
                }
                scriptWriter1.flush();
            } finally {
                countdown.countDown();
            }
            return 0;
        };

        ProcessOutputReader outReader = new ProcessOutputReader(shellProcess.getInputStream(), outputProcessor);
        ProcessOutputReader errReader = new ProcessOutputReader(shellProcess.getErrorStream(), errorProcessor);

        final Future<Integer> writerTask = rp.submit(scriptWriter);
        final Future<Integer> outReaderTask = rp.submit(outReader);
        final Future<Integer> errReaderTask = rp.submit(errReader);

        boolean interrupted = false;

        try {
            countdown.await();
        } catch (InterruptedException ex) {
            interrupted = true;
        }

        IOException exception = null;

        try {
            writerTask.get();
        } catch (InterruptedException ex) {
            interrupted = true;
        } catch (ExecutionException ex) {
            exception = new IOException(ex.getCause());
        }

        try {
            outReaderTask.get();
        } catch (InterruptedException ex) {
            interrupted = true;
        } catch (ExecutionException ex) {
            exception = new IOException(ex.getCause());
        }

        try {
            errReaderTask.get();
        } catch (InterruptedException ex) {
            interrupted = true;
        } catch (ExecutionException ex) {
            exception = new IOException(ex.getCause());
        }

        rp.shutdown();

        if (exception != null) {
            throw exception;
        }

        if (interrupted) {
            log.info("shell running thread interrupted"); // NOI18N
        }

        int result = 1;

        try {
            result = shellProcess.waitFor();
        } catch (InterruptedException ex) {
            log.info("shell running thread interrupted"); // NOI18N
        }

        return result;
    }

    public void setArguments(String... args) {
        this.args.clear();
        this.args.addAll(Arrays.asList(args));
    }

    private class ProcessOutputReader implements Callable<Integer> {

        private final LineProcessor lineProcessor;
        private final InputStream in;

        ProcessOutputReader(InputStream in, LineProcessor lineProcessor) {
            this.in = in;
            this.lineProcessor = lineProcessor;
        }

        @Override
        public Integer call() throws Exception {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (lineProcessor != null) {
                        lineProcessor.processLine(line);
                    }
                }
            } finally {
                if (lineProcessor != null) {
                    lineProcessor.close();
                }
                countdown.countDown();
            }

            return 0;
        }
    }

    public static final class LoggerLineProcessor implements LineProcessor {

        private final String prefix;

        public LoggerLineProcessor(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void processLine(String line) {
            log.log(Level.FINE, "{0}: {1}", new Object[]{prefix, line}); // NOI18N
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }
    }

    public static final class BufferedLineProcessor implements LineProcessor {

        private final List<String> buffer = new ArrayList<>();

        @Override
        public void processLine(String line) {
            buffer.add(line);
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }

        public List<String> getBuffer() {
            return Collections.unmodifiableList(buffer);
        }
        
        public String getAsString() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < buffer.size(); i++) {
                builder.append(buffer.get(i));
                //do not append /n at the last line
                if (i < buffer.size() - 1) {
                    builder.append('\n');
                }
            }
            return builder.toString();
        }
    }
}
