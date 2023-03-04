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

/**
 *
 * @author Tomas Hurka
 */
class ThreadInfo {

    private final int threadId;
    private final Map<MonitorInfo, MonitorDetail> waitMonitors;
    private final Map<MonitorInfo, MonitorDetail> ownerMonitors;
    private OpenMonitor openMonitor;
    private final String threadName;
    private final String threadClassName;

    ThreadInfo(int id, String name, String className) {
        threadId = id;
        threadName = name;
        threadClassName = className;
        waitMonitors = new HashMap<>();
        ownerMonitors = new HashMap<>();
    }

    void openMonitor(ThreadInfo owner, MonitorInfo mi, long timeStamp) {
        assert openMonitor == null;
        openMonitor = new OpenMonitor(mi, owner, timeStamp);
    }

    void closeMonitor(MonitorInfo mi, long timeStamp) {
        assert openMonitor != null;
        assert mi.equals(openMonitor.monitor);
        long wait = timeStamp - openMonitor.timeStamp;
        if (LockGraphBuilder.LOG.isLoggable(Level.FINEST)) {
            LockGraphBuilder.LOG.log(Level.FINEST, "Monitor exit mId = {0}, time diff = {1}", new Object[]{Integer.toHexString(mi.hashCode()), wait});
        }
        addMonitor(waitMonitors, mi, openMonitor.owner, wait);
        addMonitor(openMonitor.owner.ownerMonitors, mi, this, wait);
        openMonitor = null;
    }

    private static void addMonitor(Map<MonitorInfo,MonitorDetail> monitors, MonitorInfo mi, ThreadInfo ti, long wait) {
        MonitorDetail m = monitors.get(mi);
        if (m == null) {
            m = new MonitorDetail(mi);
            monitors.put(mi, m);
        }
        m.addWait(ti, wait);
    }

    void timeAdjust(long timeDiff) {
        if (openMonitor != null) {
            openMonitor.timeAdjust(timeDiff);
            openMonitor.monitor.timeAdjust(this, timeDiff);
        }
    }
    
    boolean isEmpty() {
        return waitMonitors.isEmpty() && ownerMonitors.isEmpty();
    }

    List<MonitorDetail> cloneWaitMonitorDetails() {
        return cloneMonitorDetails(waitMonitors);
    }

    List<MonitorDetail> cloneOwnerMonitorDetails() {
        return cloneMonitorDetails(ownerMonitors);
    }

    private static List<MonitorDetail> cloneMonitorDetails(Map<MonitorInfo,MonitorDetail> monitors) {
        List details = new ArrayList(monitors.size());
        for (MonitorDetail m : monitors.values()) {
            details.add(new MonitorDetail(m));
        }
        return details;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ThreadInfo) {
            ThreadInfo mi = (ThreadInfo) obj;
            return mi.threadId == threadId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return threadId;
    }

    String getName() {
        return threadName;
    }

    private static class OpenMonitor {

        final MonitorInfo monitor;
        final ThreadInfo owner;
        long timeStamp;

        OpenMonitor(MonitorInfo mi, ThreadInfo ti, long ts0) {
            assert mi != null;
            assert ti != null;
            monitor = mi;
            owner = ti;
            timeStamp = ts0;
        }

        private void timeAdjust(long timeDiff) {
            timeStamp += timeDiff;
        }
    }

    static class MonitorDetail {

        final MonitorInfo monitor;
        final Map<ThreadInfo, MonitorInfo.ThreadDetail> threads;
        long count;
        long waitTime;

        private MonitorDetail(MonitorInfo mi) {
            monitor = mi;
            threads = new HashMap<>();
        }

        MonitorDetail(MonitorDetail m) {
            monitor = m.monitor;
            count = m.count;
            waitTime = m.waitTime;
            threads = new HashMap<>();
            for (MonitorInfo.ThreadDetail td : m.threads.values()) {
                threads.put(td.threadInfo, td);
            }
        }

        List<MonitorInfo.ThreadDetail> cloneThreadDetails() {
            return MonitorInfo.cloneThreadDetails(threads);
        }

        private void addWait(ThreadInfo ti, long wait) {
            waitTime += wait;
            count++;
            if (ti != null) {
                addThread(ti, wait);
            }
        }
        
        private void addThread(ThreadInfo ti, long wait) {
            MonitorInfo.ThreadDetail td = threads.get(ti);
            
            if (td == null) {
                td = new MonitorInfo.ThreadDetail(ti);
                threads.put(ti, td);
            }
            td.addWait(null, wait);
        }
    }
}
