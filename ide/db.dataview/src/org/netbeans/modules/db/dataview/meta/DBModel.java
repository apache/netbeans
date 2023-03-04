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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container object for database tables.
 *
 * @author Ahimanikya Satapathy
 */
public final class DBModel extends DBObject<Object> {

    private static final String FQ_TBL_NAME_SEPARATOR = "."; // NOI18N
    private Map<String, DBTable> tables;
    private int dbType;

    public DBModel() {
        tables = new HashMap<String, DBTable>();
    }

    public synchronized void addTable(DBTable table) {
        if (table != null) {
            String fqName = getFullyQualifiedTableName(table);
            table.setParentObject(this);
            tables.put(fqName, table);
        }
    }

    @Override
    public boolean equals(Object refObj) {
        // Check for reflexivity.
        if (this == refObj) {
            return true;
        }

        boolean result = false;

        // Ensure castability (also checks for null refObj)
        if (refObj instanceof DBModel) {
            DBModel aSrc = (DBModel) refObj;

            if (tables != null && aSrc.tables != null) {
                Set<String> objTbls = aSrc.tables.keySet();
                Set<String> myTbls = tables.keySet();

                // Must be identical (no subsetting), hence the pair of tests.
                boolean tblCheck = myTbls.containsAll(objTbls) && objTbls.containsAll(myTbls);
                result &= tblCheck;
            }
        }
        return result;
    }

    public String getFullyQualifiedTableName(DBTable tbl) {
        if (tbl != null) {
            String tblName = tbl.getName();
            String schName = tbl.getSchema();
            String catName = tbl.getCatalog();

            StringBuilder buf = new StringBuilder(50);

            if (catName != null && catName.trim().length() != 0) {
                buf.append(catName.trim());
                buf.append(FQ_TBL_NAME_SEPARATOR);
            }

            if (schName != null && schName.trim().length() != 0) {
                buf.append(schName.trim());
                buf.append(FQ_TBL_NAME_SEPARATOR);
            }

            buf.append(tblName.trim());
            return buf.toString();
        }

        return null;
    }

    public DBTable getTable(String fqTableName) {
        return this.tables.get(fqTableName);
    }

    public int getDBType() {
        return dbType;
    }

    @Override
    public int hashCode() {
        int myHash = 0;
        if (tables != null) {
            myHash += tables.keySet().hashCode();
        }
        return myHash;
    }

    @Override
    public String toString() {
        return this.getDisplayName();
    }

    void setDBType(int dbType) {
        this.dbType = dbType;
    }
}
