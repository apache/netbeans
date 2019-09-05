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

package org.netbeans.modules.db.mysql.test;

import java.io.File;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.api.sql.execute.SQLExecutionInfo;
import org.netbeans.modules.db.api.sql.execute.StatementExecutionInfo;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author David
 */
public class TestBase extends NbTestCase  {
    
    private static final Logger LOG = Logger.getLogger(TestBase.class.getName());
    
    private static final String DRIVER_CLASSNAME = "com.mysql.cj.jdbc.Driver";
    
    private String host;
    private String port;
    private String user;
    private String password;
    private String url;
    private String schema;
    private String dbname;

    private JDBCDriver jdbcDriver;
    private static DatabaseConnection dbconn;

    public TestBase(String testName) {
        super(testName);
    }

    @Override
    public boolean canRun() {
        try {
            Class.forName(DRIVER_CLASSNAME);
            return super.canRun();
        } catch (ClassNotFoundException e) {
            LOG.warning(String.format("Test %s in %s disabled, %s not available", this.getName(), this.getClass().getName(), e.getMessage()));
            return false;
        }
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);

        // We need to set up netbeans.dirs so that the NBInst URLMapper correctly
        // finds the driver jar file if the user is using the nbinst protocol
        File jarFile = Utilities.toFile(JDBCDriverManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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
