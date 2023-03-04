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

/*
 * MemoryTestCase.java
 *
 * Created on July 19, 2005, 5:21 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package org.netbeans.lib.profiler.tests.jfluid.memory;

import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.results.EventBufferResultsProvider;
import org.netbeans.lib.profiler.results.ProfilingResultsDispatcher;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.memory.MemoryCCTProvider;
import org.netbeans.lib.profiler.results.memory.MemoryCallGraphBuilder;
import org.netbeans.lib.profiler.tests.jfluid.*;
import org.netbeans.lib.profiler.tests.jfluid.utils.*;
import org.netbeans.lib.profiler.utils.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.lib.profiler.global.CommonConstants;

/**
 *
 * @author ehucka
 */
public abstract class MemoryTestCase extends CommonProfilerTestCase {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class MemoryResultListener implements MemoryCCTProvider.Listener {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private final Object resultsLock = new Object();
        private boolean hasResults = false;

        //~ Methods --------------------------------------------------------------------------------------------------------------
        public void cctEstablished(RuntimeCCTNode appRootNode) {
            System.out.println("Memory CCT Established");

            synchronized (resultsLock) {
                hasResults = true;
                resultsLock.notify();
            }
        }

        public void cctReset() {
            synchronized (resultsLock) {
                hasResults = false;
                resultsLock.notify();
            }
        }

        public boolean wait4results(long timeout) {
            synchronized (resultsLock) {
                if (!hasResults) {
                    try {
                        resultsLock.wait(timeout);
                    } catch (InterruptedException e) {
                    }
                }

                return hasResults;
            }
        }

