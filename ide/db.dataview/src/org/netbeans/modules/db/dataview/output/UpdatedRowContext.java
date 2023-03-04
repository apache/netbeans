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
package org.netbeans.modules.db.dataview.output;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.table.TableModel;

/**
 * Holds the updated row data
 *
 * @author Ahimanikya Satapathy
 */
class UpdatedRowContext {

    private Map<Integer, Map<Integer, Object>> changedData = new LinkedHashMap<Integer, Map<Integer, Object>>();

    public UpdatedRowContext() {
    }

    public void addUpdates(int row, int col, Object value, TableModel tblModel)  {
        Map<Integer, Object> rowMap = changedData.get(row);
        if(rowMap == null){
            rowMap = new LinkedHashMap<Integer, Object>();
            changedData.put(Integer.valueOf(row), rowMap);
        }
        rowMap.put(Integer.valueOf(col), value);
    }

    public void removeAllUpdates() {
        changedData = new LinkedHashMap<Integer, Map<Integer, Object>>();
    }

    public void removeUpdateForSelectedRow(int row) {
        changedData.remove(Integer.valueOf(row));
    }

    public Set<Integer> getUpdateKeys() {
        return changedData.keySet();
    }

    public Map<Integer, Object> getChangedData(int row) {
        return changedData.get(Integer.valueOf(row));
    }

    boolean hasUpdates(int row, int col) {
        Map<Integer, Object> rowMap = changedData.get(Integer.valueOf(row));
        if(rowMap != null && rowMap.containsKey(Integer.valueOf(col))){
           return true;
        }
        return false;
    }
}
