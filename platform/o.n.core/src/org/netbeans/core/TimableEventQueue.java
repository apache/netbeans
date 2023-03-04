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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.sampler.Sampler;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Logging event queue that can report problems about too long execution times
 * 
 * 
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
final class TimableEventQueue extends EventQueue 
implements Runnable {
    private static final Logger LOG = Logger.getLogger(TimableEventQueue.class.getName());
    static final RequestProcessor RP = new RequestProcessor("Timeable Event Queue Watch Dog", 1, false, false); // NOI18N
    private static final int QUANTUM;
    private static final int REPORT;
    static {
        int quantum = 10000; // 10s
        int report = 20000; // 20s
        assert (quantum = 100) > 0; // 100ms in not production mode
        assert (report = 3000) > 0; // 3s in not production mode

        QUANTUM = Integer.getInteger("org.netbeans.core.TimeableEventQueue.quantum", quantum); // NOI18N
        REPORT = Integer.getInteger("org.netbeans.core.TimeableEventQueue.report", report); // NOI18N
    } 
    private static final int WAIT_CURSOR_LIMIT = Integer.getInteger("org.netbeans.core.TimeableEventQueue.waitcursor", 15000); // NOI18N
    private static final int PAUSE = Integer.getInteger("org.netbeans.core.TimeableEventQueue.pause", 15000); // NOI18N

    private final RequestProcessor.Task TIMEOUT;
    private final RequestProcessor.Task WAIT_CURSOR_CHECKER;
    private volatile long ignoreTill;
    private volatile long start;
    private volatile Sampler stoppable;
    private volatile boolean isWaitCursor;
    static volatile Thread eq;
    private final Frame mainWindow;

    private TimableEventQueue(Frame f) {
        this.mainWindow = f;
        TIMEOUT = RP.create(this);
        TIMEOUT.setPriority(Thread.MIN_PRIORITY);
        WAIT_CURSOR_CHECKER = RP.create(new Runnable() {

            @Override
            public void run() {
                isWaitCursor |= isWaitCursor();
            }
        }, true);
        WAIT_CURSOR_CHECKER.setPriority(Thread.MIN_PRIORITY);
        ignoreTill = System.currentTimeMillis() + PAUSE;
    }

    static void initialize() {
        initialize(null, true);
    }
    static void initialize(final Frame f, final boolean defaultWindow) {
        boolean install = Boolean.valueOf(NbBundle.getMessage(TimableEventQueue.class, "TimableEventQueue.install")); // NOI18N
        if (!install) {
            return;
        }
        
        // #28536: make sure a JRE bug does not prevent the event queue from having
        // the right context class loader
        // and #35470: do it early, before any module-loaded AWT code might run
        // and #36820: even that isn't always early enough, so we need to push
        // a new EQ to enforce the context loader
        // XXX this is a hack!
        try {
            Mutex.EVENT.writeAccess (new Mutex.Action<Void>() {
                @Override
                public Void run() {
                    Frame use = f;
                    if (defaultWindow && use == null) {
                        use = WindowManager.getDefault().getMainWindow();
                    }
                    ClassLoader scl = Lookup.getDefault().lookup(ClassLoader.class);
                    if (scl != null) {
                        Thread.currentThread().setContextClassLoader(scl);
                    }
                    Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TimableEventQueue(use));
                    LOG.fine("Initialization done");
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void dispatchEvent(AWTEvent event) {
        eq = Thread.currentThread();
        boolean scheduled = false;
        try {
            scheduled = tick("dispatchEvent"); // NOI18N
            super.dispatchEvent(event);
        } finally {
            if (scheduled) {
                done();
            }
        }
    }

    private void done() {
        TIMEOUT.cancel();
        TIMEOUT.waitFinished();
        if (!WAIT_CURSOR_CHECKER.cancel()) {
            WAIT_CURSOR_CHECKER.waitFinished();
        }

        LOG.log(Level.FINE, "isWait cursor {0}", isWaitCursor); // NOI18N
        long r;
        if (isWaitCursor) {
            r = REPORT * 10;
            if (r > WAIT_CURSOR_LIMIT) {
                r = (WAIT_CURSOR_LIMIT > REPORT) ? WAIT_CURSOR_LIMIT : REPORT;
            }
        } else {
            r = REPORT;
        }
        isWaitCursor = false;
        long time = System.currentTimeMillis() - start;
        if (time > QUANTUM) {
            LOG.log(Level.FINE, "done, timer stopped, took {0}", time); // NOI18N
            if (time > r) {
                LOG.log(Level.WARNING, "too much time in AWT thread {0}", stoppable); // NOI18N
                ignoreTill = System.currentTimeMillis() + PAUSE;
                report(stoppable, time);
                stoppable = null;
            }
        } else {
            LOG.log(Level.FINEST, "done, timer stopped, took {0}", time);
        }
        Sampler ss = stoppable;
        if (ss != null) {
            ss.cancel();
            stoppable = null;
        }
        return;
    }
    
    private boolean isShowing() {
        return mainWindow == null || mainWindow.isShowing();
    }

    private boolean tick(String name) {
        start = System.currentTimeMillis();
        if (start >= ignoreTill && isShowing()) {
            LOG.log(Level.FINEST, "tick, schedule a timer for {0}", name);
            TIMEOUT.schedule(QUANTUM);
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        if (stoppable != null) {
            LOG.log(Level.WARNING, "Still previous controller {0}", stoppable);
            return;
        }
        Sampler selfSampler = createSelfSampler();
        if (selfSampler != null) {
            selfSampler.start();
            stoppable = selfSampler;
        }
        isWaitCursor |= isWaitCursor();
        if (!isWaitCursor) {
            WAIT_CURSOR_CHECKER.schedule(Math.max(REPORT - QUANTUM, 0));
        }
    }

    private static void report(final Sampler ss, final long time) {
        if (ss == null) {
            return;
        }
        class R implements Runnable {
            @Override
            public void run() {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);
                    ss.stopAndWriteTo(dos);
                    dos.close();
                    if (dos.size() > 0) {
                        Object[] params = new Object[]{out.toByteArray(), time};
                        Logger.getLogger("org.netbeans.ui.performance").log(Level.CONFIG, "Slowness detected", params);
                    } else {
                        LOG.log(Level.WARNING, "no snapshot taken"); // NOI18N
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        RP.post(new R());
    }

    private static Sampler createSelfSampler() {
        return Sampler.createSampler("awt"); // NOI18N
    }

    private static boolean isWaitCursor() {
        Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focus != null) {
            if (focus.getCursor().getType() == Cursor.WAIT_CURSOR) {
                LOG.finer("wait cursor on focus owner"); // NOI18N
                return true;
            }
            Window w = SwingUtilities.windowForComponent(focus);
            if (w != null && isWaitCursorOnWindow(w)) {
                LOG.finer("wait cursor on window"); // NOI18N
                return true;
            }
        }
        for (Frame f : Frame.getFrames()) {
            if (isWaitCursorOnWindow(f)) {
                LOG.finer("wait cursor on frame"); // NOI18N
                return true;
            }
        }
        LOG.finest("no wait cursor"); // NOI18N
        return false;
    }

    private static boolean isWaitCursorOnWindow(Window w) {
        if (w.getCursor().getType() == Cursor.WAIT_CURSOR) {
            return true;
        }
        if (w instanceof JFrame) {
            JRootPane root = ((JFrame)w).getRootPane();
            if (null != root) {
                Component glass = root.getGlassPane();
                if (null != glass && glass.getCursor().getType() == Cursor.WAIT_CURSOR) {
                    return true;
                }
            }
        }
        return false;
    }

        /*
        long now = System.currentTimeMillis();
        ignoreTill = now + PAUSE;
        long howLong = now - start;
        
//        Logger UI_LOG = Logger.getLogger("org.netbeans.ui.performance"); // NOI18N
        LogRecord rec = new LogRecord(Level.INFO, "LOG_EventQueueBlocked"); // NOI18N
        rec.setParameters(new Object[] { howLong });
        EQException eq = new EQException(myStack);
        rec.setThrown(eq);
        rec.setResourceBundleName("org.netbeans.core.Bundle"); // NOI18N
        rec.setResourceBundle(ResourceBundle.getBundle("org.netbeans.core.Bundle")); // NOI18N
//        UI_LOG.log(rec);
        LOG.log(rec);
    }

    private static final class EQException extends Exception {
        private volatile Map<Thread, StackTraceElement[]> stack;

        public EQException(Map<Thread, StackTraceElement[]> stack) {
            this.stack = stack;
            for (Map.Entry<Thread, StackTraceElement[]> en : stack.entrySet()) {
                if (en.getKey().getName().indexOf("AWT-EventQueue") >= 0) {
                    setStackTrace(en.getValue());
                    break;
                }
            }
        }
    
        @Override
        public String getMessage() {
            return threadDump("AWT Event Queue Thread Blocked", stack); // NOI18N
        }
        
        private static void appendThread(StringBuilder sb, String indent, Thread t, Map<Thread,StackTraceElement[]> data) {
            sb.append(indent).append("Thread ").append(t.getName()).append('\n');
            indent = indent.concat("  ");
            StackTraceElement[] arr = data.get(t);
            if (arr != null) {
                for (StackTraceElement e : arr) {
                    sb.append(indent).append(e.getClassName()).append('.').append(e.getMethodName())
                            .append(':').append(e.getLineNumber()).append('\n');
                }
            } else {
                sb.append(indent).append("no stacktrace info"); // NOI18N
            }
        }

        private static void appendGroup(StringBuilder sb, String indent, ThreadGroup tg, Map<Thread,StackTraceElement[]> data) {
            sb.append(indent).append("Group ").append(tg.getName()).append('\n');
            indent = indent.concat("  ");

            int groups = tg.activeGroupCount();
            ThreadGroup[] chg = new ThreadGroup[groups];
            tg.enumerate(chg, false);
            for (ThreadGroup inner : chg) {
                if (inner != null) appendGroup(sb, indent, inner, data);
            }

            int threads = tg.activeCount();
            Thread[] cht= new Thread[threads];
            tg.enumerate(cht, false);
            for (Thread t : cht) {
                if (t != null) appendThread(sb, indent, t, data);
            }
        }

        private static String threadDump(String msg, Map<Thread,StackTraceElement[]> all) {
            ThreadGroup root = Thread.currentThread().getThreadGroup();
            while (root.getParent() != null) root = root.getParent();

            StringBuilder sb = new StringBuilder();
            sb.append(msg).append('\n');
            appendGroup(sb, "", root, all);
            sb.append('\n').append("---");
            return sb.toString();
        }
        
    }
    */
}
