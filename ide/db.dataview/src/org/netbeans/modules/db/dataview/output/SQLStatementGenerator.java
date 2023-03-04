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

import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBMetaDataFactory;
import org.netbeans.modules.db.dataview.meta.DBPrimaryKey;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.util.BinaryToStringConverter;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.util.NbBundle;

/**
 * Generates DML for editable resultset
 *
 * @author Ahimanikya Satapathy
 */
class SQLStatementGenerator {
    private static final Logger LOG =
            Logger.getLogger(SQLStatementGenerator.class.getName());

    String generateInsertStatement(DBTable table, Object[] insertedRow) throws DBException {
        List<DBColumn> columns = table.getColumnList();

        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO "); // NOI18N

        StringBuilder colNames = new StringBuilder(" ("); // NOI18N
        StringBuilder values = new StringBuilder();
        String commaStr = ", "; // NOI18N
        boolean comma = false;
        for (int i = 0; i < insertedRow.length; i++) {
            DBColumn dbcol = columns.get(i);
            Object val = insertedRow[i];

            if (dbcol.isGenerated()) { // NOI18N
                continue;
            }

            if ((val == null || val.equals("<NULL>")) && !dbcol.isNullable()) { // NOI18N
                throw new DBException(NbBundle.getMessage(SQLStatementGenerator.class, "MSG_nullable_check"));
            }

            if (comma) {
                values.append(commaStr);
                colNames.append(commaStr);
            } else {
                comma = true;
            }

            // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
            if (val instanceof SQLConstant) {
                values.append(((SQLConstant) val).name());
            } else { // ELSE literals
                values.append(val == null ? " NULL " : "?"); // NOI18N
            }
            colNames.append(dbcol.getQualifiedName(true));
        }

        colNames.append(")"); // NOI18N
        insertSql.append(table.getFullyQualifiedName(true));
        insertSql.append(colNames.toString());
        insertSql.append(" Values("); // NOI18N
        insertSql.append(values.toString());
        insertSql.append(")"); // NOI18N

        return insertSql.toString();
    }

    String generateRawInsertStatement(DBTable table, Object[] insertedRow) throws DBException {
        List<DBColumn> columns = table.getColumnList();

        StringBuilder rawInsertSql = new StringBuilder();
        rawInsertSql.append("INSERT INTO "); // NOI18N

        String rawcolNames = " ("; // NOI18N
        String rawvalues = "";  // NOI18N
        String commaStr = ", "; // NOI18N
        boolean comma = false;
        for (int i = 0; i < insertedRow.length; i++) {
            DBColumn dbcol = columns.get(i);
            Object val = insertedRow[i];

            if (dbcol.isGenerated()) { // NOI18N
                continue;
            }

            if ((val == null || val.equals("<NULL>")) && !dbcol.isNullable()) { // NOI18N
                throw new DBException(NbBundle.getMessage(SQLStatementGenerator.class, "MSG_nullable_check"));
            }

            if (comma) {
                rawvalues += commaStr;
                rawcolNames += commaStr;
            } else {
                comma = true;
            }

            // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
            if (val instanceof SQLConstant) {
                rawvalues += (((SQLConstant) val).name());
            } else { // ELSE literals
                rawvalues += getQualifiedValue(dbcol.getJdbcType(), insertedRow[i]);
            }
            rawcolNames += dbcol.getQualifiedName(false);
        }

        rawcolNames += ")"; // NOI18N
        rawInsertSql.append(table.getFullyQualifiedName(false));
        rawInsertSql.append(rawcolNames);
        rawInsertSql.append(" \n\tVALUES (");  // NOI18N
        rawInsertSql.append(rawvalues);
        rawInsertSql.append(")"); // NOI18N

        return rawInsertSql.toString();
    }

