/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        return (Driver)Class.forName(driverClassName, true, driverLoader).newInstance();
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
