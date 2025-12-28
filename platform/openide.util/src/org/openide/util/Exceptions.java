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
package org.openide.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/** Useful utility and methods to work with exceptions as
 * described in detail in the  <a href="@org-openide-util@/org/openide/util/doc-files/logging.html">NetBeans logging guide</a>.
 * Allows to annotate exceptions with messages, extract such messages
 * and provides a common utility method to report an exception.
 * 
 *
 * @since 7.2
 */
public final class Exceptions extends Object {
    private Exceptions() {
    }
    
    static final Logger LOG = Logger.getLogger(Exceptions.class.getName());
    private static final String LOC_MSG_PLACEHOLDER = "msg"; // NOI18N

    /** Attaches additional message to given exception. This message will
     * be visible when one does <code>e.printStackTrace()</code>.
     * @param <E> type of excetion
     * @param e exception to annotate
     * @param msg the message to add to the exception
     * @return the exception <code>e</code>
     */
    public static <E extends Throwable> E attachMessage(E e, String msg) {
        AnnException ae = AnnException.findOrCreate(e, true);
        LogRecord rec = new LogRecord(Level.ALL, msg);
        ae.addRecord(rec);
        return e;
    }

    /** Attaches additional localized message to given exception. This message 
     * can be extracted later by using {@link #findLocalizedMessage}.
     * @param <E> type of excetion
     * @param e exception to annotate
     * @param localizedMessage the localized message to add to the exception
     * @return the exception <code>e</code>
     */
    public static <E extends Throwable> E attachLocalizedMessage(E e, final String localizedMessage) {
        AnnException ae = AnnException.findOrCreate(e, true);
        LogRecord rec = new LogRecord(Level.ALL, LOC_MSG_PLACEHOLDER);
        ResourceBundle rb = new ResourceBundle() {
            public Object handleGetObject(String key) {
                if (LOC_MSG_PLACEHOLDER.equals(key)) {
                    return localizedMessage;
                } else {
                    return null;
                }
            }

            public Enumeration<String> getKeys() {
                return Enumerations.singleton(LOC_MSG_PLACEHOLDER);
            }
        };
        rec.setResourceBundle(rb);
        ae.addRecord(rec);
        return e;
    }
    
    /**
     * Attaches a given severity to the exception. When the exception is
     * later passed to {@link #printStackTrace} method, the level is
     * then used as a level for reported {@link LogRecord}. This allows
     * those who report exceptions to annotate them as unimportant,
     * expected.
     * @param <E> type of excetion
     * @param e exception to assign severity to
     * @param severity the severity
     * @return the exception <code>e</code>
     * @since 8.8
     */
    public static <E extends Throwable> E attachSeverity(E e, Level severity) {
        AnnException ae = AnnException.findOrCreate(e, true);
        ae.addRecord(new LogRecord(severity, null));
        return e;
    }

    /** Extracts previously attached localized message for a given throwable.
     * Complements {@link #attachLocalizedMessage}.
     *
     * @param t the exception to search for a message in
     * @return localized message attached to provided exception or <code>null</code>
     *   if no such message has been attached
     */
    public static String findLocalizedMessage(Throwable t) {
        while (t != null) {
            String msg;
            AnnException extra = AnnException.extras.get(t);
            if (extra != null) {
                msg = extractLocalizedMessage(extra);
            } else {
                msg = extractLocalizedMessage(t);
            }
            
            if (msg != null) {
                return msg;
            }
            
            t = t.getCause();
        }
        return null;
    }

    private static String extractLocalizedMessage(final Throwable t) {
        String msg = null;
        if (t instanceof Callable) {
            Object res = null;
            try {
                res = ((Callable) t).call();
            } catch (Exception ex) {
                LOG.log(Level.WARNING, null, t); // NOI18N
            }
            if (res instanceof LogRecord[]) {
                for (LogRecord r : (LogRecord[])res) {
                    ResourceBundle b = r.getResourceBundle();
                    if (b != null) {
                        msg = b.getString(r.getMessage());
                        break;
                    }
                }
            }
        }
        return msg;
    }
    
