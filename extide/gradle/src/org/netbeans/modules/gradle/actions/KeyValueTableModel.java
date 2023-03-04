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

package org.netbeans.modules.gradle.actions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Laszlo Kishalmi
 */
public class KeyValueTableModel extends  AbstractTableModel {

    final String keyPrefix;
    final String[] keys;
    final String[] values;

    public KeyValueTableModel(String keyPrefix, String... keys) {
        this(keyPrefix, keys, new String[keys.length]);
        Arrays.fill(values, "");
    }

    public KeyValueTableModel(String keyPrefix, String[] keys, String[] values) {
        this.keyPrefix = keyPrefix;
        this.keys = keys;
        this.values = values;
    }

    @Override
    public int getRowCount() {
        return keys.length;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    @Messages({
        "LBL_KVTM_Key=Parameter",
        "LBL_KVTM_Value=Value"
    })
    public String getColumnName(int columnIndex) {
        return columnIndex == 0 ? Bundle.LBL_KVTM_Key() : Bundle.LBL_KVTM_Value();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return columnIndex == 0 ? keys[rowIndex] : values[rowIndex];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            values[rowIndex] = aValue.toString();
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    public Map<String, String> getProperties() {
        Map<String, String> ret = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            ret.put(keyPrefix + keys[i], values[i]);
        }
        return ret;
    }
}
