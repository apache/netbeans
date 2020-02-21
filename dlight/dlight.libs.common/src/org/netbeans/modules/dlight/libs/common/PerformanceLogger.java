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
package org.netbeans.modules.dlight.libs.common;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.openide.util.RequestProcessor;

/**
 * Logger for internal profiling performance issues.
 * 
 */
public class PerformanceLogger {
    
    /**
     * 
     * @return true if profiling enabled
     */
    public static boolean isProfilingEnabled() {
        return PROFILING_ENABLED;
    }

    /**
     * 
     * @return true if JVM support CPU profiling
     */
    public static boolean isCpuTimeProfilingAvailable() {
        return CPU_TIME_AVAILABLE;
    }

    public static final class PerformaceAction {

        /**
         * Log event.
         * 
         * @param extra additional attributes
         */
        public void log(Object... extra) {
            PerformanceLogger.INSANCE.log(this, extra);
        }
        
        /**
         * Event will be automatically logged after time out.
         * By default time out is infinite.
         * The automatic logging does not prevent final logging by method log.
         * It is ensured that timeout event is sent before regular logging event or is not sent.
         * 
         * @param timeOut in seconds
         */
        public void setTimeOut(int timeOut) {
            PerformanceLogger.INSANCE.setTimeOut(this, timeOut);
        }
        
        //<editor-fold defaultstate="collapsed" desc="Private Implementation">
        private final String id;
        private final Object source;
        private final long start;
        private final long cpuTime;
        private final long userTime;
        private final long threadID;

        private PerformaceAction(String id, Object source) {
            this.id = id;
            this.source = source;
            if (PROFILING_ENABLED) {
                start = System.nanoTime();
                if (CPU_TIME_AVAILABLE) {
                    cpuTime = threadMXBean.getCurrentThreadCpuTime();
                    userTime = threadMXBean.getCurrentThreadUserTime();
                    threadID = Thread.currentThread().getId();
                } else {
                    cpuTime = -1;
                    userTime = -1;
                    threadID = -1;
                }
            } else {
                start = 0;
                cpuTime = -1;
                userTime = -1;
                threadID = -1;
            }
        }
        //</editor-fold>
    }

    public interface PerformanceEvent {

        /**
         *
         * @return event ID
         */
        String getId();

        /**
         *
         * @return event source
         */
        Object getSource();

        /**
         *
         * @return additional attributes of event
         */
        Object[] getAttrs();

        /**
         *
         * @return event start time in nanoseconds
         */
        long getStartTime();

        /**
         *
         * @return wall time from start to log event in nanoseconds
         */
        long getTime();
        
        /**
         *
         * @return CPU time from event to log start in nanoseconds
         */
        long getCpuTime();

        /**
         *
         * @return user time from start to log event in nanoseconds
         */
        long getUserTime();

        /**
         *
         * @return java used memory in bytes
         */
        long getUsedMemory();
    }

    public interface PerformanceListener {

        void processEvent(PerformanceEvent event);
    }
    
    //<editor-fold defaultstate="collapsed" desc="Private Implementation">
    private static final boolean PROFILING_ENABLED;
    private static final boolean CPU_TIME_AVAILABLE;
    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    
    static {
        boolean isDebugMode = false;
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        try {
            List<String> args = runtime.getInputArguments();
            if (args.contains("-Xdebug") || args.contains("-agentlib:jdwp")) { //NOI18N
                isDebugMode = true;
            } else {
                for (String arg : args) {
                    if (arg.startsWith("-agentlib:jdwp=")) { //NOI18N
                        isDebugMode = true;
                        break;
                    }
                }
            }
        } catch (SecurityException ex) {
        }
        String enabled = System.getProperty("dlight.libs.common.profiling.enabled", "false"); //NOI18N
        if ("true".equals(enabled)) { //NOI18N
            PROFILING_ENABLED = true;
        } else if ("false".equals(enabled)) { //NOI18N
            PROFILING_ENABLED = false;
        }  else if ("auto".equals(enabled)) { //NOI18N
            PROFILING_ENABLED = !isDebugMode;
        } else {
            PROFILING_ENABLED = false;
        }
        boolean cpu = true;
        if (PROFILING_ENABLED) {
            try {
                threadMXBean.setThreadCpuTimeEnabled(true);
            } catch (UnsupportedOperationException ex) {
                cpu = false;
            } catch (SecurityException ex) {
                cpu = false;
            }
        } else {
            cpu = false;
        }
        CPU_TIME_AVAILABLE = cpu;
    }

    private static final PerformanceLogger INSANCE = new PerformanceLogger();
    private final ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();
    private final List<PerformanceListener> listeners = new ArrayList<PerformanceListener>();
    private final ReentrantReadWriteLock lineLock = new ReentrantReadWriteLock();
    private final LinkedList<PerformanceEvent> line = new LinkedList<PerformanceEvent>();
    private final Map<PerformaceAction, Integer> register = new HashMap<PerformaceAction, Integer>();
    private final ScheduledFuture<?> periodicTask;
    private static final int SCHEDULE = 1; // period in seconds

