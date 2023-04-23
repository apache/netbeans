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
import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.FilterSortSupport;

/**
 *
 * @author Jiri Sedlacek
 */
class DiffCPUCCTNode extends PrestimeCPUCCTNodeBacked {
    
    final PrestimeCPUCCTNode node1;
    final PrestimeCPUCCTNode node2;
    
    
    DiffCPUCCTNode(PrestimeCPUCCTNode n1, PrestimeCPUCCTNode n2) {
        node1 = n1;
        node2 = n2;
        container = node1 == null ? node2.container : node1.container;
    }
    
    
    DiffCPUCCTNode createCopy() {
        DiffCPUCCTNode copy = new DiffCPUCCTNode(node1, node2);
        setupCopy(copy);
        return copy;
    }
    
    
    public DiffCPUCCTNode createFilteredNode() {
        DiffCPUCCTNode filtered = new DiffCPUCCTNode(node1, node2);
        setupFilteredNode(filtered);
        return filtered;
    }
    
    public DiffCPUCCTNode createRootCopy() {
//        PrestimeCPUCCTNodeBacked copy1 = node1.createRootCopy();
//        PrestimeCPUCCTNodeBacked copy2 = node2.createRootCopy();
//        return new DiffCPUCCTNode(copy1, copy2);
        return null; // Subtrees currently not supported
    }
    

    @Override
    public CCTNode getChild(int index) {
        getChildren();

        if (index < children.length) {
            return children[index];
        } else {
            return null;
        }
    }

    @Override
    public CCTNode[] getChildren() {
        if (children != null) return children;
        
        PrestimeCPUCCTNode[] children1 = node1 == null ? null : (PrestimeCPUCCTNode[])node1.getChildren();
        PrestimeCPUCCTNode[] children2 = node2 == null ? null : (PrestimeCPUCCTNode[])node2.getChildren();
        
        if (children1 == null) children1 = new PrestimeCPUCCTNode[0];
        if (children2 == null) children2 = new PrestimeCPUCCTNode[0];
        children = computeChildren(children1, children2, this);
        
        if (children == null) children = new PrestimeCPUCCTNode[0];
        nChildren = children.length;
        
        return children;
    }
    
    private static PrestimeCPUCCTNode[] computeChildren(PrestimeCPUCCTNode[] children1, PrestimeCPUCCTNode[] children2, PrestimeCPUCCTNode parent) {        
        Map<String, PrestimeCPUCCTNode> nodes1 = new HashMap<>();
        for (PrestimeCPUCCTNode node : children1) {
            String name = node.getNodeName();
            PrestimeCPUCCTNode sameNode = nodes1.get(name);
            if (sameNode == null) nodes1.put(name, node.createCopy());
            else sameNode.merge(node); // Merge same-named items
        }
        
        Map<String, PrestimeCPUCCTNode> nodes2 = new HashMap<>();
        for (PrestimeCPUCCTNode node : children2) {
            String name = node.getNodeName();
            PrestimeCPUCCTNode sameNode = nodes2.get(name);
            if (sameNode == null) nodes2.put(name, node.createCopy());
            else sameNode.merge(node); // Merge same-named items
        }
        
        List<PrestimeCPUCCTNode> children = new ArrayList<>();
        for (PrestimeCPUCCTNode node1 : nodes1.values()) {
            PrestimeCPUCCTNode node2 = nodes2.get(node1.getNodeName());
            if (node2 != null) children.add(new DiffCPUCCTNode(node1, node2));
            else children.add(new DiffCPUCCTNode(node1, null));
        }
        for (PrestimeCPUCCTNode node2 : nodes2.values()) {
            if (!nodes1.containsKey(node2.getNodeName())) children.add(new DiffCPUCCTNode(null, node2));
        }
        
        for (PrestimeCPUCCTNode child : children) child.parent = parent;
        
        return children.toArray(new PrestimeCPUCCTNode[0]);
    }
    
    protected void resetChildren() {
        if (node1 != null) node1.resetChildren();
        if (node2 != null) node2.resetChildren();
        children = null;
    }

    @Override
    public int getMethodId() {
        return node1 == null ? (-node2.getMethodId()) : node1.getMethodId();
    }

    @Override
    public int getNCalls() {
        int nCalls1 = node1 == null ? 0 : node1.getNCalls();
        int nCalls2 = node2 == null ? 0 : node2.getNCalls();
        return nCalls2 - nCalls1 + nCalls;
    }

