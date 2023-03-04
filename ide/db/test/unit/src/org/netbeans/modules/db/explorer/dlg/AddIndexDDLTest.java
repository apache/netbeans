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

import java.sql.Types;
import java.util.HashSet;
import org.netbeans.modules.db.test.DDLTestBase;

/**
 *
 * @author David
 */
public class AddIndexDDLTest extends DDLTestBase {

    public AddIndexDDLTest(String name) {
        super(name);
    }
    
    public void testAddIndex() throws Exception {
        String tablename = "mytable";
        String pkname = "id";
        String colname = "col";
        String ixname = "col_ix";
        
        createBasicTable(tablename, pkname);
        addBasicColumn(tablename, colname, Types.VARCHAR, 255);
        
        AddIndexDDL ddl = new AddIndexDDL(getSpecification(), getSchema(),
                fixIdentifier(tablename));
        
        HashSet cols = new HashSet();
        cols.add(fixIdentifier(colname));
        
        boolean wasException = ddl.execute(ixname, false, cols);
        
        assertFalse(wasException);
        assertTrue(columnInIndex(tablename, colname, ixname));
        
        colname = "col2";
        ixname = "col2_ix";
        addBasicColumn(tablename, colname, Types.VARCHAR, 255);
        
        cols.clear();
        cols.add(fixIdentifier(colname));
        wasException = ddl.execute(ixname, true, cols);
        assertFalse(wasException);
        assertTrue(columnInIndex(tablename, colname, ixname));
        assertTrue(indexIsUnique(tablename, ixname));
    }

}
