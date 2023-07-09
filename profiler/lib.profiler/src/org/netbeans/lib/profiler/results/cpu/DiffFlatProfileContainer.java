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
package org.netbeans.lib.profiler.results.cpu;

import java.util.*;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.utils.formatting.MethodNameFormatterFactory;

/**
 *
 * @author Jiri Sedlacek
 */
public class DiffFlatProfileContainer extends FlatProfileContainer {
    
    private final ClientUtils.SourceCodeSelection[] sels;
    private final double wholeGraphNetTime0;
    private final double wholeGraphNetTime1;
    private final long minTime;
    private final long maxTime;
    
    
    static DiffFlatProfileContainer create(FlatProfileContainer c1, FlatProfileContainer c2) {
        boolean collectingTwoTimeStamps = c1.collectingTwoTimeStamps && c2.collectingTwoTimeStamps;
        
        Set<ClientUtils.SourceCodeSelection> sels1 = new HashSet<>();
        int rows1 = c1.timeInMcs0.length;
        for (int i = 0; i < rows1; i++) sels1.add(c1.getSourceCodeSelectionAtRow(i));
        
        Map<ClientUtils.SourceCodeSelection, Integer> sels2 = new HashMap<>();
        int rows2 = c2.timeInMcs0.length;
        for (int i = 0; i < rows2; i++) sels2.put(c2.getSourceCodeSelectionAtRow(i), i);
        
        int pointer = 0;
        long[] timesInMcs0 = new long[rows1 + rows2];
        long[] timesInMcs1 = collectingTwoTimeStamps ? new long[rows1 + rows2] : null;
        long[] totalTimesInMcs0 = new long[rows1 + rows2];
        long[] totalTimesInMcs1 = collectingTwoTimeStamps ? new long[rows1 + rows2] : null;
        int[] nInvocations = new int[rows1 + rows2];
        ClientUtils.SourceCodeSelection[] sels = new ClientUtils.SourceCodeSelection[rows1 + rows2];
        int[] methodIDs = new int[rows1 + rows2];
        
        for (int i = 0; i < rows1; i++) {
            ClientUtils.SourceCodeSelection sel = c1.getSourceCodeSelectionAtRow(i);
            timesInMcs0[pointer] = -c1.timeInMcs0[i];
            totalTimesInMcs0[pointer] = -c1.totalTimeInMcs0[i];
            if (collectingTwoTimeStamps) {
                timesInMcs1[pointer] = -c1.timeInMcs1[i];
                totalTimesInMcs1[pointer] = -c1.totalTimeInMcs1[i];
            }
            nInvocations[pointer] = -c1.nInvocations[i];
            sels[pointer] = sel;
            methodIDs[pointer] = c1.methodIds[i];
            
            Integer i2 = sels2.get(sel);
            if (i2 != null) {
                timesInMcs0[pointer] += c2.timeInMcs0[i2];
                totalTimesInMcs0[pointer] += c2.totalTimeInMcs0[i2];
                if (collectingTwoTimeStamps) {
                    timesInMcs1[pointer] += c2.timeInMcs1[i2];
                    totalTimesInMcs1[pointer] += c2.totalTimeInMcs1[i2];
                }
                nInvocations[pointer] += c2.nInvocations[i2];
            }
            pointer++;
        }
        
        for (int i = 0; i < rows2; i++) {
            ClientUtils.SourceCodeSelection sel = c2.getSourceCodeSelectionAtRow(i);
            if (!sels1.contains(sel)) {
                timesInMcs0[pointer] = c2.timeInMcs0[i];
                totalTimesInMcs0[pointer] = c2.totalTimeInMcs0[i];
                if (collectingTwoTimeStamps) {
                    timesInMcs1[pointer] = c2.timeInMcs1[i];
                    totalTimesInMcs1[pointer] = c2.totalTimeInMcs1[i];
                }
                nInvocations[pointer] = c2.nInvocations[i];
                sels[pointer] = sel;
                methodIDs[pointer] = -c2.methodIds[i];
                pointer++;
            }
        }
        
        double wholeGraphNetTime0 = c2.getWholeGraphNetTime0() - c1.getWholeGraphNetTime0();
        double wholeGraphNetTime1 = c2.getWholeGraphNetTime1() - c1.getWholeGraphNetTime1();
        
        return new DiffFlatProfileContainer(collectingTwoTimeStamps, 
                Arrays.copyOf(timesInMcs0, pointer), collectingTwoTimeStamps ? Arrays.copyOf(timesInMcs1, pointer) : null,
                Arrays.copyOf(totalTimesInMcs0, pointer), collectingTwoTimeStamps ? Arrays.copyOf(totalTimesInMcs1, pointer) : null,
                Arrays.copyOf(nInvocations, pointer), Arrays.copyOf(sels, pointer),
                Arrays.copyOf(methodIDs, pointer), pointer, wholeGraphNetTime0, wholeGraphNetTime1);
    }
    
    private DiffFlatProfileContainer(boolean collectingTwoTimeStamps, long[] timeInMcs0, long[] timeInMcs1, 
             long[] totalTimeInMcs0, long[] totalTimeInMcs1, int[] nInvocations, ClientUtils.SourceCodeSelection[] sels, 
             int[] methodIDs, int nMethods, double wholeGraphNetTime0, double wholeGraphNetTime1) {
        super(timeInMcs0, timeInMcs1, totalTimeInMcs0, totalTimeInMcs1, nInvocations, null, nMethods);
        this.collectingTwoTimeStamps = collectingTwoTimeStamps;
        this.sels = sels;
        this.wholeGraphNetTime0 = wholeGraphNetTime0;
        this.wholeGraphNetTime1 = wholeGraphNetTime1;
        
        long minTimeX = Long.MAX_VALUE;
        long maxTimeX = Long.MIN_VALUE;
        nRows = nMethods;
        this.methodIds = methodIDs;
        for (int i = 0; i < nRows; i++) {
            minTimeX = Math.min(minTimeX, timeInMcs0[i]);
            maxTimeX = Math.max(maxTimeX, timeInMcs0[i]);
            nTotalInvocations += nInvocations[i];
        }
        
        if (minTimeX > 0 && maxTimeX > 0) minTimeX = 0;
        else if (minTimeX < 0 && maxTimeX < 0) maxTimeX = 0;
        minTime = minTimeX;
        maxTime = maxTimeX;
    }
    

    @Override
    public String getMethodNameAtRow(int row) {
        ClientUtils.SourceCodeSelection sel = getSourceCodeSelectionAtRow(row);
        return MethodNameFormatterFactory.getDefault().getFormatter()
                                         .formatMethodName(sel.getClassName(), sel.getMethodName(),
                                                           sel.getMethodSignature()).toFormatted();
    }
    
    @Override
    public ClientUtils.SourceCodeSelection getSourceCodeSelectionAtRow(int row) {
        return sels[row];
    }

    @Override
    public double getWholeGraphNetTime0() {
        return wholeGraphNetTime0;
    }

    @Override
    public double getWholeGraphNetTime1() {
        return wholeGraphNetTime1;
    }
    
    public long getMinTime() {
        return minTime;
    }
    
    public long getMaxTime() {
        return maxTime;
    }
    
    protected void swap(int a, int b) {
        ClientUtils.SourceCodeSelection sel = sels[a];
        sels[a] = sels[b];
        sels[b] = sel;
    }
    
}
