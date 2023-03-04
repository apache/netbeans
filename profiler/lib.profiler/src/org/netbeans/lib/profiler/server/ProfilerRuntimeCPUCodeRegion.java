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

package org.netbeans.lib.profiler.server;

import org.netbeans.lib.profiler.server.system.Timers;


/**
 * Functionality for single code region profiling.
 *
 * @author Misha Dmitriev
 */
public class ProfilerRuntimeCPUCodeRegion extends ProfilerRuntime {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    protected static long[] codeRegionResults = new long[0];
    protected static int newBufSize;
    protected static int bufSize;
    protected static int curIdx;
    protected static long invCount;
    protected static boolean codeRegionInstrumentationDisabled;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static void setCPUResBufSize(int size) {
        newBufSize = size; // We don't set bufSize immediately to prevent the collector from crash if it's already active
    }

    public static long[] getProfilingResults() {
        synchronized (codeRegionResults) {
            int size = (invCount < bufSize) ? (int) invCount : bufSize;
            size++; // The very first element of the returned array is the total number of invocations

            long[] res = new long[size];
            res[0] = (int) invCount;

            if (invCount < bufSize) {
                System.arraycopy(codeRegionResults, 0, res, 1, (int) invCount);
            } else {
                System.arraycopy(codeRegionResults, curIdx, res, 1, bufSize - curIdx);
                System.arraycopy(codeRegionResults, 0, res, bufSize - curIdx + 1, curIdx);
            }

            return res;
        }
    }

    public static void codeRegionEntry() {
        //System.out.println("+++++++++ codeRegionEntry " + Thread.currentThread());
        if (codeRegionInstrumentationDisabled) {
            return;
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (!ti.isInitialized()) {
            ti.initialize();
        }

        ti.inCallGraph = true;
        ti.absEntryTime = Timers.getCurrentTimeInCounts();
    }

    public static void codeRegionExit() {
        if (codeRegionInstrumentationDisabled) {
            return;
        }

        long time = Timers.getCurrentTimeInCounts();

        //System.out.println("--------- codeRegionExit " + Thread.currentThread());
        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (!ti.isInitialized() || !ti.inCallGraph) {
            return;
        }

        time = time - ti.absEntryTime;

        synchronized (codeRegionResults) {
            codeRegionResults[curIdx++] = time;
            invCount++;

            if (curIdx == bufSize) {
                curIdx = 0;
            }
        }

        ti.inCallGraph = false;
    }

    public static void enableProfiling(boolean v) {
        if (v) {
            createNewDataStructures();
            codeRegionInstrumentationDisabled = false;
        } else {
            codeRegionInstrumentationDisabled = true;
            clearDataStructures();
        }
    }

    public static void resetProfilerCollectors() {
        synchronized (codeRegionResults) {
            bufSize = newBufSize;
            codeRegionResults = new long[bufSize];
            invCount = 0;
            curIdx = 0;
        }
    }

    protected static void clearDataStructures() {
        ProfilerRuntime.clearDataStructures();

        synchronized (codeRegionResults) {
            codeRegionResults = new long[0];
            invCount = 0;
            curIdx = 0;
        }
    }

    // ---------------------------------- Handling wait/sleep/monitor times ----------------------------
    protected static long monitorEntryRegion(Thread t, Object monitor, ThreadInfo ownerTi) {
        if (codeRegionInstrumentationDisabled) {
            return -1;
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (!ti.isInitialized() || !ti.inCallGraph) {
            return -1;
        }

        // take note of the time we started waiting
        return ti.lastWaitStartTime = Timers.getCurrentTimeInCounts();
    }

    protected static long monitorExitRegion(Thread t, Object monitor) {
        if (codeRegionInstrumentationDisabled) {
            return -1;
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if ((ti == null) || !ti.inCallGraph) {
            return -1;
        }
        long timeStamp = Timers.getCurrentTimeInCounts();
        // adjust the entry time so that the time spent waiting is not accounted for
        ti.absEntryTime += (timeStamp - ti.lastWaitStartTime);
        return timeStamp;
    }

    protected static void sleepEntryRegion() {
        if (codeRegionInstrumentationDisabled) {
            return;
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (!ti.isInitialized() || !ti.inCallGraph) {
            return;
        }

        // take note of the time we started waiting
        ti.lastWaitStartTime = Timers.getCurrentTimeInCounts();
    }

    protected static void sleepExitRegion() {
        if (codeRegionInstrumentationDisabled) {
            return;
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (!ti.isInitialized() || !ti.inCallGraph) {
            return;
        }

        // adjust the entry time so that the time spent waiting is not accounted for
        ti.absEntryTime += (Timers.getCurrentTimeInCounts() - ti.lastWaitStartTime);
    }

    protected static void waitEntryRegion() {
        if (codeRegionInstrumentationDisabled) {
            return;
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (!ti.isInitialized() || !ti.inCallGraph) {
            return;
        }

        // take note of the time we started waiting
        ti.lastWaitStartTime = Timers.getCurrentTimeInCounts();
    }

    protected static void waitExitRegion() {
        if (codeRegionInstrumentationDisabled) {
            return;
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (!ti.isInitialized() || !ti.inCallGraph) {
            return;
        }

        // adjust the entry time so that the time spent waiting is not accounted for
        ti.absEntryTime += (Timers.getCurrentTimeInCounts() - ti.lastWaitStartTime);
    }

    protected static void parkEntryRegion() {
        if (codeRegionInstrumentationDisabled) {
            return;
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (!ti.isInitialized() || !ti.inCallGraph) {
            return;
        }

        // take note of the time we started waiting
        ti.lastWaitStartTime = Timers.getCurrentTimeInCounts();
    }

    protected static void parkExitRegion() {
        if (codeRegionInstrumentationDisabled) {
            return;
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (!ti.isInitialized() || !ti.inCallGraph) {
            return;
        }

        // adjust the entry time so that the time spent waiting is not accounted for
        ti.absEntryTime += (Timers.getCurrentTimeInCounts() - ti.lastWaitStartTime);
    }
}
