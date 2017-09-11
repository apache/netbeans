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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.explorer.MetaDataListener;

/**
 * Provides metadata information for a database connection.
 *
 * <p>This class is not thread-safe, its methods should be called from the same
 * thread.</p>
 *
 * @author Andrei Badea
 */
public class DBMetaDataProvider {
    
    // The return types of the methods returning a series of objects are array
    // instead of List in order to be consistent with dbschema.
    
    // Maps java.sql.Connection-s to DB metadata providers.
    private static final Map/*<Connection, DBMetadataProvider>*/ CONN_TO_PROVIDER = new WeakHashMap();
    
    // must be a weak reference -- we don't want to prevent the connection from being GCd
    // it is OK to be a weak reference -- the connection is hold strongly by the DB Explorer
    // while connected
    private final Reference/*<Connection>*/ conn;
    private final String driverClass;
    
    private Map/*<String, Catalog>*/ catalogs;
    
    /**
     * Returns a DB metadata provider for the given connection.
     */
    public static synchronized DBMetaDataProvider get(Connection conn, String driverClass) {
        assert conn != null;
        DBMetaDataProvider provider = (DBMetaDataProvider)CONN_TO_PROVIDER.get(conn);
        if (provider == null) {
            provider = new DBMetaDataProvider(conn, driverClass);
            CONN_TO_PROVIDER.put(conn, provider);
        }
        return provider;
    }
    
    public static MetaDataListener createMetaDataListener() {
        return new MetaDataListenerImpl();
    }
    
    public DBMetaDataProvider(Connection conn, String driverClass) {
        this.conn = new WeakReference(conn);
        this.driverClass = driverClass;
    }
    
    public String getDefaultCatalog() throws SQLException {
        return getConnection().getCatalog();
    }
    
    /**
     * Returns the catalogs. Note that for some databases (e.g. Derby) only a
     * Catalog instance with a null name will be returned.
     */
    public synchronized Catalog[] getCatalogs() throws SQLException {
        if (catalogs == null) {
            catalogs = new TreeMap(new CatalogComparator());
            
            ResultSet rs = getMetaData().getCatalogs();
            
            try {
                while (rs.next()) {
                    String catalogName = rs.getString("TABLE_CAT"); // NOI18N
                    Catalog catalog = new Catalog(this, catalogName);
                    catalogs.put(catalogName, catalog);
                    //if (catalogName != null) {
                    //    // testing for null since Derby returns only a null catalog here
                    //    result.add(catalogName);
                    //}
                }
            } finally {
                rs.close();
            }
            
            if (catalogs.size() <= 0) {
                Catalog defaultCatalog = new Catalog(this, null);
                catalogs.put(null, defaultCatalog);
            }
        }
        
        return (Catalog[])catalogs.values().toArray(new Catalog[catalogs.size()]);
    }
    
    public synchronized Catalog getCatalog(String name) throws SQLException {
        if (catalogs == null) {
            getCatalogs();
        }
        Catalog ret = (Catalog)catalogs.get(name);
        if(ret == null && "".equals(name))ret = (Catalog)catalogs.get(null);
        return ret;
    }
    
    Connection getConnection() {
        return (Connection)conn.get();
    }
    
    String getDriverClass() {
        return driverClass;
    }
    
    DatabaseMetaData getMetaData() throws SQLException {
        return getConnection().getMetaData();
    }
    
    public String toString() {
        return "DBMetadataProvider[conn=" + getConnection() + "]"; // NOI18N
    }
    
    private static final class CatalogComparator implements Comparator {
        
        public boolean equals(Object that) {
            return that instanceof CatalogComparator;
        }
        
        public int compare(Object o1, Object o2) {
            String name1 = (String)o1;
            String name2 = (String)o2;
            
            if (name1 == null) {
                return (name2 == null) ? 0 : -1;
            } else {
                return (name2 == null) ? 1 : name1.compareTo(name2);
            }
        }
    }

    private static final class MetaDataListenerImpl implements MetaDataListener {
        
        public MetaDataListenerImpl() {
        }

        public void tablesChanged(final DatabaseConnection dbconn) {
            try {
                Schema schema = getSchema(dbconn);
                if (schema != null) {
                    schema.refresh();
                }
            } catch (SQLException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }

        public void tableChanged(DatabaseConnection dbconn, String tableName) {
            tablesChanged(dbconn);
        }

        private Schema getSchema(DatabaseConnection dbconn) throws SQLException {
            Connection conn = dbconn.getJDBCConnection();
            if (conn == null) {
                return null;
            }
            DBMetaDataProvider provider;
            synchronized (this) {
                provider = (DBMetaDataProvider)CONN_TO_PROVIDER.get(conn);
            }
            if (provider == null) {
                return null;
            }
            Catalog catalog = provider.getCatalog(conn.getCatalog());
            if (catalog == null) {
                return null;
            }
            return catalog.getSchema(dbconn.getSchema());
        }
    }
}
