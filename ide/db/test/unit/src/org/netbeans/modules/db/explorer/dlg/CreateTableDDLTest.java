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
package org.netbeans.modules.db.explorer.dlg;

import java.util.Vector;
import org.netbeans.modules.db.test.DDLTestBase;

public class CreateTableDDLTest extends DDLTestBase {

    public CreateTableDDLTest(String name) {
        super(name);
    }

    public void testCreateBasicTable() throws Exception {
        String tablename = "basicTable";
        Vector cols = new Vector();
        Vector pkcols = new Vector();
        
        dropTable(tablename);
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id");
        TypeElement type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.PRIMARY_KEY, new Boolean(true));
        cols.add(col);

        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "firstname");
        type = new TypeElement("java.sql.Types.VARCHAR", "VARCHAR");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.SIZE, "255");
        cols.add(col);
        
        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "socsec");
        type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        cols.add(col);
        
        CreateTableDDL ddl = new CreateTableDDL(getSpecification(), getSchema(), tablename);
        
        boolean wasException = ddl.execute(cols, pkcols);
        
        assertFalse(wasException);
        
        assertTrue(tableExists(tablename));
        assertTrue(columnInPrimaryKey(tablename, "id"));
    }
    
    public void testCreateTableWithPrimaryKeys() throws Exception {
        String tablename = "basicTable";
        Vector cols = new Vector();
        Vector pkcols = new Vector();
        
        dropTable(tablename);
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id");
        TypeElement type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        cols.add(col);
        pkcols.add(col);

        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id2");
        type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        cols.add(col);
        pkcols.add(col);
        
        CreateTableDDL ddl = new CreateTableDDL(getSpecification(), getSchema(), tablename);
        
        boolean wasException = ddl.execute(cols, pkcols);
        
        assertFalse(wasException);
        
        assertTrue(tableExists(tablename));
        assertTrue(columnInPrimaryKey(tablename, "id"));
        assertTrue(columnInPrimaryKey(tablename, "id2"));
        
    }
    
    public void testCreateTableWithSecondaryIndex() throws Exception {
        String tablename = "basicTable";
        Vector cols = new Vector();
        Vector pkcols = new Vector();
        
        dropTable(tablename);
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id");
        TypeElement type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.PRIMARY_KEY, new Boolean(true));
        cols.add(col);

        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "firstname");
        type = new TypeElement("java.sql.Types.VARCHAR", "VARCHAR");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.SIZE, "255");
        cols.add(col);
        
        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "socsec");
        type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.INDEX, new Boolean(true));
        cols.add(col);
        
        CreateTableDDL ddl = new CreateTableDDL(getSpecification(), getSchema(), tablename);
        
        boolean wasException = ddl.execute(cols, pkcols);
        
        assertFalse(wasException);
        
        assertTrue(tableExists(tablename));
        assertTrue(columnInPrimaryKey(tablename, "id"));
        assertTrue(columnInAnyIndex(tablename, "socsec"));
        
    }
}
