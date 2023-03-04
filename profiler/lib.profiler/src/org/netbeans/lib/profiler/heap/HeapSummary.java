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
 * This is optional summary information. It contains summary heap data and
 * time of the heap dump.
 * @author Tomas Hurka
 */
public interface HeapSummary {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * the time when the memory heap dump occurred.
     * @return the time when the memory heap dump occurred in milliseconds since 0:00 GMT 1/1/1970
     */
    long getTime();

    /**
     * number of total bytes allocated on the heap during the run of JVM.
     * Returned only if this summary information is available in heap dump.
     * @return number of total allocated bytes on the heap during the run of JVM
     * or -1 if the information is not available in the heap dump.
     */
    long getTotalAllocatedBytes();

    /**
     * number of all instances allocated on the heap during the run of JVM.
     * Returned only if this summary information is available in heap dump.
     * @return number of instances allocated on the heap during the run of JVM
     * or -1 if the information is not available in the heap dump.
     */
    long getTotalAllocatedInstances();

    /**
     * number of total bytes allocated on the heap at the time of the heap dump.
     * If this summary information is not available in heap dump, it is computed
     * from the dump.
     * @return number of total allocated bytes in the heap
     */
    long getTotalLiveBytes();

    /**
     * total number of instances allocated on the heap at the time of the heap dump.
     * If this summary information is not available in heap dump, it is computed
     * from the dump.
     * @return number of total live instances in the heap
     */
    long getTotalLiveInstances();
}
