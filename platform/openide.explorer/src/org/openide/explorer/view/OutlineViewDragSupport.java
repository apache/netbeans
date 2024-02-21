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
        Node[] result = al.toArray (new Node[0]);
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
