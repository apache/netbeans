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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.lib.profiler.results.FilterSortSupport;

/**
 *
 * @author Jiri Sedlacek
 */
class DiffObjLivenessCCTNode extends PresoObjLivenessCCTNode {
    
    private final PresoObjLivenessCCTNode node1;
    private final PresoObjLivenessCCTNode node2;
    
    
    DiffObjLivenessCCTNode(PresoObjLivenessCCTNode node1, PresoObjLivenessCCTNode node2) {
        this.node1 = node1;
        this.node2 = node2;
        
        long nCalls1 = node1 == null ? 0 : node1.nCalls;
        long nCalls2 = node2 == null ? 0 : node2.nCalls;
        nCalls = nCalls2 - nCalls1;
        
        long totalObjSize1 = node1 == null ? 0 : node1.totalObjSize;
        long totalObjSize2 = node2 == null ? 0 : node2.totalObjSize;
        totalObjSize = totalObjSize2 - totalObjSize1;
        
        float avgObjectAge1 = node1 == null ? 0 : node1.avgObjectAge;
        float avgObjectAge2 = node2 == null ? 0 : node2.avgObjectAge;
        avgObjectAge = avgObjectAge2 - avgObjectAge1;
        
        int nLiveObjects1 = node1 == null ? 0 : node1.nLiveObjects;
        int nLiveObjects2 = node2 == null ? 0 : node2.nLiveObjects;
        nLiveObjects = nLiveObjects2 - nLiveObjects1;
        
        int survGen1 = node1 == null ? 0 : node1.survGen;
        int survGen2 = node2 == null ? 0 : node2.survGen;
        survGen = survGen2 - survGen1;
        
        PresoObjAllocCCTNode[] children1 = node1 == null ? null : (PresoObjAllocCCTNode[])node1.getChildren();
        if (children1 == null) children1 = new PresoObjAllocCCTNode[0];
        PresoObjAllocCCTNode[] children2 = node2 == null ? null : (PresoObjAllocCCTNode[])node2.getChildren();
        if (children2 == null) children2 = new PresoObjAllocCCTNode[0];
        setChildren(computeChildren(children1, children2, this));
    }
    
    
    public DiffObjLivenessCCTNode createFilteredNode() {
        DiffObjLivenessCCTNode filtered = new DiffObjLivenessCCTNode(node1, node2);
        setupFilteredNode(filtered);
        return filtered;
    }
    
    
    public String getNodeName() {
        if (nodeName == null) {
            if (isFiltered()) nodeName = FilterSortSupport.FILTERED_OUT_LBL;
            else nodeName = getNode().getNodeName();
        }
        return nodeName;
    }
    
    public String[] getMethodClassNameAndSig() {
        return getNode().getMethodClassNameAndSig();
    }
    
    
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DiffObjLivenessCCTNode)) return false;
        DiffObjLivenessCCTNode other = (DiffObjLivenessCCTNode)o;
        if (isFiltered()) {
            return other.isFiltered();
        }
        if (other.isFiltered()) {
            return false;
        }
        return getNode().equals(other.getNode());
    }

    public int hashCode() {
        return getNode().hashCode();
    }
    
    
    public boolean isLeaf() {
        boolean leaf1 = node1 == null || node1.isLeaf();
        boolean leaf2 = node2 == null || node2.isLeaf();
        return leaf1 && leaf2;
    }
    
    private PresoObjLivenessCCTNode getNode() {
        if (node1 == null) {
            return node2;
        }
        return node1;
    }
    
    private static PresoObjAllocCCTNode[] computeChildren(PresoObjAllocCCTNode[] children1, PresoObjAllocCCTNode[] children2, PresoObjLivenessCCTNode parent) {        
        Map<Handle, PresoObjAllocCCTNode> nodes1 = new HashMap<>();
        for (PresoObjAllocCCTNode node : children1) {
            Handle name = new Handle(node);
            PresoObjAllocCCTNode sameNode = nodes1.get(name);
            if (sameNode == null) nodes1.put(name, node);
            else sameNode.merge(node);
        }
        
        Map<Handle, PresoObjAllocCCTNode> nodes2 = new HashMap<>();
        for (PresoObjAllocCCTNode node : children2) {
            Handle name = new Handle(node);
            PresoObjAllocCCTNode sameNode = nodes2.get(name);
            if (sameNode == null) nodes2.put(name, node);
            else sameNode.merge(node); // Merge same-named items
        }
        
        List<PresoObjAllocCCTNode> children = new ArrayList<>();
        for (PresoObjAllocCCTNode node1 : nodes1.values()) {
            PresoObjAllocCCTNode node2 = nodes2.get(new Handle(node1));
            if (node2 != null) children.add(new DiffObjLivenessCCTNode((PresoObjLivenessCCTNode)node1, (PresoObjLivenessCCTNode)node2));
            else children.add(new DiffObjLivenessCCTNode((PresoObjLivenessCCTNode)node1, null));
        }
        for (PresoObjAllocCCTNode node2 : nodes2.values()) {
            if (!nodes1.containsKey(new Handle(node2))) children.add(new DiffObjLivenessCCTNode(null, (PresoObjLivenessCCTNode)node2));
        }
        
        return children.toArray(new PresoObjAllocCCTNode[0]);
    }
    
}
