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

package org.netbeans.installer.utils.helper.swing;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

public class NbiTreeTableColumnCellRenderer extends JLabel implements TreeCellRenderer {
    protected NbiTreeTable treeTable;
    
    public NbiTreeTableColumnCellRenderer(final NbiTreeTable treeTable) {
        this.treeTable = treeTable;
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        // we base our assumption on which node is selected on whether this row is 
        // selected in the table, since the selection is not propagated to the tree
        if (row == treeTable.getSelectedRow()) {
            setOpaque(true);
            setForeground(treeTable.getSelectionForeground());
            setBackground(treeTable.getSelectionBackground());
        } else {
            setOpaque(false);
            setForeground(treeTable.getForeground());
            setBackground(treeTable.getBackground());
        }
        
        setText(value.toString());
        
        return this;
    }
}
