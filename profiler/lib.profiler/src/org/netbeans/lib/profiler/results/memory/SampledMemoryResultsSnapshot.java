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
package org.netbeans.lib.profiler.results.memory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;


/**
 * Results snapshot for Sampled Memory Profiling.
 *
 * @author Ian Formanek
 * @author Tomas Hurka
 */
public class SampledMemoryResultsSnapshot extends MemoryResultsSnapshot {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String MEMORY_SAMPLED_MSG = ResourceBundle.getBundle("org.netbeans.lib.profiler.results.memory.Bundle").getString("SampledMemoryResultsSnapshot_MemorySamledMsg"); // NOI18N
                                                                                                                    // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int[] liveObjectsCounts;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SampledMemoryResultsSnapshot() {
    } // No-arg constructor needed for above serialization methods to work

    public SampledMemoryResultsSnapshot(long beginTime, long timeTaken, ProfilerClient client)
                               throws ClientUtils.TargetAppOrVMTerminated {
        super(beginTime, timeTaken, null, client);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int[] getObjectsCounts() {
        return liveObjectsCounts;
    }

    public void performInit(ProfilerClient client, MemoryCCTProvider provider)
                     throws ClientUtils.TargetAppOrVMTerminated {
        HeapHistogram histo = client.getHeapHistogram();
        Set<HeapHistogram.ClassInfo> info = histo.getHeapHistogram();
        
        nProfiledClasses = info.size();
        classNames = new String[nProfiledClasses];
        objectsSizePerClass = new long[nProfiledClasses];
        liveObjectsCounts = new int[nProfiledClasses];
        int i = 0;
        for (HeapHistogram.ClassInfo ci : info) {
            classNames[i] = ci.getName();
            objectsSizePerClass[i] = ci.getBytes();
            liveObjectsCounts[i] = (int)ci.getInstancesCount();
            i++;
        }
    }
    
    public SampledMemoryResultsSnapshot createDiff(MemoryResultsSnapshot snapshot) {
        if (!(snapshot instanceof SampledMemoryResultsSnapshot)) return null;
        return new SampledMemoryResultsDiff(this, (SampledMemoryResultsSnapshot)snapshot);
    }

    public void readFromStream(DataInputStream in) throws IOException {
        super.readFromStream(in);

        int len = in.readInt();
        liveObjectsCounts = new int[len];

        for (int i = 0; i < len; i++) {
            liveObjectsCounts[i] = in.readInt();
        }

        if (LOGGER.isLoggable(Level.FINEST)) {
            debugValues();
        }
    }

    public String toString() {
        return MessageFormat.format(MEMORY_SAMPLED_MSG, new Object[] { super.toString() });
    }

    //---- Serialization support
    public void writeToStream(DataOutputStream out) throws IOException {
        super.writeToStream(out);

        out.writeInt(liveObjectsCounts.length);

        for (int i = 0; i < liveObjectsCounts.length; i++) {
            out.writeInt(liveObjectsCounts[i]);
        }
    }

    protected PresoObjAllocCCTNode createPresentationCCT(RuntimeMemoryCCTNode rootNode, int classId,
                                                         boolean dontShowZeroLiveObjAllocPaths) {
        return PresoObjAllocCCTNode.createPresentationCCTFromSnapshot(getJMethodIdTable(), rootNode, getClassName(classId));
    }

    void debugValues() {
        super.debugValues();
        LOGGER.finest("objectsCounts.length: " + debugLength(liveObjectsCounts)); // NOI18N
    }
}
