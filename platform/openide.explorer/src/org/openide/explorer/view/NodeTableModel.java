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

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

import java.awt.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;


/**
* Table model with properties (<code>Node.Property</code>) as columns and nodes (<code>Node</code>) as rows.
* It is used as model for displaying node properties in table. Each column is represented by
* <code>Node.Property</code> object. Each row is represented by <code>Node</code> object.
* Each cell contains <code>Node.Property</code> property which equals with column object
* and should be in property sets of row representant (<code>Node</code>).
*
* @author Jan Rojcek
* @since 1.7
*/
public class NodeTableModel extends AbstractTableModel {
    private static final String ATTR_INVISIBLE = "InvisibleInTreeTableView"; // NOI18N
    static final String ATTR_COMPARABLE_COLUMN = "ComparableColumnTTV"; // NOI18N
    static final String ATTR_SORTING_COLUMN = "SortingColumnTTV"; // NOI18N
    static final String ATTR_DESCENDING_ORDER = "DescendingOrderTTV"; // NOI18N
    private static final String ATTR_ORDER_NUMBER = "OrderNumberTTV"; // NOI18N
    private static final String ATTR_TREE_COLUMN = "TreeColumnTTV"; // NOI18N
    private static final String ATTR_MNEMONIC_CHAR = "ColumnMnemonicCharTTV"; // NOI18N
    private static final String ATTR_DISPLAY_NAME_WITH_MNEMONIC = "ColumnDisplayNameWithMnemonicTTV"; // NOI18N

    /** all columns of model */
    ArrayColumn[] allPropertyColumns = new ArrayColumn[] {  };

    /** visible columns of model */
    private int[] propertyColumns = new int[] {  };

    /** rows of model */
    private Node[] nodeRows = new Node[] {  };

    /** sorting column */
    private int sortColumn = -1;

    /** if true, at least one column can be used to sort */
    private boolean existsComparableColumn = false;
    private Property treeColumnProperty = null;

    /** listener on node properties changes, recreates displayed data */
    private PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                //fireTableDataChanged();
                int row = rowForNode((Node) evt.getSource());

                if (row == -1) {
                    return;
                }

                int column = columnForProperty(evt.getPropertyName());

