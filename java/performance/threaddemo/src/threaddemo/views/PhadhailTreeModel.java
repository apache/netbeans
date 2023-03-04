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

package threaddemo.views;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import threaddemo.model.Phadhail;

// XXX listen to changes in display name, delete, rename, new

/**
 * Tree model displaying phadhails directly.
 * @author Jesse Glick
 */
final class PhadhailTreeModel implements TreeModel {

    private final Phadhail root;

    public PhadhailTreeModel(Phadhail root) {
        this.root = root;
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        return ((Phadhail)parent).getChildren().get(index);
    }
    
    public int getChildCount(Object parent) {
        return ((Phadhail)parent).getChildren().size();
    }
    
    public int getIndexOfChild(Object parent, Object child) {
        return ((Phadhail)parent).getChildren().indexOf(child);
    }
    
    public boolean isLeaf(Object node) {
        return !((Phadhail)node).hasChildren();
    }
    
    public void addTreeModelListener(TreeModelListener l) {}
    
    public void removeTreeModelListener(TreeModelListener l) {}
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        assert false;
    }
    
}
