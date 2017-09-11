/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.db.test.jdbcstub;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.test.stub.api.Stub;

/**
 *
 * @author Andrei Badea
 */
public final class JDBCStubUtil {
    
    // TODO need methods for returning the resultset columns, not just the resultsets
    
    private JDBCStubUtil() {
    }
    
    public static ResultSet createResultSet(List/*<List<Object>>*/ columns) {
        return (ResultSet)Stub.create(ResultSet.class, new ResultSetImpl(columns));
    }
    
    public static ResultSet singleColumnResultSet(String columnName, String[] values) {
        List column = new ArrayList();
        column.add(columnName);
        column.addAll(Arrays.asList(values));
        
        List columns = Collections.singletonList(column);
        return createResultSet(columns);
    }
    
    public static ResultSet catalogsResultSet(String[] catalogNames) {
        return singleColumnResultSet("TABLE_CAT", catalogNames);
    }
    
    public static ResultSet schemasResultSet(String[] schemaNames) {
        return singleColumnResultSet("TABLE_SCHEM", schemaNames);
    }
    
    /**
     * @param tablesBySchema something like { { "schema1", "table1", "table2" }, { "schema2", "table1" }, ... }
     */
    public static ResultSet tablesResultSet(String[][] tableNamesBySchema) {
        List tableSchemCol = new ArrayList();
        tableSchemCol.add("TABLE_SCHEM");
        List tableNameCol = new ArrayList();
        tableNameCol.add("TABLE_NAME");
        
        for (int i = 0; i < tableNamesBySchema.length; i++) {
            String[] tables = tableNamesBySchema[i];
            assert tables.length > 1;
            
            for (int j = 1; j < tables.length; j++) {
                tableSchemCol.add(tables[0]);
                tableNameCol.add(tables[j]);
            }
        }
        
        List columns = new ArrayList();
        columns.add(tableSchemCol);
        columns.add(tableNameCol);
        return createResultSet(columns);
    }
    
    public static ResultSet columnsResultSet(String[] columnNames, String[] typeNames, int[] sqlTypes, int[] columnSizes, int[] decimalDigits, int[] nullables) {
        List columnNameColumn = writeableSingletonList("COLUMN_NAME");
        columnNameColumn.addAll(Arrays.asList(columnNames));
        
        List typeNameColumn = writeableSingletonList("TYPE_NAME");
        typeNameColumn.addAll(Arrays.asList(typeNames));
        
        List sqlTypeColumn = writeableSingletonList("DATA_TYPE");
        addAllAsReferenceType(sqlTypeColumn, sqlTypes);
        
        List columnSizeColumn = writeableSingletonList("COLUMN_SIZE");
        addAllAsReferenceType(columnSizeColumn, columnSizes);
        
        List decimalDigitsColumn = writeableSingletonList("DECIMAL_DIGITS");
        addAllAsReferenceType(decimalDigitsColumn, decimalDigits);
        
        List nullablesColumn = writeableSingletonList("NULLABLE");
        addAllAsReferenceType(nullablesColumn, nullables);
        
        List columns = new ArrayList();
        columns.add(columnNameColumn);
        columns.add(typeNameColumn);
        columns.add(sqlTypeColumn);
        columns.add(columnSizeColumn);
        columns.add(decimalDigitsColumn);
        columns.add(nullablesColumn);
        return createResultSet(columns);
    }
    
    public static ResultSet indexesResultSet(String[] indexNames, String[] columnNames, boolean[] nonUniques) {
        List indexNameColumn = writeableSingletonList("INDEX_NAME");
        indexNameColumn.addAll(Arrays.asList(indexNames));
        
        List columnNameColumn = writeableSingletonList("COLUMN_NAME");
        columnNameColumn.addAll(Arrays.asList(columnNames));
        
        List nonUniqueColumn = writeableSingletonList("NON_UNIQUE");
        addAllAsReferenceType(nonUniqueColumn, nonUniques);
        
        List columns = new ArrayList();
        columns.add(indexNameColumn);
        columns.add(columnNameColumn);
        columns.add(nonUniqueColumn);
        return createResultSet(columns);
    }
    
