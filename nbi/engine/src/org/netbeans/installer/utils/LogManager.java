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

package org.netbeans.installer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 *
 * @author Kirill Sorokin
 */
public final class LogManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static final int DEFAULT_LOG_LEVEL =
            ErrorLevel.DEBUG;
    public static final boolean DEFAULT_LOG_TO_CONSOLE =
            true;
    
    private static File logFile;
    private static PrintWriter logWriter;
    private static int logLevel = DEFAULT_LOG_LEVEL;
    private static boolean logToConsole = DEFAULT_LOG_TO_CONSOLE;
    
    private static int indent;
    
    private static boolean started = false;
    
    private static List<String> logCache = new LinkedList<String>();
    
    public static synchronized void start() {
        // check for custom log level
        if (System.getProperty(LOG_LEVEL_PROPERTY) != null) {
            try {
                logLevel = Integer.parseInt(System.getProperty(LOG_LEVEL_PROPERTY));
            } catch (NumberFormatException e) {
                logLevel = DEFAULT_LOG_LEVEL;
            }
        } else {
            logLevel = DEFAULT_LOG_LEVEL;
        }
        
        initializeConsoleLogging();
        
        // init the log file and streams
        try {
            if(logFile!=null) {
                logFile.getParentFile().mkdirs();
                if(logFile.exists()) {
                   logFile.delete();
                }
                logFile.createNewFile();                
                logWriter = new PrintWriter(new FileWriter(logFile));
                System.setProperty(LOG_FILE_PROPERTY, logFile.getAbsolutePath());
            }
            
            // here is a small assumption that there will be no calls to log*(*)
            // during the cache dumping. Otherwise we'll get a concurrent
            // modification exception
            for (String string: logCache) {
                write(string);
            }
            logCache.clear();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            logWriter = null;
        }
        
        started = true;
    }
    private static void initializeConsoleLogging () {
        // check whether we should log to console as well
        if (System.getProperty(LOG_TO_CONSOLE_PROPERTY) != null) {
            logToConsole = Boolean.parseBoolean(System.getProperty(LOG_TO_CONSOLE_PROPERTY));
        } else {
            logToConsole = DEFAULT_LOG_TO_CONSOLE;
        }
    }
    
    public static synchronized void stop() {
        started = false;
        stopFileLog();
        stopConsoleLog();
    }

    private static final void stopConsoleLog() {
        // can happen if log manager haven`t started yet - dump to console everything cached
        if (!logCache.isEmpty()) {
            initializeConsoleLogging();
            if (logToConsole) {
                for (String string : logCache) {
                    try {
                        write(string);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                logCache.clear();
            }
        }
    }

    private static synchronized void stopFileLog() {
        if (logWriter != null) {
            logWriter.close();
            logWriter = null;
        }
    }
    
    public static synchronized void indent() {
        indent++;
    }
    
    public static synchronized void unindent() {
        indent--;
    }
    
    public static synchronized void log(int level, String message) {
        if (level <= logLevel) {
            BufferedReader reader = new BufferedReader(new StringReader(message));
            
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    String string =
                            "[" + DateUtils.getFormattedTimestamp() + "]: " +
                            StringUtils.pad(INDENT, indent) + line;
                    
                    if (started) {
                        write(string);
                    } else {
                        logCache.add(string);
                    }
		    //System.out.println(message);
                }
            } catch (IOException e) {
                logWriter = null;
                ErrorManager.notifyWarning(
                        ResourceUtils.getString(LogManager.class, 
                        ERROR_CANNOT_WRITE_KEY));
            }
        }
    }
    
    public static synchronized void log(int level, Throwable exception) {
        log(level, StringUtils.asString(exception));
    }
    
    public static synchronized void log(int level, Object object) {
        log(level, object.toString());
    }
    
    public static synchronized void log(String message) {
        log(ErrorLevel.MESSAGE, message);
    }
    
    public static synchronized void log(Throwable exception) {
        log(ErrorLevel.MESSAGE, exception);
    }
    
    public static synchronized void log(Object object) {
        log(ErrorLevel.MESSAGE, object);
    }
    
    public static synchronized void log(String message, Throwable exception) {
        log(message);
        log(exception);
    }
    
    public static synchronized void logEntry(String message) {
        final StackTraceElement traceElement =
                Thread.currentThread().getStackTrace()[3];
        
        log(ErrorLevel.DEBUG,
                "entering -- " +
                (traceElement.isNativeMethod() ? "[native] " : "") +
                traceElement.getClassName() + "." +
                traceElement.getMethodName() + "():" +
                traceElement.getLineNumber());
        log(ErrorLevel.MESSAGE, message);
        indent();
    }
    
    public static synchronized void logExit(String message) {
        StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
        
        unindent();
        log(message);
        log(ErrorLevel.DEBUG, "exiting -- " +
                (traceElement.isNativeMethod() ? "[native] " : "") +
                traceElement.getClassName() + "." +
                traceElement.getMethodName() + "():" +
                traceElement.getLineNumber());
    }
    
    public static synchronized void logIndent(String message) {
        log(message);
        indent();
    }
    
    public static synchronized void logUnindent(String message) {
        unindent();
        log(message);
    }
    
    public static File getLogFile() {
        return logFile;
    }
    
    public static void setLogFile(final File logFile) {
        LogManager.logFile = logFile;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static void write(String string) throws IOException {
        if (logWriter != null) {
            logWriter.println(string);
            logWriter.flush();
        }
        
        if (logToConsole) {
            System.out.println(string);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private LogManager() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String LOG_LEVEL_PROPERTY =
            "nbi.utils.log.level"; // NOI18N
    public static final String LOG_TO_CONSOLE_PROPERTY =
            "nbi.utils.log.to.console"; // NOI18N
    public static final String LOG_FILE_PROPERTY =
            "nbi.utils.log.file"; // NOI18N
    
    public static final String INDENT =
            "    "; // NOI18N
    public static final String ERROR_CANNOT_WRITE_KEY = 
            "LM.error.cannot.write";//NOI18N
}
