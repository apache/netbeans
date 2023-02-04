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

package org.netbeans.modules.web.webkit.tooling.console;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.SystemColor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.common.api.RemoteFileCache;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.webkit.debugging.api.console.Console;
import org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.FoldHandle;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.IOFolding;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 */
public class BrowserConsoleLogger implements Console.Listener {
    
    private static final String LEVEL_ERROR = "error";      // NOI18N
    private static final String LEVEL_WARNING = "warning";      // NOI18N
    private static final String LEVEL_DEBUG = "debug";      // NOI18N
    
    private static final String PROMPT = "> ";              // NOI18N

    private final Lookup projectContext;
    private InputOutput io;
    private Color colorStdBrighter;
    /** The last logged message. */
    private ConsoleMessage lastMessage;
    private Console.InputCallback input;
    private ConsoleReader reader;
    private boolean isFoldingSupported;
    //private Color colorErrBrighter;
    private final AtomicBoolean shownOnError = new AtomicBoolean(false);
    private final RequestProcessor rp = new RequestProcessor(BrowserConsoleLogger.class);

    private static final Logger LOG = Logger.getLogger(BrowserConsoleLogger.class.getName());

    public BrowserConsoleLogger(Lookup projectContext) {
        this.projectContext = projectContext;
        initIO();
    }
    
    @NbBundle.Messages({"BrowserConsoleLoggerTitle=Browser Log"})
    private void initIO() {
        io = IOProvider.getDefault().getIO(Bundle.BrowserConsoleLoggerTitle(), false);
        setIOColors();
        io.setInputVisible(true);
        isFoldingSupported = IOFolding.isSupported(io);
        io.getOut().print(PROMPT);
        Reader r = io.getIn();
        reader = new ConsoleReader(r);
        rp.post(reader);
    }
    
    private void setIOColors() {
        if (IOColors.isSupported(io) && IOColorPrint.isSupported(io)) {
            Color colorStd = IOColors.getColor(io, IOColors.OutputType.OUTPUT);
            //Color colorErr = IOColors.getColor(io, IOColors.OutputType.ERROR);
            Color background = UIManager.getDefaults().getColor("nb.output.background");    // NOI18N
            if (background == null) {
                background = SystemColor.window;
            }
            colorStdBrighter = shiftTowards(colorStd, background);
            //colorErrBrighter = shiftTowards(colorErr, background);
            Color foreground = UIManager.getDefaults().getColor("nb.output.foreground");    // NOI18N
            if (foreground == null) {
                foreground = SystemColor.textText;
            }
            IOColors.setColor(io, IOColors.OutputType.INPUT, shiftTowards(foreground, Color.GREEN));
        }
    }

