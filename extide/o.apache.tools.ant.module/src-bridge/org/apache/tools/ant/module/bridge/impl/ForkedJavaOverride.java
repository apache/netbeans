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

package org.apache.tools.ant.module.bridge.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.run.StandardLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Redirector;
import org.openide.util.RequestProcessor;
import org.openide.windows.FoldHandle;
import org.openide.windows.IOFolding;
import org.openide.windows.OutputWriter;

/**
 * Replacement for Ant's java task which directly sends I/O to the output without line buffering.
 * Idea from ide/projectimport/bluej/antsrc/org/netbeans/bluej/ant/task/BlueJava.java.
 * See issue #56341.
 */
public class ForkedJavaOverride extends Java {

    private static final RequestProcessor PROCESSOR = new RequestProcessor(ForkedJavaOverride.class.getName(), Integer.MAX_VALUE);
    public static final int LOGGER_MAX_LINE_LENGTH = Integer.getInteger("logger.max.line.length", 3000); //NOI18N

    // should be consistent with java.project.JavaAntLogger.STACK_TRACE
    // should be consistent with org.netbeans.modules.java.j2seembedded.project.RemoteJavaAntLogger
    private static final String JIDENT = "[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*"; // NOI18N
    private static final Pattern STACK_TRACE = Pattern.compile(
            "(.*?((?:" + JIDENT + "[.])*)(" + JIDENT + ")[.](?:" + JIDENT + "|<init>|<clinit>)" + // NOI18N
            "[(])(((?:"+JIDENT+"(?:\\."+JIDENT+")*/)?" + JIDENT + "[.]java):([0-9]+)|Unknown Source)([)].*)"); // NOI18N

    public ForkedJavaOverride() {
        redirector = new NbRedirector(this);
        super.setFork(true);
    }

    @Override
    public void setFork(boolean fork) {
        // #47645: ignore! Does not work to be set to false.
    }

    private void useStandardRedirector() { // #121512, #168153
        if (redirector instanceof NbRedirector) {
            redirector = new Redirector(this);
        }
        getProject().setProperty(StandardLogger.USING_STANDARD_REDIRECTOR, "true");
    }
    public @Override void setInput(File input) {
        useStandardRedirector();
        super.setInput(input);
    }
    public @Override void setInputString(String inputString) {
        useStandardRedirector();
        super.setInputString(inputString);
    }
    public @Override void setOutput(File out) {
        useStandardRedirector();
        super.setOutput(out);
    }
    public @Override void setOutputproperty(String outputProp) {
        useStandardRedirector();
        super.setOutputproperty(outputProp);
    }
    public @Override void setError(File error) {
        useStandardRedirector();
        super.setError(error);
    }
    public @Override void setErrorProperty(String errorProperty) {
        useStandardRedirector();
        super.setErrorProperty(errorProperty);
    }

    private class NbRedirector extends Redirector {

        private String outEncoding = System.getProperty("file.encoding"); // NOI18N
        private String errEncoding = System.getProperty("file.encoding"); // NOI18N

        // #158492. In Ant 1.8.0 output redirection cannot be distinguished by OutputStream subclass type
        // (LogOutputStream vs OutputStreamFunneler$Funnel) as OutputStreamFunneler$Funnel is
        // used in both cases
        private boolean delegateOutputStream = true;
        private boolean delegateErrorStream = true;

        public NbRedirector(Task task) {
            super(task);
        }

        public @Override ExecuteStreamHandler createHandler() throws BuildException {
            createStreams();
            return new NbOutputStreamHandler();
        }

        public @Override synchronized void setOutputEncoding(String outputEncoding) {
            outEncoding = outputEncoding;
            super.setOutputEncoding(outputEncoding);
        }

        public @Override synchronized void setErrorEncoding(String errorEncoding) {
            errEncoding = errorEncoding;
            super.setErrorEncoding(errorEncoding);
        }

        @Override
        public void setOutput(File out) {
            if (out != null) {
                delegateOutputStream = false;
            }
            super.setOutput(out);
        }

