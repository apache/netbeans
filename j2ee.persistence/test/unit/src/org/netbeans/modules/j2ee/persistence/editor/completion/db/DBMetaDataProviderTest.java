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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.test.jdbcstub.ConnectionImpl;
import org.netbeans.test.stub.api.Stub;
import org.netbeans.test.stub.api.StubDelegate;

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
