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

package org.netbeans.modules.profiler.snaptracer.impl.details;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
class DetailsTableCellRenderer implements TableCellRenderer {

    private static final Color BACKGROUND;
    private static final Color DARKER_BACKGROUND;

    static {
        BACKGROUND = UIUtils.getProfilerResultsBackground();

        int darkerR = BACKGROUND.getRed() - 11;
        if (darkerR < 0) darkerR += 26;
        int darkerG = BACKGROUND.getGreen() - 11;
        if (darkerG < 0) darkerG += 26;
        int darkerB = BACKGROUND.getBlue() - 11;
        if (darkerB < 0) darkerB += 26;
        DARKER_BACKGROUND = new Color(darkerR, darkerG, darkerB);
    }

    private TableCellRenderer impl;


    DetailsTableCellRenderer(TableCellRenderer impl) {
        this.impl = impl;
    }


    protected Object formatValue(JTable table, Object value, boolean isSelected,
                                 boolean hasFocus, int row, int column) {
        return value;
    }

    protected void updateRenderer(Component c, JTable table, Object value,
                                  boolean isSelected, boolean hasFocus, int row,
                                  int column) {
        if (!isSelected) {
            c.setBackground(row % 2 == 0 ? DARKER_BACKGROUND : BACKGROUND);
            // Make sure the renderer paints its background (Nimbus)
            if (c instanceof JComponent) ((JComponent)c).setOpaque(true);
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        if (impl == null) impl = table.getDefaultRenderer(table.getColumnClass(column));
        
        value = formatValue(table, value, isSelected, hasFocus, row, column);
        Component c = impl.getTableCellRendererComponent(table, value, isSelected,
                                                         hasFocus, row, column);
        updateRenderer(c, table, value, isSelected, hasFocus, row, column);

        return c;
    }
    
}
