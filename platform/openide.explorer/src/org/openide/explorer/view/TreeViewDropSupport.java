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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.PasteType;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

/** Implementation of drop support for asociated Tree View.
*
* @author Dafe Simonek, Jiri Rechtacek
*/
final class TreeViewDropSupport implements DropTargetListener, Runnable {
    protected static final int FUSSY_POINTING = 3;
    private static final int DELAY_TIME_FOR_EXPAND = 1000;
    private static final int SHIFT_DOWN = -1;
    private static final int SHIFT_RIGHT = 10;
    private static final int SHIFT_LEFT = 15;

    // Attributes

    /** true if support is active, false otherwise */
    boolean active = false;
    boolean dropTargetPopupAllowed;

    /** Drop target asociated with the tree */
    DropTarget dropTarget;

    /** Node area which we were during
    * DnD operation. */
    Rectangle lastNodeArea;
    private int upperNodeIdx = -1;
    private int lowerNodeIdx = -1;
    private int dropIndex = -1;

    /** Swing Timer for expand node's parent with delay time. */
    Timer timer;

    /** Glass pane for JTree which is associate with this class. */
    DropGlassPane dropPane;
    private int pointAt = DragDropUtilities.NODE_CENTRAL;

    // Associations

    /** View manager. */
    protected TreeView view;

    /** The component we are supporting with drop support */
    protected JTree tree;

    // Operations

    /** Creates new TreeViewDropSupport */
    public TreeViewDropSupport(TreeView view, JTree tree, boolean dropTargetPopupAllowed) {
        this.view = view;
        this.tree = tree;
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
        checkStoredGlassPane();

        dropIndex = -1;
        
        // set a status and cursor of dnd action
        doDragOver(dtde);
    }

    /** User drags over us */
    public void dragOver(DropTargetDragEvent dtde) {
        // bugfix #34483; jdk1.4.1 on w2k could calls dragOver() before dragEnter()
        // (jkdbug fixed in 1.4.2)
        // this check make dragOver/Enter more robust
        checkStoredGlassPane();

        // set a status and cursor of dnd action
        doDragOver(dtde);
    }

    private void checkStoredGlassPane() {
        // remember current glass pane to set back at end of dragging over this compoment
        if (!DropGlassPane.isOriginalPaneStored()) {
            Component comp = tree.getRootPane().getGlassPane();
            DropGlassPane.setOriginalPane(tree, comp, comp.isVisible());

            // set glass pane for paint selection line
            dropPane = DropGlassPane.getDefault(tree);
            tree.getRootPane().setGlassPane(dropPane);
            dropPane.revalidate();
            dropPane.setVisible(true);
        }
    }

