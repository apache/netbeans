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

package org.netbeans.lib.profiler.results;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.results.cpu.CPUDataFrameProcessor;
import org.netbeans.lib.profiler.results.cpu.CPUProfilingResultListener;
import org.netbeans.lib.profiler.results.cpu.CPUSamplingDataFrameProcessor;
import org.netbeans.lib.profiler.results.locks.LockDataFrameProcessor;
import org.netbeans.lib.profiler.results.locks.LockProfilingResultListener;
import org.netbeans.lib.profiler.results.memory.MemoryDataFrameProcessor;
import org.netbeans.lib.profiler.results.memory.MemoryProfilingResultsListener;


/**
 *
 * @author Jaroslav Bachorik
 * @author Tomas Hurka
 */
public final class ProfilingResultsDispatcher implements ProfilingResultsProvider.Dispatcher {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger(ProfilingResultsDispatcher.class.getName());
    private static final int QLengthLowerBound = 13;
    private static final int QLengthUpperBound = 15;
    private static ProfilingResultsDispatcher instance;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final AbstractDataFrameProcessor cpuDataProcessor = new CPUDataFrameProcessor();
    private final AbstractDataFrameProcessor cpuSamplingDataProcessor = new CPUSamplingDataFrameProcessor();
    private final AbstractDataFrameProcessor memoryDataProcessor = new MemoryDataFrameProcessor();
    private final AbstractDataFrameProcessor lockDataProcessor = new LockDataFrameProcessor();
    private final Object cpuDataProcessorQLengthLock = new Object();
    private final Object memDataProcessorQLengthLock = new Object();
    private final Object lockDataProcessorQLengthLock = new Object();
    private ExecutorService queueProcessor;
    private volatile boolean pauseFlag = true;

    // @GuardedBy cpuDataProcessorQLengthLock
    private int cpuDataProcessorQLength = 0;

    // @GuardedBy memDataProcessorQLengthLock
    private int memDataProcessorQLength = 0;

    // @GuardedBy lockDataProcessorQLengthLock
    private int lockDataProcessorQLength = 0;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static synchronized ProfilingResultsDispatcher getDefault() {
        if (instance == null) {
            instance = new ProfilingResultsDispatcher();
        }

        return instance;
    }

    public void addListener(final CPUProfilingResultListener listener) {
        cpuDataProcessor.addListener(listener);
        cpuSamplingDataProcessor.addListener(listener);
    }

    public void addListener(final MemoryProfilingResultsListener listener) {
        memoryDataProcessor.addListener(listener);
    }

    public void addListener(final LockProfilingResultListener listener) {
        lockDataProcessor.addListener(listener);
    }