    String generateUpdateStatement(DBTable table, int row, Map<Integer, Object> changedRow, List<Object> values, List<Integer> types, DataViewTableUIModel tblModel) throws DBException {
        List<DBColumn> columns = table.getColumnList();

        StringBuilder updateStmt = new StringBuilder();
        updateStmt.append("UPDATE ").append(table.getFullyQualifiedName(true)).append(" SET "); // NOI18N
        String commaStr = ", "; // NOI18N
        boolean comma = false;
        for (Integer col : changedRow.keySet()) {
            DBColumn dbcol = columns.get(col);
            Object value = changedRow.get(col);
            int type = dbcol.getJdbcType();

            if ((value == null || value.equals("<NULL>")) && !dbcol.isNullable()) { // NOI18N
                throw new DBException(NbBundle.getMessage(SQLStatementGenerator.class, "MSG_nullable_check"));
            }

            if (comma) {
                updateStmt.append(commaStr);
            } else {
                comma = true;
            }

            updateStmt.append(dbcol.getQualifiedName(true));
            // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
            if (value instanceof SQLConstant) {
                updateStmt.append(" = ").append(((SQLConstant) value).name());
            // NULL ist reported as an SQL constant, so treat it as such
            } else if ( value == null ) {
                updateStmt.append(" = NULL"); // NOI18N
            } else { // ELSE literals
                updateStmt.append(" = ?"); // NOI18N
                values.add(value);
                types.add(type);
            }
        }

        updateStmt.append(" WHERE "); // NOI18N
        generateWhereCondition(table, updateStmt, types, values, row, tblModel);
        return updateStmt.toString();
    }

    String generateUpdateStatement(DBTable table, int row, Map<Integer, Object> changedRow, DataViewTableUIModel tblModel) throws DBException {
        List<DBColumn> columns = table.getColumnList();

        StringBuilder rawUpdateStmt = new StringBuilder();
        rawUpdateStmt.append("UPDATE ").append(table.getFullyQualifiedName(false)).append(" SET "); // NOI18N

        String commaStr = ", "; // NOI18N
        boolean comma = false;
        for (Integer col : changedRow.keySet()) {
            DBColumn dbcol = columns.get(col);
            Object value = changedRow.get(col);
            int type = dbcol.getJdbcType();

            if ((value == null || value.equals("<NULL>")) && !dbcol.isNullable()) { // NOI18N
                throw new DBException(NbBundle.getMessage(SQLStatementGenerator.class, "MSG_nullable_check"));
            }

            if (comma) {
                rawUpdateStmt.append(commaStr);
            } else {
                comma = true;
            }

            rawUpdateStmt.append(dbcol.getQualifiedName(true));
            // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
            if (value instanceof SQLConstant) {
                rawUpdateStmt.append(" = ").append(((SQLConstant) value).name());
            } else { // ELSE literals
                rawUpdateStmt.append(" = ").append(getQualifiedValue(type, value).toString());
            }
        }

        rawUpdateStmt.append(" WHERE "); // NOI18N
        generateWhereCondition(table, rawUpdateStmt, row, tblModel);
        return rawUpdateStmt.toString();
    }

    String generateDeleteStatement(DBTable table, List<Integer> types, List<Object> values, int rowNum, DataViewTableUIModel tblModel) {
        StringBuilder deleteStmt = new StringBuilder();
        deleteStmt.append("DELETE FROM ").append(table.getFullyQualifiedName(true)).append(" WHERE "); // NOI18N

        generateWhereCondition(table, deleteStmt, types, values, rowNum, tblModel);
        return deleteStmt.toString();
    }

    String generateDeleteStatement(DBTable table, int rowNum, DataViewTableUIModel tblModel) {
        StringBuilder rawDeleteStmt = new StringBuilder();
        rawDeleteStmt.append("DELETE FROM ").append(table.getFullyQualifiedName(false)).append(" WHERE "); // NOI18N

        generateWhereCondition(table, rawDeleteStmt, rowNum, tblModel);
        return rawDeleteStmt.toString();
    }

