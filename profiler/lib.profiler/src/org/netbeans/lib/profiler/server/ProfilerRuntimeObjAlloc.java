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

package org.netbeans.lib.profiler.server;


/**
 * This class contains instrumentation methods for object allocation profiling.
 *
 * @author Misha Dmitriev
 */
public class ProfilerRuntimeObjAlloc extends ProfilerRuntimeMemory {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    protected static boolean objAllocProfilingDisabled = true;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static void enableProfiling(boolean v) {
        if (!v) {
            objAllocProfilingDisabled = true;
        }

        if (v) {
            createNewDataStructures();
            ProfilerRuntimeMemory.enableProfiling(v);
        } else {
            ProfilerRuntimeMemory.enableProfiling(v);

            // Give the threads that are currently executing instrumentation enough time to finish
            // before we nullify the data structures that are used in instrumentation code.
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
            }

            clearDataStructures();
        }

        if (v) {
            objAllocProfilingDisabled = false;
        }
    }

    public static void traceObjAlloc(Object object, char classId) {
        if (objAllocProfilingDisabled) {
            return;
        }

        if (ThreadInfo.profilingSuspended()
            || ThreadInfo.isCurrentThreadProfilerServerThread()
            || (classId == 0 && isInternalClass(object.getClass()))) {
            // Avoid counting objects allocated by our own agent threads, or by this method's callees
            return;
        }

        ThreadInfo ti = ThreadInfo.getThreadInfo();

        if (ti.inProfilingRuntimeMethod > 0) {
            return;
        }

        if (!ti.isInitialized()) {
            ti.initialize();
            if (lockContentionMonitoringEnabled) writeThreadCreationEvent(ti);
        }

        ti.inProfilingRuntimeMethod++;

        int classInt;

        if (classId == 0) {
            //System.out.println("traceObjAlloc(Object object, 0) "+ object.getClass());
            classInt = getClassId(object.getClass());
            if (classInt == -1) {
                ti.inProfilingRuntimeMethod--;
                return;
            }
        } else {
            // See comment marked with (***) in ProfilerRuntimeCPUFullInstr
            classInt = classId&0xff;
            classInt |= classId&0xff00;
        }
        synchronized (allocatedInstancesCount) {
            allocatedInstancesCount[classInt]++;
        }

        if (allocatedInstThreshold[classInt] <= 0) {
            long objSize = getCachedObjectSize(classInt, object);
            getAndSendCurrentStackTrace(classInt, objSize);
            allocatedInstThreshold[classInt] = nextRandomizedInterval();
        }

        allocatedInstThreshold[classInt]--;
        ti.inProfilingRuntimeMethod--;
    }

    protected static void clearDataStructures() {
        ProfilerRuntimeMemory.clearDataStructures();
    }

    protected static void createNewDataStructures() {
        ProfilerRuntimeMemory.createNewDataStructures();
    }
}
