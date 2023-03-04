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

package org.netbeans.modules.debugger.jpda.ui.options;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Cell renderer that makes setEnabled() to work.
 * 
 * @author Martin Entlicher
 */
public class DisablingCellRenderer implements TableCellRenderer {

    private TableCellRenderer r;
    private JTable t;
    private Color background;

    public DisablingCellRenderer(TableCellRenderer r, JTable t) {
        this(r, t, null);
    }

    public DisablingCellRenderer(TableCellRenderer r, JTable t, Color background) {
        this.r = r;
        this.t = t;
        this.background = background;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setEnabled(t.isEnabled());
        if (background != null) {
            c.setBackground(background);
        }
        return c;
    }

    /**
     * Applies DisablingCellRenderer to all cell renderers in the table.
     * @param t the table
     */
    public static void apply(JTable t) {
        apply(t, null);
    }

    /**
     * Applies DisablingCellRenderer to all cell renderers in the table.
     * @param t the table
     * @param disabled if the table content should be disabled
     */
    public static void apply(JTable t, Color background) {
        int nc = t.getColumnModel().getColumnCount();
        for (int i = 0; i < nc; i++) {
            TableCellRenderer columnRenderer = t.getColumnModel().getColumn(i).getCellRenderer();
            if (columnRenderer == null) columnRenderer = t.getDefaultRenderer(t.getColumnClass(i));
            t.getColumnModel().getColumn(i).setCellRenderer(
                new DisablingCellRenderer(columnRenderer, t, background));
        }
    }

}