    /** Process events dragEnter or dragOver. */
    private void doDragOver(DropTargetDragEvent dtde) {
        ExplorerDnDManager.getDefault().setMaybeExternalDragAndDrop( true );

        int dropAction = dtde.getDropAction();
        int allowedDropActions = view.getAllowedDropActions();
        
        dropAction = ExplorerDnDManager.getDefault().getAdjustedDropAction(
                dropAction, allowedDropActions);

        // 1. test if I'm over any node
        TreePath tp = getTreePath(dtde, dropAction);
        Node dropNode;
        
        // 2. find node for drop
        Point p = dtde.getLocation();
        if (tp == null) {
            // #64469: Can't drop into empty explorer area
            dropNode = view.manager.getRootContext ();
            if (canDrop(dropNode, dropAction, dtde.getTransferable())) {
                // ok, root accept
                dtde.acceptDrag(dropAction);
            } else {
                dtde.rejectDrag();
            }
            return ;
        } else {
            dropNode = getNodeForDrop(p);
        }

        // if I haven't any node for drop then reject drop
        if (dropNode == null) {
            dropIndex = -1;
            dtde.rejectDrag();
            removeDropLine();

            return;
        }

        Rectangle nodeArea = tree.getPathBounds(tp);
        int endPointX = nodeArea.x + nodeArea.width;
        int row = tree.getRowForPath(tp);

        if (nodeArea != null) {
            pointAt = DragDropUtilities.NODE_CENTRAL;

            if (p.y <= (nodeArea.y + FUSSY_POINTING)) {
                // don't get line above root
                if (row != 0 || !view.isRootVisible()) {
                    // point above node
                    pointAt = DragDropUtilities.NODE_UP;

                    TreePath upPath = tree.getPathForRow(row - 1);

                    if ((upPath != null) && !upPath.equals(tp)) {
                        endPointX = Math.max(
                                nodeArea.x + nodeArea.width,
                                tree.getPathBounds(upPath).x + tree.getPathBounds(upPath).width
                            );
                    }

                    // drop candidate is parent
                    if (dropNode.getParentNode() != null) {
                        dropNode = dropNode.getParentNode();
                        tp = null;
                    }
                }
            } else if (p.y >= ((nodeArea.y + nodeArea.height) - FUSSY_POINTING)) {
                // exclude expanded folder
                if (!view.isExpanded(dropNode)) {
                    // point bellow node
                    pointAt = DragDropUtilities.NODE_DOWN;

                    TreePath downPath = tree.getPathForRow(row + 1);

                    if ((downPath != null) && !downPath.equals(tp)) {
                        endPointX = Math.max(
                                nodeArea.x + nodeArea.width,
                                tree.getPathBounds(downPath).x + tree.getPathBounds(downPath).width
                            );
                    }

                    // drop candidate is parent
                    if (dropNode.getParentNode() != null) {
                        dropNode = dropNode.getParentNode();
                        tp = null;
                    }
                }
            }
        }

        endPointX = endPointX + SHIFT_RIGHT;

        // 2.b. check index cookie
        Index indexCookie = dropNode.getCookie(Index.class);

        if (indexCookie != null) {
            if (pointAt == DragDropUtilities.NODE_UP) {
                lowerNodeIdx = indexCookie.indexOf(getNodeForDrop(p));
                upperNodeIdx = lowerNodeIdx - 1;
            } else if (pointAt == DragDropUtilities.NODE_DOWN) {
                upperNodeIdx = indexCookie.indexOf(getNodeForDrop(p));
                lowerNodeIdx = upperNodeIdx + 1;
            }
            dropIndex = lowerNodeIdx;
        }
        if( dropNode == getNodeForDrop(p) )
            dropIndex = -1;

        // 3. expand with a delay
        if (
            ((timer == null) || !timer.isRunning()) && (dropNode != null) && !dropNode.isLeaf() &&
                !view.isExpanded(dropNode)
        ) {
            // ok, let's expand in a while
            // node is candidate for expand
            final Node cn = dropNode;

            // remove old timer
            removeTimer();

            // create new timer
            timer = new Timer(
                    DELAY_TIME_FOR_EXPAND,
                    new ActionListener() {
                        public final void actionPerformed(ActionEvent e) {
                            view.expandNode(cn);
                        }
                    }
                );
            timer.setRepeats(false);
            timer.start();
        }

        // 4. present node for drop
        // prepare selection or line
        if (pointAt == DragDropUtilities.NODE_CENTRAL) {
            // no line
            if( null != dropPane )
                dropPane.setDropLine(null);
        } else {
            // line and selection of parent if any
            if (pointAt == DragDropUtilities.NODE_UP) {
                Line2D line = new Line2D.Double(
                        nodeArea.x - SHIFT_LEFT, nodeArea.y + SHIFT_DOWN, endPointX, nodeArea.y + SHIFT_DOWN
                    );
                convertBoundsAndSetDropLine(line);

                // enlagre node area with area for line
                Rectangle lineArea = new Rectangle(
                        nodeArea.x - SHIFT_LEFT, (nodeArea.y + SHIFT_DOWN) - 3, endPointX - nodeArea.x + SHIFT_LEFT, 5
                    );
                nodeArea = (Rectangle) nodeArea.createUnion(lineArea);
            } else {
                Line2D line = new Line2D.Double(
                        nodeArea.x - SHIFT_LEFT, nodeArea.y + nodeArea.height + SHIFT_DOWN, endPointX,
                        nodeArea.y + nodeArea.height + SHIFT_DOWN
                    );
                convertBoundsAndSetDropLine(line);

                // enlagre node area with area for line
                Rectangle lineArea = new Rectangle(
                        nodeArea.x - SHIFT_LEFT, nodeArea.y + nodeArea.height, endPointX - nodeArea.x + SHIFT_LEFT,
                        SHIFT_DOWN + 3
                    );
                nodeArea = (Rectangle) nodeArea.createUnion(lineArea);
            }

            // the parent node won't be selected

            /*// select parent and enlarge paint area
            if (tp.getParentPath ()!=null) {
                tp = tp.getParentPath ();
            }
            nodeArea = (Rectangle)nodeArea.createUnion (tree.getPathBounds (tp));*/
        }

        // back normal view w/o any selecetion nor line
        if ((lastNodeArea != null) && (!lastNodeArea.equals(nodeArea))) {
            NodeRenderer.dragExit();
            repaint(lastNodeArea);
        }

        // paint new state
        if (!nodeArea.equals(lastNodeArea)) {
            if (tp != null) {
                NodeRenderer.dragEnter(tp.getLastPathComponent());
            }

            repaint(nodeArea);
            lastNodeArea = nodeArea;
            removeTimer();
        }

        // 5. show to cursor belong to state
        if (canDrop(dropNode, dropAction, dtde.getTransferable())) {
            // ok, can accept
            dtde.acceptDrag(dropAction);
        } else {
            // can only reorder?
            Node[] draggedNodes = ExplorerDnDManager.getDefault().getDraggedNodes();
            if( null != draggedNodes && canReorderWhenMoving(dropNode, draggedNodes) ) {
                // ok, can accept only reoder
                dtde.acceptDrag(dropAction);
            } else {
                dtde.rejectDrag();
            }
        }
    }

