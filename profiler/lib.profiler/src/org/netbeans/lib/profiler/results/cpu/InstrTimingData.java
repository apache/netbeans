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

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class is simply a container for instrumentation timing data that needs to be passed around between
 * a number of different objects when processing CPU profiling results.
 *
 * @author Misha Dmitriev
 */
public class InstrTimingData implements Cloneable {
    public static final InstrTimingData DEFAULT = new InstrTimingData();

    // Of these variables, the xxx0 ones are used when either only absolute or only thread CPU timer is used.
    // xxx0 and xxx1 together are used only when both timers are used.
    double methodEntryExitCallTime0 = 0;
    double methodEntryExitCallTime1 = 0;
    double methodEntryExitInnerTime0 = 0;
    double methodEntryExitInnerTime1 = 0;
    double methodEntryExitOuterTime0 = 0;
    double methodEntryExitOuterTime1 = 0;
    long timerCountsInSecond0 = 1000; // default is a millisecond timer granularity; will get replaced from the calibration data
    long timerCountsInSecond1 = 1000; // default is a millisecond timer granularity; will get replaced from the calibration data

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            Logger.getLogger(InstrTimingData.class.getName()).log(Level.SEVERE, "Unable to clone " + InstrTimingData.class.getName(), e);
            return null;
        }
    }

    public InstrTimingData() {}

    //~ Methods ------------------------------------------------------------------------------------------------------------------



    public String toString() {
        String s1 = "callTime0 = " + methodEntryExitCallTime0 + ", innerTime0 = " + methodEntryExitInnerTime0
                    + ", outerTime0 = " // NOI18N
                    + methodEntryExitOuterTime0 + "\n" // NOI18N
                    + "callTime1 = " + methodEntryExitCallTime1 + ", innerTime1 = " + methodEntryExitInnerTime1
                    + ", outerTime1 = " // NOI18N
                    + methodEntryExitOuterTime1 + "\n" // NOI18N
                    + "countsInSec0 = " + timerCountsInSecond0 + ", countsInSec1 = " + timerCountsInSecond1 + "\n"; // NOI18N

        return s1;
    }
}
