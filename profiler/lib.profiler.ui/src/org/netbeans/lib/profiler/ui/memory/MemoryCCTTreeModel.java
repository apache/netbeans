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

package org.netbeans.lib.profiler.ui.memory;

import org.netbeans.lib.profiler.results.memory.*;
import javax.swing.tree.*;


/**
 * Implementation of TreeModel for Memory CCT Trees
 *
 * @author Misha Dmitriev
 * @author Jiri Sedlacek
 */
public class MemoryCCTTreeModel implements TreeModel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private PresoObjAllocCCTNode root;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of MemoryCCTTreeModel */
    public MemoryCCTTreeModel(PresoObjAllocCCTNode root) {
        this.root = root;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Object getChild(Object obj, int index) {
        if (obj == null) {
            return null;
        }

        PresoObjAllocCCTNode node = (PresoObjAllocCCTNode) obj;

        return node.getChild(index);
    }

    public int getChildCount(Object obj) {
        if (obj == null) {
            return -1;
        }

        PresoObjAllocCCTNode node = (PresoObjAllocCCTNode) obj;

        return node.getNChildren();
    }

    public int getIndexOfChild(Object parentObj, Object childObj) {
        if ((parentObj == null) || (childObj == null)) {
            return -1;
        }

        PresoObjAllocCCTNode parent = (PresoObjAllocCCTNode) parentObj;
        PresoObjAllocCCTNode child = (PresoObjAllocCCTNode) childObj;

        return parent.getIndexOfChild(child);
    }

    public boolean isLeaf(Object obj) {
        if (obj == null) {
            return true;
        }

        PresoObjAllocCCTNode node = (PresoObjAllocCCTNode) obj;

        return (node.getNChildren() == 0);
    }

    public Object getRoot() {
        return root;
    }

    public void addTreeModelListener(javax.swing.event.TreeModelListener treeModelListener) {
    }

    public void removeTreeModelListener(javax.swing.event.TreeModelListener treeModelListener) {
    }

    // --------------------------------------------------------------  

    // TreeModel interface methods that we don't implement
    public void valueForPathChanged(javax.swing.tree.TreePath treePath, Object obj) {
    }
}
