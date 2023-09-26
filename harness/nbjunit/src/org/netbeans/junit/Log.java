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

package org.netbeans.junit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;
import static junit.framework.TestCase.fail;
import org.netbeans.junit.internal.NbModuleLogHandler;

/** Collects log messages.
 *
 * @author Jaroslav Tulach
 */
public final class Log extends Handler {
    /** the test that is currently running */
    private static NbTestCase current;
    /** last 40Kb of collected error messages */
    private static final StringBuffer messages = new StringBuffer ();
    /** initial length of messages */
    private static int initialMessages;
    /** stream to log to */
    private Reference<PrintStream> log;
    /** logger we are assigned to */
    private Logger logger;

        
    /** Creates a new instance of Log */
    public Log() {
    }

    /** Creates handler with assigned logger
     */
    private Log(Logger l, PrintStream ps) {
        log = new WeakReference<PrintStream>(ps);
        logger = l;
    }

    static Runnable internalLog() {
        return new IL(true);
    }

    /** Enables logging for given logger name and given severity.
     * Everything logged to the object is going to go to the returned
     * CharSequence object which can be used to check the content or
     * converted <code>toString</code>.
     * <p>
     * The logging stops when the returned object is garbage collected.
     *
     * @param loggerName the name to capture logging for
     * @param level the level of details one wants to get
     * @return character sequence which can be check or converted to string
     * @since 1.27
     */
    public static CharSequence enable(String loggerName, Level level) {
        IL il = new IL(false);
        class MyPs extends PrintStream implements CharSequence {
            private ByteArrayOutputStream os;

            public MyPs() {
                this(new ByteArrayOutputStream());
            }

            private MyPs(ByteArrayOutputStream arr) {
                super(arr);
                os = arr;
            }

            public int length() {
                return toString().length();
            }

            public char charAt(int index) {
                return toString().charAt(index);
            }

            public CharSequence subSequence(int start, int end) {
                return toString().subSequence(start, end);
            }

            @Override
            public String toString() {
                return os.toString();
            }
        }

        Logger l = Logger.getLogger(loggerName);
        if (l.getLevel() == null || l.getLevel().intValue() > level.intValue()) {
            l.setLevel(level);
        }
        MyPs ps = new MyPs();
        Log log = new Log(l, ps);
        log.setLevel(level);
        l.addHandler(log);
        return ps;
    }

    /** 
     * Can emulate the execution flow of multiple threads in a deterministic
     * way so it is easy to emulate race conditions or deadlocks just with
     * the use of additional log messages inserted into the code.
     * <p>
     * The best example showing usage of this method is real life test.
     * Read <a href="https://github.com/apache/netbeans/tree/master/harness/nbjunit/test/unit/src/org/netbeans/junit/FlowControlTest.java">FlowControlTest.java</a> to know everything
     * about the expected usage of this method.
     * <p>
     * The method does listen on output send to a logger <code>listenTo</code>
     * by various threads and either suspends them or wake them up trying
     * as best as it can to mimic the log output described in <code>order</code>.
     * Of course, this may not always be possible, so there is the <code>timeout</code>
     * value which specifies the maximum time a thread can be suspended while
     * waiting for a single message. The information about the internal behaviour
     * of the controlFlow method can be send to <code>reportTo</code> logger,
     * if provided, so in case of failure one can analyse what went wrong.
     * <p>
     * The format of the order is a set of lines like:
     * <pre>
     * THREAD:name_of_the_thread MSG:message_to_expect
     * </pre>
     * which define the order saying that at this time a thread with a given name
     * is expected to send given message. Both the name of the thread and
     * the message are regular expressions so one can shorten them by using <code>.*</code>
     * or any other trick. Btw. the format of the <code>order</code> is similar
     * to the one logged by the {@link Log#enable} or {@link NbTestCase#logLevel} methods,
     * so when one gets a test failure with enabled logging,
     * it is enough to just delete the unnecessary messages, replace too specific
     * texts like <code>@574904</code> with <code>.*</code> and the order is
     * ready for use.
     *
     * @param listenTo the logger to listen to and guide the execution according to messages sent to it
     * @param reportTo the logger to report internal state to or <code>null</code> if the logging is not needed
     * @param order the string describing the expected execution order of threads
     * @param timeout the maximal wait time of each thread on given message, zero if the waiting shall be infinite
     *
     * @author Jaroslav Tulach, invented during year 2005
     * @since 1.28
     */
    public static void controlFlow(Logger listenTo, Logger reportTo, String order, int timeout) {
        ControlFlow.registerSwitches(listenTo, reportTo, order, timeout);
    }
    
