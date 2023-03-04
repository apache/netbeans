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

package org.netbeans.lib.profiler.ui.components.treetable;

import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.ui.components.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;


public abstract class AbstractTreeTableModel extends DefaultTableModel implements TreeTableModel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    /**
     * The root of the tree.
     */
    protected CCTNode root;
    protected boolean initialSortingOrder;
    protected boolean supportsSorting;
    protected int initialSortingColumn;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new AbstractTreeTableModel which supports sorting.
     */
    public AbstractTreeTableModel(CCTNode root) {
        this(root, false, -1, false);
    }

    public AbstractTreeTableModel(CCTNode root, int sortingColumn, boolean sortingOrder) {
        this(root, true, sortingColumn, sortingOrder);
    }

    public AbstractTreeTableModel(CCTNode root, boolean supportsSorting, int sortingColumn, boolean sortingOrder) {
        super();
        this.root = root;
        this.supportsSorting = supportsSorting;
        this.initialSortingColumn = sortingColumn;
        this.initialSortingOrder = sortingOrder;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the child at index 'num' of the given node.
     * <p/>
     * Although the method expects an Object because of the TreeModel contract,
     * in reality it assumes the given node is a 'TreeTableNode'.
     *
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(Object node, int num) {
        return ((CCTNode) node).getChild(num);
    }

    /**
     * Returns the number of children a given node has.
     * <p/>
     * Although the method expects an Object because of the TreeModel contract,
     * in reality it assumes the given node is a 'TreeTableNode'.
     *
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount(Object node) {
        return ((CCTNode) node).getNChildren();
    }

    /**
     * Returns the column class for column <code>column</code>. This is set
     * in the constructor.
     */
    public abstract Class getColumnClass(int column);

    /**
     * Returns the number of column names passed into the constructor.
     */
    public abstract int getColumnCount();

    /**
     * Returns the column name passed into the constructor.
     */
    public abstract String getColumnName(int column);

    public String getColumnToolTipText(int column) {
        return null;
    }

    /**
     * Returns the index of the child node in the parent node.
     * <p/>
     * Although the method expects Objects because of the TreeModel contract,
     * it assumes both parent and child are TreeTableNodes.
     */
    public int getIndexOfChild(Object parent, Object child) {
        if ((parent == null) || (child == null)) {
            return -1;
        }

        return ((CCTNode) parent).getIndexOfChild(child);
    }

    /**
     * This method should be overridden for TreeTableModel descendant which supports sorting.
     *
     * @param column The table column index
     * @return Initial sorting for the specified column - if true, ascending, if false descending
     */
    public boolean getInitialSorting(int column) {
        return false;
    }

    public int getInitialSortingColumn() {
        return initialSortingColumn;
    }

    public boolean getInitialSortingOrder() {
        return initialSortingOrder;
    }

    /**
     * Returns true when the given node is a 'leaf'.
     * <p/>
     * Although the method expects an Object because of the TreeModel contract,
     * in reality it assumes the given node is a 'TreeTableNode'.
     *
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    public boolean isLeaf(Object node) {
        return ((CCTNode) node).getNChildren() == 0;
    }

    public void setRoot(Object root) {
        this.root = (CCTNode) root;
    }

    /**
     * Returns the root node.
     */
    public Object getRoot() {
        return root;
    }

    /**
     * Returns the value for the column <code>column</code> and object <code>node</code>.
     * The return value is determined by invoking the method specified in
     * constructor for the passed in column.
     */
    public abstract Object getValueAt(Object node, int column);

    /**
     * Returns true if there is a setter method name for column <code>column</code>.
     * This is set in the constructor.
     */
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    /**
     * Builds the parents of the node up to and including the root node, where
     * the original node is the last element in the returned array. The length
     * of the returned array gives the node's depth in the tree.
     *
     * @param aNode the TreeNode to get the path for
     */
    public CCTNode[] getPathToRoot(CCTNode aNode) {
        return getPathToRoot(aNode, 0);
    }

    /**
     * Sets the value to <code>aValue</code> for the object <code>node</code>
     * in column <code>column</code>. This is done by using the setter
     * method name, and coercing the passed in value to the specified type.
     */

    // Note: This looks up the methods each time! This is rather inefficient;
    // it should really be changed to cache matching methods/constructors
    // based on <code>node</code>'s class, and <code>aValue</code>'s class.
    public void setValueAt(Object aValue, Object node, int column) {
    }

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     */
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     *
     * @see javax.swing.event.EventListenerList
     */
    public void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices, children);
                }

                ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
            }
        }
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     *
     * @see javax.swing.event.EventListenerList
     */
    public void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices, children);
                }

                ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
            }
        }
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     *
     * @see javax.swing.event.EventListenerList
     */
    public void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices, children);
                }

                ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
            }
        }
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     *
     * @see javax.swing.event.EventListenerList
     */
    public void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices, children);
                }

                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }

    /**
     * Removes a listener previously added with addTreeModelListener().
     */
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    /**
     * This method should be overridden for TreeTableModel descendant which supports sorting.
     *
     * @param column The table column index
     * @param order sorting for the specified column - if true, ascending, if false descending
     */
    public void sortByColumn(int column, boolean order) {
    }

    public boolean supportsSorting() {
        return supportsSorting;
    }

    /**
     * Overwrite if you are going to user editors in the JTree.
     * <p/>
     * The default implementation does nothing (dummy method).
     *
     * @see javax.swing.tree.DefaultTreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    /**
     * Builds the parents of the node up to and including the root node, where
     * the original node is the last element in the returned array. The length
     * of the returned array gives the node's depth in the tree.
     *
     * @param aNode the TreeNode to get the path for
     * @param depth an int giving the number of steps already taken towards the
     *              root (on recursive calls), used to size the returned array
     * @return an array of TreeNodes giving the path from the root to the
     *         specified node
     */
    private CCTNode[] getPathToRoot(CCTNode aNode, int depth) {
        CCTNode[] retNodes;

        // This method recurses, traversing towards the root in order
        // size the array. On the way back, it fills in the nodes,
        // starting from the root and working back to the original node.

        /*
         * Check for null, in case someone passed in a null node, or
         */
        if (aNode == null) {
            if (depth == 0) {
                return null;
            } else {
                retNodes = new CCTNode[depth];
            }
        } else {
            depth++;

            if (aNode == root) {
                retNodes = new CCTNode[depth];
            } else {
                retNodes = getPathToRoot((CCTNode) aNode.getParent(), depth);
            }

            retNodes[retNodes.length - depth] = aNode;
        }

        return retNodes;
    }
}
