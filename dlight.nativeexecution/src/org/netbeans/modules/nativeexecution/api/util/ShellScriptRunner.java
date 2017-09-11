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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
public final class ShellScriptRunner {

    private static final Logger log = org.netbeans.modules.nativeexecution.support.Logger.getInstance();
    private Charset scriptCS;
    private Charset outputCS;
    private final ExecutionEnvironment env;
    private final String script;
    private final URI scriptURI;
    private LineProcessor outputProcessor;
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

        Callable<Integer> scriptWriter = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                BufferedWriter scriptWriter = null;
                try {
                    scriptWriter = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream(), scriptCS));
                    if (script != null) {
                        scriptWriter.write(script);
                        scriptWriter.write('\n');
                    } else {
                        BufferedReader scriptReader = null;
                        try {
                            scriptReader = new BufferedReader(new InputStreamReader(scriptURI.toURL().openStream(), scriptCS));
                            String scriptLine;

                            while ((scriptLine = scriptReader.readLine()) != null) {
                                scriptWriter.write(scriptLine);
                                scriptWriter.write('\n');
                            }
                        } finally {
                            if (scriptReader != null) {
                                scriptReader.close();
                            }
                        }
                    }
                    scriptWriter.flush();
                } finally {
                    if (scriptWriter != null) {
                        scriptWriter.close();
                    }

                    countdown.countDown();
                }

                return 0;
            }
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
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (lineProcessor != null) {
                        lineProcessor.processLine(line);
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (lineProcessor != null) {
                    lineProcessor.close();
                }
                countdown.countDown();
            }

            return 0;
        }
    }

    public final static class LoggerLineProcessor implements LineProcessor {

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

    public final static class BufferedLineProcessor implements LineProcessor {

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
