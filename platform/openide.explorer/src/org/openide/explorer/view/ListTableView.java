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

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TableSheet.ControlledTableView;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


/** Explorer view. Allows to view list of nodes on the left
 * and its properties in table on the right.
 *
 * @deprecated Use <code>org.openide.explorer.view.TreeTableView</code> instead.
 *
 * @author  jrojcek
 * @since 1.7
 */
public @Deprecated class ListTableView extends ListView {
    /** flag that allows group more requests into one */
    private boolean tableChanging = false;

    /** manager is used only for synchronization of header name */
    private ExplorerManager manager;

    /** listener on explorer manager */
    private PropertyChangeListener pchl;

    /** table view that is controlled by list view */
    private ControlledTableView controlledTableView;

    /** listener on changes in list model */
    private Listener listener;

    /** preferred size*/
    private Dimension prefSize;

    /** table  */
    private JTable table;

    /** Create ListTableView with default NodeTableModel
     */
    public ListTableView() {
        this(null);
    }

    /** Creates ListTableView with provided NodeTableModel.
     * @param ntm node table model
     */
    public ListTableView(NodeTableModel ntm) {
        // do not use scroll bars, this scroll pane is dummy, only for border painting
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // remove list from viewportview
        setViewportView(null);

        // insert list into new scrollpane
        JScrollPane listView = new JScrollPane(list);
        listView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        listView.setBorder(null);

        // create table view controlled by new scrollpane
        controlledTableView = (ntm == null) ? new TableSheet.ControlledTableView(listView)
                                            : new TableSheet.ControlledTableView(listView, ntm);
        setViewportView(controlledTableView.compoundScrollPane());

        listener = new Listener();
        delayedFireTableDataChanged();

        setPreferredSize(new Dimension(400, 400));

        table = controlledTableView.getTable();
    }

    /** Set columns.
     * @param props each column is constructed from Node.Property
     */
    public void setProperties(Property[] props) {
        controlledTableView.setProperties(props);
    }

    /** Sets resize mode of table.
     *
     * @param mode - One of 5 legal values: <pre>JTable.AUTO_RESIZE_OFF,
     *                                           JTable.AUTO_RESIZE_NEXT_COLUMN,
     *                                           JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS,
     *                                           JTable.AUTO_RESIZE_LAST_COLUMN,
     *                                           JTable.AUTO_RESIZE_ALL_COLUMNS</pre>
     */
    public final void setTableAutoResizeMode(int mode) {
        controlledTableView.setAutoResizeMode(mode);
    }

    /** Gets resize mode of table.
     *
     * @return mode - One of 5 legal values: <pre>JTable.AUTO_RESIZE_OFF,
     *                                           JTable.AUTO_RESIZE_NEXT_COLUMN,
     *                                           JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS,
     *                                           JTable.AUTO_RESIZE_LAST_COLUMN,
     *                                           JTable.AUTO_RESIZE_ALL_COLUMNS</pre>
     */
    public final int getTableAutoResizeMode() {
        return controlledTableView.getAutoResizeMode();
    }

    /** Sets preferred width of table column
     * @param index column index
     * @param width preferred column width
     */
    public final void setTableColumnPreferredWidth(int index, int width) {
        controlledTableView.setColumnPreferredWidth(index, width);
    }

    /** Gets preferred width of table column
     * @param index column index
     * @return preferred column width
     */
    public final int getTableColumnPreferredWidth(int index) {
        return controlledTableView.getColumnPreferredWidth(index);
    }

    /** Set preferred width of list view
     * @param width preferred width
     */
    public void setListPreferredWidth(int width) {
        controlledTableView.setControllingViewWidth(width);

        Dimension dim = getPreferredSize();

        //        controlledTableView.setPreferredSize(new Dimension(dim.width - width, dim.height));
        table.setPreferredScrollableViewportSize(new Dimension(dim.width - width, dim.height));
    }

    /** Get preferred size of list view
     * @return preferred width of list view
     */
    public final int getListPreferredWidth() {
        return controlledTableView.getControllingViewWidth();
    }

    @Override
    public void setPreferredSize(Dimension dim) {
        super.setPreferredSize(dim);
        prefSize = dim;
    }

    @Override
    public Dimension getPreferredSize() {
        return prefSize;
    }

    @Override
    public void addNotify() {
        super.addNotify();

        ExplorerManager newManager = ExplorerManager.find(this);

        if (newManager != manager) {
            if (manager != null) {
                manager.removePropertyChangeListener(pchl);
            }

            manager = newManager;
            manager.addPropertyChangeListener(pchl = org.openide.util.WeakListeners.propertyChange(listener, manager));
            controlledTableView.setHeaderText(manager.getExploredContext().getDisplayName());
        }

        list.getModel().addListDataListener(listener);
        list.addFocusListener(listener);
        delayedFireTableDataChanged();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        list.getModel().removeListDataListener(listener);
        list.removeFocusListener(listener);

        // clear node listeners
        controlledTableView.setNodes(new Node[] {  });
    }

    /** Chnage table data in awt thread */
    private void delayedFireTableDataChanged() {
        if (tableChanging) {
            return;
        }

        tableChanging = true;
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    if (list.getCellBounds(0, 0) != null) {
                        controlledTableView.setRowHeight(list.getCellBounds(0, 0).height);
                    }

                    changeTableModel();
                    tableChanging = false;
                }
            }
        );
    }

    /** Change table model. Sets rows (nodes) of table mmodel */
    private void changeTableModel() {
        Node[] nodes = new Node[list.getModel().getSize()];

        for (int i = 0; i < list.getModel().getSize(); i++) {
            nodes[i] = Visualizer.findNode(list.getModel().getElementAt(i));
        }

        controlledTableView.setNodes(nodes);
    }

    /** Listenes on changes in list model.
     */
    private class Listener implements PropertyChangeListener, FocusListener, ListDataListener {
        Listener() {
        }

        /**
         * Sent after the indices in the index0,index1
         * interval have been inserted in the data model.
         * The new interval includes both index0 and index1.
         *
         * @param e  a ListDataEvent encapuslating the event information
         */
        public void intervalAdded(ListDataEvent e) {
            delayedFireTableDataChanged();
        }

        /**
         * Sent after the indices in the index0,index1 interval
         * have been removed from the data model.  The interval
         * includes both index0 and index1.
         *
         * @param e  a ListDataEvent encapuslating the event information
         */
        public void intervalRemoved(ListDataEvent e) {
            delayedFireTableDataChanged();
        }

        /**
         * Sent when the contents of the list has changed in a way
         * that's too complex to characterize with the previous
         * methods.  Index0 and index1 bracket the change.
         *
         * @param e  a ListDataEvent encapuslating the event information
         */
        public void contentsChanged(ListDataEvent e) {
            delayedFireTableDataChanged();
        }

        public void focusGained(FocusEvent evt) {
        }

        public void focusLost(FocusEvent evt) {
            if (evt.isTemporary()) {
                return;
            }

            int selectedRow = list.getSelectedIndex();
            table.getSelectionModel().setAnchorSelectionIndex(selectedRow);
            table.getColumnModel().getSelectionModel().setAnchorSelectionIndex(0);
        }

        /** Updates header name.
         * @param evt event
         */
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                controlledTableView.setHeaderText(manager.getExploredContext().getDisplayName());
            }
        }
    }

    /*
        public static void main (String[] args) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Node n = new org.netbeans.core.ModuleNode();
                    ExplorerManager em = new ExplorerManager();
                    em.setRootContext(n);
                    ListTableView ttv = new ListTableView();
    //                ttv.model.setDepth(2);
    //                ttv.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
                    ttv.setProperties(
    //                    n.getChildren().getNodes()[0].getPropertySets()[0].getProperties());
                        new Node.Property[]{
                            new PropertySupport.ReadWrite (
                                "enabled", // NOI18N
                                Boolean.TYPE,
                                org.openide.util.NbBundle.getMessage (org.netbeans.core.Main.class, "PROP_modules_enabled"),
                                org.openide.util.NbBundle.getMessage (org.netbeans.core.Main.class, "HINT_modules_enabled")
                            ) {
                                public Object getValue () {
                                    return null;
                                }

                                public void setValue (Object o) {
                                }
                            },
                            new PropertySupport.ReadOnly (
                                "specVersion", // NOI18N
                                String.class,
                                org.openide.util.NbBundle.getMessage (org.netbeans.core.Main.class, "PROP_modules_specversion"),
                                org.openide.util.NbBundle.getMessage (org.netbeans.core.Main.class, "HINT_modules_specversion")
                            ) {
                                public Object getValue () {
                                    return null;
                                }

                            }
                        }
                    );

                    org.openide.explorer.ExplorerPanel ep = new org.openide.explorer.ExplorerPanel (em);
                    ep.setLayout (new java.awt.BorderLayout ());
                    ep.add ("Center", ttv);
                    ep.open ();
                }
            });
        }
    */
}
