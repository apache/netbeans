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
package org.openide.explorer.view;

import java.awt.Component;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.swing.etable.ETable;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Allows customization of the popup menus of TableView and OutlineView.
 * Just create a subclass of this class and override method createPopupMenu
 * (and call setNodePopupFactory() on TableView or OutlineView).
 * @author David Strupl
 */
public class NodePopupFactory {
    
    private boolean showQuickFilter = true;
    
    /** Creates a new instance of NodePopupFactory */
    public NodePopupFactory() {
    }
    
    /**
     * Creates a popup menu with entries from the selected nodes
     * related to the given component (usually a ETable subclass). The popup
     * is created for the table element in given column and row (column
     *  and row are in the view's coordinates (not the model's)).
     */
    public JPopupMenu createPopupMenu(int row, int column, Node[] selectedNodes,
            Component component) {
        
        Action[] actions = NodeOp.findActions (selectedNodes);
        JPopupMenu res = Utilities.actionsToPopup(actions, component);
        if (showQuickFilter) {
            if ((component instanceof ETable) && (column >= 0)) {
                ETable et = (ETable)component;
                if (row >= 0) {
                    Object val = et.getValueAt(row, column);
                    val = et.transformValue(val);
                    String s = NbBundle.getMessage(NodePopupFactory.class, "LBL_QuickFilter");
                    res.add(et.getQuickFilterPopup(column, val, s));
                } else if (et.getQuickFilterColumn() == column) {
                    addNoFilterItem(et, res);
                }
            }
        }
        return res;
    }

    void addNoFilterItem(ETable et, JPopupMenu popup) {
        if (showQuickFilter && et.getQuickFilterColumn() != -1) {
            String s = NbBundle.getMessage(NodePopupFactory.class, "LBL_QuickFilter");
            JMenu menu = new JMenu(s);
            JMenuItem noFilterItem = et.getQuickFilterNoFilterItem(et.getQuickFilterFormatStrings()[6]);
            menu.add(noFilterItem);
            popup.add(menu);
        }
    }

    /**
     * 
     */
    public void setShowQuickFilter(boolean show) {
        this.showQuickFilter = show;
    }
}
