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

package org.netbeans.lib.profiler.server.system;

/**
 * Provides methods for obtaining various high-resolution system times. A
 * version for CVM
 *
 * @author Misha Dmitriev
 */
public class Timers {

    /**
     * Should be called at earliest possible time
     */
    public static void initialize() {
        getThreadCPUTimeInNanos();
    }

  
    /**
     * "counts" instead of nanoseconds in this method are for compatibility with
     * the previous versions of JFluid, that call a native method for system
     * timer, which, in turn, returns the result in sub-microsecond "counts" on
     * Windows.
     */
    public static native long getCurrentTimeInCounts();

    public static long getNoOfCountsInSecond() {
        return 1000000000;
    }

  
    public static native long getThreadCPUTimeInNanos();

    /**
     * Returns the approximate accumulated process CPU elapsed time in
     * nanoseconds. Note that the time is normalized to one processor.
     * This method returns <tt>-1</tt> if the collection elapsed
     * time is undefined for this collector.
     *
     * @return the approximate accumulated process CPU elapsed time in
     * nanoseconds.
     */
    public static long getProcessCpuTime() {
        return -1;
    }

    /**
     * WORKS ONLY ON UNIX, calls nanosleep(). On Solaris, this is more precise
     * than the built-in Thread.sleep() call implementation that, at least in
     * JDK 1.4.2, goes to select(3C). On Linux, it should be more precise, but
     * it turns out that nanosleep() in this OS, at least in version 7.3 that I
     * tested, has a resolution of at least 20ms. This seems to be a known
     * issue; hopefully they fix it in future.
     */
    public static native void osSleep(int ns);

  
    /**
     * This is relevant only on Solaris. By default, the resolution of the
     * thread local CPU timer is 10 ms. If we enable micro state accounting, it
     * enables significantly (but possibly at a price of some overhead). So I
     * turn it on only when thread CPU timestamps are really collected.
     */
    public static native void enableMicrostateAccounting(boolean v);
}
