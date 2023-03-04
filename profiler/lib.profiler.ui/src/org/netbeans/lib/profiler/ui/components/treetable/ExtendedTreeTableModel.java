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


/**
 * TreeTable model that extends AbstractTableModel and allows to hide columns
 *
 * @author  Jiri Sedlacek
 */
public class ExtendedTreeTableModel extends AbstractTreeTableModel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AbstractTreeTableModel realModel;
    private int[] columnsMapping; // mapping virtual columns -> real columns
    private boolean[] columnsVisibility; // visibility flags of real columns
    private int realColumnsCount;
    private int virtualColumnsCount;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ExtendedTreeTableModel(AbstractTreeTableModel realModel) {
        super(realModel.root, realModel.supportsSorting, realModel.initialSortingColumn, realModel.initialSortingOrder);

        realColumnsCount = realModel.getColumnCount();
        virtualColumnsCount = realColumnsCount;

        this.realModel = realModel;
        columnsMapping = new int[realColumnsCount];

        boolean[] initialColumnsVisibility = new boolean[realColumnsCount];

        for (int i = 0; i < realColumnsCount; i++) {
            initialColumnsVisibility[i] = true;
        }

        setColumnsVisibility(initialColumnsVisibility);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isCellEditable(Object node, int column) {
        return realModel.isCellEditable(node, getRealColumn(column));
    }

    public Class getColumnClass(int col) {
        return realModel.getColumnClass(getRealColumn(col));
    }

    public int getColumnCount() {
        return virtualColumnsCount;
    }

    //---------------------
    // AbstractTreeTableModel interface
    public String getColumnName(int col) {
        return realModel.getColumnName(getRealColumn(col));
    }

    public String getColumnToolTipText(int columnIndex) {
        int realColumn = getRealColumn(columnIndex);

        if (realColumn == -1) {
            return null;
        }

        return realModel.getColumnToolTipText(realColumn);
    }

    public void setColumnsVisibility(boolean[] columnsVisibility) {
        this.columnsVisibility = columnsVisibility;
        recomputeColumnsMapping();
    }

    public boolean[] getColumnsVisibility() {
        return columnsVisibility;
    }

    public boolean getInitialSorting(int column) {
        return realModel.getInitialSorting(getRealColumn(column));
    }

    public int getInitialSortingColumn() {
        return realModel.getInitialSortingColumn();
    }

    public boolean getInitialSortingOrder() {
        return realModel.getInitialSortingOrder();
    }

    public boolean isLeaf(Object node) {
        return realModel.isLeaf(node);
    }

    public int getRealColumn(int column) {
        if ((column > -1) && (column < columnsMapping.length)) {
            return columnsMapping[column];
        }

        return -1;
    }

    public void setRealColumnVisibility(int column, boolean visible) {
        if (visible) {
            showRealColumn(column);
        } else {
            hideRealColumn(column);
        }
    }

    public boolean isRealColumnVisible(int column) {
        if ((column > -1) && (column < columnsMapping.length)) {
            return columnsVisibility[column];
        }

        return false;
    }

    public void setRoot(Object root) {
        realModel.setRoot(root);
    }

    public Object getRoot() {
        return realModel.getRoot();
    }

    public void setValueAt(Object aValue, Object node, int column) {
        realModel.setValueAt(aValue, node, getRealColumn(column));
    }

    /*public Object getValueAt (int rowIndex, int columnIndex) {
       return realModel.getValueAt(rowIndex, getRealColumn(columnIndex));
       }*/
    public Object getValueAt(Object node, int column) {
        return realModel.getValueAt(node, getRealColumn(column));
    }

    public int getVirtualColumn(int column) {
        for (int i = 0; i < virtualColumnsCount; i++) {
            if (getRealColumn(i) == column) {
                return i;
            }
        }

        return -1;
    }

    public void hideRealColumn(int column) {
        if (isRealColumnVisible(column)) {
            columnsVisibility[column] = false;
            recomputeColumnsMapping();
        }
    }

    public void showRealColumn(int column) {
        if (!isRealColumnVisible(column)) {
            columnsVisibility[column] = true;
            recomputeColumnsMapping();
        }
    }

    public void sortByColumn(int column, boolean order) {
        realModel.sortByColumn(getRealColumn(column), order);
    }

    private void recomputeColumnsMapping() {
        virtualColumnsCount = 0;

        int virtualColumnIndex = 0;

        // set indexes virtual columns -> real columns
        for (int i = 0; i < realColumnsCount; i++) {
            if (columnsVisibility[i] == true) {
                columnsMapping[virtualColumnIndex] = i;
                virtualColumnsCount++;
                virtualColumnIndex++;
            }
        }

        // clear mappings of unused real columns
        for (int i = virtualColumnIndex; i < realColumnsCount; i++) {
            columnsMapping[i] = -1;
        }
        
        fireTableStructureChanged();
        realModel.fireTableStructureChanged();
    }
}
