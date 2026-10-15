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

package org.apache.tools.ant.module.run;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;

/**
 * Standard logger for producing Ant output messages.
 * @author Jesse Glick
 */
@ServiceProvider(service=AntLogger.class, position=100)
public final class StandardLogger extends AntLogger {
    
    private static final Logger ERR = Logger.getLogger(StandardLogger.class.getName());
    public static final int LOGGER_MAX_LINE_LENGTH = Integer.getInteger("logger.max.line.length", 3000); //NOI18N
    
    /**
     * Regexp matching an output line that is a column marker from a compiler or similar.
     * Captured groups:
     * <ol>
     * <li>spaces preceding caret; length indicates column number
     * </ol>
     * @see "#37358"
     */
    private static final Pattern CARET_SHOWING_COLUMN = Pattern.compile("^( *)\\^$"); // NOI18N
    /**
     * Regexp matching an output line indicating a change into a current working directory, as e.g. from make.
     * Captured groups:
     * <ol>
     * <li>new working dir
     * </ol>
     */
    private static final Pattern CWD_ENTER = Pattern.compile(".*Entering directory [`'\"]?([^`'\"]+)(['\"]|$|\\.\\.\\.$)"); // NOI18N
    /**
     * Regexp matching an output line indicating a change out of a current working directory.
     * Captured groups:
     * <ol>
     * <li>previous working dir
     * </ol>
     */
    private static final Pattern CWD_LEAVE = Pattern.compile(".*Leaving directory [`'\"]?([^`'\"]+)(['\"]|$|\\.\\.\\.$)"); // NOI18N

    /** Hack for #194151. */
    public static final String USING_STANDARD_REDIRECTOR = "USING_STANDARD_REDIRECTOR";
    
    /**
     * Data stored in the session.
     */
    private static final class SessionData {
        /** Time build was started. */
        public long startTime;
        /** Last-created hyperlink, in case we need to adjust the column number. */
        public Hyperlink lastHyperlink;
        /** Current stack of working directories for which output is being displayed; top is current location. */
        public Stack<File> currentDir = new Stack<File>();
        public SessionData() {}
    }
    
    /** used only for unit testing */
    private final long mockTotalTime;
    
    /** Default constructor for lookup. */
    public StandardLogger() {
        mockTotalTime = 0L;
    }
    
    /** used only for unit testing */
    StandardLogger(long mockTotalTime) {
        this.mockTotalTime = mockTotalTime;
    }
    
