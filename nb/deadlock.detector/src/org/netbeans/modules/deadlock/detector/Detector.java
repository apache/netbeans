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
package org.netbeans.modules.deadlock.detector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Detects deadlocks using ThreadMXBean.
 * @see java.lang.management.ThreadMXBean
 * @author Mandy Chung, David Strupl
 */
class Detector implements Runnable {

    private static final Logger LOG = Logger.getLogger(Detector.class.getName());
    
    /**
     * This variable is used from different threads and is protected by 
     * synchronized(this).
     */
    private boolean running = true;
    /**
     * How long  to wait (in milliseconds) between the deadlock checks.
     */
    private static long PAUSE = 2000;
    /**
     * How long to wait (in milliseconds) before the deadlock detection starts.
     */
    private static long INITIAL_PAUSE = 10000;
    /**
     * Indents for printing the thread dumps.
     */
    private static final String INDENT = "    "; // NOI18N
    /**
     * The thread bean used for the deadlock detection.
     */
    private final ThreadMXBean threadMXBean;
    
    Detector() {
        threadMXBean = ManagementFactory.getThreadMXBean();
        Integer pauseFromSysProp = Integer.getInteger("org.netbeans.modules.deadlock.detector.Detector.PAUSE"); // NOI18N
        if (pauseFromSysProp != null) {
            PAUSE = pauseFromSysProp.longValue();
        }
        Integer initialPauseFromSysProp = Integer.getInteger("org.netbeans.modules.deadlock.detector.Detector.INITIAL_PAUSE"); // NOI18N
        if (initialPauseFromSysProp != null) {
            INITIAL_PAUSE = initialPauseFromSysProp.longValue();
        }
    }
    
    /**
     * Starts a new thread that periodically checks for deadlocks.
     */
    void start() {
        if (threadMXBean == null) {
            return;
        }
        Thread t = new Thread(this, "Deadlock Detector"); // NOI18N
        t.start();
    }
    
    /**
     * Stops the detector thread.
     */
    synchronized void stop() {
        running = false;
    }
    
    /**
     * Accessing the variable running under the synchronized (this).
     * @return whether we are still running the detector thread
     */
    private synchronized boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(INITIAL_PAUSE);
            while (isRunning()) {
                long time = System.currentTimeMillis();
                detectDeadlock();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Deadlock detection took: {0} ms.", System.currentTimeMillis() - time); // NOI18N
                }
                if (isRunning()) {
                    Thread.sleep(PAUSE);
                }
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /**
     * The main method called periodically by the deadlock detector thread.
     */
    private void detectDeadlock() {
        if (threadMXBean == null) {
            return;
        }
        long[] tids = threadMXBean.findDeadlockedThreads();
        if (tids == null) {
            return;
        }
        
        // Report deadlock just once
        stop();
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Deadlock detected"); // NOI18N
        }
        PrintStream out;
        File file = null;
        try {
            file = Files.createTempFile("deadlock", ".txt").toFile(); // NOI18N
            out = new PrintStream(new FileOutputStream(file));
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Temporrary file created: {0}" , file); // NOI18N
            }            
        } catch (IOException iOException) {
            out = System.out;            
        }        
        out.println("Deadlocked threads :"); // NOI18N
        ThreadInfo[] deadlocked = threadMXBean.getThreadInfo(tids, true, true);
        for (ThreadInfo ti : deadlocked) {
            printThreadInfo(ti, out);
            printMonitorInfo(ti, out);
            printLockInfo(ti.getLockedSynchronizers(), out);
            out.println();
        }
        out.println("All threads :"); // NOI18N
        ThreadInfo[] infos = threadMXBean.dumpAllThreads(true, true);
        for (ThreadInfo ti : infos) {
            if (ti == null) {
                continue; // null can be returned in the array
            }
            printThreadInfo(ti, out);
            printMonitorInfo(ti, out);
            printLockInfo(ti.getLockedSynchronizers(), out);
            out.println();
        }
        if (out != System.out) {
            out.close();
        }
        
        reportStackTrace(deadlocked, file);
    }

    private void printThreadInfo(ThreadInfo ti, PrintStream out) {
       printThread(ti, out);

       // print stack trace with locks
       StackTraceElement[] stacktrace = ti.getStackTrace();
       MonitorInfo[] monitors = ti.getLockedMonitors();
       for (int i = 0; i < stacktrace.length; i++) {
           StackTraceElement ste = stacktrace[i];
           out.println(INDENT + "at " + ste.toString()); // NOI18N
           for (MonitorInfo mi : monitors) {
               if (mi.getLockedStackDepth() == i) {
                   out.println(INDENT + "  - locked " + mi); // NOI18N
               }
           }
       }
       out.println();
    }

    private void printThread(ThreadInfo ti, PrintStream out) {
       StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\"" + // NOI18N
                                            " Id=" + ti.getThreadId() +       // NOI18N
                                            " in " + ti.getThreadState());    // NOI18N
       if (ti.getLockName() != null) {
           sb.append(" on lock=").append(ti.getLockName()); // NOI18N
       }
       if (ti.isSuspended()) {
           sb.append(" (suspended)"); // NOI18N
       }
       if (ti.isInNative()) {
           sb.append(" (running in native)"); // NOI18N
       }
       out.println(sb.toString());
       if (ti.getLockOwnerName() != null) {
            out.println(INDENT + " owned by " + ti.getLockOwnerName() + // NOI18N
                               " Id=" + ti.getLockOwnerId());           // NOI18N
       }
    }

    private void printMonitorInfo(ThreadInfo ti, PrintStream out) {
       MonitorInfo[] monitors = ti.getLockedMonitors();
       out.println(INDENT + "Locked monitors: count = " + monitors.length); // NOI18N
       for (MonitorInfo mi : monitors) {
           out.println(INDENT + "  - " + mi + " locked at "); // NOI18N
           out.println(INDENT + "      " + mi.getLockedStackDepth() + // NOI18N
                              " " + mi.getLockedStackFrame());       // NOI18N
       }
    }

    private void printLockInfo(LockInfo[] locks, PrintStream out) {
       out.println(INDENT + "Locked synchronizers: count = " + locks.length); // NOI18N
       for (LockInfo li : locks) {
           out.println(INDENT + "  - " + li); // NOI18N
       }
       out.println();
    }    

    /**
     * Use exception reporter to report the stack trace of the deadlocked threads.
     * @param deadlocked 
     */
    private void reportStackTrace(ThreadInfo[] deadlocked, File report) {
        DeadlockDetectedException deadlockException = new DeadlockDetectedException(null);
        deadlockException.setStackTrace(deadlocked[0].getStackTrace());
        DeadlockDetectedException lastDde = deadlockException;
        for (ThreadInfo toBeReported : deadlocked) {
            DeadlockDetectedException dde = new DeadlockDetectedException(toBeReported.getThreadName());
            dde.setStackTrace(toBeReported.getStackTrace());
            lastDde.initCause(dde);
            lastDde = dde;
        }
        LOG.log(Level.SEVERE, report.getAbsolutePath(), deadlockException);
    }
    
    private static class DeadlockDetectedException extends RuntimeException {
        
        public DeadlockDetectedException(String threadName) {
            super(threadName);
        }

        @NbBundle.Messages("MSG_DeadlockDetected=A deadlock was detected.\nWe suggest to restart the IDE to recover.")
        @Override
        public String getLocalizedMessage() {
            if (getMessage() == null) {
                return Bundle.MSG_DeadlockDetected();
            } else {
                return super.getLocalizedMessage();
            }
        }
        
    }
}
