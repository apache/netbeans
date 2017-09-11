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

package org.netbeans.modules.db.mysql.test;

import java.io.File;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.api.sql.execute.SQLExecutionInfo;
import org.netbeans.modules.db.api.sql.execute.StatementExecutionInfo;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author David
 */
public class TestBase extends NbTestCase  {
    private String host;
    private String port;
    private String user;
    private String password;
    private String url;
    private String schema;
    private String dbname;

    private static String DRIVER_CLASSNAME = "com.mysql.jdbc.Driver";

    private JDBCDriver jdbcDriver;
    private static DatabaseConnection dbconn;

    public TestBase(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);

        // We need to set up netbeans.dirs so that the NBInst URLMapper correctly
        // finds the driver jar file if the user is using the nbinst protocol
        File jarFile = new File(JDBCDriverManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File clusterDir = jarFile.getParentFile().getParentFile();
        System.setProperty("netbeans.dirs", clusterDir.getAbsolutePath());
        
        getProperties();
        setUrl();
    }
        
    private void setUrl() {
        url = "jdbc:mysql://" + host + ":" + port + "/" + dbname;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public String getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    protected DatabaseConnection getDatabaseConnection(boolean connect) throws Exception {
        JDBCDriver driver = getJDBCDriver();
        assertNotNull(driver);

        if (dbconn == null) {
            dbconn = DatabaseConnection.create(driver, url, user, schema, password, true);
            ConnectionManager.getDefault().addConnection(dbconn);
        }


        if (connect) {
            connect(dbconn);
        }

        return dbconn;
    }
    
    /**
     * Connect a database connection
     * 
     * @param dbconn
     * @throws java.lang.Exception
     */
    protected void connect(DatabaseConnection dbconn) throws Exception {
        if (dbconn.getJDBCConnection() != null && !dbconn.getJDBCConnection().isClosed()) {
            return;
        }

        assertTrue(ConnectionManager.getDefault().connect(dbconn));

        assertNotNull(dbconn.getJDBCConnection());
        assertFalse(dbconn.getJDBCConnection().isClosed());
    }

    protected JDBCDriver getJDBCDriver() throws Exception {
        if (jdbcDriver == null) {
            jdbcDriver = JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASSNAME)[0];
            assertNotNull(jdbcDriver);
        }

        return jdbcDriver;
    }

    protected void getProperties() throws Exception {
        host = System.getProperty("mysql.host", "localhost");
        port = System.getProperty("mysql.port", "3306");
        user = System.getProperty("mysql.user", "root");
        password = System.getProperty("mysql.password", null);
        dbname = System.getProperty("mysql.dbname", "mysql");

        String message = "\nPlease set the following in nbproject/private/private.properties:\n" +
                "test-unit-sys-prop.mysql.host=<mysql-hostname> [optional, default=localhost]\n" +
                "test-unit-sys-prop.mysql.port=<mysql-port-number> [optional, default=3306]\n" +
                "test-unit-sys-prop.mysql.user=<database-user> [optional, default=root]\n" +
                "test-unit-sys-prop.mysql.password=<database-password> [optional, default=empty]\n" +
                "test-unit-sys-prop.mysql.dbname=<database-name>\n" +
                "Here is an example:\n" +
                "test-unit-sys-prop.mysql.dbname=test\n" +
                "test-unit-sys-prop.mysql.password=root\n" +
                "test-unit-sys-prop.mysql.port=8889";


        if (dbname == null) {
            fail("mysql.dbname was not set. " + message);
        }
    }
    
    public void checkExecution(SQLExecutionInfo info) throws Exception {
        assertNotNull(info);

        if (info.hasExceptions()) {
            for (StatementExecutionInfo stmtinfo : info.getStatementInfos()) {
                if (stmtinfo.hasExceptions()) {
                    System.err.println("The following SQL had exceptions:");
                } else {
                    System.err.println("The following SQL executed cleanly:");
                }
                System.err.println(stmtinfo.getSQL());

                for (Throwable t : stmtinfo.getExceptions()) {
                    t.printStackTrace();
                }
            }

            throw new Exception("Executing SQL generated exceptions - see output for details");
        }        
    }


}
