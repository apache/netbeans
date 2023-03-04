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
package org.netbeans.modules.db.explorer.node;

import java.sql.Types;
import org.netbeans.modules.db.test.DDLTestBase;

/**
 * @author <href="mailto:david@vancouvering.com">David Van Couvering</href>
 */
public class DDLHelperTest extends DDLTestBase {
    public DDLHelperTest(String name) {
        super(name);
    }

    public void testDeleteIndex() throws Exception {
        String tablename = "testIndexDelete";
        String colname = "indexcol";
        String indexname = "indexcol_idx";
        
        createBasicTable(tablename, "id");
        addBasicColumn(tablename, colname, Types.INTEGER, 0);
        
        // Create an index
        createSimpleIndex(tablename, indexname, colname);
        
        DDLHelper.deleteIndex(getSpecification(), getSchema(),
                fixIdentifier(tablename), 
                fixIdentifier(indexname));
        
        assertFalse(indexExists(tablename, indexname));
    }
    
    public void testDeleteTable() throws Exception {
        String tablename = "testDeleteTable";
        
        createBasicTable(tablename, "id");
        assertTrue(tableExists(tablename));
        
        DDLHelper.deleteTable(getSpecification(), getSchema(), fixIdentifier(tablename));
        
        assertFalse(tableExists(tablename));
    }
    
    public void testDeleteView() throws Exception {
        String tablename = "testDeleteViewTable";
        String viewname = "testDeleteView";
        
        createBasicTable(tablename, "id");
        
        createView(viewname, "SELECT * FROM " + tablename);
        assertTrue(viewExists(viewname));
        
        DDLHelper.deleteView(getSpecification(), getSchema(), fixIdentifier(viewname));
        
        assertFalse(viewExists(viewname));
    }
    

}
