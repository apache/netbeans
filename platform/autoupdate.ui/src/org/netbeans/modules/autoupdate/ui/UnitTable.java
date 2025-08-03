/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.autoupdate.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.api.autoupdate.UpdateManager;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public final class UnitTable extends JTable {
    private UnitCategoryTableModel model = null;
    private static final int DARKER_COLOR_COMPONENT = 10;
    private TableCellRenderer enableRenderer = null;
    
    /** Creates a new instance of UpdateTable */
    public UnitTable (TableModel model) {
        super (model);
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UnitTable.class, "ACN_UnitTable")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UnitTab.class, "ACD_UnitTable")); // NOI18N        
        this.model = (UnitCategoryTableModel) model;
        setShowGrid (false);
        setColumnsSize ();
        if(UIManager.getLookAndFeel().getID().equals("Nimbus")) {
            setBackground(new Color(getBackground().getRGB(), false));
        }
        //setFillsViewportHeight(true);        
        setIntercellSpacing (new Dimension (0, 0));
        setAutoCreateRowSorter(true);
        revalidate ();
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return model.isExpansionControlAtRow(row) ? new MoreRenderer() : super.getCellRenderer(row, column);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        //instead of setFillsViewportHeight(true);        
        getParent().setBackground(getBackground());
    }
    
    @Override
    public void removeNotify () {
        super.removeNotify ();
        enableRenderer = null;
    }
    
    @Override
    public String getToolTipText (MouseEvent e) {
        String tip;
        java.awt.Point p = e.getPoint ();
        int rowIndex = rowAtPoint (p);
        int colIndex = columnAtPoint (p);
        int realRowIndex = convertRowIndexToModel(rowIndex);
        int realColumnIndex = convertColumnIndexToModel (colIndex);
        tip = model.getToolTipText(realRowIndex, realColumnIndex);
        return tip != null ? tip : super.getToolTipText (e);
    }
    
    void resetEnableRenderer () {
        if (enableRenderer != null) {
            setEnableRenderer (enableRenderer);
        }
    }
    
    void setEnableRenderer (TableCellRenderer renderer) {
        enableRenderer = renderer;
        columnModel.getColumn(columnModel.getColumnCount() - 1).setCellRenderer(renderer);
    }
    
    void resortByDefault () {
        RowSorter sorter = getRowSorter();
        sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(2, SortOrder.ASCENDING)));
    }
    
    void setColumnsSize () {
        int columnCount = model.getColumnCount ();
        for (int i = 0; i < columnCount; i++) {
            TableColumn activeColumn = getColumnModel ().getColumn (i);
            activeColumn.setPreferredWidth (this.model.getPreferredWidth (getTableHeader (), i));
        }
    }
    
    @Override
    public Component prepareRenderer (TableCellRenderer renderer,
            int rowIndex, int vColIndex) {
        if (rowIndex < 0 || vColIndex < 0) {
            return null;
        }
        Component c = super.prepareRenderer (renderer, rowIndex, vColIndex);
        Color bgColor = getBackground ();
        Color bgColorDarker = getDarkerColor(bgColor);
        
        Unit u = model.getUnitAtRow(convertRowIndexToModel(rowIndex));
        if (u != null && !u.canBeMarked ()) {
            c.setForeground (Color.gray);
        } else {
            if (vColIndex == 1 && u != null && UpdateManager.TYPE.FEATURE.equals(u.updateUnit.getType())) {
                c.setFont(getFont().deriveFont(java.awt.Font.BOLD));
            } else {
                c.setFont(getFont());
            }
            if (isRowSelected(rowIndex)) {
                c.setForeground(getSelectionForeground());
            } else {
                c.setForeground(getForeground());
            }
        }
        if (!isCellSelected (rowIndex, vColIndex)) {
            if (rowIndex % 2 == 0 && !model.isExpansionControlAtRow(rowIndex)) {
                c.setBackground (bgColorDarker);
            } else {
                c.setBackground (bgColor);
            }
        } else if (model.isExpansionControlAtRow(rowIndex)) {
            c.setBackground (getBackground ());
            c.setForeground(getForeground());
            JComponent jc = (JComponent)c;
            jc.setBorder(BorderFactory.createEmptyBorder());
        }
        int fontHeight = c.getFontMetrics(c.getFont()).getHeight();
        if (rowHeight < 0 || rowHeight < fontHeight) {
            int def = new JTable().getRowHeight();
            rowHeight = Math.max(def, fontHeight);
            setRowHeight(rowHeight);
        }
        
        return c;
    }
    
    static Color getDarkerColor(Color color) {
        return new Color(
                Math.abs(color.getRed() - DARKER_COLOR_COMPONENT),
                Math.abs(color.getGreen() - DARKER_COLOR_COMPONENT),
                Math.abs(color.getBlue() - DARKER_COLOR_COMPONENT));
        
    }
    
    @Override
    protected JTableHeader createDefaultTableHeader () {
        return new MyTableHeader ( columnModel );
    }
    
    private class MyTableHeader extends JTableHeader {        
        public MyTableHeader ( TableColumnModel model ) {
            super ( model );
            this.setReorderingAllowed ( false );
        }
        
        @Override
        public void setDraggedColumn ( TableColumn aColumn ) {
            if( null != aColumn && aColumn.getModelIndex () == 0 )
                return; //don't allow the first column to be dragged
            super.setDraggedColumn ( aColumn );
        }        
        
        @Override
        public void setResizingColumn ( TableColumn col ) {
            if( null != getResizingColumn () && null == col ) {
                //maybe could be persistent later
                //storeColumnState();
            }
            super.setResizingColumn ( col );
        }        
    }        
    
    private class MoreRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent (JTable table, Object value,
                              boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component res = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
            
            if (res == null || value == null) {
                return res;
            }
            if (column == 1 && res instanceof JLabel) {
                JLabel original = (JLabel)res;
                StringBuilder text = new StringBuilder();
                if (isSelected || hasFocus) {
                    text.append("<b>").append(model.getExpansionControlText()).append("</b>");//NOI18N                    
                } else {
                    text.append(model.getExpansionControlText());
                }
                setEnabled(isSelected);
                original.setText("<html>" + "<a href=\"\">" + text.toString() + "</a></html>");//NOI18N
            } else if (column != 1) {
                res = new JLabel();
            }
            
            return res;
        }
    }    
}
