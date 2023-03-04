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

package org.netbeans.modules.options.keymap;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;


/**
 * Renderer for table cells customizing shortcut.
 *
 * @author Max Sauer
 */
public class ButtonCellRenderer implements TableCellRenderer {

    private TableCellRenderer defaultRenderer;

    private static ShortcutCellPanel panel;

    public ButtonCellRenderer (TableCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getTableCellRendererComponent (
        JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column
    ) {
        JLabel c = (JLabel)defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (value instanceof String) {
            Rectangle cellRect = table.getCellRect(row, column, false);
            String scCell = (String) value;
            Dimension d = new Dimension((int) cellRect.getWidth(), (int) cellRect.getHeight());
            if (panel == null)
                panel = new ShortcutCellPanel(scCell);
            panel.setText(scCell);
            panel.setSize(d);

            if (isSelected) {
                panel.setBgColor(table.getSelectionBackground());
                if (UIManager.getLookAndFeel ().getID ().equals ("GTK"))
                    panel.setFgCOlor(table.getForeground(), true);
                else
                    panel.setFgCOlor(table.getSelectionForeground(), true);
            } else {
                panel.setBgColor(c.getBackground());
                panel.setFgCOlor(c.getForeground(), false);
            }
            if (hasFocus) {
                panel.setBorder(c.getBorder());
            } else {
                panel.setBorder(null);
            }

            return panel;
        }
        else {
            return c;
        }
    }

}
