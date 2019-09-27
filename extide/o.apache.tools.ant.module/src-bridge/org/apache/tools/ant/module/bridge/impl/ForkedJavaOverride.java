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

package org.apache.tools.ant.module.bridge.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
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

            private Thread outTask;
            private Thread errTask;
            private Thread inTask;
            private Copier outCopier, errCopier; // #212526
            private FoldingHelper foldingHelper;

            NbOutputStreamHandler() {
                this.foldingHelper = new FoldingHelper();
            }

            public void start() throws IOException {}

            public void stop() {
                if (errTask != null) {
                    try {
                        errTask.join(3000);
                    } catch (InterruptedException ex) {
                    }
                }
                if (outTask != null) {
                    try {
                        outTask.join(3000);
                    } catch (InterruptedException ex) {
                    }
                }
                if (inTask != null) {
                    inTask.interrupt();
                    try {
                        inTask.join(1000);
                    } catch (InterruptedException ex) {
                    }
                }
                if (outCopier != null) {
                    outCopier.maybeFlush();
                }
                if (errCopier != null) {
                    errCopier.maybeFlush();
                }
            }

            public void setProcessOutputStream(InputStream inputStream) throws IOException {
                OutputStream os = getOutputStream();
                Integer logLevel = null;
                if (os == null || delegateOutputStream) {
                    os = AntBridge.delegateOutputStream(false);
                    logLevel = Project.MSG_INFO;
                }
                outTask = new Thread(Thread.currentThread().getThreadGroup(), outCopier = new Copier(inputStream, os, logLevel, outEncoding, foldingHelper),
                        "Out Thread for " + getProject().getName()); // NOI18N
                outTask.setDaemon(true);
                outTask.start();
            }

            public void setProcessErrorStream(InputStream inputStream) throws IOException {
                OutputStream os = getErrorStream();
                Integer logLevel = null;
                if (os == null || delegateErrorStream) {
                    os = AntBridge.delegateOutputStream(true);
                    logLevel = Project.MSG_WARN;
                }
                errTask = new Thread(Thread.currentThread().getThreadGroup(), errCopier = new Copier(inputStream, os, logLevel, errEncoding, foldingHelper),
                        "Err Thread for " + getProject().getName()); // NOI18N
                errTask.setDaemon(true);
                errTask.start();
            }

            public void setProcessInputStream(OutputStream outputStream) throws IOException {
                InputStream is = getInputStream();
                if (is == null) {
                    is = AntBridge.delegateInputStream();
                }
                inTask = new Thread(Thread.currentThread().getThreadGroup(), new Copier(is, outputStream, null, null, foldingHelper),
                        "In Thread for " + getProject().getName()); // NOI18N
                inTask.setDaemon(true);
                inTask.start();
            }

        }

    }

    private class Copier implements Runnable {

        private final InputStream in;
        private final OutputStream out;
        private final Integer logLevel;
        private final String encoding;
        private final RequestProcessor.Task flusher;
        private final ByteArrayOutputStream currentLine;
        private OutputWriter ow = null;
        private boolean err;
        private AntSession session = null;
        private final FoldingHelper foldingHelper;

        public Copier(InputStream in, OutputStream out, Integer logLevel, String encoding/*, long init*/,
                FoldingHelper foldingHelper) {
            this.in = in;
            this.out = out;
            this.logLevel = logLevel;
            this.encoding = encoding;
            this.foldingHelper = foldingHelper;
            if (logLevel != null) {
                flusher = PROCESSOR.create(new Runnable() {
                    public void run() {
                        maybeFlush();
                    }
                });
                currentLine = new ByteArrayOutputStream();
            } else {
                flusher = null;
                currentLine = null;
            }
        }

        public void run() {
            /*
            StringBuilder content = new StringBuilder();
            long tick = System.currentTimeMillis();
            content.append(String.format("[init: %1.1fsec]", (tick - init) / 1000.0));
             */
            
            if (ow == null && logLevel != null) {
                Vector v = getProject().getBuildListeners();
                for (Object o : v) {
                    if (o instanceof NbBuildLogger) {
                        NbBuildLogger l = (NbBuildLogger) o;
                        err = logLevel != Project.MSG_INFO;
                        ow = err ? l.err : l.out;
                        session = l.thisSession;
                        break;
                    }
                }
            }
            try {
                try {
                    int c;
                    while ((c = in.read()) != -1) {
                        if (logLevel == null) {
                            // Input gets sent immediately.
                            out.write(c);
                            out.flush();
                        } else {
                            synchronized (this) {
                                if (c == '\n') {                                    
                                    String str = currentLine.toString(encoding);
                                    int len = str.length();
                                    if (len > 0 && str.charAt(len - 1) == '\r') {
                                        str = str.substring(0, len - 1);
                                    }

                                    foldingHelper.checkFolds(str, err, session);
                                    if (str.length() < LOGGER_MAX_LINE_LENGTH) { // not too long message, probably interesting
                                        // skip stack traces (hyperlinks are created by JavaAntLogger), everything else write directly
                                        if (!STACK_TRACE.matcher(str).find()) {
                                            StandardLogger.findHyperlink(str, session, null).println(session, err);
                                        }
                                    } else {
                                        // do not match long strings, directly create a trivial hyperlink
                                        StandardLogger.findHyperlink(str, session, null).println(session, err);
                                    }
                                    log(str, logLevel);
                                    currentLine.reset();
                                } else {                                    
                                    currentLine.write(c);
                                    if(currentLine.size() > 8192) {
                                        flusher.run();
                                    } else {
                                        flusher.schedule(250);
                                    }
                                }
                            }    
                        }
                    }
                } finally {
                    if (logLevel != null) {
                        maybeFlush();
                        if (err) {
                            foldingHelper.clearHandle();
                        }
                    }
                }
            } catch (IOException x) {
                // ignore IOException: Broken pipe from FileOutputStream.writeBytes in BufferedOutputStream.flush
            } catch (ThreadDeath d) {
                // OK, build just stopped.
                return;
            }
            //System.err.println("copied " + in + " to " + out + "; content='" + content + "'");
        }

        private synchronized void maybeFlush() {
            if (ow == null) { // ?? #200365
                return;
            }
            try {
                if (currentLine.size() > 0) {
                    String str = currentLine.toString(encoding);
                    ow.write(str);
                    log(str, logLevel);
                }
            } catch (IOException x) {
                // probably safe to ignore
            } catch (ThreadDeath d) {
                // OK, build just stopped.
            }
            currentLine.reset();
        }

    }

    /**
     * A helper class for detecting stacktraces in the output and for creating
     * folds for them. It is also used as a shared lock for {@link Copier}s of
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
