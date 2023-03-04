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

package org.netbeans.lib.profiler.results.threads;

import org.netbeans.lib.profiler.global.CommonConstants;
import java.awt.*;


/**
 * A representation of the thread timeline data for a single thread
 *
 * @author Misha Dmitriev
 */
public class ThreadData {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static final byte NO_STATE = 127;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final Object dataLock = new Object();
    private final String name;
    private final String className;
    private long[] times = new long[6];
    private byte[] threadStates; // Array of states corresponding to above timestamps
                                 // @GuardedBy dataLock

    // @GuardedBy dataLock
    private long[] timeStamps; // Array of points in time at which thread's state changes
                               // @GuardedBy dataLock

    // @GuardedBy dataLock
    private int capacity;
    private int curSize;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ThreadData(String name, String className) {
        synchronized (dataLock) {
            capacity = 50;
            timeStamps = new long[capacity];
            threadStates = new byte[capacity];
            curSize = 0;
        }

        this.name = name;
        this.className = className;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getClassName() {
        return className;
    }

    public byte getFirstState() {
        synchronized (dataLock) {
            if (curSize == 0) {
                return NO_STATE;
            } else {
                return threadStates[0];
            }
        }
    }

    public long getFirstTimeStamp() {
        synchronized (dataLock) {
            if (curSize == 0) {
                return 0;
            } else {
                return timeStamps[0];
            }
        }
    }

    public byte getLastState() {
        synchronized (dataLock) {
            if (curSize == 0) {
                return NO_STATE;
            } else {
                return threadStates[curSize - 1];
            }
        }
    }

    public long getLastTimeStamp() {
        synchronized (dataLock) {
            if (curSize == 0) {
                return 0;
            } else {
                return timeStamps[curSize - 1];
            }
        }
    }

    public String getName() {
        synchronized (dataLock) {
            return name;
        }
    }
    
    public long getRunningTime(long lastTimestamp) {
        synchronized (dataLock) {
            long time = times[CommonConstants.THREAD_STATUS_RUNNING];
            if (getLastState() == CommonConstants.THREAD_STATUS_RUNNING)
                time += (lastTimestamp - getLastTimeStamp());
            return time;
        }
    }
    
    public long getSleepingTime(long lastTimestamp) {
        synchronized (dataLock) {
            long time = times[CommonConstants.THREAD_STATUS_SLEEPING];
            if (getLastState() == CommonConstants.THREAD_STATUS_SLEEPING)
                time += (lastTimestamp - getLastTimeStamp());
            return time;
        }
    }
    
    public long getWaitTime(long lastTimestamp) {
        synchronized (dataLock) {
            long time = times[CommonConstants.THREAD_STATUS_WAIT];
            if (getLastState() == CommonConstants.THREAD_STATUS_WAIT)
                time += (lastTimestamp - getLastTimeStamp());
            return time;
        }
    }
    
    public long getParkTime(long lastTimestamp) {
        synchronized (dataLock) {
            long time = times[CommonConstants.THREAD_STATUS_PARK];
            if (getLastState() == CommonConstants.THREAD_STATUS_PARK)
                time += (lastTimestamp - getLastTimeStamp());
            return time;
        }
    }
    
    public long getMonitorTime(long lastTimestamp) {
        synchronized (dataLock) {
            long time = times[CommonConstants.THREAD_STATUS_MONITOR];
            if (getLastState() == CommonConstants.THREAD_STATUS_MONITOR)
                time += (lastTimestamp - getLastTimeStamp());
            return time;
        }
    }
    
    public long getTotalTime(long lastTimestamp) {
        return isAliveState(getLastState()) ? lastTimestamp - getFirstTimeStamp() :
                                              getLastTimeStamp() - getFirstTimeStamp();
    }

    public byte getStateAt(int idx) {
        synchronized (dataLock) {
            return threadStates[idx];
        }
    }

    public static Color getThreadStateColor(int threadState) {
        switch (threadState) {
            case CommonConstants.THREAD_STATUS_UNKNOWN:
                return CommonConstants.THREAD_STATUS_UNKNOWN_COLOR;
            case CommonConstants.THREAD_STATUS_ZOMBIE:
                return CommonConstants.THREAD_STATUS_ZOMBIE_COLOR;
            case CommonConstants.THREAD_STATUS_RUNNING:
                return CommonConstants.THREAD_STATUS_RUNNING_COLOR;
            case CommonConstants.THREAD_STATUS_SLEEPING:
                return CommonConstants.THREAD_STATUS_SLEEPING_COLOR;
            case CommonConstants.THREAD_STATUS_MONITOR:
                return CommonConstants.THREAD_STATUS_MONITOR_COLOR;
            case CommonConstants.THREAD_STATUS_WAIT:
                return CommonConstants.THREAD_STATUS_WAIT_COLOR;
            case CommonConstants.THREAD_STATUS_PARK:
                return CommonConstants.THREAD_STATUS_PARK_COLOR;
            default:
                return CommonConstants.THREAD_STATUS_UNKNOWN_COLOR;
        }
    }
    
    public static boolean isAliveState(int threadState) {
        if (threadState == CommonConstants.THREAD_STATUS_RUNNING) return true;
        if (threadState == CommonConstants.THREAD_STATUS_SLEEPING) return true;
        if (threadState == CommonConstants.THREAD_STATUS_MONITOR) return true;
        if (threadState == CommonConstants.THREAD_STATUS_WAIT) return true;
        if (threadState == CommonConstants.THREAD_STATUS_PARK) return true;
        return false;
    }

    public Color getThreadStateColorAt(int idx) {
        synchronized (dataLock) {
            return getThreadStateColor(threadStates[idx]);
        }
    }

    public long getTimeStampAt(int idx) {
        synchronized (dataLock) {
            return timeStamps[idx];
        }
    }

    public void add(long timeStamp, byte threadState) {
        synchronized (dataLock) {
            if (curSize == capacity) {
                long[] oldStamps = timeStamps;
                byte[] oldStates = threadStates;
                int oldCapacity = capacity;
                capacity = capacity * 2;
                timeStamps = new long[capacity];
                threadStates = new byte[capacity];
                System.arraycopy(oldStamps, 0, timeStamps, 0, oldCapacity);
                System.arraycopy(oldStates, 0, threadStates, 0, oldCapacity);
            }

            timeStamps[curSize] = timeStamp;
            threadStates[curSize] = threadState;
            
            if (curSize > 0) {
                long duration = timeStamp - timeStamps[curSize - 1];
                times[threadStates[curSize - 1]] += duration;
                times[0] += duration;
            }
            
            curSize++;
        }
    }

    public void clearStates() {
        synchronized (dataLock) {
            capacity = 50;
            timeStamps = new long[capacity];
            threadStates = new byte[capacity];
            curSize = 0;
            times = new long[6];
        }
    }

    public int size() {
        synchronized (dataLock) {
            return curSize;
        }
    }
    
    public String toString() {
        return getName();
    }
}
