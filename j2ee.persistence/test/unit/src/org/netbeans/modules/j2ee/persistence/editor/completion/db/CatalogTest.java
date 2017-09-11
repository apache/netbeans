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
