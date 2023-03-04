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
package org.netbeans.lib.profiler.results.locks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.global.TransactionalSupport;
import org.netbeans.lib.profiler.results.BaseCallGraphBuilder;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUProfilingResultListener;
import org.netbeans.lib.profiler.results.memory.MemoryProfilingResultsListener;

/**
 *
 * @author Tomas Hurka
 */
public class LockGraphBuilder extends BaseCallGraphBuilder implements LockProfilingResultListener, LockCCTProvider {

    static final Logger LOG = Logger.getLogger(LockGraphBuilder.class.getName());
    private final ThreadInfos threadInfos = new ThreadInfos();
    private Map<Integer, MonitorInfo> monitorInfos = new HashMap<>();
    private final TransactionalSupport transaction = new TransactionalSupport();

    @Override
    protected RuntimeCCTNode getAppRootNode() {
        Map<ThreadInfo, List<List<ThreadInfo.MonitorDetail>>> threadsCopy = new HashMap<>(threadInfos.threadInfos.length);
        Map<MonitorInfo, List<List<MonitorInfo.ThreadDetail>>> monitorsCopy = new HashMap<>(monitorInfos.size());

        for (ThreadInfo ti : threadInfos.threadInfos) {
            if (ti != null) {
                List<List<ThreadInfo.MonitorDetail>> monitors = new ArrayList<>(2);
                
                if (!ti.isEmpty()) {
                    monitors.add(ti.cloneWaitMonitorDetails());
                    monitors.add(ti.cloneOwnerMonitorDetails());
                    threadsCopy.put(ti, monitors);
                }
            }
        }
        for (MonitorInfo mi : monitorInfos.values()) {
            List<List<MonitorInfo.ThreadDetail>> threads = new ArrayList<>(2);
            
            threads.add(mi.cloneWaitThreadDetails());
            threads.add(mi.cloneOwnerThreadDetails());
            monitorsCopy.put(mi, threads);
        }
        return new LockRuntimeCCTNode(threadsCopy, monitorsCopy);
    }

    @Override
    protected void doBatchStart() {
        transaction.beginTrans(true);
    }

    @Override
    protected void doBatchStop() {
        transaction.endTrans();
    }

    @Override
    protected void doReset() {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Do Reset called");
        }
        boolean threadLocked = transaction.beginTrans(true, true);

        if (threadLocked) { // ignore request for reset received durin an ongoing active transaction

            try {
                threadInfos.reset();
                monitorInfos = new HashMap<>();
            } finally {
                transaction.endTrans();
            }
        }
    }

    @Override
    protected void doShutdown() {
        threadInfos.reset();
        monitorInfos = new HashMap<>();
    }

    @Override
    protected void doStartup(ProfilerClient profilerClient) {
        // do nothing
    }

    @Override
    public void monitorEntry(int threadId, long timeStamp0, long timeStamp1, int monitorId, int ownerThreadId) {
        ThreadInfo ti = getThreadInfo(threadId);

        if (ti == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Monitor entry thread id = {0}, mId = {1}, owner id = {2}", new Object[]{threadId, Integer.toHexString(monitorId), ownerThreadId});
        }
        MonitorInfo m = getMonitorInfo(monitorId);
        ThreadInfo ownerTi = getThreadInfo(ownerThreadId);
        assert ownerTi != null;
        ti.openMonitor(ownerTi, m, timeStamp0);
        m.openThread(ti, ownerTi, timeStamp0);
    }

    @Override
    public void monitorExit(int threadId, long timeStamp0, long timeStamp1, int monitorId) {
        ThreadInfo ti = getThreadInfo(threadId);

        if (ti == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Monitor exit thread id = {0}, mId = {1}", new Object[]{threadId, Integer.toHexString(monitorId)});
        }
        MonitorInfo m = getMonitorInfo(monitorId);
        ti.closeMonitor(m, timeStamp0);
        m.closeThread(ti, timeStamp0);
        batchNotEmpty = true;
    }

    @Override
    public void newThread(int threadId, String threadName, String threadClassName) {
        if (!isReady()) {
            return;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "New thread creation for thread id = {0}, name = {1}", new Object[]{threadId, threadName});
        }
        threadInfos.newThreadInfo(threadId, threadName, threadClassName);
    }

    @Override
    public void newMonitor(int hash, String className) {
        if (!isReady()) {
            return;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "New monitor creation, mId = {0}, className = {1}", new Object[]{Integer.toHexString(hash), className});
        }
        registerNewMonitor(hash,className);
    }

