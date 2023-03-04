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
package org.netbeans.modules.notifications.center;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.notifications.NotificationImpl;

/**
 *
 * @author jpeska
 */
public class NotificationTableModel extends AbstractTableModel {

    static final int PRIORITY_COLUMN = 0;
    static final int MESSAGE_COLUMN = 1;
    static final int TIMESTAMP_COLUMN = 2;
    static final int CATEGORY_COLUMN = 3;

    private static final int COLUMN_COUNT = 4;

    private final List<NotificationImpl> entries;

    public NotificationTableModel() {
        this.entries = new ArrayList<NotificationImpl>();
    }

    public void setEntries(List<NotificationImpl> entries) {
        this.entries.clear();
        this.entries.addAll(entries);
        fireTableDataChanged();
    }

    public NotificationImpl getEntry(int index) {
        return entries.get(index);
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        NotificationImpl notification = getEntry(rowIndex);
        if (notification == null) {
            return null;
        }
        switch (columnIndex) {
            case PRIORITY_COLUMN:
                return notification.getPriority();
            case TIMESTAMP_COLUMN:
                return notification.getDateCreated();
            case CATEGORY_COLUMN:
                return notification.getCategory().getDisplayName();
            case MESSAGE_COLUMN:
                return notification.getTitle();
            default:
                throw new IllegalStateException("Invalid columnIndex=" + columnIndex); // NOI18N
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    void updateIndex(int index) {
        fireTableRowsUpdated(index, index);
    }
}
