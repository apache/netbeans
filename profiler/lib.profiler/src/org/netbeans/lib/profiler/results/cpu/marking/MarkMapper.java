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

package org.netbeans.lib.profiler.results.cpu.marking;

import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author Jaroslav Bachorik
 */
class MarkMapper implements MarkingEngine.StateObserver {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    // @GuardedBy marksGuard
    private final Map markMap = new HashMap();
    private final Object marksGuard = new Object();

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Mark getMark(int methodId, ProfilingSessionStatus status) {
        if (status == null) {
            return Mark.DEFAULT;
        }
        
        synchronized (marksGuard) {
            Mark mark = (Mark) markMap.get(Integer.valueOf(methodId));

            if (mark == null) {
                mark = MarkingEngine.getDefault().mark(methodId, status); // do mark the method
                markMap.put(Integer.valueOf(methodId), mark);
            }

            return mark;
        }
    }

    public void stateChanged(MarkingEngine instance) {
        reset();
    }

    private void reset() {
        synchronized (marksGuard) {
            markMap.clear();
        }
    }
}
