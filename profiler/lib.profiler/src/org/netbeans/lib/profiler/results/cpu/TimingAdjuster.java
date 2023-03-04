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

package org.netbeans.lib.profiler.results.cpu;


//import org.netbeans.lib.profiler.global.Platform;
//import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
//import org.netbeans.lib.profiler.utils.StringUtils;

/**
 *
 * @author Jaroslav Bachorik
 */
public class TimingAdjuster {
    //  // @GuardedBy this
    //  private InstrTimingData timingData;
    //  
    //  public TimingAdjuster(final ProfilingSessionStatus status, final boolean collectingTwoTimeStamps) {
    //    calculateTimingData(status, collectingTwoTimeStamps);
    //  }
    //  
    //  final public synchronized TimeStamp adjustTime(TimeStamp time, int incommingInv, int outgoingInv) {
    //    TimeStamp result = null;
    //    
    //    long timeExCleansed = timingData.timerCountsInSecond1 == 0 ? 0 : (long)((double) time.getExtendedTime() - incommingInv * timingData.methodEntryExitInnerTime1 - outgoingInv * timingData.methodEntryExitOuterTime1) * 1000000 / timingData.timerCountsInSecond1;
    //    long timeCleansed = timingData.timerCountsInSecond0 == 0 ? 0 : (long)((double) time.getTime() - incommingInv * timingData.methodEntryExitInnerTime0 - outgoingInv * timingData.methodEntryExitOuterTime0) * 1000000 / timingData.timerCountsInSecond0;
    //
    //    timeCleansed = timeCleansed < 0 ? 0 : timeCleansed;
    //    timeExCleansed = timeExCleansed < 0 ? 0 : timeExCleansed;
    //    
    //    result = TimeStamp.createTimeStamp(timeCleansed, timeExCleansed);
    //    
    //    return result;
    //  }
    //  
    //  final public synchronized double delta(int incommingInv, int outgoingInv, boolean secondTimestamp) {
    //    double adjusted = 0;
    //    if (secondTimestamp) {
    //      adjusted = ((double) incommingInv * timingData.methodEntryExitInnerTime1 - outgoingInv * timingData.methodEntryExitOuterTime1) * 1000000 / timingData.timerCountsInSecond1;
    //    } else {
    //      adjusted = ((double) incommingInv * timingData.methodEntryExitInnerTime0 - outgoingInv * timingData.methodEntryExitOuterTime0) * 1000000 / timingData.timerCountsInSecond0;
    //    }
    //    
    //    if (adjusted < 0) adjusted = 0;
    //    return adjusted;
    //  }
    //  
    //  final public synchronized boolean isAdjustible(long time, int incommingInv, int outgoingInv, boolean secondTimestamp) {
    //    if (secondTimestamp) {
    //      return time > (incommingInv * timingData.methodEntryExitInnerTime1 + outgoingInv * timingData.methodEntryExitOuterTime1);
    //    } else {
    //      return time > (incommingInv * timingData.methodEntryExitInnerTime0 + outgoingInv * timingData.methodEntryExitOuterTime0);
    //    }
    //  }
    //  
    //  public synchronized void calculateTimingData(final ProfilingSessionStatus status, final boolean collectingTwoTimeStamps) {
    //    timingData = new InstrTimingData();
    //    // We use the following "safety margins" to artificially decrease the time spent in instrumentation.
    //    // That's because it looks like due to various CPU level speed optimizations the execution time for some code with
    //    // added instrumentation may be pretty much equal to the execution time of the instrumentation itself. The profiled
    //    // code, esp. if it's just one or two lines, is effectively executed in parallel with instrumentation.
    //    // Furthermore, highly optimizing CPU plus possibly smth. like rounding errors may occasionally result in the
    //    // measured time that is even *smaller* than the execution time of the instrumentation. This will result in
    //    // *negative* cleansed times presented to the user.
    //    // This safety margin is rough, but probably most reliable way to deal with these issues. The value has been
    //    // chosen experimentally, and seems to result in reasonable compromise between data accuracy and no occurrences
    //    // of zero time results.
    //    double safetyMargin0 = 0.88;
    //    double safetyMargin1 = 0.88;
    //    double safetyMargin2 = 0.88;
    //    if (collectingTwoTimeStamps) {
    //      // Note that all methodEntryExitXXX times are measured in status in absolute timer counts.
    //      // For usage in factor-out operations, we need to convert those that are used with thread CPU time values
    //      // into thread CPU time units.
    //      timingData.methodEntryExitCallTime0 = status.methodEntryExitCallTime[2] * safetyMargin2;
    //      timingData.methodEntryExitCallTime1 =
    //        status.methodEntryExitCallTime[3] * status.timerCountsInSecond[1] / status.timerCountsInSecond[0]
    //        * safetyMargin2;
    //      timingData.methodEntryExitInnerTime0 = status.methodEntryExitInnerTime[2] * safetyMargin2;
    //      timingData.methodEntryExitOuterTime0 = status.methodEntryExitOuterTime[2] * safetyMargin2;
    //      timingData.methodEntryExitInnerTime1 =
    //        status.methodEntryExitInnerTime[3] * status.timerCountsInSecond[1] / status.timerCountsInSecond[0]
    //        * safetyMargin2;
    //      timingData.methodEntryExitOuterTime1 =
    //        status.methodEntryExitOuterTime[3] * status.timerCountsInSecond[1] / status.timerCountsInSecond[0]
    //        * safetyMargin2;
    //      timingData.timerCountsInSecond0 = status.timerCountsInSecond[0];
    //      timingData.timerCountsInSecond1 = status.timerCountsInSecond[1];
    //    } else {
    //      if (status.absoluteTimerOn) {
    //        timingData.methodEntryExitCallTime0 = status.methodEntryExitCallTime[0] * safetyMargin0;
    //        timingData.methodEntryExitInnerTime0 = status.methodEntryExitInnerTime[0] * safetyMargin0;
    //        timingData.methodEntryExitOuterTime0 = status.methodEntryExitOuterTime[0] * safetyMargin0;
    //        timingData.timerCountsInSecond0 = status.timerCountsInSecond[0];
    //      } else {
    //        // Thread CPU only timer used. On Windows and Linux, with extremely low (10 ms or even worse) resolution of
    //        // this timer, it doesn't make sense to use cleansing - it introduces negative figures for short-running
    //        // methods, for which I didn't find any graceful way to deal with.
    //        // Also, cleansing is needed to make results sensible for short-running methods - but here for such methods
    //        // they are already non-sensible, with 0 as a most typical result.
    //        String targetMachineOSName = status.targetMachineOSName;
    //        if (targetMachineOSName == null) return; // dont calculate timings for an undefined OS
    //        if (Platform.isWindows(targetMachineOSName) || Platform.isLinux(targetMachineOSName)) {
    //          safetyMargin1 = 0;
    //        }
    //        timingData.methodEntryExitCallTime0 =
    //          status.methodEntryExitCallTime[1] * status.timerCountsInSecond[1] / status.timerCountsInSecond[0]
    //          * safetyMargin1;
    //        timingData.methodEntryExitInnerTime0 =
    //          status.methodEntryExitInnerTime[1] * status.timerCountsInSecond[1] / status.timerCountsInSecond[0]
    //          * safetyMargin1;
    //        timingData.methodEntryExitOuterTime0 =
    //          status.methodEntryExitOuterTime[1] * status.timerCountsInSecond[1] / status.timerCountsInSecond[0]
    //          * safetyMargin1;
    //        timingData.timerCountsInSecond0 = status.timerCountsInSecond[1];
    //      }
    //    }
    //    System.out.println(timingData);
    //  }
}
