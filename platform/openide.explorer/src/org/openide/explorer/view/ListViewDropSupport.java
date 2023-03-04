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

import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;

import java.awt.datatransfer.*;
import java.awt.dnd.*;

import javax.swing.JList;
import javax.swing.SwingUtilities;


/**
*
* @author Dafe Simonek
*/
final class ListViewDropSupport implements DropTargetListener, Runnable {
    // Attributes

    /** true if support is active, false otherwise */
    boolean active = false;
    boolean dropTargetPopupAllowed;

    /** Drop target asociated with the tree */
    DropTarget dropTarget;

    /** The index of last item the cursor hotspot was above */
    int lastIndex = -1;

    // Associations

    /** View manager. */
    protected ListView view;

    /** The component we are supporting with drop support */
    protected JList list;

    // Operations
    public ListViewDropSupport(ListView view, JList list) {
        this(view, list, true);
    }

    /** Creates new TreeViewDropSupport */
    public ListViewDropSupport(ListView view, JList list, boolean dropTargetPopupAllowed) {
        this.view = view;
        this.list = list;
        this.dropTargetPopupAllowed = dropTargetPopupAllowed;
    }

    public void setDropTargetPopupAllowed(boolean value) {
        dropTargetPopupAllowed = value;
    }

    public boolean isDropTargetPopupAllowed() {
        return dropTargetPopupAllowed;
    }

    /** User is starting to drag over us */
    public void dragEnter(DropTargetDragEvent dtde) {
        ExplorerDnDManager.getDefault().setMaybeExternalDragAndDrop( true );
        int dropAction = ExplorerDnDManager.getDefault().getAdjustedDropAction(
                dtde.getDropAction(), view.getAllowedDropActions()
            );

        lastIndex = indexWithCheck(dtde);

        if (lastIndex < 0) {
            dtde.rejectDrag();
        } else {
            dtde.acceptDrag(dropAction);
            NodeRenderer.dragEnter(list.getModel().getElementAt(lastIndex));
            list.repaint(list.getCellBounds(lastIndex, lastIndex));
        }
    }

    /** User drags over us */
    public void dragOver(DropTargetDragEvent dtde) {
        ExplorerDnDManager.getDefault().setMaybeExternalDragAndDrop( true );
        int dropAction = ExplorerDnDManager.getDefault().getAdjustedDropAction(
                dtde.getDropAction(), view.getAllowedDropActions()
            );

        int index = indexWithCheck(dtde);

        if (index < 0) {
            dtde.rejectDrag();

            if (lastIndex >= 0) {
                NodeRenderer.dragExit();
                list.repaint(list.getCellBounds(lastIndex, lastIndex));
                lastIndex = -1;
            }
        } else {
            dtde.acceptDrag(dropAction);

            if (lastIndex != index) {
                if (lastIndex < 0) {
                    lastIndex = index;
                }

                NodeRenderer.dragExit();
                NodeRenderer.dragEnter(list.getModel().getElementAt(index));
                list.repaint(list.getCellBounds(lastIndex, index));
                lastIndex = index;
            }
        }
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        // PENDING...?
    }

    /** User exits the dragging */
    public void dragExit(DropTargetEvent dte) {
        ExplorerDnDManager.getDefault().setMaybeExternalDragAndDrop( false );
        if (lastIndex >= 0) {
            NodeRenderer.dragExit();
            list.repaint(list.getCellBounds(lastIndex, lastIndex));
        }
    }

