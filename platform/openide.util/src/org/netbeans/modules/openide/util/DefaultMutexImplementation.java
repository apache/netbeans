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
package org.netbeans.modules.openide.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;
import org.openide.util.spi.MutexImplementation;


public class DefaultMutexImplementation implements MutexImplementation {

    /** counter of created mutexes */
    static int counter;

    /** logger for things that happen in mutex */
    private static final Logger LOG = Logger.getLogger(DefaultMutexImplementation.class.getName());

    /** this is used from tests to prevent upgrade from readAccess to writeAccess
     * by strictly throwing exception. Otherwise we just notify that using ErrorManager.
     */
    public static boolean beStrict;

    // lock mode constants

    /** Lock free */
    private static final int NONE = 0x0;

    /** Enqueue all requests */
    private static final int CHAIN = 0x1;

    /** eXclusive */
    private static final int X = 0x2;

    /** Shared */
    private static final int S = 0x3;

    /** number of modes */
    private static final int MODE_COUNT = 0x4;

    /** compatibility matrix */

    // [requested][granted]
    private static final boolean[][] cmatrix = {null,
        null, // NONE, CHAIN
        { true, false, false, false },{ true, false, false, true }
    };

    /** granted mode 
     * @GuaredBy("LOCK")
     */
    private int grantedMode = NONE;
    
    /** The mode the mutex was in before it started chaining 
     * @GuaredBy("LOCK")
     */
    private int origMode;

    /** protects internal data structures */
    private final Object LOCK;
    
    /** wrapper, if any */
    private final Executor wrapper;

    /** threads that - owns or waits for this mutex 
     * @GuaredBy("LOCK")
     */
    private final Map<Thread,ThreadInfo> registeredThreads = new HashMap<Thread,ThreadInfo>(7);

    /** number of threads that holds S mode (readersNo == "count of threads in registeredThreads that holds S") */

    // NOI18N
    private int readersNo = 0;

    /** a queue of waiting threads for this mutex */
    private List<QueueCell> waiters;

    /** identification of the mutex */
    private int cnt;
        
    public static DefaultMutexImplementation create() {
        return new DefaultMutexImplementation();
    }
        
    public static DefaultMutexImplementation usingLock(Object lock) {
        return new DefaultMutexImplementation(lock);
    }
        
    public static DefaultMutexImplementation controlledBy(Privileged p) {
        return new DefaultMutexImplementation(p);
    }
    
    public static DefaultMutexImplementation controlledBy(Privileged p, Executor e) {
        return new DefaultMutexImplementation(p, e);
    }

    
    private DefaultMutexImplementation(Object lock) {
        this.LOCK = init(lock);
        this.wrapper = null;
    }

    private DefaultMutexImplementation() {
        this.LOCK = init(new InternalLock());
        this.wrapper = null;
    }

    private DefaultMutexImplementation(Privileged privileged) {
        if (privileged == null) {
            throw new IllegalArgumentException("privileged == null"); //NOI18N
        } else {
            this.LOCK = init(new InternalLock());
            privileged.setParent(this);
        }
        this.wrapper = null;
    }


    private DefaultMutexImplementation(Privileged privileged, Executor executor) {
        LOCK = new DefaultMutexImplementation(privileged);
        this.wrapper = executor;
    }

