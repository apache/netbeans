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
package org.netbeans.modules.hibernate.loaders.cfg.multiview;

import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.hibernate.cfg.model.Event;
import org.openide.util.NbBundle;

/**
 * 
 * @author Dongmei Cao
 */
public class EventTableModel extends AbstractTableModel {

    private static final String[] columnNames = {
        NbBundle.getMessage(EventTableModel.class, "LBL_Class")
    ,
        };
    // Matches the attribute name used in org.netbeans.modules.hibernate.cfg.model.Event
    private static final String attrName = "Class";
    private Event event;

    public EventTableModel(Event event) {
        this.event = event;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
    // TODO
    }

    public Object getValueAt(int row, int column) {

        if (event == null) {
            return null;
        } else {
            String attrValue = event.getAttributeValue(Event.LISTENER, row, attrName);
            return attrValue;
        }
    }

    public int getRowCount() {
        if (event == null) {
            return 0;
        } else {
            return event.sizeListener();
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return (false);
    }

    public void addRow(String listenerClass) {
        
        int index = event.addListener(true);
        event.setAttributeValue(Event.LISTENER, index, attrName, listenerClass);

        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    public void editRow(int row, String listenerClass) {
        event.setAttributeValue(Event.LISTENER, row, attrName, listenerClass);

        fireTableRowsUpdated(row, row);
    }

    public void removeRow(int row) {
        event.removeListener(row);

        fireTableRowsDeleted(row, row);
    }
}
