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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.test.jdbcstub.ConnectionImpl;
import org.netbeans.test.stub.api.Stub;

/**
 *
 * @author Andrei Badea
 */
public class DBMetaDataProviderTest extends NbTestCase {
    
    private Connection conn;
    private DatabaseMetaData metaData;
    
    public DBMetaDataProviderTest(String testName) {
        super(testName);
    }
    
    private void createConnection(String[] catalogNames) {
        metaData = (DatabaseMetaData)Stub.create(DatabaseMetaData.class, new SimpleDatabaseMetaDataImpl(catalogNames));
        conn = (Connection)Stub.create(Connection.class, new ConnectionImpl(metaData));
    }
    
    public void testSameProviderForSameConnection() {
        createConnection(new String[0]);
        
        DBMetaDataProvider provider1 = DBMetaDataProvider.get(conn, "");
        DBMetaDataProvider provider2 = DBMetaDataProvider.get(conn, "");
        assertSame("Same provider for the same connection", provider1, provider2);
    }
    
    public void testConnectionAndProviderLeak() {
        createConnection(new String[0]);
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        
        Reference ref = new WeakReference(conn);
        conn = null;
        assertGC("The connection can be GCd", ref);
        
        // causes the stale entries (those, whose keys have been GCd) to be removed from the weak map
        DBMetaDataProvider.get((Connection)Stub.create(Connection.class), "");
        
        ref = new WeakReference(provider);
        provider = null;
        assertGC("The provider can be GCd", ref);
    }
    
    public void testGetCatalogs() throws Exception {
        createConnection(new String[] { "cat2", "cat1" });
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        
        Catalog[] catalogs = provider.getCatalogs();
        assertEquals("cat1", catalogs[0].getName());
        assertEquals("cat2", catalogs[1].getName());
        
        assertSame(catalogs[0], provider.getCatalog("cat1"));
        assertSame(catalogs[1], provider.getCatalog("cat2"));
    }
    
    public void testGetCatalogsCache() throws Exception {
        createConnection(new String[] { "cat1", "cat2"  });
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        
        Catalog[] catalogs1 = provider.getCatalogs();
        assertEquals("cat1", catalogs1[0].getName());
        assertEquals("cat2", catalogs1[1].getName());
        
        ((SimpleDatabaseMetaDataImpl)Stub.getDelegate(metaData)).setCatalogs(new String[] { "newcat1", "newcat2" });
        
        Catalog[] catalogs2 = provider.getCatalogs();
        assertEquals("cat1", catalogs2[0].getName());
        assertEquals("cat2", catalogs2[1].getName());
    }
    
    public void testGetCatalogsWhenNoCatalogs() throws Exception {
        createConnection(new String[0]);
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        
        Catalog[] catalogs = provider.getCatalogs();
        assertNull(catalogs[0].getName());
        assertSame(catalogs[0], provider.getCatalog(null));
    }
}
