/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class representing a difference between two snapshots, diffing only values
 * available in SampledMemoryResultsSnapshot
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
public class SampledMemoryResultsDiff extends SampledMemoryResultsSnapshot {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int[] objectsCounts;
    private int nClasses;
    private long maxObjectsSizePerClassDiff;
    private long minObjectsSizePerClassDiff;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public SampledMemoryResultsDiff(SampledMemoryResultsSnapshot snapshot1, SampledMemoryResultsSnapshot snapshot2) {
        computeDiff(snapshot1, snapshot2);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    public long getBeginTime() {
        return -1;
    }

    public String getClassName(int classId) {
        return null;
    }

    public String[] getClassNames() {
        return classNames;
    }

    public JMethodIdTable getJMethodIdTable() {
        return null;
    }

    public long getMaxObjectsSizePerClassDiff() {
        return maxObjectsSizePerClassDiff;
    }

    public long getMinObjectsSizePerClassDiff() {
        return minObjectsSizePerClassDiff;
    }

    public int getNProfiledClasses() {
        return nClasses;
    }

    public int[] getObjectsCounts() {
        return objectsCounts;
    }

    public long[] getObjectsSizePerClass() {
        return objectsSizePerClass;
    }

    public long getTimeTaken() {
        return -1;
    }

    public boolean containsStacks() {
        return false;
    }

    public PresoObjAllocCCTNode createPresentationCCT(int classId, boolean dontShowZeroLiveObjAllocPaths) {
        return null;
    }

    public void readFromStream(DataInputStream in) throws IOException {
        throw new UnsupportedOperationException("Persistence not supported for snapshot comparison"); // NOI18N
    }

    //---- Serialization support
    public void writeToStream(DataOutputStream out) throws IOException {
        throw new UnsupportedOperationException("Persistence not supported for snapshot comparison"); // NOI18N
    }

    protected PresoObjAllocCCTNode createPresentationCCT(RuntimeMemoryCCTNode rootNode, int classId,
            boolean dontShowZeroLiveObjAllocPaths) {
        return null;
    }

    private void computeDiff(SampledMemoryResultsSnapshot snapshot1, SampledMemoryResultsSnapshot snapshot2) {
        // must detect the minimum, same approach as in SnapshotAllocResultsPanel.fetchResultsFromSnapshot()
        int s1nClasses = Math.min(snapshot1.getNProfiledClasses(), snapshot1.getObjectsCounts().length);
        s1nClasses = Math.min(s1nClasses, snapshot1.getObjectsSizePerClass().length);

        int s2nClasses = Math.min(snapshot2.getNProfiledClasses(), snapshot2.getObjectsCounts().length);
        s2nClasses = Math.min(s2nClasses, snapshot2.getObjectsSizePerClass().length);

        // temporary cache for creating diff
        HashMap<String, Integer> classNamesIdxMap = new HashMap<>(s1nClasses);
        ArrayList<Integer> objCountsArr = new ArrayList<>(s1nClasses);
        ArrayList<Long> objSizesArr = new ArrayList<>(s1nClasses);

        // fill the cache with negative values from snapshot1
        String[] s1ClassNames = snapshot1.getClassNames();
        int[] s1ObjectsCount = snapshot1.getObjectsCounts();
        long[] s1ObjectsSizes = snapshot1.getObjectsSizePerClass();

        for (int i = 0; i < s1nClasses; i++) {
            Integer classIdx = classNamesIdxMap.get(s1ClassNames[i]);

            if (classIdx != null) { // duplicate classname - add objCountsArr and objSizesArr to original classname
                objCountsArr.set(classIdx, objCountsArr.get(classIdx) - s1ObjectsCount[i]);
                objSizesArr.set(classIdx, objSizesArr.get(classIdx) - s1ObjectsSizes[i]);
            } else {
                classNamesIdxMap.put(s1ClassNames[i], objCountsArr.size());
                objCountsArr.add(-s1ObjectsCount[i]);
                objSizesArr.add(-s1ObjectsSizes[i]);
            }
        }

        // create diff using values from snapshot2
        String[] s2ClassNames = snapshot2.getClassNames();
        int[] s2ObjectsCount = snapshot2.getObjectsCounts();
        long[] s2ObjectsSizes = snapshot2.getObjectsSizePerClass();

        for (int i = 0; i < s2nClasses; i++) {
            int objectsCount = s2ObjectsCount[i];
            Integer classIdx = classNamesIdxMap.get(s2ClassNames[i]);

            if (classIdx != null) {
                // class already present in snapshot1
                if (objectsCount != 0 || objCountsArr.get(classIdx) != 0) { // Do not add classes not displayed in compared snapshots (zero instances number)
                    objCountsArr.set(classIdx, objCountsArr.get(classIdx) + objectsCount);
                    objSizesArr.set(classIdx, objSizesArr.get(classIdx) + s2ObjectsSizes[i]);
                } else {
                    classNamesIdxMap.remove(s2ClassNames[i]); // Remove classname that should not be displayed
                }
            } else {
                // class not present in snapshot1
                if (objectsCount != 0) { // Do not add classes not displayed in compared snapshots (zero instances number)
                    classNamesIdxMap.put(s2ClassNames[i], objCountsArr.size());
                    objCountsArr.add(objectsCount);
                    objSizesArr.add(s2ObjectsSizes[i]);
                }
            }
        }

        // move the diff to instance variables
        nClasses = classNamesIdxMap.size();
        classNames = new String[nClasses];
        objectsCounts = new int[nClasses];
        objectsSizePerClass = new long[nClasses];
        minObjectsSizePerClassDiff = Long.MAX_VALUE;
        maxObjectsSizePerClassDiff = Long.MIN_VALUE;

        Iterator<Map.Entry<String, Integer>> classNamesIter = classNamesIdxMap.entrySet().iterator();
        int index = 0;

        while (classNamesIter.hasNext()) {
            Map.Entry<String, Integer> entry = classNamesIter.next();
            int classIndex =  entry.getValue();

            classNames[index] = entry.getKey();
            objectsCounts[index] = objCountsArr.get(classIndex);
            objectsSizePerClass[index] = objSizesArr.get(classIndex);

            minObjectsSizePerClassDiff = Math.min(minObjectsSizePerClassDiff, objectsSizePerClass[index]);
            maxObjectsSizePerClassDiff = Math.max(maxObjectsSizePerClassDiff, objectsSizePerClass[index]);

            index++;
        }

        if ((minObjectsSizePerClassDiff > 0) && (maxObjectsSizePerClassDiff > 0)) {
            minObjectsSizePerClassDiff = 0;
        } else if ((minObjectsSizePerClassDiff < 0) && (maxObjectsSizePerClassDiff < 0)) {
            maxObjectsSizePerClassDiff = 0;
        }
    }
}
