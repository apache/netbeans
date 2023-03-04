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

package org.netbeans.lib.profiler.results;

import java.util.Collections;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;


/**
 * This interface must be implemented by every CCT node.
 *
 * @author Jiri Sedlacek
 */
public abstract class CCTNode implements TreeNode {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public abstract CCTNode getChild(int index);

    public abstract CCTNode[] getChildren();

    public abstract int getIndexOfChild(Object child);

    public abstract int getNChildren();

    public abstract CCTNode getParent();

    //public boolean hasChildren();
    
    
    // --- Filtering support ---
    
    private boolean filtered;
    
    public CCTNode createFilteredNode() { return null; }
    
    protected void setFilteredNode() { filtered = true; }
    
    public boolean isFiltered() { return filtered; }
    
    public void merge(CCTNode node) {}
    
    // ---
    
    
    //--- TreeNode adapter ---
    public Enumeration<CCTNode> children() {
        final CCTNode[] _children = getChildren();
        final int _childrenCount = _children == null ? 0 : _children.length;
        
        if (_childrenCount == 0) return Collections.emptyEnumeration();
        
        return new Enumeration<CCTNode>() {
            private int index = 0;
            public boolean hasMoreElements() { return index < _childrenCount; }
            public CCTNode nextElement()     { return _children[index++]; }
        };
    }
    
    public boolean isLeaf() {
        return getChildCount() == 0;
    }
    
    public boolean getAllowsChildren() {
        return true;
    }
    
    public int getIndex(TreeNode node) {
        return getIndexOfChild(node);
    }
    
    public int getChildCount() {
        return getNChildren();
    }
    
    public TreeNode getChildAt(int index) {
        return getChild(index);
    }
    //---
    
    
    public static interface FixedPosition {}
    
    public static interface AlwaysFirst extends FixedPosition {}
    
    public static interface AlwaysLast extends FixedPosition {}
    
}