    public static ResultSet primaryKeysResultSet(String[] pkNames, String[] columnNames, short[] keySeqs) {
        List pkNameColumn = writeableSingletonList("PK_NAME");
        pkNameColumn.addAll(Arrays.asList(pkNames));
        
        List columnNameColumn = writeableSingletonList("COLUMN_NAME");
        columnNameColumn.addAll(Arrays.asList(columnNames));
        
        List keySeqColumn = writeableSingletonList("KEY_SEQ");
        addAllAsReferenceType(keySeqColumn, keySeqs);
        
        List columns = new ArrayList();
        columns.add(pkNameColumn);
        columns.add(columnNameColumn);
        columns.add(keySeqColumn);
        return createResultSet(columns);
    }
    
    public static ResultSet importedKeysResultSet(
            String[] fkNames,
            String[] pkTableCats, String[] pkTableSchemas, String[] pkTableNames, String[] pkColumnNames,
            String[] fkTableCats, String[] fkTableSchemas, String[] fkTableNames, String[] fkColumnNames) {
        
        List pkTableCatColumn = writeableSingletonList("PKTABLE_CAT");
        pkTableCatColumn.addAll(Arrays.asList(pkTableCats));
        
        List pkTableSchemaColumn = writeableSingletonList("PKTABLE_SCHEM");
        pkTableSchemaColumn.addAll(Arrays.asList(pkTableSchemas));
        
        List pkTableNameColumn = writeableSingletonList("PKTABLE_NAME");
        pkTableNameColumn.addAll(Arrays.asList(pkTableNames));
        
        List pkColumnNameColumn = writeableSingletonList("PKCOLUMN_NAME");
        pkColumnNameColumn.addAll(Arrays.asList(pkColumnNames));
        
        List fkTableCatColumn = writeableSingletonList("FKTABLE_CAT");
        fkTableCatColumn.addAll(Arrays.asList(fkTableCats));
        
        List fkTableSchemaColumn = writeableSingletonList("FKTABLE_SCHEM");
        fkTableSchemaColumn.addAll(Arrays.asList(fkTableSchemas));
        
        List fkTableNameColumn = writeableSingletonList("FKTABLE_NAME");
        fkTableNameColumn.addAll(Arrays.asList(fkTableNames));
        
        List fkColumnNameColumn = writeableSingletonList("FKCOLUMN_NAME");
        fkColumnNameColumn.addAll(Arrays.asList(fkColumnNames));
        
        List fkNameColumn = writeableSingletonList("FK_NAME");
        fkNameColumn.addAll(Arrays.asList(fkNames));
        
        List columns = new ArrayList();
        columns.add(pkTableCatColumn);
        columns.add(pkTableSchemaColumn);
        columns.add(pkTableNameColumn);
        columns.add(pkColumnNameColumn);
        columns.add(fkTableCatColumn);
        columns.add(fkTableSchemaColumn);
        columns.add(fkTableNameColumn);
        columns.add(fkColumnNameColumn);
        columns.add(fkNameColumn);
        return createResultSet(columns);
    }
    
    public static ResultSet emptyResultSet() {
        return createResultSet(Collections.EMPTY_LIST);
    }
    
    private static List writeableSingletonList(Object value) {
        List result = new ArrayList();
        result.add(value);
        return result;
    }
    
    private static void addAllAsReferenceType(List list, int[] values) {
        for (int i = 0; i < values.length; i++) {
            list.add(new Integer(values[i]));
        }
    }
    
    private static void addAllAsReferenceType(List list, short[] values) {
        for (int i = 0; i < values.length; i++) {
            list.add(new Short(values[i]));
        }
    }
    
    private static void addAllAsReferenceType(List list, boolean[] values) {
        for (int i = 0; i < values.length; i++) {
            list.add(Boolean.valueOf(values[i]));
        }
    }
}
