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

import org.netbeans.lib.profiler.results.AbstractDataFrameProcessor;
import org.netbeans.lib.profiler.results.ProfilingResultListener;

/**
 *
 * @author Tomas Hurka
 */
public abstract class AbstractLockDataFrameProcessor extends AbstractDataFrameProcessor {
    
    protected volatile int currentThreadId = -1;

    protected void fireMonitorEntry(final int threadId, final long timeStamp0, final long timeStamp1, final int monitorId, final int ownerThreadId) {
        foreachListener(new ListenerFunctor() {
            public void execute(ProfilingResultListener listener) {
                ((LockProfilingResultListener) listener).monitorEntry(threadId, timeStamp0, timeStamp1, monitorId, ownerThreadId);
            }
        });
    }

    protected void fireMonitorExit(final int threadId, final long timeStamp0, final long timeStamp1, final int monitorId) {
        foreachListener(new ListenerFunctor() {
            public void execute(ProfilingResultListener listener) {
                ((LockProfilingResultListener) listener).monitorExit(threadId, timeStamp0, timeStamp1, monitorId);
            }
        });
    }

    protected void fireNewMonitor(final int hash, final String className) {
        foreachListener(new ListenerFunctor() {
            public void execute(ProfilingResultListener listener) {
                ((LockProfilingResultListener) listener).newMonitor(hash, className);
            }
        });
    }

    protected void fireNewThread(final int threadId, final String threadName, final String threadClassName) {
        foreachListener(new ListenerFunctor() {
            public void execute(ProfilingResultListener listener) {
                ((LockProfilingResultListener) listener).newThread(threadId, threadName, threadClassName);
            }
        });
    }

    protected void fireAdjustTime(final int threadId, final long timeStamp0, final long timeStamp1) {
        foreachListener(new ListenerFunctor() {
                public void execute(ProfilingResultListener listener) {
                    ((LockProfilingResultListener) listener).timeAdjust(threadId, timeStamp0, timeStamp1);
                }
            });
    }
}
