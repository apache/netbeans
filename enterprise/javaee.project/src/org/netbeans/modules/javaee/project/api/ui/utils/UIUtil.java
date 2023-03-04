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
package org.netbeans.modules.javaee.project.api.ui.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

public class UIUtil {

    public static void updateColumnWidths(JTable table) {
        double pw = table.getParent().getSize().getWidth();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = table.getColumnModel().getColumn(1);
        int w = ((int) pw / 2) - 1;
        if (w > column.getMaxWidth()) {
            w = column.getMaxWidth();
        }
        column.setWidth(w);
        column.setPreferredWidth(w);
        w = (int) pw - w;
        column = table.getColumnModel().getColumn(0);
        column.setWidth(w);
        column.setPreferredWidth(w);
    }

    public static void initTwoColumnTableVisualProperties(Component component, JTable table) {
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getParent().setBackground(table.getBackground());
        updateColumnWidths(table);
        component.addComponentListener(new TableColumnSizeComponentAdapter(table));
    }

    private static class TableColumnSizeComponentAdapter extends ComponentAdapter {

        private JTable table = null;

        public TableColumnSizeComponentAdapter(JTable table) {
            this.table = table;
        }

        @Override
        public void componentResized(ComponentEvent evt) {
            UIUtil.updateColumnWidths(table);
        }
    }
}
