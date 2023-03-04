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

import org.netbeans.lib.profiler.client.ClientUtils;


/**
 * Management of the reverse call graph representation for object allocations call paths.
 *
 * @author Misha Dmitriev
 */
public class MemoryCCTManager {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private PresoObjAllocCCTNode rootNode;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    //  public MemoryCCTManager(MemoryCCTProvider provider, int classId, boolean dontShowZeroLiveObjAllocPaths)
    //      throws ClientUtils.TargetAppOrVMTerminated {
    //    rootNode = provider.createPresentationCCT(classId, dontShowZeroLiveObjAllocPaths);
    //  }
    public MemoryCCTManager(MemoryResultsSnapshot snapshot, int classId, boolean dontShowZeroLiveObjAllocPaths) {
        rootNode = snapshot.createPresentationCCT(classId, dontShowZeroLiveObjAllocPaths);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * @return true if the allocation stack traces are empty (and the getRootNode returns null), false otherwise
     * @see #getRootNode()
     */
    public boolean isEmpty() {
        return rootNode == null;
    }

    /**
     * @return The root noe of allocation stack traces or null if empty.
     * @see #isEmpty()
     */
    public PresoObjAllocCCTNode getRootNode() {
        return rootNode;
    }
}
