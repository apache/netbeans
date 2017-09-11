/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
