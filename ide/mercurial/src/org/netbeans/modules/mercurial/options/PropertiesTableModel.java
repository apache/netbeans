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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.ui.properties.HgPropertiesNode;

/**
 *
 * @author Peter Pis
 */
public class PropertiesTableModel extends AbstractTableModel {
    
    public static final String COLUMN_NAME_NAME = "name"; // NOI18N
    public static final String COLUMN_NAME_VALUE = "value"; // NOI18N
    
    private HgPropertiesNode[] nodes;
    private String[] columns;
    
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(2); 
    
    {
        ResourceBundle loc = NbBundle.getBundle(PropertiesTableModel.class);    
        columnLabels.put(COLUMN_NAME_NAME, new String[] {loc.getString("CTL_PropertiesTable_Column_Name"), loc.getString("CTL_PropertiesTable_Column_Name")}); // NOI18N
        columnLabels.put(COLUMN_NAME_VALUE, new String[] {loc.getString("CTL_PropertiesTable_Column_Value"), loc.getString("CTL_PropertiesTable_Column_Value")}); // NOI18N
    }
    
    /** Creates a new instance of PropertiesTableModel */
    public PropertiesTableModel(String[] clms) {
        if (Arrays.equals(columns, clms))
            return;
        setColumns(clms);
        setNodes(new HgPropertiesNode[0]);
    }
    
    public void setColumns(String[] clms) {
        this.columns = clms;
        fireTableStructureChanged();
    }
    
    public void setNodes(HgPropertiesNode[] nodes) {
        this.nodes = nodes;
        fireTableDataChanged();
    }
    
    public HgPropertiesNode[] getNodes() {
        return nodes;
    }
    
    public HgPropertiesNode getNode(int row) {
        return nodes[row];
    }
    
    public int getRowCount() {
        return nodes.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnLabels.get(columns[column])[0];
    }
    
    public int getColumnCount() {
        return columns.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        String clm = columns[columnIndex];
        if (clm.equals(COLUMN_NAME_NAME)) {
            return nodes[rowIndex].getName();
        } else if (clm.equals(COLUMN_NAME_VALUE)) {
            return nodes[rowIndex].getValue();
        }
        throw new IllegalArgumentException("The column index is out of index: " + columnIndex); // NOI18N
    }

    
}