    @Override
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    @Override
    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }
    
    @Override
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
    @Override
    public String[] interestedInTasks(AntSession session) {
        return AntLogger.ALL_TASKS;
    }
    
    @Override
    public int[] interestedInLogLevels(AntSession session) {
        int verb = session.getVerbosity();
        assert verb >= AntEvent.LOG_ERR && verb <= AntEvent.LOG_DEBUG : verb;
        int[] levels = new int[verb + 1];
        for (int i = 0; i <= verb; i++) {
            levels[i] = i;
        }
        return levels;
    }
    
    private SessionData getSessionData(AntSession session) {
        SessionData data = (SessionData) session.getCustomData(this);
        if (data == null) {
            data = new SessionData();
            session.putCustomData(this, data);
        }
        return data;
    }
    
    @Override
    public void buildInitializationFailed(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        // Write errors to the output window, since
        // a lot of errors could be annoying as dialogs
        Throwable t = event.getException();
        if (event.getSession().getVerbosity() >= AntEvent.LOG_VERBOSE) {
            deliverStackTrace(t, event);
        } else {
            event.getSession().println(t.toString(), true, null);
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_target_failed_status", event.getSession().getDisplayName()));
        event.consume();
    }

    private static void deliverBlockOfTextAsLines(String lines, AntEvent originalEvent, int level) {
        StringTokenizer tok = new StringTokenizer(lines, "\r\n"); // NOI18N
        while (tok.hasMoreTokens()) {
            String line = tok.nextToken();
            originalEvent.getSession().deliverMessageLogged(originalEvent, line, level);
        }
    }
    
    private static void deliverStackTrace(Throwable t, AntEvent originalEvent) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        deliverBlockOfTextAsLines(sw.toString(), originalEvent, AntEvent.LOG_ERR);
    }
    
    @Override
    public void buildStarted(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        AntSession session = event.getSession();
        getSessionData(session).startTime = System.currentTimeMillis();
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_running_ant", session.getDisplayName()));
        if (!session.getOriginatingScript().getParentFile().getName().equals("executor-snippets")) { // internal detail of JavaRunner
            List<String> cmd = new ArrayList<String>();
            cmd.add("ant");
            switch (session.getVerbosity()) {
            case AntEvent.LOG_DEBUG:
                cmd.add("-d");
                break;
            case AntEvent.LOG_VERBOSE:
                cmd.add("-v");
                break;
            case AntEvent.LOG_WARN:
                cmd.add("-q");
                break;
            }
            cmd.add("-f");
            File script = session.getOriginatingScript();
            if (script.getName().equals("build.xml")) {
                script = script.getParentFile();
            }
            cmd.add(script.getAbsolutePath());
            for (Map.Entry<String,String> prop : session.getProperties().entrySet()) {
                final String key = prop.getKey();
                if (key.equals("build.compiler.emacs")) {
                    continue; // uninteresting
                }
                String value = prop.getValue();
                if (session.isConcealed(key)) {
                    value = "*****";    //NOI18N
                }
                cmd.add(String.format("-D%s=%s",key,value));    //NOI18N
            }
            for (String target : session.getOriginatingTargets()) {
                cmd.add(target);
            }
            String msg = Utilities.escapeParameters(cmd.toArray(new String[0]));
            InputOutput io = session.getIO();
            if (IOColorLines.isSupported(io)) {
                try {
                    IOColorLines.println(io, msg, IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG));
                } catch (IOException x) {
                    ERR.log(Level.INFO, null, x);
                }
            } else {
                session.println(msg.toString(), false, null);
            }
        }
        event.consume();
    }
    
    @Override
    public void buildFinished(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        AntSession session = event.getSession();
        Throwable t = event.getException();
        long time = System.currentTimeMillis() - getSessionData(session).startTime; // #10305
        if (mockTotalTime != 0L) {
            time = mockTotalTime;
        }
        if (t == null) {
            formatColoredMessageWithTime(session, "FMT_finished_target_printed", false, time);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_finished_target_status", session.getDisplayName()));
        } else {
            if (isStopException(t.getCause())) {
                // Sometimes wrapped, but we really want to know just that the thread was stopped.
                t = t.getCause();
            }
            if (!session.isExceptionConsumed(t)) {
                session.consumeException(t);
                StringBuilder msg = new StringBuilder();
                while (isBuildException(t)) { // http://issues.apache.org/bugzilla/show_bug.cgi?id=43398
                    Throwable cause = t.getCause();
                    if (cause == null) {
                        break;
                    }
                    String msg1 = t.toString();
                    String msg2 = cause.toString();
                    if (msg1.endsWith(msg2)) {
                        msg.append(msg1.substring(0, msg1.length() - msg2.length()));
                        t = cause;
                    } else {
                        break;
                    }
                }
                if (isBuildException(t) && session.getVerbosity() < AntEvent.LOG_VERBOSE) {
                    // Stack trace probably not required.
                    // Check for hyperlink to handle e.g. <fail>
                    // which produces a BE whose toString is the location + message.
                    // But send to other loggers since they may wish to suppress such an error.
                    msg.append(t);
                    deliverBlockOfTextAsLines(msg.toString(), event, AntEvent.LOG_ERR);
                } else if (!isStopException(t) || event.getSession().getVerbosity() >= AntEvent.LOG_VERBOSE) {
                    // ThreadDeath can be thrown when killing an Ant process, so don't print it normally
                    if (msg.length() > 0) {
                        deliverBlockOfTextAsLines(msg.toString(), event, AntEvent.LOG_ERR);
                    }
                    deliverStackTrace(t, event);
                }
            }
            if (isStopException(t)) {
                formatColoredMessageWithTime(session, "FMT_target_stopped_printed", true, time);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_target_stopped_status", event.getSession().getDisplayName()));
            } else {
                formatColoredMessageWithTime(session, "FMT_target_failed_printed", true, time);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StandardLogger.class, "FMT_target_failed_status", event.getSession().getDisplayName()));
            }
        }
        event.consume();
    }
    private static boolean isBuildException(Throwable t) {
        Class c = t.getClass();
        while (c != Throwable.class) {
            if (c.getName().equals("org.apache.tools.ant.BuildException")) { // NOI18N
                return true;
            }
            c = c.getSuperclass();
        }
        return false;
    }
    private static boolean isStopException(Throwable t) {
        return (t instanceof ThreadDeath) || (t instanceof InterruptedException);
    }
    
    /** Formats the millis in a human readable String.
     * Total time: {0} minutes
     *             {1} seconds
     */
    private static void formatColoredMessageWithTime(AntSession session, String key, boolean error, long millis) {
        int secs = (int) (millis / 1000);
        int minutes = secs / 60;
        int seconds = secs % 60;
        String msg = NbBundle.getMessage(StandardLogger.class, key, minutes, seconds);
        InputOutput io = session.getIO();
        if (IOColorLines.isSupported(io)) {
            try {
                IOColorLines.println(io, msg, IOColors.getColor(io, error
                        ? IOColors.OutputType.LOG_FAILURE
                        : IOColors.OutputType.LOG_SUCCESS));
                return;
            } catch (IOException x) {
                ERR.log(Level.INFO, null, x);
            }
        }
        session.println(msg, error, null);
    }
    
    @Override
    public void targetStarted(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        String name = event.getTargetName();
        if (name != null) {
            // Avoid printing internal targets normally:
            int minlevel = (name.length() > 0 && name.charAt(0) == '-') ? AntEvent.LOG_VERBOSE : AntEvent.LOG_INFO;
            AntSession session = event.getSession();
            if (session.getVerbosity() >= minlevel) {
                // Avoid printing prefix for top-level script.
                // Note that event.scriptLocation may be different if this target is <import>ed.
                if (!session.getOriginatingScript().getAbsolutePath().equals(event.getProperty("ant.file"))) { // NOI18N
                    String projectName = event.getProperty("ant.project.name"); // NOI18N
                    if (projectName != null && /* hack for JavaRunner */ !projectName.contains("{0}")) { // NOI18N
                        name = projectName + '.' + name;
                    }
                }
                String msg = NbBundle.getMessage(StandardLogger.class, "MSG_target_started_printed", name);
                InputOutput io = session.getIO();
                if (IOColorLines.isSupported(io)) {
                    try {
                        IOColorLines.println(io, msg, IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG));
                    } catch (IOException x) {
                        ERR.log(Level.INFO, null, x);
                    }
                } else {
                    session.println(msg, false, null);
                }
            }
        }
        event.consume();
    }
    
    @Override
    public void messageLogged(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        event.consume();
        AntSession session = event.getSession();
        String line = event.getMessage();
        if (line.startsWith("Trying to override old definition of ") && event.getLogLevel() == AntEvent.LOG_WARN) { // NOI18N
            // #56341, #43968, and many other things in the IDE.
            session.deliverMessageLogged(event, line, AntEvent.LOG_VERBOSE);
            return;
        }
        ERR.log(Level.FINE, "Received message: {0}", line);
        if (line.indexOf('\n') != -1) {
            // Multiline message. Should be split into blocks and redelivered,
            // to allow other loggers (e.g. JavaAntLogger) to process individual
            // lines (e.g. stack traces). Note that other loggers are still capable
            // of handling the original multiline message specially. Note also that
            // only messages at or above the session verbosity will be split.
            deliverBlockOfTextAsLines(line, event, event.getLogLevel());
            return;
        }
        if (line.length() >= LOGGER_MAX_LINE_LENGTH) { // too long message, probably coming from user, so just print it
            if (!isRedirectedJavaOutput(event)) { // if not already printed
                session.println(line, false, null);
            }
            return;
        }
        Matcher m = CARET_SHOWING_COLUMN.matcher(line);
        if (m.matches()) {
            // #37358: adjust the column number of the last hyperlink accordingly.
            ERR.fine("  Looks like a special caret line");
            SessionData data = getSessionData(session);
            if (data.lastHyperlink != null) {
                // For "  ^", infer a column number of 3.
                data.lastHyperlink.setColumn1(m.group(1).length() + 1);
                data.lastHyperlink = null;
                // Don't print the actual caret line, just noise.
                return;
            }
        }
        m = CWD_ENTER.matcher(line);
        if (m.matches()) {
            ERR.fine("  Looks like a change of CWD");
            File d = new File(m.group(1));
            if (d.isDirectory()) {
                Stack<File> stack = getSessionData(session).currentDir;
                stack.push(d);
                ERR.log(Level.FINE, "  ...is a change of CWD; stack now: {0}", stack);
            }
        }
        m = CWD_LEAVE.matcher(line);
        if (m.matches()) {
            ERR.fine("  Looks like a change of CWD back out");
            File d = new File(m.group(1));
            Stack<File> stack = getSessionData(session).currentDir;
            if (stack.empty()) {
                ERR.log(Level.FINE, "  ...but there was nowhere to change out of");
            } else {
                File previous = stack.pop();
                if (!previous.equals(d)) {
                    ERR.log(Level.FINE, "  ...stack mismatch: {0} vs. {1}", new Object[] {previous, d});
                }
            }
        }
        if (isRedirectedJavaOutput(event)) {
            // stdout and stderr is printed directly for java
            return;
        }
        PartiallyLinkedLine parse = findHyperlink(line, event.getSession(), getSessionData(session).currentDir);
        if (parse.hyperlink instanceof Hyperlink) {
            getSessionData(session).lastHyperlink = (Hyperlink) parse.hyperlink;
        }
        parse.println(event.getSession(), event.getLogLevel() <= AntEvent.LOG_WARN);
    }

    /**
     * Check whether a "message logged" event is related to "java" task output
     * which is printed directly.
     *
     * @param event A "message logged" event.
     *
     * @return True if the event is related to some directly printed message
     * from task "java", false otherwise.
     */
    private static boolean isRedirectedJavaOutput(AntEvent event) {
        return "java".equals(event.getTaskName())
                && event.getProperty(USING_STANDARD_REDIRECTOR) == null
                && (event.getLogLevel() == AntEvent.LOG_WARN
                || event.getLogLevel() == AntEvent.LOG_INFO);
    }
    
    @Override
    public void taskFinished(AntEvent event) {
        // Do not consider hyperlinks from previous tasks.
        getSessionData(event.getSession()).lastHyperlink = null;
    }

    private static final Pattern UNIMPORTANT_MESSAGE = Pattern.compile("\\[deprecation\\]|warning|stopped|cannot find symbol|wrong ELF class");
    public static boolean isImportant(String message) {
        if (message.length() >= LOGGER_MAX_LINE_LENGTH) { // too long message, probably coming from user, so not really important
            return false;
        }
        return !UNIMPORTANT_MESSAGE.matcher(message).find();
    }

    public static class PartiallyLinkedLine { // used also in ForkedJavaOverride
        final OutputListener/*|null*/ hyperlink;
        final String all;
        final String/*|null*/ mainPart, postLinkPart;
        PartiallyLinkedLine(String line) {
            this(null, line, null, null);
        }
        PartiallyLinkedLine(OutputListener hyperlink, String all, String mainPart, String postLinkPart) {
            this.hyperlink = hyperlink;
            this.all = all;
            this.mainPart = mainPart;
            this.postLinkPart = postLinkPart;
        }
        public void println(AntSession session, boolean err) {
            if (hyperlink != null) {
                InputOutput io = session.getIO();
                if (IOColorPrint.isSupported(io)) {
                    try {
                        assert mainPart != null;
                        IOColorPrint.print(io, mainPart, hyperlink, isImportant(all), null);
                        assert postLinkPart != null;
                        session.println(postLinkPart, true, null);
                        return;
                    } catch (IOException x) {
                        ERR.log(Level.INFO, null, x);
                    }
                }
                session.println(all, err, hyperlink);
            } else {
                session.println(all, err, null);
            }
        }
    }

    /**
     * Regexp matching an output line which should (perhaps) be hyperlinked to a file.
     * Captured groups:
     * <ol>
     * <li>file name plus optional line/column information (i.e. hyperlinkable portion)
     * <li>file name (rel/abs path or URL)
     * <li>line1
     * <li>col1
     * <li>line2
     * <li>col2
     * <li>non-hyperlinkable portion
     * <li>message
     * </ol>
     */
    private static final Pattern HYPERLINK = Pattern.compile("(\"?(.+?)\"?(?:(?::|, line )(\\d+)(?::(\\d+)(?::(\\d+):(\\d+))?)?)?)(: +(.*))"); // NOI18N
    /**
     * Possibly hyperlink a message logged event.
     */
    public static PartiallyLinkedLine findHyperlink(String line, AntSession session, Stack<File> cwd) {
        if (line.length() >= LOGGER_MAX_LINE_LENGTH) { // too long message, probably coming from user, so just print it without trying to hyperlink
            return new PartiallyLinkedLine(line);
        }
        Matcher m = HYPERLINK.matcher(line);
        if (!m.matches()) {
            ERR.fine("does not look like a hyperlink");
            return new PartiallyLinkedLine(line);
        }
        String path = m.group(2);
        File file;
        if (path.startsWith("file:")) {
            try {
                file = Utilities.toFile(new URI(path));
            } catch (URISyntaxException e) {
                ERR.log(Level.FINE, "invalid URI, skipping", e);
                return new PartiallyLinkedLine(line);
            } catch (IllegalArgumentException e) {
                ERR.log(Level.FINE, "invalid URI, skipping", e);
                return new PartiallyLinkedLine(line);
            }
        } else {
            file = new File(path);
            if (!file.isAbsolute()) {
                if (cwd == null || cwd.isEmpty()) {
                    ERR.fine("Non-absolute path with no CWD, skipping");
                    // don't waste time on File.exists!
                    return new PartiallyLinkedLine(line);
                } else {
                    file = new File(cwd.peek(), path);
                }
            }
        }
        if (!file.exists()) {
            ERR.log(Level.FINE, "no such file {0}, skipping", file);
            return new PartiallyLinkedLine(line);
        }

        int line1 = -1, col1 = -1, line2 = -1, col2 = -1;
        String num = m.group(3);
        try {
            if (num != null) {
                line1 = Integer.parseInt(num);
                num = m.group(4);
                if (num != null) {
                    col1 = Integer.parseInt(num);
                    num = m.group(5);
                    if (num != null) {
                        line2 = Integer.parseInt(num);
                        col2 = Integer.parseInt(m.group(6));
                    }
                }
            }
        } catch (NumberFormatException e) {
            ERR.log(Level.FINE, "bad line/col #", e);
            return new PartiallyLinkedLine(line);
        }

        String message = m.group(8);
        
        file = FileUtil.normalizeFile(file); // do this late, after File.exists
        ERR.log(Level.FINE, "Hyperlink: {0} [{1}:{2}:{3}:{4}]: {5}", new Object[] {file, line1, col1, line2, col2, message});
        try {
            return new PartiallyLinkedLine(
                    session.createStandardHyperlink(Utilities.toURI(file).toURL(), message, line1, col1, line2, col2),
                    line, m.group(1), m.group(7));
        } catch (MalformedURLException e) {
            assert false : e;
            return new PartiallyLinkedLine(line);
        }
    }

}
