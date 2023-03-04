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

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * @author Tomas Hurka
 */
class MonitorCCTNode extends LockCCTNode {

    // I18N String constants
    private static final String WAIT_THREADS_LBL;
    private static final String WAIT_THREADS_OWNER_LBL;
    private static final String OWNER_THREADS_LBL;
    private static final String OWNER_THREADS_WAIT_LBL;

    static {
        ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.results.locks.Bundle"); // NOI18N
        WAIT_THREADS_LBL = messages.getString("MonitorCCTNode_WaitThreads"); // NOI18N
        WAIT_THREADS_OWNER_LBL = messages.getString("MonitorCCTNode_WaitThreadsOwner"); // NOI18N
        OWNER_THREADS_LBL = messages.getString("MonitorCCTNode_OwnerThreads"); // NOI18N
        OWNER_THREADS_WAIT_LBL = messages.getString("MonitorCCTNode_OwnerThreadsWait"); // NOI18N
    }

    private final MonitorInfo monitor;
    private final List<MonitorInfo.ThreadDetail> waitThreads;
    private final List<MonitorInfo.ThreadDetail> ownerThreads;
    private ThreadsCCTNode waitNode;
    private long allTime;
    private long allCount;

    MonitorCCTNode(LockCCTNode top, MonitorInfo key, List<List<MonitorInfo.ThreadDetail>> value) {
        super(top);
        assert value.size() == 2;
        monitor = key;
        waitThreads = value.get(0);
        ownerThreads = value.get(1);
    }

    @Override
    public String getNodeName() {
        return monitor.getName();
    }

    @Override
    public long getTime() {
        if (allTime == 0) {
            summarize();
        }
        return allTime;
    }

    @Override
    public long getWaits() {
        if (allCount == 0) {
            summarize();
        }
        return allCount;
    }
    
    @Override
    public boolean isMonitorNode() {
        return true;
    }

    @Override
    public int hashCode() {
        return monitor.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MonitorCCTNode) {
            return monitor.equals(((MonitorCCTNode)obj).monitor);
        }
        return false;
    }

    private void summarize() {
        getChildren();
        if (waitNode != null) {
            allTime = waitNode.getTime();
            allCount = waitNode.getWaits();
        }
    }

    @Override
    void computeChildren() {
        super.computeChildren();
        if (!waitThreads.isEmpty()) {
            waitNode = new ThreadsCCTNode(this, WAIT_THREADS_LBL, WAIT_THREADS_OWNER_LBL, waitThreads);
            addChild(waitNode);
        }
        if (!ownerThreads.isEmpty()) {
            addChild(new ThreadsCCTNode(this, OWNER_THREADS_LBL, OWNER_THREADS_WAIT_LBL, ownerThreads));
        }
    }
    
    static class ThreadsCCTNode extends LockCCTNode {

        private final List<MonitorInfo.ThreadDetail> threads;
        private final String name;
        private final String threadNameFormat;
        private long allTime;
        private long allCount;
        
        ThreadsCCTNode(MonitorCCTNode p, String n, String tnf, List<MonitorInfo.ThreadDetail> ths) {
            super(p);
            name = n;
            threadNameFormat = tnf;
            threads = ths;
        }
        
        @Override
        public String getNodeName() {
            return name;
        }

        @Override
        public long getTime() {
            if (allTime == 0) {
                summarize();
            }
            return allTime;
       }

        @Override
        public long getWaits() {
            if (allCount == 0) {
                summarize();
            }
            return allCount;
       }

        @Override
        void computeChildren() {
            super.computeChildren();
            for (MonitorInfo.ThreadDetail td : threads) {
                addChild(new ThreadDetailLockCCTNode(this, threadNameFormat, false, td));
            }
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ThreadsCCTNode) {
                ThreadsCCTNode tn = (ThreadsCCTNode) obj;
                return name.equals(tn.name) && getParent().equals(tn.getParent());
            }
            return false;
        }

        private void summarize() {
            for (MonitorInfo.ThreadDetail td : threads) {
                allTime += td.waitTime;
                allCount += td.count;
            }
        }
    }
    
    static class ThreadDetailLockCCTNode extends LockCCTNode {

        private final MonitorInfo.ThreadDetail threadDetail;
        private final String threadNameFormat;
        private final boolean useFormat;
        
        ThreadDetailLockCCTNode(LockCCTNode p, String nf, boolean uf, MonitorInfo.ThreadDetail td) {
            super(p);
            threadDetail = td;
            threadNameFormat = nf;
            useFormat = uf;
        }

        ThreadDetailLockCCTNode(LockCCTNode p, String nf, MonitorInfo.ThreadDetail td) {
            this(p, nf, true, td);
        }

        @Override
        public boolean isThreadLockNode() {
            return true;
        }

        @Override
        public String getNodeName() {
            String name = threadDetail.threadInfo.getName();

            if (threadNameFormat != null && useFormat) {
                name = MessageFormat.format(threadNameFormat, name);
            }
            return name;
        }

        @Override
        public long getTime() {
            return threadDetail.waitTime;
        }

        @Override
        public long getWaits() {
            return threadDetail.count;
        }

        @Override
        public int hashCode() {
            return threadDetail.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ThreadDetailLockCCTNode) {
                ThreadDetailLockCCTNode tn = (ThreadDetailLockCCTNode) obj;
                return threadDetail.threadInfo.equals(tn.threadDetail.threadInfo) && getParent().equals(tn.getParent());
            }
            return false;
        }

        @Override
        void computeChildren() {
            super.computeChildren();
            for (MonitorInfo.ThreadDetail td : threadDetail.cloneThreadDetails()) {
                addChild(new ThreadDetailLockCCTNode(this, threadNameFormat, td));
            }
        }

    }
}