    /** Initiates this ReadWriteAccess */
    private Object init(Object lock) {
        this.waiters = new LinkedList<QueueCell>();
        this.cnt = counter++;
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "[" + cnt + "] created here", new Exception());
        }
        return lock;
    }

    @Override
    public void readAccess(Runnable runnable) {
        if (wrapper != null) {
            try {
                doWrapperAccess(null, runnable, true);
                return;
            } catch (MutexException ex) {
                throw new IllegalStateException(ex);
            }
        }
        Thread t = Thread.currentThread();
        readEnter(t, 0);

        try {
            runnable.run();
        } finally {
            leave(t);
        }
    }

    @Override
    public <T> T readAccess(final ExceptionAction<T> action) throws MutexException {
        if (wrapper != null) {
            return doWrapperAccess(action, null, true);
        }

        Thread t = Thread.currentThread();
        readEnter(t, 0);

        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MutexException(e);
        } finally {
            leave(t);
        }
    }

    @Override
    public void writeAccess(Runnable runnable) {
        if (wrapper != null) {
            try {
                doWrapperAccess(null, runnable, false);
            } catch (MutexException ex) {
                throw new IllegalStateException(ex);
            }
            return;
        }

        Thread t = Thread.currentThread();
        writeEnter(t, 0);

        try {
            runnable.run();
        } finally {
            leave(t);
        }
    }

    @Override
    public <T> T writeAccess(ExceptionAction<T> action) throws MutexException {
        if (wrapper != null) {
            return doWrapperAccess(action, null, false);
        }

        Thread t = Thread.currentThread();
        writeEnter(t, 0);

        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MutexException(e);
        } finally {
            leave(t);
        }
    }

    @Override
    public boolean isReadAccess() {
        if (wrapper != null) {
            DefaultMutexImplementation m = (DefaultMutexImplementation)LOCK;
            return m.isReadAccess();
        }

        Thread t = Thread.currentThread();
        ThreadInfo info;

        synchronized (LOCK) {
            info = getThreadInfo(t);

            if (info != null) {
                if (info.counts[S] > 0) {
                    return true;
                }
            }
        }

        return false;
    }
    
    @Override
    public boolean isWriteAccess() {
        if (wrapper != null) {
            DefaultMutexImplementation m = (DefaultMutexImplementation)LOCK;
            return m.isWriteAccess();
        }

        Thread t = Thread.currentThread();
        ThreadInfo info;

        synchronized (LOCK) {
            info = getThreadInfo(t);

            if (info != null) {
                if (info.counts[X] > 0) {
                    return true;
                }
            }
        }

        return false;
    }
    
    @Override
    public void postReadRequest(final Runnable run) {
        postRequest(S, run, null);
    }

    @Override
    public void postWriteRequest(Runnable run) {
        postRequest(X, run, null);
    }

    /** toString */
    @Override
    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuilder sbuff = new StringBuilder(512);
        sbuff.append("DefaultMutexImplementation").append(newline);
        synchronized (LOCK) {
            sbuff.append("threads: ").append(getRegisteredThreads()).append(newline); // NOI18N
            sbuff.append("readersNo: ").append(readersNo).append(newline); // NOI18N
            sbuff.append("waiters: ").append(waiters).append(newline); // NOI18N
            sbuff.append("grantedMode: ").append(getGrantedMode(false)).append(newline); // NOI18N
        }

        return sbuff.toString();
    }

    // priv methods  -----------------------------------------

    /** enters this mutex for writing
     * @param t the value of t
     * @param timeout the value of timeout */
    final boolean writeEnter(Thread t, long timeout) {
        return enter(X, t, timeout);
    }

    /** enters this mutex for reading
     * @param t the value of t
     * @param timeout the value of timeout */
    final boolean readEnter(Thread t, long timeout) {
        return enter(S, t, timeout);
    }

    private void doLog(String action, Object ... params) {
        String tid = Integer.toHexString(Thread.currentThread().hashCode());
        LOG.log(Level.FINER, "[#" + cnt + "@" + tid + "] " + action, params);
    }
    
    /** enters this mutex with given mode
    * @param requested one of S, X
    * @param t
    */
    private boolean enter(int requested, Thread t, long timeout) {
        boolean log = LOG.isLoggable(Level.FINER);

        if (log) doLog("Entering {0}, {1}", requested, timeout); // NOI18N

        boolean ret = enterImpl(requested, t, timeout);

        if (log) doLog("Entering exit: {0}", ret); // NOI18N

        return ret;
    }

    private boolean enterImpl(int requested, Thread t, long timeout) {
        QueueCell cell = null;
        int loopc = 0;

        for (;;) {
            loopc++;
            synchronized (LOCK) {
                // does the thread reenter this mutex?
                ThreadInfo info = getThreadInfo(t);

                if (info != null) {
                    if (getGrantedMode(false) == NONE) {
                        // defensive
                        throw new IllegalStateException();
                    }
                    // reenters
                    // requested == S -> always succeeds
                    // info.mode == X -> always succeeds
                    if (((info.mode == S) && (getGrantedMode(false) == X)) ||
                        ((info.mode == X) && (getGrantedMode(false) == S))) {
                        // defensive
                        throw new IllegalStateException();
                    }
                    if ((info.mode == X) || (info.mode == requested)) {
                        if (info.forced) {
                            info.forced = false;
                        } else {
                            if ((requested == X) && (info.counts[S] > 0)) {
                                IllegalStateException e = new IllegalStateException("WARNING: Going from readAccess to writeAccess, see #10778: http://www.netbeans.org/issues/show_bug.cgi?id=10778 ");

                                if (beStrict) {
                                    throw e;
                                }
                                Exceptions.printStackTrace(e);
                            }
                            info.counts[requested]++;
                            if ((requested == S) &&
                                (info.counts[requested] == 1)) {
                                readersNo++;
                            }
                        }
                        return true;
                    } else if (canUpgrade(info.mode, requested)) {
                        IllegalStateException e = new IllegalStateException("WARNING: Going from readAccess to writeAccess, see #10778: http://www.netbeans.org/issues/show_bug.cgi?id=10778 ");

                        if (beStrict) {
                            throw e;
                        }
                        Exceptions.printStackTrace(e);
                        info.mode = X;
                        info.counts[requested]++;
                        info.rsnapshot = info.counts[S];
                        if (getGrantedMode(false) == S) {
                            setGrantedMode(X);
                        } else if (getGrantedMode(false) == X) {
                            // defensive
                            throw new IllegalStateException();
                        }
                        // else if grantedMode == CHAIN - let it be
                        return true;
                    } else {
                        IllegalStateException e = new IllegalStateException("WARNING: Going from readAccess to writeAccess through queue, see #10778: http://www.netbeans.org/issues/show_bug.cgi?id=10778 ");

                        if (beStrict) {
                            throw e;
                        }
                        Exceptions.printStackTrace(e);
                    }
                } else {
                    if (isCompatible(requested)) {
                        setGrantedMode(requested);
                        getRegisteredThreads().put(t,
                                              info = new ThreadInfo(t, requested));
                        if (requested == S) {
                            readersNo++;
                        }
                        return true;
                    }
                }
                if (timeout == -1) {
                    return false;
                }
                setGrantedMode(CHAIN);
                cell = chain(requested, t, 0);
            }
            // sync
            cell.sleep(timeout);
            if (timeout > 0) {
                // exit immediately next round
                timeout = -1;
            }
        }
         // for
    }
    
    /** privilegedEnter serves for processing posted requests */
    private boolean reenter(Thread t, int mode) {
        boolean log = LOG.isLoggable(Level.FINER);

        if (log) doLog("Re-Entering {0}", mode); // NOI18N

        boolean ret = reenterImpl(t, mode);

        if (log) doLog("Re-Entering exit: {0}", ret); // NOI18N

        return ret;
    }


    private boolean reenterImpl(Thread t, int mode) {
        // from leaveX -> grantedMode is NONE or S
        if (mode == S) {
            if ((getGrantedMode(false) != NONE) && (getGrantedMode(false) != S)) {
                throw new IllegalStateException(this.toString());
            }

            enter(mode, t, 0);

            return false;
        }

        // assert (mode == X)
        ThreadInfo tinfo = getThreadInfo(t);
        boolean chainFromLeaveX = ((getGrantedMode(false) == CHAIN) && (tinfo != null) && (tinfo.counts[X] > 0));

        // process grantedMode == X or CHAIN from leaveX OR grantedMode == NONE from leaveS
        if ((getGrantedMode(false) == X) || (getGrantedMode(false) == NONE) || chainFromLeaveX) {
            enter(mode, t, 0);

            return false;
        } else { // remains grantedMode == CHAIN or S from leaveS, so it will be CHAIN

            if (readersNo == 0) {
                throw new IllegalStateException(this.toString());
            }

            ThreadInfo info = new ThreadInfo(t, mode);
            getRegisteredThreads().put(t, info);

            // prevent from grantedMode == NONE (another thread - leaveS)
            readersNo += 2;

            // prevent from new readers
            setGrantedMode(CHAIN);

            return true;
        }
         // else X means ERROR!!!
    }

    /** @param t holds S (one entry) and wants X, grantedMode != NONE && grantedMode != X */
    private void privilegedEnter(Thread t, int mode) {
        boolean decrease = true;

        synchronized (LOCK) {
            getThreadInfo(t);
        }

        for (;;) {
            QueueCell cell;

            synchronized (LOCK) {
                if (decrease) {
                    decrease = false;
                    readersNo -= 2;
                }

                // always chain this thread
                // since there can be another one
                // in the queue with higher priority
                setGrantedMode(CHAIN);
                cell = chain(mode, t, Integer.MAX_VALUE);

                if (readersNo == 0) { // seems I may enter

                    // no one has higher prio?
                    if (waiters.get(0) == cell) {
                        waiters.remove(0);
                        
                        setGrantedMode(mode);

                        return;
                    } else {
                        setGrantedMode(NONE);
                        wakeUpOthers();
                    }
                }
            }
             // synchronized (LOCK)

            cell.sleep();

            // cell already removed from waiters here
        }
    }

    /** Leaves this mutex */
    final void leave(Thread t) {
        boolean log = LOG.isLoggable(Level.FINER);

        if (log) doLog("Leaving {0}", getGrantedMode(true)); // NOI18N

        leaveImpl(t);

        if (log) doLog("Leaving exit: {0}", getGrantedMode(true)); // NOI18N
    }

    private void leaveImpl(Thread t) {
        ThreadInfo info;
        int postedMode = NONE;
        boolean needLock = false;

        synchronized (LOCK) {
            info = getThreadInfo(t);

            switch (getGrantedMode(false)) {
            case NONE:
                throw new IllegalStateException();

            case CHAIN:

                if (info.counts[X] > 0) {
                    // it matters that X is handled first - see ThreadInfo.rsnapshot
                    postedMode = leaveX(info);
                } else if (info.counts[S] > 0) {
                    postedMode = leaveS(info);
                } else {
                    throw new IllegalStateException();
                }

                break;

            case X:
                postedMode = leaveX(info);

                break;

            case S:
                postedMode = leaveS(info);

                break;
            } // switch

            // do not give up LOCK until queued runnables are run
            if (postedMode != NONE) {
                int runsize = info.getRunnableCount(postedMode);

                if (runsize != 0) {
                    needLock = reenter(t, postedMode); // grab lock
                }
            }
        } // sync

        // check posted requests
        if ((postedMode != NONE) && (info.getRunnableCount(postedMode) > 0)) {
            doLog("Processing posted requests: {0}", postedMode); // NOI18N
            try {
                if (needLock) { // go from S to X or CHAIN
                    privilegedEnter(t, postedMode);
                }

                // holds postedMode lock here
                List<Runnable> runnables = info.dequeue(postedMode);
                final int size = runnables.size();

                for (int i = 0; i < size; i++) {
                    try {
                        Runnable r = runnables.get(i);

                        r.run();
                    }
                    catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                    catch (StackOverflowError e) {
                        // Try as hard as possible to get a real stack trace
                        e.printStackTrace();
                        Exceptions.printStackTrace(e);
                    }
                    catch (ThreadDeath td) {
                        throw td;
                    }
                    catch (Error e) {
                        Exceptions.printStackTrace(e);
                    }
                }
                 // for

                // help gc
                runnables = null;
            } finally {
                leave(t); // release lock grabbed - shared
            }
        }
         // mode
    }

    /** Leaves the lock supposing that info.counts[X] is greater than zero */
    private int leaveX(ThreadInfo info) {
        if ((info.counts[X] <= 0) || (info.rsnapshot > info.counts[S])) {
            // defensive
            throw new IllegalStateException();
        }

        if (info.rsnapshot == info.counts[S]) {
            info.counts[X]--;

            if (info.counts[X] == 0) {
                info.rsnapshot = 0;

                // downgrade the lock
                if (info.counts[S] > 0) {
                    info.mode = S;
                    setGrantedMode(S);
                } else {
                    info.mode = NONE;
                    setGrantedMode(NONE);
                    getRegisteredThreads().remove(info.t);
                }

                if (info.getRunnableCount(S) > 0) {
                    // wake up other readers of this mutex
                    wakeUpReaders();

                    return S;
                }

                // mode has changed
                wakeUpOthers();
            }
        } else {
            // rsnapshot < counts[S]
            if (info.counts[S] <= 0) {
                // defensive
                throw new IllegalStateException();
            }

            if (--info.counts[S] == 0) {
                if (readersNo <= 0) {
                    throw new IllegalStateException();
                }

                readersNo--;

                return X;
            }
        }

        return NONE;
    }

    /** Leaves the lock supposing that info.counts[S] is greater than zero */
    private int leaveS(ThreadInfo info) {
        if ((info.counts[S] <= 0) || (info.counts[X] > 0)) {
            // defensive
            throw new IllegalStateException();
        }

        info.counts[S]--;

        if (info.counts[S] == 0) {
            // remove the thread
            info.mode = NONE;
            getRegisteredThreads().remove(info.t);

            // downsize readersNo
            if (readersNo <= 0) {
                throw new IllegalStateException();
            }

            readersNo--;

            if (readersNo == 0) {
                // set grantedMode to NONE
                // and then wakeUp others - either immediately 
                // or in privelegedEnter()
                setGrantedMode(NONE);

                if (info.getRunnableCount(X) > 0) {
                    return X;
                }

                wakeUpOthers();
            } else if (info.getRunnableCount(X) > 0) {
                return X;
            } else if ((getGrantedMode(false) == CHAIN) && (readersNo == 1)) {
                // can be the mode advanced from CHAIN? Examine first item of waiters!
                for (int i = 0; i < waiters.size(); i++) {
                    QueueCell qc = waiters.get(i);

                    synchronized (qc) {
                        if (qc.isGotOut()) {
                            waiters.remove(i--);

                            continue;
                        }

                        ThreadInfo tinfo = getThreadInfo(qc.t);

                        if (tinfo != null) {
                            if (tinfo.mode == S) {
                                if (qc.mode != X) {
                                    // defensive
                                    throw new IllegalStateException();
                                }

                                if (waiters.size() == 1) {
                                    setGrantedMode(X);
                                }
                                 // else let CHAIN

                                tinfo.mode = X;
                                waiters.remove(i);
                                qc.wakeMeUp();
                            }
                        }
                         // else first request is a first X request of some thread

                        break;
                    }
                     // sync (qc)
                }
                 // for
            }
             // else
        }
         // count[S] == 0

        return NONE;
    }

    /** Adds this thread to the queue of waiting threads
    * @warning LOCK must be held
    */
    private QueueCell chain(final int requested, final Thread t, final int priority) {
        //long timeout = 0;

        /*
        if (killDeadlocksOn) {
            checkDeadlock(requested, t);
            timeout = (isDispatchThread() || checkAwtTreeLock() ? TIMEOUT : 0);
        }
        */
        QueueCell qc = new QueueCell(requested, t);

        //qc.timeout = timeout;
        qc.priority2 = priority;

        final int size = waiters.size();

        if (size == 0) {
            waiters.add(qc);
        } else if (qc.getPriority() == Integer.MAX_VALUE) {
            waiters.add(0, qc);
        } else {
            QueueCell cursor;
            int i = 0;

            do {
                cursor = waiters.get(i);

                if (cursor.getPriority() < qc.getPriority()) {
                    waiters.add(i, qc);

                    break;
                }

                i++;
            } while (i < size);

            if (i == size) {
                waiters.add(qc);
            }
        }

        return qc;
    }

    /** Scans through waiters and wakes up them */
    private void wakeUpOthers() {
        if ((getGrantedMode(false) == X) || (getGrantedMode(false) == CHAIN)) {
            // defensive
            throw new IllegalStateException();
        }

        if (waiters.isEmpty()) {
            return;
        }

        for (int i = 0; i < waiters.size(); i++) {
            QueueCell qc = waiters.get(i);

            synchronized (qc) {
                if (qc.isGotOut()) {
                    // bogus waiter
                    waiters.remove(i--);

                    continue;
                }

                if (isCompatible(qc.mode)) { // woken S -> should I wake X? -> no
                    waiters.remove(i--);
                    qc.wakeMeUp();
                    setGrantedMode(qc.mode);

                    if (getThreadInfo(qc.t) == null) {
                        // force to have a record since recorded threads
                        // do not use isCompatible call
                        ThreadInfo ti = new ThreadInfo(qc.t, qc.mode);
                        ti.forced = true;

                        if (qc.mode == S) {
                            readersNo++;
                        }

                        getRegisteredThreads().put(qc.t, ti);
                    }
                } else {
                    setGrantedMode(CHAIN);

                    break;
                }
            }
             // sync (qc)
        }
    }

    private void wakeUpReaders() {
        assert (getGrantedMode(false) == NONE) || (getGrantedMode(false) == S);

        if (waiters.isEmpty()) {
            return;
        }

        for (int i = 0; i < waiters.size(); i++) {
            QueueCell qc = waiters.get(i);

            synchronized (qc) {
                if (qc.isGotOut()) {
                    // bogus waiter
                    waiters.remove(i--);

                    continue;
                }

                if (qc.mode == S) { // readers only
                    waiters.remove(i--);
                    qc.wakeMeUp();
                    setGrantedMode(S);

                    if (getThreadInfo(qc.t) == null) {
                        // force to have a record since recorded threads
                        // do not use isCompatible call
                        ThreadInfo ti = new ThreadInfo(qc.t, qc.mode);
                        ti.forced = true;
                        readersNo++;
                        getRegisteredThreads().put(qc.t, ti);
                    }
                }
            }
             // sync (qc)
        }
    }

    /** Posts new request for current thread.
     * This method is pacakge-private only to allow access to o.o.openide.Mutex subclass.
    * @param mutexMode mutex mode for which the action is rquested
    * @param run the action
    */
    // published by bytecode patching
    void postRequest(final int mutexMode, final Runnable run, Executor exec) {
        if (wrapper != null) {
            DefaultMutexImplementation m = (DefaultMutexImplementation)LOCK;
            m.postRequest(mutexMode, run, wrapper);
            return;
        }

        final Thread t = Thread.currentThread();
        ThreadInfo info;

        synchronized (LOCK) {
            info = getThreadInfo(t);

            if (info != null) {
                // the same mode and mutex is not entered in the other mode
                // assert (mutexMode == S || mutexMode == X)
                if ((mutexMode == info.mode) && (info.counts[(S + X) - mutexMode] == 0)) {
                    enter(mutexMode, t, 0);
                } else { // the mutex is held but can not be entered in X mode
                    info.enqueue(mutexMode, run);

                    return;
                }
            }
        }

        // this mutex is not held
        if (info == null) {
            if (exec != null) {
                class Exec implements Runnable {
                    @Override
                    public void run() {
                        enter(mutexMode, t, 0);
                        try {
                            run.run();
                        } finally {
                            leave(t);
                        }
                    }
                }
                exec.execute(new Exec());
                return;
            }
            
            enter(mutexMode, t, 0);
            try {
                run.run();
            } finally {
                leave(t);
            }

            return;
        }

        // run it immediately
        // info != null so enter(...) succeeded
        try {
            run.run();
        } finally {
            leave(t);
        }
    }

    /** @param requested is requested mode of locking
    * @return <tt>true</tt> if and only if current mode and requested mode are compatible
    */
    private boolean isCompatible(int requested) {
        // allow next reader in even in chained mode, if it was read access before
        if (requested == S && getGrantedMode(false) == CHAIN && getOrigMode() == S) return true;
        return cmatrix[requested][getGrantedMode(false)];
    }

    private ThreadInfo getThreadInfo(Thread t) {
        return getRegisteredThreads().get(t);
    }

    private boolean canUpgrade(int threadGranted, int requested) {
        return (threadGranted == S) && (requested == X) && (readersNo == 1);
    }
    
    // -------------------------------- WRAPPERS --------------------------------
    
    private <T> T doWrapperAccess(
        final ExceptionAction<T> action,
        final Runnable runnable,
        final boolean readOnly) throws MutexException {
        class R implements Runnable {
           T ret;
           MutexException e;

           @Override
           public void run() {
               DefaultMutexImplementation m = (DefaultMutexImplementation)LOCK;
               try {
                   if (readOnly) {
                       if (action != null) {
                           ret = m.readAccess(action);
                       } else {
                           m.readAccess(runnable);
                       }
                   } else {
                       if (action != null) {
                           ret = m.writeAccess(action);
                       } else {
                           m.writeAccess(runnable);
                       }
                   }
               } catch (MutexException ex) {
                   e = ex;
               }
           }
       }
       R run = new R();
       DefaultMutexImplementation m = (DefaultMutexImplementation)LOCK;
       if (m.isWriteAccess() || m.isReadAccess()) {
           run.run();
       } else {
           wrapper.execute(run);
       }
       if (run.e != null) {
           throw run.e;
       }
       return run.ret;
    }

    private static final class ThreadInfo {
        /** t is forcibly sent from waiters to enter() by wakeUpOthers() */
        boolean forced;

        /** ThreadInfo for this Thread */
        final Thread t;

        /** granted mode */
        int mode;

        // 0 - NONE, 1 - CHAIN, 2 - X, 3 - S

        /** enter counter */
        int[] counts;

        /** queue of runnable rquests that are to be executed (in X mode) right after S mode is left
        * deadlock avoidance technique
        */
        List<Runnable>[] queues;

        /** value of counts[S] when the mode was upgraded
        * rsnapshot works as follows:
        * if a thread holds the mutex in the S mode and it reenters the mutex
        * and requests X and the mode can be granted (no other readers) then this
        * variable is set to counts[S]. This is used in the leave method in the X branch.
        * (X mode is granted by other words)
        * If rsnapshot is less than counts[S] then the counter is decremented etc. If the rsnapshot is
        * equal to count[S] then count[X] is decremented. If the X counter is zeroed then
        * rsnapshot is zeroed as well and current mode is downgraded to S mode.
        * rsnapshot gets less than counts[S] if current mode is X and the mutex is reentered
        * with S request.
        */
        int rsnapshot;

        @SuppressWarnings("unchecked")
        public ThreadInfo(Thread t, int mode) {
            this.t = t;
            this.mode = mode;
            this.counts = new int[MODE_COUNT];
            this.queues = (List<Runnable>[])new List[MODE_COUNT];
            counts[mode] = 1;
        }

        @Override
        public String toString() {
            return super.toString() + " thread: " + t + " mode: " + mode + " X: " + counts[2] + " S: " + counts[3]; // NOI18N
        }

        /** Adds the Runnable into the queue of waiting requests */
        public void enqueue(int mode, Runnable run) {
            if (queues[mode] == null) {
                queues[mode] = new ArrayList<>(13);
            }

            queues[mode].add(run);
        }

        /** @return a List of enqueued Runnables - may be null */
        public List<Runnable> dequeue(int mode) {
            List<Runnable> ret = queues[mode];
            queues[mode] = null;
            return ret;
        }

        public int getRunnableCount(int mode) {
            return ((queues[mode] == null) ? 0 : queues[mode].size());
        }
    }

    /** This class is defined only for better understanding of thread dumps where are informations like
    * java.lang.Object@xxxxxxxx owner thread_x
    *   wait for enter thread_y
    */
    private static final class InternalLock {
        InternalLock() {
        }
    }

    private static final class QueueCell {
        int mode;
        Thread t;
        boolean signal;
        boolean left;

        /** priority of the cell */
        int priority2;

        public QueueCell(int mode, Thread t) {
            this.mode = mode;
            this.t = t;
            this.left = false;
            this.priority2 = 0;
        }

        @Override
        public String toString() {
            return super.toString() + " mode: " + mode + " thread: " + t; // NOI18N
        }

        /** @return priority of this cell */
        public long getPriority() {
            return ((priority2 == 0) ? t.getPriority() : priority2);
        }

        /** @return true iff the thread left sleep */
        public boolean isGotOut() {
            return left;
        }

        /** current thread will sleep until wakeMeUp is called
        * if wakeMeUp was already called then the thread will not sleep
        */
        public void sleep() {
            sleep(0);
        }
        synchronized void sleep(long timeout) {
            boolean wasInterrupted = false;
            try {
                while (!signal) {
                    try {
                        long start = System.currentTimeMillis();
                        wait(timeout);
                        /*
                        if (LOG.isLoggable(Level.FINE) && EventQueue.isDispatchThread() && (System.currentTimeMillis() - start) > 1000) {
                            LOG.log(Level.WARNING, toString(), new IllegalStateException("blocking on a mutex from EQ"));
                        }
                        */
                        return;
                    } catch (InterruptedException e) {
                        wasInterrupted = true;
                        LOG.log(Level.FINE, null, e);
                    }
                }
            } finally {
                left = true;
                if (wasInterrupted) { // #129003
                    Thread.currentThread().interrupt();
                }
            }
        }

        /** sends signal to a sleeper - to a thread that is in the sleep() */
        public void wakeMeUp() {
            signal = true;
            notifyAll();
        }
    }
    
    /** Provides access to ReadWriteAccess's internal methods.
     *
     * This class can be used when one wants to avoid creating a
     * bunch of Runnables. Instead,
     * <pre>
     * try {
     *     enterXAccess ();
     *     yourCustomMethod ();
     * } finally {
     *     exitXAccess ();
     * }
     * </pre>
     * can be used.
     *
     * You must, however, control the related ReadWriteAccess, i.e. you must be creator of
     * the ReadWriteAccess.
     *
     * @since 1.17
     */
    @SuppressWarnings("PublicInnerClass")
    public static class Privileged {
        private DefaultMutexImplementation parent;

        final void setParent(DefaultMutexImplementation parent) {
            this.parent = parent;
        }

        public void enterReadAccess() {
            parent.readEnter(Thread.currentThread(), 0);
        }
        
        /** Tries to obtain read access. If the access cannot by
         * gained by given milliseconds, the method returns without gaining
         * it.
         * 
         * @param timeout amount of milliseconds to wait before giving up.
         *   <code>0</code> means to wait indefinitely.
         *   <code>-1</code> means to not wait at all and immediately exit
         * @return <code>true</code> if the access has been granted, 
         *   <code>false</code> otherwise
         * @since 8.37
         */
        public boolean tryReadAccess(long timeout) {
            return parent.readEnter(Thread.currentThread(), timeout);
        }

        public void enterWriteAccess() {
            parent.writeEnter(Thread.currentThread(), 0);
        }
        
        /**
         * Tries to obtain write access. If the access cannot by gained by given
         * milliseconds, the method returns without gaining it.
         *
         * @param timeout amount of milliseconds to wait before giving up.
         *   <code>0</code> means to wait indefinitely.
         *   <code>-1</code> means to not wait at all and immediately exit
         * @return <code>true</code> if the access has been granted,
         * <code>false</code> otherwise
         * @since 8.37
         */
        public boolean tryWriteAccess(long timeout) {
            return parent.writeEnter(Thread.currentThread(), timeout);
        }

        public void exitReadAccess() {
            parent.leave(Thread.currentThread());
        }

        public void exitWriteAccess() {
            parent.leave(Thread.currentThread());
        }
    }

    private void setGrantedMode(int mode) {
        assert Thread.holdsLock(LOCK);
        if (grantedMode != CHAIN && mode == CHAIN) {
            this.origMode = grantedMode;
        }
        grantedMode = mode;
    }
    
    private int getGrantedMode(boolean skipCheck) {
        assert skipCheck || Thread.holdsLock(LOCK);
        return grantedMode;
    }

    private int getOrigMode() {
        assert Thread.holdsLock(LOCK);
        return origMode;
    }

    private Map<Thread,ThreadInfo> getRegisteredThreads() {
        assert Thread.holdsLock(LOCK);
        return registeredThreads;
    }
}
