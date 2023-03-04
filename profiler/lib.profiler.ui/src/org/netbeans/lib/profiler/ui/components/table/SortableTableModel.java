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

package org.netbeans.lib.profiler.ui.components.table;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;


/**
 * This class provides a superclass, from which Table Models can be derived, that will support
 * sorting by a column on which the user clicks. A subclass should call setTable(table),
 * and should provide an implementation of the sortByColumn(int column) method.
 *
 * @author Misha Dmitriev
 * @author Jiri Sedlacek
 */
public abstract class SortableTableModel extends AbstractTableModel {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    /**
     * This class is used for listening to the table header mouse events.
     */
    private class HeaderListener extends MouseAdapter implements MouseMotionListener {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        /*
         * If the user clicks to the sorting column (column defining the sort criterium and order), the sorting order is reversed.
         * If new sorting column is selected, the appropriate sorting order for column's datatype is set.
         */
        public void mouseClicked(MouseEvent e) {
            if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
                int column = tableHeader.columnAtPoint(e.getPoint());
                int sortingColumn = headerRenderer.getSortingColumn();

                if (column == sortingColumn) {
                    headerRenderer.reverseSortingOrder();
                } else {
                    headerRenderer.setSortingColumn(column);

                    if (getInitialSorting(column)) {
                        headerRenderer.setSortingOrder(SORT_ORDER_ASC); // Default sort order for strings is Ascending
                    } else {
                        headerRenderer.setSortingOrder(SORT_ORDER_DESC); // Default sort order for numbers is Descending
                    }
                }

                tableHeader.repaint();

                sortByColumn(column, headerRenderer.getSortingOrder());
            }
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
            int focusedColumn = tableHeader.columnAtPoint(e.getPoint());

            if ((focusedColumn != lastFocusedColumn) && (focusedColumn != -1)) {
                tableHeader.setToolTipText(SortableTableModel.this.getColumnToolTipText(focusedColumn));
                lastFocusedColumn = focusedColumn;
            }
        }

        /*
         * Here the active header button is programatically pressed
         */
        public void mousePressed(MouseEvent e) {
            if ((e.getModifiers() == InputEvent.BUTTON1_MASK) && (tableHeader.getResizingColumn() == null)) {
                headerRenderer.setPressedColumn(tableHeader.columnAtPoint(e.getPoint()));
                tableHeader.repaint();
            }
        }

        /*
         * Here the active header button is programatically released
         */
        public void mouseReleased(MouseEvent e) {
            if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
                headerRenderer.setPressedColumn(-1);
                tableHeader.repaint();
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final boolean SORT_ORDER_DESC = false;
    public static final boolean SORT_ORDER_ASC = true;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private CustomSortableHeaderRenderer headerRenderer;
    private HeaderListener headerListener;
    private ImageIcon sortAscIcon = Icons.getImageIcon(GeneralIcons.SORT_ASCENDING);
    private ImageIcon sortDescIcon = Icons.getImageIcon(GeneralIcons.SORT_DESCENDING);
    private JTableHeader tableHeader;
    private int lastFocusedColumn = -1;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SortableTableModel() {
        headerListener = new HeaderListener();
        headerRenderer = new CustomSortableHeaderRenderer(sortAscIcon, sortDescIcon);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * After the table to which this model belongs has been set, this method allows to set the initial sorting column and sorting order.
     * @param sortingColumn The initial sorting column
     * @param sortingOrder The initial sorting order
     */
    public void setInitialSorting(int sortingColumn, boolean sortingOrder) {
        if (headerRenderer != null) {
            headerRenderer.setSortingColumn(sortingColumn);
            headerRenderer.setSortingOrder(sortingOrder);
        }
    }

    /**
     * @param column The table column index
     * @return Initial sorting for the specified column - if true, ascending, if false descending
     */
    public abstract boolean getInitialSorting(int column); /* {
       return (getColumnClass(column).equals(String.class));
       }*/

    public int getSortingColumn() {
        return headerRenderer.getSortingColumn();
    }

    public boolean getSortingOrder() {
        return headerRenderer.getSortingOrder();
    }

    /**
     * Assigns this SortableTableModel to the JTable and sets the custom renderer for the selectable table header.
     * @param table The JTable to set this table model to
     */
    public void setTable(JTable table) {
        TableColumnModel tableModel = table.getColumnModel();
        int n = tableModel.getColumnCount();

        for (int i = 0; i < n; i++) {
            tableModel.getColumn(i).setHeaderRenderer(headerRenderer);
        }

        if (tableHeader != table.getTableHeader()) {
            if (tableHeader != null) {
                tableHeader.removeMouseListener(headerListener);
                tableHeader.removeMouseMotionListener(headerListener);
                lastFocusedColumn = -1;
            }

            tableHeader = table.getTableHeader();
            tableHeader.setReorderingAllowed(false);
            tableHeader.addMouseListener(headerListener);
            tableHeader.addMouseMotionListener(headerListener);
        }
    }

    public abstract void sortByColumn(int column, boolean order);

    public String getColumnToolTipText(int column) {
        return null;
    }
}
