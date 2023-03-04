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

package org.netbeans.modules.web.wizards;

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import org.openide.util.NbBundle;

/**
 *
 * @author  mk115033
 */
public class AttrTableModel extends AbstractTableModel {

    public AttrTableModel() {
    }

    private String[] colheaders = null;
    private Object[][] data = null;
    private int numCols;
    private int numRows=0;

    /** Creates a new instance of AttrTableModel */
    AttrTableModel(String[] headers) { 
        this.colheaders = headers; 
        numCols = colheaders.length;
        
    }
    
    AttrTableModel(String[] headers, Object[][] data) { 
        this.colheaders = headers; 
        numCols = colheaders.length;
        this.data=data;
        numRows = data.length;
    }
    
    @Override
    public String getColumnName(int col) { 
        String key = "LBL_"+colheaders[col]; //NOI18N
        return NbBundle.getMessage(AttrTableModel.class, key); 
    }

    public int getRowCount() {
        return numRows;
    }
    
    public int getColumnCount() {
        return numCols;
    }

    public Object getValueAt(int row, int col) { 
        return data[row][col];
    }
    
    public int addRow(String name, String type, boolean required, boolean rtexpr) { 
        Object[][] data2 = new Object[numRows+1][numCols]; 
        int i=0, j=0; 

        if(numRows > 0) { 
            for(j=0; j<numRows; ++j) 
                data2[j] = data[j]; 
        }

        data2[j][0] = name;
        data2[j][1] = type;
        data2[j][2] = required;
        data2[j][3] = rtexpr;
        data = data2; 
        numRows++;
        return j; 
    }

    public void removeRow(int row) { 
        Object[][] data2 = new Object[numRows-1][numCols]; 
        int newRowIndex = 0; 
        for(int i=0; i<numRows; ++i) { 
            if(i==row) continue; 
            data2[newRowIndex]=data[i]; 
            newRowIndex++;
        }
        data = data2; 
        numRows--;
    }

    public void setData(String name, String value, boolean required, boolean rtexpr, int row) { 
        data[row][0] = name;
        data[row][1] = value;
        data[row][2] = required;
        data[row][3] = rtexpr;
        fireTableChanged(new TableModelEvent(this, row)); 
    } 

    @Override
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public Object[][] getAttributes() {
        if (data==null) return new Object[][]{};
        else return data;
    }
}
