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

package org.netbeans.lib.profiler.client;

import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.wireprotocol.MonitoredNumbersResponse;


/**
 * A representation of the monitored data, returned by the server on demand, that is suitable for use by
 * presentation code.
 *
 * @author Tomas Hurka
 * @author  Misha Dmitriev
 */
public class MonitoredData {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private long[] gcFinishs;
    private long[] gcStarts;

    // The following array contains:
    // - the amounts of free and total memory in elements 0 and 1
    // - the number of user and system threads in elements 2 and 3
    // - number of surviving generations in element 4
    // - relative time spent in GC, in per mil (1/10th of per cent), and the duration of the last GC pause (in ms) in elements 5 and 6
    // - timestamp at the moment when this packet was generated (obtained with System.currentTimeMillis()) in element 7
    private long[] generalMNumbers;
    private String[] newThreadClassNames;
    private int[] newThreadIds;
    private String[] newThreadNames;
    private long[] stateTimestamps;
    private int[] threadIds;
    private byte[][] threadStates = new byte[20][20];

    private int[] exThreadIds;
    private long[] exStateTimestamps;
    private byte[] exThreadStates;
    private int mode = CommonConstants.MODE_THREADS_NONE;

    // Data on new threads. Any thread that has been created between the previous and the current use of this object
    // shows up on the list below, but just once. nNewThreads is the real number of threads, which may be shorter than
    // the size of the following arrays.
    private int nNewThreads;
    private int nThreadStates;

    // Data on thread states. nThreads is the real number of threads (dimension 0 of the following arrays), which
    // may be shorter than the actual size of the following arrays. nStates is the number of thread states
    // (dimension 1 of these arrays), and also may be shorter than the actual size.
    // Thread state timestamps are expressed in milliseconds as obtained by System.currentTimeMillis() on server side.
    // threadStates use constants defined in CommonConstants for thread states.
    private int nThreads;

