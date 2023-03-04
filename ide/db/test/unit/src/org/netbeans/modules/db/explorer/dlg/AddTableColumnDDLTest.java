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

import org.netbeans.modules.db.test.DDLTestBase;

/**
 *
 * @author David
 */
public class AddTableColumnDDLTest extends DDLTestBase {

    public AddTableColumnDDLTest(String name) {
        super(name);
    }

    /**
     * Basic test, nothing fancy
     */
    public void testAddTableColumn() throws Exception {
        String tablename = "testAddColumn";
        String colname = "testColumn";
        String pkeyName = "id";
        
        createBasicTable(tablename, pkeyName);
        
        addColumn(tablename, colname);
        
        // Now verify the column exists
        assertTrue(columnExists(tablename, colname));
    }

    
    private void addColumn(String tablename, String colname) throws Exception {
        AddTableColumnDDL ddl = new AddTableColumnDDL(
                getSpecification(), getSchema(), fixIdentifier(tablename));
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, colname);
        TypeElement type = new TypeElement("java.sql.Types.VARCHAR", "VARCHAR");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.SIZE, "255");
        
        ddl.execute(colname, col);
    }
}
