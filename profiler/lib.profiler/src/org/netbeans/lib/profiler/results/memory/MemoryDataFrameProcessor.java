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

package org.netbeans.lib.profiler.results.memory;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.results.ProfilingResultListener;
import org.netbeans.lib.profiler.results.locks.AbstractLockDataFrameProcessor;

/**
 *
 * @author Jaroslav Bachorik
 * @author Tomas Hurka
 */
public class MemoryDataFrameProcessor extends AbstractLockDataFrameProcessor {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void doProcessDataFrame(ByteBuffer buffer) {
        int currentEpoch = -1;

        while (buffer.hasRemaining()) {
            byte eventType = buffer.get();

            switch (eventType) {
                case CommonConstants.OBJ_ALLOC_STACK_TRACE: {
                    char classId = buffer.getChar();
                    long objSize = getObjSize(buffer);
                    int depth = getDepth(buffer);

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Allocation stack trace: classId=" + (int) classId + ", objSize=" + objSize + ", depth=" + depth); // NOI18N
                    }

                    int[] methodIds = new int[depth];

                    for (int i = 0; i < depth; i++) {
                        methodIds[i] = buffer.getInt();
                    }

                    fireAllocStackTrace(classId, objSize, methodIds);

                    break;
                }
                case CommonConstants.OBJ_LIVENESS_STACK_TRACE: {
                    char classId = buffer.getChar();
                    int objEpoch = buffer.getChar();

                    if (objEpoch > currentEpoch) {
                        currentEpoch = objEpoch; // objEpoch may be < currentEpoch if e.g. the GC event is being processed
                    }

                    long objectId = ((((long) classId) & 0xFFFF) << 48) | ((((long) objEpoch) & 0xFFFF) << 32)
                                    | (((long) buffer.getInt()) & 0xFFFFFFFF);
                    long objSize = getObjSize(buffer);

                    int depth = getDepth(buffer);

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Liveness stack trace: classId=" + (int) classId + ", objectId=" + objectId + ", objEpoch=" //NOI18N
                                      + objEpoch + ", objSize=" + objSize + ", depth=" + depth); // NOI18N
                    }

                    int[] methodIds = new int[depth];

                    for (int i = 0; i < depth; i++) {
                        methodIds[i] = buffer.getInt();
                    }

                    fireLivenessStackTrace(classId, objectId, objEpoch, objSize, methodIds);

                    break;
                }
                case CommonConstants.OBJ_GC_HAPPENED: {
                    char classId = buffer.getChar();
                    int objEpoch = buffer.getChar();

                    if (objEpoch > currentEpoch) {
                        currentEpoch = objEpoch; // objEpoch may be < currentEpoch if e.g. the GC event is being processed
                    }

                    long objectId = ((((long) classId) & 0xFFFF) << 48) | ((((long) objEpoch) & 0xFFFF) << 32)
                                    | (((long) buffer.getInt()) & 0xFFFFFFFF);

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("GC Performed: classId=" + (int) classId + ", objectId=" + objectId + ", objEpoch=" + objEpoch); // NOI18N
                    }

                    fireGCPerformed(classId, objectId, objEpoch);

                    break;
                }
                case CommonConstants.RESET_COLLECTORS: {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Profiling data reset"); // NOI18N
                    }

                    fireReset();

                    break;
                }
                case CommonConstants.BUFFEREVENT_PROFILEPOINT_HIT: {
                    int id = buffer.getChar();
                    long timeStamp = getTimeStamp(buffer);
                    int threadId = buffer.getChar();
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Profile Point Hit " + id + ", threadId=" + id + ", timeStamp=" + timeStamp); // NOI18N
                    }

                    fireProfilingPoint(threadId, id, timeStamp);

                    break;
                }
                case CommonConstants.SET_FOLLOWING_EVENTS_THREAD: {
                    currentThreadId = buffer.getChar();
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Change current thread , tId={0}", currentThreadId); // NOI18N
                    }

                    break;
                }
                case CommonConstants.NEW_THREAD: {
                    int threadId = buffer.getChar();
                    String threadName = getString(buffer);
                    String threadClassName = getString(buffer);

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Creating new thread , tId={0}", threadId); // NOI18N
                    }

                    fireNewThread(threadId, threadName, threadClassName);
                    currentThreadId = threadId;

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
                    LOGGER.severe("*** Profiler Engine: internal error: got unknown event type in MemoryDataFrameProcessor: " // NOI18N
                                  + (int) eventType
                                  + " at " + buffer.position() // NOI18N
                                  );

                    break;
                }
            }
        }
    }

    private void fireAllocStackTrace(final char classId, final long objSize, final int[] methodIds) {
        foreachListener(new ListenerFunctor() {
                public void execute(ProfilingResultListener listener) {
                    try {
                        ((MemoryProfilingResultsListener) listener).onAllocStackTrace(classId, objSize, methodIds);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    private void fireGCPerformed(final char classId, final long objectId, final int objEpoch) {
        foreachListener(new ListenerFunctor() {
                public void execute(ProfilingResultListener listener) {
                    ((MemoryProfilingResultsListener) listener).onGcPerformed(classId, objectId, objEpoch);
                }
            });
    }

    private void fireLivenessStackTrace(final char classId, final long objectId, final int objEpoch, final long objSize,
                                        final int[] methodIds) {
        foreachListener(new ListenerFunctor() {
                public void execute(ProfilingResultListener listener) {
                    ((MemoryProfilingResultsListener) listener).onLivenessStackTrace(classId, objectId, objEpoch, objSize,
                                                                                     methodIds);
                }
            });
    }
    
    private static long getObjSize(ByteBuffer buffer) {
        long objSize = (((long) buffer.get() & 0xFF) << 32) | (((long) buffer.get() & 0xFF) << 24)
                       | (((long) buffer.get() & 0xFF) << 16) | (((long) buffer.get() & 0xFF) << 8)
                       | ((long) buffer.get() & 0xFF);
        return objSize;
    }
    
    private static int getDepth(ByteBuffer buffer) {
        int depth = ((((int) buffer.get()) & 0xFF) << 16) | ((((int) buffer.get()) & 0xFF) << 8)
                    | (((int) buffer.get()) & 0xFF);
        return depth;
    }
}