    /** Repaints TreeView, the given rectangle is enlarged for 5 pixels
     * because some parts was not repainted correctly.
     * @param Rectangle r rectangle which will be repainted.*/
    private void repaint(Rectangle r) {
        tree.repaint(r.x - 5, r.y - 5, r.width + 10, r.height + 10);
    }

    /** Converts line's bounds by the bounds of the drop glass pane.
     * After covert a given line is set to drop glass pane.
     * @param line line for show in drop glass pane */
    private void convertBoundsAndSetDropLine(final Line2D line) {
        if (dropPane == null)
            return;

        int x1 = (int) line.getX1();
        int x2 = (int) line.getX2();
        int y1 = (int) line.getY1();
        int y2 = (int) line.getY2();
        Point p1 = SwingUtilities.convertPoint(tree, x1, y1, dropPane);
        Point p2 = SwingUtilities.convertPoint(tree, x2, y2, dropPane);
        line.setLine(p1, p2);
        dropPane.setDropLine(line);
    }

    /** Removes timer and all listeners. */
    private void removeTimer() {
        if (timer != null) {
            ActionListener[] l = timer.getListeners(ActionListener.class);

            for (int i = 0; i < l.length; i++) {
                timer.removeActionListener(l[i]);
            }

            timer.stop();
            timer = null;
        }
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        // check if the nodes are willing to do selected action
        Node[] nodes = ExplorerDnDManager.getDefault().getDraggedNodes();
        if( null != nodes ) {
            int dropAction = ExplorerDnDManager.getDefault().getAdjustedDropAction(
                    dtde.getDropAction(), view.getAllowedDropActions()
                );

            for (int i = 0; i < nodes.length; i++) {
                if (
                    ((view.getAllowedDropActions() & dropAction) == 0) ||
                        !DragDropUtilities.checkNodeForAction(nodes[i], dropAction)
                ) {
                    // this action is not supported
                    dtde.rejectDrag();

                    return;
                }
            }
        }

        return;
    }

    /** User exits the dragging */
    public void dragExit(DropTargetEvent dte) {
        dropIndex = -1;
        
        ExplorerDnDManager.getDefault().setMaybeExternalDragAndDrop( false );
        stopDragging();
    }

