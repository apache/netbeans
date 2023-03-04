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

import org.netbeans.junit.NbTestCase;
/**
 *
 * @author jawed
 */
public class DBColumnTest extends NbTestCase {
    
    private DBTable table;
    
    public DBColumnTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBColumnTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void createTable(){
        String aName = "aName";
        String aSchema = "aSchema";
        String aCatalog = "aCatalog";
        table = new DBTable(aName, aSchema, aCatalog);
    }
//---------- Test Cases ---------------
    
    public void testConstructor() {
        DBColumn col = new DBColumn(table, "colName", 12, "varchar", 10, 5, true, false);
        assertEquals("colName", col.getName());
        assertEquals(12, col.getJdbcType());
        assertEquals(10, col.getScale());
        assertEquals(5, col.getPrecision());
    }
    
    public void testEqualsAndHashCode() {
        DBColumn col1 = new DBColumn(table, "colName", 12,"varchar", 10, 5, true, false);
        DBColumn col2 = new DBColumn(table, "colName", 12,"varchar", 10, 5, true, false);
        DBColumn col3 = new DBColumn(table, "colNameDiff", 12,"varchar", 10, 5, true, false);
        assertEquals(col1,col1);
        assertEquals(col1.getDisplayName(),col2.getDisplayName());
        assertEquals(col2.getName(),col1.getName());
        
        assertTrue(! col1.equals(col3) );
        assertTrue(col1.hashCode() != col3.hashCode() );
        assertTrue(! col1.equals(null) );
        assertTrue(! col1.equals(new Object()) );
    }
}
