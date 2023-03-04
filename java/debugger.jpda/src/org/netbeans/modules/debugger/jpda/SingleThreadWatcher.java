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

package org.netbeans.modules.debugger.jpda;

import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.RequestProcessor.Task;

/**
 * Watches execution in a single thread and detects whether it is blocked
 * by some other suspended threads.
 *
 * @author Martin Entlicher
 */
public class SingleThreadWatcher implements Runnable {

    private static final int DELAY = 3000;

    private final JPDAThreadImpl t;
    private Task watchTask;

    public SingleThreadWatcher(JPDAThreadImpl t) {
        this.t = t;
        if (!Boolean.valueOf(Bundle.USE_JPDA_DEADLOCK_DETECTOR())) {
            return;
        }
        //System.err.println("\nnew SingleThreadWatcher("+t+")");
        watchTask = t.getDebugger().getRequestProcessor().post(this, DELAY);
    }

    public synchronized void destroy() {
        if (watchTask == null) {
            return ;
        }
        watchTask.cancel();
        watchTask = null;
        //t.setLockerThreads(null);
    }

    public void run() {
        synchronized (this) {
            if (watchTask == null) {
                return ;
            }
        }
        if (t.getDebugger().getState() == JPDADebuggerImpl.STATE_DISCONNECTED) {
            destroy();
            return;
        }
        //boolean areLocks =
        t.checkForBlockingThreads();
        //if (!areLocks) {
            synchronized (this) {
                if (watchTask != null) {
                    watchTask.schedule(DELAY);
                }
            }
        //}
    }

    /*private boolean checkLocks() {
        ThreadReference tr = t.getThreadReference();
        VirtualMachine vm = tr.virtualMachine();
        Map<JPDAThread, Variable> threadsWithMonitors = null;
        //synchronized (t.getDebugger().LOCK) { - can not synchronize on that - method invocation uses this lock.
            vm.suspend();
            try {
                ObjectReference waitingMonitor = tr.currentContendedMonitor();
                if (waitingMonitor != null) {
                    Map<ThreadReference, ObjectReference> lockedThreadsWithMonitors = findLockPath(tr, waitingMonitor);
                    if (lockedThreadsWithMonitors != null) {
                        threadsWithMonitors = new LinkedHashMap<JPDAThread, Variable>(lockedThreadsWithMonitors.size());
                        for (ThreadReference ltr : lockedThreadsWithMonitors.keySet()) {
                            JPDAThread lt = t.getDebugger().getThread(ltr);
                            ObjectReference or = lockedThreadsWithMonitors.get(ltr);
                            Variable m = new ThisVariable (t.getDebugger(), or, "" + or.uniqueID());
                            threadsWithMonitors.put(lt, m);
                        }
                    }
                }
            } catch (IncompatibleThreadStateException ex) {
            } finally {
                vm.resume();
            }
        //}
        t.setLockerThreads(threadsWithMonitors);
        return threadsWithMonitors != null;
    }*/

    /*
    private Map<ThreadReference, ObjectReference> findLockPath(ThreadReference tr, ObjectReference waitingMonitor) throws IncompatibleThreadStateException {
        Map<ThreadReference, ObjectReference> threadsWithMonitors = new LinkedHashMap<ThreadReference, ObjectReference>();
        Map<ObjectReference, ThreadReference> monitorMap = new HashMap<ObjectReference, ThreadReference>();
        for (ThreadReference t : tr.virtualMachine().allThreads()) {
            List<ObjectReference> monitors = t.ownedMonitors();
            for (ObjectReference m : monitors) {
                monitorMap.put(m, t);
            }
        }
        while (tr != null && waitingMonitor != null) {
            tr = monitorMap.get(waitingMonitor);
            if (tr != null) {
                if (tr.suspendCount() > 1) { // Add it if it was suspended before
                    threadsWithMonitors.put(tr, waitingMonitor);
                }
                waitingMonitor = tr.currentContendedMonitor();
            }
        }
        if (threadsWithMonitors.size() > 0) {
            return threadsWithMonitors;
        } else {
            return null;
        }
    }
     */
}
