/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.dbschema;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider;
import org.netbeans.modules.dbschema.jdbcimpl.SchemaElementImpl;
import org.netbeans.modules.dbschema.test.dbsupport.DbSupport;
import org.netbeans.modules.dbschema.test.dbsupport.DbSupport.FEATURE;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author David
 */
public class ColumnElementTest extends NbTestCase {
    private String url;
    private String user;
    private String password;
    private String jarpath;
    private Connection conn;
    private String driverClassName;
    private String schema;
    private DbSupport dbsupport;


    public ColumnElementTest(String testName) {
        super(testName); 
    }     
    
    @Override
    protected void setUp() throws Exception {
        try {
            super.setUp();

            Lookup.getDefault().lookup(ModuleInfo.class);

            // We need to set up netbeans.dirs so that the NBInst URLMapper correctly
            // finds our driver jar file
            File jarFile = new File(JDBCDriverManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File clusterDir = jarFile.getParentFile().getParentFile();
            System.setProperty("netbeans.dirs", clusterDir.getAbsolutePath());

            getProperties();

            dbsupport = DbSupport.getInstance(driverClassName);

            conn = getConnection();
        } catch (SQLException sqle) {
            reportSQLException(sqle);
            throw sqle;
        }
    }

    private void reportSQLException(SQLException sqle) {
        while (sqle != null) {
            sqle.printStackTrace();
            sqle = sqle.getNextException();
        }
    }
        
    private void getProperties() {
        url = System.getProperty("db.url", null);
        user = System.getProperty("db.user", null);
        password = System.getProperty("db.password", null);
        driverClassName = System.getProperty("db.driver.classname");
        schema = System.getProperty("db.schema");

        jarpath = System.getProperty("db.driver.jarpath", null);
        
        String message = "\nPlease set the following in nbproject/private/private.properties:\n" +
                "test-unit-sys-prop.db.url=<database-url>\n" +
                "test-unit-sys-prop.db.user=<database-user>\n" +
                "test-unit-sys-prop.db.password=<database-password> (optional)\n" +
                "test-unit-sys-prop.db.schema=<database-schema>\n" +
                "test-unit-sys-prop.db.driver.jarpath=<path-to-driver-jar-file> (can use 'nbinst:///' protocol',\n" +
                "test-unit-sys-prop.db.driver.classname=<name-of-jdbc-driver-class>\n\n" +
                "A template is available in test/private.properties.template";

        
        if (url == null) {
            fail("db.url was not set. " + message);
        }
        if (user == null) {
            fail("db.user was not set.  " + message);
        }
        if (schema == null) {
            fail("db.schema was not set.  " + message);
        }
        if (jarpath == null) {
            fail("db.driver.jarpath was not set. " + message);
        }
        if (driverClassName == null) {
            fail("db.driver.classname was not set. " + message);
        }
    }

    private Connection getConnection() throws Exception {
        Driver driver = getDriver();
        Properties props = new Properties();
        props.put("user", user == null ? "" : user);
        if (password != null) {
            props.put("password", password);
        }

        Connection connection = driver.connect(url, props);
        assertNotNull(connection);

        return connection;
    }

    private Driver getDriver() throws Exception {
        URLClassLoader driverLoader = new URLClassLoader(new URL[] {new URL(jarpath)});
        return (Driver)Class.forName(driverClassName, true, driverLoader).getDeclaredConstructor().newInstance();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAutoIncrementColumn() throws Exception {
        try {
            String tableName = "aitable";
            String columnName = "aicolumn";

            if (! dbsupport.supportsFeature(FEATURE.AUTOINCREMENT)) {
                return;
            } 

            SchemaElement schemaElement = getSchemaElement();
            TableElement[] tables = schemaElement.getTables();
            TableElement table = null;
            for ( TableElement tab : tables) {
                System.out.println(tab.getName().getName());
                if(tab.getName().getName().toLowerCase().contains(tableName)) {
                    table = tab;
                    break;
                }
            }
            assertNotNull(table);

            ColumnElement[] columns = table.getColumns();
            ColumnElement col = null;
            ColumnElement othercol = null;

            for (ColumnElement column : columns) {
                if (column.getName().getName().toLowerCase().contains(columnName)) {
                    col = column;
                } else {
                    othercol = column;
                }
            }
            assertNotNull(col);
            assertNotNull(othercol);

            assertTrue(col.isAutoIncrement());
            assertFalse(othercol.isAutoIncrement());
        } catch (SQLException sqle) {
            reportSQLException(sqle);
            throw sqle;
        }
    }

    /**
     * Get the schema element representing the selected tables.
     */
    private SchemaElement getSchemaElement() throws SQLException, DBException {

        ConnectionProvider connectionProvider = new ConnectionProvider(conn, driverClassName);
        connectionProvider.setSchema(schema);
        SchemaElementImpl impl = new SchemaElementImpl(connectionProvider);
        SchemaElement schemaElement = new SchemaElement(impl);
        schemaElement.setName(DBIdentifier.create("test-schema")); // NOI18N
        impl.initTables(connectionProvider);

        return schemaElement;
    }

}
