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

package org.netbeans.lib.profiler.heap;

import java.util.Iterator;


/**
 *
 * @author Tomas Hurka
 */
class ComputedSummary implements HeapSummary {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final long bytes;
    private final long instances;
    private final long time;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    ComputedSummary(HprofHeap heap) {
        long bytesCount = 0;
        long instancesCount = 0;
        Iterator classIt = heap.getAllClasses().iterator();

        while (classIt.hasNext()) {
            JavaClass jcls = (JavaClass) classIt.next();

            instancesCount += jcls.getInstancesCount();
            bytesCount += jcls.getAllInstancesSize();
        }
        bytes = bytesCount;
        instances = instancesCount;
        time = heap.dumpBuffer.getTime();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public long getTime() {
        return time;
    }

    public long getTotalAllocatedBytes() {
        return -1;
    }

    public long getTotalAllocatedInstances() {
        return -1;
    }

    public long getTotalLiveBytes() {
        return bytes;
    }

    public long getTotalLiveInstances() {
        return instances;
    }
}
