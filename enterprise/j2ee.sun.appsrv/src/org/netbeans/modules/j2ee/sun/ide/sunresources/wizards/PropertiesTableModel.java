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
/*
 * PropertiesTableModel.java
 *
 * Created on October 1, 2003, 9:18 AM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.util.Vector;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author  nityad
 */
public class PropertiesTableModel extends javax.swing.table.AbstractTableModel {
    private static java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.Bundle"); // NOI18N
    private Vector data = null;
    /** Creates a new instance of PropertiesTableModel */
    public PropertiesTableModel(ResourceConfigData data) {
        this.data = data.getProperties();   //NOI18N
    }
    
    public int getColumnCount() {
        return 2;
    }
    
    public int getRowCount() {
        return data.size();
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        NameValuePair pair = (NameValuePair)data.elementAt(rowIndex);
        if (columnIndex == 0) 
            return pair.getParamName();
        else
            return pair.getParamValue();
    }
    
    public String getColumnName(int col) {
        if (0 == col) 
            return bundle.getString("COL_HEADER_NAME"); //NOI18N
        if (1 == col)
            return bundle.getString("COL_HEADER_VALUE"); //NOI18N
        throw new RuntimeException(bundle.getString("COL_HEADER_ERR_ERR_ERR")); //NOI18N
    }
    
    public boolean isCellEditable(int row, int col) {
       return true;
    }
    
    public void setValueAt(Object value, int row, int col) {
        if((row >=0) && (row < data.size())){
            NameValuePair property = (NameValuePair)data.elementAt(row);
            if (col == 0){
                if(! isNotUnique((String)value))
                    property.setParamName((String)value);
            }else if (col == 1)
                property.setParamValue((String)value);
        }    
        fireTableDataChanged();
    }

    //Fix for bug#5026041 - Table should not accept duplicate prop names.
    private boolean isNotUnique(String newVal){
        for(int i=0; i<data.size()-1; i++){
            NameValuePair pair = (NameValuePair)data.elementAt(i);
            if(pair.getParamName().equals(newVal)){
                NotifyDescriptor d = new NotifyDescriptor.Message(bundle.getString("Err_DuplicateValue"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return true;
            }    
        }
        return false;
    }
    
    public void setData(ResourceConfigData data) {
        this.data = data.getProperties();
        fireTableDataChanged();
    }
    
////    private boolean changed = false;
}
