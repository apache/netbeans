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

package org.netbeans.lib.profiler.results.cpu.cct;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.results.cpu.TimingAdjusterOld;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.marking.MarkAwareNodeProcessorPlugin;


/**
 *
 * @author Jaroslav Bachorik
 */
public class TimeCollector extends MarkAwareNodeProcessorPlugin {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class TimingData {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        int inCalls;
        int outCalls;
        long netTime0;
        long netTime1;
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Map timing;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of MarkTimer
     */
    public TimeCollector() {
        this.timing = new HashMap();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public synchronized long getNetTime0(Mark mark) {
        if (isReset()) {
            return 0;
        }

        TimingData currentTiming = (TimingData) timing.get(mark);
        long time = (currentTiming != null)
                    ? (long) TimingAdjusterOld.getDefault()
                                              .adjustTime(currentTiming.netTime0, currentTiming.inCalls, currentTiming.outCalls,
                                                          false) : 0;

        return (time > 0) ? time : 0;
    }

    public synchronized long getNetTime1(Mark mark) {
        if (isReset()) {
            return 0;
        }

        TimingData currentTiming = (TimingData) timing.get(mark);
        long time = (currentTiming != null)
                    ? (long) TimingAdjusterOld.getDefault()
                                              .adjustTime(currentTiming.netTime1, currentTiming.inCalls, currentTiming.outCalls,
                                                          true) : 0;

        return (time > 0) ? time : 0;
    }

    @Override
    public void onStart() {
        super.onStart();
        timing.clear();
    }

    @Override
    public void onStop() {
        if (isReset()) {
            this.timing = new HashMap();
        }
        super.onStop();
    }

    @Override
    public void onNode(MethodCPUCCTNode node) {
        if (isReset()) {
            return;
        }

        Mark mark = getCurrentMark();
        Mark parentMark = getParentMark();

        if (mark != null) {
            TimingData data = (TimingData) timing.get(mark);

            if (data == null) {
                data = new TimingData();
                timing.put(mark, data);
            }

            data.inCalls += node.getNCalls();
            data.netTime0 += node.getNetTime0();
            data.netTime1 += node.getNetTime1();
        }

        if (parentMark != null) {
            TimingData parentData = (TimingData) timing.get(parentMark);

            if (parentData == null) {
                parentData = new TimingData();
                timing.put(parentMark, parentData);
            }

            parentData.outCalls += node.getNCalls();
        }
    }
}
