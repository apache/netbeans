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

package org.netbeans.modules.db.test.jdbcstub;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.test.stub.api.StubDelegate;

/**
 *
 * @author Andrei Badea
 */
public class ResultSetImpl extends StubDelegate {
    
    private List<Object> columns;
    private Map<String, Iterator> names2iterators = new HashMap<>();
    private Map<String, Object> names2values; // current row values

    public ResultSetImpl(List columns) {
        this.columns = columns;

        for (Iterator it = columns.iterator(); it.hasNext();) {
            List column = (List)it.next();
            Iterator columnIterator = column.iterator();
            String columnName = columnIterator.next().toString();
            names2iterators.put(columnName, columnIterator);
        }
    }

    public boolean next() {
        if (names2values != null) {
            names2values.clear();
        } else {
            names2values = new HashMap<>();
        }
        
        Iterator it = names2iterators.entrySet().iterator();
        if (!it.hasNext()) {
            return false;
        }

        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String columnName = (String)entry.getKey();
            Iterator columnIterator = (Iterator)entry.getValue();

            if (!columnIterator.hasNext()) {
                return false;
            }

            Object value = columnIterator.next();
            names2values.put(columnName, value);
        }
        
        return true;
    }
    
    public Object getObject(String columnName) throws SQLException {
        if (names2values == null) {
            throw new SQLException("The next() method has not been called yet");
        }
        if (!names2values.containsKey(columnName)) {
            throw new SQLException("Unknown column name " + columnName + ".");
        }
        return names2values.get(columnName);
    }
    
    public short getShort(String columnName) throws SQLException {
        Object value = getObject(columnName);
        if (value instanceof Short) {
            return ((Short)value);
        } else {
            throw new SQLException(value + "is not a short.");
        }
    }
    
    public int getInt(String columnName) throws SQLException {
        Object value = getObject(columnName);
        if (value instanceof Integer){
            return ((Integer)value);
        } else {
            throw new SQLException(value + " is not an int.");
        }
    }
    
    public boolean getBoolean(String columnName) throws SQLException {
        Object value = getObject(columnName);
        if (value instanceof Boolean) {
            return ((Boolean)value);
        } else {
            throw new SQLException(value + " is not a boolean.");
        }
    }

    public String getString(String columnName) throws SQLException {
        Object value = getObject(columnName);
        return value != null ? value.toString() : null;
    }
    
    public void close() {
    }
}
