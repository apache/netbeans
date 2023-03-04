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
package org.netbeans.lib.profiler.results.cpu;

import org.netbeans.lib.profiler.global.TransactionalSupport;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ThreadInfos {

    public ThreadInfo[] threadInfos;
    String[] threadNames;
    String[] threadClassNames;
    int threadInfosLastIdx;
    private TransactionalSupport transaction = new TransactionalSupport();

    public ThreadInfos() {
        reset();
    }

    public void beginTrans(boolean mutable) {
        transaction.beginTrans(mutable);
    }

    boolean beginTrans(boolean mutable, boolean failEarly) {
        return transaction.beginTrans(mutable, failEarly);
    }

    public void endTrans() {
        transaction.endTrans();
    }

    public boolean isEmpty() {
        beginTrans(false);
        try {
            if ((threadInfos == null) || (threadInfos.length == 0)) {
                return true;
            }
            for (int i = 0; i < threadInfos.length; i++) {
                if ((threadInfos[i] != null) && (threadInfos[i].stack != null) && (threadInfos[i].stack[0] != null) && (threadInfos[i].stack[0].getChildren() != null) && (threadInfos[i].stack[0].getChildren().length > 0)) {
                    return false;
                }
            }
            return true;
        } finally {
            endTrans();
        }
    }

    public String[] getThreadNames() {
        beginTrans(false);
        try {
            return threadNames;
        } finally {
            endTrans();
        }
    }

    public void newThreadInfo(int threadId, String threadName, String threadClassName) {
        beginTrans(true);
        try {
            if ((threadId > threadInfosLastIdx) || (threadInfos == null)) {
                int newLen = threadId + 1;
                ThreadInfo[] newInfos = new ThreadInfo[newLen];
                String[] newNames = new String[newLen];
                String[] newClassNames = new String[newLen];
                if (threadInfos != null) {
                    System.arraycopy(threadInfos, 0, newInfos, 0, threadInfos.length);
                    System.arraycopy(threadNames, 0, newNames, 0, threadNames.length);
                    System.arraycopy(threadClassNames, 0, newClassNames, 0, threadNames.length);
                }
                threadInfos = newInfos;
                threadNames = newNames;
                threadClassNames = newClassNames;
                threadInfosLastIdx = threadId;
            }
            if (threadInfos[threadId] == null) {
                threadInfos[threadId] = new ThreadInfo(threadId);
                threadNames[threadId] = threadName;
                threadClassNames[threadId] = threadClassName;
            }
        } finally {
            endTrans();
        }
    }

    public void reset() {
        beginTrans(true);
        try {
            threadInfos = null;
            threadNames = null;
            threadClassNames = null;
            threadInfosLastIdx = -1;
        } finally {
            endTrans();
        }
    }
}
