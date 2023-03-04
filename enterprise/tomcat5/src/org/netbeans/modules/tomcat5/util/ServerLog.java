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

/*
 * ServerLog.java
 *
 * Created on September 13, 2004, 7:13 PM
 */

package org.netbeans.modules.tomcat5.util;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.InputReaderTask;
import org.netbeans.api.extexecution.input.InputReaders;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.util.LogSupport.LineInfo;
import org.openide.util.Exceptions;

/**
 * Tomcat server log reads from the Tomcat standard and error output and 
 * writes to output window.
 */ 
class ServerLog {
    private final InputOutput io;
    private final OutputWriter writer;
    private final OutputWriter errorWriter;
    private final InputReaderTask inReader;
    private final InputReaderTask errReader;
    private final boolean autoFlush;
    private final boolean takeFocus;
    private final ServerLogSupport logSupport;
    private final TomcatManager tomcatManager;
    private final AtomicInteger runningTasks = new AtomicInteger(2);

    /* GuardedBy("this") */
    private ExecutorService service;

    /**
     * Tomcat server log reads from the Tomcat standard and error output and 
     * writes to output window.
     * 
     * @param tomcatManager Tomcat deployment manager
     * @param displayName output window display name.
     * @param in Tomcat standard output reader.
     * @param err Tomcat error output reader.
     * @param autoFlush should we flush after a change?
     * @param takeFocus should be the output window made visible after each
     *        changed?
     */
    public ServerLog(TomcatManager tomcatManager, String displayName, Reader in, Reader err, boolean autoFlush,
            boolean takeFocus) {
        inReader = InputReaderTask.newDrainingTask(InputReaders.forReader(in),
                InputProcessors.bridge(new AnalyzingLineProcessor()));

        errReader = InputReaderTask.newDrainingTask(InputReaders.forReader(err),
                InputProcessors.bridge(new AnalyzingLineProcessor()));
        this.autoFlush = autoFlush;
        this.takeFocus = takeFocus;
        this.tomcatManager = tomcatManager;
        io = UISupport.getServerIO(tomcatManager.getUri());
        try {
            io.getOut().reset();
        } 
        catch (IOException e) {
            Logger.getLogger(ServerLog.class.getName()).log(Level.INFO, null, e);
        }
        writer = io.getOut();
        errorWriter = io.getErr();
        io.select();
        logSupport = new ServerLogSupport();
    }

    public void start() {
        synchronized (this) {
            service = Executors.newFixedThreadPool(2);
            service.submit(inReader);
            service.submit(errReader);
        }
    }
    
    /**
     * Test whether ServerLog thread is still running.
     *
     * @return <code>true</code> if the thread is still running, <code>false</code>
     *         otherwise.
     */
    public boolean isRunning() {
        synchronized (this) {
            return !service.isShutdown();
        }
    }
    
    /**
     * Make the log tab visible.
     */
    public void takeFocus() {
        io.select();
    }

    public void stop() {
        AccessController.doPrivileged( (PrivilegedAction<Void>) () -> {
            synchronized (ServerLog.this) {
                service.shutdownNow();
            }
            return null;
        });
    }
    
    /**
     * Support class for Tomcat server output log line analyzation and for 
     * creating links in the output window.
     */
    static class ServerLogSupport extends LogSupport {
        private String prevMessage;
        private GlobalPathRegistry globalPathRegistry = GlobalPathRegistry.getDefault();
        