                if (column == -1) {
                    fireTableRowsUpdated(row, row);
                } else {
                    fireTableCellUpdated(row, column);
                }
            }
        };

    /** Set rows.
     * @param nodes the rows
     */
    public void setNodes(Node[] nodes) {
        boolean asserts = false;
        assert (asserts = true);
        if (asserts && !EventQueue.isDispatchThread()) {
            Exceptions.printStackTrace(new IllegalStateException("Must be called in AWT to assure data consistency."));
        }
        for (int i = 0; i < nodeRows.length; i++)
            nodeRows[i].removePropertyChangeListener(pcl);

        nodeRows = nodes;

        for (int i = 0; i < nodeRows.length; i++)
            nodeRows[i].addPropertyChangeListener(pcl);

        fireTableDataChanged();
    }

    /** Set columns.
     * @param props the columns
     */
    public void setProperties(Property[] props) {
        boolean asserts = false;
        assert (asserts = true);
        if (asserts && !EventQueue.isDispatchThread()) {
            Exceptions.printStackTrace(new IllegalStateException("Must be called in AWT to assure data consistency."));
        }
        int size = props.length;
        sortColumn = -1;
        treeColumnProperty = null;

        int treePosition = -1;

        for (int i = 0; i < props.length; i++) {
            Object o = props[i].getValue(ATTR_TREE_COLUMN);
            boolean x;

            if (o instanceof Boolean) {
                if (((Boolean) o).booleanValue()) {
                    treeColumnProperty = props[i];
                    size--;
                    treePosition = i;
                }
            }
        }

        allPropertyColumns = new ArrayColumn[size];

        int visibleCount = 0;
        existsComparableColumn = false;

        Map<Double, Integer> sort = new TreeMap<>();
        int i = 0;
        int ia = 0;

        while (i < props.length) {
            if (i != treePosition) {
                allPropertyColumns[ia] = new ArrayColumn();
                allPropertyColumns[ia].setProperty(props[i]);

                if (isVisible(props[i])) {
                    visibleCount++;

                    Object o = props[i].getValue(ATTR_ORDER_NUMBER);

                    if (o instanceof Integer) {
                        sort.put(((Integer)o).doubleValue(), ia);
                    } else {
                        sort.put(ia + 0.1, ia);
                    }
                } else {
                    allPropertyColumns[ia].setVisibleIndex(-1);

                    Object o = props[i].getValue(ATTR_SORTING_COLUMN);

                    if (o instanceof Boolean) {
                        props[i].setValue(ATTR_SORTING_COLUMN, Boolean.FALSE);
                    }
                }

                if (!existsComparableColumn) {
                    Object o = props[i].getValue(ATTR_COMPARABLE_COLUMN);

                    if (o instanceof Boolean) {
                        existsComparableColumn = ((Boolean) o).booleanValue();
                    }
                }

                ia++;
            }

            i++;
        }

        // visible columns
        propertyColumns = new int[visibleCount];

        int j = 0;
        Iterator it = sort.values().iterator();

        while (it.hasNext()) {
            i = ((Integer) it.next()).intValue();
            allPropertyColumns[i].setVisibleIndex(j);
            propertyColumns[j] = i;
            j++;
        }

        fireTableStructureChanged();
    }

    /* recompute set of visible columns
     */
    private void computeVisiblePorperties(int visCount) {
        propertyColumns = new int[visCount];

        Map<Double, Integer> sort = new TreeMap<>();

        for (int i = 0; i < allPropertyColumns.length; i++) {
            int vi = allPropertyColumns[i].getVisibleIndex();

            if (vi == -1) {
                sort.put((i - 0.1), i);
            } else {
                sort.put((double)vi, i);
            }
        }

        int j = 0;
        Iterator<Integer> it = sort.values().iterator();

        while (it.hasNext()) {
            int i = it.next().intValue();
            Property p = allPropertyColumns[i].getProperty();

            if (isVisible(p)) {
                propertyColumns[j] = i;
                allPropertyColumns[i].setVisibleIndex(j);
                j++;
            } else {
                allPropertyColumns[i].setVisibleIndex(-1);

                Object o = p.getValue(ATTR_SORTING_COLUMN);

                if (o instanceof Boolean) {
                    if (((Boolean) o).booleanValue()) {
                        p.setValue(ATTR_SORTING_COLUMN, Boolean.FALSE);
                        p.setValue(ATTR_DESCENDING_ORDER, Boolean.FALSE);
                    }
                }
            }
        }

        fireTableStructureChanged();
    }

    /** Get width of visible column.
     * @param column number
     * @return column width
     */
    int getVisibleColumnWidth(int column) {
        return allPropertyColumns[propertyColumns[column]].getWidth();
    }

    /** Get width of column from whole property set
     * @param column number
     * @return column width
     */
    int getArrayColumnWidth(int column) {
        return allPropertyColumns[column].getWidth();
    }

    /** Set width of visible column.
     * @param column number
     * @param column width
     */
    void setVisibleColumnWidth(int column, int width) {
        allPropertyColumns[propertyColumns[column]].setWidth(width);
    }

    /** Set width of column from whole property set
     * @param column number
     * @param column width
     */
    void setArrayColumnWidth(int column, int width) {
        allPropertyColumns[column].setWidth(width);
    }

    /** Get index of visible column
     * @param column number from whole property set
     * @return column index
     */
    int getVisibleIndex(int arrayIndex) {
        return allPropertyColumns[arrayIndex].getVisibleIndex();
    }

    /** Get index of visible column
     * @param column number from whole property set
     * @return column index
     */
    int getArrayIndex(int visibleIndex) {
        for (int i = 0; i < allPropertyColumns.length; i++) {
            if (allPropertyColumns[i].getVisibleIndex() == visibleIndex) {
                return i;
            }
        }

        return -1;
    }

    /**
     * If true, column property should be comparable - allows sorting
     * @param column Index of a visible column
     */
    boolean isComparableColumn(int column) {
        return isComparableColumnEx(propertyColumns[column]);
    }

    /**
     * If true, column property should be comparable - allows sorting
     * @param column Index to the array of all properties
     */
    boolean isComparableColumnEx(int column) {
        Property p = allPropertyColumns[column].getProperty();
        Object o = p.getValue(ATTR_COMPARABLE_COLUMN);

        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue();
        }

        return false;
    }

    /**
     * @param column Index to the array of all properties
     * @return True if the property at the given index is visible
     */
    boolean isVisibleColumnEx(int column) {
        for (int i = 0; i < propertyColumns.length; i++) {
            if (column == propertyColumns[i]) {
                return true;
            }
        }

        return false;
    }

    /* If true, at least one column is comparable
     */
    boolean existsComparableColumn() {
        return existsComparableColumn;
    }

    /**
     * If true, column is currently used for sorting
     * @param Index to the array of all properties (the column may not be visible)
     */
    boolean isSortingColumnEx(int column) {
        Property p = allPropertyColumns[column].getProperty();
        Object o = p.getValue(ATTR_SORTING_COLUMN);

        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue();
        }

        return false;
    }

    /**
     * Sets column to be currently used for sorting
     *@param Index to the array of all properties (the column may not by visible)
     */
    void setSortingColumnEx(int column) {
        if (sortColumn != -1) {
            Property p = allPropertyColumns[sortColumn].getProperty();
            p.setValue(ATTR_SORTING_COLUMN, Boolean.FALSE);
            p.setValue(ATTR_DESCENDING_ORDER, Boolean.FALSE);
        }

        if (column != -1) {
            sortColumn = column; //propertyColumns[column];

            Property p = allPropertyColumns[sortColumn].getProperty();
            p.setValue(ATTR_SORTING_COLUMN, Boolean.TRUE);
        } else {
            sortColumn = -1;
        }
    }

    int translateVisibleColumnIndex(int index) {
        if (index < 0) {
            return index;
        }

        return propertyColumns[index];
    }

    /* Gets column index of sorting column, if it's visible.
     * Otherwise returns -1.
     */
    int getVisibleSortingColumn() {
        if (sortColumn == -1) {
            for (int i = 0; i < propertyColumns.length; i++) {
                if (isSortingColumnEx(propertyColumns[i])) {
                    sortColumn = propertyColumns[i];

                    return i;
                }
            }
        } else {
            if (isVisible(allPropertyColumns[sortColumn].getProperty())) {
                return getVisibleIndex(sortColumn);
            }
        }

        return -1;
    }

    /* Gets column index of sorting column, if it's visible.
     * Otherwise returns -1.
     */
    int getSortingColumn() {
        if (sortColumn == -1) {
            for (int i = 0; i < allPropertyColumns.length; i++) {
                if (isSortingColumnEx(i)) {
                    sortColumn = i;

                    return i;
                }
            }
        } else {
            return sortColumn;
        }

        return -1;
    }

    /* If true, current sorting uses descending order.
     */
    boolean isSortOrderDescending() {
        if (sortColumn == -1) {
            return false;
        }

        Property p = allPropertyColumns[sortColumn].getProperty();
        Object o = p.getValue(ATTR_DESCENDING_ORDER);

        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue();
        }

        return false;
    }

    /* Sets sorting order for current sorting.
     */
    void setSortOrderDescending(boolean descending) {
        if (sortColumn != -1) {
            Property p = allPropertyColumns[sortColumn].getProperty();
            p.setValue(ATTR_DESCENDING_ORDER, descending ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    /** Returns node property if found in nodes property sets. Could be overriden to
     * return property which is not in nodes property sets.
     * @param node represents single row
     * @param prop represents column
     * @return nodes property
     */
    protected Property getPropertyFor(Node node, Property prop) {
        Node.PropertySet[] propSets = node.getPropertySets();

        for (int i = 0; i < propSets.length; i++) {
            Node.Property[] props = propSets[i].getProperties();

            for (int j = 0; j < props.length; j++) {
                if (prop.equals(props[j])) {
                    return props[j];
                }
            }
        }

        return null;
    }

    /** Helper method to ask for a node representant of row.
     */
    Node nodeForRow(int row) {
        return nodeRows[row];
    }

    /**
     * Helper method to ask for a property representant of column.
     * @param Index of a visible column
     */
    Property propertyForColumn(int column) {
        if (column >= 0) {
            column = propertyColumns[column];
        }

        return propertyForColumnEx(column);
    }

    /**
     * Helper method to ask for a property representant of column.
     * @param Index to the array of all properties.
     */
    Property propertyForColumnEx(int column) {
        if (column == -1) {
            return treeColumnProperty;
        } else {
            return allPropertyColumns[column].getProperty();
        }
    }

    /**
     * @return The count of all properties (includes invisible columns)
     */
    int getColumnCountEx() {
        return allPropertyColumns.length;
    }

    private int rowForNode(Node node) {
        for (int i = 0; i < nodeRows.length; i++) {
            if (node.equals(nodeRows[i])) {
                return i;
            }
        }

        return -1;
    }

    private int columnForProperty(String propName) {
        for (int i = 0; i < propertyColumns.length; i++) {
            if (allPropertyColumns[propertyColumns[i]].getProperty().getName().equals(propName)) {
                return i;
            }
        }

        return -1;
    }

    /** Helper method to ask if column representing a property should be
     * visible
     */
    private boolean isVisible(Property p) {
        Object o = p.getValue(ATTR_INVISIBLE);

        if (o instanceof Boolean) {
            return !((Boolean) o).booleanValue();
        }

        return true;
    }

    /** Set column representing a property to be visible
     */
    private void setVisible(Property p, boolean visible) {
        p.setValue(ATTR_INVISIBLE, (!visible) ? Boolean.TRUE : Boolean.FALSE);
    }

    //
    // TableModel methods
    //

    /** Getter for row count.
     * @return row count
     */
    public int getRowCount() {
        return nodeRows.length;
    }

    /** Getter for column count.
     * @return column count
     */
    public int getColumnCount() {
        return propertyColumns.length;
    }

    /** Getter for property.
     * @param row table row index
     * @param column table column index
     * @return property at (row, column)
     */
    public Object getValueAt(int row, int column) {
        Node node = nodeRows[row];
        int pc = propertyColumns[column];
        ArrayColumn ac = allPropertyColumns[pc];
        return getPropertyFor(node, ac.getProperty());
    }

    /** Cell is editable only if it has non null value.
     * @param row table row index
     * @param column table column index
     * @return true if cell contains non null value
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return getValueAt(row, column) != null;
    }

    /** Getter for column class.
     * @param column table column index
     * @return  <code>Node.Property.class</code>
     */
    @Override
    public Class getColumnClass(int column) {
        return Node.Property.class;
    }

    /** Getter for column name
     * @param column table column index
     * @return display name of property which represents column
     */
    @Override
    public String getColumnName(int column) {
        return getColumnNameEx(propertyColumns[column]);
    }

    /** Getter for column name
     * @param column table column index
     * @return display name of property which represents column
     */
    String getColumnNameEx(int column) {
        return allPropertyColumns[column].getProperty().getDisplayName();
    }

    /* display panel to set/unset set of visible columns
     */
    boolean selectVisibleColumns(String viewName, String treeColumnName, String treeColumnDesc) {
        boolean changed = false;

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new GridBagLayout());
        
        panel.getAccessibleContext().setAccessibleName( 
                NbBundle.getMessage(NodeTableModel.class, "ACSN_ColumnDialog") );
        panel.getAccessibleContext().setAccessibleDescription( 
                NbBundle.getMessage(NodeTableModel.class, "ACSD_ColumnDialog") );

        ArrayList<JCheckBox> boxes = new ArrayList<JCheckBox>(allPropertyColumns.length);
        boolean[] oldvalues = new boolean[allPropertyColumns.length];
        int[] sortpointer = new int[allPropertyColumns.length];
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        labelConstraints.anchor = java.awt.GridBagConstraints.WEST;
        labelConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        labelConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        labelConstraints.weightx = 1.0;

        JLabel desc = new JLabel(NbBundle.getMessage(NodeTableModel.class, "LBL_ColumnDialogDesc"));
        panel.add(desc, labelConstraints);

        GridBagConstraints firstConstraints = new GridBagConstraints();
        firstConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        firstConstraints.anchor = java.awt.GridBagConstraints.WEST;
        firstConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        firstConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        firstConstraints.weightx = 1.0;

        JCheckBox first = new JCheckBox(treeColumnName + ": " + treeColumnDesc, true); // NOI18N
        first.setEnabled(false);
        panel.add(first, firstConstraints);

        String boxtext;
        TreeMap<String, Integer> sort = new TreeMap<String, Integer>();

        for (int i = 0; i < allPropertyColumns.length; i++) {
            oldvalues[i] = isVisible(allPropertyColumns[i].getProperty());
            boxtext = getDisplayNameWithMnemonic( allPropertyColumns[i].getProperty() ) 
                    + ": " 
                    + allPropertyColumns[i].getProperty().getShortDescription(); // NOI18N
            sort.put(boxtext, Integer.valueOf(i));
        }

        Iterator<String> it = sort.keySet().iterator();
        int j = 0;

        while (it.hasNext()) {
            boxtext = it.next();

            int i = sort.get(boxtext).intValue();
            JCheckBox b = new JCheckBox(boxtext, oldvalues[i]);
            Mnemonics.setLocalizedText(b, boxtext);
            makeAccessibleCheckBox(b, allPropertyColumns[i].getProperty());
            sortpointer[j] = i;
            panel.add(b, gridBagConstraints);
            boxes.add(b);
            j++;
        }

        String title = NbBundle.getMessage(NodeTableModel.class, "LBL_ColumnDialogTitle");

        if ((viewName != null) && (viewName.length() > 0)) {
            title = viewName + " - " + title; // NOI18N
        }

        DialogDescriptor dlg = new DialogDescriptor(
                panel, title, true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, null, null
            );

        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setVisible(true);

        if (dlg.getValue().equals(DialogDescriptor.OK_OPTION)) {
            int num = boxes.size();
            int nv = 0;

            for (int i = 0; i < num; i++) {
                JCheckBox b = boxes.get(i);

                j = sortpointer[i];

                if (b.isSelected() != oldvalues[j]) {
                    setVisible(allPropertyColumns[j].getProperty(), b.isSelected());
                    changed = true;
                }

                if (b.isSelected()) {
                    nv++;
                }
            }

            // Don't allow the user to disable ALL columns

            /*
            if (nv == 0) {
                setVisible( allPropertyColumns[0].getProperty(), true );
                nv = 1;
            }
             */
            if (changed) {
                computeVisiblePorperties(nv);
            }
        }

        return changed;
    }
    
    String getDisplayNameWithMnemonic( Property p ) {
        String res = null;
        Object displayNameWithMnemonic = p.getValue(ATTR_DISPLAY_NAME_WITH_MNEMONIC);
        if( null !=displayNameWithMnemonic && displayNameWithMnemonic.toString().length() > 0 ) {
            res = displayNameWithMnemonic.toString();
        } else {
            res = p.getDisplayName();
        }
        return res;
    }

    void makeAccessibleCheckBox(JCheckBox box, Property p) {
        box.getAccessibleContext().setAccessibleName(p.getDisplayName());
        box.getAccessibleContext().setAccessibleDescription(p.getShortDescription());

        Object mnemonicChar = p.getValue(ATTR_MNEMONIC_CHAR);

        if ((null != mnemonicChar) && (mnemonicChar.toString().length() > 0)) {
            box.setMnemonic(mnemonicChar.toString().charAt(0));
        }
    }

    void moveColumn(int from, int to) {
        int i = propertyColumns[from];
        int j = propertyColumns[to];

        propertyColumns[from] = j;
        propertyColumns[to] = i;

        allPropertyColumns[i].setVisibleIndex(to);
        allPropertyColumns[j].setVisibleIndex(from);

        sortColumn = -1;
    }

    /* class representing property column
     */
    static class ArrayColumn {
        /** Property representing column */
        private Property property;

        /** Preferred width of column */
        private int width;

        ArrayColumn() {
        }

        /** Getter for property property.
         * @return Value of property property.
         */
        public Property getProperty() {
            return this.property;
        }

        /** Setter for property property.
         * @param property New value of property property.
         */
        public void setProperty(Property property) {
            this.property = property;
        }

        /** Getter for property width.
         * @return Value of property width.
         */
        public int getWidth() {
            return this.width;
        }

        /** Setter for property width.
         * @param width New value of property width.
         */
        public void setWidth(int width) {
            this.width = width;
        }

        /** Getter for property visibleIndex.
         * @return Value of property visibleIndex.
         */
        public int getVisibleIndex() {
            Integer order = (Integer) property.getValue(ATTR_ORDER_NUMBER);
            if (order == null) return -1;
            else return order.intValue();
        }

        /** Setter for property visibleIndex.
         * @param visibleIndex New value of property visibleIndex.
         */
        public void setVisibleIndex(int visibleIndex) {
            property.setValue(ATTR_ORDER_NUMBER, visibleIndex);
        }
    }
}
