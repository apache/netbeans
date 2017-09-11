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
package org.netbeans.modules.db.dataview.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.dataview.spi.DBConnectionProviderImpl;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author jawed
 */
public class DbUtil {

    public static String URL = "url";
    public static String USER = "user";
    public static String PASSWORD = "password";
    public static final String AXION_DRIVER = "org.axiondb.jdbc.AxionDriver";
    private static List<Connection> localConnectionList = new ArrayList<Connection>();

    public static DatabaseConnection getDBConnection() {
        try {
            TestCaseContext context = getContext();
            Properties prop = context.getProperties();
            File[] jars = context.getJars();
            ArrayList<URL> list = new java.util.ArrayList<URL>();
            for (int i = 0; i < jars.length; i++) {
                list.add(Utilities.toURI(jars[i]).toURL());
            }
            URL[] urls = list.toArray(new URL[0]);
            Class.forName(AXION_DRIVER);
            JDBCDriver driver = JDBCDriver.create(AXION_DRIVER, "MashupDB", AXION_DRIVER, urls);
            DatabaseConnection dbconn = DatabaseConnection.create(driver, prop.getProperty("url"), prop.getProperty("user"),
                    "", prop.getProperty("password"), true);
            return dbconn;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Connection getjdbcConnection() {
        try {
            DBConnectionProviderImpl dbp = new DBConnectionProviderImpl();
            Connection conn = dbp.getConnection(getDBConnection());
            return conn;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    private static JDBCDriver registerDriver(String driverName) throws Exception {
        JDBCDriver drv;

        if (driverName.equals(AXION_DRIVER)) {
            drv = registerAxionDriverInstance();
        } else {
            drv = registerDriverInstance(driverName);
        }

        return drv;
    }

    public static Connection createConnection(Properties connProps) throws Exception {
        String driver = connProps.getProperty("DRIVER");
        String username = connProps.getProperty("user");
        String password = connProps.getProperty("password");
        String url = connProps.getProperty("url");
        return createConnection(driver, url, username, password);
    }
    public static final String AXION_URL_PREFIX = "jdbc:axiondb:";

    public static String[] parseConnUrl(String url) {
        String name, workDir;
        String prefixStripped = url.substring(AXION_URL_PREFIX.length());
        int colon = prefixStripped.indexOf(":");
        if (colon == -1 || (prefixStripped.length() - 1 == colon)) {
            name = prefixStripped;
            workDir = name;
        } else {
            name = prefixStripped.substring(0, colon);
            workDir = unifyPath(prefixStripped.substring(colon + 1));
        }

        String[] connStr = new String[2];
        connStr[0] = name;
        connStr[1] = workDir;
        return connStr;
    }

    public static Connection createConnection(DatabaseConnection dbConn) throws Exception {
        Connection conn = null;
        if (dbConn != null) {
            conn = createConnection(dbConn.getDriverClass(), dbConn.getDatabaseURL(), dbConn.getUser(), dbConn.getPassword());
        }
        return conn;
    }


    public static Connection createConnection(String driverName, String url, String username, String password) throws Exception {
        // Try to get the connection directly. Dont go through DB Explorer.
        // It may pop up a window asking for password.
        Connection conn = null;
        try {
            //url = adjustDatabaseURL(url);
            JDBCDriver drv  = registerDriver(driverName);

            conn = getConnection(drv, driverName, url, username, password);
            if (conn == null) { // get from db explorer

                DatabaseConnection dbConn = createDatabaseConnection(driverName, url, username, password);
                try {
                    if (dbConn != null) {
                        conn = dbConn.getJDBCConnection();
                        if (conn == null) { // make a final try

                            ConnectionManager.getDefault().showConnectionDialog(dbConn);
                            Thread.sleep(5000);
                            conn = dbConn.getJDBCConnection();
                        }
                    }
                } catch (Exception ex) {
                    // ignore
                }

                // If connection is still nul throw exception
                if (conn == null) {
                    throw new Exception("Connection could not be established. Please check the Database Server.");
                }
            } else {
                synchronized (localConnectionList) {
                    localConnectionList.add(conn);
                }
            }
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
            if (ex.getMessage().indexOf("Specified JDBC Driver not found in DB Explorer: ") != -1) {
            }
        }
        return conn;
    }

    public static void closeIfLocalConnection(Connection conn) {
        if (localConnectionList.contains(conn)) {
            try {
                localConnectionList.remove(conn);
                conn.close();
            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static DatabaseConnection createDatabaseConnection(String driverName, String url, String username, String password) throws Exception {
        DatabaseConnection dbconn = null;
        try {
            //url = adjustDatabaseURL(url);
            JDBCDriver drv = registerDriver(driverName);

            // check if connection exists in DB Explorer. Else add the connection to DB Explorer.

            DatabaseConnection[] dbconns = ConnectionManager.getDefault().getConnections();
            for (int i = 0; i < dbconns.length; i++) {
                if (dbconns[i].getDriverClass().equals(driverName) && dbconns[i].getDatabaseURL().equals(url) && dbconns[i].getUser().equals(username)) {
                    dbconn = dbconns[i];
                    break;
                }
            }

            // dont add instance db and monitor db and local dbs connections to db explorer.
            if (dbconn == null) {
                String schema;
                if (url.startsWith(AXION_URL_PREFIX)) {
                    schema = "";
                } else {
                    schema = username.toUpperCase();
                }
                dbconn = DatabaseConnection.create(drv, url, username, schema, password, true);

                 if (url.indexOf("InstanceDB") == -1 && url.indexOf("MonitorDB") == -1 ) {
                    ConnectionManager.getDefault().addConnection(dbconn);
                }
            }

            return dbconn;

        } catch (Exception e) {
            throw new Exception("Connection could not be established.", e);
        }
    }

    /**
     * Registers an instance of Driver associated with the given driver class name. Does
     * nothing if an instance has already been registered with the JDBC DriverManager.
     *
     * @param driverName class name of driver to be created
     * @return Driver instance associated with <code>driverName</code>
     * @throws Exception if error occurs while creating or looking up the desired driver
     *         instance
     */
    public static JDBCDriver registerDriverInstance(final String driverName) throws Exception {
        JDBCDriver driver = null;
        JDBCDriver[] drivers;
        try {
            drivers = JDBCDriverManager.getDefault().getDrivers(driverName);
        } catch (Exception ex) {
            throw new Exception("Invalid driver name specified.");
        }
        if (driverName.equals(AXION_DRIVER)) {
            driver = registerAxionDriverInstance();
        } else {
            if (drivers.length == 0) {
                throw new Exception("Specified JDBC Driver not found in DB Explorer: " + driverName);
            } else {
                driver = drivers[0];
            }
        }
        return driver;
    }

    private static JDBCDriver registerAxionDriverInstance() throws Exception {
        JDBCDriver driver = null;
        String driverName = AXION_DRIVER;
        TestCaseContext cxt = getContext();
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverName);
        if (drivers.length == 0) {
            // if axion db driver not available in db explorer, add it.
            //URL[] url = new URL[1];
            File[] jars = cxt.getJars();
            ArrayList<URL> list = new java.util.ArrayList<URL>();
            for (int i = 0; i < jars.length; i++) {
                list.add(Utilities.toURI(jars[i]).toURL());
            }
            URL[] url = list.toArray(new URL[0]);
            driver = JDBCDriver.create(driverName, "Mashup DB", driverName, url);
            JDBCDriverManager.getDefault().addDriver(driver);
        }
        if (driver == null) {
            for (int i = 0; i < drivers.length; i++) {
                if (drivers[i].getClassName().equals(driverName)) {
                    driver = drivers[i];
                    break;
                }
            }
        }
        return driver;
    }

    /**
     * Manually load the driver class and get the connection for the specified properties.
     *
     * @return connection for the corresponding db url and properties.
     */
    private static Connection getConnection(JDBCDriver drv, String driverName, String url, String username, String password) {
        Connection conn = null;
        Driver newDriverClass = null;
        try {
            // get the driver jar files and load them manually
            URL[] urls = drv.getURLs();
            ClassLoader cl = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
            newDriverClass = (Driver) cl.loadClass(driverName).newInstance();
            Properties prop = new Properties();
            prop.setProperty("user", username);
            prop.setProperty("password", password);
            conn = newDriverClass.connect(url, prop);
        } catch (SQLException ex) {
            try {
                // may be some class forgot to decrypt the password. Check if this one works
                Properties prop = new Properties();
                prop.setProperty("user", username);
                // if its not an encrypted password, decrypt operation will fail. Caught as general Exception
                //password = ScEncrypt.decrypt(username, password);
                prop.setProperty("password", password);
                conn = newDriverClass.connect(url, prop);
            } catch (SQLException e) {
            } catch (Exception numex) {
            }
        } catch (Exception ex) {
        }
        return conn;
    }

    public static String unifyPath(String workDir) {
        //return workDir.replace('/', '\\');
        return workDir;
    }
    
        public static TestCaseContext getContext() {
        try {
            TestCaseDataFactory tfactory = TestCaseDataFactory.getTestCaseFactory();
            TestCaseContext context = (TestCaseContext) tfactory.getTestCaseContext()[0];
            return context;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public static void createTable(){
        try {
            TestCaseContext cxt = getContext();
            Connection con = getjdbcConnection();
            Statement stmt = con.createStatement();
            stmt.execute(cxt.getSqlCreate());
            stmt.execute(cxt.getSqlInsert());
            con.close();
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static void dropTable() {
        try {
            TestCaseContext cxt = getContext();
            Connection con = getjdbcConnection();
            con.createStatement().execute(cxt.getSqlDel());
            con.close();
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}

