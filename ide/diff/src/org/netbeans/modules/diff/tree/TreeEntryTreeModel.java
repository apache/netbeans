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

package org.netbeans.modules.diff.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TreeEntryTreeModel implements TreeModel {
    private TreeEntry root;
    private final List<TreeModelListener> tml = new ArrayList<>();

    public TreeEntryTreeModel(TreeEntry root) {
        this.root = root;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    public void setRoot(TreeEntry te) {
        this.root = te;
        TreeModelEvent tme = new TreeModelEvent(this, new TreePath(root));
        for(TreeModelListener tl: tml) {
            tl.treeStructureChanged(tme);
        }
    }

    @Override
    public Object getChild(Object o, int i) {
        return ((TreeEntry) o).getChildren().get(i);
    }

    @Override
    public int getChildCount(Object o) {
        return ((TreeEntry) o).getChildren().size();
    }

    @Override
    public boolean isLeaf(Object o) {
        return ((TreeEntry) o).getChildren().isEmpty();
    }

    @Override
    public void valueForPathChanged(TreePath tp, Object o) {
        TreeModelEvent tme = new TreeModelEvent(this, tp);
        for (TreeModelListener tl : tml) {
            tl.treeNodesChanged(tme);
        }
    }

    @Override
    public int getIndexOfChild(Object o, Object o1) {
        return ((TreeEntry) o).getChildren().indexOf(o1);
    }

    @Override
    public void addTreeModelListener(TreeModelListener tl) {
        tml.add(tl);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener tl) {
        tml.remove(tl);
    }

}
