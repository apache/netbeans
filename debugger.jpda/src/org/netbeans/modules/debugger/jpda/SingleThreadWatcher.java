/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

    private JPDAThreadImpl t;
    private Task watchTask;

    public SingleThreadWatcher(JPDAThreadImpl t) {
        this.t = t;
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
