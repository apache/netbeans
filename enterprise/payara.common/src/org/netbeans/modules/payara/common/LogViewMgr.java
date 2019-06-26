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

package org.netbeans.modules.payara.common;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import org.netbeans.modules.payara.tooling.server.FetchLog;
import org.netbeans.modules.payara.tooling.server.FetchLogEvent;
import org.netbeans.modules.payara.tooling.server.FetchLogEventListener;
import org.netbeans.modules.payara.tooling.server.FetchLogPiped;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.payara.common.actions.DebugAction;
import org.netbeans.modules.payara.common.actions.RefreshAction;
import org.netbeans.modules.payara.common.actions.RestartAction;
import org.netbeans.modules.payara.common.actions.StartServerAction;
import org.netbeans.modules.payara.common.actions.StopServerAction;
import org.netbeans.modules.payara.spi.Recognizer;
import org.netbeans.modules.payara.spi.RecognizerCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.windows.*;
import org.netbeans.modules.payara.spi.PayaraModule;


/**
 * This class is capable of tailing the specified file or input stream. It
 * checks for changes at the specified intervals and outputs the changes to
 * the given I/O panel in NetBeans.
 *
 * FIXME Refactor: LogViewMgr should be a special case of SimpleIO
 * 
 * @author Peter Williams
 * @author Michal Mocnak
 */
public class LogViewMgr {

    /** Local logger. */
    private static final Logger LOGGER = PayaraLogger.get(LogViewMgr.class);

    private static final boolean strictFilter = Boolean.getBoolean("glassfish.logger.strictfilter"); // NOI18N

    /**
     * Amount of time in milliseconds to wait between checks of the input
     * stream
     */
    private static final int DELAY = Utilities.isWindows() ? 1 : 100;
    
    /**
     * Singleton model pattern
     */
    private static final Map<String, WeakReference<LogViewMgr>> INSTANCES =
            new HashMap<>();

    /**
     * Server URI for this log view
     */
    private final String uri;

    /**
     * The I/O window where to output the changes
     */
    private InputOutput io;

    /**
     * Active readers for this log view.  This list contains references either
     * to nothing (which means log is not active), a single file reader to
     * monitor server log if the server is running outside the IDE, or two
     * stream readers for servers started within the IDE.
     *
     * !PW not sure this complexity is worth it.  Reading server log correctly
     * is a major pain compared to reading server I/O streams directly.  But we
     * don't have that luxury for servers created outside the IDE, so this is a
     * feeble attempt to have our cake and eat it too :)  I'll probably regret
     * it later.
     */
    private final List<WeakReference<LoggerRunnable>> readers 
            = Collections.synchronizedList(new ArrayList<WeakReference<LoggerRunnable>>());

    private final Map<String, String> localizedLevels = getLevelMap();

