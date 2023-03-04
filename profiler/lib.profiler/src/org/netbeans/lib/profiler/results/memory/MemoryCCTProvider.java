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
import org.netbeans.lib.profiler.results.CCTProvider;


/**
 * @author Jaroslav Bachorik
 */
public interface MemoryCCTProvider extends CCTProvider {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public static interface Listener extends CCTProvider.Listener {
    }

    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    // *****************************************************

    /**
     * A container class, needed just for correct data transfer to its consumers.
     * @author Misha Dmitirev
     */
    public static class ObjectNumbersContainer {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        public float[] avgObjectAge;
        public int[] maxSurvGen;
        public long[] nTrackedAllocObjects;
        public int[] nTrackedLiveObjects;
        public long[] trackedLiveObjectsSize;
        public int nInstrClasses;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        ObjectNumbersContainer(long[] nTrackedAllocObjects, int[] nTrackedLiveObjects, long[] trackedLiveObjectsSize,
                               float[] avgObjectAge, int[] maxSurvGen, boolean[] unprofiledClass, int nProfiledClasses) {
            nInstrClasses = nProfiledClasses;

            int len = nProfiledClasses;
            this.nTrackedAllocObjects = new long[len];
            this.nTrackedLiveObjects = new int[len];
            this.trackedLiveObjectsSize = new long[len];
            this.avgObjectAge = avgObjectAge;
            this.maxSurvGen = maxSurvGen;

            if (nTrackedAllocObjects != null) {
                System.arraycopy(nTrackedAllocObjects, 0, this.nTrackedAllocObjects, 0, len);
            }

            if (nTrackedLiveObjects != null) {
                System.arraycopy(nTrackedLiveObjects, 0, this.nTrackedLiveObjects, 0, len);
            }

            if (trackedLiveObjectsSize != null) {
                System.arraycopy(trackedLiveObjectsSize, 0, this.trackedLiveObjectsSize, 0, len);
            }

            for (int i = 0; i < unprofiledClass.length; i++) {
                if (unprofiledClass[i]) {
                    this.nTrackedAllocObjects[i] = -1; // Special value
                    this.nTrackedLiveObjects[i] = 0;
                    this.trackedLiveObjectsSize[i] = 0;
                    this.avgObjectAge[i] = 0.0f;
                    this.maxSurvGen[i] = 0;
                }
            }
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    long[] getAllocObjectNumbers();

    int getCurrentEpoch();

    ObjectNumbersContainer getLivenessObjectNumbers();

    // following methods should be used only temporarily
    int getNProfiledClasses();

    long[] getObjectsSizePerClass();

    RuntimeMemoryCCTNode[] getStacksForClasses();

    void beginTrans(boolean mutable);

    boolean classMarkedUnprofiled(int classId);

    PresoObjAllocCCTNode createPresentationCCT(int classId, boolean dontShowZeroLiveObjAllocPaths)
                                        throws ClientUtils.TargetAppOrVMTerminated;

    void endTrans();

    /**
     * Marks the class with the given id as "unprofiled". Instrumentation for the class is not removed at this point.
     */
    void markClassUnprofiled(int classId);

    void updateInternals();
}