    /** Performs the drop action, if we are dropping on
    * right node and target node agrees.
    */
    public void drop(DropTargetDropEvent dtde) {
        boolean dropResult = true;
        try {
            // obtain the node we have cursor on
            int index = list.locationToIndex(dtde.getLocation());
            Object obj = list.getModel().getElementAt(index);
            Node dropNode = null;

            if (obj instanceof VisualizerNode) {
                dropNode = ((VisualizerNode) obj).node;
            }

            int dropAction = ExplorerDnDManager.getDefault().getAdjustedDropAction(
                    dtde.getDropAction(), view.getAllowedDropActions()
                );

            ExplorerDnDManager.getDefault().setMaybeExternalDragAndDrop( false );

            // return if conditions are not satisfied
            if ((index < 0) || !canDrop(dropNode, dropAction, dtde.getTransferable(), index)) {
                dtde.rejectDrop();

                return;
            }

            // get paste types for given transferred transferable
            Transferable t = ExplorerDnDManager.getDefault().getDraggedTransferable((DnDConstants.ACTION_MOVE & dropAction) != 0);
            if( null == t )
                t = dtde.getTransferable();
            PasteType pt = DragDropUtilities.getDropType( dropNode, t, dropAction, index );

            if (pt == null) {
                dropResult = false;

                // something is wrong, notify user
                // ugly hack, but if we don't wait, deadlock will come
                // (sun's issue....)
                RequestProcessor.getDefault().post(this, 500);

                return;
            }

            // finally perform the drop
            dtde.acceptDrop(dropAction);

            if (dropAction == DnDConstants.ACTION_LINK) {
                // show popup menu to the user
                // PENDING
            } else {
                DragDropUtilities.performPaste(pt, null);
            }
        } finally {
            dtde.dropComplete(dropResult);
        }
    }

    /** Can node recieve given drop action? */

    // XXX canditate for more general support
    private boolean canDrop(Node n, int dropAction, Transferable dndEventTransferable, int dropIndex) {
        if (n == null) {
            return false;
        }

        if (ExplorerDnDManager.getDefault().getNodeAllowedActions() == DnDConstants.ACTION_NONE) {
            return false;
        }

        // test if a parent of the dragged nodes isn't the node over
        // only for MOVE action
        if ((DnDConstants.ACTION_MOVE & dropAction) != 0) {
            Node[] nodes = ExplorerDnDManager.getDefault().getDraggedNodes();

            if( null != nodes ) {
                for (int i = 0; i < nodes.length; i++) {
                    if (n.equals(nodes[i].getParentNode())) {
                        return false;
                    }
                }
            }
        }

        Transferable trans = ExplorerDnDManager.getDefault().getDraggedTransferable(
                (DnDConstants.ACTION_MOVE & dropAction) != 0
            );

        if (trans == null) {
            trans = dndEventTransferable;
            if( trans == null ) {
                return false;
            }
        }

        // get paste types for given transferred transferable
        PasteType pt = DragDropUtilities.getDropType(n, trans, dropAction, dropIndex);

        return (pt != null);
    }

    /** Activates or deactivates Drag support on asociated JTree
    * component
    * @param active true if the support should be active, false
    * otherwise
    */
    public void activate(boolean active) {
        if (this.active == active) {
            return;
        }

        this.active = active;
        getDropTarget().setActive(active);
    }

    /** Implementation of the runnable interface.
    * Notifies user in AWT thread. */
    public void run() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this);

            return;
        }

        DragDropUtilities.dropNotSuccesfull();
    }

    /** @return The tree path to the node the cursor is above now or
    * null if no such node currently exists or if conditions were not
    * satisfied to continue with DnD operation.
    */
    int indexWithCheck(DropTargetDragEvent dtde) {
        int dropAction = ExplorerDnDManager.getDefault().getAdjustedDropAction(
                dtde.getDropAction(), view.getAllowedDropActions()
            );

        // check actions
        if ((dropAction & view.getAllowedDropActions()) == 0) {
            return -1;
        }

        // check location
        int index = list.locationToIndex(dtde.getLocation());
        if (index == -1) return -1;
        Object obj = list.getModel().getElementAt(index);

        if (obj instanceof VisualizerNode) {
            obj = ((VisualizerNode) obj).node;
        }

        if (index < 0) {
            return -1;
        }

        if (!(obj instanceof Node)) {
            return -1;
        }

        /* JST: Is necessary? Cannot be replaced by the use of special
        * transferable?

        // accept only node data flavors or multi flavor
        if (!dtde.isDataFlavorSupported(NodeTransfer.nodeCutFlavor) &&
            !dtde.isDataFlavorSupported(NodeTransfer.nodeCopyFlavor) &&
            !dtde.isDataFlavorSupported(ExTransferable.multiFlavor))
          return -1;
        */

        // succeeded
        return index;
    }

    /** Safe accessor to the drop target which is asociated
    * with the tree */
    DropTarget getDropTarget() {
        if (dropTarget == null) {
            dropTarget = new DropTarget(list, view.getAllowedDropActions(), this, false);
        }

        return dropTarget;
    }
}
