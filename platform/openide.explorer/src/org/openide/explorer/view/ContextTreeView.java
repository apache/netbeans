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

import org.openide.explorer.*;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

import java.awt.*;

import java.beans.*;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.tree.*;


/** A view displaying tree of {@link Node}s but not showing its leaf nodes.
 * Works well together (e.g. sharing one {@link ExplorerManager}) with {@link ListView}.
 *
 * <p>
 * This class is a <em>view</em>
 * to use it properly you need to add it into a component which implements
 * {@link Provider}. Good examples of that can be found 
 * in {@link ExplorerUtils}. Then just use 
 * {@link Provider#getExplorerManager} call to get the {@link ExplorerManager}
 * and control its state.
 * </p>
 * <p>
 * There can be multiple <em>views</em> under one container implementing {@link Provider}. Select from
 * range of predefined ones or write your own:
 * </p>
 * <ul>
 *      <li>{@link org.openide.explorer.view.BeanTreeView} - shows a tree of nodes</li>
 *      <li>{@link org.openide.explorer.view.ContextTreeView} - shows a tree of nodes without leaf nodes</li>
 *      <li>{@link org.openide.explorer.view.ListView} - shows a list of nodes</li>
 *      <li>{@link org.openide.explorer.view.IconView} - shows a rows of nodes with bigger icons</li>
 *      <li>{@link org.openide.explorer.view.ChoiceView} - creates a combo box based on the explored nodes</li>
 *      <li>{@link org.openide.explorer.view.TreeTableView} - shows tree of nodes together with a set of their {@link Property}</li>
 *      <li>{@link org.openide.explorer.view.MenuView} - can create a {@link JMenu} structure based on structure of {@link Node}s</li>
 * </ul>
 * <p>
 * All of these views use {@link ExplorerManager#find} to walk up the AWT hierarchy and locate the
 * {@link ExplorerManager} to use as a controler. They attach as listeners to
 * it and also call its setter methods to update the shared state based on the
 * user action. Not all views make sence together, but for example
 * {@link org.openide.explorer.view.ContextTreeView} and {@link org.openide.explorer.view.ListView} were designed to complement
 * themselves and behaves like windows explorer. The {@link org.openide.explorer.propertysheet.PropertySheetView}
 * for example should be able to work with any other view.
 * </p>
 *
 * @author   Petr Hamernik
 */
public class ContextTreeView extends TreeView {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8282594827988436813L;
    /** logger to find out why the ContextTreeView tests fail so randomly */
    static final Logger LOG = Logger.getLogger(ContextTreeView.class.getName());

