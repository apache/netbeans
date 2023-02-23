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
package org.netbeans.lib.profiler.results.locks;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.lib.profiler.results.CCTNode;

/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
public abstract class LockCCTNode extends CCTNode {
    
//    public static final int SORT_BY_NAME = 1;
//    public static final int SORT_BY_TIME = 2;
//    public static final int SORT_BY_WAITS = 3;
    
    public static final LockCCTNode EMPTY = new LockCCTNode(null) {
        public LockCCTNode getChild(int index) { return null; }
        public LockCCTNode[] getChildren() { return new LockCCTNode[0]; }
        public int getIndexOfChild(Object child) { return -1; }
        public int getNChildren() { return 0; }
        public String getNodeName() { return ""; } // NOI18N
        public long getTime() { return 0; }
        public double getTimeInPerCent() { return 0; }
        public long getWaits() { return 0; }
    };

    private List<LockCCTNode> children;
    private final LockCCTNode parent;
    
//    private int sortBy;
//    private boolean sortOrder;

    LockCCTNode(LockCCTNode p) {
        parent = p;
    }
    

    @Override
    public LockCCTNode getChild(int index) {
        if (children == null) {
            computeChildrenImpl();
        }
        return children.get(index);
    }

    @Override
    public LockCCTNode[] getChildren() {
        if (children == null) {
            computeChildrenImpl();
        }
        return children.toArray(new LockCCTNode[0]);
    }

    @Override
    public int getIndexOfChild(Object child) {
        if (children == null) {
            computeChildrenImpl();
        }
        return children.indexOf(child);
    }

    @Override
    public int getNChildren() {
        if (children == null) {
            computeChildrenImpl();
        }
        return children.size();
    }

    @Override
    public LockCCTNode getParent() {
        return parent;
    }

    void addChild(LockCCTNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    void computeChildren() {
        children = new ArrayList<>();
    }
    
    private void computeChildrenImpl() {
        computeChildren();
//        sortChildren(sortBy, sortOrder);
    }

    public double getTimeInPerCent() {
        LockCCTNode p = getParent();
        long allTime = p.getTime();
        double ppercent = p.getTimeInPerCent();
        long time = getTime();
        return ppercent * time / allTime;
    }

    public abstract String getNodeName();

    public abstract long getTime();

    public abstract long getWaits();
    
    public boolean isThreadLockNode() { return false; }
    public boolean isMonitorNode() { return false; }
    
    public String toString() { return getNodeName(); }
    
//    public void sortChildren(int sortBy, boolean sortOrder) {
//        this.sortBy = sortBy;
//        this.sortOrder = sortOrder;
//        
//        if (children == null || getNChildren() < 2) return;
//        
//        doSortChildren(sortBy, sortOrder);
//    }
//    
//    protected void doSortChildren(int sortBy, boolean sortOrder) {
//        switch (sortBy) {
//            case SORT_BY_NAME:
//                sortChildrenByName(sortOrder);
//                break;
//            case SORT_BY_TIME:
//                sortChildrenByTime(sortOrder);
//                break;
//            case SORT_BY_WAITS:
//                sortChildrenByWaits(sortOrder);
//                break;
//        }
//        
//        for (LockCCTNode child : children) child.sortChildren(sortBy, sortOrder);
//    }
//
//    protected void sortChildrenByName(final boolean sortOrder) {
//        Collections.sort(children, new Comparator<LockCCTNode>() {
//            public int compare(LockCCTNode n1, LockCCTNode n2) {
//                return sortOrder ?
//                       n1.getNodeName().toLowerCase().compareTo(n2.getNodeName().toLowerCase()) :
//                       n2.getNodeName().toLowerCase().compareTo(n1.getNodeName().toLowerCase());
//            }
//        });
//    }
//
//    protected void sortChildrenByTime(final boolean sortOrder) {
//        Collections.sort(children, new Comparator<LockCCTNode>() {
//            public int compare(LockCCTNode n1, LockCCTNode n2) {
//                long result = sortOrder ? n1.getTime() - n2.getTime() :
//                                          n2.getTime() - n1.getTime();
//                return result == 0 ? 0 : (result > 0 ? 1 : -1);
//            }
//        });
//    }
//    
//    protected void sortChildrenByWaits(final boolean sortOrder) {
//        Collections.sort(children, new Comparator<LockCCTNode>() {
//            public int compare(LockCCTNode n1, LockCCTNode n2) {
//                long result = sortOrder ? n1.getWaits() - n2.getWaits() :
//                                          n2.getWaits() - n1.getWaits();
//                return result == 0 ? 0 : (result > 0 ? 1 : -1);
//            }
//        });
//    }
    

    public void debug() {
        if (parent != null) {
            String offset = "";
            for (CCTNode p = parent; p != null; p = p.getParent()) {
                offset += "  ";
            }
            System.out.println(offset + getNodeName() + 
                    " Waits: " + getWaits() + 
                    " Time: " + getTime() + 
                    " " + NumberFormat.getPercentInstance().format(getTimeInPerCent()/100));
        }
        for (CCTNode ch : getChildren()) {
            if (ch instanceof LockCCTNode) {
                ((LockCCTNode) ch).debug();
            }
        }
    }
}
