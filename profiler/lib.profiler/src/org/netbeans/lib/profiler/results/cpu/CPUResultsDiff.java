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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.CCTNode;

/**
 *
 * @author Jiri Sedlacek
 */
public class CPUResultsDiff extends CPUResultsSnapshot {
    
    private final CPUResultsSnapshot snapshot1;
    private final CPUResultsSnapshot snapshot2;
    
    
    public CPUResultsDiff(CPUResultsSnapshot snapshot1, CPUResultsSnapshot snapshot2) {
//        this.snapshot1 = new CPUResultsSnapshot();
//        this.snapshot1.readFromSnapshot(snapshot1);
//        this.snapshot2 = new CPUResultsSnapshot();
//        this.snapshot2.readFromSnapshot(snapshot2);
        this.snapshot1 = snapshot1;
        this.snapshot2 = snapshot2;
        
        threadCCTContainers = snapshot1.threadCCTContainers;
    }
    
    public boolean isCollectingTwoTimeStamps() {
        return snapshot1.collectingTwoTimeStamps &&
               snapshot2.collectingTwoTimeStamps;
    }

    // Not used for diff
    public CPUCCTContainer getContainerForThread(int threadId, int view) {
        return snapshot1.getContainerForThread(threadId, view);
    }

    public DiffFlatProfileContainer getFlatProfile(int threadId, int view) {
        // NOTE: only works correctly for threadId == -1 (All Threads)
        // TODO: resolve correct threadId for snapshot2 !!!
        FlatProfileContainer fpc1 = snapshot1.getFlatProfile(threadId, view);
        FlatProfileContainer fpc2 = snapshot2.getFlatProfile(threadId, view);
        return DiffFlatProfileContainer.create(fpc1, fpc2);
    }
    
    public FlatProfileContainer getFlatProfile(Collection<Integer> threads, int view) {
        FlatProfileContainer fpc1 = snapshot1.getFlatProfile(threads, view);
        FlatProfileContainer fpc2 = snapshot2.getFlatProfile(threads2(threads), view);
        return DiffFlatProfileContainer.create(fpc1, fpc2);
    }
    
    // Not used for diff
    public String[] getInstrMethodClasses(int view) {
        return snapshot1.getInstrMethodClasses(view);
    }

    // Used by CPUTestCase.logInstrumented
    // Otherwise not used for diff
    public String[] getInstrMethodNames() {
        return snapshot1.getInstrMethodNames();
    }

    // Not used for diff
    public String[] getInstrMethodSignatures() {
        return snapshot1.getInstrMethodSignatures();
    }

    public String[] getMethodClassNameAndSig(int methodId, int view) {
        return methodId < 0 ? snapshot2.getMethodClassNameAndSig(-methodId, view) :
                              snapshot1.getMethodClassNameAndSig(methodId, view);
    }
    
    public Map<Integer, ClientUtils.SourceCodeSelection> getMethodIDMap(int view) {
        Map<Integer, ClientUtils.SourceCodeSelection> map = new HashMap<>();
        for (int i = 0; i < snapshot1.instrMethodClassesViews[view].length; i++)
            map.put(i, snapshot1.getSourceCodeSelection(i, view));
        for (int i = 0; i < snapshot2.instrMethodClassesViews[view].length; i++)
            map.put(-i, snapshot2.getSourceCodeSelection(i, view));
        return map;
    }

    // Not used for diff
    public int getNInstrMethods() {
        return snapshot1.getNInstrMethods();
    }

    // Not used
    public int getNThreads() {
        return snapshot1.getNThreads();
    }

    public PrestimeCPUCCTNode getReverseCCT(int threadId, int methodId, int view) {
        // TODO: resolve correct threadId for snapshot2
        PrestimeCPUCCTNode root1 = snapshot1.getReverseCCT(threadId, methodId, view);
        PrestimeCPUCCTNode root2 = snapshot2.getReverseCCT(threadId, methodId, view);
        return new DiffCPUCCTNode(root1, root2);
    }

    public PrestimeCPUCCTNode getRootNode(int view) {
        PrestimeCPUCCTNode root1 = snapshot1.getRootNode(view);
        PrestimeCPUCCTNode root2 = snapshot2.getRootNode(view);
        return new DiffCPUCCTNode(root1, root2);
    }
    
    public PrestimeCPUCCTNode getRootNode(int view, Collection<Integer> threads, boolean merge) {
        PrestimeCPUCCTNode root1 = snapshot1.getRootNode(view, threads, merge);
        PrestimeCPUCCTNode root2 = snapshot2.getRootNode(view, threads2(threads), merge);
        return new DiffCPUCCTNode(root1, root2);
    }
    
    public PrestimeCPUCCTNode getReverseRootNode(int view, Collection<Integer> threads, boolean merge) {
        PrestimeCPUCCTNode root1 = snapshot1.getReverseRootNode(view, threads, merge);
        PrestimeCPUCCTNode root2 = snapshot2.getReverseRootNode(view, threads2(threads), merge);
        return new DiffCPUCCTNode(root1, root2);
    }
    
    private Collection<Integer> threads2(Collection<Integer> threads1) {
        if (threads1 == null || threads1.isEmpty()) return threads1;
        
        Set<String> threads1Names = new HashSet<>();
        for (int thread1Id : threads1) threads1Names.add(getThreadNameForId(thread1Id));
        Set<Integer> threads2 = new HashSet<>();
        for (int thread2Id : snapshot2.getThreadIds())
            if (threads1Names.contains(snapshot2.getThreadNameForId(thread2Id)))
                threads2.add(thread2Id);
        
        return threads2;
    }
    
    public long getBound(int view) {
        long bound = Long.MIN_VALUE;
        
        PrestimeCPUCCTNode root = getRootNode(view);
        CCTNode[] children = root.getChildren();
        for (CCTNode child : children)
            bound = Math.max(bound, Math.abs(((PrestimeCPUCCTNode)child).getTotalTime0()));
        
        return bound;
    }
    
    public void filterForward(final String filter, final int filterType, final PrestimeCPUCCTNodeBacked root) {
//        PrestimeCPUCCTNodeBacked node1 = ((DiffCPUCCTNode)root).node1;
//        if (node1 != null) snapshot1.filterForward(filter, filterType, node1);
//        
//        PrestimeCPUCCTNodeBacked node2 = ((DiffCPUCCTNode)root).node2;
//        if (node2 != null) snapshot2.filterForward(filter, filterType, node2);
//        
//        super.filterForward(filter, filterType, root);
    }
    
    public void saveSortParams(int sortBy, boolean sortOrder, CCTNode node) {
//        PrestimeCPUCCTNodeBacked node1 = ((DiffCPUCCTNode)node).node1;
//        if (node1 != null) snapshot1.saveSortParams(sortBy, sortOrder, node1);
//        
//        PrestimeCPUCCTNodeBacked node2 = ((DiffCPUCCTNode)node).node2;
//        if (node2 != null) snapshot2.saveSortParams(sortBy, sortOrder, node2);
//        
//        super.saveSortParams(sortBy, sortOrder, node);
    }
    
    // TODO: used by CPUDiffPanel, fix!
    public int[] getThreadIds() {
        return snapshot1.getThreadIds();
    }

    // Not used for diff
    public String getThreadNameForId(int threadId) {
        return snapshot1.getThreadNameForId(threadId);
    }

    // TODO: used by CPUDiffPanel, fix!
    public String[] getThreadNames() {
        return snapshot1.getThreadNames();
    }
    
}
