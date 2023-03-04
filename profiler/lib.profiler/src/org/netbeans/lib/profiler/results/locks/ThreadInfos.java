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

/**
 *
 * @author Tomas Hurka
 */
final class ThreadInfos {

    ThreadInfo[] threadInfos;
    int threadInfosLastIdx;
    ThreadInfo unknownThread = new ThreadInfo(-1, "Unknown", "N/A");     // NOI18N

    ThreadInfos() {
        reset();
    }

    boolean isEmpty() {
        if ((threadInfos == null) || (threadInfos.length == 0)) {
            return true;
        }
        for (int i = 0; i < threadInfos.length; i++) {
            if (threadInfos[i] != null) {
                return false;
            }
        }
        return true;
    }

    void newThreadInfo(int threadId, String threadName, String threadClassName) {
        if ((threadId > threadInfosLastIdx) || (threadInfos == null)) {
            int newLen = threadId + 1;
            ThreadInfo[] newInfos = new ThreadInfo[newLen];
            if (threadInfos != null) {
                System.arraycopy(threadInfos, 0, newInfos, 0, threadInfos.length);
            }
            threadInfos = newInfos;
            threadInfosLastIdx = threadId;
        }
        threadInfos[threadId] = new ThreadInfo(threadId, threadName, threadClassName);
    }

    void reset() {
        threadInfos = null;
        threadInfosLastIdx = -1;
    }

    ThreadInfo getThreadInfo(int threadId) {
        if (threadId == -1) return unknownThread;
        if (!isEmpty()) {
            return threadInfos[threadId];
        }
        return null;
    }
}
