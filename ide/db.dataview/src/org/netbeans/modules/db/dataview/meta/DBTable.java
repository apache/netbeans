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
package org.netbeans.modules.db.dataview.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.modules.db.dataview.util.DataViewUtils;

/**
 * Represent Database Table
 * 
 * @author Ahimanikya Satapathy
 */
public final class DBTable extends DBObject<DBModel> {

    private static final String FQ_TBL_NAME_SEPARATOR = "."; // NOI18N
    private String catalog;
    private Map<String, DBColumn> columns;
    private Map<String, DBForeignKey> foreignKeys;
    private String name;
    private DBPrimaryKey primaryKey;
    private String schema;
    private Quoter quoter;

    public DBTable(String aName, String aSchema, String aCatalog) {
        columns = new LinkedHashMap<>();
        foreignKeys = new HashMap<>();

        name = (aName != null) ? aName.trim() : null;
        schema = (aSchema != null) ? aSchema.trim() : null;
        catalog = (aCatalog != null) ? aCatalog.trim() : null;
    }

    public synchronized boolean addColumn(DBColumn theColumn) {
        if (theColumn != null) {
            theColumn.setParentObject(this);
            columns.put(theColumn.getName() + theColumn.getOrdinalPosition(), theColumn);
            return true;
        }
        return false;
    }

    public int compareTo(Object refObj) {
        if (refObj == null) {
            return -1;
        }

        if (refObj == this) {
            return 0;
        }

        DBModel parentDBModel = getParentObject();
        String refName = (parentDBModel != null) ? parentDBModel.getFullyQualifiedTableName((DBTable) refObj) : ((DBTable) refObj).getName();
        String myName = (parentDBModel != null) ? parentDBModel.getFullyQualifiedTableName(this) : name;
        return (myName != null) ? myName.compareTo(refName) : (refName != null) ? 1 : -1;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        // Check for reflexivity first.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DBTable)) {
            return false;
        }

        result = super.equals(obj);

        if (!result) {
            return result;
        }

        // Check for castability (also deals with null obj)
        if (obj instanceof DBTable) {
            DBTable aTable = (DBTable) obj;
            String aTableName = aTable.getName();
            Map<String, DBColumn> aTableColumns = aTable.getColumns();
            DBPrimaryKey aTablePK = aTable.primaryKey;
            List<DBForeignKey> aTableFKs = aTable.getForeignKeys();

            result &= (aTableName != null && name != null && name.equals(aTableName));
            if (columns != null && aTableColumns != null) {
                Set<String> objCols = aTableColumns.keySet();
                Set<String> myCols = columns.keySet();

                // Must be identical (no subsetting), hence the pair of tests.
                result &= myCols.containsAll(objCols) && objCols.containsAll(myCols);
            } else if (!(columns == null && aTableColumns == null)) {
                result = false;
            }

            result &= (primaryKey != null) ? primaryKey.equals(aTablePK) : aTablePK == null;
            if (foreignKeys != null && aTableFKs != null) {
                Collection<DBForeignKey> myFKs = foreignKeys.values();
                // Must be identical (no subsetting), hence the pair of tests.
                result &= myFKs.containsAll(aTableFKs) && aTableFKs.containsAll(myFKs);
            } else if (!(foreignKeys == null && aTableFKs == null)) {
                result = false;
            }
        }
        return result;
    }

    public String getCatalog() {
        return catalog;
    }

    public Quoter getQuoter() {
        return quoter;
    }

    void setQuoter(Quoter quoter) {
        this.quoter = quoter;
    }

    public List<DBColumn> getColumnList() {
        List<DBColumn> list = new ArrayList<>(columns.values());
        list.sort(new ColumnOrderComparator());
        return list;
    }

    public Map<String, DBColumn> getColumns() {
        return columns;
    }

    public DBColumn getColumn(String name) {
        Collection<DBColumn> list = columns.values();
        for (DBColumn dBColumn : list) {
            if (dBColumn.getName().equalsIgnoreCase(name)) {
                return dBColumn;
            }
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return this.getFullyQualifiedName(false);
    }

    public List<DBForeignKey> getForeignKeys() {
        return new ArrayList<DBForeignKey>(foreignKeys.values());
    }

    public String getFullyQualifiedName(boolean quoteAlways) {
        StringBuilder buf = new StringBuilder(50);

        if (!DataViewUtils.isNullString(catalog)) {
            buf.append(quoteAlways ? quoter.quoteAlways(catalog.trim()) : quoter.quoteIfNeeded(catalog.trim()));
            buf.append(FQ_TBL_NAME_SEPARATOR);
        }

        if (!DataViewUtils.isNullString(schema)) {
            buf.append(quoteAlways ? quoter.quoteAlways(schema.trim()) : quoter.quoteIfNeeded(schema.trim()));
            buf.append(FQ_TBL_NAME_SEPARATOR);
        }
        if (quoter != null) {
            buf.append(quoteAlways ? quoter.quoteAlways(name.trim()) : quoter.quoteIfNeeded(name.trim()));
        } else {
            buf.append(name);
        }
        return buf.toString();
    }

    public String getQualifiedName(boolean quoteAlways) {
        if (quoter != null) {
            return quoteAlways ? quoter.quoteAlways(name.trim()) : quoter.quoteIfNeeded(name.trim());
        } else {
            return name.trim();
        }
    }

    public String getName() {
        return name;
    }

    public DBPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public String getSchema() {
        return schema;
    }

    @Override
    public int hashCode() {
        int myHash = super.hashCode();
        myHash = (name != null) ? name.hashCode() : 0;
        myHash += (schema != null) ? schema.hashCode() : 0;
        myHash += (catalog != null) ? catalog.hashCode() : 0;

        // Include hashCodes of all column names.
        if (columns != null) {
            myHash += columns.keySet().hashCode();
        }

        if (primaryKey != null) {
            myHash += primaryKey.hashCode();
        }

        if (foreignKeys != null) {
            myHash += foreignKeys.keySet().hashCode();
        }

        myHash += (displayName != null) ? displayName.hashCode() : 0;
        return myHash;
    }

    public void setName(String newName) {
        name = newName;
        setDisplayName(newName);
    }

    public void setCatalogName(String newName) {
        catalog = newName;
    }

    public void setSchemaName(String newName) {
        schema = newName;
    }

    boolean setPrimaryKey(DBPrimaryKey newPk) {
        if (newPk != null) {
            newPk.setParentObject(this);
        }

        primaryKey = newPk;
        return true;
    }

    void setForeignKeyMap(Map<String, DBForeignKey> fkMap) {
        foreignKeys = fkMap;
    }

    @Override
    public String toString() {
        return getFullyQualifiedName(false);
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

