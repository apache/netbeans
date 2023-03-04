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

package org.netbeans.core.startup;

import java.io.IOException;
import java.util.Stack;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

/** Logger that will enable the logging of important events during the startup
 * annotated with real time and possibly time differences.
 *
 * @author Petr Nejedly, Jesse Glick
 */
public class StartLog {
    private static final Logger LOG = Logger.getLogger("org.netbeans.log.startup"); // NOI18N
    private static final Stack<String> actions = new Stack<String>();
    private static final Stack<Throwable> places = new Stack<Throwable>();
    private static final boolean DEBUG_NESTING = Boolean.getBoolean("org.netbeans.log.startup.debug"); // NOI18N
    
    private static final String logProp; 
    private static final String logFileProp;
    static final Handler impl;
    
    static {
        try {
            logProp = System.getProperty( "org.netbeans.log.startup" ); // NOI18N
            // NOI18N
            logFileProp = System.getProperty( "org.netbeans.log.startup.logfile" ); // NOI18N

            if(logProp == null)
                impl = new StartImpl();
            else if("print".equals(logProp))
                impl = new PrintImpl();
            else if("tests".equals(logProp))
                impl = new PerformanceTestsImpl();
            else if("xml".equals(logProp)) {
                if (logFileProp == null) {
                    throw new NullPointerException("Specify also 'org.netbeans.log.startup.logfile' property!"); // NOI18N
                }
                FileHandler h = new FileHandler(logFileProp);
                h.setFormatter(new SimplerFormatter());
                impl = h;
            } else
                throw new Error("Unknown org.netbeans.log.startup value [" + logProp + "], it should be (print or tests or xml) !"); // NOI18N
            register();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    static void register() {
        LOG.setUseParentHandlers(false);
        LOG.addHandler(impl);
        LOG.setLevel(impl.getLevel());
    }
    
    static void unregister() {
        LOG.removeHandler(impl);
    }
    
    /** Start running some interval action.
     * @param action some identifying string
     */
    public static void logStart( String action ) {
        if (willLog()) {
            LOG.log(Level.FINE, "start", action); // NOI18N
            actions.push(action);
            if (DEBUG_NESTING) {
                places.push(new Throwable("logStart called here:")); // NOI18N
            }
        }
    }

    /** Note that something happened, but not an interval.
     * The log will note only the time elapsed since the last interesting event.
     * @param note some identifying string
     */
    public static void logProgress( String note ) {
        if( willLog() ) {
            LOG.log(Level.FINE, "progress", note); // NOI18N
        }
    }

    /** Stop running some interval action.
     * The log will note the time elapsed since the start of the action.
     * Actions <em>must</em> be properly nested.
     * @param action some identifying string
     */
    public static void logEnd( String action ) {
        if (willLog()) {
            String old = actions.empty() ? null : actions.pop();
            Throwable oldplace = DEBUG_NESTING && !places.empty() ? places.pop() : null;
            if (!action.equals(old)) {
                // Error, not ISE; don't want this caught and reported
                // with ErrorManager, for then you get a wierd cycle!
                if (oldplace != null) {
                    oldplace.printStackTrace();
                } else {
                    System.err.println("Either ending too soon, or no info about caller of unmatched start log."); // NOI18N
                    System.err.println("Try running with -J-Dorg.netbeans.log.startup.debug=true"); // NOI18N
                }
                Error e = new Error("StartLog mismatch: ending '" + action + "' but expecting '" + old + "'; rest of stack: " + actions); // NOI18N
                // Print stack trace since you can get strange situations
                // when ErrorManager tries to report it - may need to initialize
                // ErrorManager, etc.
                e.printStackTrace();
                // Presumably you did want to keep on going at this point.
                System.err.flush();
                LOG.setLevel(Level.OFF);
            }
            LOG.log(Level.FINE, "end", action); // NOI18N
        }
    }

    public static boolean willLog() {
        return LOG.isLoggable(Level.FINE);
    }
    
    /** Logs the startup time. The begining is tracked by this class. 
     *  The end is passed as argument.
     */
    public static void logMeasuredStartupTime(long end){
        LOG.log(Level.FINE, "finish", end);
        if("tests".equals(logProp)) {
            impl.flush();
        }            
    }
    
    /** The dummy, no-op implementation */
    private static class StartImpl extends Handler {
        final long zero = System.nanoTime()/1000000;
        
        StartImpl() {}
        void start( String action, long time ) {}
        void progress( String note, long time ) {}
        void end( String action, long time ) {}
        boolean willLog() {
            return false;
        }

        @Override
        public Level getLevel() {
            return willLog() ? Level.FINEST : Level.OFF;
        }


        @Override
        public void publish(LogRecord rec) {
            Object[] args = rec.getParameters();
            String msg = (args.length >= 1 && args[0] instanceof String) ? (String)args[0] : ""; // NOI18N
            long time = System.nanoTime()/1000000;
            if ("start".equals(rec.getMessage())) { // NOI18N
                start(msg, time);
                return;
            }
            if ("end".equals(rec.getMessage())) { // NOI18N
                end(msg, time);
                return;
            }
            if ("progress".equals(rec.getMessage())) { // NOI18N
                progress(msg, time);
                return;
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public final void close() throws SecurityException {
        }
    }

    private static class PrintImpl extends StartImpl {
        PrintImpl() {}
        private Stack<Long> starts = new Stack<Long>();
        long prog;
        private int indent = 0;
        
        @Override
        synchronized void start( String action, long time ) {
            starts.push(time);
            prog=time;
            System.err.println( getIndentString(indent) + "@" + 
                    (time - zero) + " - " + action + " started" // NOI18N
            );
            indent += 2;
        }
        
        @Override
        synchronized void progress( String note, long time ) {
            System.err.println( getIndentString(indent) + "@" + 
                    (time - zero) + " - " + note + " dT=" + (time - prog) // NOI18N
            );
            prog = time;
        }
        
        @Override
        synchronized void end( String action, long time ) {
            indent -= 2;
            long start = starts.pop();
            prog = time;
            System.err.println( getIndentString(indent) + "@" + 
                    (time - zero) + " - " + action + " finished, took " + // NOI18N
                    (time - start) + "ms" // NOI18N
            );
        }
        
        @Override
        boolean willLog() {
            return true;
        }
        
        private char[] spaces = new char[0];
        private String getIndentString( int indent ) {
            if( spaces.length < indent ) {
                spaces = new char[Math.max( spaces.length*2, indent+10 )];
                Arrays.fill( spaces, ' ');
            }
            return new String(spaces,0, indent);
        }
    }

    private static class PerformanceTestsImpl extends StartImpl {
        private StringBuffer logs = new StringBuffer();
        private Stack<Long> starts = new Stack<Long>();
        long prog;
        private int indent = 0;
        
        PerformanceTestsImpl() {}
        
        @Override
        synchronized void start( String action, long time ) {
            starts.push(time);
            prog=time;
            log(getIndentString(indent) + "@" + 
                    (time - zero) + " - " + action + " started" // NOI18N
            );
            indent += 2;
        }
        
        @Override
        synchronized void progress( String note, long time ) {
            log(getIndentString(indent) + "@" + 
                    (time - zero) + " - " + note + " dT=" + (time - prog) // NOI18N
            );
            prog = time;
        }
        
        @Override
        synchronized void end( String action, long time ) {
            indent -= 2;
            long start = starts.pop();
            prog = time;
            log(getIndentString(indent) + "@" + 
                    (time - zero) + " - " + action + " finished, took " + // NOI18N
                    (time - start) + "ms" // NOI18N
            );
        }
        
        @Override
        boolean willLog() {
            return true;
        }
        
        private char[] spaces = new char[0];
        private String getIndentString( int indent ) {
            if( spaces.length < indent ) {
                spaces = new char[Math.max( spaces.length*2, indent+10 )];
                Arrays.fill( spaces, ' ');
            }
            return new String(spaces,0, indent);
        }
        
        synchronized void log(String log){
            logs.append("\n" + log);
        }

        @Override
        public void publish(LogRecord rec) {
            super.publish(rec);
            if ("finish".equals(rec.getMessage())) { // NOI18N
                long end = (Long)rec.getParameters()[0];
                log("IDE starts t = " + Long.toString(zero) + "\nIDE is running t=" + Long.toString(end) + "\n");
            }
        }
        
        @Override
        public synchronized void flush(){
            if(logFileProp!=null){
                try{
                    java.io.File logFile = new java.io.File(logFileProp);
                    java.io.FileWriter writer = new java.io.FileWriter(logFile);
                    writer.write(logs.toString());
                    writer.close();
                }catch (Exception exc){
                    System.err.println("EXCEPTION rises during startup logging:");
                    exc.printStackTrace(System.err);
                }
            } else
                throw new IllegalStateException("You are trying to log startup logs to unexisting file. You have to set property org.netbeans.log.startup.logfile.");
        }

    }

    private static class SimplerFormatter extends XMLFormatter {
        @Override
        public String getHead(Handler h) {
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\"");
            sb.append(" encoding='UTF-8'?>\n");
            sb.append("<log>\n");
            return sb.toString();
        }
    }
}
