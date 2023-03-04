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

package org.netbeans.lib.profiler.results.cpu;

import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.utils.formatting.MethodNameFormatterFactory;


/**
 * Container for CPU profiling results in the flat profile form. A concrete subclass of FlatProfileContainer,
 * where the data is backed by CPUCCTContainer.
 *
 * @author Misha Dmitriev
 */
public class FlatProfileContainerBacked extends FlatProfileContainer {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected CPUCCTContainer cctContainer;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * The data passed to this constructor may contain some zero-invocation rows. That's because the size of passed arrays
     * is equal to the number of currently instrumented methods, but in general not all of the methods may be invoked even
     * once at an arbitrary moment.
     *
     * @param cctContainer The CPU results
     * @param timeInMcs0   Array of Absolute timer values (self-time) for each method - always used
     * @param timeInMcs1   Array of CPU timer values (self-time) for each method - optional, may be null
     * @param totalTimeInMcs0   Array of Absolute timer (total time) values for each method - always used
     * @param totalTimeInMcs1   Array of CPU timer (total time) values for each method - optional, may be null
     * @param nInvocations Array of number of invocations for each method
     * @param nMethods     Total number of profiled methods - length of the provided arrays
     */
    public FlatProfileContainerBacked(CPUCCTContainer cctContainer, long[] timeInMcs0, long[] timeInMcs1, 
            long[] totalTimeInMcs0, long[] totalTimeInMcs1, int[] nInvocations, int nMethods) {
        super(timeInMcs0, timeInMcs1, totalTimeInMcs0, totalTimeInMcs1, nInvocations, null, nMethods);
        this.cctContainer = cctContainer;

        collectingTwoTimeStamps = cctContainer.isCollectingTwoTimeStamps();

        // Now get rid of zero-invocation entries once and forever. Also set nTotalInvocations and set negative times
        // (that may be possible due to time cleansing inaccuracies) to zero.
        removeZeroInvocationEntries();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public CPUCCTContainer getCCTContainer() {
        return cctContainer;
    }

    public String getMethodNameAtRow(int row) {
        ClientUtils.SourceCodeSelection sel = getSourceCodeSelectionAtRow(row);
        return MethodNameFormatterFactory.getDefault().getFormatter().formatMethodName(sel).toFormatted();
        
//        int methodId = methodIds[row];
//        String[] methodClassNameAndSig = cctContainer.getMethodClassNameAndSig(methodId);
//
//        return MethodNameFormatterFactory.getDefault().getFormatter()
//                                         .formatMethodName(methodClassNameAndSig[0], methodClassNameAndSig[1],
//                                                           methodClassNameAndSig[2]).toFormatted();
//
//        //    return format.getFormattedClassAndMethod();
    }
    
    public ClientUtils.SourceCodeSelection getSourceCodeSelectionAtRow(int row) {
        int methodId = methodIds[row];
        String[] sel = cctContainer.getMethodClassNameAndSig(methodId);
        return new ClientUtils.SourceCodeSelection(sel[0], sel[1], sel[2]);
    }

    public double getWholeGraphNetTime0() {
        return cctContainer.getWholeGraphNetTime0();
    }

    public double getWholeGraphNetTime1() {
        return cctContainer.getWholeGraphNetTime1();
    }
}
