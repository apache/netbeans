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

import org.netbeans.lib.profiler.results.DataManager;

/**
 *
 * @author Jiri Sedlacek
 */
public class ClassHistoryDataManager extends DataManager {

    private int trackedClassID;
    private String trackedClassName;

    public long[] timeStamps; // Timeline
    public int[] nTotalAllocObjects; // Objects Allocated
    public long[] totalAllocObjectsSize; // Bytes Allocated
    public int[] nTrackedLiveObjects; // Live Objects
    public long[] trackedLiveObjectsSize; // Live Bytes


    private int arrayBufferSize;
    private int currentArraysSize;
    private int itemCount;


    public ClassHistoryDataManager() {
        this(50);
    }

    public ClassHistoryDataManager(int arrayBufferSize) {
        this.arrayBufferSize = arrayBufferSize;
        reset();
    }


    public synchronized void setArrayBufferSize(int arrayBufferSize) {
        this.arrayBufferSize = arrayBufferSize;
    }

    public synchronized int getArrayBufferSize() {
        return arrayBufferSize;
    }

    public synchronized int getItemCount() {
        return itemCount;
    }

    public synchronized int getTrackedClassID() {
        return trackedClassID;
    }

    public synchronized String getTrackedClassName() {
        return trackedClassName;
    }

    public synchronized boolean isTrackingClass() {
        return trackedClassName != null;
    }


    public synchronized void setupClass(int trackedClassID, String trackedClassName) {
        reset();
        
        this.trackedClassID = trackedClassID;
        this.trackedClassName = trackedClassName;
    }

    public synchronized void resetClass() {
        reset();
    }

    public synchronized void processData(int[] nTotalAllocObjects,
                                         long[] totalAllocObjectsSize) {

        checkArraysSize();

        timeStamps[itemCount] = System.currentTimeMillis();
        this.nTotalAllocObjects[itemCount] = nTotalAllocObjects[trackedClassID];
        this.totalAllocObjectsSize[itemCount] = totalAllocObjectsSize[trackedClassID];

        itemCount++;

        fireDataChanged();

    }

    public synchronized void processData(int[] nTotalAllocObjects,
                                         int[] nTrackedLiveObjects,
                                         long[] trackedLiveObjectsSize) {

        checkArraysSize();

        timeStamps[itemCount] = System.currentTimeMillis();
        this.nTotalAllocObjects[itemCount] = nTotalAllocObjects[trackedClassID];
        this.nTrackedLiveObjects[itemCount] = nTrackedLiveObjects[trackedClassID];
        this.trackedLiveObjectsSize[itemCount] = trackedLiveObjectsSize[trackedClassID];

        itemCount++;

        fireDataChanged();

    }


    private void reset() {
        itemCount = 0;

        trackedClassID = -1;
        trackedClassName = null;

        timeStamps = new long[arrayBufferSize];
        nTotalAllocObjects = new int[arrayBufferSize];
        totalAllocObjectsSize = new long[arrayBufferSize];
        nTrackedLiveObjects = new int[arrayBufferSize];
        trackedLiveObjectsSize = new long[arrayBufferSize];

        currentArraysSize = arrayBufferSize;

        fireDataReset();
    }
    
    private void checkArraysSize() {
        // array extension is needed
        if (currentArraysSize == itemCount) {
            timeStamps = extendArray(timeStamps, arrayBufferSize);
            nTotalAllocObjects = extendArray(nTotalAllocObjects, arrayBufferSize);
            totalAllocObjectsSize = extendArray(totalAllocObjectsSize, arrayBufferSize);
            nTrackedLiveObjects = extendArray(nTrackedLiveObjects, arrayBufferSize);
            trackedLiveObjectsSize = extendArray(trackedLiveObjectsSize, arrayBufferSize);

            // update current array size
            currentArraysSize += arrayBufferSize;
        }
    }

    // extends 1-dimensional long array
    private static long[] extendArray(long[] array, int extraLength) {
        int originalLength = array.length;
        long[] newArray = new long[originalLength + extraLength];
        System.arraycopy(array, 0, newArray, 0, originalLength);

        return newArray;
    }

    // extends 1-dimensional int array
    private static int[] extendArray(int[] array, int extraLength) {
        int originalLength = array.length;
        int[] newArray = new int[originalLength + extraLength];
        System.arraycopy(array, 0, newArray, 0, originalLength);

        return newArray;
    }

}