/*
    @Override
    public void sleepEntry(int threadId, long timeStamp0, long timeStamp1) {
        ThreadInfo ti = getThreadInfo(threadId);

        if (ti == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Sleep entry thread id = {0}", threadId);
        }
    }

    @Override
    public void sleepExit(int threadId, long timeStamp0, long timeStamp1) {
        ThreadInfo ti = getThreadInfo(threadId);

        if (ti == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Sleep exit thread id = {0}", threadId);
        }
    }
*/

    public void profilingPoint(final int threadId, final int ppId, final long timeStamp) {
        // do nothing
    }

    @Override
    public void timeAdjust(int threadId, long timeDiff0, long timeDiff1) {
        ThreadInfo ti = getThreadInfo(threadId);

        if (ti == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Time adjust thread id = {0}, time = {1}, CPU time = {2}", new Object[]{threadId, timeDiff0, timeDiff1});
        }
        ti.timeAdjust(timeDiff0);
    }
/*
    @Override
    public void waitEntry(int threadId, long timeStamp0, long timeStamp1) {
        ThreadInfo ti = getThreadInfo(threadId);

        if (ti == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Wait entry thread id = {0}", threadId);
        }
    }

    @Override
    public void waitExit(int threadId, long timeStamp0, long timeStamp1) {
        ThreadInfo ti = getThreadInfo(threadId);

        if (ti == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Wait exit thread id = {0}", threadId);
        }
    }

    @Override
    public void parkEntry(int threadId, long timeStamp0, long timeStamp1) {
        ThreadInfo ti = getThreadInfo(threadId);

        if (ti == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Park entry thread id = {0}", threadId);
        }
    }

    @Override
    public void parkExit(int threadId, long timeStamp0, long timeStamp1) {
        ThreadInfo ti = getThreadInfo(threadId);

        if (ti == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Park entry thread id = {0}", threadId);
        }
    }
*/
    private boolean isReady() {
        return (status != null);
    }

    private ThreadInfo getThreadInfo(int threadId) {
        if (!isReady()) {
            return null;
        }

        return threadInfos.getThreadInfo(threadId);
    }

    private MonitorInfo getMonitorInfo(int monitorId) {
        Integer mid = monitorId;
        MonitorInfo mi = monitorInfos.get(mid);
        if (mi == null) {
            mi = new MonitorInfo(monitorId);
            monitorInfos.put(mid, mi);
        }
        return mi;
    }

    private void registerNewMonitor(int monitorId, String className) {
        Integer mid = monitorId;
        MonitorInfo mi = monitorInfos.get(mid);
        if (mi == null) {
            mi = new MonitorInfo(monitorId,className);
            monitorInfos.put(mid, mi);        
        } else {
            mi.setClassName(className);
        }
    }
    
    public static final class CPULockGraphBuilder extends LockGraphBuilder implements CPUProfilingResultListener {

        @Override
        public void methodEntry(int methodId, int threadId, int methodType, long timeStamp0, long timeStamp1, List parameters, int[] methoIds) {
        }

        @Override
        public void methodEntryUnstamped(int methodId, int threadId, int methodType, List parameters, int[] methoIds) {
        }

        @Override
        public void methodExit(int methodId, int threadId, int methodType, long timeStamp0, long timeStamp1, Object retVal) {
        }

        @Override
        public void methodExitUnstamped(int methodId, int threadId, int methodType) {
        }

        @Override
        public void servletRequest(int threadId, int requestType, String servletPath, int sessionId) {
        }

        @Override
        public void sleepEntry(int threadId, long timeStamp0, long timeStamp1) {
        }

        @Override
        public void sleepExit(int threadId, long timeStamp0, long timeStamp1) {
        }

        @Override
        public void threadsResume(long timeStamp0, long timeStamp1) {
        }

        @Override
        public void threadsSuspend(long timeStamp0, long timeStamp1) {
        }

        @Override
        public void waitEntry(int threadId, long timeStamp0, long timeStamp1) {
        }

        @Override
        public void waitExit(int threadId, long timeStamp0, long timeStamp1) {
        }

        @Override
        public void parkEntry(int threadId, long timeStamp0, long timeStamp1) {
        }

        @Override
        public void parkExit(int threadId, long timeStamp0, long timeStamp1) {
        }
        
    }
    public static final class MemoryLockGraphBuilder extends LockGraphBuilder implements MemoryProfilingResultsListener {

        @Override
        public void onAllocStackTrace(char classId, long objSize, int[] methodIds) {
        }

        @Override
        public void onGcPerformed(char classId, long objectId, int objEpoch) {
        }

        @Override
        public void onLivenessStackTrace(char classId, long objectId, int objEpoch, long objSize, int[] methodIds) {
        }
        
    }
}
