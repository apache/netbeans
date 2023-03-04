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

import java.awt.Rectangle;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

import java.awt.dnd.*;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.*;


/** Support for the drag operations in the TreeView.
*
* @author Dafe Simonek, Jiri Rechtacek
*/
final class TreeViewDragSupport extends ExplorerDragSupport {
    // Attributes
    // Associations

    /** The view that manages viewing the data in a tree. */
    protected TreeView view;

    /** The tree which we are supporting (our client) */
    private JTree tree;

    /** Cell renderer - PENDING - do we need it? */

    //protected DnDTreeViewCellRenderer cellRenderer;
    // Operations

    /** Creates new TreeViewDragSupport, initializes gesture */
    public TreeViewDragSupport(TreeView view, JTree tree) {
        this.view = view;
        this.comp = tree;
        this.tree = tree;
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

        // notify tree cell editor that DnD operationm is active
        if (exDnD.isDnDActive()) {
            TreeCellEditor tce = tree.getCellEditor();

            if (tce instanceof TreeViewCellEditor) {
                ((TreeViewCellEditor) tce).setDnDActive(true);
            }
        }
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

        // notify tree cell editor that DnD operationm is active
        // no more
        TreeCellEditor tce = tree.getCellEditor();

        if (tce instanceof TreeViewCellEditor) {
            ((TreeViewCellEditor) tce).setDnDActive(false);
        }
    }

    /** Utility method. Returns either selected nodes in tree
    * (if cursor hotspot is above some selected node) or the node
    * the cursor points to.
    * @return Node array or null if position of the cursor points
    * to no node.
    */
    Node[] obtainNodes(DragGestureEvent dge) {
        TreePath[] tps = tree.getSelectionPaths();

        if (tps == null) {
            return null;
        }

        Node[] result = new Node[tps.length];

        int cnt = 0;

        for (int i = 0; i < tps.length; i++) {
            Rectangle r = tree.getPathBounds(tps[i]);
            if (r != null && r.contains(dge.getDragOrigin())) {
                cnt++;
            }

            result[i] = DragDropUtilities.secureFindNode(tps[i].getLastPathComponent());
        }

        // #41954:
        // if the drag source is not at all in path location, do not return
        // any nodes
        return (cnt == 0) ? null : result;
    }
}
 /* end class TreeViewDragSupport */
