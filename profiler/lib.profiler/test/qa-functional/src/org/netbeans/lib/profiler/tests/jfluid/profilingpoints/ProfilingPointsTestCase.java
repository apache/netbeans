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
 * ProfilingPointsTestCase.java
 *
 * Created on July 19, 2005, 5:20 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package org.netbeans.lib.profiler.tests.jfluid.profilingpoints;

import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.tests.jfluid.*;

//import org.netbeans.lib.profiler.client.ProfilingPoint;
//import org.netbeans.lib.profiler.client.ProfilingPoint.HitEvent;
import org.netbeans.lib.profiler.tests.jfluid.utils.TestProfilerAppHandler;
import org.netbeans.lib.profiler.tests.jfluid.utils.Utils;
import org.netbeans.lib.profiler.utils.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


/**
 *
 * @author ehucka
 */
public abstract class ProfilingPointsTestCase extends CommonProfilerTestCase {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static boolean CHECK_ACCURACY = false;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    double MAX_DIFF_PERC = 70;
    int ppid = 0;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    //    StartStopwatch startStopWatch = null;
    //    StopStopwatch stopStopWatch = null;

    //    HashMap<StopWatchPP, ArrayList<Long>> results = new HashMap(128);
    public ProfilingPointsTestCase(String name) {
        super(name);
    }

    //    protected ProfilerEngineSettings initPPTest(String projectName, String mainClass, String[][] rootMethods) {
    //        ProfilerEngineSettings settings = initTest(projectName, mainClass, rootMethods);
    //        //defaults
    //        settings.setInstrumentSpawnedThreads(true);
    //        settings.setExcludeWaitTime(true);
    //        settings.setNProfiledThreadsLimit(32);
    //        //addJVMArgs(settings, "-Dorg.netbeans.lib.profiler.wireprotocol.WireIO=true");
    //        
    //        settings.setThreadCPUTimerOn(false);
    //        
    //        return settings;
    //    }

    //    protected void startPPStopwatchTest(ProfilerEngineSettings settings, StopWatchPP[] stopwatches) {
    //        
    //        startStopWatch = new StartStopwatch();
    //        stopStopWatch = new StopStopwatch();
    //        
    //        ProfilingPoint[] points;
    //        ArrayList<ProfilingPoint> list=new ArrayList(64);
    //        for (StopWatchPP stopw:stopwatches) {
    //            points = stopw.getPPoints(this);
    //            for (ProfilingPoint pp:points) {
    //                list.add(pp);
    //            }
    //        }
    //        settings.setProfilingPoints(list.toArray(new ProfilingPoint[0]));
    //        
    //        TargetAppRunner runner = new TargetAppRunner(settings, new TestProfilerAppHandler(this));
    //        runner.getProfilingSessionStatus().startProfilingPointsActive = true;
    //        runner.addProfilingEventListener(Utils.createProfilingListener(this));
    //        try {
    //            runner.readSavedCalibrationData();
    //            runner.getProfilerClient().initiateRecursiveCPUProfInstrumentation(settings.getInstrumentationRootMethods());
    //
    //            Process p = startTargetVM(runner);
    //            assertNotNull("Target JVM is not started", p);
    //            bindStreams(p);            
    //            runner.attachToTargetVMOnStartup();
    //            
    //            waitForStatus(STATUS_RUNNING);
    //            assertTrue("runner is not running", runner.targetAppIsRunning());
    //            
    //            waitForStatus(STATUS_APP_FINISHED);
    //            if (runner.targetJVMIsAlive()) {
    //                log("JVM is Alive: "+System.currentTimeMillis());
    //            }
    //            setStatus(STATUS_MEASURED);
    //            
    //            ref("Stopwatches:");
    //            for (StopWatchPP stopw:stopwatches) {
    //                if (results.get(stopw) != null)
    //                    stopw.measureStopwatch(results.get(stopw), this);
    //                else
    //                    log("\nStopwatch "+stopw+" has null results.");
    //            }
    //            
    //            setStatus(STATUS_MEASURED);
    //        } catch (Exception ex) {
    //            log(ex);
    //            assertTrue("Exception thrown: "+ex.getMessage(), false);
    //        } finally {
    //            finalizeTest(runner);
    //        }
    //    }

    //    HashMap<Integer, StopWatchPP> stopwatches = new HashMap(32);

