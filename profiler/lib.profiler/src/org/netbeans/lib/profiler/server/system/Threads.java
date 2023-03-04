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
 * Provides methods for accessing various information related to threads.
 *
 * @author  Misha Dmitriev
 */
public class Threads {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Returns all live Java threads in this JVM. If the number of threads fits into the threads array, it is reused
     * (unused elements are filled with nulls). Otherwise, or if threads is null, a new array is created.
     */
    public static native Thread[] getAllThreads(Thread[] threads);

    //----------------- Miscellaneous
    public static native String getJVMArguments();

    public static native String getJavaCommand();

    /** For each passed thread, stores its status as defined in CommonConstants, in the status array. threads may contain nulls. */
    public static native void getThreadsStatus(Thread[] threads, int[] status);

    /** Returns the total number of live Java threads. */
    public static native int getTotalNumberOfThreads();

    /** Should be called at earliest possible time */
    public static void initialize() {
        // Doesn't do anything in this version
    }

    /**
     * Records a given thread as a profiler's own thread, so that targetAppTreadsExist() does not treat it as a
     * target app thread. Note that the current implementation allows only one additional profiler thread; if this
     * is called more than once, only the latest thread is remembered.
     */
    public static native void recordAdditionalProfilerOwnThread(Thread specialThread);

    /**
     * Record profiler's own threads. If excludeSpecialThread is true, record all the Java threads currently existing
     * in this JVM, minus specialThread. Otherwise, record only the specialThread. Returns the number of recorded threads.
     */
    public static native int recordProfilerOwnThreads(boolean excludeSpecialThread, Thread specialThread);

    public static synchronized native void resumeTargetAppThreads(Thread excludedThread);

    public static synchronized native void suspendTargetAppThreads(Thread excludedThread);

    /**
     * Checks if any live target application threads still exist. A target application thread is any thread not recorded
     * previously by recordProfilerOwnThreads() or recordAdditionalProfilerOwnThread().
     */
    public static native boolean targetAppThreadsExist();

    public static void terminateTargetAppThreads() {
        terminateTargetAppThreads(new ThreadDeath());
    }

    public static native void terminateTargetAppThreads(Object exception);
}
