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
 * MessageSecurityTableModel.java
 *
 * Created on April 24, 2006, 3:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity;

/**
 *
 * @author Peter Williams
 */
public class MessageSecurityTableModel extends AbstractTableModel {
    
//    private String [] columnNames = { "Operation / Java Method", "Request Protection", "Response Protection" };
    private static final String [] columnNames = { "Operation", "Req Source", "Req Target", "Resp Source", "Resp Target" };
    
    /** Hashset of all the rows.  Stores instances of MessageSecurity
     */
    private ArrayList rowData;
    
    public MessageSecurityTableModel(MessageSecurity [] ms) {
        if(ms != null) {
            rowData = new ArrayList(ms.length);
            for(int i = 0; i < ms.length; i++) {
                rowData.add(ms[i]);
            }
        } else {
            rowData = new ArrayList();
        }
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        if(rowIndex >= 0 && rowIndex < rowData.size()) {
            MessageSecurity row = (MessageSecurity) rowData.get(rowIndex);
            if(row != null) {
                result = getFieldByColumn(row, columnIndex);
            }
        }
        return result;
    }

    public int getRowCount() {
        return rowData.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    public String getColumnName(int column) {
        assert column < 0 || column > columnNames.length;
        return (column >= 0 && column < columnNames.length) ? columnNames[column] : "unknown";
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex > 0);
    }

    private String getFieldByColumn(MessageSecurity row, int columnIndex) {
        assert columnIndex < 0 || columnIndex > columnNames.length;
        switch(columnIndex) {
            case 0:
                return row.getMessage(0).getOperationName();
            case 1:
                return row.getRequestProtectionAuthSource();
            case 2: 
                return row.getRequestProtectionAuthRecipient();
            case 3:
                return row.getResponseProtectionAuthSource();
            case 4:
                return row.getResponseProtectionAuthRecipient();
        }
        return null;
    }

    private void setFieldByColumn(MessageSecurity row, int columnIndex, String field) {
    }
}
