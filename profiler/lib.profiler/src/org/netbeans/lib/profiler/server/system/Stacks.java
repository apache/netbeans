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
 * Provides methods for accessing thread stacks contents.
 *
 * @author  Misha Dmitriev
 */
public class Stacks {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /** Returns the number of Java frames on the stack of the current thread */
    public static native int getCurrentJavaStackDepth(Thread thread);

    /**
     * stackDepth parameter is the maximum number of stack frames that can be sampled. Returns the actual number of
     * stack frames sampled.
     */
    public static native int getCurrentStackFrameIds(Thread thread, int stackDepth, int[] stackFrameIds);

    /**
     * For the given array of jmethodIds, returns the names of the respective methods as
     * (class name, method name and method signature) triplets.
     * All this symbolic information is returned as a single packed array of bytes (with each string in UTF8 format).
     * packedArrayOffsets is filled out with offsets of all of these strings.
     *
     * @param nMethods The number of methods, length of the methodIds array
     * @param methodIds An array of jMethodIds for which we need their names
     * @param packedArrayOffsets An array that, upon return from this method, will contain the indexes into the returned
     *        array
     * @return A packed array of bytes of triplets [class name, method name, method signature], packedArrayOffsets
     *         contains indexes into this array for individual items
     */
    public static native byte[] getMethodNamesForJMethodIds(int nMethods, int[] methodIds, int[] packedArrayOffsets);

    /**
     * Get information about the stacks of all live threads
     * @param threads used to return all threads
     * @param states used to return thread's states
     * @param frames used to return jMethodIds of frames of all threads
     */
    public static native void getAllStackTraces(Thread[][] threads, int[][] states, int[][][] frames);
    
    /** Clear the above stack frame buffer permanently. */
    public static native void clearNativeStackFrameBuffer();

    /**
     * Creates the internal, C-level stack frame buffer, used for intermediate storage of data obtained using
     * getCurrentStackFrameIds. Since just a single buffer is used, getCurrentStackFrameIds is obviously not
     * multithread-safe. The code that uses this stuff has to use a single lock - so far not a problem for memory
     * profiling where we use it, since normally it collects data for just every 10th object, thus the probability
     * of contention is not very high.
     */
    public static native void createNativeStackFrameBuffer(int sizeInFrames);

    /** Should be called at earliest possible time */
    public static void initialize() {
        // Doesn't do anything in this version
    }
}
