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

package org.netbeans.lib.profiler.results.monitor;

import org.netbeans.lib.profiler.client.MonitoredData;
import org.netbeans.lib.profiler.results.DataManager;


/**
 * A class that holds telemetry data about the target VM for a single profiling
 * session. It consumes/processes data obtained from the server via the
 * MonitoredData class. A listener is provided for those who want to be notified
 * about newly arrived data.
 *
 * @author Ian Formanek
 * @author Jiri Sedlacek
 */
public class VMTelemetryDataManager extends DataManager {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    public long[] freeMemory;
    public long[] lastGCPauseInMS;
    public long[] loadedClassesCount;
    public long[] processCPUTimeInPromile;
    public long[] nSurvivingGenerations;
    public long[] nSystemThreads;
    public long[] nTotalThreads;
    public long[] nUserThreads;
    public long[] relativeGCTimeInPerMil;
    public long[] timeStamps;
    public long[] totalMemory;
    public long[] usedMemory;

    public long[][] gcFinishs;
    public long[][] gcStarts;

    public long maxHeapSize = Long.MAX_VALUE; // value of Xmx, constant within one profiling session

    // --- Data storage ----------------------------------------------------------
    private MonitoredData lastData = null; // last data processed

    private boolean firstStart;
    private int lastUnpairedStart;

    // --- Arrays extending policy -----------------------------------------------
    private int arrayBufferSize;
    private int currentArraysSize;
    private int itemCount;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------

    /**
     * Creates a new instance of VMTelemetryDataManager
     */
    public VMTelemetryDataManager() {
        this(50);
    }

