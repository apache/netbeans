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

import java.util.HashSet;
import java.util.ResourceBundle;


/**
 * A container for all threads merged CPU data. Currently supports/provides only flat profile data.
 *
 * @author Misha Dmitriev
 */
public class AllThreadsMergedCPUCCTContainer extends CPUCCTContainer {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String ALL_THREADS_STRING = ResourceBundle.getBundle("org.netbeans.lib.profiler.results.cpu.Bundle").getString("AllThreadsMergedCPUCCTContainer_AllThreadsString"); // NOI18N
                                                                                                                             // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected int view;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public AllThreadsMergedCPUCCTContainer(CPUResultsSnapshot cpuResSnapshot, PrestimeCPUCCTNode[] rootNodeSubNodes, int view) {
        super(cpuResSnapshot);
        this.threadId = -1;
        this.threadName = ALL_THREADS_STRING;
        this.view = view;
        collectingTwoTimeStamps = cpuResSnapshot.isCollectingTwoTimeStamps();

        compactData = new byte[OFS_SUBNODE02];
        setNCallsForNodeOfs(0, 1); // 1 call for "All threads" node looks more logical than 0 calls
        rootNode = new PrestimeCPUCCTNodeBacked(this, rootNodeSubNodes);

        // Calculate the total execution time for all threads by just summing individual thread total times
        long time = 0;

        for (int i = 0; i < rootNodeSubNodes.length; i++) {
            time += rootNodeSubNodes[i].getTotalTime0();
        }

        wholeGraphNetTime0 = time;
        setTotalTime0ForNodeOfs(0, time);

        if (collectingTwoTimeStamps) {
            time = 0;

            for (int i = 0; i < rootNodeSubNodes.length; i++) {
                time += rootNodeSubNodes[i].getTotalTime1();
            }

            wholeGraphNetTime1 = time;
            setTotalTime1ForNodeOfs(0, time);
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String[] getMethodClassNameAndSig(int methodId) {
        return cpuResSnapshot.getMethodClassNameAndSig(methodId, view);
    }

    protected FlatProfileContainer generateFlatProfile() {
        preGenerateFlatProfile();

        PrestimeCPUCCTNode[] children = (PrestimeCPUCCTNode[]) rootNode.getChildren();

        if (children != null) for (int i = 0; i < children.length; i++) {
            CPUCCTContainer childContainer = children[i].getContainer();
            childContainer.timePerMethodId0 = this.timePerMethodId0;
            childContainer.timePerMethodId1 = this.timePerMethodId1;
            childContainer.totalTimePerMethodId0 = this.totalTimePerMethodId0;
            childContainer.totalTimePerMethodId1 = this.totalTimePerMethodId1;
            childContainer.invPerMethodId = this.invPerMethodId;
            childContainer.methodsOnStack = new HashSet();
            
            childContainer.addFlatProfTimeForNode(0);

            childContainer.timePerMethodId0 = childContainer.timePerMethodId1 = null;
            childContainer.totalTimePerMethodId0 = childContainer.totalTimePerMethodId1 = null;
            childContainer.invPerMethodId = null;
            childContainer.methodsOnStack = null;
        }

        return postGenerateFlatProfile();
    }

    protected PrestimeCPUCCTNodeFree generateReverseCCT(int methodId) {
        PrestimeCPUCCTNode[] children = (PrestimeCPUCCTNode[]) rootNode.getChildren();

        PrestimeCPUCCTNodeFree rev = null;

        for (int i = 0; i < children.length; i++) {
            CPUCCTContainer childContainer = children[i].getContainer();

            if (rev == null) {
                rev = childContainer.generateReverseCCT(methodId);
            } else {
                childContainer.addToReverseCCT(rev, methodId);
            }
        }

        return rev;
    }
}