    /** Starts to listen on given log and collect parameters of messages that
     * were send to it. This is supposed to be called at the beginning of a test,
     * to get messages from the programs that use 
     * <a href="https://netbeans.apache.org/wiki/FitnessViaTimersCounter">timers/counters</a>
     * infrastructure. At the end one should call {@link #assertInstances}.
     * 
     * 
     * @param log logger to listen on, if null, it uses the standard timers/counters one
     * @param msg name of messages to collect, if null, all messages will be recorded
     * @param level level of messages to record
     * @since 1.44
     */
    public static void enableInstances(Logger log, String msg, Level level) {
        if (log == null) {
            log = Logger.getLogger("TIMER"); // NOI18N
        }
        
        log.addHandler(new InstancesHandler(msg, level));
        
        if (log.getLevel() == null || log.getLevel().intValue() > level.intValue()) {
            log.setLevel(level);
        }
    }

    /** Assert to verify that all collected instances via {@link #enableInstances} 
     * can disappear. Uses {@link NbTestCase#assertGC} on each of them. 
     * 
     * @param msg message to display in case of potential failure
     */
    public static void assertInstances(String msg) {
        InstancesHandler.assertGC(msg);
    }

    /** Assert to verify that all properly named instances collected via {@link #enableInstances} 
     * can disappear. Uses {@link NbTestCase#assertGC} on each of them.
     *
     * @param msg message to display in case of potential failure
     * @param names list of names of instances to test for and verify that they disappear
     * @since 1.53
     */
    public static void assertInstances(String msg, String... names) {
        InstancesHandler.assertGC(msg, names);
    }



    static void configure(Level lev, String root, NbTestCase current) {
        IL il = new IL(false);
        
        String c = "handlers=" + Log.class.getName() + "\n" +
                   root + ".level=" + lev.intValue() + "\n";

        ByteArrayInputStream is = new ByteArrayInputStream(c.getBytes());
        try {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException ex) {
            // exception
            ex.printStackTrace();
        }

        Log.current = current;
        Log.messages.setLength(0);
        Log.messages.append("Starting test ");
        Log.messages.append(current.getName());
        Log.messages.append('\n');
        Log.initialMessages = Log.messages.length();
    }

    private PrintStream getLog() {
        if (log != null) {
            PrintStream ps = log.get();
            if (ps == null) {
                // gc => remove the handler
                setLevel(Level.OFF);
                logger.removeHandler(this);
            }

            return ps;
        }

        NbTestCase c = current;
        Runnable off = Log.internalLog();
        try {
            return c == null ? System.err : c.getLog();
        } finally {
            off.run();
        }
    }

