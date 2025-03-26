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
package org.netbeans.modules.db.dataview.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBTable;

/**
 * Wrapper class provides ordered columns and tooltips
 *
 * @author Ahimanikya Satapathy
 */
public class DataViewDBTable {

    private final DBTable[] dbTables;
    private final List<DBColumn> columns;

    public DataViewDBTable(Collection<DBTable> tables) {
        assert tables != null;

        dbTables = new DBTable[tables.size()];
        List<DBColumn> cols = new ArrayList<>();

        for (DBTable tbl : tables.toArray(dbTables)) {
            cols.addAll(tbl.getColumnList());
        }
        cols.sort(new ColumnOrderComparator());
        columns = Collections.unmodifiableList(cols);
    }

    public DBTable getTable(int index) {
        return dbTables[index];
    }

    public int getTableCount() {
        return dbTables.length;
    }

    public boolean hasOneTable() {
        return dbTables != null && dbTables.length == 1 && !dbTables[0].getName().equals("");
    }

    public String getFullyQualifiedName(int index, boolean quoteAlways) {
        return dbTables[index].getFullyQualifiedName(quoteAlways);
    }

    public DBColumn getColumn(int index) {
        return columns.get(index);
    }

    public int getColumnType(int index) {
        return columns.get(index).getJdbcType();
    }

    public String getColumnName(int index) {
        return columns.get(index).getName();
    }

    public String getQualifiedName(int index, boolean quoteAlways) {
        return columns.get(index).getQualifiedName(quoteAlways);
    }

    public int getColumnCount() {
        return columns.size();
    }

    public List<DBColumn> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public synchronized Map<String,DBColumn> getColumnMap() {
        Map<String, DBColumn> colMap = new HashMap<>();
        for (DBTable tbl : dbTables) {
            colMap.putAll(tbl.getColumns());
        }
        return Collections.unmodifiableMap(colMap);
    }

    private final class ColumnOrderComparator implements Comparator<DBColumn> {

        private ColumnOrderComparator() {
        }

        @Override
        public int compare(DBColumn col1, DBColumn col2) {
            return col1.getOrdinalPosition() - col2.getOrdinalPosition();
        }
    }
}
