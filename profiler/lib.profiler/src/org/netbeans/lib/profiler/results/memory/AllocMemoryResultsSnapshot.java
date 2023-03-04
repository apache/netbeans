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
import java.util.logging.Level;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;


/**
 * Results snapshot for Allocations Memory Profiling.
 *
 * @author Ian Formanek
 */
public class AllocMemoryResultsSnapshot extends MemoryResultsSnapshot {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String MEMORY_ALLOC_MSG = ResourceBundle.getBundle("org.netbeans.lib.profiler.results.memory.Bundle").getString("AllocMemoryResultsSnapshot_MemoryAllocMsg"); // NOI18N
                                                                                                                    // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int[] objectsCounts;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public AllocMemoryResultsSnapshot() {
    } // No-arg constructor needed for above serialization methods to work

    public AllocMemoryResultsSnapshot(long beginTime, long timeTaken, MemoryCCTProvider provider, ProfilerClient client)
                               throws ClientUtils.TargetAppOrVMTerminated {
        super(beginTime, timeTaken, provider, client);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int[] getObjectsCounts() {
        return objectsCounts;
    }

    public void performInit(ProfilerClient client, MemoryCCTProvider provider)
                     throws ClientUtils.TargetAppOrVMTerminated {
        super.performInit(client, provider);
        
        int[] cnts = client.getAllocatedObjectsCountResults();
        objectsCounts = new int[cnts.length];
        System.arraycopy(cnts, 0, objectsCounts, 0, cnts.length);

        if (LOGGER.isLoggable(Level.FINEST)) {
            debugValues();
        }
    }
    
    public AllocMemoryResultsSnapshot createDiff(MemoryResultsSnapshot snapshot) {
        if (!(snapshot instanceof AllocMemoryResultsSnapshot)) return null;
        return new AllocMemoryResultsDiff(this, (AllocMemoryResultsSnapshot)snapshot);
    }

    public void readFromStream(DataInputStream in) throws IOException {
        super.readFromStream(in);

        int len = in.readInt();
        objectsCounts = new int[len];

        for (int i = 0; i < len; i++) {
            objectsCounts[i] = in.readInt();
        }

        if (LOGGER.isLoggable(Level.FINEST)) {
            debugValues();
        }
    }

    public String toString() {
        return MessageFormat.format(MEMORY_ALLOC_MSG, new Object[] { super.toString() });
    }

    //---- Serialization support
    public void writeToStream(DataOutputStream out) throws IOException {
        super.writeToStream(out);

        out.writeInt(objectsCounts.length);

        for (int i = 0; i < objectsCounts.length; i++) {
            out.writeInt(objectsCounts[i]);
        }
    }

    protected PresoObjAllocCCTNode createPresentationCCT(RuntimeMemoryCCTNode rootNode, int classId,
                                                         boolean dontShowZeroLiveObjAllocPaths) {
        return PresoObjAllocCCTNode.createPresentationCCTFromSnapshot(getJMethodIdTable(), rootNode, getClassName(classId));
    }

    void debugValues() {
        super.debugValues();
        LOGGER.finest("objectsCounts.length: " + debugLength(objectsCounts)); // NOI18N
    }
}