    /** Constructor.
    */
    public ContextTreeView() {
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /* @return true if this TreeView accept the selected beans.
    */
    protected boolean selectionAccept(Node[] nodes) {
        if (nodes.length == 0) {
            return true;
        }

        Node parent = nodes[0].getParentNode();

        for (int i = 1; i < nodes.length; i++) {
            if (nodes[i].getParentNode() != parent) {
                return false;
            }
        }

        return true;
    }

    /* Called whenever the value of the selection changes.
    * @param listSelectionEvent the event that characterizes the change.
    */
    protected void selectionChanged(Node[] nodes, ExplorerManager man)
    throws PropertyVetoException {
        if (nodes.length > 0) {
            man.setExploredContext(nodes[0]);
        }

        man.setSelectedNodes(nodes);
    }

    /** Expand the given path and makes it visible.
    * @param path the path
    */
    protected void showPath(TreePath path) {
        tree.makeVisible(path);

        Rectangle rect = tree.getPathBounds(path);

        if (rect != null) {
            rect.width += rect.x;
            rect.x = 0;
            tree.scrollRectToVisible(rect);
        }

        tree.setSelectionPath(path);
    }

    /** Shows selection to reflect the current state of the selection in the explorer.
    *
    * @param paths array of paths that should be selected
    */
    protected void showSelection(TreePath[] paths) {
        if (paths.length == 0) {
            tree.setSelectionPaths(new TreePath[0]);
        } else {
            tree.setSelectionPath(paths[0].getParentPath());
        }
    }

    /** Permit use of explored contexts.
    *
    * @return <code>true</code> always
    */
    protected boolean useExploredContextMenu() {
        return true;
    }

    /** Create model.
    */
    protected NodeTreeModel createModel() {
        return new NodeContextModel();
    }

    /** Excludes leafs from the model.
     */
    static final class NodeContextModel extends NodeTreeModel {
        //
        // Event filtering
        //
        private int[] newIndices;
        private Object[] newChildren;

        public java.lang.Object getChild(java.lang.Object parent, int index) {
            int superCnt = super.getChildCount(parent);
            int myCnt = 0;

            for (int i = 0; i < superCnt; i++) {
                Object origChild = super.getChild(parent, i);
                Node n = Visualizer.findNode(origChild);

                if (!n.isLeaf()) {
                    if (myCnt++ == index) {
                        return origChild;
                    }
                }
            }

            return null;
        }

        public int getChildCount(java.lang.Object parent) {
            int superCnt = super.getChildCount(parent);
            int myCnt = 0;

            for (int i = 0; i < superCnt; i++) {
                Node n = Visualizer.findNode(super.getChild(parent, i));

                if (!n.isLeaf()) {
                    myCnt++;
                }
            }

            return myCnt;
        }

        public int getIndexOfChild(java.lang.Object parent, java.lang.Object child) {
            int superCnt = super.getChildCount(parent);
            int myCnt = 0;

            for (int i = 0; i < superCnt; i++) {
                Object origChild = super.getChild(parent, i);

                if (child.equals(origChild)) {
                    return myCnt;
                }

                Node n = Visualizer.findNode(origChild);

                if (!n.isLeaf()) {
                    myCnt++;
                }
            }

            return -1;
        }

        public boolean isLeaf(java.lang.Object node) {
            return false;
        }

        /** Filters given childIndices and children to contain only non-leafs
         * return true if there is still something changed.
         */
        private boolean filterEvent(Object[] path, int[] childIndices, Object[] children) {
            if (path.length == 1 && path[0] == root && childIndices == null) {
                return true;
            }
            assert (childIndices != null) && (children != null) : " ch: " + children + " indices: " + childIndices; // NOI18N
            assert children.length == childIndices.length : "They should be the same: " + children.length + " == " +
            childIndices.length; // NOI18N
            assert newChildren == null : "Children should be cleared: " + newChildren; // NOI18N
            assert newIndices == null : "indices should be cleared: " + newIndices; // NOI18N
            assert path.length > 0 : "Path has to be greater than zero " + path.length; // NOI18N

            VisualizerNode parent = (VisualizerNode) path[path.length - 1];

            int[] filter = new int[childIndices.length];
            int accepted = 0;

            for (int i = 0; i < childIndices.length; i++) {
                VisualizerNode n = (VisualizerNode) children[i];

                if (!n.isLeaf()) {
                    filter[accepted++] = i;
                }
            }

            if (accepted == 0) {
                return false;
            }

            newIndices = new int[accepted];
            newChildren = new Object[accepted];

            for (int i = 0; i < accepted; i++) {
                newChildren[i] = children[filter[i]];
                newIndices[i] = getIndexOfChild(parent, newChildren[i]);
            }

            return true;
        }

        /** Filters given childIndices and children to contain only non-leafs
         * return true if there is still something changed.
         */
        private boolean removalEvent(Object[] path, int[] childIndices, Object[] children) {
            assert (childIndices != null) && (children != null) : " ch: " + children + " indices: " + childIndices; // NOI18N
            assert children.length == childIndices.length : "They should be the same: " + children.length + " == " +
            childIndices.length; // NOI18N
            assert newChildren == null : "Children should be cleared: " + newChildren; // NOI18N
            assert newIndices == null : "indices should be cleared: " + newIndices; // NOI18N
            assert path.length > 0 : "Path has to be greater than zero " + path.length; // NOI18N

            VisualizerNode parent = (VisualizerNode) path[path.length - 1];

            int[] filter = new int[childIndices.length];
            int accepted = 0;

            for (int i = 0; i < childIndices.length; i++) {
                VisualizerNode n = (VisualizerNode) children[i];

                if (!n.isLeaf()) {
                    filter[accepted++] = i;
                }
            }

            if (accepted == 0) {
                return false;
            }

            newIndices = new int[accepted];
            newChildren = new Object[accepted];

            int size = getChildCount(parent);
            int index = 0;
            int myPos = 0;
            int actualI = 0;
            int i = 0;

            for (int pos = 0; pos < accepted;) {
                if (childIndices[index] <= i) {
                    VisualizerNode n = (VisualizerNode) children[index];

                    if (!n.isLeaf()) {
                        newIndices[pos] = myPos++;
                        newChildren[pos] = n;
                        pos++;
                    }

                    index++;
                } else {
                    VisualizerNode n = (VisualizerNode) getChild(parent, actualI++);

                    if ((n != null) && !n.isLeaf()) {
                        myPos++;
                    }
                }

                i++;
            }

            return true;
        }

        /* sends childIndices and children == null, no tranformation
        protected void fireTreeStructureChanged (Object source, Object[] path, int[] childIndices, Object[] children) {
            if (!filterEvent (childIndices, children)) return;
            super.fireTreeStructureChanged(source, path, newIndices, newChildren);
            newIndices = null;
            newChildren = null;
        }
         */
        @Override
        protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
            LOG.fine("fireTreeNodesRemoved"); // NOI18N
            if (!removalEvent(path, childIndices, children)) {
                LOG.fine("fireTreeNodesRemoved - exit"); // NOI18N
                return;
            }

            super.fireTreeNodesRemoved(source, path, newIndices, newChildren);
            newIndices = null;
            newChildren = null;
            LOG.fine("fireTreeNodesRemoved - end"); // NOI18N
        }

        @Override
        void nodesWereInsertedInternal(final VisualizerEvent ev) {
            TreeNode node = ev.getVisualizer();
            int[] childIndices = ev.getArray();
            Object[] path = getPathToRoot(node);
            fireTreeNodesInserted(this, path, childIndices, NodeTreeModel.computeChildren(ev));
        }

        @Override
        protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
            LOG.fine("fireTreeNodesInserted"); // NOI18N
            if (!filterEvent(path, childIndices, children)) {
                LOG.fine("fireTreeNodesInserted - exit"); // NOI18N
                return;
            }

            super.fireTreeNodesInserted(source, path, newIndices, newChildren);
            newIndices = null;
            newChildren = null;
            LOG.fine("fireTreeNodesInserted - end"); // NOI18N
        }

        @Override
        protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
            LOG.fine("fireTreeNodesChanged"); // NOI18N
            if (!filterEvent(path, childIndices, children)) {
                LOG.fine("fireTreeNodesChanged - exit"); // NOI18N
                return;
            }

            super.fireTreeNodesChanged(source, path, newIndices, newChildren);
            newIndices = null;
            newChildren = null;
            LOG.fine("fireTreeNodesChanged - end"); // NOI18N
        }
    }
     // end of NodeContextModel
}