        public void cctEstablished(RuntimeCCTNode appRootNode, boolean empty) {
            if (!empty) {
                cctEstablished(appRootNode);
            }
        //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    MemoryCallGraphBuilder builder = new MemoryCallGraphBuilder();
    MemoryResultListener resultListener = null;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of MemoryTestCase
     */
    public MemoryTestCase(String name) {
        super(name);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    protected void checkMemoryResults(TargetAppRunner targetAppRunner, String[] matchingPrefixes,
            MemoryResultListener resultListener, int instrMode)
            throws Exception {
        boolean gotResults = false;
        int retryCounter = 4;

        do {
            targetAppRunner.getProfilerClient().forceObtainedResultsDump();
            gotResults = resultListener.wait4results(2500);
        } while (!gotResults && (--retryCounter > 0));

        assertTrue("CallGraphBuilder: Results do not exist.", gotResults);
        log("results obtained: " + System.currentTimeMillis());

        int[] totalAllocObjects = targetAppRunner.getProfilerClient().getAllocatedObjectsCountResults();
        String[] classnames = targetAppRunner.getProfilerClient().getStatus().getClassNames();
        long[] totalAllocObjectsSize = null;
        ArrayList list = new ArrayList(128);
        totalAllocObjectsSize = builder.getAllocObjectNumbers();
        
        if (instrMode == CommonConstants.INSTR_OBJECT_ALLOCATIONS) {
            for (int i = 0; i < totalAllocObjectsSize.length; i++) {
                boolean match = false;

                for (int j = 0; j < matchingPrefixes.length; j++) {
                    if (classnames[i].startsWith(matchingPrefixes[j])) {
                        match = true;

                        break;
                    }
                }

                if (match) {
                    StringBuilder out = new StringBuilder();
                    out.append(complete(StringUtils.userFormClassName(classnames[i]), 32));
                    out.append(complete(String.valueOf(totalAllocObjects[i]), 8));
                    //out.append(complete(StringUtils.nBytesToString(totalAllocObjectsSize[i]), 10));
                    list.add(out.toString());
                }
            }

            ref(complete("Name", 32) + complete("DCount", 8)); //+complete("DSize", 10));
        }

        if (instrMode == CommonConstants.INSTR_OBJECT_LIVENESS) {
            try {
                MemoryCCTProvider.ObjectNumbersContainer liveness = builder.getLivenessObjectNumbers();

                totalAllocObjectsSize = liveness.trackedLiveObjectsSize;

                float[] avgage = liveness.avgObjectAge;
                int[] maxSurvGen = liveness.maxSurvGen;
                long[] ntrackedallocobjects = liveness.nTrackedAllocObjects;
                int[] ntrackedliveobjects = liveness.nTrackedLiveObjects;

                for (int i = 0; i < totalAllocObjectsSize.length; i++) {
                    boolean match = false;

                    for (int j = 0; j < matchingPrefixes.length; j++) {
                        if (classnames[i].startsWith(matchingPrefixes[j])) {
                            match = true;

                            break;
                        }
                    }

                    if (match) {
                        StringBuilder out = new StringBuilder();
                        out.append(complete(StringUtils.userFormClassName(classnames[i]), 32));
                        //out.append(complete(StringUtils.nBytesToString(totalAllocObjectsSize[i]), 10));
                        out.append(complete(String.valueOf(ntrackedliveobjects[i]), 10));
                        out.append(complete(String.valueOf(ntrackedallocobjects[i]), 8));
                        //out.append(complete(String.valueOf((int)avgage[i]), 8));
                        //out.append(complete(String.valueOf(maxSurvGen[i]), 8));
                        list.add(out.toString());
                    }
                }

                ref(complete("Name", 32) /*complete("LiveBytes", 10)+*/ + complete("LiveObjs", 10) + complete("Allocs", 8)); //+complete("AvgAge", 8)+complete("MaxSurv", 8));
            } catch (IllegalStateException e) {
            }
        }
        //log results
        Collections.sort(list);

        for (int i = 0; i < list.size(); i++) {
            ref(list.get(i));
        }

        ref("");
    }

    protected ProfilerEngineSettings initMemoryTest(String projectName, String className) {
        ProfilerEngineSettings settings = initTest(projectName, className, null);
        //defaults
        settings.setThreadCPUTimerOn(false);
        settings.setAllocTrackEvery(1); //default is not strict - cannot be measured in test
        settings.setRunGCOnGetResultsInMemoryProfiling(true);

        return settings;
    }

    protected void startMemoryTest(ProfilerEngineSettings settings, int instrMode, String[] classPrefixes) {
        //create runner //instrMode CommonConstants.INSTR_OBJECT_ALLOCATIONS
        assertTrue(builder != null);

        TestProfilerAppHandler handler = new TestProfilerAppHandler(this);
        TargetAppRunner runner = new TargetAppRunner(settings, handler, new TestProfilingPointsProcessor());
        runner.addProfilingEventListener(Utils.createProfilingListener(this));

        builder.removeAllListeners();
        ProfilingResultsDispatcher.getDefault().removeAllListeners();

        resultListener = new MemoryResultListener();
        builder.addListener(resultListener);

        ProfilingResultsDispatcher.getDefault().addListener(builder);
        builder.startup(runner.getProfilerClient());

        try {
            assertTrue("not read calibration data", runner.readSavedCalibrationData());
            runner.getProfilerClient().initiateMemoryProfInstrumentation(instrMode);

            Process p = startTargetVM(runner);
            assertNotNull("Target JVM is not started", p);
            bindStreams(p);
            runner.attachToTargetVMOnStartup();

            waitForStatus(STATUS_RUNNING);
            assertTrue("runner is not running", runner.targetAppIsRunning());

            waitForStatus(STATUS_RESULTS_AVAILABLE | STATUS_APP_FINISHED);

            if (!isStatus(STATUS_APP_FINISHED)) {
                waitForStatus(STATUS_APP_FINISHED);
            }
            Thread.sleep(1000);
            checkMemoryResults(runner, classPrefixes, resultListener, instrMode);
            setStatus(STATUS_MEASURED);
        } catch (Exception ex) {
            log(ex);
            assertTrue("Exception thrown: " + ex.getMessage(), false);
        } finally {
            ProfilingResultsDispatcher.getDefault().pause(true);
            builder.shutdown();

            builder.removeListener(resultListener);
            ProfilingResultsDispatcher.getDefault().removeListener(builder);

            finalizeTest(runner);
        }
    }
}