    public synchronized void dataFrameReceived(final byte[] buffer, final int instrumentationType) {
        if (!cpuDataProcessor.hasListeners() && !memoryDataProcessor.hasListeners() &&
            !cpuSamplingDataProcessor.hasListeners() && !lockDataProcessor.hasListeners()) {
            return; // no consumers
        }

        switch (instrumentationType) {
            case CommonConstants.INSTR_RECURSIVE_FULL:
            case CommonConstants.INSTR_RECURSIVE_SAMPLED: {
                synchronized (cpuDataProcessorQLengthLock) {
                    cpuDataProcessorQLength++;

                    if (cpuDataProcessorQLength > QLengthUpperBound) {
                        try {
                            cpuDataProcessorQLengthLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    getExecutor().submit(new Runnable() {
                            public void run() {
                                try {
                                    cpuDataProcessor.processDataFrame(buffer);
                                } finally {
                                    synchronized (cpuDataProcessorQLengthLock) {
                                        cpuDataProcessorQLength--;

                                        if (cpuDataProcessorQLength < QLengthLowerBound) {
                                            cpuDataProcessorQLengthLock.notifyAll();
                                        }
                                    }
                                }
                            }
                        });
                }

                break;
            }
            case CommonConstants.INSTR_OBJECT_ALLOCATIONS:
            case CommonConstants.INSTR_OBJECT_LIVENESS: {
                synchronized (memDataProcessorQLengthLock) {
                    memDataProcessorQLength++;

                    if (memDataProcessorQLength > QLengthUpperBound) {
                        try {
                            memDataProcessorQLengthLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    getExecutor().submit(new Runnable() {
                            public void run() {
                                try {
                                    memoryDataProcessor.processDataFrame(buffer);
                                } finally {
                                    synchronized (memDataProcessorQLengthLock) {
                                        memDataProcessorQLength--;

                                        if (memDataProcessorQLength < QLengthLowerBound) {
                                            memDataProcessorQLengthLock.notifyAll();
                                        }
                                    }
                                }
                            }
                        });
                }

                break;
            }
            case CommonConstants.INSTR_NONE_SAMPLING: {
                synchronized (cpuDataProcessorQLengthLock) {
                    cpuDataProcessorQLength++;

                    if (cpuDataProcessorQLength > QLengthUpperBound) {
                        try {
                            cpuDataProcessorQLengthLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    getExecutor().submit(new Runnable() {
                            public void run() {
                                try {
                                    cpuSamplingDataProcessor.processDataFrame(buffer);
                                } finally {
                                    synchronized (cpuDataProcessorQLengthLock) {
                                        cpuDataProcessorQLength--;

                                        if (cpuDataProcessorQLength < QLengthLowerBound) {
                                            cpuDataProcessorQLengthLock.notifyAll();
                                        }
                                    }
                                }
                            }
                        });
                }

                break;
            }                
            default: {
                synchronized (lockDataProcessorQLengthLock) {
                    lockDataProcessorQLength++;

                    if (lockDataProcessorQLength > QLengthUpperBound) {
                        try {
                            lockDataProcessorQLengthLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    getExecutor().submit(new Runnable() {
                            public void run() {
                                try {
                                    lockDataProcessor.processDataFrame(buffer);
                                } finally {
                                    synchronized (lockDataProcessorQLengthLock) {
                                        lockDataProcessorQLength--;

                                        if (lockDataProcessorQLength < QLengthLowerBound) {
                                            lockDataProcessorQLengthLock.notifyAll();
                                        }
                                    }
                                }
                            }
                        });
                }
            }
        }
    }

    public void pause(boolean flush) {
        pauseFlag = true;
    }

    public void removeAllListeners() {
        cpuDataProcessor.removeAllListeners();
        cpuSamplingDataProcessor.removeAllListeners();
        memoryDataProcessor.removeAllListeners();
        lockDataProcessor.removeAllListeners();
    }

    public void removeListener(final CPUProfilingResultListener listener) {
        cpuDataProcessor.removeListener(listener);
        cpuSamplingDataProcessor.removeListener(listener);
    }

    public void removeListener(final MemoryProfilingResultsListener listener) {
        memoryDataProcessor.removeListener(listener);
    }

    public void removeListener(final LockProfilingResultListener listener) {
        lockDataProcessor.removeListener(listener);
    }

    public void reset() {
        fireReset();
    }

    public void resume() {
        pauseFlag = false;
    }

    public synchronized void shutdown() {
        //    queueProcessor.shutdownNow();
        fireShutdown(); // signalize shutdown
        removeAllListeners();
    }

    public synchronized void startup(ProfilerClient client) {
        fireStartup(client);
        resume();
    }

    private synchronized ExecutorService getExecutor() {
        if (queueProcessor == null) {
            queueProcessor = Executors.newSingleThreadExecutor();
        }

        return queueProcessor;
    }

    private synchronized void fireReset() {
        cpuDataProcessor.reset();
        cpuSamplingDataProcessor.reset();
        memoryDataProcessor.reset();
        lockDataProcessor.reset();
    }

    private synchronized void fireShutdown() {
        cpuDataProcessor.shutdown();
        cpuSamplingDataProcessor.shutdown();
        memoryDataProcessor.shutdown();
        lockDataProcessor.shutdown();
    }

    private synchronized void fireStartup(ProfilerClient client) {
        cpuSamplingDataProcessor.startup(client);
        cpuDataProcessor.startup(client);
        memoryDataProcessor.startup(client);
        lockDataProcessor.startup(client);
    }
}