    /** Notifies an exception with a severe level. Such exception is going
     * to be printed to log file and possibly also notified to alarm the
     * user somehow.
     * <p class="nonnormative">
     * Since version 8.29 the default implementation of this method inside
     * a NetBeans Platform based application understands 
     * <code>UserQuestionException</code>. If the exception is thrown and later
     * reported via this method, it is properly shown to the user as a 
     * dialog with approve/reject buttons. If approved, the infrastructure
     * calls <code>UserQuestionException.confirmed()</code> method.
     * </p>
     *
     * @param t the exception to notify
     */
    public static void printStackTrace(Throwable t) {
        AnnException ae = AnnException.findOrCreate(t, false);
        Level level = null;
        if (ae != null) {
            for (LogRecord r : ae.records) {
                if (r.getLevel() != Level.ALL) {
                    level = r.getLevel();
                    break;
                }
            }
        }
        if (level == null) {
            level = OwnLevel.UNKNOWN;
        }
        AnnException extra = AnnException.extras.get(t);
        if (extra != null) {
            assert t == extra.getCause();
            t = extra;
        }
        LOG.log(level, null, t);
    }

    /** An exception that has a log record associated with itself, so
     * the NbErrorManager can extract info about the annotation.
     */
    static final class AnnException extends Exception implements Callable<LogRecord[]> {
        private List<LogRecord> records;
        
        private AnnException() {
            super();
        }
        
        private AnnException(String msg) {
            super(msg);
        }

        @Override
        public String getMessage() {
            StringBuilder sb = new StringBuilder();
            String sep = "";
            final List<LogRecord> rec = records;
            if (rec != null) for (LogRecord r : rec) {
                String m = r.getMessage();
                if (m != null && !m.equals(LOC_MSG_PLACEHOLDER)) {
                    sb.append(sep);
                    sb.append(m);
                    sep = "\n";
                }
            }
            return sb.toString();
        }
        
        
        /** additional mapping from throwables that refuse initCause call */
        private static Map<Throwable, AnnException> extras = new WeakHashMap<Throwable, AnnException>();

        static AnnException findOrCreate(Throwable t, boolean create) {
            AnnException ann;
            try {
                ann = findOrCreate0(t, create);
                if (ann != null) {
                    return ann;
                }
            } catch (IllegalStateException ex) {
                assert create;
                ann = extras.get(t);
                if (ann == null) {
                    ann = new AnnException(t.getMessage());
                    ann.initCause(t);
                    LOG.log(Level.FINE, "getCause was null yet initCause failed for " + t, ex);
                    extras.put(t, ann);
                }
            }
            return ann;
        }
        private static AnnException findOrCreate0(Throwable t, boolean create) {
            if (t instanceof AnnException) {
                return (AnnException) t;
            }
            if (t.getCause() == null) {
                if (create) {
                    final AnnException ae = new AnnException();
                    t.initCause(ae);
                    if (ae != t.getCause()) {
                        throw new IllegalStateException();
                    }
                }
                return (AnnException) t.getCause();
            }
            return findOrCreate0(t.getCause(), create);
        }

        public synchronized void addRecord(LogRecord rec) {
            if (records == null) {
                records = new ArrayList<LogRecord>();
            }
            records.add(rec);
        }

        public LogRecord[] call() {
            List<LogRecord> r = records;
            LogRecord[] empty = new LogRecord[0];
            return r == null ? empty : r.toArray(empty);
        }

        @Override
        public void printStackTrace(PrintStream s) {
            super.printStackTrace(s);
            logRecords(s);
        }

        @Override
        public void printStackTrace(PrintWriter s) {
            super.printStackTrace(s);
            logRecords(s);
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }

        @Override
        public String toString() {
            return getMessage();
        }

        private void logRecords(Appendable a) {
            List<LogRecord> r = records;
            if (r == null) {
                return;
            }
            try {

                for (LogRecord log : r) {
                    if (log.getMessage() != null) {
                        a.append(log.getMessage()).append("\n");
                    }
                    if (log.getThrown() != null) {
                        StringWriter w = new StringWriter();
                        log.getThrown().printStackTrace(new PrintWriter(w));
                        a.append(w.toString()).append("\n");
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    } // end AnnException
    private static final class OwnLevel extends Level {
        public static final Level UNKNOWN = new OwnLevel("SEVERE", Level.SEVERE.intValue() + 1); // NOI18N

        private OwnLevel(String s, int i) {
            super(s, i);
        }
    } // end of OwnLevel
}
