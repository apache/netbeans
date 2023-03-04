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
package org.netbeans.core.startup.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.core.startup.TopLogging;
import org.openide.util.NbBundle;
import org.xml.sax.SAXParseException;

/** Modified formater for use in NetBeans.
 */
public final class NbFormatter extends java.util.logging.Formatter {
    private static String lineSeparator = System.getProperty("line.separator"); // NOI18N
    public static final java.util.logging.Formatter FORMATTER = new NbFormatter();

    @Override
    public String format(java.util.logging.LogRecord record) {
        StringBuilder sb = new StringBuilder();
        print(sb, record, new HashSet<Throwable>());
        String r = sb.toString();
        if (NbLogging.DEBUG != null) {
            NbLogging.DEBUG.print("received: " + r); // NOI18N
        }
        if (NbLogging.unwantedMessages != null && NbLogging.unwantedMessages.matcher(r).find()) {
            new Exception().printStackTrace(NbLogging.DEBUG);
        }
        return r;
    }

    private void print(StringBuilder sb, LogRecord record, Set<Throwable> beenThere) {
        String message = formatMessage(record);
        if (message != null && message.indexOf('\n') != -1 && record.getThrown() == null) {
            // multi line messages print witout any wrappings
            sb.append(message);
            if (message.charAt(message.length() - 1) != '\n') {
                sb.append(lineSeparator);
            }
            return;
        }
        if ("stderr".equals(record.getLoggerName()) && record.getLevel() == Level.INFO) {
            // NOI18N
            // do not prefix stderr logging...
            sb.append(message);
            return;
        }
        sb.append(record.getLevel().getName());
        addLoggerName(sb, record);
        if (message != null) {
            sb.append(": ");
            sb.append(message);
        }
        sb.append(lineSeparator);
        if (record.getThrown() != null && record.getLevel().intValue() != 1973) {
            // 1973 signals ErrorManager.USER
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                // All other kinds of throwables we check for a stack trace.
                printStackTrace(record.getThrown(), pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
            }
            LogRecord[] arr = extractDelegates(sb, record.getThrown(), beenThere);
            if (arr != null) {
                for (LogRecord r : arr) {
                    print(sb, r, beenThere);
                }
            }
            specialProcessing(sb, record.getThrown(), beenThere);
        }
    }

    private static void addLoggerName(StringBuilder sb, java.util.logging.LogRecord record) {
        String name = record.getLoggerName();
        if (!"".equals(name)) {
            sb.append(" [");
            sb.append(name);
            sb.append(']');
        }
    }

