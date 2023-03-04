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

/**
 *
 * @author Tomas Hurka
 */
final class Summary implements HeapSummary {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    final int totalLiveBytes;
    final int totalLiveInstances;
    final long time;
    final long totalAllocatedBytes;
    final long totalAllocatedInstances;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    Summary(HprofByteBuffer dumpBuffer, long startOffset) {
        assert dumpBuffer.get(startOffset) == HprofHeap.HEAP_SUMMARY;
        dumpBuffer.getInt(startOffset + 1); // time
        dumpBuffer.getInt(startOffset + 1 + 4); // tag length
        totalLiveBytes = dumpBuffer.getInt(startOffset + 1 + 4 + 4);
        totalLiveInstances = dumpBuffer.getInt(startOffset + 1 + 4 + 4 + 4);
        totalAllocatedBytes = dumpBuffer.getLong(startOffset + 1 + 4 + 4 + 4 + 4);
        totalAllocatedInstances = dumpBuffer.getLong(startOffset + 1 + 4 + 4 + 4 + 4 + 8);
        time = dumpBuffer.getTime();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public long getTime() {
        return time;
    }

    public long getTotalAllocatedBytes() {
        return totalAllocatedBytes;
    }

    public long getTotalAllocatedInstances() {
        return totalAllocatedInstances;
    }

    public long getTotalLiveBytes() {
        return totalLiveBytes;
    }

    public long getTotalLiveInstances() {
        return totalLiveInstances;
    }
}
