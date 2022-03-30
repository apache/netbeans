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
package org.netbeans.modules.mercurial.options;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.versioning.util.TableSorter;
import org.netbeans.modules.mercurial.ui.properties.HgPropertiesNode;
import org.netbeans.modules.mercurial.options.PropertiesTableModel;
import org.netbeans.modules.versioning.util.SortedTable;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Pis
 */
public class PropertiesTable implements AncestorListener, TableModelListener {
    
    public static final String[] PROPERTIES_COLUMNS = new String[] {PropertiesTableModel.COLUMN_NAME_NAME, PropertiesTableModel.COLUMN_NAME_VALUE};
            
    private PropertiesTableModel tableModel;
    private JTable table;
    private TableSorter sorter;
    private JComponent component;
    private String[] columns;
    private String[] sortByColumns;
    
    /** Creates a new instance of PropertiesTable */
    public PropertiesTable(JLabel label, String[] columns, String[] sortByColumns) {
        init(label, columns, null);
        this.sortByColumns = sortByColumns;
        setSortingStatus();
    }
    
    public PropertiesTable(JLabel label, String[] columns, TableSorter sorter) {
        init(label, columns, sorter);
    } 
    
    private void init(JLabel label, String[] columns, TableSorter sorter) {
        tableModel = new PropertiesTableModel(columns);
        tableModel.addTableModelListener(this);
        if(sorter == null) {
            sorter = new TableSorter(tableModel);
        } 
        this.sorter = sorter;   
        table = new SortedTable(this.sorter);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(String.class, new PropertiesTableCellRenderer());
        //table.setDefaultEditor(CommitOptions.class, new CommitOptionsCellEditor());
        table.getTableHeader().setReorderingAllowed(true);
        table.setRowHeight(table.getRowHeight());
        table.addAncestorListener(this);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        component.setPreferredSize(new Dimension(340, 150));
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PropertiesTable.class, "ACSD_PropertiesTable")); // NOI18N        
        label.setLabelFor(table);
        setColumns(columns);
    }
    
    public void setColumns(String[] clmns) {
        if (Arrays.equals(columns, clmns))
            return;
        columns = clmns;
        tableModel.setColumns(clmns);
        setDefaultColumnSize();
    }
    
    public JTable getTable() {
        return table;
    }
    
    private void setDefaultColumnSize() {
        int width = table.getWidth();
        TableColumnModel columnModel = table.getColumnModel();
        if (columns == null || columnModel == null)
            return;
        if (columnModel.getColumnCount() != columns.length)
            return;
        for (int i = 0; i < columns.length; i++) {
            String col = columns[i];                                
            sorter.setColumnComparator(i, null);                    
            if (col.equals(PropertiesTableModel.COLUMN_NAME_NAME)) {
                columnModel.getColumn(i).setPreferredWidth(width * 20 / 100);
            } else if (col.equals(PropertiesTableModel.COLUMN_NAME_VALUE)) {
                columnModel.getColumn(i).setPreferredWidth(width * 40 / 100);
            }
        }
    }
    
    private void setSortingStatus() {
        for (int i = 0; i < sortByColumns.length; i++) {
            String sortByColumn = sortByColumns[i];        
            for (int j = 0; j < columns.length; j++) {
                String column = columns[j];
                if(column.equals(sortByColumn)) {
                    sorter.setSortingStatus(j, column.equals(sortByColumn) ? TableSorter.ASCENDING : TableSorter.NOT_SORTED);                       
                    break;
                }                    
            }                        
        }        
    }
    
    TableModel getTableModel() {
        return tableModel;
    }
    
    void dataChanged() {
        int idx = table.getSelectedRow();
        tableModel.fireTableDataChanged();
        if (idx != -1) {
            table.getSelectionModel().addSelectionInterval(idx, idx);
        }    
    }
    
    public int getModelIndex(int viewIndex) {
        return sorter.modelIndex(viewIndex);
    }
    
    public int[] getSelectedItems() {
        return table.getSelectedRows();
    }
     
    public HgPropertiesNode[] getNodes() {
        return tableModel.getNodes();
    }
    
    public void setNodes(HgPropertiesNode[] nodes) {
        tableModel.setNodes(nodes);
    }
    
    public JComponent getComponent() {
        return component;
    }
    
    public void ancestorAdded(AncestorEvent arg0) {
        setDefaultColumnSize();
    }

    public void ancestorRemoved(AncestorEvent arg0) {
    }

    public void ancestorMoved(AncestorEvent arg0) {
    }

    public void tableChanged(TableModelEvent event) {
        table.repaint();
    }

    public class PropertiesTableCellRenderer extends DefaultTableCellRenderer {
           
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
            Component renderer =  super.getTableCellRendererComponent(table, value, hasFocus, hasFocus, rowIndex, columnIndex);
            if (renderer instanceof JComponent) {
                String strValue = tableModel.getNode(sorter.modelIndex(rowIndex)).getValue(); 
                ((JComponent) renderer).setToolTipText(strValue);
            }
            setToolTipText(value.toString());
            return renderer;
        }
    }
    
    
}