    @Override
    public int getNChildren() {
        return getChildren().length;
    }
    
    public boolean isLeaf() {
        boolean leaf1 = node1 == null || node1.isLeaf();
        boolean leaf2 = node2 == null || node2.isLeaf();
        return leaf1 && leaf2;
    }

    @Override
    public long getSleepTime0() {
        long sleepTime0_1 = node1 == null ? 0 : node1.getSleepTime0();
        long sleepTime0_2 = node2 == null ? 0 : node2.getSleepTime0();
        return sleepTime0_2 - sleepTime0_1 + sleepTime0;
    }

    @Override
    public int getThreadId() {
        return node1 == null ? node2.getThreadId() : node1.getThreadId();
    }

    @Override
    public long getTotalTime0() {
        long totalTime0_1 = node1 == null ? 0 : node1.getTotalTime0();
        long totalTime0_2 = node2 == null ? 0 : node2.getTotalTime0();
        return totalTime0_2 - totalTime0_1 + totalTime0;
    }

    @Override
    public float getTotalTime0InPerCent() {
        float totalTime0ipc_1 = node1 == null ? 0 : node1.getTotalTime0InPerCent();
        float totalTime0ipc_2 = node2 == null ? 0 : node2.getTotalTime0InPerCent();
        return totalTime0ipc_2 - totalTime0ipc_1;
    }

    @Override
    public long getTotalTime1() {
        long totalTime1_1 = node1 == null ? 0 : node1.getTotalTime1();
        long totalTime1_2 = node2 == null ? 0 : node2.getTotalTime1();
        return totalTime1_2 - totalTime1_1 + totalTime1;
    }

    @Override
    public float getTotalTime1InPerCent() {
        float totalTime1ipc_1 = node1 == null ? 0 : node1.getTotalTime1InPerCent();
        float totalTime1ipc_2 = node2 == null ? 0 : node2.getTotalTime1InPerCent();
        return totalTime1ipc_2 - totalTime1ipc_1;
    }

    @Override
    public long getWaitTime0() {
        long waitTime0_1 = node1 == null ? 0 : node1.getWaitTime0();
        long waitTime0_2 = node2 == null ? 0 : node2.getWaitTime0();
        return waitTime0_2 - waitTime0_1 + waitTime0;
    }

    @Override
    public void sortChildren(int sortBy, boolean sortOrder) {
//        if (node1 != null) node1.sortChildren(sortBy, sortOrder);
//        if (node2 != null) node2.sortChildren(sortBy, sortOrder);
//        super.sortChildren(sortBy, sortOrder);
    }    
    
    
    @Override
    public String getNodeName() {
        if (isFiltered()) return FilterSortSupport.FILTERED_OUT_LBL;
        return node1 == null ? node2.getNodeName() : node1.getNodeName();
    }
    
//    public void setSelfTimeNode() {
//        if (node1 != null) node1.setSelfTimeNode();
//        if (node2 != null) node2.setSelfTimeNode();
//    }
    
    @Override
    public boolean isSelfTimeNode() {
        return node1 == null ? node2.isSelfTimeNode() : node1.isSelfTimeNode();
    }
    
//    public void setThreadNode() {
//        if (node1 != null) node1.setThreadNode();
//        if (node2 != null) node2.setThreadNode();
//    }

    @Override
    public boolean isThreadNode() {
        return node1 == null ? node2.isThreadNode() : node1.isThreadNode();
    }
    
    @Override
    public boolean isContextCallsNode() {
        return node1 == null ? node2.isContextCallsNode() : node1.isContextCallsNode();
    }
    
//    public void setFilteredNode() {
//        if (node1 != null) node1.setFilteredNode();
//        if (node2 != null) node2.setFilteredNode();
//    }
    
//    public void resetFilteredNode() {
//        if (node1 != null) node1.resetFilteredNode();
//        if (node2 != null) node2.resetFilteredNode();
//    }

//    @Override
//    public boolean isFilteredNode() {
//        return node1 == null ? node2.isFilteredNode() : node1.isFilteredNode();
//    }
    
    public boolean equals(Object o) {
        if (!(o instanceof PrestimeCPUCCTNode)) return false;
        return getNodeName().equals(((PrestimeCPUCCTNode)o).getNodeName());
    }
    
    public int hashCode() {
        return getNodeName().hashCode();
    }
    
}