    @Override
    public void publish(LogRecord record) {
        if (record.getLevel().intValue() < getLevel().intValue()) {
            return;
        }
        if (IL.isInternalLog()) {
            return;
        }
        Runnable off = internalLog();
        try {
            StringBuffer sb = NbModuleLogHandler.toString(record);
            PrintStream ps = getLog();
            if (ps != null) {
                try {
                    ps.println(sb.toString());
                } catch (LinkageError err) {
                    // prevent circular references
                }
            }
            
            if (messages.length() + sb.length() > 20000) {
                if (sb.length() > 20000) {
                    messages.setLength(0);
                    sb.delete(0, sb.length() - 20000); 
                } else {
                    messages.setLength(20000 - sb.length());
                }
            }

            messages.append(sb.toString());
        } finally {
            off.run();
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        Logger l = this.logger;
        if (getLevel() != Level.OFF && l != null) {
            l.addHandler(this);
        }
    }

    static /* @CheckForNull */ String normalizedMessages(String workDirPath) {
        if (messages.length() == initialMessages) {
            return null;
        }
        return NbModuleLogHandler.normalize(messages, workDirPath);
    }

    static Throwable wrapWithMessages(Throwable ex, String workDirPath) {
        String m = normalizedMessages(workDirPath);
        if (m == null) {
            // no wrapping
            return ex;
        }
        return wrapWithAddendum(ex, "Log:\n" + m, true);
    }

    static Throwable wrapWithAddendum(Throwable ex, String addendum, boolean after) {
        if (ex instanceof AssertionFailedError) {
            AssertionFailedError ne = new AssertionFailedError(combineMessages(ex, addendum, after));
            if (ex.getCause() != null) {
                ne.initCause(ex.getCause());
            }
            ne.setStackTrace (ex.getStackTrace ());
            return ne;
        }
        if (ex instanceof AssertionError) { // preferred in JUnit 4
            AssertionError ne = new AssertionError(combineMessages(ex, addendum, after));
            if (ex.getCause() != null) {
                ne.initCause(ex.getCause());
            }
            ne.setStackTrace(ex.getStackTrace());
            return ne;
        }
        if (ex instanceof IOException) {//#66208
            IOException ne = new IOException(combineMessages(ex, addendum, after));
            if (ex.getCause() != null) {
                ne.initCause(ex.getCause());
            }
            ne.setStackTrace (ex.getStackTrace ());
            return ne;
        }
        if (ex instanceof Exception) {
            return new InvocationTargetException(ex, combineMessages(ex, addendum, after));
        }
        return ex;
    }
    private static String combineMessages(Throwable ex, String addendum, boolean after) {
        String baseMessage = ex.getMessage();
        return (baseMessage == null || baseMessage.equals("null")) ? addendum : after ? baseMessage + " " + addendum : addendum + " " + baseMessage;
    }

        
    private static class InstancesHandler extends Handler {
        static final Map<Object,String> instances = Collections.synchronizedMap(new WeakHashMap<Object,String>());
        static int cnt;

        private final String msg;
        
        public InstancesHandler(String msg, Level level) {
            setLevel(level);
            this.msg = msg;
        }

        @Override
        public void publish(LogRecord record) {
            Object[] param = record.getParameters();
            if (param == null) {
                return;
            }
            if (msg != null && !msg.equals(record.getMessage())) {
                return;
            }
            cnt++;
            for (Object o : param) {
                instances.put(o, record.getMessage());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
        
        public static void assertGC(String msg, String... names) {
            AssertionFailedError t = null;
            
            List<Reference> refs = new ArrayList<Reference>();
            List<String> txts = new ArrayList<String>();
            int count = 0;
            Set<String> nameSet = names == null || names.length == 0 ? null : new HashSet<String>(Arrays.asList(names));
            synchronized (instances) {
                for (Iterator<Map.Entry<Object, String>> it = instances.entrySet().iterator(); it.hasNext();) {
                    Entry<Object, String> entry = it.next();
                    if (nameSet != null && !nameSet.contains(entry.getValue())) {
                        continue;
                    }

                    refs.add(new WeakReference<Object>(entry.getKey()));
                    txts.add(entry.getValue());
                    it.remove();
                    count++;
                }
            }

            if (count == 0) {
                fail("No instance of this type reported");
            }
            
            for (int i = 0; i < count; i++) {
                Reference<?> r = refs.get(i);
                try {
                    NbTestCase.assertGC(msg + " " + txts.get(i), r);
                } catch (AssertionFailedError ex) {
                    if (t == null) {
                        t = ex;
                    } else {
                        Throwable last = t;
                        while (last.getCause() != null) {
                            last = last.getCause();
                        }
                        last.initCause(ex);
                    }
                }
            }
            if (t != null) {
                throw t;
            }
        }
        
    } // end of InstancesHandler

    private static class IL implements Runnable {
        private static ThreadLocal<Boolean> INTERNAL_LOG = new ThreadLocal<Boolean>();
        private final Boolean prev;

        public IL(boolean on) {
            prev = INTERNAL_LOG.get();
            INTERNAL_LOG.set(on);
        }

        @Override
        public void run() {
            INTERNAL_LOG.set(prev);
        }

        public static boolean isInternalLog() {
            return Boolean.TRUE.equals(INTERNAL_LOG.get());
        }
    } // end of IL
}