    private void removeDropLine() {
        if( null != dropPane ) {
            dropPane.setDropLine(null);
        }

        if (lastNodeArea != null) {
            NodeRenderer.dragExit();
            repaint(lastNodeArea);
            lastNodeArea = null;
        }
    }

    private void stopDragging() {
        removeDropLine();
        removeTimer();

        // set back the remembered glass pane
        if (DropGlassPane.isOriginalPaneStored()) {
            DropGlassPane.putBackOriginal();
        }
    }

    /** Get a node on given point or null if there none*/
    private Node getNodeForDrop(Point p) {
        if (p != null) {
            TreePath tp = tree.getPathForLocation(p.x, p.y);
            if( null == tp ) {
                //make the drop area a bit bigger at the end of the tree
                tp = tree.getPathForLocation(p.x, p.y-tree.getRowHeight()/2);
            }

            if (tp != null) {
                return DragDropUtilities.secureFindNode(tp.getLastPathComponent());
            }
        }

        return null;
    }

    private boolean canReorderWhenMoving(Node folder, Node[] dragNodes) {
        if ((ExplorerDnDManager.getDefault().getNodeAllowedActions() & DnDConstants.ACTION_MOVE) == 0) {
            return false;
        }
        return canReorder( folder, dragNodes );
    }

    private boolean canReorder(Node folder, Node[] dragNodes) {
        if ((folder == null) || (dragNodes.length == 0)) {
            return false;
        }

        // has folder a index cookie?
        Index ic = folder.getCookie(Index.class);

        if (ic == null) {
            return false;
        }

        // folder has index cookie
        // check if all dragNodes are from same folder
        for (int i = 0; i < dragNodes.length; i++) {
            // bugfix #23988, check if dragNodes[i] isn't null
            if (dragNodes[i] == null) {
                return false;
            }

            if (dragNodes[i].getParentNode() == null) {
                return false;
            }

            if (!dragNodes[i].getParentNode().equals(folder)) {
                return false;
            }
        }

        return true;
    }

    private void performReorder(final Node folder, Node[] dragNodes, int lNode, int uNode) {
        try {
            Index indexCookie = folder.getCookie(Index.class);

            if (indexCookie != null) {
                int[] perm = new int[indexCookie.getNodesCount()];
                int[] indexes = new int[dragNodes.length];
                int indexesLength = 0;

                for (int i = 0; i < dragNodes.length; i++) {
                    int idx = indexCookie.indexOf(dragNodes[i]);

                    if ((idx >= 0) && (idx < perm.length)) {
                        indexes[indexesLength++] = idx;
                    }
                }

                // XXX: normally indexes of dragged nodes should be in ascending order, but
                // it seems that Tree.getSelectionPaths doesn't keep this order
                Arrays.sort(indexes);

                if ((lNode < 0) || (uNode >= perm.length) || (indexesLength == 0)) {
                    return;
                }

                int k = 0;

                for (int i = 0; i < perm.length; i++) {
                    if (i <= uNode) {
                        if (!containsNumber(indexes, indexesLength, i)) {
                            perm[i] = k++;
                        }

                        if (i == uNode) {
                            for (int j = 0; j < indexesLength; j++) {
                                if (indexes[j] <= uNode) {
                                    perm[indexes[j]] = k++;
                                }
                            }
                        }
                    } else {
                        if (i == lNode) {
                            for (int j = 0; j < indexesLength; j++) {
                                if (indexes[j] >= lNode) {
                                    perm[indexes[j]] = k++;
                                }
                            }
                        }

                        if (!containsNumber(indexes, indexesLength, i)) {
                            perm[i] = k++;
                        }
                    }
                }

                // check for identity permutation
                for (int i = 0; i < perm.length; i++) {
                    if (perm[i] != i) {
                        indexCookie.reorder(perm);

                        break;
                    }
                }
            }
        } catch (Exception e) {
            // Pending: add annotation or remove try/catch block
            Logger.getLogger(TreeViewDropSupport.class.getName()).log(Level.WARNING, null, e);
        }
    }

    private boolean containsNumber(int[] arr, int arrLength, int n) {
        for (int i = 0; i < arrLength; i++) {
            if (arr[i] == n) {
                return true;
            }
        }

        return false;
    }

