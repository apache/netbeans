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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.server.system.Stacks;
import org.netbeans.lib.profiler.server.system.Timers;

/**
 * @author Tomas Hurka
 */
class ProfilerRuntimeSampler extends ProfilerRuntime {

    private static Sampling sampling;
    private static int samplingFrequency = 10;

    static void setSamplngFrequency(int v) {
        samplingFrequency = v;
    }
    
    static class Sampling extends SamplingThread {
        private int[] states = new int[0];
        private int[][] methodIds = new int[0][];        
        private Map arrayOffsetMap = new HashMap();
        private Map threadIdMap = new HashMap();
        private volatile boolean resetData = false;
        private boolean sendDataAvailable = true;
        
        Sampling(int samplingInterval) {
            super(samplingInterval);
            setName(PROFILER_SPECIAL_EXEC_THREAD_NAME + " 10"); // NOI18N
        }
        
        void sample() {
            Thread[][] newThreads = new Thread[1][];
            int[][] newStates = new int[1][];
            int[][][] newMethodIds = new int[1][][];        
            Map newArrayOffsetMap = new HashMap();
            Map newThreadIdMap = new HashMap();
            long timestamp;
            
            if (resetData) {
                resetProfilerCollectors();
                resetData = false;
                sendDataAvailable = true;
            }
            Stacks.getAllStackTraces(newThreads, newStates, newMethodIds);
            timestamp = Timers.getCurrentTimeInCounts();
            
            if (newThreads[0] != null && eventBuffer != null) { // ignore samples without data 
                synchronized (eventBuffer) {
                    if (resetData) return;  // skip this sample if the collectors was not reset yet
                    writeThreadDumpStart(timestamp);
                    for (int i = 0; i < newThreads[0].length; i++) {
                        Thread t = newThreads[0][i];
                        int[] mids = newMethodIds[0][i];

                        if (!ThreadInfo.isProfilerServerThread(t)) {
                            int status = newStates[0][i];
                            Long ltid = Long.valueOf(t.getId());
                            Integer index = (Integer) arrayOffsetMap.get(ltid);
                            Integer tid = (Integer) threadIdMap.get(ltid);

                            if (index != null) {
                                if (status == states[index.intValue()] && Arrays.equals(mids,methodIds[index.intValue()])) {
                                    writeThreadInfoNoChange(tid);
                                } else {
                                    writeThreadInfo(tid,status,mids);
                                }
                            } else if (status != CommonConstants.THREAD_STATUS_ZOMBIE && mids.length>0) { 
                                // new thread with a stacktrace
                                ThreadInfo ti = ThreadInfo.getThreadInfo(t);
                                tid = ti.getThreadId();
                                if (!ti.isInitialized()) {
                                    ti.initialize();
                                    ProfilerRuntime.writeThreadCreationEvent(t,tid.intValue());
                                }
                                writeThreadInfo(tid,status,mids);
                            } else { // new thread which is not started yet or it did not ever have stacktrace 
                                continue; 
                            }
                            newArrayOffsetMap.put(ltid, i);
                            newThreadIdMap.put(ltid,tid);
                        }
                    }
                    writeThreadDumpEnd();
                }
                arrayOffsetMap = newArrayOffsetMap;
                threadIdMap = newThreadIdMap;
                states = newStates[0];
                methodIds = newMethodIds[0];
            }
        }

        private void resetProfilerCollectors() {
            arrayOffsetMap = new HashMap();
            threadIdMap = new HashMap();
            states = new int[0];
            methodIds = new int[0][];        
        }
         