    private PerformanceLogger() {
        if (PROFILING_ENABLED) {
            periodicTask = new RequestProcessor("PerformanceLoggerUpdater").scheduleAtFixedRate(new Runnable() { //NOI18N
                @Override
                public void run() {
                    while (true) {
                        PerformanceEvent last = null;
                        lineLock.writeLock().lock();
                        try {
                            if (!line.isEmpty()) {
                                last = line.pollLast();
                            }
                        } finally {
                            lineLock.writeLock().unlock();
                        }
                        if (last != null) {
                            listenersLock.readLock().lock();
                            try {
                                if (listeners.size() > 0) {
                                    for (PerformanceListener listener : listeners) {
                                        listener.processEvent(last);
                                    }
                                }
                            } finally {
                                listenersLock.readLock().unlock();
                            }
                        } else {
                            List<PerformaceAction> toSend = null;
                            lineLock.writeLock().lock();
                            try {
                                if (!register.isEmpty()) {
                                    toSend = new ArrayList<PerformaceAction>();
                                    for(Map.Entry<PerformaceAction, Integer> entry : register.entrySet()) {
                                        long delta = System.nanoTime() - entry.getKey().start;
                                        if (delta/1000/1000/1000 > entry.getValue()) {
                                            toSend.add(entry.getKey());
                                        }
                                    }
                                }
                            } finally {
                                lineLock.writeLock().unlock();
                            }
                            if (toSend != null && !toSend.isEmpty()) {
                                for(PerformaceAction action : toSend) {
                                    logTimeOut(action);
                                }
                            }
                            return;
                        }
                    }
                }
            }, SCHEDULE, SCHEDULE, TimeUnit.SECONDS);
        } else {
            periodicTask = null;
        }
    }
    

    private void setTimeOut(PerformaceAction action, int timeOut) {
        if (PROFILING_ENABLED) {
            lineLock.writeLock().lock();
            try {
                register.put(action, timeOut);
            } finally {
                lineLock.writeLock().unlock();
            }
        }
    }

    private void log(PerformaceAction action, Object... extra) {
        if (PROFILING_ENABLED) {
            long delta = System.nanoTime() - action.start;
            Runtime runtime = Runtime.getRuntime();
            long usedMemeory = runtime.totalMemory() - runtime.freeMemory();
            long cpuTime;
            long userTime;
            if (CPU_TIME_AVAILABLE && action.cpuTime != -1 && action.userTime != -1) {
                cpuTime = threadMXBean.getCurrentThreadCpuTime() - action.cpuTime;
                userTime = threadMXBean.getCurrentThreadUserTime() - action.userTime;
            } else {
                cpuTime = 0;
                userTime = 0;
            }
            PerformanceEvent event = new PerformanceEventImpl(action.id, action.source, action.start, delta, cpuTime, userTime, usedMemeory, extra);
            lineLock.writeLock().lock();
            try {
                register.remove(action);
                line.addFirst(event);
            } finally {
                lineLock.writeLock().unlock();
            }
        }
    }
    
    private void logTimeOut(PerformaceAction action) {
        if (PROFILING_ENABLED) {
            long delta = System.nanoTime() - action.start;
            Runtime runtime = Runtime.getRuntime();
            long usedMemeory = runtime.totalMemory() - runtime.freeMemory();
            long cpuTime;
            long userTime;
            if (CPU_TIME_AVAILABLE && action.cpuTime != -1 && action.userTime != -1 && action.threadID >= 0) {
                cpuTime = threadMXBean.getThreadCpuTime(action.threadID) - action.cpuTime;
                userTime = threadMXBean.getThreadUserTime(action.threadID) - action.userTime;
            } else {
                cpuTime = 0;
                userTime = 0;
            }
            PerformanceEvent event = new PerformanceEventImpl(action.id, action.source, action.start, delta, cpuTime, userTime, usedMemeory, new Object[0]);
            lineLock.writeLock().lock();
            try {
                Integer remove = register.remove(action);
                if (remove != null) {
                    line.addFirst(event);
                }
            } finally {
                lineLock.writeLock().unlock();
            }
        }
    }
    //</editor-fold>

    public static PerformanceLogger getLogger() {
        return INSANCE;
    }

    public PerformaceAction start(String id, Object source) {
        return new PerformaceAction(id, source);
    }

    public void addPerformanceListener(PerformanceListener listener) {
        listenersLock.writeLock().lock();
        try {
            listeners.add(listener);
        } finally {
            listenersLock.writeLock().unlock();
        }
    }

    public void removePerformanceListener(PerformanceListener listener) {
        listenersLock.writeLock().lock();
        try {
            listeners.remove(listener);
        } finally {
            listenersLock.writeLock().unlock();
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Private Class">
    private static final class PerformanceEventImpl implements PerformanceEvent {

        private final String id;
        private final Object source;
        private final long startTime;
        private final long time;
        private final long cpu;
        private final long user;
        private final Object[] extra;
        private final long usedMemeory;

        private PerformanceEventImpl(String id, Object source, long startTime, long time, long cpu, long user, long usedMemeory, Object[] extra) {
            this.id = id;
            this.source = source;
            this.startTime = startTime;
            this.time = time;
            this.cpu = cpu;
            this.user = user;
            this.extra = extra;
            this.usedMemeory = usedMemeory;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Object getSource() {
            return source;
        }

        @Override
        public Object[] getAttrs() {
            return extra;
        }

        @Override
        public long getStartTime() {
            return startTime;
        }

        @Override
        public long getTime() {
            return time;
        }

        @Override
        public long getCpuTime() {
            return cpu;
        }

        @Override
        public long getUserTime() {
            return user;
        }

        @Override
        public long getUsedMemory() {
            return usedMemeory;
        }
    }
    //</editor-fold>
}
