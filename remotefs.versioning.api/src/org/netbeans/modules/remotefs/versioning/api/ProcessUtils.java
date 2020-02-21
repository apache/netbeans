/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
