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

package org.netbeans.modules.profiler.ppoints;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.results.DataManagerListener;
import org.netbeans.lib.profiler.client.MonitoredData;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.results.monitor.VMTelemetryDataManager;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
public class GlobalProfilingPointsProcessor implements DataManagerListener {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static GlobalProfilingPointsProcessor defaultInstance;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private List<TimedGlobalProfilingPoint> scheduledTimedPPs = new ArrayList<>();
    private List<TriggeredGlobalProfilingPoint> scheduledTriggeredPPs = new ArrayList<>();
    private ProfilingSettings profilingSettings;
    private Lookup.Provider profiledProject;
    private GlobalProfilingPoint[] gpp;
    private boolean isRunning = false;
    private long currentHeapSize;
    private long currentHeapUsage;
    private long currentLoadedClasses;
    private long currentSurvGen;
    private long currentCpuTime;
    private long currentGcTime;
    private long currentThreads;
    private long currentTime = System.currentTimeMillis(); // local time of one iteration

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- DataManagerListener implementation ------------------------------------
    public void dataChanged() {
        processTelemetryEvent();
    }

    public void dataReset() {
        processTelemetryEvent();
    }

    // --- Internal interface ----------------------------------------------------
    static synchronized GlobalProfilingPointsProcessor getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new GlobalProfilingPointsProcessor();
        }

        return defaultInstance;
    }

    void notifyProfilingStateChanged() {
        synchronized (this) {
            boolean profilingInProgress = ProfilingPointsManager.getDefault().isProfilingInProgress();
            boolean sessionInProgress = ProfilingPointsManager.getDefault().isProfilingSessionInProgress();

            if (sessionInProgress && !profilingInProgress) { // transition between states

                if (isRunning) {
                    stop(); // modify profiling
                }

//                init(); // TODO: unnecessarily called when finishing profiling session
            } else if (profilingInProgress) { // profiling in progress

                if (!isRunning) {
                    init(); // #232978
                    start();
                }
            } else { // profiling inactive
                stop();
            }
        }
    }

    private boolean anyProfilingPointsScheduled() {
        return (scheduledTimedPPs.size() > 0) || (scheduledTriggeredPPs.size() > 0);
    }

    private void checkForStop() {
        if (!anyProfilingPointsScheduled()) {
            stop();
        }
    }

    private void init() {
        profiledProject = NetBeansProfiler.getDefaultNB().getProfiledProject();
        profilingSettings = Profiler.getDefault().getLastProfilingSettings();

        if ((profiledProject != null) && profilingSettings.useProfilingPoints()) {
            gpp = ProfilingPointsManager.getDefault().createGlobalProfilingConfiguration(profiledProject, profilingSettings);

            for (GlobalProfilingPoint pp : gpp) {
                scheduleProfilingPoint(pp);
            }
        }
    }

    private void initListeners() {
        Profiler.getDefault().getVMTelemetryManager().addDataListener(this);
    }

    private void processTelemetryEvent() {
        VMTelemetryDataManager dataManager = Profiler.getDefault().getVMTelemetryManager();
        MonitoredData data = dataManager.getLastData();

        if (data != null) {
            // ----------------------
            // Actually this is being called periodically each 1.2 sec from ProfilingMonitor, can be also used as a timer for timed Profiling Points
            // If no MonitoredData available, also other data most likely won't be available => that's why calling it here
            processTimeEvent();

            // ----------------------
            long currentMaxHeap = dataManager.maxHeapSize;
            currentHeapSize = data.getTotalMemory();

            long currentUsedHeap = currentHeapSize - data.getFreeMemory();
            currentHeapUsage = (long) Math.round(((double) currentUsedHeap / (double) currentMaxHeap) * 100);
            currentSurvGen = data.getNSurvivingGenerations();
            currentLoadedClasses = data.getLoadedClassesCount();
            currentCpuTime = dataManager.processCPUTimeInPromile[dataManager.getItemCount() - 1] / 10;
            currentGcTime = data.getRelativeGCTimeInPerMil() / 10;
            currentThreads = data.getNThreads();

            processTriggeredProfilingPoints();
        } else {
            // no telemetry data available yet
        }
    }

    // - Core functionality ----------------------------------------------------
    private void processTimeEvent() {
        currentTime = System.currentTimeMillis();
        processTimedProfilingPoints();
    }

    private void processTimedProfilingPoint(TimedGlobalProfilingPoint tgpp, List<TimedGlobalProfilingPoint> rescheduledTimedPPs) {
        tgpp.hit(currentTime);
        scheduleTimedProfilingPoint(tgpp, rescheduledTimedPPs);
    }

    private void processTimedProfilingPoints() {
        synchronized (this) {
            checkForStop();

            if (isRunning) {
                List<TimedGlobalProfilingPoint> rescheduledTimedPPs = new ArrayList<>();

                for (TimedGlobalProfilingPoint tgpp : scheduledTimedPPs) {
                    if (timeConditionMet(tgpp.getCondition())) {
                        processTimedProfilingPoint(tgpp, rescheduledTimedPPs); // Perform PP and eventually reschedule it
                    } else {
                        rescheduledTimedPPs.add(tgpp); // reschedule currently not active PP for next check
                    }
                }

                scheduledTimedPPs.clear();
                scheduledTimedPPs.addAll(rescheduledTimedPPs);
            }
        }
    }

    private void processTriggeredProfilingPoint(TriggeredGlobalProfilingPoint tgpp,
                                                List<TriggeredGlobalProfilingPoint> rescheduledTriggeredPPs) {
        TriggeredGlobalProfilingPoint.TriggerCondition condition = tgpp.getCondition();

        if (!condition.isTriggered()) {
            condition.setTriggered(true);

            long hitValue = -1;

            if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_HEAPSIZ) {
                hitValue = currentHeapSize;
            } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_HEAPUSG) {
                hitValue = currentHeapUsage;
            } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_SURVGEN) {
                hitValue = currentSurvGen;
            } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_LDCLASS) {
                hitValue = currentLoadedClasses;
            } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_CPUUSG) {
                hitValue = currentCpuTime;
            } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_GCUSG) {
                hitValue = currentGcTime;
            } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_THREADS) {
                hitValue = currentThreads;
            }

            tgpp.hit(hitValue);
        } else {
            condition.setTriggered(false);
        }

        if (!condition.isOnetime()) {
            scheduleTriggeredProfilingPoint(tgpp, rescheduledTriggeredPPs);
        }
    }

    private void processTriggeredProfilingPoints() {
        synchronized (this) {
            checkForStop();

            if (isRunning) {
                List<TriggeredGlobalProfilingPoint> rescheduledTriggeredPPs = new ArrayList<>();

                for (TriggeredGlobalProfilingPoint tgpp : scheduledTriggeredPPs) {
                    if (triggerConditionMet(tgpp.getCondition())) {
                        processTriggeredProfilingPoint(tgpp, rescheduledTriggeredPPs); // Perform PP and eventually reschedule it
                    } else {
                        rescheduledTriggeredPPs.add(tgpp); // reschedule currently not active PP for next check
                    }
                }

                scheduledTriggeredPPs.clear();
                scheduledTriggeredPPs.addAll(rescheduledTriggeredPPs);
            }
        }
    }

    private void reset() {
        gpp = null;
        profiledProject = null;
        profilingSettings = null;
        scheduledTimedPPs.clear();
        scheduledTriggeredPPs.clear();
    }

    private void resetListeners() {
        Profiler.getDefault().getVMTelemetryManager().removeDataListener(this);
    }

    private void scheduleProfilingPoint(GlobalProfilingPoint gpp) {
        if (gpp instanceof TimedGlobalProfilingPoint) {
            scheduleTimedProfilingPoint((TimedGlobalProfilingPoint) gpp, scheduledTimedPPs);
        } else if (gpp instanceof TriggeredGlobalProfilingPoint) {
            scheduleTriggeredProfilingPoint((TriggeredGlobalProfilingPoint) gpp, scheduledTriggeredPPs);
        }
    }

    private void scheduleTimedProfilingPoint(TimedGlobalProfilingPoint gpp, List<TimedGlobalProfilingPoint> timedPPs) {
        TimedGlobalProfilingPoint.TimeCondition tc = gpp.getCondition();

        long currentTime = System.currentTimeMillis();
        long scheduledTime = tc.getScheduledTime();
        boolean repeats = tc.getRepeats();

        long periodTime = (long) tc.getPeriodTime();

        switch (tc.getPeriodUnits()) {
            case TimedGlobalProfilingPoint.TimeCondition.UNITS_MINUTES:
                periodTime *= (60 * 1000);

                break;
            case TimedGlobalProfilingPoint.TimeCondition.UNITS_HOURS:
                periodTime *= (60 * 60 * 1000);

                break;
            default:
                break;
        }

        if (scheduledTime < currentTime) {
            if (!repeats) {
                return; // old, won't schedule
            }

            long factor = (long) Math.ceil((double) (currentTime - scheduledTime) / (double) periodTime); // some periods missed, compute first following period
            scheduledTime += (factor * periodTime);
        }

        if (scheduledTime >= currentTime) { // should be always true, forced by the above code
            tc.setScheduledTime(scheduledTime);
            timedPPs.add(gpp);
        }
    }

    private void scheduleTriggeredProfilingPoint(TriggeredGlobalProfilingPoint gpp,
                                                 List<TriggeredGlobalProfilingPoint> triggeredPPs) {
        triggeredPPs.add(gpp);
    }

    // --- Private implementation ------------------------------------------------

    // - Lifecycle management --------------------------------------------------
    private void start() {
        if ((profiledProject == null) || (gpp == null) || (gpp.length == 0) || !anyProfilingPointsScheduled()) {
            reset();

            return;
        }

        isRunning = true;
        initListeners();
    }

    private void stop() {
        isRunning = false;
        resetListeners();
        reset();
    }

    private boolean timeConditionMet(TimedGlobalProfilingPoint.TimeCondition condition) {
        return condition.getScheduledTime() <= currentTime;
    }

    private boolean triggerConditionMet(TriggeredGlobalProfilingPoint.TriggerCondition condition) {
        if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_HEAPSIZ) {
            return condition.isTriggered() ? (condition.getValue() >= currentHeapSize) : (condition.getValue() < currentHeapSize);
        } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_HEAPUSG) {
            return condition.isTriggered() ? (condition.getValue() >= currentHeapUsage) : (condition.getValue() < currentHeapUsage);
        } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_SURVGEN) {
            return condition.isTriggered() ? (condition.getValue() >= currentSurvGen) : (condition.getValue() < currentSurvGen);
        } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_LDCLASS) {
            return condition.isTriggered() ? (condition.getValue() >= currentLoadedClasses)
                                           : (condition.getValue() < currentLoadedClasses);
        } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_CPUUSG) {
            return condition.isTriggered() ? (condition.getValue() >= currentCpuTime) : (condition.getValue() < currentCpuTime);
        } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_GCUSG) {
            return condition.isTriggered() ? (condition.getValue() >= currentGcTime) : (condition.getValue() < currentGcTime);
        } else if (condition.getMetric() == TriggeredGlobalProfilingPoint.TriggerCondition.METRIC_THREADS) {
            return condition.isTriggered() ? (condition.getValue() >= currentThreads) : (condition.getValue() < currentThreads);
        } else {
            return false;
        }
    }
}
