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

import java.awt.Component;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreeCellEditor;

/**
 * @author Jesse Glick
 */
@SuppressWarnings("unchecked")
class LookTreeCellEditor extends DefaultTreeCellEditor {

    public LookTreeCellEditor(JTree tree, LookTreeCellRenderer r) {
        super(tree, r);
    }

    public boolean isCellEditable(EventObject ev) {
        if (!super.isCellEditable(ev)) {
            return false;
        }
        LookTreeNode n = (LookTreeNode)lastPath.getLastPathComponent();
        return n.getLook().canRename(n.getData(), n.getLookup());
        // XXX is it better to override JTree.isPathEditable?
    }
    
    protected TreeCellEditor createTreeCellEditor() {
        JTextField tf = new JTextField();
        Ed ed = new Ed(tf);
        ed.setClickCountToStart(1);
        return ed;
    }

    private static class Ed extends DefaultCellEditor {

        public Ed(JTextField tf) {
            super(tf);
        }

        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            LookTreeNode n = (LookTreeNode)value;
            delegate.setValue(n.getLook().getName(n.getData(), n.getLookup() ));
            ((JTextField)editorComponent).selectAll();
            return editorComponent;
        }
    }

}
