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
package org.netbeans.modules.db.dataview.meta;

import java.util.Collections;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.db.dataview.util.DataViewUtils;

/**
 * Holds DB PrimaryKey meta info of a given table.
 *
 * @author Ahimanikya Satapathy
 */
public final class DBPrimaryKey extends DBObject<DBTable> {
    private static final String RS_COLUMN_NAME = "COLUMN_NAME"; // NOI18N
    private static final String RS_KEY_NAME = "PK_NAME"; // NOI18N
    private static final String RS_KEY_SEQ = "KEY_SEQ"; // NOI18N
    private List<String> columnNames;
    private String name;
    private DBTable parent;

    public DBPrimaryKey(ResultSet rs) throws SQLException {
        assert rs != null;

        Map<Integer,String> pkColumns = new HashMap<Integer,String>();

        while (rs.next()) {
            int keySeq = rs.getShort(RS_KEY_SEQ);
            String columName = rs.getString(RS_COLUMN_NAME);

            pkColumns.put(keySeq, columName);

            name = rs.getString(RS_COLUMN_NAME);
            String tmpName = rs.getString(RS_KEY_NAME);
            if (!DataViewUtils.isNullString(tmpName) && name == null) {
                name = tmpName;
            }
        }

        columnNames = new ArrayList<String>();

        List<Integer> columnEntries = new ArrayList<Integer>(pkColumns.keySet());
        Collections.sort(columnEntries);

        for(Integer id: columnEntries) {
            columnNames.add(pkColumns.get(id));
        }
    }

    public boolean contains(DBColumn col) {
        return contains(col.getName());
    }

    public boolean contains(String columnName) {
        return columnNames.contains(columnName);
    }

    @Override
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof DBPrimaryKey)) {
            return false;
        }

        DBPrimaryKey ref = (DBPrimaryKey) refObj;
        boolean result = (getName() != null) ? name.equals(ref.name) : (ref.name == null);
        result &= (columnNames != null) ? columnNames.equals(ref.columnNames) : (ref.columnNames != null);
        return result;
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    public String getName() {
        if (name == null && parent != null) {
            name = "PK_" + parent.getName(); // NOI18N
        }
        return name;
    }

    @Override
    public int hashCode() {
        int myHash = (getName() != null) ? name.hashCode() : 0;
        myHash += (columnNames != null) ? columnNames.hashCode() : 0;

        return myHash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(100);
        for (int i = 0; i < columnNames.size(); i++) {
            if (i != 0) {
                buf.append(","); // NOI18N
            }
            buf.append((columnNames.get(i)).trim());
        }
        return buf.toString();
    }
}