    private static LogRecord[] extractDelegates(StringBuilder sb, Throwable t, Set<Throwable> beenThere) {
        if (!beenThere.add(t)) {
            sb.append("warning: cyclic dependency between annotated throwables"); // NOI18N
            return null;
        }
        if (t instanceof Callable) {
            Object rec = null;
            try {
                rec = ((Callable) t).call();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (rec instanceof LogRecord[]) {
                return (LogRecord[]) rec;
            }
        }
        if (t == null) {
            return null;
        }
        return extractDelegates(sb, t.getCause(), beenThere);
    }

    private void specialProcessing(StringBuilder sb, Throwable t, Set<Throwable> beenThere) {
        // MissingResourceException should be printed nicely... --jglick
        if (t instanceof MissingResourceException) {
            MissingResourceException mre = (MissingResourceException) t;
            String cn = mre.getClassName();
            if (cn != null) {
                LogRecord rec = new LogRecord(Level.CONFIG, null);
                rec.setResourceBundle(NbBundle.getBundle(TopLogging.class));
                rec.setMessage("EXC_MissingResourceException_class_name");
                rec.setParameters(new Object[]{cn});
                print(sb, rec, beenThere);
            }
            String k = mre.getKey();
            if (k != null) {
                LogRecord rec = new LogRecord(Level.CONFIG, null);
                rec.setResourceBundle(NbBundle.getBundle(TopLogging.class));
                rec.setMessage("EXC_MissingResourceException_key");
                rec.setParameters(new Object[]{k});
                print(sb, rec, beenThere);
            }
        }
        if (t instanceof SAXParseException) {
            // For some reason these fail to come with useful data, like location.
            SAXParseException spe = (SAXParseException) t;
            String pubid = spe.getPublicId();
            String sysid = spe.getSystemId();
            if (pubid != null || sysid != null) {
                int col = spe.getColumnNumber();
                int line = spe.getLineNumber();
                String msg;
                Object[] param;
                if (col != -1 || line != -1) {
                    msg = "EXC_sax_parse_col_line"; // NOI18N
                    param = new Object[]{String.valueOf(pubid), String.valueOf(sysid), col, line};
                } else {
                    msg = "EXC_sax_parse"; // NOI18N
                    param = new Object[]{String.valueOf(pubid), String.valueOf(sysid)};
                }
                LogRecord rec = new LogRecord(Level.CONFIG, null);
                rec.setResourceBundle(NbBundle.getBundle(TopLogging.class));
                rec.setMessage(msg);
                rec.setParameters(param);
                print(sb, rec, beenThere);
            }
        }
    }
    private static final Map<Throwable, Integer> catchIndex = Collections.synchronizedMap(new WeakHashMap<Throwable, Integer>()); // #190623

    /**
     * For use also from NbErrorManager.
     *
     * @param t throwable to print
     * @param pw the destination
     */
    public static void printStackTrace(Throwable t, PrintWriter pw) {
        doPrintStackTrace(pw, t, null, 10);
    }

    /**
     * #91541: show stack traces in a more natural order.
     */
    private static void doPrintStackTrace(PrintWriter pw, Throwable t, Throwable higher, int depth) {
        if (depth == 0) {
            pw.println("Truncating the output at ten nested exceptions..."); // NOI18N
            return;
        }
        //if (t != null) {t.printStackTrace(pw);return;}//XxX
        try {
            if (t.getClass().getMethod("printStackTrace", PrintWriter.class).getDeclaringClass() != Throwable.class) { // NOI18N
                // Hmm, overrides it, we should not try to bypass special logic here.
                //System.err.println("using stock printStackTrace from " + t.getClass());
                t.printStackTrace(pw);
                return;
            }
            //System.err.println("using custom printStackTrace from " + t.getClass());
        } catch (NoSuchMethodException e) {
            assert false : e;
        }
        Throwable lower = t.getCause();
        if (lower != null) {
            doPrintStackTrace(pw, lower, t, depth - 1);
            pw.print("Caused: "); // NOI18N
        }
        String summary = t.toString();
        if (lower != null) {
            String suffix = ": " + lower;
            if (summary.endsWith(suffix)) {
                summary = summary.substring(0, summary.length() - suffix.length());
            }
        }
        pw.println(summary);
        StackTraceElement[] trace = t.getStackTrace();
        int end = trace.length;
        if (higher != null) {
            StackTraceElement[] higherTrace = higher.getStackTrace();
            while (end > 0) {
                int higherEnd = end + higherTrace.length - trace.length;
                if (higherEnd <= 0 || !higherTrace[higherEnd - 1].equals(trace[end - 1])) {
                    break;
                }
                end--;
            }
        }
        Integer caughtIndex = catchIndex.get(t);
        for (int i = 0; i < end; i++) {
            if (caughtIndex != null && i == caughtIndex) {
                // Translate following tab -> space since formatting is bad in
                // Output Window (#8104) and some mail agents screw it up etc.
                pw.print("[catch] at "); // NOI18N
            } else {
                pw.print("\tat "); // NOI18N
            }
            pw.println(trace[i]);
        }
    }

    static void registerCatchIndex(Throwable t, int index) {
        catchIndex.put(t, index);
    }
    
} // end of NbFormater