        @Override
        public synchronized void setOutput(File[] out) {
            if (out != null && out.length > 0) {
                delegateOutputStream = false;
            }
            super.setOutput(out);
        }

        @Override
        public void setError(File error) {
            if (error != null) {
                delegateErrorStream = false;
            }
            super.setError(error);
        }

        @Override
        public synchronized void setError(File[] error) {
            if (error != null && error.length > 0) {
                delegateOutputStream = false;
            }
            super.setError(error);
        }

        private class NbOutputStreamHandler implements ExecuteStreamHandler {

            private final ExecutorService tasks;
            private final FoldingHelper foldingHelper;
            private Future inputTask;
            private InputStream stdout, stderr;
            private OutputStream stdin;
            
            NbOutputStreamHandler() {
                this.foldingHelper = new FoldingHelper();
                this.tasks = Executors.newFixedThreadPool(3, (r) -> {
                    Thread t = new Thread(Thread.currentThread().getThreadGroup(),
                        r,
                        "I/O Thread for " + getProject().getName()); // NOI18N
                    t.setDaemon(true);
                    return t;
                });
            }

            private void setCopier(InputStream inputStream, OutputStream os, boolean delegate, boolean err) {
                if (os == null || delegate) {
                    tasks.submit(new TransferCopier(inputStream, AntBridge.delegateOutputStream(err)));
                } else {
                    tasks.submit(new TransferCopier(inputStream, os));
                }
            }

            public void start() throws IOException {
                NbBuildLogger buildLogger = getProject().getBuildListeners().stream()
                    .filter(o -> o instanceof NbBuildLogger)
                    .map(o -> (NbBuildLogger)o)
                    .findFirst()
                    .orElse(null);
                if (buildLogger != null) {
                    tasks.submit(new MarkupCopier(stdout, Project.MSG_INFO, false, outEncoding, buildLogger, foldingHelper));
                    tasks.submit(new MarkupCopier(stderr, Project.MSG_WARN, true, errEncoding, buildLogger, foldingHelper));
                } else {
                    setCopier(stdout, getOutputStream(), delegateOutputStream, false);
                    setCopier(stderr, getErrorStream(), delegateErrorStream, true);
                }                                
                InputStream is = getInputStream();
                if (is == null)
                    is = AntBridge.delegateInputStream();
                inputTask = tasks.submit(new TransferCopier(is, stdin));
            }

            public void stop() {
                try {
                    if (inputTask != null)
                        inputTask.cancel(true);
                    tasks.shutdown();
                    tasks.awaitTermination(3, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                } finally {
                    tasks.shutdownNow();
                }
            }

            public void setProcessOutputStream(InputStream inputStream) throws IOException {
                this.stdout = inputStream;
            }

            public void setProcessErrorStream(InputStream inputStream) throws IOException {
                this.stderr = inputStream;
            }

            public void setProcessInputStream(OutputStream outputStream) throws IOException {
                this.stdin = outputStream;
            }

        }

    }

    /**
     * Simple copier that transfers all input to output.
     */
    private class TransferCopier implements Runnable {

        private final InputStream in;
        private final OutputStream out;

        public TransferCopier(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                byte[] data = new byte[1024];
                int len;
                while ((len = in.read(data)) >= 0) {
                    out.write(data, 0, len);
                    out.flush();
                }
            } catch (IOException  x) {
                // ignore IOException: Broken pipe from FileOutputStream.writeBytes in BufferedOutputStream.flush
            }
        }

    }

    /**
     * Filtering copier that marks up links, ignoring stack traces.
     */
    private class MarkupCopier implements Runnable {

        private final InputStream in;
        private final int logLevel;
        private final String encoding;
        private final RequestProcessor.Task flusher;
        private final ByteArrayOutputStream currentLine;
        private final OutputWriter ow;
        private final boolean err;
        private final AntSession session;
        private final FoldingHelper foldingHelper;

