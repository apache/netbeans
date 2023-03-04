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
package org.netbeans.modules.db.dataview.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.db.dataview.util.ColorHelper;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class JXTableDecorator extends JTable {

    public static final Color ROW_COLOR = ColorHelper.getTableBackground();
    public static final Color ALTERNATE_ROW_COLOR = ColorHelper.getTableAltbackground();
    public static final Color GRID_COLOR = ColorHelper.getTableGrid();
    public static final Color ROLLOVER_ROW_COLOR = ColorHelper.getTableRollOverRowBackground();

    JXTableDecorator() {
        super();
    }

    public JXTableDecorator(TableModel model, TableColumnModel columnModel, ListSelectionModel selectionModel) {
        super(model, columnModel, selectionModel);
    }

    public JXTableDecorator(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
    }

    public JXTableDecorator(TableModel dm) {
        super(dm);
    }

    /**
     * Paints empty rows too
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintEmptyRows(g);
    }

    /**
     * Paints the backgrounds of the implied empty rows when the
     * table model is insufficient to fill all the visible area
     * available to us. We don't involve cell renderers, because
     * we have no data.
     */
    protected void paintEmptyRows(Graphics g) {
        final int rowCount = getRowCount();
        final Rectangle clip = g.getClipBounds();
        final int height = clip.y + clip.height;
        if (rowCount * rowHeight < height) {
            for (int i = rowCount; i <= height / rowHeight; ++i) {
                g.setColor(backgroundColorForRow(i));
                g.fillRect(clip.x, i * rowHeight, clip.width, rowHeight);
                drawHorizontalLine(g, clip, i);
            }
            drawVerticalLines(g, rowCount, height);
        }
    }

    protected void drawHorizontalLine(Graphics g, final Rectangle clip, int i) {
        g.setColor(ResultSetJXTable.GRID_COLOR);
        g.drawLine(clip.x, i * rowHeight - 1, clip.x + clip.width, i * rowHeight - 1);
    }

    protected void drawVerticalLines(Graphics g, final int rowCount, final int height) {

        g.setColor(ResultSetJXTable.GRID_COLOR);
        TableColumnModel colModel = getColumnModel();
        int x = 0;
        for (int i = 0; i < colModel.getColumnCount(); ++i) {
            TableColumn column = colModel.getColumn(i);
            x += column.getWidth();
            g.drawLine(x - 1, rowCount * rowHeight, x - 1, height);
        }
    }

    protected Color backgroundColorForRow(int row) {
        return (row % 2 == 0) ? ResultSetJXTable.ROW_COLOR : ResultSetJXTable.ALTERNATE_ROW_COLOR;
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        boolean selected = false;
        for(int i: getSelectedRows()) {
            if(row == i) {
                selected = true;
                break;
            }
        }
        Component c = super.prepareRenderer(renderer, row, column);
        if(selected) {
            c.setBackground(getSelectionBackground());
        } else {
            c.setBackground(backgroundColorForRow(row));
        }
        return c;
    }
    
}
