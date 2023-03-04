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

package org.netbeans.installer.utils.helper.swing;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JTable;
import javax.swing.JTree; 
import javax.swing.table.TableCellRenderer;
import org.netbeans.installer.utils.LogManager;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiTreeTableColumnRenderer extends JTree implements TableCellRenderer {
    private NbiTreeTable treeTable;
    
    private int visibleRow = 0;
    
    private NbiTreeTableColumnCellRenderer cellRenderer;
    
    public NbiTreeTableColumnRenderer(final NbiTreeTable treeTable) {
        this.treeTable = treeTable;
        
        setModel(treeTable.getModel().getTreeModel());
        
        setRootVisible(false);
        setShowsRootHandles(true);
        
        setTreeColumnCellRenderer(new NbiTreeTableColumnCellRenderer(treeTable));
        
        setRowHeight(treeTable.getRowHeight());
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        visibleRow = row;
        
        if (selected) {
            setOpaque(true);
            setBackground(treeTable.getSelectionBackground());
            setForeground(treeTable.getSelectionForeground());
        } else {
            setOpaque(false);
            setBackground(treeTable.getBackground());
            setForeground(treeTable.getForeground());
        }
        
        return this;
    }
    
    public void setBounds(int x, int y, int w, int h) {
        if (treeTable != null) {
            super.setBounds(x, 0, w, treeTable.getHeight());
        } else {
            super.setBounds(x, y, w, h);
        }
    }
    
    public void paint(Graphics g) {
        g.translate(0, -visibleRow * getRowHeight());
        super.paint(g);
    }
    
    public NbiTreeTableColumnCellRenderer getTreeColumnCellRenderer() {
        return cellRenderer;
    }
    
    public void setTreeColumnCellRenderer(final NbiTreeTableColumnCellRenderer renderer) {
        cellRenderer = renderer;
        setCellRenderer(renderer);
    }
}