        public LineInfo analyzeLine(String logLine) {
            String path = null;
            int line = -1;
            String message = null;
            boolean error = false;
            boolean accessible = false;

            logLine = logLine.trim();
            int lineLenght = logLine.length();

            // look for unix file links (e.g. /foo/bar.java:51: 'error msg')
            if (logLine.startsWith("/")) {
                error = true;
                int colonIdx = logLine.indexOf(":");
                if (colonIdx > -1) {
                    path = logLine.substring(0, colonIdx);
                    accessible = true;
                    if (lineLenght > colonIdx) {
                        int nextColonIdx = logLine.indexOf(":", colonIdx + 1);
                        if (nextColonIdx > -1) {
                            String lineNum = logLine.substring(colonIdx + 1, nextColonIdx);
                            try {
                                line = Integer.valueOf(lineNum);
                            } catch(NumberFormatException nfe) { 
                                // ignore it
                                Logger.getLogger(ServerLog.class.getName()).log(Level.INFO, null, nfe);
                            }
                            if (lineLenght > nextColonIdx) {
                                message = logLine.substring(nextColonIdx + 1, lineLenght); 
                            }
                        }
                    }
                }
            }
            // look for windows file links (e.g. c:\foo\bar.java:51: 'error msg')
            else if (lineLenght > 3 && Character.isLetter(logLine.charAt(0))
                        && (logLine.charAt(1) == ':') && (logLine.charAt(2) == '\\')) {
                error = true;
                int secondColonIdx = logLine.indexOf(":", 2);
                if (secondColonIdx > -1) {
                    path = logLine.substring(0, secondColonIdx);
                    accessible = true;
                    if (lineLenght > secondColonIdx) {
                        int thirdColonIdx = logLine.indexOf(":", secondColonIdx + 1);
                        if (thirdColonIdx > -1) {
                            String lineNum = logLine.substring(secondColonIdx + 1, thirdColonIdx);
                            try {
                                line = Integer.valueOf(lineNum);
                            } catch(NumberFormatException nfe) { 
                                // ignore it
                                Logger.getLogger(ServerLog.class.getName()).log(Level.INFO, null, nfe);
                            }
                            if (lineLenght > thirdColonIdx) {
                                message = logLine.substring(thirdColonIdx + 1, lineLenght);
                            }
                        }
                    }
                }
            }
            // look for stacktrace links (e.g. at java.lang.Thread.run(Thread.java:595)
            //                                 at t.HyperlinkTest$1.run(HyperlinkTest.java:24))
            else if (logLine.startsWith("at ") && lineLenght > 3) {
                error = true;
                int parenthIdx = logLine.indexOf("(");
                if (parenthIdx > -1) {
                    String classWithMethod = logLine.substring(3, parenthIdx);
                    int lastDotIdx = classWithMethod.lastIndexOf(".");
                    if (lastDotIdx > -1) {  
                        int lastParenthIdx = logLine.lastIndexOf(")");
                        String content = null;
                        if (lastParenthIdx > -1) {
                            content = logLine.substring(parenthIdx + 1, lastParenthIdx);
                        }
                        if (content != null) {
                            int lastColonIdx = content.lastIndexOf(":");
                            if (lastColonIdx > -1) {
                                String lineNum = content.substring(lastColonIdx + 1);
                                try {
                                    line = Integer.valueOf(lineNum);
                                } catch(NumberFormatException nfe) {
                                    // ignore it
                                    Logger.getLogger(ServerLog.class.getName()).log(Level.INFO, null, nfe);
                                }
                                message = prevMessage;
                            }
                        }
                        int firstDolarIdx = classWithMethod.indexOf("$"); // > -1 for inner classes
                        String className = classWithMethod.substring(0, firstDolarIdx > -1 ? firstDolarIdx : lastDotIdx);
                        path = className.replace('.','/') + ".java"; // NOI18N
                        accessible = globalPathRegistry.findResource(path) != null;
                    }
                }
            }
            // every other message treat as normal info message
            else {
                prevMessage = logLine;
            }
            return new LineInfo(path, line, message, error, accessible);
        }
    }
    
    private class AnalyzingLineProcessor implements LineProcessor {

        @Override
        public void processLine(String line) {
            ServerLogSupport.LineInfo lineInfo = logSupport.analyzeLine(line);
            if (lineInfo.isError()) {
                if (lineInfo.isAccessible()) {
                    try {
                        errorWriter.println(line, logSupport.getLink(lineInfo.message() , lineInfo.path(), lineInfo.line()));
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    errorWriter.println(line);
                }
            } else {
                writer.println(line);
                if (line.startsWith("SEVERE: WSSERVLET11: failed to parse runtime descriptor: java.lang.LinkageError:")) { // NOI18N
                    File jaxwsApi = InstalledFileLocator.getDefault().locate("modules/ext/jaxws22/api/jakarta.xml.ws-api.jar", null, false); // NOI18N
                    File jaxbApi  = InstalledFileLocator.getDefault().locate("modules/ext/jaxb/api/jaxb-api.jar", null, false); // NOI18N
                    File endoresedDir = tomcatManager.getTomcatProperties().getJavaEndorsedDir();
                    if (jaxwsApi != null && jaxbApi != null) {
                        writer.println(NbBundle.getMessage(ServerLog.class, "MSG_WSSERVLET11", jaxwsApi.getParent(), jaxbApi.getParent(), endoresedDir));
                    } else {
                        writer.println(NbBundle.getMessage(ServerLog.class, "MSG_WSSERVLET11_NOJAR", endoresedDir));
                    }
                }
            }
            if (autoFlush) {
                writer.flush();
                errorWriter.flush();
            }
            if (takeFocus) {
                io.select();
            }
        }

        @Override
        public void close() {
            int running = runningTasks.decrementAndGet();
            if (running == 0) {
                logSupport.detachAnnotation();
                writer.close();
                errorWriter.close();                    
            }
        }
        
        @Override
        public void reset() {
            // noop
        }
    }
}
