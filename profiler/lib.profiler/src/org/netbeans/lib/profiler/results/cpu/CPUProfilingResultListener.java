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

import java.util.List;
import org.netbeans.lib.profiler.results.locks.LockProfilingResultListener;


/**
 *
 * @author Jaroslav Bachorik
 */
public interface CPUProfilingResultListener extends LockProfilingResultListener {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static final int METHODTYPE_NORMAL = 1;
    static final int METHODTYPE_ROOT = 2;
    static final int METHODTYPE_MARKER = 3;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    void methodEntry(final int methodId, final int threadId, final int methodType, final long timeStamp0, final long timeStamp1,
            final List parameters, final int[] methodIds);

    void methodEntryUnstamped(final int methodId, final int threadId, final int methodType, final List parameters, final int[] methodIds);

    void methodExit(final int methodId, final int threadId, final int methodType, final long timeStamp0, final long timeStamp1, final Object retVal);

    void methodExitUnstamped(final int methodId, final int threadId, final int methodType);

    void servletRequest(final int threadId, final int requestType, final String servletPath, final int sessionId);

    void sleepEntry(final int threadId, final long timeStamp0, final long timeStamp1);

    void sleepExit(final int threadId, final long timeStamp0, final long timeStamp1);

    void threadsResume(final long timeStamp0, final long timeStamp1);

    void threadsSuspend(final long timeStamp0, final long timeStamp1);

    void waitEntry(final int threadId, final long timeStamp0, final long timeStamp1);

    void waitExit(final int threadId, final long timeStamp0, final long timeStamp1);

    void parkEntry(final int threadId, final long timeStamp0, final long timeStamp1);

    void parkExit(final int threadId, final long timeStamp0, final long timeStamp1);
}
