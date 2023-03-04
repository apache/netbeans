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
public class DBModelTest extends NbTestCase {
    
    DBTable tbl = null;
    public DBModelTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBModelTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tbl = createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public DBTable createTable() {
        DBTable table = null;
        String aName = "aName";
        String aSchema = "aSchema";
        String aCatalog = "aCatalog";
        table = new DBTable(aName, aSchema, aCatalog);
        return table;
    }
    //----------- Test Cases --------------
    /**
     * Test of addTable method, of class DBModel.
     */
    public void testAddTable() {
        DBModel instance = new DBModel();
        instance.addTable(tbl);
        String fqName = instance.getFullyQualifiedTableName(tbl);
        assertNotNull(tbl);
        assertEquals(tbl, instance.getTable(fqName));
    }
    
    /**
     * Test of getFullyQualifiedTableName method, of class DBModel.
     */
    public void testGetFullyQualifiedTableName() {
        DBModel instance = new DBModel();
        String expResult = "aCatalog.aSchema.aName";
        String result = instance.getFullyQualifiedTableName(tbl);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class DBModel.
     */
    public void testHashCode() {
        DBModel instance = new DBModel();
        int result = instance.hashCode();
        assertNotNull(result);
    }
}