    private Node[] findDropedNodes(Node folder, Node[] dragNodes) {
        if ((folder == null) || (dragNodes.length == 0)) {
            return null;
        }

        Node[] dropNodes = new Node[dragNodes.length];
        Children children = folder.getChildren();

        for (int i = 0; i < dragNodes.length; i++) {
            dropNodes[i] = children.findChild(dragNodes[i].getName());
        }

        return dropNodes;
    }

    /** Can node recieve given drop action? */

    // XXX canditate for more general support
    private boolean canDrop(Node n, int dropAction, Transferable dndEventTransferable) {
        if (n == null) {
            return false;
        }

        // Test to see if the target node supports the drop action
        if ((view.getAllowedDropActions() & dropAction) == 0) {
            return false;
        }

        // test if a parent of the dragged nodes isn't the node over
        // only for MOVE action
        if ((DnDConstants.ACTION_MOVE & dropAction) != 0) {
            Node[] nodes = ExplorerDnDManager.getDefault().getDraggedNodes();

            if (nodes != null) {
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
            if( null == trans ) {
                return false;
            }
        }

        // get paste types for given transferred transferable
        PasteType pt = DragDropUtilities.getDropType(n, trans, dropAction, dropIndex);

        return (pt != null);
    }

    /** Performs the drop action, if we are dropping on
    * right node and target node agrees.
    */
    public void drop(DropTargetDropEvent dtde) {
        boolean dropResult = true;
        try {
            stopDragging();

            // find node for the drop perform
            Node dropNode = getNodeForDrop(dtde.getLocation());

            // #64469: Can't drop into empty explorer area
            if (dropNode == null) {
                dropNode = view.manager.getRootContext ();
            } else if (pointAt != DragDropUtilities.NODE_CENTRAL) {
                dropNode = dropNode.getParentNode();
            }

            Node[] dragNodes = ExplorerDnDManager.getDefault().getDraggedNodes();
            int dropAction = ExplorerDnDManager.getDefault().getAdjustedDropAction(
                    dtde.getDropAction(), view.getAllowedDropActions()
                );

            ExplorerDnDManager.getDefault().setMaybeExternalDragAndDrop( false );

            // finally perform the drop
            if( dropAction != DnDConstants.ACTION_NONE ) {
                dtde.acceptDrop(dropAction);
            }

            if (!canDrop(dropNode, dropAction, dtde.getTransferable())) {
                if( null != dragNodes && canReorderWhenMoving(dropNode, dragNodes)) {
                    performReorder(dropNode, dragNodes, lowerNodeIdx, upperNodeIdx);
                } else {
                    dropResult = false;
                }

                return;
            }

            if (DnDConstants.ACTION_LINK == dropAction && null != dragNodes) {
                // construct all paste types
                PasteType[] ptCut = new PasteType[] {  };

                // construct all paste types
                PasteType[] ptCopy = new PasteType[] {  };

                // do not try get paste types for move if MOVE is not allowed
                if ((ExplorerDnDManager.getDefault().getNodeAllowedActions() & DnDConstants.ACTION_MOVE) != 0) {
                    ptCut = DragDropUtilities.getPasteTypes(
                            dropNode, ExplorerDnDManager.getDefault().getDraggedTransferable(true)
                        );
                }

                // do not try get paste types for copy if COPY is not allowed
                if ((ExplorerDnDManager.getDefault().getNodeAllowedActions() & DnDConstants.ACTION_COPY) != 0) {
                    ptCopy = DragDropUtilities.getPasteTypes(
                            dropNode, ExplorerDnDManager.getDefault().getDraggedTransferable(false)
                        );
                }

                TreeSet<PasteType> setPasteTypes = new TreeSet<PasteType>(
                        new Comparator<PasteType>() {
                            public int compare(PasteType obj1, PasteType obj2) {
                                return obj1.getName().compareTo(obj2.getName());

                            }
                        }
                    );

                for (int i = 0; i < ptCut.length; i++) {
                    //System.out.println(ptCut[i].getName()+", "+System.identityHashCode(ptCut[i]));
                    setPasteTypes.add(ptCut[i]);
                }

                for (int i = 0; i < ptCopy.length; i++) {
                    //System.out.println(ptCopy[i].getName()+", "+System.identityHashCode(ptCopy[i]));
                    setPasteTypes.add(ptCopy[i]);
                }

                DragDropUtilities.createDropFinishPopup(setPasteTypes).show(
                    tree, Math.max(dtde.getLocation().x - 5, 0), Math.max(dtde.getLocation().y - 5, 0)
                );

                // reorder have to be perform
                if (canReorder(dropNode, dragNodes)) {
                    final Node tempDropNode = dropNode;
                    final int tmpUpper = upperNodeIdx;
                    final int tmpLower = lowerNodeIdx;
                    final Node[] tempDragNodes = dragNodes;
                    DragDropUtilities.setPostDropRun(
                        new Runnable() {
                            public void run() {
                                performReorder(
                                    tempDropNode, findDropedNodes(tempDropNode, tempDragNodes), tmpLower, tmpUpper
                                );
                            }
                        }
                    );
                }
            } else if( dropAction != DnDConstants.ACTION_LINK ) {
                // get correct paste type
                Transferable t = ExplorerDnDManager.getDefault().getDraggedTransferable( (DnDConstants.ACTION_MOVE & dropAction) != 0 );
                if( null == t ) {
                    t = dtde.getTransferable();
                }
                PasteType pt = DragDropUtilities.getDropType( dropNode, t, dropAction, dropIndex );

                //remember the Nodes before the drop
                final Node[] preNodes = dropNode.getChildren().getNodes( true );
                final Node parentNode = dropNode;

                Node[] diffNodes = DragDropUtilities.performPaste(pt, dropNode);

                if( null != ExplorerDnDManager.getDefault().getDraggedTransferable( (DnDConstants.ACTION_MOVE & dropAction) != 0 ) )
                    ExplorerDnDManager.getDefault().setDraggedNodes(diffNodes);

                if (dropIndex != -1) {
                    //postpone the potential re-order so that the drop Node has enough
                    //time to re-create its children
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            Node[] diffNodes = getDiffNodes( parentNode, preNodes );
                            if( canReorder( parentNode, diffNodes ) ) {
                                performReorder( parentNode, diffNodes, lowerNodeIdx, upperNodeIdx );
                            }
                        }
                    });
                }
            }

