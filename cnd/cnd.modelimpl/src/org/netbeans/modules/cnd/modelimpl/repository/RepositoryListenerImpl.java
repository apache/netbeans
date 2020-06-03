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
package org.netbeans.modules.cnd.modelimpl.repository;

import org.netbeans.modules.cnd.repository.api.RepositoryListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.Timer;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.repository.api.*;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * RepositoryListener implementation.
 * Watches implicit and explicit opening of units;
 * ensures that implicitly opened units are closed 
 * after the specified interval has passed
 */
public class RepositoryListenerImpl implements RepositoryListener, RepositoryExceptionListener {

    /** Singleton's instance */
    private static final RepositoryListenerImpl instance = new RepositoryListenerImpl();
    /** Interval, in seconds, after which implicitly opened unit should be closed */
    private static final int IMPLICIT_CLOSE_INTERVAL = Integer.getInteger("cnd.implicit.close.interval", 20); // NOI18N
    private static final String TRACE_PROJECT_NAME = System.getProperty("cnd.repository.trace.project"); //NOI18N    
    private static final boolean TRACE_PROJECT = (TRACE_PROJECT_NAME != null && TRACE_PROJECT_NAME.length() > 0);

    /** A shutdown hook to guarantee that repository is shutted down */
    private static class RepositoryShutdownHook extends Thread {

        public RepositoryShutdownHook() {
            setName("Repository Shutdown Hook Thread"); // NOI18N
        }

        @Override
        public void run() {
            if (!CndUtils.isUnitTestMode()) {
                RepositoryUtils.shutdown();
            }
        }
    }

    /** 
     * A pair of (unit name, timer) 
     * used to track implicitly opened units
     */
    private class UnitTimer implements ActionListener {

        private final int unitId;
        private final Timer timer;

        public UnitTimer(int unitId, int interval) {
            this.unitId = unitId;
            timer = new Timer(interval, this);
            timer.start();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            timeoutElapsed(unitId);
        }

        public void cancel() {
            timer.stop();
        }
    }
    /** Access both unitTimers and explicitelyOpened only under this lock! */
    private static final class Lock {}
    private final Object lock = new Lock();
    /** 
     * Implicitly opened units.
     * Access only under the lock!
     */
    private final Map<Integer, UnitTimer> unitTimers = new HashMap<>();
    /** 
     * Explicitly opened units.
     * Access only under the lock!
     */
    private final Set<Integer> explicitelyOpened = new HashSet<>();

    private RepositoryListenerImpl() {
        Runtime.getRuntime().addShutdownHook(new RepositoryShutdownHook());
    }

    /** Singleton's getter */
    public static RepositoryListenerImpl instance() {
        return instance;
    }

    /** RepositoryListener implementation */
    @Override
    public boolean unitOpened(final int unitId) {
        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
            trace("RepositoryListener: unitOpened %d\n", unitId); // NOI18N
        }
        //commented out as we do not have project name
//        if (TRACE_PROJECT && TRACE_PROJECT_NAME.equals(unitName)) {
//            trace("Watched project %s is opening\n", unitName); // NOI18N
//        }
        synchronized (lock) {
            if (!explicitelyOpened.contains(unitId)) {
                if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                    trace("RepositoryListener: implicit open !!! %d\n", unitId); // NOI18N
                }
                unitTimers.put(unitId, new UnitTimer(unitId,  IMPLICIT_CLOSE_INTERVAL * 1000));
            }
        }
        return true;
    }

//    @Override
//    public boolean repositoryOpened(int repositoryId, CacheLocation cacheLocation) {
//        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
//            trace("RepositoryListener: repositoryOpened %s\n", cacheLocation.getLocation().getAbsolutePath()); //NOI18N // NOI18N
//        }
//        return true;
//    }

    /** RepositoryListener implementation */
    @Override
    public void unitClosed(final int unitId) {
        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
            trace("RepositoryListener: unitClosed %d\n", unitId); // NOI18N
        }
//        if (TRACE_PROJECT && TRACE_PROJECT_NAME.equals(unitName)) {
//            trace("Watched project %s is explicitly closing\n", unitName); // NOI18N
//        }
        synchronized (lock) {
            killTimer(unitId);
            explicitelyOpened.remove(unitId);
        }
    }

    @Override
    public void unitRemoved(int unitId) {
    }
    
    /** RepositoryListener implementation */
    @Override
    public void anExceptionHappened(final int unitId, final CharSequence unitName, RepositoryException exc) {
        assert exc != null;
        if (TraceFlags.DEBUG_BROKEN_REPOSITORY && exc.getMessage() != null && exc.getMessage().contains("INTENTIONAL")) { // NOI18N
            return;
        }
        if (exc.getCause() != null) {
            DiagnosticExceptoins.register(exc.getCause()); 
       }
    }

    // NB: un-synchronized!
    private void killTimer(int unitId) {
        UnitTimer unitTimer = unitTimers.remove(unitId);
        if (unitTimer != null) {
            if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                trace("RepositoryListener: killing timer for %d %s\n", unitId, KeyUtilities.getUnitName(unitId)); // NOI18N
            }
            unitTimer.cancel();
        }
    }

    public void onExplicitOpen(int unitId) {
        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
            trace("RepositoryListener: onExplicitOpen %d %s\n", unitId, KeyUtilities.getUnitName(unitId)); // NOI18N
        }
        synchronized (lock) {
            killTimer(unitId);
            explicitelyOpened.add(unitId);
        }
    }

    public void onExplicitClose(CharSequence unitName) {
        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
            trace("RepositoryListener: onExplicitClose %s\n", unitName); // NOI18N
        }
    }

    private void timeoutElapsed(final int unitId) {
        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
            trace("RepositoryListener: timeout elapsed for %d\n", unitId); // NOI18N
        }
        synchronized (lock) {
            UnitTimer unitTimer = unitTimers.remove(unitId);
            if (unitTimer != null) {
                if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                    trace("RepositoryListener: scheduling closure for %d\n", unitId); // NOI18N
                }
                unitTimer.cancel();
                scheduleClosing(unitId);
            }
        }
    }

    private void scheduleClosing(final int unitId) {
        assert Thread.holdsLock(lock);
        final CharSequence unitName = KeyUtilities.getUnitName(unitId);
        if (explicitelyOpened.contains(unitId)) {
            if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                trace("Cancelling closure (A) for implicitely opened unit %s\n", unitName); // NOI18N
            }
            return;
        }
        ModelImpl.instance().enqueueModelTask(new Runnable() {

            @Override
            public void run() {
                synchronized (lock) {
                    if (explicitelyOpened.contains(unitId)) {
                        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                            trace("Cancelling closure (B) for implicitely opened unit %s\n", unitName); // NOI18N
                        }
                        return;
                    }
                }
                if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                    trace("RepositoryListener: closing implicitely opened unit %s\n", unitName); // NOI18N
                }
                if (TRACE_PROJECT && TRACE_PROJECT_NAME.equals(unitName)) {
                    trace("Watched project %s is implicitely closing\n", unitName); // NOI18N
                }
                RepositoryUtils.closeUnit(unitId, null, !TraceFlags.PERSISTENT_REPOSITORY); // null means the list of required units stays unchanged
            }
        }, "Closing implicitly opened project " + unitName + ":" + unitId); // NOI18N
    }

    private void trace(String format, Object... args) {
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = Long.valueOf(System.currentTimeMillis());
        System.arraycopy(args, 0, newArgs, 1, args.length);
        System.err.printf("RepositoryListener [%d] " + format, newArgs); // NOI18N
    }
}
