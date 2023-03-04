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

package org.netbeans.lib.profiler.ui.threads;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.lib.profiler.charts.axis.TimeAxisUtils;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.swing.renderer.BaseRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer;

/**
 *
 * @author Jiri Sedlacek
 */
public class TimelineHeaderRenderer extends BaseRenderer implements TableCellRenderer {
    
    private final TableCellRenderer impl;
    private final int column;
    
    private final ViewManager view;
    
    
    public TimelineHeaderRenderer(TableCellRenderer impl, int column, ViewManager view) {
        this.impl = impl;
        this.column = column;
        this.view = view;
    }
    
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        boolean timelineColumn = column == table.convertColumnIndexToModel(col);
        setVisible(timelineColumn);
        
        Component c = impl.getTableCellRendererComponent(table, timelineColumn ?
                      " " : value, isSelected, hasFocus, row, col); // NOI18N
        if (timelineColumn) {
            if (getParent() == null && c instanceof Container) ((Container)c).add(this);

            JTableHeader header = table.getTableHeader();
            if (painter == null) initStaticUI(c, header);
            
            TableColumn _column = header.getColumnModel().getColumn(col);
            
            setSize(_column.getWidth(), header.getSize().height);
        }
        
        return c;
    }
    
    public void paint(Graphics g) {        
        long time = view.getFirstTimeMark(true);
        long step = view.getTimeMarksStep();
        String format = view.getTimeMarksFormat();
        
        int w = getWidth();
        int h = getHeight();
        int x = view.getTimePosition(time, true);
        
        g.setColor(painter.getForeground());
        int oldX = x;
        while (x < w) {
            paintTimeMark(x, TimeAxisUtils.formatTime(time, format), h, g);
            time += step;
            x = view.getTimePosition(time, true);
            
            // Workaround to prevent endless loop until fixed
            if (x <= oldX) break;
            else oldX = x;
        }
    }
    
    private void paintTimeMark(int x, String time, int h, Graphics g) {
        painter.setText(time);
        
        Dimension d = painter.getPreferredSize();
        painter.setSize(d);
        painter.move(x - d.width / 2, (h - d.height) / 2 + Y_LAF_OFFSET);
        painter.paint(g);
    }
    
    private static LabelRenderer painter;
    private static int Y_LAF_OFFSET;
    private static void initStaticUI(Component c, JTableHeader header) {
        painter = new LabelRenderer(true);
        
        Color color = c.getForeground();
        if (color == null) color = header.getForeground();
        if (color == null) color = UIManager.getColor("TableHeader.foreground"); // NOI18N
        if (color != null) painter.setForeground(color);
        Font font = c.getFont();
        if (font == null) font = header.getFont();
        if (font == null) font = UIManager.getFont("TableHeader.font"); // NOI18N
        if (font != null) painter.setFont(font);
        
        if (UIUtils.isWindowsXPLookAndFeel()) Y_LAF_OFFSET = 1;
        else if (UIUtils.isNimbusLookAndFeel()) Y_LAF_OFFSET = -1;
        else Y_LAF_OFFSET = 0;
    }
    
}
