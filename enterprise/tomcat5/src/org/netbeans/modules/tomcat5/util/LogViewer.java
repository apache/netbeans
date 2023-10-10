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

package org.netbeans.modules.tomcat5.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.util.LogSupport.LineInfo;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Thread which displays Tomcat log files in the output window. The output
 * window name equals prefix minus trailing dot, if present.
 *
 * Currently only <code>org.apache.catalina.logger.FileLogger</code> logger
 * is supported.
 *
 * @author  Stepan Herold
 */
public class LogViewer extends Thread {

    private static final Logger LOGGER = Logger.getLogger(LogViewer.class.getName());

    private volatile boolean stop = false;
    private final TomcatManager tomcatManager;
    private InputOutput inOut;
    private OutputWriter writer;
    private OutputWriter errorWriter;
    private File directory;
    private String prefix;
    private String suffix;
    private boolean isTimestamped;
    private boolean takeFocus;
    
    private ContextLogSupport logSupport;
    private String catalinaWorkDir;
    private String webAppContext;
    private boolean isStarted;
    
    /**
     * List of listeners which are notified when the log viewer is stoped.
     */
    private List<LogViewerStopListener> stopListeners = Collections.synchronizedList(new LinkedList());

    private String displayName;
    
    /**
     * Create a new LogViewer thread.
     *
     * @param tomcatManager Tomcat deployment manager.
     * @param webAppContext web application's context this logger is declared for,
     *        may be <code>null</code> for shared context log. It is used to look
     *        up sources of servlets generated from JSPs.
     * @param className class name of logger implementation
     * @param directory absolute or relative pathname of a directory in which log 
     *        files reside, if null catalina default is used.
     * @param prefix log file prefix, if null catalina default is used.
     * @param suffix log file suffix, if null catalina default is used.
     * @param isTimestamped whether logged messages are timestamped.
     * @param takeFocus whether output window should get focus after each change.
     * 
     * @throws UnsupportedLoggerException logger specified by the className parameter
     *         is not supported.
     */
    public LogViewer(TomcatManager tomcatManager, String webAppContext, 
            String className, String directory, String prefix, String suffix, 
            boolean isTimestamped, boolean takeFocus) throws UnsupportedLoggerException {
        super("LogViewer - Thread"); // NOI18N
        this.tomcatManager = tomcatManager;
        this.catalinaWorkDir = tomcatManager.getCatalinaWork();
        if (className != null && !"org.apache.catalina.logger.FileLogger".equals(className)) { // NOI18N
            throw new UnsupportedLoggerException(className);
        }        
        if (directory != null) {
            this.directory = new File(directory);
            if (!this.directory.isAbsolute()) {
                this.directory = new File(tomcatManager.getTomcatProperties().getCatalinaDir(), directory);
            }
        } else {
            this.directory = new File(tomcatManager.getTomcatProperties().getCatalinaDir(), "logs");  // NOI18N
        }
        if (prefix != null) {
            this.prefix = prefix;
        } else {
            this.prefix = "catalina."; // NOI18N
        }
        if (suffix != null) {
            this.suffix = suffix;
        } else {
            this.suffix = ".log";  // NOI18N
        }
        this.isTimestamped = isTimestamped;
        this.takeFocus = takeFocus;
        this.webAppContext = webAppContext;
        logSupport = new ContextLogSupport(catalinaWorkDir, webAppContext);
        setDaemon(true);
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Stop the LogViewer thread.
     */
    public void close() {
        synchronized(this) {
            stop = true;
            notify();
        }        
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LogViewer) {
            LogViewer anotherLogViewer = (LogViewer)obj;            
            if (catalinaWorkDir.equals(anotherLogViewer.catalinaWorkDir) 
                && (((webAppContext != null) && webAppContext.equals(anotherLogViewer.webAppContext)) 
                    || (webAppContext == anotherLogViewer.webAppContext))
                && directory.equals(anotherLogViewer.directory)
                && prefix.equals(anotherLogViewer.prefix)
                && suffix.equals(anotherLogViewer.suffix)
                && isTimestamped) {
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Tests whether LogViewer thread is still running.
     * @return <code>false</code> if thread was stopped or its output window
     * was closed, <code>true</code> otherwise.
     */
    public boolean isOpen() {
        InputOutput io = inOut;
        return !(io == null || stop || (isStarted && io.isClosed()));
    }
    
    /**
     * Make the log tab visible
     */
    public void takeFocus() {
        InputOutput io = inOut;
        if (io != null) {
            io.select();
        }
    }
    
    private File getLogFile(String timestamp) throws IOException {
        File f = new File(directory, prefix + timestamp + suffix);
        try {
            FileUtil.createData(f);
        } catch (IOException ex) {
            // this can happen when directory is RO for netbeans
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
        return f;
    }
    
    private String getTimestamp() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); //NOI18N
        return df.format(new Date());
    }
    
    private void processLine(String line) {
        ContextLogSupport.LineInfo lineInfo = logSupport.analyzeLine(line);
        if (lineInfo.isError()) {
            if (lineInfo.isAccessible()) {
                try {
                    errorWriter.println(line, logSupport.getLink(lineInfo.message(), lineInfo.path(), lineInfo.line()));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                errorWriter.println(line);
            }
        } else {
            if (line.contains("java.lang.LinkageError: JAXB 2.0 API")) { // NOI18N
                File jaxwsApi = InstalledFileLocator.getDefault().locate("modules/ext/jaxws22/api/jakarta.xml.ws-api.jar", null, false); // NOI18N
                File jaxbApi = InstalledFileLocator.getDefault().locate("modules/ext/jaxb/api/jaxb-api.jar", null, false); // NOI18N
                File endoresedDir = tomcatManager.getTomcatProperties().getJavaEndorsedDir();
                if (jaxwsApi != null && jaxbApi != null) {
                    writer.println(NbBundle.getMessage(LogViewer.class, "MSG_WSSERVLET11", jaxwsApi.getParent(), jaxbApi.getParent(), endoresedDir));
                } else {
                    writer.println(NbBundle.getMessage(LogViewer.class, "MSG_WSSERVLET11_NOJAR", endoresedDir));
                }
            }
            writer.println(line);
        }
    }

    @Override
    public void run() {
        if (displayName == null) {
            // cut off trailing dot
            displayName = this.prefix;
            int trailingDot = displayName.lastIndexOf(".");
            if (trailingDot > -1) {
                displayName = displayName.substring(0, trailingDot);
            }
        }
        inOut = IOProvider.getDefault().getIO(displayName, false);
        try {
            inOut.getOut().reset();
        } 
        catch (IOException e) {
            // not a critical error, continue
            Logger.getLogger(LogViewer.class.getName()).log(Level.INFO, null, e);
        }        
        inOut.select();
        writer = inOut.getOut();
        errorWriter = inOut.getErr();
        isStarted = true;
        
        String timestamp = getTimestamp();
        String oldTimestamp = timestamp;
        try {
            File logFile = getLogFile(timestamp);
            BufferedReader reader = null;
            if (logFile != null && logFile.isFile()) {
                reader = new BufferedReader(new FileReader(logFile));
            }
            try {
                while (!stop && !inOut.isClosed()) {
                    Process process = tomcatManager.getTomcatProcess();
                    try {
                        if (process != null) {
                            process.exitValue();
                            // finished
                            break;
                        }
                    } catch (IllegalThreadStateException ex) {
                        //ok running
                    }

                    // check whether a log file has rotated
                    timestamp = getTimestamp();
                    if (!timestamp.equals(oldTimestamp)) {
                        oldTimestamp = timestamp;
                        if (reader != null) {
                            reader.close();
                        }
                        logFile = getLogFile(timestamp);
                        if (logFile != null && logFile.isFile()) {
                            reader = new BufferedReader(new FileReader(logFile));
                        }
                    }
                    if (reader != null) {
                        int count = 0;
                        // take a nap after 1024 read cycles, this should ensure responsiveness
                        // even if log file is growing fast
                        boolean updated = false;
                        while (reader.ready() && count++ < 1024) {
                            processLine(reader.readLine());
                            updated = true;
                        }
                        if (updated) {
                            writer.flush();
                            errorWriter.flush();
                            if (takeFocus) {
                                inOut.select();
                            }
                        }
                    }
                    // wait for the next attempt
                    try {
                        synchronized(this) {
                            if (!stop &&  !inOut.isClosed()) {
                                wait(100);
                            }
                        }
                    } catch(InterruptedException ex) {
                        // no op - the thread was interrupted 
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            writer.close();
            errorWriter.close();
        }
        fireLogViewerStopListener();
        logSupport.detachAnnotation();
    }
    
    /** 
     * Add a <code>LogViewerStopListener</code>.
     * 
     * @param listener <code>LogViewerStopListener</code> which will be notified
     *        when the <code>LogViewer</code> stops running.
     */
    public void addLogViewerStopListener(LogViewerStopListener listener) {
        stopListeners.add(listener);
    }

    /** 
     * Remove all registered <code>LogViewerStopListener</code> listeners.
     * 
     * @param listener <code>LogViewerStopListener</code> which will be notified
     *        when the <code>LogViewer</code> stops running.
     */    
    public void removeAllLogViewerStopListener() {
        stopListeners.removeAll(stopListeners);
    }
    
    private void fireLogViewerStopListener() {
        for (Iterator i = stopListeners.iterator(); i.hasNext();) {
            ((LogViewerStopListener)i.next()).callOnStop();
        }
    }
    
    /**
     * <code>LogViewerStopListener</code> is notified when the <code>LogViewer</code>
     * stops running.
     */
    public static interface LogViewerStopListener extends EventListener {
        public void callOnStop();
    }
    
    /**
     * Support class for context log line analyzation and for creating links in 
     * the output window.
     */
    static class ContextLogSupport extends LogSupport {
        private final String CATALINA_WORK_DIR;
        private String context = null;
        private String prevMessage = null;
        private static final String STANDARD_CONTEXT = "StandardContext["; // NOI18N
        private static final int STANDARD_CONTEXT_LENGTH = STANDARD_CONTEXT.length();
        private GlobalPathRegistry globalPathReg = GlobalPathRegistry.getDefault();
        

        public ContextLogSupport(String catalinaWork, String webAppContext) {
            CATALINA_WORK_DIR = catalinaWork;
            context = webAppContext;
        }
        
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
                                Logger.getLogger(LogViewer.class.getName()).log(Level.INFO, null, nfe);
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
                            } catch(NumberFormatException nfe) { // ignore it
                                Logger.getLogger(LogViewer.class.getName()).log(Level.INFO, null, nfe);
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
                        int lastColonIdx = logLine.lastIndexOf(":");
                        if (lastParenthIdx > -1 && lastColonIdx > -1) {
                            String lineNum = logLine.substring(lastColonIdx + 1, lastParenthIdx);
                            try {
                                line = Integer.valueOf(lineNum);
                            } catch(NumberFormatException nfe) { // ignore it
                                Logger.getLogger(LogViewer.class.getName()).log(Level.INFO, null, nfe);
                            }
                            message = prevMessage;
                        }
                        int firstDolarIdx = classWithMethod.indexOf("$"); // > -1 for inner classes
                        String className = classWithMethod.substring(0, firstDolarIdx > -1 ? firstDolarIdx : lastDotIdx);
                        path = className.replace('.','/') + ".java"; // NOI18N              
                        accessible = globalPathReg.findResource(path) != null;
                        if (className.startsWith("org.apache.jsp.") && context != null) { // NOI18N
                            if (context != null) {
                                String contextPath = context.equals("/") 
                                                        ? "/_"     // hande ROOT context
                                                        : context;
                                path = CATALINA_WORK_DIR + contextPath + "/" + path;
                                accessible = new File(path).exists();
                            }
                        }
                    }
                }
            }
            // every other message treat as normal info message
            else {
                prevMessage = logLine;
                // try to get context, if stored
                int stdContextIdx = logLine.indexOf(STANDARD_CONTEXT);
                int lBracketIdx = -1;
                if (stdContextIdx > -1) {
                    lBracketIdx = stdContextIdx + STANDARD_CONTEXT_LENGTH;
                }
                int rBracketIdx = logLine.indexOf("]");
                if (lBracketIdx > -1 && rBracketIdx > -1 && rBracketIdx > lBracketIdx) {
                    context = logLine.substring(lBracketIdx, rBracketIdx);
                }
            }
            return new LineInfo(path, line, message, error, accessible);
        }
    }
}
