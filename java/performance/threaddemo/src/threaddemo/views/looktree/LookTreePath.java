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

package threaddemo.views.looktree;

import javax.swing.tree.TreePath;

/**
 * Path from root to a lower "bottom" node in the look tree.
 * @author Jesse Glick
 */
public final class LookTreePath extends TreePath {

    private final LookTreeNode bottom;

    public LookTreePath(LookTreeNode node) {
        bottom = node;
    }

    public Object[] getPath() {
        int c = getPathCount();
        Object[] path = new Object[c];
        LookTreeNode n = bottom;
        for (int i = c - 1; i >= 0; i--) {
            path[i] = n;
            n = n.getParent();
        }
        return path;
    }
    
    public int getPathCount() {
        LookTreeNode n = bottom;
        int i = 0;
        while (n != null) {
            i++;
            n = n.getParent();
        }
        return i;
    }
    
    public Object getPathComponent(int x) {
        int c = getPathCount();
        LookTreeNode n = bottom;
        for (int i = 0; i < c - x - 1; i++) {
            n = n.getParent();
        }
        return n;
    }
    
    public TreePath pathByAddingChild(Object child) {
        return new LookTreePath((LookTreeNode)child);
    }
    
    public boolean equals(Object o) {
        return (o instanceof LookTreePath) &&
            ((LookTreePath)o).bottom == bottom;
    }
    
    public int hashCode() {
        return bottom.hashCode();
    }
    
    public Object getLastPathComponent() {
        return bottom;
    }
    
    public TreePath getParentPath() {
        LookTreeNode p = bottom.getParent();
        if (p != null) {
            return new LookTreePath(p);
        } else {
            return null;
        }
    }
    
}

