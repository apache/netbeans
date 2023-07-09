/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.results.cpu;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.client.ClientUtils.TargetAppOrVMTerminated;
import org.netbeans.lib.profiler.filters.InstrumentationFilter;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.results.ProfilingResultListener;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder.SampledThreadInfo;
import org.netbeans.lib.profiler.results.locks.AbstractLockDataFrameProcessor;
import org.netbeans.lib.profiler.results.memory.JMethodIdTable;
import org.netbeans.lib.profiler.results.memory.JMethodIdTable.JMethodIdTableEntry;
import org.netbeans.lib.profiler.utils.formatting.DefaultMethodNameFormatter;
import org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter;
import org.netbeans.lib.profiler.utils.formatting.MethodNameFormatterFactory;

/**
 * This class main purpose is to parse a chunk of CPU sampled data received from
 * the JFluid server agent and dispatch the resulting events to all interested parties
 * @author Tomas Hurka
 */
public class CPUSamplingDataFrameProcessor extends AbstractLockDataFrameProcessor {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------    
    private String currentThreadName, currentThreadClassName;
    private long currentTimestamp;
    private Map<Integer,ThreadInfo> currentThreadsDump;
    private Map<Integer,ThreadInfo> lastThreadsDump;
    private List<ThreadDump> threadDumps = new ArrayList<>();
    private MethodNameFormatter formatter = MethodNameFormatterFactory.getDefault(new DefaultMethodNameFormatter(DefaultMethodNameFormatter.VERBOSITY_FULLMETHOD)).getFormatter();
    private StackTraceSnapshotBuilder builder;
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    public void doProcessDataFrame(ByteBuffer buffer) {
        JMethodIdTable methodIdsTable = JMethodIdTable.getDefault();
        
        threadDumps = new ArrayList<>();
        while (buffer.hasRemaining()) {
            byte eventType = buffer.get();
            
            switch (eventType) {    
                case CommonConstants.THREAD_DUMP_START:
                    currentThreadsDump = new HashMap<>();
                    currentTimestamp = getTimeStamp(buffer);
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Thread dump start: Timestamps:"+currentTimestamp); // NOI18N
                    }
                    break;
                case CommonConstants.NEW_THREAD: {
                    int threadId = buffer.getChar();
                    String threadName = getString(buffer);
                    String threadClassName = getString(buffer);
                    
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Creating new thread: tId="+threadId+" name="+threadName); // NOI18N
                    }
                    
                    currentThreadId = threadId;
                    currentThreadName = threadName;
                    currentThreadClassName = threadClassName;
                    fireNewThread(threadId, threadName, threadClassName);
                    break;
                }
                case CommonConstants.SET_FOLLOWING_EVENTS_THREAD: {
                    currentThreadId = buffer.getChar();
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Change current thread , tId={0}", currentThreadId); // NOI18N
                    }

                    break;
                }
                case CommonConstants.THREAD_INFO_IDENTICAL: {
                    int threadId = buffer.getChar();
                    Integer threadIdObj = Integer.valueOf(threadId);
                    ThreadInfo lastInfo = lastThreadsDump.get(threadIdObj);
                    assert lastInfo != null;
                    currentThreadsDump.put(threadIdObj,lastInfo);
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Thread info identical: tId:"+threadId); // NOI18N
                    }
                    break;
                }
                case CommonConstants.THREAD_INFO: {
                    int threadId = buffer.getChar();
                    byte state = buffer.get();
                    int stackLen = buffer.getChar();
                    int methodIds[] = new int[stackLen];
                    ThreadInfo info;
                    
                    for (int i=0; i<stackLen; i++) {
                        methodIds[i] = buffer.getInt();
                        methodIdsTable.checkMethodId(methodIds[i]);
                    }
                    if (currentThreadId == threadId) {
                        info = new ThreadInfo(currentThreadName,threadId,state,methodIds);
                    } else {
                        info = new ThreadInfo(null,threadId,state,methodIds);                        
                    }
                    currentThreadsDump.put(Integer.valueOf(threadId),info);
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Thread info: tId:"+threadId+" state:"+state+" mIds:"+Arrays.toString(methodIds)); // NOI18N
                    }
                    break;  
                }
                case CommonConstants.THREAD_DUMP_END: {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Thread dump end"); // NOI18N
                    }
                    lastThreadsDump = currentThreadsDump;
                    threadDumps.add(new ThreadDump(currentTimestamp,currentThreadsDump));
                    break;
                }
                case CommonConstants.RESET_COLLECTORS: {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Profiling data reset"); // NOI18N
                    }
                    fireReset();
                    builder.reset();
                    break;
                }
                case CommonConstants.NEW_MONITOR: {
                    int hash = buffer.getInt();
                    String className = getString(buffer);

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Creating new monitor , monitorId={0} , className={1}", new Object[] {Integer.toHexString(hash), className}); // NOI18N
                    }

                    fireNewMonitor(hash, className);
                    break;
                }
                case CommonConstants.METHOD_ENTRY_MONITOR:
                case CommonConstants.METHOD_EXIT_MONITOR: {
                    long timeStamp0 = getTimeStamp(buffer);
                    long timeStamp1 = -1;
                    int hash = buffer.getInt();
                    
                    if (eventType == CommonConstants.METHOD_ENTRY_MONITOR) {
                        int ownerThreadId = buffer.getInt();
                        if (LOGGER.isLoggable(Level.FINEST)) {
                            LOGGER.log(Level.FINEST, "Monitor entry , tId={0} , monitorId={1} , ownerId={2}", new Object[]{currentThreadId,Integer.toHexString(hash),ownerThreadId}); // NOI18N
                        }

                        fireMonitorEntry(currentThreadId, timeStamp0, timeStamp1, hash, ownerThreadId);
                    }
                    if (eventType == CommonConstants.METHOD_EXIT_MONITOR) {
                        if (LOGGER.isLoggable(Level.FINEST)) {
                            LOGGER.log(Level.FINEST, "Monitor exit , tId={0} , monitorId={1}", new Object[]{currentThreadId,Integer.toHexString(hash)}); // NOI18N
                        }

                        fireMonitorExit(currentThreadId, timeStamp0, timeStamp1, hash);
                    }
                    break;
                }
                case CommonConstants.ADJUST_TIME: {
                    long timeStamp0 = getTimeStamp(buffer);
                    long timeStamp1 = collectingTwoTimeStamps ? getTimeStamp(buffer) : 0;
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Adjust time , tId={0}", currentThreadId); // NOI18N
                    }

                    fireAdjustTime(currentThreadId, timeStamp0, timeStamp1);

                    break;
                }
                default: {
                    LOGGER.log(Level.SEVERE, "*** Profiler Engine: internal error: got unknown event type in CPUSamplingDataFrameProcessor: {0} at {1}", // NOI18N
                            new Object[]{(int) eventType, buffer.position()});
                    
                    break;
                }
            }
        }
        try {
            methodIdsTable.getNamesForMethodIds(client);
        } catch (TargetAppOrVMTerminated ex) {
            ProfilerLogger.log(ex.getMessage());
            return;
        }
        processCollectedDumps(methodIdsTable,threadDumps);
        threadDumps.clear();
    }

    public void startup(ProfilerClient client) {
        final CPUCallGraphBuilder[] ccgb = new CPUCallGraphBuilder[1];
        
        super.startup(client);
        
        foreachListener(new ListenerFunctor() {
            public void execute(ProfilingResultListener listener) {
                if (listener instanceof CPUCallGraphBuilder) {
                    ccgb[0] = (CPUCallGraphBuilder) listener;
                }
            }
        });
        builder = new StackTraceSnapshotBuilder(ccgb[0],client.getSettings().getInstrumentationFilter(),client.getStatus());
    }
    
    public void shutdown() {
        super.shutdown();
        builder = null;
    }
  
    private static Thread.State getThreadState(int threadState) {
        switch (threadState) {
            case CommonConstants.THREAD_STATUS_UNKNOWN:
                return Thread.State.TERMINATED;
            case CommonConstants.THREAD_STATUS_ZOMBIE:
                return Thread.State.TERMINATED;
            case CommonConstants.THREAD_STATUS_RUNNING:
                return Thread.State.RUNNABLE;
            case CommonConstants.THREAD_STATUS_SLEEPING:
                return Thread.State.TIMED_WAITING;
            case CommonConstants.THREAD_STATUS_MONITOR:
                return Thread.State.BLOCKED;
            case CommonConstants.THREAD_STATUS_WAIT:
            case CommonConstants.THREAD_STATUS_PARK:
                return Thread.State.WAITING;
            default:
                return Thread.State.TERMINATED;
        }
    }

    private void processCollectedDumps(JMethodIdTable methodIdTable, List<ThreadDump> threadDumps) {
        Map<Integer,StackTraceElement> stackTraceElements = new HashMap<>();
        InstrumentationFilter filter = builder.getFilter();
        
        for (ThreadDump td : threadDumps) {
            SampledThreadInfo[] sampledThreadInfos = new SampledThreadInfo[td.threadDumps.length];
            int tindex = 0;
            
            for (ThreadInfo ti : td.threadDumps) {
                int[] methodIds = ti.methodsIds;
                StackTraceElement[] stackTrace = new StackTraceElement[methodIds.length];

                for (int i=0; i<methodIds.length; i++) {
                    int methodId = methodIds[i];
                    StackTraceElement el = stackTraceElements.get(Integer.valueOf(methodId));
                    
                    if (el == null) {
                        JMethodIdTableEntry entry = methodIdTable.getEntry(methodId);
                        String method = formatter.formatMethodName(entry.className, entry.methodName, entry.methodSig).toFormatted();
                        String className = entry.className.replace('/','.'); // NOI18N
                        String methodName = method+StackTraceSnapshotBuilder.NAME_SIG_SPLITTER+entry.methodSig;
                        el = new StackTraceElement(className, methodName, null, entry.isNative?-2:-1);
                        stackTraceElements.put(Integer.valueOf(methodId),el);
                    }
                    stackTrace[i] = el;
                }
                sampledThreadInfos[tindex++] = new SampledThreadInfo(ti.threadName,ti.threadId,ti.state,stackTrace,filter);
            }
            builder.addStacktrace(sampledThreadInfos,td.timestamp);
        }
    }
    
    private static final class ThreadInfo {
        private int[] methodsIds;
        private Thread.State state;
        private String threadName;
        private long threadId;
 
        ThreadInfo(String tn, long tid, byte ts, int[] st) {
            threadName = tn;
            threadId = tid;
            state = getThreadState(ts);
            methodsIds = st;
        }
    }
     
    private static final class ThreadDump {
        private long timestamp;
        private ThreadInfo[] threadDumps;
        
        ThreadDump(long ts, Map<Integer,ThreadInfo> threadsMap) {
            timestamp = ts;
            threadDumps = threadsMap.values().toArray(new ThreadInfo[0]);
        }
    }    
}