    // TODO: Support for FK, and other constraint and Index recreation.
    String generateCreateStatement(DBTable table) throws DBException, Exception {
        boolean isdb2 = table.getParentObject().getDBType() == DBMetaDataFactory.DB2;

        StringBuilder sql = new StringBuilder();
        List<DBColumn> columns = table.getColumnList();
        sql.append("CREATE TABLE ").append(table.getQualifiedName(false)).append(" ("); // NOI18N
        int count = 0;
        for (DBColumn col : columns) {
            if (count++ > 0) {
                sql.append(", "); // NOI18N
            }

            String typeName = col.getTypeName();
            sql.append(col.getQualifiedName(false)).append(" ");

            int scale = col.getScale();
            int precision = col.getPrecision();
            if (precision > 0 && DataViewUtils.isPrecisionRequired(col.getJdbcType(), isdb2)) {
                if (typeName.contains("(")) { // Handle MySQL Binary Type // NOI18N
                    sql.append(typeName.replace("(", "(" + precision)); // NOI18N
                } else {
                    sql.append(typeName).append("(").append(precision); // NOI18N
                    if (scale > 0 && DataViewUtils.isScaleRequired(col.getJdbcType())) {
                        sql.append(", ").append(scale).append(")"); // NOI18N
                    } else {
                        sql.append(")"); // NOI18N
                    }
                }
            } else {
                sql.append(typeName);
            }

            if (DataViewUtils.isBinary(col.getJdbcType()) && isdb2) {
                sql.append("  FOR BIT DATA "); // NOI18N
            }

            if (col.hasDefault()) {
                sql.append(" DEFAULT ").append(col.getDefaultValue()).append(" "); // NOI18N
            }

            if (!col.isNullable()) {
                sql.append(" NOT NULL"); // NOI18N
            }

            if (col.isGenerated()) {
                sql.append(" ").append(getAutoIncrementText(table.getParentObject().getDBType()));
            }
        }

        DBPrimaryKey pk = table.getPrimaryKey();
        if (pk != null) {
            count = 0;
            sql.append(", PRIMARY KEY ("); // NOI18N
            for (String col : pk.getColumnNames()) {
                if (count++ > 0) {
                    sql.append(", "); // NOI18N
                }
                sql.append(table.getQuoter().quoteIfNeeded(col));
            }
            sql.append(")"); // NOI18N
        }
        sql.append(")"); // NOI18N

        return sql.toString();
    }

    private boolean addSeparator(boolean and, StringBuilder sql, String sep) {
        if (and) {
            sql.append(sep);
            return true;
        } else {
            return true;
        }
    }

    private void generateNameValue(DBColumn column, StringBuilder sql, Object value, List<Object> values, List<Integer> types) {
        sql.append(column.getQualifiedName(true));
        if (value != null) {
            values.add(value);
            types.add(column.getJdbcType());
            sql.append(" = ? "); // NOI18N
        } else { // Handle NULL value in where condition
            sql.append(" IS NULL "); // NOI18N
        }
    }

    private void generateNameValue(DBColumn column, StringBuilder sql, Object value) {
        String columnName = column.getQualifiedName(false);
        int type = column.getJdbcType();

        sql.append(columnName);
        if (value != null) {
            sql.append(" = ").append(getQualifiedValue(type, value)); // NOI18N
        } else { // Handle NULL value in where condition
            sql.append(" IS NULL"); // NOI18N
        }
    }

    void generateWhereCondition(DBTable table, StringBuilder result, List<Integer> types, List<Object> values, int rowNum, DataViewTableUIModel model) {
        assert SwingUtilities.isEventDispatchThread() : "Needs to be called on the EDT";

        DBPrimaryKey key = table.getPrimaryKey();
        Set<String> columnsSelected = new HashSet<>();
        boolean and = false;

        List<DBColumn> columns = table.getColumnList();

        StringBuilder pkSelect = new StringBuilder();
        List<Integer> pkTypes = new ArrayList<>();
        List<Object> pkObject = new ArrayList<>();

        if (key != null) {
            for (String keyName : key.getColumnNames()) {
                for (int i = 0; i < model.getColumnCount(); i++) {
                    DBColumn dbcol = columns.get(i);
                    String columnName = dbcol.getName();
                    if (columnName.equals(keyName)) {
                        Object val = model.getOriginalValueAt(rowNum, i);
                        if (val != null) {
                            columnsSelected.add(columnName);
                            and = addSeparator(and, pkSelect, " AND "); // NOI18N
                            generateNameValue(dbcol, pkSelect, val, pkObject, pkTypes);
                            break;
                        }
                    }
                }
            }
        }

        if (key != null && columnsSelected.equals(new HashSet<>(key.getColumnNames()))) {
            result.append(pkSelect);
            types.addAll(pkTypes);
            values.addAll(pkObject);
        } else {
            and = false;
            for (int i = 0; i < model.getColumnCount(); i++) {
                DBColumn dbcol = columns.get(i);
                Object val = model.getOriginalValueAt(rowNum, i);
                and = addSeparator(and, result, " AND "); // NOI18N
                generateNameValue(dbcol, result, val, values, types);
            }
        }
    }

