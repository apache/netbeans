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

import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.server.system.HeapDump;
import org.netbeans.lib.profiler.server.system.Timers;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * Performs special handling of Take HeapDump profiling points on server side.
 *
 * @author Tomas Hurka
 */
public class TakeHeapdumpProfilingPointHandler extends ProfilingPointServerHandler {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static Map instances;
    private static final String TAKEN_HEAPDUMP_PREFIX = "heapdump-"; // NOI18N
    private static final String HEAPDUMP_EXTENSION = "hprof"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final String heapdumpFilePrefix;
    private final boolean remoteProfiling;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private TakeHeapdumpProfilingPointHandler(String dir) {
        heapdumpFilePrefix = dir + File.separatorChar + TAKEN_HEAPDUMP_PREFIX;
        remoteProfiling = ProfilerServer.getProfilingSessionStatus().remoteProfiling;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static synchronized ProfilingPointServerHandler getInstance(String clientInfo) {
        TakeHeapdumpProfilingPointHandler instance;
        
        if (instances == null) {
            instances = new HashMap();
        }
        instance = (TakeHeapdumpProfilingPointHandler) instances.get(clientInfo);
        if (instance == null) {
            instance = new TakeHeapdumpProfilingPointHandler(clientInfo);
            instances.put(clientInfo, instance);
        }

        return instance;
    }

    public void profilingPointHit(int id) {
        int instrType = ProfilerInterface.getCurrentInstrType();
        boolean cpuProfiling = (instrType == CommonConstants.INSTR_RECURSIVE_FULL)
                               || (instrType == CommonConstants.INSTR_RECURSIVE_SAMPLED);

        if (cpuProfiling) { // CPU profiling
            ProfilerRuntimeCPU.suspendCurrentThreadTimer();
        }

        long absTimeStamp = Timers.getCurrentTimeInCounts();

        if (!remoteProfiling) { // take heap dump is supported only for local profiling

            String heapdumpName = getHeapDumpName(absTimeStamp);
            String error = HeapDump.takeHeapDump(heapdumpName);

            if (error != null) {
                System.err.println("Dump to " + heapdumpName + " failed with " + error); // NOI18N
            }
        }

        super.profilingPointHit(id, absTimeStamp);

        if (cpuProfiling) {
            ProfilerRuntimeCPU.resumeCurrentThreadTimer();
        }
    }

    private String getHeapDumpName(long time) {
        return heapdumpFilePrefix + (time & 0xFFFFFFFFFFFFFFL) + "." + HEAPDUMP_EXTENSION;
    }
}