        private void writeThreadDumpStart(long absTimeStamp) {
            if (eventBuffer == null) {
                return; 
            }

            if (sendDataAvailable) {
                ProfilerServer.notifyClientOnResultsAvailability();
                sendDataAvailable = false;
            }

            int curPos = globalEvBufPos;

            if (curPos + 8 > globalEvBufPosThreshold) { // Dump the buffer
                externalActionsHandler.handleEventBufferDump(eventBuffer, 0, curPos);
                curPos = 0;
            }

            eventBuffer[curPos++] = THREAD_DUMP_START;
            eventBuffer[curPos++] = (byte) ((absTimeStamp >> 48) & 0xFF);
            eventBuffer[curPos++] = (byte) ((absTimeStamp >> 40) & 0xFF);
            eventBuffer[curPos++] = (byte) ((absTimeStamp >> 32) & 0xFF);
            eventBuffer[curPos++] = (byte) ((absTimeStamp >> 24) & 0xFF);
            eventBuffer[curPos++] = (byte) ((absTimeStamp >> 16) & 0xFF);
            eventBuffer[curPos++] = (byte) ((absTimeStamp >> 8) & 0xFF);
            eventBuffer[curPos++] = (byte) ((absTimeStamp) & 0xFF);

            globalEvBufPos = curPos;
        }
              
        private void writeThreadDumpEnd() {
            if (eventBuffer == null) {
                return; 
            }

            int curPos = globalEvBufPos;

            if (curPos + 1 > globalEvBufPosThreshold) { // Dump the buffer
                externalActionsHandler.handleEventBufferDump(eventBuffer, 0, curPos);
                curPos = 0;
            }

            eventBuffer[curPos++] = THREAD_DUMP_END;
            globalEvBufPos = curPos;
        }

        private void writeThreadInfoNoChange(Integer tid) {
            if (eventBuffer == null) {
                return; 
            }

            int curPos = globalEvBufPos;

            if (curPos + 3 > globalEvBufPosThreshold) { // Dump the buffer
                externalActionsHandler.handleEventBufferDump(eventBuffer, 0, curPos);
                curPos = 0;
            }

            int threadId = tid.intValue();
            
            eventBuffer[curPos++] = THREAD_INFO_IDENTICAL;
            eventBuffer[curPos++] = (byte) ((threadId >> 8) & 0xFF);
            eventBuffer[curPos++] = (byte) ((threadId) & 0xFF);
            globalEvBufPos = curPos;
        }

        private void writeThreadInfo(Integer tid, int status, int[] mids) {
            if (eventBuffer == null) {
                return; 
            }

            int curPos = globalEvBufPos;

            if (curPos + 6 + mids.length*4 > globalEvBufPosThreshold) { // Dump the buffer
                externalActionsHandler.handleEventBufferDump(eventBuffer, 0, curPos);
                curPos = 0;
            }

            int threadId = tid.intValue();
            int stackLen = mids.length;
            
            eventBuffer[curPos++] = THREAD_INFO;
            eventBuffer[curPos++] = (byte) ((threadId >> 8) & 0xFF);
            eventBuffer[curPos++] = (byte) ((threadId) & 0xFF);
            eventBuffer[curPos++] = (byte) ((status) & 0xFF);
            eventBuffer[curPos++] = (byte) ((stackLen >> 8) & 0xFF);
            eventBuffer[curPos++] = (byte) ((stackLen) & 0xFF);
            for (int i = 0; i < mids.length; i++) {
                eventBuffer[curPos++] = (byte) ((mids[i] >> 24) & 255);
                eventBuffer[curPos++] = (byte) ((mids[i] >> 16) & 255);
                eventBuffer[curPos++] = (byte) ((mids[i] >> 8) & 255);
                eventBuffer[curPos++] = (byte) ((mids[i]) & 255);                
            }
            globalEvBufPos = curPos;
        }
    }

    static void initialize() {
        sampling = new Sampling(samplingFrequency);
        sampling.start();
    }

    public static void shutdown() {
        sampling.terminate();
        sampling = null;
        ProfilerRuntime.clearDataStructures();
    }
    
    static void resetProfilerCollectors() {
        if (sampling != null) sampling.resetData = true;   
    }
}