    /**
     * Creates and starts a new instance of LogViewMgr
     * 
     * @param uri the uri of the server
     */
    private LogViewMgr(final String uri) {
        this.uri = uri;
        io = getServerIO(uri);
        
        if (io == null) {
            return; // finish, it looks like this server instance has been unregistered
        }
        
        // clear the old output
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            // no op
        }
    }
    
    /**
     * Returns uri specific instance of LogViewMgr
     * 
     * @param uri the uri of the server
     * @return uri specific instance of LogViewMgr
     */
    public static LogViewMgr getInstance(String uri) {
        LogViewMgr logViewMgr;
        synchronized (INSTANCES) {
            WeakReference<LogViewMgr> viewRef = INSTANCES.get(uri);
            logViewMgr = viewRef != null ? viewRef.get() : null;
            if(logViewMgr == null) {
                logViewMgr = new LogViewMgr(uri);
                INSTANCES.put(uri, new WeakReference<>(logViewMgr));
            }
        }
        return logViewMgr;
    }
    
    public void ensureActiveReader(List<Recognizer> recognizers,
            FetchLog serverLog, PayaraInstance instance) {
        synchronized (readers) {
            boolean activeReader = false;
            for(WeakReference<LoggerRunnable> ref: readers) {
                LoggerRunnable logger = ref.get();
                if(logger != null) {
                    activeReader = true;
                    break;
                }
            }

            if(!activeReader && serverLog != null) {
                readInputStreams(recognizers,
                        serverLog.getInputStream() instanceof FileInputStream,
                        instance, serverLog);
            }
        }
    }

    /**
     * Reads a newly included InputSreams.
     *
     * @param recognizers
     * @param fromFile
     * @param instance
     * @param serverLogs
     */
    public void readInputStreams(List<Recognizer> recognizers, boolean fromFile,
            PayaraInstance instance, FetchLog... serverLogs) {
        synchronized (readers) {
            stopReaders();

            for(FetchLog serverLog : serverLogs){
                // LoggerRunnable will close the stream if necessary.
                LoggerRunnable logger = new LoggerRunnable(recognizers,
                        serverLog, fromFile, instance);
                readers.add(new WeakReference<>(logger));
                Thread t = new Thread(logger);
                t.start();
            }
        }
    }
        
    public void stopReaders() {
        synchronized (readers) {
            for(WeakReference<LoggerRunnable> ref: readers) {
                LoggerRunnable logger = ref.get();
                if(logger != null) {
                    logger.stop();
                }
            }
            readers.clear();
        }
    }
    
    private void removeReader(LoggerRunnable logger) {
        synchronized (readers) {
            int size = readers.size();
            for(int i = 0; i < size; i++) {
                WeakReference<LoggerRunnable> ref = readers.get(i);
                if(logger == ref.get()) {
                    readers.remove(i);
                    break;
                }
            }
        }
    }
    
    /**
     * Writes a message into output
     * 
     * @param s message to write
     */
    public synchronized void write(String s, boolean error) {
        OutputWriter writer = getWriter(error);
        if(writer != null) {
            writer.print(s);
        }
    }

    /**
     * Writes a message into output, including a link to a portion of the
     * content being written.
     * 
     * @param s message to write
     * @param link
     * @param important
     * @param error
     */
    public synchronized void write(String s, OutputListener link, boolean important, boolean error) {
        try {
            OutputWriter writer = getWriter(error);
            if(writer != null) {
                writer.println(s, link, important);
            }
        } catch(IOException ex) {
            LOGGER.log(Level.FINE, ex.getLocalizedMessage(), ex);
        }
    }

    private OutputWriter getWriter(boolean error) {
        if (null == io) {
            return null;
        }
        OutputWriter writer = error ? io.getErr() : io.getOut();
        if(LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "getIOWriter: closed = {0} [ {1}" + " ]" + ", output error flag = " + "{2}", new Object[]{io.isClosed(), error ? "STDERR" : "STDOUT", writer.checkError()}); // NOI18N
        }
        if(writer.checkError() == true) {
            InputOutput newIO = getServerIO(uri);
            if(newIO == null) {
                if(LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.log(Level.INFO, "Unable to recreate I/O for {0}, still in error state", uri); // NOI18N
                }
                writer = null;
            } else {
                io = newIO;
                writer = error ? io.getErr() : io.getOut();
            }
        }
        return writer;
    }

    private Map<String, String> getLevelMap() {
        Map<String, String> levelMap = new HashMap<String, String>();
        for(Level l: new Level [] { Level.ALL, Level.CONFIG, Level.FINE,
                Level.FINER, Level.FINEST, Level.INFO, Level.SEVERE, Level.WARNING } ) {
            String name = l.getName();
            levelMap.put(name, l.getLocalizedName());
        }
        return levelMap;
    }

    /**
     * Selects output panel
     * @param force
     */
    public synchronized void selectIO(boolean force) {
        if(LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "selectIO: closed = {0}, output error flag = {1}", new Object[]{io.isClosed(), io.getOut().checkError()}); // NOI18N
        }

        // Only select the output window if it's closed.  This makes sure it's
        // created properly and displayed.  However, if the user minimizes the
        // output window or switches to another one, we don't switch back.
        if(io.isClosed()) {
            try {
                io.getOut().reset();
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, "ignorable problem", ex);
            }
            io.select();

            // Horrible hack.  closed flag is never reset, so reset it after reopening.
            invokeSetClosed(io, false);
        }

        if (force) {
            lastVisibleCheck = 0;
            io.select();
        }

        // If the user happened to close the OutputWindow TopComponent, reopen it.
        // Don't this check too often, but often enough.
        if(System.currentTimeMillis() > lastVisibleCheck + VISIBILITY_CHECK_DELAY) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    if(visibleCheck.getAndSet(true)) {
                        try {
                            TopComponent tc = null;
                            if(outputTCRef != null) {
                                tc = outputTCRef.get();
                            }
                            if(tc == null) {
                                tc = WindowManager.getDefault().findTopComponent(OUTPUT_WINDOW_TCID);
                                if(tc != null) {
                                    outputTCRef = new WeakReference<>(tc);
                                }
                            }
                            if(tc != null && !tc.isOpened()) {
                                tc.open();
                            }
                            lastVisibleCheck = System.currentTimeMillis();
                        } finally {
                            visibleCheck.set(false);
                        }
                    }
                }
            });
        }
    }

    private AtomicBoolean visibleCheck = new AtomicBoolean();
    private final long VISIBILITY_CHECK_DELAY = 60 * 1000;
    private final String OUTPUT_WINDOW_TCID = "output"; // NOI18N
    private volatile long lastVisibleCheck = 0;
    private WeakReference<TopComponent> outputTCRef =
            new WeakReference<>(null);
    private volatile Method setClosedMethod;

    private void invokeSetClosed(InputOutput io, boolean closed) {
        if(setClosedMethod == null) {
            setClosedMethod = initSetClosedMethod(io);
        }
        if(setClosedMethod != null) {
            try {
                setClosedMethod.invoke(io, Boolean.valueOf(closed));
            } catch (Exception ex) {
                if(LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "invokeSetClosed", ex); // NOI18N
                }
            }
        }
    }

    private Method initSetClosedMethod(InputOutput io) {
        Method method = null;
        try {
            Class clazz = io.getClass();
            method = clazz.getDeclaredMethod("setClosed", boolean.class); // NOI18N
            method.setAccessible(true);
        } catch(Exception ex) {
            if(LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "initSetClosedMethod", ex); // NOI18N
            }
        }
        return method;
    }

    private class LoggerRunnable implements Runnable {

        private final List<Recognizer> recognizers;
        private FetchLog serverLog;
        private final boolean ignoreEof;
        private volatile boolean shutdown;
        private final PayaraInstance instance;
        //private final Map<String, String> properties;
        
        public LoggerRunnable(List<Recognizer> recognizers, FetchLog serverLog, 
                boolean ignoreEof, PayaraInstance instance) {
            this.recognizers = recognizers;
            this.serverLog = serverLog;
            this.ignoreEof = ignoreEof;
            this.shutdown = false;
            this.instance = instance;
        }

        public void stop() {
            shutdown = true;
        }
        
        /**
         * Implementation of the Runnable interface. Here all tailing is
         * performed
         */
        @SuppressWarnings("SleepWhileInLoop")
        @Override
        public void run() {
            final String originalName = Thread.currentThread().getName();
            BufferedReader reader = null;
            
            try {
                Thread.currentThread().setName(this.getClass().getName()
                        + " - " + serverLog.getInputStream()); // NOI18N

                reader = new BufferedReader(new InputStreamReader(
                        serverLog.getInputStream()));

                // ignoreEof is true for log files and false for process streams.
                // FIXME Should differentiate filter types more cleanly.
                Filter filter = ignoreEof ? new LogFileFilter(localizedLevels) : 
                    (uri.contains("]deployer:pfv3ee6") ? new LogFileFilter(localizedLevels) :new StreamFilter());
                
                // read from the input stream and put all the changes to the I/O window
                char [] chars = new char[1024];
                int len = 0;

                while(!shutdown && len != -1) {
                    if(ignoreEof) {
                        reader = followLogRotation(reader);
                        // For file streams, only read if there is something there.
                        while(!shutdown && reader.ready()) {
                            String text = filter.process((char) reader.read());
                            if(text != null) {
                                processLine(text);
                            }
                        }
                    } else {
                        // For process streams, check for EOF every <DELAY> interval.
                        // We don't use readLine() here because it blocks.
                        while(!shutdown && (len = reader.read(chars)) != -1) {
                            for(int i = 0; i < len; i++) {
                                String text = filter.process(chars[i]);
                                if(text != null) {
                                    processLine(text);
                                }
                            }
                            if(!reader.ready()) {
                                break;
                            }
                        }
                    }
                    
                    // sleep for a while when the stream is empty
                    try {
                        if (ignoreEof) {
                            // read from file case... not associated with a process...
                            //     make sure there is no star
                            io.getErr().close();
                            io.getOut().close();
                        }
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "I/O exception reading server log", ex); // NOI18N
            } finally {
                serverLog.close();
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, "I/O exception closing stream buffer", ex); // NOI18N
                    }
                }
                
                removeReader(this);
                
                Thread.currentThread().setName(originalName);
            }
            io.getErr().close();
            io.getOut().close();
        }

        private void processLine(String line) {
            if(LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "processing text: ''{0}''", line); // NOI18N
            }
            // XXX sort of a hack to eliminate specific glassfish messages that
            // ought not to be printed at their current level (INFO vs FINE+).
            if(strictFilter && filter(line)) {
                return;
            }
            // Track level, color, listener
            Message message = new Message(line);
            message.process(recognizers);
                message.print();
                selectIO(false);
            if (shutdown) {
                // some messages get processed after the server has 'stopped'
                //    prevent new stars on the output caption.
                io.getErr().close();
                io.getOut().close();
            }
        }

        private synchronized BufferedReader followLogRotation(
                BufferedReader reader) {
            BufferedReader retVal = reader;
            if (instance != null && instance.getProperties() != null) {
                FetchLog newServerLog = null;
                String dir = instance.getProperty(PayaraModule.DOMAINS_FOLDER_ATTR);
                if (null == dir) {
                    // this log cannot rotate... it isn't based on a file
                    return retVal;
                }
                try {
                  newServerLog  = getServerLogStream(instance);
                  if (serverLog.getInputStream() instanceof FileInputStream
                          && newServerLog.getInputStream()
                          instanceof FileInputStream) {
                      FileInputStream fis = (FileInputStream)newServerLog
                              .getInputStream();
                      long newSize = fis.getChannel().size();
                      long oldSize = ((FileInputStream)serverLog
                              .getInputStream()).getChannel().size();
                      if (oldSize != newSize) {
                          retVal = new BufferedReader(new InputStreamReader(
                                  newServerLog.getInputStream()));
                          serverLog.close();
                          serverLog = newServerLog;
                      }
                  }
                } catch (IOException ioe) {
                    Logger.getLogger("payara").log(Level.WARNING, null, ioe);
                } finally {
                    if (null != newServerLog && !(newServerLog == serverLog)) {
                        newServerLog.close();
                    }
                }
            }
            return retVal;
        }
    }

    private static final Pattern COLOR_PATTERN = Pattern.compile(
            "\\033\\[([\\d]{1,3})(?:;([\\d]{1,3}))?(?:;([\\d]{1,3}))?(?:;([\\d]{1,3}))?(?:;([\\d]{1,3}))?m"); // NOI18N

    private static final Color LOG_RED = new Color(204, 0, 0);
    private static final Color LOG_GREEN = new Color(0, 192, 0);
    private static final Color LOG_YELLOW = new Color(204, 204, 0);
    private static final Color LOG_BLUE = Color.BLUE;
    private static final Color LOG_MAGENTA = new Color(204, 0, 204);
    private static final Color LOG_CYAN = new Color(0, 153, 255);

    private static final Color [] COLOR_TABLE = {
        Color.BLACK, LOG_RED, LOG_GREEN, LOG_YELLOW, LOG_BLUE, LOG_MAGENTA, LOG_CYAN,
    };

    private class Message {

        private String message;
        private int level;
        private Color color;
        private OutputListener listener;

        public Message(String line) {
            message = line;
        }

        void process(List<Recognizer> recognizers) {
            processLevel();
            processColors();
            processRecognizers(recognizers);
        }

        private void processLevel() {
            level = 0;
            int colon = message.substring(0, Math.min(message.length(), 15)).indexOf(':');
            if(colon != -1) {
                try {
                    String levelPrefix = message.substring(0, colon);
                    level = Level.parse(levelPrefix).intValue();
                } catch(IllegalArgumentException ex) {
                }
            }
        }

        private void processColors() {
            try {
                Matcher matcher = COLOR_PATTERN.matcher(message);
                boolean result = matcher.find();
                if(result) {
                    StringBuffer sb = new StringBuffer(message.length());
                    do {
                        int count = matcher.groupCount();
                        for(int i = 1; i < count && matcher.group(i) != null; i++) {
                            int code = Integer.parseInt(matcher.group(i));
                            if(code >= 30 && code <= 36 && color == null) {
                                color = COLOR_TABLE[code - 30];
                            }
                        }
                        matcher.appendReplacement(sb, "");
                        result = matcher.find();
                    } while(result);
                    matcher.appendTail(sb);
                    message = sb.toString();
                }
            } catch(Exception ex) {
                Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
            }
            if(color == null && level > 0) {
                if(level <= Level.FINE.intValue()) {
                    color = LOG_GREEN;
                } else if(level <= Level.INFO.intValue()) {
                    color = Color.GRAY;
                }
            }
        }

        private void processRecognizers(List<Recognizer> recognizers) {
            // Don't run recognizers on excessively long lines
            if(message.length() > 500) {
                return;
            }
            Iterator<Recognizer> iterator = recognizers.iterator();
            while(iterator.hasNext() && listener == null) {
                Recognizer r = iterator.next();
                try {
                    listener = r.processLine(message);
                } catch (Exception ex) {
                    Logger.getLogger("payara").log(Level.INFO, "Recognizer " + r.getClass().getName() + " generated an exception.", ex);
                }
            }
        }

        void print() {
            OutputWriter writer = getWriter(level >= 900);
            try {
                if(color != null && listener == null && IOColorLines.isSupported(io)) {
                    message = stripNewline(message);
                    IOColorLines.println(io, message, color);
                } else if(writer != null) {
                    if(listener != null) {
                        message = stripNewline(message);
                        writer.println(message, listener, false);
                    } else {
                        writer.print(message);
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, ex.getLocalizedMessage(), ex);
            }
        }

    }

    private boolean filter(String line) {
        return line.startsWith("INFO: Started bundle ")
                || line.startsWith("INFO: Stopped bundle ")
                || line.startsWith("INFO: ### ")
                || line.startsWith("felix.")
                || line.startsWith("log4j:")
                ;
    }

    private static String stripNewline(String s) {
        int len = s.length();
        if(len > 0 && '\n' == s.charAt(len-1)) {
            s = s.substring(0, len-1);
        }
        return s;
    }

    private static interface Filter {
        
        public String process(char c);
        
    }
    
    private static abstract class StateFilter implements Filter {
        
        protected String message;
        
        protected int state;
        protected StringBuilder msg;
        
        StateFilter() {
            state = 0;
            msg = new StringBuilder(128);
        }
        
        protected void reset() {
            message = ""; // NOI18N
        }
        
        @Override
        public abstract String process(char c);
        
    }
    
    private static final class StreamFilter extends StateFilter {

        private static final Pattern MESSAGE_PATTERN = Pattern.compile("([\\p{Lu}]{0,16}?):|([^\\r\\n]{0,24}?\\d\\d?:\\d\\d?:\\d\\d?)"); // NOI18N
        
        private String line;
        
        public StreamFilter() {
            reset();
        }

        @Override
        protected void reset() {
            super.reset();
            line = ""; // NOI18N
        }

        /**
         * Payara server log format, when read from process stream:
         *
         * Aug 13, 2008 3:01:49 PM com.sun.enterprise.glassfish.bootstrap.ASMain main
         * INFO: Launching Payara on Apache Felix OSGi platform
         * Aug 13, 2008 3:01:50 PM com.sun.enterprise.glassfish.bootstrap.ASMainHelper setUpOSGiCache
         * INFO: Removing Felix cache profile dir /space/tools/v3Aug7/domains/domain1/.felix/gf left from a previous run
         * 
         * Welcome to Felix.
         * =================
         * 
         * Aug 13, 2008 3:01:51 PM HK2Main start
         * INFO: contextRootDir = /space/tools/v3Aug7/modules
         * ...
         * Aug 13, 2008 3:02:14 PM
         * SEVERE: Exception in thread "pool-6-thread-1"
         * Aug 13, 2008 3:02:14 PM org.glassfish.scripting.rails.RailsDeployer load
         * INFO: Loading application RailsGFV3 at /RailsGFV3
         * Aug 13, 2008 3:02:14 PM
         * SEVERE: /...absolute.path.../connection_specification.rb:232:in `establish_connection':
         *
         * !PW FIXME This parser should be checked for I18N stability.
         */
        @Override
        public String process(char c) {
            String result = null;

            if(c == '\n') {
                if(msg.length() > 0) {
                    msg.append(c);
                    line = msg.toString();
                    msg.setLength(0);

                    Matcher matcher = MESSAGE_PATTERN.matcher(line);
                    if(matcher.find() && matcher.start() == 0 && matcher.groupCount() > 1 && matcher.group(2) != null) {
                        result = null;
                    } else {
                        result = line;
                    }
                }
            } else if(c != '\r') {
                msg.append(c);
            }

            return result;
        }

    }

    private static final class LogFileFilter extends StateFilter {
        
        private String time;
        private String type;
        private String version;
        private String classinfo;
        private String threadinfo;
        private boolean multiline;
        private final Map<String, String> typeMap;

        public LogFileFilter(Map<String, String> typeMap) {
            this.typeMap = typeMap;
            reset();
        }

        @Override
        protected void reset() {
            super.reset();
            time = type = version = classinfo = threadinfo = ""; // NOI18N
            multiline = false;
        }
        
        private String getLocalizedType(String type) {
            String localizedType = typeMap.get(type);
            return localizedType != null ? localizedType : type;
        }

        /**
         * Payara server log entry format (unformatted), when read from file:
         *
         * [#|
         *    2008-07-20T16:59:11.738-0700|
         *    INFO|
         *    Payara10.0|
         *    org.jvnet.hk2.osgiadapter|
         *    _ThreadID=11;_ThreadName=Thread-6;org.glassfish.admin.config-api [1794];|
         *    Started bundle org.glassfish.admin.config-api [1794]
         * |#]
         *
         * !PW FIXME This parser should be checked for I18N stability.
         */
        @Override
        public String process(char c) {
            String result = null;

            switch(state) {
                case 0:
                    if(c == '[') {
                        state = 1;
                    } else {
                        if(c == '\n') {
                            if(msg.length() > 0) {
                                msg.append(c);
                                result = msg.toString();
                                msg.setLength(0);
                            }
                        } else if(c != '\r') {
                            msg.append(c);
                        }
                    }
                    break;
                case 1:
                    if(c == '#') {
                        state = 2;
                    } else {
                        state = 0;
                        if(c == '\n') {
                            if(msg.length() > 0) {
                                msg.append(c);
                                result = msg.toString();
                                msg.setLength(0);
                            }
                        } else if(c != '\r') {
                            msg.append('[');
                            msg.append(c);
                        }
                    }
                    break;
                case 2:
                    if(c == '|') {
                        state = 3;
                        msg.setLength(0);
                    } else {
                        if(c == '\n') {
                            if(msg.length() > 0) {
                                msg.append(c);
                                result = msg.toString();
                                msg.setLength(0);
                            }
                        } else if(c != '\r') {
                            state = 0;
                            msg.append('[');
                            msg.append('#');
                            msg.append(c);
                        }
                    }
                    break;
                case 3:
                    if(c == '|') {
                        state = 4;
                        time = msg.toString();
                        msg.setLength(0);
                    } else {
                        msg.append(c);
                    }
                    break;
                case 4:
                    if(c == '|') {
                        state = 5;
                        type = getLocalizedType(msg.toString());
                        msg.setLength(0);
                    } else {
                        msg.append(c);
                    }
                    break;
                case 5:
                    if(c == '|') {
                        state = 6;
                        version = msg.toString();
                        msg.setLength(0);
                    } else {
                        msg.append(c);
                    }
                    break;
                case 6:
                    if(c == '|') {
                        state = 7;
                        classinfo = msg.toString();
                        msg.setLength(0);
                    } else {
                        msg.append(c);
                    }
                    break;
                case 7:
                    if(c == '|') {
                        state = 8;
                        threadinfo = msg.toString();
                        msg.setLength(0);
                    } else {
                        msg.append(c);
                    }
                    break;
                case 8:
                    if(c == '|') {
                        state = 9;
                        message = msg.toString();
                    } else if(c == '\n') {
                        if(msg.length() > 0) { // suppress blank lines in multiline messages
                            msg.append('\n');
                            result = !multiline ? type + ": " + msg.toString() : msg.toString(); // NOI18N
                            multiline = true;
                            msg.setLength(0);
                        }
                    } else if(c != '\r') {
                        msg.append(c);
                    }
                    break;
                case 9:
                    if(c == '#') {
                        state = 10;
                    } else {
                        state = 8;
                        msg.append('|');
                        msg.append(c);
                    }
                    break;
                case 10:
                    if(c == ']') {
                        state = 0;
                        msg.setLength(0);
                        result = (multiline ? message : type + ": " + message) + '\n'; // NOI18N
                        reset();
                    } else {
                        state = 8;
                        msg.append('|');
                        msg.append('#');
                        msg.append(c);
                    }
                    break;
            }
            return result;
        }
    }
    
    private static final WeakHashMap<ServerInstance, InputOutput> ioWeakMap = 
            new WeakHashMap<ServerInstance, InputOutput>();
    
    public static InputOutput getServerIO(String uri) {

        ServerInstance si = PayaraInstanceProvider.getInstanceByUri(uri);
        if(null == si) {
            return null;
        }

        synchronized (ioWeakMap) {
            // look in the cache
            InputOutput serverIO = ioWeakMap.get(si);
            if(serverIO != null) {
                boolean valid = true;
                if(serverIO.isClosed()) {
                    if(LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Output window for {0} is closed.", uri); // NOI18N
                    }
                }
                if(serverIO.getOut().checkError()) {
                    if(LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Standard out for {0} is in error state.", uri); // NOI18N
                    }
                    valid = false;
                }
                if(serverIO.getErr().checkError()) {
                    if(LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Standard error for {0} is in error state.", uri); // NOI18N
                    }
                    valid = false;
                }

                if(valid) {
                    return serverIO;
                } else {
                    if(!serverIO.isClosed()) {
                        serverIO.closeInputOutput();
                    }
                    ioWeakMap.put(si, null);
                }
            }
        }

        // look up the node that belongs to the given server instance
        Node node = si.getFullNode();

        // it looks like that the server instance has been removed
        if (node == null) {
            return null;
        }

        // No server control interface...
        PayaraModule commonSupport = node.getLookup().lookup(PayaraModule.class);
        if(commonSupport == null) {
            return null;
        }

        Action[] actions = new Action[] {
            new StartServerAction.OutputAction(commonSupport),
            new DebugAction.OutputAction(commonSupport),
            new RestartAction.OutputAction(commonSupport),
            new StopServerAction.OutputAction(commonSupport),
            new RefreshAction.OutputAction(commonSupport)
        };

        InputOutput newIO;
        synchronized (ioWeakMap) {
            newIO = ioWeakMap.get(si);
            if(newIO == null) {
                newIO = IOProvider.getDefault().getIO(si.getDisplayName(), actions);
                ioWeakMap.put(si, newIO);
            }
        }
        return newIO;
    }

    static public void displayOutput(PayaraInstance instance, Lookup lookup) {
        String uri = instance.getProperty(PayaraModule.URL_ATTR);
        if (null != uri && (uri.contains("pfv3ee6wc") || uri.contains("localhost"))) {
                FetchLog log = getServerLogStream(instance);
                LogViewMgr mgr = LogViewMgr.getInstance(uri);
                List<Recognizer> recognizers = new ArrayList<Recognizer>();
                if (null != lookup) {
                    recognizers = getRecognizers(lookup.lookupAll(RecognizerCookie.class));
                }
                mgr.ensureActiveReader(recognizers, log, instance);
                mgr.selectIO(true);
        }
    }

    static private List<Recognizer> getRecognizers(Collection<? extends RecognizerCookie> cookies) {
        List<Recognizer> recognizers;
        if(!cookies.isEmpty()) {
            recognizers = new LinkedList<Recognizer>();
            for(RecognizerCookie cookie: cookies) {
                recognizers.addAll(cookie.getRecognizers());
            }
            recognizers = Collections.unmodifiableList(recognizers);
        } else {
            recognizers = Collections.emptyList();
        }
        return recognizers;
    }

    /**
     * Log fetcher state change listener.
     */
    private static class LogStateListener implements FetchLogEventListener {

        /** Payara server instance associated with log fetcher
         *  and it's state change listener. */
        private final PayaraInstance instance;

        /** Log fetcher associated with log fetcher state change listener. */
        private final FetchLogPiped log;

        /**
         * Creates an instance of log fetcher state change listener.
         * @param instance Payara server instance associated with log fetcher
         *                 and it's state change listener.
         * @param log Log fetcher associated with log fetcher state change
         *            listener.
         */
        LogStateListener(PayaraInstance instance, FetchLogPiped log) {
            this.instance = instance;
            this.log = log;
        }

        /**
         * Remove log fetcher from instance to log fetcher mapping
         * when log fetcher task is finished or has failed.
         * <p/>
         * @param event Payara log fetcher state change event.
         */
        @Override
        public void stateChanged(final FetchLogEvent event) {
            switch (event.getState()) {
                case COMPLETED: case FAILED:
                    FetchLog oldLog = null;
                    synchronized (serverInputStreams) {
                        FetchLog storedLog = serverInputStreams.get(instance);
                        if (this.log.equals(storedLog)) {
                            oldLog = serverInputStreams.remove(instance);
                        }
                    }
                    if (oldLog != null) {
                        oldLog.close();
                    }
            }
        }
        
    }

    /** Internal Payara server instance to log fetcher mapping.*/
    private static final Map<PayaraInstance, FetchLog> serverInputStreams
            = new HashMap<PayaraInstance, FetchLog>();

    /**
     * Add log fetcher into local instance to fetcher mapping.
     * <p/>
     * Log fetcher life cycle is linked with internal fetcher mapping. Fetcher
     * removed from map must be cleaned up.
     * Do not access internal Payara server instance to log fetcher mapping
     * without using those access methods!
     * <p/>
     * @param instance Payara server instance used as key in local
     *                 instance to fetcher mapping.
     * @param log New Payara log fetcher for given server instance
     *            to be stored into mapping.
     */
    private static void addLog(final PayaraInstance instance,
            final FetchLogPiped log) {
        FetchLog oldLog;
        synchronized (serverInputStreams) {
            oldLog = serverInputStreams.put(instance, log);
        }
        log.addListener(new LogStateListener(instance, log));
        if (oldLog != null) {
            oldLog.close();
        }
    }

    /**
     * Remove log fetcher from local instance to fetcher mapping.
     * <p/>
     * Log fetcher life cycle is linked with internal fetcher mapping. Fetcher
     * removed from map must be cleaned up.
     * Do not access internal Payara server instance to log fetcher mapping
     * without using those access methods!
     * <p/>
     * @param instance Payara server instance used as key in local
     *                 instance to fetcher mapping.
     * @param log New Payara log fetcher for given server instance
     *            to be stored into mapping.
     */
    public static void removeLog(final PayaraInstance instance) {
        FetchLog oldLog;
        synchronized (serverInputStreams) {
            oldLog = serverInputStreams.remove(instance);
        }
        if (oldLog != null) {
            oldLog.close();
        }
    }
    
    /**
     * Get Payara stored log fetcher for given server instance.
     * <p/>
     * Payara log fetchers are reused so only one log fetcher exists for
     * each running server instance.
     * <p/>
     * @param instance Payara server instance used as key to retrieve
     *                 log fetcher.
     * @return Payara log fetcher stored for given server instance or newly
     *         cerated one when no log fetcher was found.
     * @throws IOException 
     */
    static private FetchLog getServerLogStream(
            final PayaraInstance instance) {
        FetchLog log;
        FetchLog deadLog = null;
        synchronized (serverInputStreams) {
            log = serverInputStreams.get(instance);
            if (log != null) {
                if (log instanceof FetchLogPiped) {
                    // Log reading task in running state
                    if (((FetchLogPiped) log).isRunning()) {
                        return log;
                    // Log reading task is dead
                    } else {
                        // Postpone cleanup after synchronized block.
                        deadLog = log;
                        removeLog(instance);
                    }
                } else {
                    return log;
                }
            }
            log = FetchLogPiped.create(
                    PayaraExecutors.fetchLogExecutor(), instance);
            addLog(instance, (FetchLogPiped)log);
        }
        if (deadLog != null) {
            deadLog.close();
        }
        return log;
    }

}
