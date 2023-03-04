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

package org.netbeans.modules.j2ee.persistence.editor.completion.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import junit.framework.*;
import org.netbeans.modules.db.test.jdbcstub.ConnectionImpl;
import org.netbeans.test.stub.api.Stub;

/**
 *
 * @author Andrei Badea
 */
public class CatalogTest extends TestCase {
    
    private Connection conn;
    private DatabaseMetaData metaData;
    
    public CatalogTest(String testName) {
        super(testName);
    }
    
    private void createConnection(String[] catalogNames, String[] schemaNames, String[][] tableNamesBySchema) {
        metaData = (DatabaseMetaData)Stub.create(DatabaseMetaData.class, new SimpleDatabaseMetaDataImpl(catalogNames, schemaNames, tableNamesBySchema));
        conn = (Connection)Stub.create(Connection.class, new ConnectionImpl(metaData));
    }
    
    public void testGetSchemasWhenDefaultCatalog() throws Exception {
        createConnection(new String[0], new String[] { "schema2", "schema1" }, new String[0][0]);
        
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        Catalog catalog = provider.getCatalog(null);
        
        Schema[] schemas = catalog.getSchemas();
        assertEquals("schema1", schemas[0].getName());
        assertEquals("schema2", schemas[1].getName());
        
        assertSame(schemas[0], catalog.getSchema("schema1"));
        assertSame(schemas[1], catalog.getSchema("schema2"));
    }
    
    public void testGetSchemasWhenMultipleCatalogs() throws Exception {
        createConnection(new String[] { "cat2", "cat1" }, new String[0], new String[][] { new String[] { "schema2", "s2table2", "s2table1" }, new String[] { "schema1", "s1table1" } });
        
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        Catalog catalog = provider.getCatalog("cat1");
        
        Schema[] schemas = catalog.getSchemas();
        assertEquals("schema1", schemas[0].getName());
        assertEquals("schema2", schemas[1].getName());
        
        assertSame(schemas[0], catalog.getSchema("schema1"));
        assertSame(schemas[1], catalog.getSchema("schema2"));
    }
    
    public void testGetSchemasCache() throws Exception {
        createConnection(new String[] { "cat2", "cat1" }, new String[0], new String[][] { new String[] { "schema2", "s2table2", "s2table1" }, new String[] { "schema1", "s1table1" } });
        
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        Catalog catalog = provider.getCatalog("cat1");
        
        Schema[] schemas1 = catalog.getSchemas();
        assertEquals("schema1", schemas1[0].getName());
        assertEquals("schema2", schemas1[1].getName());
        
        ((SimpleDatabaseMetaDataImpl)Stub.getDelegate(metaData)).setTables(new String[][] { new String[] { "newschema2", "s2table2", "s2table1" }, new String[] { "newschema1", "s1table1" } });

        Schema[] schemas2 = catalog.getSchemas();
        assertEquals("schema1", schemas2[0].getName());
        assertEquals("schema2", schemas2[1].getName());
    }
}