    /**
     * Creates a new instance of VMTelemetryDataManager
     */
    public VMTelemetryDataManager(int arrayBufferSize) {
        this.arrayBufferSize = arrayBufferSize;
        reset();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public synchronized void setArrayBufferSize(int arrayBufferSize) {
        this.arrayBufferSize = arrayBufferSize;
    }

    public synchronized int getArrayBufferSize() {
        return arrayBufferSize;
    }

    // --- Getters / setters -----------------------------------------------------
    public synchronized int getItemCount() {
        return itemCount;
    }

    public synchronized MonitoredData getLastData() {
        return lastData;
    }

    // --- Public runtime API ----------------------------------------------------
    public synchronized void processData(MonitoredData data) {
        long cpuTimeInPromile;
        long processCPUTime = data.getProcessCpuTime();
        
        if (processCPUTime != -1) {
            if (lastData != null) {
                long cpuDiffInMicroSec = (processCPUTime - lastData.getProcessCpuTime())/1000L;
                long timeDiffMicroSec = (data.getTimestamp() - lastData.getTimestamp())*1000L;
                if (timeDiffMicroSec < 1000) timeDiffMicroSec = 1000;
                cpuTimeInPromile = (1000*cpuDiffInMicroSec)/timeDiffMicroSec;
            } else {
               cpuTimeInPromile = 0; 
            }
        } else {
            cpuTimeInPromile = -1;
        }
        addValuesInternal(data.getTimestamp(), data.getFreeMemory(), data.getTotalMemory(), data.getNUserThreads(),
                          data.getNSystemThreads(), data.getNSurvivingGenerations(), data.getRelativeGCTimeInPerMil(),
                          data.getLastGCPauseInMS(), data.getLoadedClassesCount(), cpuTimeInPromile, data.getGCStarts(), data.getGCFinishs());
        lastData = data;
    }

    public synchronized void reset() {
        lastData = null;

        itemCount = 0;

        timeStamps = new long[arrayBufferSize];

        freeMemory = new long[arrayBufferSize];

        totalMemory = new long[arrayBufferSize];

        usedMemory = new long[arrayBufferSize];

        nUserThreads = new long[arrayBufferSize];

        nSystemThreads = new long[arrayBufferSize];

        nTotalThreads = new long[arrayBufferSize];

        nSurvivingGenerations = new long[arrayBufferSize];

        relativeGCTimeInPerMil = new long[arrayBufferSize];

        lastGCPauseInMS = new long[arrayBufferSize];

        loadedClassesCount = new long[arrayBufferSize];

        processCPUTimeInPromile = new long[arrayBufferSize];
        
        currentArraysSize = arrayBufferSize;
        
        gcStarts = new long[arrayBufferSize][];
        gcFinishs = new long[arrayBufferSize][];

        firstStart = true;
        lastUnpairedStart = -1;

        fireDataReset();
    }

    // --- Data storage management -----------------------------------------------
    private void addValuesInternal(long timeStamp, long freeMemory, long totalMemory, long nUserThreads, long nSystemThreads,
                                   long nSurvivingGenerations, long relativeGCTimeInPerMil, long lastGCPauseInMS,
                                   long loadedClassesCount, long cpuTimeInPromile, long[] gcStarts, 
                                   long[] gcFinishs) {
        checkArraysSize();

        this.timeStamps[itemCount] = timeStamp;
        this.freeMemory[itemCount] = freeMemory;
        this.totalMemory[itemCount] = totalMemory;
        this.usedMemory[itemCount] = totalMemory - freeMemory;
        this.nUserThreads[itemCount] = nUserThreads;
        this.nSystemThreads[itemCount] = nSystemThreads;
        this.nTotalThreads[itemCount] = nSystemThreads + nUserThreads;
        this.nSurvivingGenerations[itemCount] = nSurvivingGenerations;

        // TODO: should be one tenth (relativeGCTimeInPerMil / 10)
        this.relativeGCTimeInPerMil[itemCount] = relativeGCTimeInPerMil;
        this.lastGCPauseInMS[itemCount] = lastGCPauseInMS;
        this.loadedClassesCount[itemCount] = loadedClassesCount;
        this.processCPUTimeInPromile[itemCount] = cpuTimeInPromile;

        if (gcStarts.length > 0 || gcFinishs.length > 0) {

//            // Ensure the first event is gc start (filter-out leading gc end)
//            if (firstStart && gcStarts.length > 0) {
//                if (gcFinishs.length > 0 && gcStarts[0] > gcFinishs[0]) {
//                    long[] gcFinishs2 = new long[gcFinishs.length - 1];
//                    if (gcFinishs2.length > 0) System.arraycopy(gcFinishs, 1,
//                                                                gcFinishs2, 0,
//                                                                gcFinishs2.length);
//                    gcFinishs = gcFinishs2;
//                }
//                firstStart = false;
//            }

            // Ensure the first event is gc start (fake leading gc start)
            int starts = gcStarts.length;
            if (firstStart && (starts > 0 || gcFinishs.length > 0)) {
                if (starts == 0 || gcStarts[0] > gcFinishs[0]) {
                    long[] gcStarts2 = new long[starts + 1];
                    if (starts > 0) System.arraycopy(gcStarts, 0, gcStarts2, 1, starts);
                    gcStarts2[0] = timeStamps[0];
                    gcStarts = gcStarts2;
                }
                firstStart = false;
            }

            // Check if this item is paired
            boolean sameStartsFinishsCount = gcStarts.length == gcFinishs.length;
            boolean thisItemsPaired = (sameStartsFinishsCount && lastUnpairedStart == -1) ||
                                      (!sameStartsFinishsCount && lastUnpairedStart != -1);

            // Prepare extra buffer for unpaired items
            int extraItemsBuffer = thisItemsPaired ? 0 : 1;

            // Compute length of new data
            int newItemsLength = Math.max(gcStarts.length, gcFinishs.length) +
                                 extraItemsBuffer;

            // Add new gc starts
            if (gcStarts.length == newItemsLength) {
                this.gcStarts[itemCount] = gcStarts;
            } else {
                this.gcStarts[itemCount] = new long[newItemsLength];
                System.arraycopy(gcStarts, 0,
                                 this.gcStarts[itemCount], extraItemsBuffer,
                                 gcStarts.length);
            }

            // Add new gc finishs
            if (gcFinishs.length == newItemsLength) {
                this.gcFinishs[itemCount] = gcFinishs;
            } else {
                this.gcFinishs[itemCount] = new long[newItemsLength];
                System.arraycopy(gcFinishs, 0,
                                 this.gcFinishs[itemCount], 0,
                                 gcFinishs.length);
            }

            // Mark the unpaired finish
            if (!thisItemsPaired) {
                this.gcFinishs[itemCount][newItemsLength - 1] = -1;
            }

            // Fix the unpaired start
            if (lastUnpairedStart != -1) {
                long[] unpairedStarts = this.gcStarts[lastUnpairedStart];
                long[] unpairedFinishs = this.gcFinishs[lastUnpairedStart];
                unpairedFinishs[unpairedFinishs.length - 1] = this.gcFinishs[itemCount][0];
                this.gcStarts[itemCount][0] = unpairedStarts[unpairedStarts.length - 1];
            }

            // Update last unpaired start
            if (!thisItemsPaired) {
                lastUnpairedStart = itemCount;
            } else {
                lastUnpairedStart = -1;
            }

        } else {
            this.gcStarts[itemCount] = gcStarts;
            this.gcFinishs[itemCount] = gcFinishs;
        }

        itemCount++;

        fireDataChanged();
    }

    private void checkArraysSize() {
        // array extension is needed
        if (currentArraysSize == itemCount) {
            timeStamps = extendArray(timeStamps, arrayBufferSize);
            freeMemory = extendArray(freeMemory, arrayBufferSize);
            totalMemory = extendArray(totalMemory, arrayBufferSize);
            usedMemory = extendArray(usedMemory, arrayBufferSize);
            nUserThreads = extendArray(nUserThreads, arrayBufferSize);
            nSystemThreads = extendArray(nSystemThreads, arrayBufferSize);
            nTotalThreads = extendArray(nTotalThreads, arrayBufferSize);
            nSurvivingGenerations = extendArray(nSurvivingGenerations, arrayBufferSize);
            relativeGCTimeInPerMil = extendArray(relativeGCTimeInPerMil, arrayBufferSize);
            lastGCPauseInMS = extendArray(lastGCPauseInMS, arrayBufferSize);
            loadedClassesCount = extendArray(loadedClassesCount, arrayBufferSize);
            processCPUTimeInPromile = extendArray(processCPUTimeInPromile, arrayBufferSize);

            gcStarts = extendArray(gcStarts, arrayBufferSize);
            gcFinishs = extendArray(gcFinishs, arrayBufferSize);

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

    // extends 2-dimensional long array
    private static long[][] extendArray(long[][] array, int extraLength) {
        int originalLength = array.length;
        long[][] newArray = new long[originalLength + extraLength][];
        System.arraycopy(array, 0, newArray, 0, originalLength);

        return newArray;
    }
}