    void generateWhereCondition(DBTable table, StringBuilder sql, int rowNum, DataViewTableUIModel model) {
        assert SwingUtilities.isEventDispatchThread() : "Needs to be called on the EDT";

        DBPrimaryKey key = table.getPrimaryKey();
        Set<String> columnsSelected = new HashSet<>();
        boolean and = false;

        List<DBColumn> columns = table.getColumnList();

        StringBuilder pkSelect = new StringBuilder();

        if (key != null) {
            for (String keyName : key.getColumnNames()) {
                for (int i = 0; i < model.getColumnCount(); i++) {
                    DBColumn dbcol = columns.get(i);
                    String columnName = dbcol.getName();
                    if (columnName.equals(keyName)) {
                        Object val = model.getOriginalValueAt(rowNum, i);
                        if (val != null) {
                            columnsSelected.add(columnName);
                            and = addSeparator(and, pkSelect, " AND "); // NOI18N
                            generateNameValue(dbcol, pkSelect, val);
                            break;
                        }
                    }
                }
            }
        }

        if (key != null && columnsSelected.equals(new HashSet<>(key.getColumnNames()))) {
            sql.append(pkSelect);
        } else {
            and = false;
            for (int i = 0; i < model.getColumnCount(); i++) {
                DBColumn dbcol = columns.get(i);
                Object val = model.getOriginalValueAt(rowNum, i);
                and = addSeparator(and, sql, " AND "); // NOI18N
                generateNameValue(dbcol, sql, val);
            }
        }
    }

    private Object getQualifiedValue(int type, Object val) {
        if (val == null) {
            return "NULL"; // NOI18N
        }
        if (type == Types.BIT && !(val instanceof Boolean)) {
            return "b'" + val + "'"; // NOI18N
        } else if (DataViewUtils.isNumeric(type)) {
            return val;
        } else if (val instanceof Clob) {
            try {
                Clob lob = (Clob) val;
                String result = lob.getSubString(1, (int) lob.length());
                return "'" + result.replace("'", "''") + "'"; //NOI18N
            } catch (SQLException ex) {
                LOG.log(Level.INFO, "Failed to read CLOB", ex); //NOI18N
            }
        } else if (val instanceof Blob) {
            try {
                Blob lob = (Blob) val;
                byte[] result = lob.getBytes(1, (int) lob.length());
                return "x'" + BinaryToStringConverter.convertToString(
                        result, 16, false) + "'"; // NOI18N
            } catch (SQLException ex) {
                LOG.log(Level.INFO, "Failed to read BLOB", ex); //NOI18N
            }
        }
        // Fallback if previous converts fail
        return "'" + val.toString().replace("'", "''") + "'"; //NOI18N
    }

    private String getAutoIncrementText(int dbType) throws Exception {
        switch (dbType) {
            case DBMetaDataFactory.MYSQL:
                return "AUTO_INCREMENT"; // NOI18N

            case DBMetaDataFactory.PostgreSQL:
                return "SERIAL"; // NOI18N

            case DBMetaDataFactory.SQLSERVER:
                return "IDENTITY"; // NOI18N
            default:
                return "GENERATED ALWAYS AS IDENTITY"; // NOI18N
        }
    }
}