    public void close() {
        LOG.fine("close() closing IO");
        reader.closing();
        io.getErr().close();
        io.getOut().close();
        try {
            io.getIn().close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static Color shiftTowards(Color c, Color b) {
        return new Color((c.getRed() + b.getRed())/2, (c.getGreen() + b.getGreen())/2, (c.getBlue() + b.getBlue())/2);
    }
    
    @Override
    public void messageAdded(ConsoleMessage message) {
        try {
            lastMessage = message;
            logMessage(message);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void messagesCleared() {
        LOG.fine("messagesCleared() resetting IO");
        try {
            io.getOut().reset();
        } catch (IOException ex) {}
        setIOColors();
        io.setInputVisible(true);
        // HACK: Wait for the processing of reset event
        try {
            EventQueue.invokeAndWait(new Runnable() {
                @Override public void run() {}
            });
        } catch (InterruptedException ex) {
        } catch (InvocationTargetException ex) {}
        io.getOut().print(PROMPT);
    }

    @Override
    public void messageRepeatCountUpdated(int count) {
        LOG.log(Level.FINE, "messageRepeatCountUpdated({0})", count);
        try {
            logMessage(lastMessage);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
    public static String getCurrentTime() {
        return formatter.format(new Date(System.currentTimeMillis()));
    }
    
    private boolean shouldLogMessage(ConsoleMessage msg) {
        if (msg == null) {
            return false; // Issue 250389
        }
        String level = msg.getLevel();
        String source = msg.getSource();
        // Ignore CSS warnings
        if (LEVEL_WARNING.equals(level) && "css".equals(source)) {              // NOI18N
            return false;
        }
        return true;
    }
    
    private void logMessage(ConsoleMessage msg) throws IOException {
        if (!shouldLogMessage(msg)) {
            return ;
        }
        io.getOut().print("\b\b");
        Project project = projectContext.lookup(Project.class);
        logMessage(msg, project);
        io.getOut().print(PROMPT);
        boolean isErr = LEVEL_ERROR.equals(msg.getLevel());
        if (io.isClosed() || (isErr && !shownOnError.getAndSet(true))) {
            io.select();
        }
    }
    
    private void logMessage(ConsoleMessage msg, Project project) throws IOException {
        String level = msg.getLevel();
        boolean isErr = LEVEL_ERROR.equals(level);
        String time = getCurrentTime();

        String logInfo = createLogInfo(time, level, msg.getSource(), msg.getType());
        OutputWriter ow = isErr ? io.getErr() : io.getOut();
        String text = msg.getText();
        if (text != null) { // Text is not marked as optional, but apparently, it is.
            String lines[] = text.replace("\r", "").split("\n");
            for (int i = 0; i < lines.length; i++) {
                String singleMessageLine = lines[i];
                if (colorStdBrighter == null && i == lines.length-1) {
                    singleMessageLine += logInfo;
                }
                Object res[] = tryToConvertLineToHyperlink(project, singleMessageLine);
                MyListener l = null;
                String newMessage1 = null;
                String newMessage2 = null;
                if (res != null) {
                    l = (MyListener)res[0];
                    newMessage1 = (String)res[1];
                    newMessage2 = (String)res[2];
                }
                if (l != null && l.isValidHyperlink()) {
                    if (colorStdBrighter != null && i == lines.length-1) {
                        newMessage2 += logInfo;
                    }
                    ow.print(newMessage1);
                    ow.println(newMessage2, l);
                } else {
                    ow.print(singleMessageLine);
                    if (colorStdBrighter != null && i == lines.length-1) {
                        //if (isErr) {
                        //    IOColorPrint.print(io, logInfo, colorErrBrighter);
                        //} else {
                            IOColorPrint.print(io, logInfo, colorStdBrighter);
                        //}
                    } else {
                        ow.println("");
                    }
                }
            }
        }
        
        boolean doPrintStackTrace = LEVEL_ERROR.equals(level) ||
                                    LEVEL_DEBUG.equals(level);
        
        StringBuilder sb;
        boolean first = true;
        if (doPrintStackTrace && msg.getStackTrace() != null) {
            FoldHandle fold = null;
            if (isFoldingSupported) {
                fold = IOFolding.startFold(io, false);
            }
            for (ConsoleMessage.StackFrame sf : msg.getStackTrace()) {
                String indent;
                if (first) {
                    indent = "    at ";
                    first = false;
                } else {
                    indent = "    at ";
                }
                ow.print(indent);
                ow.print(sf.getFunctionName());
                sb = new StringBuilder();
                
                String urlStr = sf.getURLString();
                urlStr = getProjectPath(project, urlStr);
                sb.append(" (").append(urlStr).append(":").append(sf.getLine()).append(":").append(sf.getColumn()).append(")");
                MyListener l = new MyListener(project, sf.getURLString(), sf.getLine(), sf.getColumn());
                if (l.isValidHyperlink()) {
                    ow.println(sb.toString(), l);
                } else {
                    ow.println(sb.toString());
                }
            }
            if (fold != null) {
                fold.finish();
            }
        }
        if (first && msg.getURLString() != null && msg.getURLString().length() > 0) {
            ow.print("  at ");
            String url = msg.getURLString();
            String file = getProjectPath(project, url);
            sb = new StringBuilder(file);
            int line = msg.getLine();
            if (line != -1 && line != 0) {
                sb.append(":");
                sb.append(line);
            }        
            MyListener l = new MyListener(project, url, line, -1);
            if (l.isValidHyperlink()) {
                ow.println(sb.toString(), l);
            } else {
                ow.println(sb.toString());
            }
        }
        List<ConsoleMessage> subMessages = msg.getSubMessages();
        if (!subMessages.isEmpty()) {
            FoldHandle fold = null;
            if (isFoldingSupported) {
                fold = IOFolding.startFold(io, false);
            }
            for (ConsoleMessage cm : subMessages) {
                logMessage(cm, project);
            }
            if (fold != null) {
                fold.finish();
            }
        }
    }

    @NbBundle.Messages({"BrowserConsoleLoggerSessionClosed=Debugging session with browser was closed."})
    void sessionWasClosed() {
        io.getOut().print("\b\b");
        io.getOut().println(Bundle.BrowserConsoleLoggerSessionClosed());
    }

    // XXX: exact this algorithm is also in 
    // javascript.jstestdriver/src/org/netbeans/modules/javascript/jstestdriver/JSTestDriverSupport.java
    // keep them in sync
    private Object[] tryToConvertLineToHyperlink(Project project, String line) {
        // pattern is "at ...... (file:line:column)"
        // file can be also http:// url
        if (!line.endsWith(")")) {
            return tryToConvertLineURLToHyperlink(project, line);
        }
        int start = line.lastIndexOf('(');
        if (start == -1) {
            return null;
        }
        int lineNumberEnd = line.lastIndexOf(':');
        if (lineNumberEnd == -1) {
            return null;
        }
        int fileEnd = line.lastIndexOf(':', lineNumberEnd-1);
        if (fileEnd == -1) {
            return null;
        }
        if (start >= fileEnd) {
            return null;
        }
        int lineNumber = -1;
        int columnNumber = -1;
        try {
            lineNumber = Integer.parseInt(line.substring(fileEnd+1, lineNumberEnd));
            columnNumber = Integer.parseInt(line.substring(lineNumberEnd+1, line.length()-1));
        } catch (NumberFormatException e) {
            //ignore
        }
        if (columnNumber != -1 && lineNumber == -1) {
            // perhaps stack trace had only line number:
            lineNumber = columnNumber;
        }
        if (lineNumber == -1) {
            return null;
        }
        String file = line.substring(start+1, fileEnd);
        if (file.length() == 0) {
            return null;
        }
        String s1 = line.substring(0, start);
        String s2 = "(" +  // NOI18N
                getProjectPath(project, file) +
            line.substring(fileEnd);
        MyListener l = new MyListener(project, file, lineNumber, columnNumber);
        return new Object[]{l,s1,s2};
    }
    
    private Object[] tryToConvertLineURLToHyperlink(Project project, String line) {
        int u1 = line.indexOf("http://");   // NOI18N
        if (u1 < 0) {
            u1 = line.indexOf("https://");  // NOI18N
        }
        if (u1 < 0) {
            return null;
        }
        int ue = line.indexOf(' ', u1);
        if (ue < 0) {
            ue = line.length();
        }
        int col2 = line.lastIndexOf(':', ue);
        if (col2 < 0) {
            return null;
        }
        int col1 = line.lastIndexOf(':', col2 - 1);
        if (col1 < 0) {
            return null;
        }
        int lineNumber = -1;
        int columnNumber = -1;
        try {
            lineNumber = Integer.parseInt(line.substring(col1+1, col2));
            columnNumber = Integer.parseInt(line.substring(col2+1, ue));
        } catch (NumberFormatException e) {
            //ignore
        }
        if (columnNumber != -1 && lineNumber == -1) {
            // perhaps stack trace had only line number:
            lineNumber = columnNumber;
        }
        if (lineNumber == -1) {
            return null;
        }
        String file = line.substring(u1, col1);
        if (file.length() == 0) {
            return null;
        }
        String s1 = line.substring(0, u1);
        String s2 = line.substring(u1);
        MyListener l = new MyListener(project, file, lineNumber, columnNumber);
        return new Object[]{l,s1,s2};
    }
    
    
    private static final String LOG_IGNORED = "log";    // NOI18N
    private static final String CONSOLE_API = "console-api";    // NOI18N
    private static final String TIME_SEPARATOR = " | "; // NOI18N
    private static String createLogInfo(String time, String level, String source, String type) {
        //String logInfo = " ("+time+" | "+level+","+msg.getSource()+","+msg.getType()+")\n";
        StringBuilder logInfoBuilder = new StringBuilder(" (");
        logInfoBuilder.append(time);
        boolean separator = false;
        if (!LOG_IGNORED.equals(level) && !level.isEmpty()) {
            separator = true;
            logInfoBuilder.append(TIME_SEPARATOR);
            logInfoBuilder.append(level);
        }
        if (!CONSOLE_API.equals(source) && !source.isEmpty()) {
            if (separator) {
                logInfoBuilder.append(", ");
            } else {
                logInfoBuilder.append(TIME_SEPARATOR);
            }
            logInfoBuilder.append(source);
        }
        if (!LOG_IGNORED.equals(type)) {
            if (separator) {
                logInfoBuilder.append(", ");
            } else {
                logInfoBuilder.append(TIME_SEPARATOR);
            }
            logInfoBuilder.append(type);
        }
        logInfoBuilder.append(")\n");
        return logInfoBuilder.toString();
    }
    
    /**
     * Try to find a more readable project-relative path.<p>
     * E.g.: "http://localhost:89/SimpleLiveHTMLTest/js/app.js:8:9"
     * is turned into: "js/app.js:8:9"
     * @param urlStr The URL
     * @return a project-relative path, or the original URL.
     */
    public static String getProjectPath(Project project, String urlStr) {
        try {
            URL url = new URL(urlStr);
            if (project != null) {
                FileObject fo = ServerURLMapping.fromServer(project, url);
                if (fo != null) {
                    String relPath = FileUtil.getRelativePath(project.getProjectDirectory(), fo);
                    if (relPath != null) {
                        urlStr = relPath;
                    }
                }
            }
        } catch (MalformedURLException murl) {}
        return urlStr;
    }

    void setInput(Console.InputCallback input) {
        this.input = input;
    }

    public static class MyListener implements OutputListener {

        private final String url;
        private final int line;
        private final int column;
        private final Project project;

        public MyListener(Project project, String url, int line, int column) {
            this.url = url;
            this.line = line;
            this.column = column;
            this.project = project;
        }
        
        @Override
        public void outputLineSelected(OutputEvent ev) {
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            Line l = getLine();
            if (l != null) {
                l.show(Line.ShowOpenType.OPEN, 
                    Line.ShowVisibilityType.FOCUS, column != -1 ? column -1 : -1);
            }
        }
        private Line getLine() {
            return BrowserConsoleLogger.getLine(project, url, line-1);
        }

        @Override
        public void outputLineCleared(OutputEvent ev) {
        }
        
        public boolean isValidHyperlink() {
            return getLine() != null;
        }
    
    }

    private static Line getLine(Project project, final String filePath, final int lineNumber) {
        if (filePath == null || lineNumber < 0) {
            return null;
        }

        FileObject fileObject = null;
        try {
            if (filePath.startsWith("http:") || filePath.startsWith("https:")) {    // NOI18N
                URL url;
                try {
                    url = new URI(filePath).toURL();
                } catch (URISyntaxException usex) {
                    // Issue 231564
                    url = new URL(filePath);
                }
                if (project != null) {
                    fileObject = ServerURLMapping.fromServer(project, url);
                }
                if (fileObject == null) {
                    fileObject = RemoteFileCache.getRemoteFile(url);
                }
            } else {
                File file;
                if (filePath.startsWith("file:/")) {                                // NOI18N
                    URI uri;
                    try {
                        // Issue 255536
                        uri = new URI(filePath);
                    } catch (URISyntaxException usex) {
                        uri = null;
                    }
                    if (uri == null || uri.getQuery() != null || uri.getFragment() != null) {
                        // Remove the query and/or fragment
                        uri = WebUtils.stringToUrl(WebUtils.urlToString(new URL(filePath), true)).toURI();
                    }
                    file = Utilities.toFile(uri);
                } else {
                    file = new File(filePath);
                    if (!file.isAbsolute() && project != null) {
                        fileObject = findFileInProjecSources(project, filePath);
                    }
                }
                if (fileObject == null) {
                    fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
                }
            }
        } catch (IOException | URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (fileObject == null) {
            LOG.log(Level.FINE, "Cannot resolve \"{0}\"", filePath);
            return null;
        }

        LineCookie lineCookie = getLineCookie(fileObject);
        if (lineCookie == null) {
            LOG.log(Level.INFO, "No line cookie for \"{0}\"", fileObject);
            return null;
        }
        try {
            return lineCookie.getLineSet().getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ioob) {
            List<? extends Line> lines = lineCookie.getLineSet().getLines();
            if (lines.size() > 0) {
                return lines.get(lines.size() - 1);
            } else {
                return null;
            }
        }
    }
    
    private static final String[] KNOWN_SOURCE_TYPES = {Sources.TYPE_GENERIC,
                                                        WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT,
                                                        WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST,
                                                        WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST_SELENIUM };
    private static FileObject findFileInProjecSources(Project project, String filePath) {
        Sources sources = ProjectUtils.getSources(project);
        for (String sourceType : KNOWN_SOURCE_TYPES) {
            SourceGroup[] sourceGroups = sources.getSourceGroups(sourceType);
            for (SourceGroup sourceGroup : sourceGroups) {
                FileObject rootFolder = sourceGroup.getRootFolder();
                FileObject fileObject = rootFolder.getFileObject(filePath);
                if (fileObject != null) {
                    return fileObject;
                }
            }
        }
        return null;
    }

    public static LineCookie getLineCookie(final FileObject fo) {
        LineCookie result = null;
        try {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject != null) {
                result = dataObject.getLookup().lookup(LineCookie.class);
            }
        } catch (DataObjectNotFoundException e) {
            Exceptions.printStackTrace(Exceptions.attachSeverity(e, Level.INFO));
        }
        return result;
    }
    
    private class ConsoleReader implements Runnable {
        
        private final BufferedReader r;
        private volatile boolean closing = false;
        
        public ConsoleReader(Reader r) {
            this.r = new BufferedReader(r);
        }
        
        void closing() {
            closing = true;
        }

        @Override
        public void run() {
            try {
                String line;
                while (!closing) {
                    line = r.readLine();
                    LOG.log(Level.FINE, "Got line from Console Reader: \"{0}\"", line);
                    if (line != null) {
                        input.line(line);
                    }
                }
            } catch (IOException ex) {
                if (!closing) {
                    Exceptions.printStackTrace(ex);
                }
            }
            LOG.log(Level.FINE, "ConsoleReader has finished. Bye.");
        }
        
    }
}
