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

import org.openide.nodes.*;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import javax.swing.tree.TreePath;


/* NodeTableModel synchronizing tree and table model. Used by TreeTable.
 *
 * @author Jan Rojcek
 */
class TreeTableModelAdapter extends NodeTableModel {
    private JTree tree;
    private NodeTableModel nodeTableModel;

    public TreeTableModelAdapter(JTree t, NodeTableModel ntm) {
        this.tree = t;
        this.nodeTableModel = ntm;

        Listener listener = new Listener();
        tree.addTreeExpansionListener(listener);
        tree.getModel().addTreeModelListener(listener);
        nodeTableModel.addTableModelListener(listener);
    }

    // NodeTableModel methods
    @Override
    public void setNodes(Node[] nodes) {
        nodeTableModel.setNodes(nodes);
    }

    @Override
    public void setProperties(Node.Property[] props) {
        nodeTableModel.setProperties(props);
    }

    @Override
    protected Node.Property getPropertyFor(Node node, Node.Property prop) {
        return nodeTableModel.getPropertyFor(node, prop);
    }

    @Override
    Node nodeForRow(int row) {
        return Visualizer.findNode(tree.getPathForRow(row).getLastPathComponent());
    }

    @Override
    Node.Property propertyForColumn(int column) {
        return nodeTableModel.propertyForColumn(column - 1);
    }

    // Wrappers, implementing TableModel interface. 
    @Override
    public int getColumnCount() {
        return nodeTableModel.getColumnCount() + 1;
    }

    @Override
    public String getColumnName(int column) {
        return (column == 0) ? Visualizer.findNode(tree.getModel().getRoot()).getDisplayName()
                             : nodeTableModel.getColumnName(column - 1);
    }

    @Override
    public Class getColumnClass(int column) {
        return (column == 0) ? TreeTableModelAdapter.class : nodeTableModel.getColumnClass(column - 1);
    }

    @Override
    public int getRowCount() {
        return tree.getRowCount();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            TreePath path = tree.getPathForRow(row);
            if (path == null) {
                throw new IndexOutOfBoundsException("row " + row + " vs. count " + tree.getRowCount() + " with UI " + tree.getUI());
            }
            return path.getLastPathComponent();
        } else {
            return nodeTableModel.getPropertyFor(nodeForRow(row), propertyForColumn(column));
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 0) {
            return true;
        }

        Object o = getValueAt(row, column);

        if (o == null) {
            return false;
        }

        if (o instanceof Node.Property) {
            return ((Node.Property) o).canWrite();
        }

        return false;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
    }

    /* Listener for synchronizing tree and table model.
     */
    class Listener implements TreeExpansionListener, TreeModelListener, TableModelListener, Runnable {
        // selection paths stored for restore after update
        TreePath[] tps = null;

        // TreeExpansionListener
        public void treeExpanded(TreeExpansionEvent event) {
            updateNodes();
        }

        public void treeCollapsed(TreeExpansionEvent event) {
            updateNodes();
        }

        // TreeModelListener
        // Install a TreeModelListener that can update the table when
        // tree changes. We use delayedUpdateNodes as we can
        // not be guaranteed the tree will have finished processing
        // the event before us.
        public void treeNodesChanged(TreeModelEvent e) {
            delayedUpdateNodes(e);
        }

        public void treeNodesInserted(TreeModelEvent e) {
            delayedUpdateNodes(e);
        }

        public void treeNodesRemoved(TreeModelEvent e) {
            delayedUpdateNodes(e);
        }

        public void treeStructureChanged(TreeModelEvent e) {
            // bugfix #23757, store selection paths
            tps = tree.getSelectionPaths();

            // bugfix #30355, don't restore selection when the tree root changed
            // (see javadoc TreeModelListener.treeStructureChanged)
            if ((e.getPath().length == 1) && !e.getTreePath().equals(e.getPath()[0])) {
                tps = null;
            }

            delayedUpdateNodes(e);
        }

        // TableModelListener
        public void tableChanged(TableModelEvent e) {
            int c = e.getColumn();
            int column = (c == TableModelEvent.ALL_COLUMNS) ? TableModelEvent.ALL_COLUMNS : (c + 1);
            fireTableChanged(
                new TableModelEvent(TreeTableModelAdapter.this, e.getFirstRow(), e.getLastRow(), column, e.getType())
            );
        }

        /**
         * Invokes fireTableDataChanged after all the pending events have been processed.
         */
        protected void delayedUpdateNodes(TreeModelEvent e) {
            // Something like this can be used for updating tree column name ?!
            //if (tree.getModel().getRoot().equals(e.getTreePath().getLastPathComponent())) {
            //    fireTableStructureChanged();
            //}
            SwingUtilities.invokeLater(this);
        }

        public void run() {
            updateNodes();
        }

        private void updateNodes() {
            Node[] nodes = new Node[tree.getRowCount()];

            for (int i = 0; i < tree.getRowCount(); i++) {
                nodes[i] = Visualizer.findNode(tree.getPathForRow(i).getLastPathComponent());
            }

            setNodes(nodes);

            // retore selection paths
            if (tps != null) {
                tree.setSelectionPaths(tps);
                tps = null;
            }
        }
    }
}