    private int serverState;
    private int serverProgress;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private MonitoredData(MonitoredNumbersResponse mresp) {
        long[] gn = mresp.getGeneralMonitoredNumbers();
        generalMNumbers = new long[gn.length];
        System.arraycopy(gn, 0, generalMNumbers, 0, gn.length);
        mode = mresp.getThreadsDataMode();
        
        if (mode == CommonConstants.MODE_THREADS_SAMPLING) {
            nThreads = mresp.getNThreads();
            nThreadStates = mresp.getNThreadStates();
            
            int[] ids = mresp.getThreadIds();
            threadIds = new int[nThreads];
            System.arraycopy(ids, 0, threadIds, 0, nThreads);
            
            long[] ts = mresp.getStateTimestamps();
            stateTimestamps = new long[nThreadStates];
            System.arraycopy(ts, 0, stateTimestamps, 0, nThreadStates);
            
            setThreadStates(mresp.getThreadStates());
        } else if (mode == CommonConstants.MODE_THREADS_EXACT) {
            int expLen = mresp.getExactThreadIds().length;
            exThreadIds = new int[expLen];
            System.arraycopy(mresp.getExactThreadIds(), 0, exThreadIds, 0, expLen);
            exThreadStates = new byte[expLen];
            System.arraycopy(mresp.getExactThreadStates(), 0, exThreadStates, 0, expLen);
            exStateTimestamps = new long[expLen];
            System.arraycopy(mresp.getExactStateTimestamps(), 0, exStateTimestamps, 0, expLen);
        }

        nNewThreads = mresp.getNNewThreads();

        if (nNewThreads > 0) {
            int[] newIds = mresp.getNewThreadIds();
            newThreadIds = new int[nNewThreads];
            System.arraycopy(newIds, 0, newThreadIds, 0, nNewThreads);
            newThreadNames = new String[nNewThreads];
            System.arraycopy(mresp.getNewThreadNames(), 0, newThreadNames, 0, nNewThreads);
            newThreadClassNames = new String[nNewThreads];
            System.arraycopy(mresp.getNewThreadClassNames(), 0, newThreadClassNames, 0, nNewThreads);
        }

        gcStarts = mresp.getGCStarts();
        convertToTimeInMillis(gcStarts);
        gcFinishs = mresp.getGCFinishs();
        convertToTimeInMillis(gcFinishs);

        serverState = mresp.getServerState();
        serverProgress = mresp.getServerProgress();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getServerState() {
        return serverState;
    }

    public int getServerProgress() {
        return serverProgress;
    }

    public long getFreeMemory() {
        return generalMNumbers[MonitoredNumbersResponse.FREE_MEMORY_IDX];
    }

    public long[] getGCFinishs() {
        return gcFinishs;
    }

    public long[] getGCStarts() {
        return gcStarts;
    }

    public long getLastGCPauseInMS() {
        return generalMNumbers[MonitoredNumbersResponse.GC_PAUSE_IDX];
    }

    public long getLoadedClassesCount() {
        return generalMNumbers[MonitoredNumbersResponse.LOADED_CLASSES_IDX];
    }

    /**
     * Returns the approximate accumulated process CPU elapsed time
     * in nanoseconds. Note that the time is normalized to one processor.
     * This method returns <tt>-1</tt> if the collection
     * elapsed time is undefined for this collector.
     *
     * @return the approximate accumulated process CPU elapsed time
     * in nanoseconds.
     */
    public long getProcessCpuTime() {
        return generalMNumbers[MonitoredNumbersResponse.CPU_TIME_IDX];
    }

    /**
     * With mresp, the same instance is reused all the time to save memory. However, with MonitoredData we
     * generally can't afford that, so here we create a new object every time and copy data into it.
     */
    public static MonitoredData getMonitoredData(MonitoredNumbersResponse mresp) {
        return new MonitoredData(mresp);
    }

    public int getNNewThreads() {
        return nNewThreads;
    }

    public long getNSurvivingGenerations() {
        return generalMNumbers[MonitoredNumbersResponse.SURVIVING_GENERATIONS_IDX];
    }

    public long getNSystemThreads() {
        return generalMNumbers[MonitoredNumbersResponse.SYSTEM_THREADS_IDX];
    }

    public int getNThreadStates() {
        return nThreadStates;
    }

    public int getNThreads() {
        return nThreads;
    }

    public long getNUserThreads() {
        return generalMNumbers[MonitoredNumbersResponse.USER_THREADS_IDX];
    }

    public String[] getNewThreadClassNames() {
        return newThreadClassNames;
    }

    public int[] getNewThreadIds() {
        return newThreadIds;
    }

    public String[] getNewThreadNames() {
        return newThreadNames;
    }

    public int getThreadsDataMode() {
        return mode; 
    }

    public int[] getExplicitThreadIds() {
        return exThreadIds;
    }
    public long[] getExplicitStateTimestamps() {
        return exStateTimestamps;
    }
    public byte[] getExplicitThreadStates() {
        return exThreadStates;
    }

    public long getRelativeGCTimeInPerMil() {
        return generalMNumbers[MonitoredNumbersResponse.GC_TIME_IDX];
    }

    public long[] getStateTimestamps() {
        return stateTimestamps;
    }

    public int[] getThreadIds() {
        return threadIds;
    }

    public byte[][] getThreadStates() {
        return threadStates;
    }

    public long getTimestamp() {
        return generalMNumbers[MonitoredNumbersResponse.TIMESTAMP_IDX];
    }

    public long getTotalMemory() {
        return generalMNumbers[MonitoredNumbersResponse.TOTAL_MEMORY_IDX];
    }

    private static void convertToTimeInMillis(final long[] hiResTimeStamp) {
        if (hiResTimeStamp.length > 0) {
            ProfilingSessionStatus session = TargetAppRunner.getDefault().getProfilingSessionStatus();
            long statupInCounts = session.startupTimeInCounts;
            long startupMillis = session.startupTimeMillis;

            for (int i = 0; i < hiResTimeStamp.length; i++) {
                hiResTimeStamp[i] = startupMillis + ((hiResTimeStamp[i] - statupInCounts) / (1000000000 / 1000L)); // 1 ms has 1000000000/1000 ns
            }
        }
    }

    private void setThreadStates(byte[] packedStates) {
        threadStates = new byte[nThreads][nThreadStates];

        int idx = 0;

        for (int i = 0; i < nThreads; i++) {
            System.arraycopy(packedStates, idx, threadStates[i], 0, nThreadStates);
            idx += nThreadStates;
        }
    }

    /** Debugging support */
    private void print() {
        for (int i = 0; i < nThreads; i++) {
            System.err.print("id = "); // NOI18N
            System.err.print(threadIds[i]);
            System.err.print(", states = "); // NOI18N

            for (int j = 0; j < nThreadStates; j++) {
                System.err.print(threadStates[i][j]);
            }

            System.err.println();
        }

        if (nNewThreads > 0) {
            System.err.println("New threads added: " + nNewThreads); // NOI18N

            for (int i = 0; i < nNewThreads; i++) {
                System.err.println("  id = " + newThreadIds[i] + ", name = " + newThreadNames[i] + ", classname = " // NOI18N
                                   + newThreadClassNames[i]);
            }
        }

        System.err.println();
    }
}