        public MarkupCopier(InputStream in, int logLevel, boolean err, String encoding, NbBuildLogger buildLogger, FoldingHelper foldingHelper) {
            this.in = in;
            this.logLevel = logLevel;
            this.err = err;
            this.encoding = encoding;
            this.foldingHelper = foldingHelper;

            flusher = PROCESSOR.create(() -> maybeFlush(false));
            currentLine = new ByteArrayOutputStream();

            ow = err ? buildLogger.err : buildLogger.out;
            session = buildLogger.thisSession;
        }

        private synchronized void append(byte[] data, int off, int len) {
            currentLine.write(data, off, len);
            if (currentLine.size() > 8192) {
                flusher.run();
            } else {
                flusher.schedule(250);
            }
        }

        private synchronized String appendAndTake(byte[] data, int off, int len) throws UnsupportedEncodingException {
            currentLine.write(data, off, len);
            String str = currentLine.toString(encoding);
            currentLine.reset();
            return str;
        }

        private synchronized String take() throws UnsupportedEncodingException {
            String str = currentLine.toString(encoding);
            currentLine.reset();
            return str;
        }

        public void run() {
            try {
                byte[] data = new byte[1024];
                int len;

                try {
                    while ((len = in.read(data)) >= 0) {
                        int last = 0;
                        for (int i = 0; i < len; i++) {
                            int c = data[i] & 0xff;

                            // Add folds for stack traces and mark up lines
                            // not processed by JavaAntLogger stack trace detection
                            if (c == '\n') {
                                String str = appendAndTake(data, last, i > last && data[i - 1] == '\r' ? i - last - 1 : i - last);

                                synchronized (foldingHelper) {
                                    foldingHelper.checkFolds(str, err, session);
                                    if (str.length() >= LOGGER_MAX_LINE_LENGTH || !STACK_TRACE.matcher(str).matches())
                                        StandardLogger.findHyperlink(str, session, null).println(session, err);
                                    log(str, logLevel);
                                }
                                last = i + 1;
                            }
                        }

                        if (last < len)
                            append(data, last, len - last);
                    }
                } finally {
                    maybeFlush(true);
                }
            } catch (IOException x) {
                // ignore IOException: Broken pipe from FileOutputStream.writeBytes in BufferedOutputStream.flush
            }
        }

        public void maybeFlush(boolean end) {
            try {
                String str = take();
                synchronized (foldingHelper) {
                    if (!str.isEmpty()) {
                        ow.write(str);
                        log(str, logLevel);
                    }
                    if (end && err)
                        foldingHelper.clearHandle();
                }
            } catch (IOException x) {
                // ignore IOException: Broken pipe from FileOutputStream.writeBytes in BufferedOutputStream.flush
            }
        }

    }

    /**
     * A helper class for detecting stacktraces in the output and for creating
     * folds for them. It is also used as a shared lock for {@link MarkupCopier}s of
     * standard and error outputs, which should make the mixed output a bit more
     * readable.
     */
    public static class FoldingHelper {

        private final Pattern STACK_TRACE = Pattern.compile(
                "^\\s+at.*:");                                          //NOI18N
        private final Pattern EXCEPTION = Pattern.compile(
                "^(\\.?\\w)*(Exception|Error).*");                      //NOI18N
        private FoldHandle foldHandle = null;
        boolean inStackTrace = false;

        private void checkFolds(String s, boolean error, AntSession session) {
            // ignore too long, expensive messages, probably coming from user, so no need for folds
            boolean cheap = s.length() < LOGGER_MAX_LINE_LENGTH;
            if (cheap && error && EXCEPTION.matcher(s).find()) {
                clearHandle();
                inStackTrace = true;
            } else if (cheap && error && inStackTrace
                    && STACK_TRACE.matcher(s).find()) {
                if (foldHandle == null) {
                    foldHandle = IOFolding.startFold(session.getIO(), true);
                }
            } else {
                inStackTrace = false;
                clearHandle();
            }
        }

        void clearHandle() {
            if (foldHandle != null) {
                if (!foldHandle.isFinished()) {
                    foldHandle.silentFinish();
                }
                foldHandle = null;
            }
        }
    }
}
