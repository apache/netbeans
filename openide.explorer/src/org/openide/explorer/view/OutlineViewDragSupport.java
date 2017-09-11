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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

import java.awt.dnd.*;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import javax.swing.JComponent;


/** Support for the drag operations in the TreeView.
*
* @author Dafe Simonek, Jiri Rechtacek
*/
final class OutlineViewDragSupport extends ExplorerDragSupport {
    // Attributes
    // Associations

    /** The view that manages viewing the data in a tree. */
    protected OutlineView view;

    /** Cell renderer - PENDING - do we need it? */

    //protected DnDTreeViewCellRenderer cellRenderer;
    // Operations

    /** Creates new TreeViewDragSupport, initializes gesture */
    public OutlineViewDragSupport(OutlineView view, JComponent table) {
        this.view = view;
        this.comp = table;
    }

    @Override
    public int getAllowedDragActions() {
        return view.getAllowedDragActions();
    }

    int getAllowedDropActions() {
        return view.getAllowedDropActions();
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        super.dragGestureRecognized(dge);

        // TODO
        
        // notify tree cell editor that DnD operationm is active
//        if (exDnD.isDnDActive()) {
//            TreeCellEditor tce = ((JTree) tree).getCellEditor();
//
//            if (tce instanceof TreeViewCellEditor) {
//                ((TreeViewCellEditor) tce).setDnDActive(true);
//            }
//        }
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        // get the droped nodes
        Node[] dropedNodes = exDnD.getDraggedNodes();
        super.dragDropEnd(dsde);

        // if any original glass pane was stored (the DnD was broken e.g. by Esc)
        if (DropGlassPane.isOriginalPaneStored()) {
            // give back the orig glass pane
            DropGlassPane.putBackOriginal();

            // DnD is not active
            exDnD.setDnDActive(false);
        }

        // select the droped nodes
        try {
            if (dropedNodes != null) {
                ExplorerManager.Provider panel = (ExplorerManager.Provider) SwingUtilities.getAncestorOfClass(
                        ExplorerManager.Provider.class, view
                    );

                if (panel != null) {
                    panel.getExplorerManager().setSelectedNodes(dropedNodes);
                }
            }
        } catch (Exception e) {
            // don't care
        }

        // TODO
        
        // notify tree cell editor that DnD operationm is active
        // no more
//        TreeCellEditor tce = tree.getCellEditor();
//
//        if (tce instanceof TreeViewCellEditor) {
//            ((TreeViewCellEditor) tce).setDnDActive(false);
//        }
    }

    /** Utility method. Returns either selected nodes in tree
    * (if cursor hotspot is above some selected node) or the node
    * the cursor points to.
    * @return Node array or null if position of the cursor points
    * to no node.
    */
    Node[] obtainNodes(DragGestureEvent dge) {
        int[] selRows = view.getOutline().getSelectedRows();
        ArrayList<Node> al = new ArrayList<Node> (selRows.length);
        for (int i = 0; i < selRows.length; i++) {
            Node n = view.getNodeFromRow(selRows[i]);
            if (n != null) {
                al.add(n);
            }
        }
        Node[] result = al.toArray (new Node[al.size ()]);
        return result;
        // dge.getDragOrigin()
        
//        Node[] result = new Node[tps.length];
//
//        int cnt = 0;
//
//        for (int i = 0; i < tps.length; i++) {
//            if (tree.getPathBounds(tps[i]).contains()) {
//                cnt++;
//            }
//
//            result[i] = DragDropUtilities.secureFindNode(tps[i].getLastPathComponent());
//        }
//
//        // #41954:
//        // if the drag source is not at all in path location, do not return
//        // any nodes
//        return (cnt == 0) ? null : result;
    }
}
