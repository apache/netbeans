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
/**
 * MappingTable.java
 *
 * @author Ana von Klopp
 */
package org.netbeans.modules.web.wizards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelListener;
import org.openide.util.NbBundle;

class MappingTable extends JTable {

    // Handle resizing for larger fonts
    private boolean fontChanged = true;
    private int margin = 6;
    private static final long serialVersionUID = 3482048644419079279L;

    MappingTable(String filterName, List<FilterMappingData> filterMappings) {
        super();
        this.setModel(new MappingTableModel(filterName, filterMappings));

        TableColumnModel tcm = this.getColumnModel();

        // The filter name - this one is never editable
        tcm.getColumn(0).setPreferredWidth(72);

        // The pattern or servlet that we match to
        // This editor depends on whether the value of the other is
        // URL or Servlet
        tcm.getColumn(1).setPreferredWidth(72);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //setColors(false);
        setIntercellSpacing(new Dimension(margin, margin));
    }

    List<FilterMappingData> getFilterMappings() {
        return ((MappingTableModel) this.getModel()).getFilterMappings();
    }

    void setFilterName(String name) {
        ((MappingTableModel) this.getModel()).setFilterName(name);
        this.invalidate();
    }

    void addRow(FilterMappingData fmd) {
        this.invalidate();
        ((MappingTableModel) getModel()).addRow(fmd);
    }

    void addRow(int row, FilterMappingData fmd) {
        this.invalidate();
        ((MappingTableModel) getModel()).addRow(row, fmd);
    }

    void setRow(int row, FilterMappingData fmd) {
        this.invalidate();
        ((MappingTableModel) getModel()).setRow(row, fmd);
    }

    FilterMappingData getRow(int row) {
        return ((MappingTableModel) getModel()).getRow(row);
    }

    void moveRowUp(int row) {
        ((MappingTableModel) getModel()).moveRowUp(row);
        getSelectionModel().setSelectionInterval(row - 1, row - 1);
        this.invalidate();
    }

    void moveRowDown(int row) {
        ((MappingTableModel) getModel()).moveRowUp(row + 1);
        getSelectionModel().setSelectionInterval(row + 1, row + 1);
        this.invalidate();
    }

    void removeRow(int row) {
        ((MappingTableModel) getModel()).removeRow(row);
        this.invalidate();
        return;
    }

    @Override
    public void setValueAt(Object o, int row, int col) {
        return;
    }

    private void setColors(boolean editable) {
        Color bg;
        this.setBorder(BorderFactory.createLoweredBevelBorder());
        if (!editable) {
            bg = this.getBackground().darker();
        } else {
            bg = Color.white;
        }
        this.setBackground(bg);
    }

    void addTableModelListener(TableModelListener tml) {
        TableModel tableModel = getModel();
        if (tableModel != null) {
            tableModel.addTableModelListener(tml);
        }
    }

    void removeTableModelListener(TableModelListener tml) {
        TableModel tableModel = getModel();
        if (tableModel != null) {
            tableModel.removeTableModelListener(tml);
        }
    }

    @Override
    public void setFont(Font f) {
        fontChanged = true;
        super.setFont(f);
    }

    /** 
     * When paint is first invoked, we set the rowheight based on the
     * size of the font. */
    @Override
    public void paint(Graphics g) {
        if (fontChanged) {
            fontChanged = false;

            int height = 0;
            FontMetrics fm = g.getFontMetrics(getFont());
            height = fm.getHeight() + margin;
            if (height > rowHeight) {
                rowHeight = height;
            }
            //triggers paint, just return afterwards
            this.setRowHeight(rowHeight);
            return;
        }
        super.paint(g);
    }

    private static class MappingTableModel extends AbstractTableModel {

        private final String[] colheaders = {
            NbBundle.getMessage(MappingTable.class, "LBL_filter_name"),
            NbBundle.getMessage(MappingTable.class, "LBL_applies_to"),};
        private List<FilterMappingData> filterMappings = null;
        private String filterName;
        private static final long serialVersionUID = 2845252365404044474L;

        MappingTableModel(String filterName, List<FilterMappingData> filterMappings) {
            this.filterName = filterName;
            this.filterMappings = filterMappings;
        }

        List<FilterMappingData> getFilterMappings() {
            return filterMappings;
        }

        void setFilterName(String name) {
            Iterator<FilterMappingData> i = filterMappings.iterator();
            FilterMappingData fmd;
            while (i.hasNext()) {
                fmd = i.next();
                if (fmd.getName().equals(filterName)) {
                    fmd.setName(name);
                }
            }
            this.filterName = name;
        }

        public int getColumnCount() {
            return colheaders.length;
        }

        public int getRowCount() {
            return filterMappings.size();
        }

        @Override
        public String getColumnName(int col) {
            return colheaders[col];
        }

        public Object getValueAt(int row, int col) {
            FilterMappingData fmd = filterMappings.get(row);
            if (col == 0) {
                return fmd.getName();
            } else {
                return fmd.getPattern();
            }
        }

        @Override
        public Class getColumnClass(int c) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            return;
        }

        void addRow(int row, FilterMappingData fmd) {
            filterMappings.add(row, fmd);
        }

        void addRow(FilterMappingData fmd) {
            filterMappings.add(fmd);
        }

        FilterMappingData getRow(int row) {
            return filterMappings.get(row);
        }

        void setRow(int row, FilterMappingData fmd) {
            filterMappings.set(row, fmd);
        }

        void moveRowUp(int row) {
            FilterMappingData o = filterMappings.remove(row);
            filterMappings.add(row - 1, o);
        }

        void removeRow(int row) {
            filterMappings.remove(row);
        }
    } // MappingTableModel
} // MappingTable