            TreeCellEditor tce = tree.getCellEditor();

            if (tce instanceof TreeViewCellEditor) {
                ((TreeViewCellEditor) tce).setDnDActive(false);
            }
        } finally {
            // finished
            dtde.dropComplete( dropResult );
        }
    }

    private Node[] getDiffNodes( Node parent, Node[] childrenBefore ) {
        Node[] childrenCurrent = parent.getChildren().getNodes(true);

        // calculate new nodes
        List<Node> pre = Arrays.asList(childrenBefore);
        List<Node> post = Arrays.asList(childrenCurrent);
        Iterator<Node> it = post.iterator();
        List<Node> diff = new ArrayList<Node>();

        while (it.hasNext()) {
            Node n = it.next();

            if (!pre.contains(n)) {
                diff.add(n);
            }
        }

        return diff.toArray(new Node[0]);
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
    TreePath getTreePath(DropTargetDragEvent dtde, int dropAction) {
        // check location
        Point location = dtde.getLocation();
        TreePath tp = tree.getPathForLocation(location.x, location.y);
        if( null == tp ) {
            //make the drop area a bit bigger at the end of the tree
            tp = tree.getPathForLocation(location.x, location.y-tree.getRowHeight()/2);
        }

        return ((tp != null) && (DragDropUtilities.secureFindNode(tp.getLastPathComponent()) != null)) ? tp : null;
    }

    /** Safe accessor to the drop target which is asociated
    * with the tree */
    DropTarget getDropTarget() {
        if (dropTarget == null) {
            dropTarget = new DropTarget(tree, view.getAllowedDropActions(), this, false);
        }

        return dropTarget;
    }
}
 /* end class TreeViewDropSupport */
