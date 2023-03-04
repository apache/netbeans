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

import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;


/**
 * This class is used for rendering the JTable header. It also holds information about current sorting column and sorting order.
 * The column header is rendered by the JButton using the appropriate icon.
 * @author Jiri Sedlacek
 */
public class CustomSortableHeaderRenderer implements TableCellRenderer {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ImageIcon ascIcon;
    private ImageIcon descIcon;
    private boolean sortOrder = SortableTableModel.SORT_ORDER_DESC;

    /**
     * The column which is currently being pressed.
     * The button has to be pressed programatically because the mouse events are not delivered to the JButton from the table header.
     */
    private int pressedColumn = -1;

    /**
     * The column which currently defines the sorting order. Only this column header contains the appropriate icon.
     */
    private int sortingColumn = -1;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of CustomSortableHeaderRenderer
     * @param asc The icon representing ascending sort order.
     * @param desc The icon representing descending sort order.
     */
    public CustomSortableHeaderRenderer(ImageIcon asc, ImageIcon desc) {
        ascIcon = asc;
        descIcon = desc;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setPressedColumn(int column) {
        pressedColumn = column;
    }

    public int getPressedColumn() {
        return pressedColumn;
    }

    public void setSortingColumn(int column) {
        sortingColumn = column;
    }

    public int getSortingColumn() {
        return sortingColumn;
    }

    public void setSortingOrder(boolean order) {
        sortOrder = order;
    }

    public boolean getSortingOrder() {
        return sortOrder;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column) {
        TableCellRenderer tableCellRenderer = table.getTableHeader().getDefaultRenderer();
        Component c = tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (c instanceof JLabel) {
            JLabel l = (JLabel) c;

            if (column == sortingColumn) { // only for sorting column the icon is displayed
                l.setIcon((sortOrder == SortableTableModel.SORT_ORDER_ASC) ? ascIcon : descIcon);
                l.setFont(l.getFont().deriveFont(Font.BOLD));
            } else {
                l.setIcon(null);
            }

            l.setHorizontalTextPosition(JLabel.LEFT);
        }

        return c;
    }

    public void reverseSortingOrder() {
        sortOrder = !sortOrder;
    }
}
