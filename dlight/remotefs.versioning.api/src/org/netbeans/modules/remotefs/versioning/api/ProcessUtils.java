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
package org.netbeans.modules.remotefs.versioning.api;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.util.Cancellable;

/**
 *
 */
public class ProcessUtils  {

    public static final Logger LOG = Logger.getLogger("nativeexecution.support.logger"); // NOI18N
    
    private ProcessUtils() {
    }

    public static ExitStatus executeInDir(final String workingDir, final Map<String,String> env, boolean binaryOutput, Canceler canceler,
            final VCSFileProxy fileToBuilder, final String executable, final String... args) {
        ProcessBuilder builder = VersioningSupport.createProcessBuilder(fileToBuilder);
        if (workingDir != null) {
            builder.setWorkingDirectory(workingDir);
        }
        if (env != null) {
            builder.setEnvironmentVariables(env);
        }
        builder.setExecutable(executable);
        builder.setArguments(Arrays.asList(args));
        Charset encoding = RemoteVcsSupport.getEncoding(fileToBuilder);
        return execute(builder, binaryOutput, canceler, encoding);
    }

    public static final class Canceler implements Cancellable {
        private final List<Process> listeners = new ArrayList<>();
        private final AtomicBoolean canceled = new AtomicBoolean(false);
        public Canceler(){
        }
        @Override
        public boolean cancel() {
            canceled.set(true);
            for(Process listener : listeners) {
                listener.destroy();
            }
            return true;
        }
        public boolean canceled() {
            return canceled.get();
        }
        public void addListener(Process listener) {
            listeners.add(listener);
        }
    }
    
    private static ExitStatus execute(final ProcessBuilder processBuilder, boolean binaryOutput, Canceler canceler, final Charset encoding) {
        ExitStatus result;
        if (processBuilder == null) {
            throw new NullPointerException("NULL process builder!"); // NOI18N
        }
        try {
            final Process process = processBuilder.call();
            canceler.addListener(process);
            if (binaryOutput) {
                Future<String> error = NativeTaskExecutorService.submit(new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        return readProcessErrorLine(process, encoding);
                    }
                }, "e"); // NOI18N
                Future<byte[]> output = NativeTaskExecutorService.submit(new Callable<byte[]>() {

                    @Override
                    public byte[] call() throws Exception {
                        return readProcessOutputBytes(process);
                    }
                }, "o"); // NOI18N
                result = new ExitStatus(process.waitFor(), output.get(), error.get());
            } else {
                Future<String> error = NativeTaskExecutorService.submit(new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        return readProcessErrorLine(process, encoding);
                    }
                }, "e"); // NOI18N
                Future<String> output = NativeTaskExecutorService.submit(new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        return readProcessOutputLine(process, encoding);
                    }
                }, "o"); // NOI18N
                result = new ExitStatus(process.waitFor(), output.get(), error.get());
            }

        } catch (InterruptedException ex) {
            result = new ExitStatus(-100, "", ex.getMessage());
        } catch (Throwable th) {
            LOG.log(Level.INFO, th.getMessage(), th);
            result = new ExitStatus(-200, "", th.getMessage());
        }

        return result;
    }

    private static BufferedReader getReader(final InputStream is, Charset charSet) {
        // set charset
        return new BufferedReader(new InputStreamReader(is, charSet));
    }

    private static String readProcessErrorLine(final Process p, Charset charSet) throws IOException {
        if (p == null) {
            return ""; // NOI18N
        }

        return readProcessStreamLine(p.getErrorStream(), charSet);
    }

    private static String readProcessOutputLine(final Process p, Charset charSet) throws IOException {
        if (p == null) {
            return ""; // NOI18N
        }

        return readProcessStreamLine(p.getInputStream(), charSet);
    }

    private static byte[] readProcessOutputBytes(final Process p) throws IOException {
        if (p == null) {
            return new byte[0];
        }

        return readProcessStreamBytes(p.getInputStream());
    }

    private static String readProcessStreamLine(final InputStream stream, Charset charSet) throws IOException {
        if (stream == null) {
            return ""; // NOI18N
        }

        final StringBuilder result = new StringBuilder();
        final BufferedReader br = getReader(stream, charSet);

        try {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (!first) {
                    result.append('\n');
                }
                result.append(line);
                first = false;
            }
        } finally {
            br.close();
        }

        return result.toString();
    }
    
    private static byte[] readProcessStreamBytes(final InputStream stream) throws IOException {
        if (stream == null) {
            return new byte[0];
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try {
            int i;
            while((i = stream.read()) != -1) {
                b.write(i);
            }
        } finally {
            stream.close();
        }
        return b.toByteArray();
    }

    public static final class ExitStatus {

        public final int exitCode;
        public final String error;
        public final String output;
        public final byte[] bytes;

        private ExitStatus(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.error = error;
            this.output = output;
            bytes = null;
        }

        private ExitStatus(int exitCode, byte[] bytes, String error) {
            this.exitCode = exitCode;
            this.error = error;
            this.bytes = bytes;
            output = null;
        }

        public boolean isOK() {
            return exitCode == 0;
        }

        @Override
        public String toString() {
            return "ExitStatus " + "exitCode=" + exitCode + "\nerror=" + error + "\noutput=" + output; // NOI18N
        }
    }

    public static interface PostExecutor {

        public void processFinished(ExitStatus status);
    }
}
