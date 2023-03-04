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

package org.netbeans.core;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.core.startup.TopLogging;
import org.openide.util.UserQuestionException;

/** Wraps errormanager with logger.
 *
 * @author Jaroslav Tulach, Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=java.util.logging.Handler.class)
public final class NbErrorManager extends Handler {

    public NbErrorManager() {
        // Try to avoid a ClassCircularityError by preloading Exc.class:
        new Exc(null, null, null, null);
    }
    
    static Exc createExc(Throwable t, Level severity, LogRecord add) {
        LogRecord[] ann = findAnnotations(t, add);
        return new Exc(t, severity, ann, findAnnotations0(t, add, true, new HashSet<Throwable>()));
    }

    public void publish(LogRecord record) {
        if (record.getThrown() != null) {
            Level level = record.getLevel();
            if (level.intValue() == Level.WARNING.intValue() + 1) {
                // unknown level
                level = null;
            }
            if (level != null && level.intValue() == Level.SEVERE.intValue() + 1) {
                // unknown level
                level = null;
            }
            Exc ex = createExc(record.getThrown(), level, record.getLevel().intValue() == 1973 ? record : null);
            NotifyExcPanel.notify(ex);
        }
    }
    
    public void flush() {
        //logWriter.flush();
    }
    
    public void close() throws SecurityException {
        // nothing needed
    }
    
    /** Extracts localized message from a LogRecord */
    private static final String getLocalizedMessage(LogRecord rec) {
        ResourceBundle rb = rec.getResourceBundle();
        if (rb == null) {
            return null;
        }
        
        String msg = rec.getMessage();
        if (msg == null) {
            return null;
        }
        
        String format = rb.getString(msg);

        Object[] arr = rec.getParameters();
        if (arr == null) {
            return format;
        }

        return MessageFormat.format(format, arr);
    }

    /** Finds annotations associated with given exception.
     * @param t the exception
     * @return array of annotations or null
     */
    private static LogRecord[] findAnnotations(Throwable t, LogRecord add) {
        return findAnnotations0(t, add, false, new HashSet<Throwable>());
    }
    
    /** If recursively is true it is not adviced to print all annotations
     * because a lot of warnings will be printed. But while searching for
     * localized message we should scan all the annotations (even recursively).
     */
    private static LogRecord[] findAnnotations0(Throwable t, LogRecord add, boolean recursively, Set<Throwable> alreadyVisited) {
        List<LogRecord> l = new ArrayList<LogRecord>();
        Throwable collect = t;
        while (collect != null) {
            if (collect instanceof Callable) {
                Object res = null;
                try {
                    res = ((Callable) collect).call();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (res instanceof LogRecord[]) {
                    LogRecord[] arr = (LogRecord[])res;
                    l.addAll(Arrays.asList(arr));
                }
            }
            collect = collect.getCause();
        }

        if (add != null) {
            l.add(add);
        }

        
        if (recursively) {
            ArrayList<LogRecord> al = new ArrayList<LogRecord>();
            for (LogRecord ano : l) {
                Throwable t1 = ano.getThrown();
                if ((t1 != null) && (! alreadyVisited.contains(t1))) {
                    alreadyVisited.add(t1);
                    LogRecord[] tmpAnnoArray = findAnnotations0(t1, null, true, alreadyVisited);
                    if ((tmpAnnoArray != null) && (tmpAnnoArray.length > 0)) {
                        al.addAll(Arrays.asList(tmpAnnoArray));
                    }
                }
            }
            l.addAll(al);
        }

        Throwable cause = t.getCause();
        if (cause != null) {
            LogRecord[] extras = findAnnotations0(cause, null, true, alreadyVisited);
            if (extras != null && extras.length > 0) {
                l.addAll(Arrays.asList(extras));
            }
        }
        
        LogRecord[] arr;
        arr = new LogRecord[l.size()];
        l.toArray(arr);
        
        return arr;
    }
    
    /**
     * Another final class that is used to communicate with
     * NotifyExcPanel and provides enough information to the dialog.
     */
    static final class Exc {
        /** the original throwable */
        private final Throwable t;
        private Date d;
        final LogRecord[] arr;      // Accessed from tests
        final LogRecord[] arrAll;   // all - recursively, accessed from tests
        private Level severity;
        
        /** @param severity if -1 then we will compute the
         * severity from annotations
         */
        Exc(Throwable t, Level severity, LogRecord[] arr, LogRecord[] arrAll) {
            this.t = t;
            this.severity = severity;
            this.arr = arr == null ? new LogRecord[0] : arr;
            this.arrAll = arrAll == null ? new LogRecord[0] : arrAll;
        }
        
        /** @return message */
        String getMessage() {
            String m = t.getMessage();
            if (m != null) {
                return m;
            }
            return (String)find(1);
        }
        
        String getFirstStacktraceLine(){
            StackTraceElement[] elems = t.getStackTrace();
            if ((elems == null) || (elems.length == 0)){
                return null;
            }
            StackTraceElement elem = elems[0];
            return elem.getClassName() + "." + elem.getMethodName(); // NOI18N
        }
        
        /** @return localized message */
        String getLocalizedMessage() {
            String m = t.getLocalizedMessage();
            if (m != null && !m.equals(t.getMessage())) {
                return m;
            }
            if (arrAll == null) {
                // arrAll not filled --> use the old non recursive variant
                return (String)find(2);
            }
            for (int i = 0; i < arrAll.length; i++) {
                String s = NbErrorManager.getLocalizedMessage(arrAll[i]);
                if (s != null) {
                    return s;
                }
            }
            return m;
        }
        
        boolean isLocalized() {
            String m = t.getLocalizedMessage();
            if (m != null && !m.equals(t.getMessage())) {
                return true;
            }
            if (arrAll == null) {
                // arrAll not filled --> use the old non recursive variant
                return (String)find(2) != null;
            }
            for (int i = 0; i < arrAll.length; i++) {
                String s = NbErrorManager.getLocalizedMessage(arrAll[i]);
                if (s != null) {
                    return true;
                }
            }
            return false;
        }
        
        final boolean isUserQuestion() {
            return t instanceof UserQuestionException;
        }
        
        final void  confirm() throws IOException {
            ((UserQuestionException)t).confirmed();
        }
        
        /** @return class name of the exception */
        String getClassName() {
            return (String)find(3);
        }
        
        /** @return the severity of the exception */
        Level getSeverity() {
            if (severity != null) {
                return severity;
            }
            
            LogRecord[] anns = (arrAll != null) ? arrAll : arr;
            for (int i = 0; i < anns.length; i++) {
                Level s = anns[i].getLevel();
                if (severity == null || s.intValue() > severity.intValue()) {
                    severity = s;
                }
            }
            
            if (severity == null || severity == Level.ALL) {
                // no severity specified, assume this is an error
                severity = t instanceof Error ? Level.SEVERE : Level.WARNING;
            }
            
            return severity;
        }
        
        /** @return date assigned to the exception */
        Date getDate() {
            if (d == null) {
                d = (Date)find(4);
            }
            return d;
        }
        
        void printStackTrace(PrintStream ps) {
            printStackTrace(new PrintWriter(new OutputStreamWriter(ps)));
        }
        /** Prints stack trace of all annotations and if
         * there is no annotation trace then of the exception
         */
        void printStackTrace(PrintWriter pw) {
            // #19487: don't go into an endless loop here
            printStackTrace(pw, new HashSet<Throwable>(10));
        }
        
        private void printStackTrace(PrintWriter pw, Set<Throwable> nestingCheck) {
            if (t != null && !nestingCheck.add(t)) {
                // Unlocalized log message - this is for developers of NB, not users
                Logger l = Logger.getAnonymousLogger();
                l.warning("WARNING - ErrorManager detected cyclic exception nesting:"); // NOI18N
                for (Throwable thrw : nestingCheck) {
                    l.warning("\t" + thrw); // NOI18N
                    LogRecord[] anns = findAnnotations(thrw, null);
                    if (anns != null) {
                        for (int i = 0; i < anns.length; i++) {
                            Throwable t2 = anns[i].getThrown();
                            if (t2 != null) {
                                l.warning("\t=> " + t2); // NOI18N
                            }
                        }
                    }
                }
                l.warning("Be sure not to annotate an exception with itself, directly or indirectly."); // NOI18N
                return;
            }
            /*Heaeder
            pw.print (getDate ());
            pw.print (": "); // NOI18N
            pw.print (getClassName ());
            pw.print (": "); // NOI18N
            String theMessage = getMessage();
            if (theMessage != null) {
                pw.print(theMessage);
            } else {
                pw.print("<no message>"); // NOI18N
            }
            pw.println ();
             */
            /*Annotations */
            for (LogRecord rec : arr) {
                if (rec == null) {
                    continue;
                }
                
                Throwable thr = rec.getThrown();
                String annotation = NbErrorManager.getLocalizedMessage(rec);
                
                if (annotation == null) {
                    annotation = rec.getMessage();
                }
                /*
                if (annotation == null && thr != null) annotation = thr.getLocalizedMessage();
                if (annotation == null && thr != null) annotation = thr.getMessage();
                 */
                
                if (annotation != null) {
                    if (thr == null) {
                        pw.println("Annotation: "+annotation);// NOI18N
                    }
                    //else pw.println ("Nested annotation: "+annotation);// NOI18N
                }
            }
            
            // ok, print trace of the original exception too
            // Attempt to show an annotation indicating where the exception
            // was caught. Not 100% reliable but often helpful.
            if (t instanceof VirtualMachineError) {
                // Decomposition may not work here, e.g. for StackOverflowError.
                // Play it safe.
                t.printStackTrace(pw);
            } else {
                TopLogging.printStackTrace(t, pw);
            }
            /*Nested annotations */
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    continue;
                }
                
                Throwable thr = arr[i].getThrown();
                if (thr != null) {
                    LogRecord[] ans = findAnnotations(thr, null);
                    Exc ex = new Exc(thr, null, ans, null);
                    pw.println("==>"); // NOI18N
                    ex.printStackTrace(pw, nestingCheck);
                }
            }
        }

        /**
         * Method that iterates over annotations to find out
         * the first annotation that brings the requested value.
         *
         * @param kind what to look for (1, 2, 3, 4, ...);
         * @return the found object
         */
        private Object find(int kind) {
            return find(kind, true);
        }
        
        /**
         * Method that iterates over annotations to find out
         * the first annotation that brings the requested value.
         *
         * @param kind what to look for (1, 2, 3, 4, ...);
         * @return the found object
         */
        private Object find(int kind, boolean def) {
            for (int i = 0; i < arr.length; i++) {
                LogRecord a = arr[i];
                
                Object o = null;
                switch (kind) {
                    case 1: // message
                        o = a.getMessage(); break;
                    case 2: // localized
                        o = NbErrorManager.getLocalizedMessage(a); break;
                    case 3: // class name
                    {
                        Throwable t = a.getThrown();
                        o = t == null ? null : t.getClass().getName();
                        break;
                    }
                    case 4: // date
                        o = new Date(a.getMillis()); break;
                }
                
                if (o != null) {
                    return o;
                }
            }
            
            if (!def) {
                return null;
            }
            switch (kind) {
                case 1: // message
                    return t.getMessage();
                case 2: // loc.msg.
                    return t.getLocalizedMessage();
                case 3: // class name
                    return t.getClass().getName();
                case 4: // date
                    return new Date();
                default:
                    throw new IllegalArgumentException(
                        "Unknown " + Integer.valueOf(kind) // NOI18N
                        );
            }
        }
    }
    
}