    //    protected ProfilingPoint[] createStopwatch(StopWatchPP stopwatch) {
    //        ProfilingPoint[] points=new ProfilingPoint[2];
    //        points[0] = new ProfilingPoint(ppid++, stopwatch.className, stopwatch.startLine, startStopWatch, "org.netbeans.lib.profiler.global.ProfilingPointServerHandler");
    //        points[1] = new ProfilingPoint(ppid++, stopwatch.className, -stopwatch.stopLine, stopStopWatch, "org.netbeans.lib.profiler.global.ProfilingPointServerHandler");
    //        stopwatches.put(ppid-2, stopwatch);
    //        stopwatches.put(ppid-1, stopwatch);
    //        
    //        return points;
    //    }
    //    /*
    //    protected void startPPTest(ProfilerEngineSettings settings, String crClasse,
    //            String crMethod, String crSignature, long idealTime, double diffPercent) {
    //        //create runner
    //     
    //        TargetAppRunner runner = new TargetAppRunner(settings, new TestProfilerAppHandler(this));
    //        runner.addProfilingEventListener(Utils.createProfilingListener(this));
    //        try {
    //            runner.readSavedCalibrationData();
    //            //create selection
    //            SourceCodeSelection select = new SourceCodeSelection(crClasse, crMethod, crSignature);
    //            runner.getProfilerClient().initiateCodeRegionInstrumentation(select);
    //     
    //            //measureCR(runner, idealTime, diffPercent);
    //        } catch (Exception ex) {
    //            log(ex);
    //            assertTrue("Exception thrown: "+ex.getMessage(), false);
    //        } finally {
    //            finalizeTest(runner);
    //        }
    //    }*/
    //    
    //    static int counts = 0;
    //    static int counte = 0;
    //    
    //    HashMap<StopWatchPP, HashMap<Integer, Long>> hits = new HashMap(32);
    //    
    //    class StartStopwatch implements ProfilingPoint.Executor {
    //        public void profilePointHit(HitEvent event) {
    //            System.out.println("start id "+counts++);
    //            //log("start "+event.getId()+"");
    //            StopWatchPP stopwatch = stopwatches.get(event.getId());
    //            HashMap<Integer, Long> map = hits.get(stopwatch);
    //            if (map == null) {
    //                map = new HashMap(32);
    //                hits.put(stopwatch, map);
    //            }
    //            map.put(event.getThreadId(), event.getTimestamp());
    //        }
    //    }
    //    
    //    class StopStopwatch implements ProfilingPoint.Executor {
    //        public void profilePointHit(HitEvent event) {
    //            System.out.println("end id "+counte++);
    //            StopWatchPP stopwatch = stopwatches.get(event.getId());
    //            HashMap<Integer, Long> map = hits.get(stopwatch);
    //            if (map != null && map.get(event.getThreadId()) != null) {
    //                long tms = map.remove(event.getThreadId());
    //                ArrayList<Long> res = results.get(stopwatch);
    //                if (res == null) {
    //                    res = new ArrayList(128);
    //                    results.put(stopwatch, res);
    //                }
    //                res.add((event.getTimestamp() - tms)/1000);
    //            }
    //        }
    //    }
    //    
    //    public static class StopWatchPP {
    //        String className;
    //        int startLine, stopLine, diffMillis;
    //        
    //        public StopWatchPP(String classname, int start, int end, int diff) {
    //            className = classname;
    //            startLine = start;
    //            stopLine = end;
    //            diffMillis = diff;
    //        }
    //        
    //        public ProfilingPoint[] getPPoints(ProfilingPointsTestCase test) {
    //            return test.createStopwatch(this);
    //        }
    //        
    //        
    //        protected void measureStopwatch(ArrayList<Long> results, ProfilingPointsTestCase test) throws Exception {
    //            ArrayList<Long> times = new ArrayList(results.size());
    //            times.addAll(results);
    //            Collections.sort(times);
    //            
    //            double all=0.0;
    //            StringBuilder sb=new StringBuilder();
    //            for (int i=0;i < times.size();i++) {
    //                all+=times.get(i);
    //                sb.append(StringUtils.mcsTimeToString(results.get(i)));
    //                sb.append(" ");
    //            }
    //            
    //            double median;
    //            if (times.size()%2 > 0)
    //                median=times.get(times.size()/2);
    //            else
    //                median=(times.get(times.size()/2)+times.get(times.size()/2-1))/2;
    //            int match=0;
    //            for (int i = 0; i < times.size(); i++) {
    //                if (Math.abs(times.get(i)-median)/1000 <= diffMillis) {
    //                    match++;
    //                }
    //            }
    //            double max=times.get(times.size()-1);
    //            double min=times.get(0);
    //            test.log("\nStopWatch "+this+"\n---------------------------------------------");
    //            test.log("Values:");
    //            test.log(sb);
    //            test.log("match="+match);
    //            test.log("Invocations: "+results.size());
    //            test.log("Total="+StringUtils.mcsTimeToString((long)all)+" ms  Median="+
    //                    StringUtils.mcsTimeToString((long)median)+" ms  Min="+
    //                    StringUtils.mcsTimeToString((long)min)+
    //                    " ms  Max="+StringUtils.mcsTimeToString((long)max)+" ms");
    //            test.ref(this+", "+results.size()+" pass(es)");
    //            if (match < times.size()*test.MAX_DIFF_PERC/100.0) {
    //                test.log("\n!!!Results don't match: "+test.complete(String.valueOf((times.size()-match)*100.0/times.size()), 6)+
    //                        " %  of values are different more than "+diffMillis+" ms than median value "+StringUtils.mcsTimeToString((long)median)+"\n");
    //                if (CHECK_ACCURACY)
    //                    assertTrue("Results are too different", false);
    //            }
    //        }
    //        
    //        public String toString() {
    //            return className+":"+startLine+"->"+stopLine;
    //        }
    //    }
}
