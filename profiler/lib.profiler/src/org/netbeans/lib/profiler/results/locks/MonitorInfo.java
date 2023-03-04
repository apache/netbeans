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
import org.netbeans.lib.profiler.utils.StringUtils;

/**
 *
 * @author Tomas Hurka
 */
class MonitorInfo {

    private final int monitorId;
    private String className;
    private Map<ThreadInfo, OpenThread> openThreads;
    private Map<ThreadInfo, ThreadDetail> waitThreads;
    private Map<ThreadInfo, ThreadDetail> ownerThreads;

    MonitorInfo(int id) {
        monitorId = id;
        waitThreads = new HashMap<>();
        ownerThreads = new HashMap<>();
        openThreads = new HashMap<>();
        className = "*unknown*"; // NOI18N
    }
    
    MonitorInfo(int id, String cname) {
        this(id);
        className = StringUtils.userFormClassName(cname);
    }

    void setClassName(String cname) {
        className = StringUtils.userFormClassName(cname);
    }
    
   @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MonitorInfo) {
            MonitorInfo mi = (MonitorInfo) obj;
            return mi.monitorId == monitorId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return monitorId;
    }

    void openThread(ThreadInfo ti, ThreadInfo owner, long timeStamp0) {
        assert openThreads.get(ti) == null;
        openThreads.put(ti, new OpenThread(ti, owner, timeStamp0));
    }

    void closeThread(ThreadInfo ti, long timeStamp0) {
        OpenThread openThread = openThreads.remove(ti);
        assert openThread != null;
        long wait = timeStamp0 - openThread.timeStamp;
        addThread(waitThreads, ti, openThread.owner, wait);
        addThread(ownerThreads, openThread.owner, ti, wait);
    }

    private static void addThread(Map<ThreadInfo,ThreadDetail> threads, ThreadInfo master, ThreadInfo detail, long wait) {
        ThreadDetail td = threads.get(master);
        if (td == null) {
            td = new ThreadDetail(master);
            threads.put(master, td);
        }
        td.addWait(detail, wait);
    }

    void timeAdjust(ThreadInfo ti, long timeDiff) {
        OpenThread openThread = openThreads.get(ti);
        assert openThread != null;
        openThread.timeAdjust(timeDiff);
    }

    List<ThreadDetail> cloneWaitThreadDetails() {
        return cloneThreadDetails(waitThreads);
    }

    List<ThreadDetail> cloneOwnerThreadDetails() {
        return cloneThreadDetails(ownerThreads);
    }

    static List<ThreadDetail> cloneThreadDetails(Map<ThreadInfo,ThreadDetail> threads) {
        List details = new ArrayList(threads.size());
        for (ThreadDetail d : threads.values()) {
            details.add(new ThreadDetail(d));
        }
        return details;

    }

    String getName() {
        return new StringBuffer(className).append('(').append(Integer.toHexString(monitorId)).append(')').toString(); // NOI18N
    }

    private static class OpenThread {

        private final ThreadInfo threadInfo;
        private final ThreadInfo owner;
        private long timeStamp;

        OpenThread(ThreadInfo ti, ThreadInfo ownerTi, long ts) {
            threadInfo = ti;
            owner = ownerTi;
            timeStamp = ts;
        }

        private void timeAdjust(long timeDiff) {
            timeStamp += timeDiff;
        }
    }

    static class ThreadDetail {

        final ThreadInfo threadInfo;
        private Map<ThreadInfo, ThreadDetail> threads;
        long count;
        long waitTime;

        ThreadDetail(ThreadInfo ti) {
            threadInfo = ti;
            threads = new HashMap<>();
        }

        ThreadDetail(ThreadDetail d) {
            threadInfo = d.threadInfo;
            count = d.count;
            waitTime = d.waitTime;
            threads = new HashMap<>();
            for (ThreadDetail td : d.threads.values()) {
                threads.put(td.threadInfo, new ThreadDetail(td));
            }
        }

        List<ThreadDetail> cloneThreadDetails() {
            return MonitorInfo.cloneThreadDetails(threads);
        }

        void addWait(ThreadInfo ti, long wait) {
            waitTime += wait;
            count++;
            if (ti != null) {
                addThread(ti, wait);
            }
        }
        
        private void addThread(ThreadInfo ti, long wait) {
            ThreadDetail td = threads.get(ti);
            
            if (td == null) {
                td = new ThreadDetail(ti);
                threads.put(ti, td);
            }
            td.addWait(null, wait);
        }
    }
}
