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

package org.netbeans.modules.db.explorer;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.test.TestBase;

/**
 *
 * @author Andrei Badea
 */
public class DbDriverManagerTest extends TestBase {
    
    public DbDriverManagerTest(String testName) {
        super(testName);
    }

    /**
     * Tests that drivers from DriverManager are loaded.
     */
    public void testLoadFromDriverManager() throws Exception {
        final String URL = "jdbc:testLoadFromDriverManager";
        
        Driver d = new DriverImpl(URL);
        DriverManager.registerDriver(d);
        try {
            Driver found = DbDriverManager.getDefault().getDriver(URL, null);
            assertSame(d, found);
            Connection conn = DbDriverManager.getDefault().getConnection(URL, new Properties(), null);
            assertNotNull(conn);
            assertSame(d, ((ConnectionEx)conn).getDriver());
        } finally {
            DriverManager.deregisterDriver(d);
        }
    }
    
    /**
     * Tests that registered drivers are loaded.
     */
    public void testLoadRegisteredDriver() throws Exception {
        final String URL = "jdbc:testLoadRegisteredDriver";
        
        Driver d = new DriverImpl(URL);
        DbDriverManager.getDefault().registerDriver(d);
        try {
            Driver found = DbDriverManager.getDefault().getDriver(URL, null);
            assertSame(d, found);
            Connection conn = DbDriverManager.getDefault().getConnection(URL, new Properties(), null);
            assertNotNull(conn);
            assertSame(d, ((ConnectionEx)conn).getDriver());
        } finally {
            DbDriverManager.getDefault().deregisterDriver(d);
        }
        try {
            DbDriverManager.getDefault().getDriver(URL, null);
            fail("The driver is still registered.");
        } catch (SQLException e) {
            assertEquals("08001", e.getSQLState());
        }
    }
    
    /**
     * Tests that the fallback JDBCDriver instance is used.
     */
    public void testLoadJDBCDriver() throws Exception {
        JDBCDriver drv = createJDBCDriver();
        Connection conn = DbDriverManager.getDefault().getConnection(DriverImpl.DEFAULT_URL, new Properties(), drv);
        Driver d = ((ConnectionEx)conn).getDriver();
        assertSame(DriverImpl.class, d.getClass());
        assertTrue(d.acceptsURL(DriverImpl.DEFAULT_URL));
    }

    /**
     * Tests the driver priority if there are multiple drivers which accept the same URL.
     * Also tests the getSameDriverConnection() method.
     */
    public void testGetConnection() throws Exception {
        // register a driver to DriverManager
        Driver ddm = new DriverImpl(DriverImpl.DEFAULT_URL);
        DriverManager.registerDriver(ddm);
        try {
            // register a driver to DbDriverManager
            Driver dreg = new DriverImpl(DriverImpl.DEFAULT_URL);
            DbDriverManager.getDefault().registerDriver(dreg);
            try {
                // create a JDBC driver
                JDBCDriver drv = createJDBCDriver();

                Connection conn, newConn;

                // the drivers registered with DbDriverManager have the greatest priority
                conn = DbDriverManager.getDefault().getConnection(DriverImpl.DEFAULT_URL, new Properties(), drv);
                assertSame(dreg, ((ConnectionEx)conn).getDriver());
                // also test the getSameDriverConnection() method
                newConn = DbDriverManager.getDefault().getSameDriverConnection(conn, DriverImpl.DEFAULT_URL, new Properties());
                assertSame(((ConnectionEx)conn).getDriver(), ((ConnectionEx)newConn).getDriver());

                // if nothing registered, try to load a driver from the JDBCDriver URLs 
                DbDriverManager.getDefault().deregisterDriver(dreg);
                conn = DbDriverManager.getDefault().getConnection(DriverImpl.DEFAULT_URL, new Properties(), drv);
                assertNotSame(dreg, ((ConnectionEx)conn).getDriver());
                assertNotSame(ddm, ((ConnectionEx)conn).getDriver());
                // also test the getSameDriverConnection() method
                newConn = DbDriverManager.getDefault().getSameDriverConnection(conn, DriverImpl.DEFAULT_URL, new Properties());
                assertSame(((ConnectionEx)conn).getDriver(), ((ConnectionEx)newConn).getDriver());

                // if no JDBCDriver, try DriverManager
                conn = DbDriverManager.getDefault().getConnection(DriverImpl.DEFAULT_URL, new Properties(), null);
                assertSame(ddm, ((ConnectionEx)conn).getDriver());
                // also test the getSameDriverConnection() method
                newConn = DbDriverManager.getDefault().getSameDriverConnection(conn, DriverImpl.DEFAULT_URL, new Properties());
                assertSame(((ConnectionEx)conn).getDriver(), ((ConnectionEx)newConn).getDriver());
                
                // test if getSameDriverConnection() throws IAE when passed a conn not obtained from DbDriverManager
                conn = dreg.connect(DriverImpl.DEFAULT_URL, new Properties());
                try {
                    DbDriverManager.getDefault().getSameDriverConnection(conn, DriverImpl.DEFAULT_URL, new Properties());
                    fail();
                } catch (IllegalArgumentException e) {
                    // ok
                }
            } finally {
                DbDriverManager.getDefault().deregisterDriver(dreg);
            }
        } finally {
            DriverManager.deregisterDriver(ddm);
        }
    }
    
    /**
     * Tests the driver priority if there are multiple drivers which accept the same URL.
     */
    public void testGetDriverPriority() throws Exception {
        // register a driver to DriverManager
        Driver ddm = new DriverImpl(DriverImpl.DEFAULT_URL);
        DriverManager.registerDriver(ddm);
        try {
            // register a driver to DbDriverManager
            Driver dreg = new DriverImpl(DriverImpl.DEFAULT_URL);
            DbDriverManager.getDefault().registerDriver(dreg);
            try {
                // create a JDBC driver
                JDBCDriver drv = createJDBCDriver();

                Driver driver;

                // the drivers registered with DbDriverManager have the greatest priority
                driver = DbDriverManager.getDefault().getDriver(DriverImpl.DEFAULT_URL, drv);
                assertSame(dreg, driver);

                // if nothing registered, try to load a driver from the JDBCDriver URLs 
                DbDriverManager.getDefault().deregisterDriver(dreg);
                driver = DbDriverManager.getDefault().getDriver(DriverImpl.DEFAULT_URL, drv);
                assertNotSame(dreg, driver);
                assertNotSame(ddm, driver);

                // if no JDBCDriver, try DriverManager
                driver = DbDriverManager.getDefault().getDriver(DriverImpl.DEFAULT_URL, null);
                assertSame(ddm, driver);
            } finally {
                DbDriverManager.getDefault().deregisterDriver(dreg);
            }
        } finally {
            DriverManager.deregisterDriver(ddm);
        }
    }
    
    public void testJDBCDriverCached() throws Exception {
        JDBCDriver drv = createJDBCDriver();
        Driver driver1 = DbDriverManager.getDefault().getDriver(DriverImpl.DEFAULT_URL, drv);
        Driver driver2 = DbDriverManager.getDefault().getDriver(DriverImpl.DEFAULT_URL, drv);
        assertSame(driver1.getClass(), driver2.getClass());
    }
    
    public void testNoJDBCDriverLeaks() throws Exception {
        JDBCDriver drv = createJDBCDriver();
        Driver driver = DbDriverManager.getDefault().getDriver(DriverImpl.DEFAULT_URL, drv);
        Reference<JDBCDriver> drvRef = new WeakReference<>(drv);
        drv = null;

        assertGC("Should be possible to GC the driver", drvRef);
    }
    
    private static JDBCDriver createJDBCDriver() {
        URL url = DbDriverManagerTest.class.getProtectionDomain().getCodeSource().getLocation();
        return JDBCDriver.create("test_driver", "DbDriverManagerTest Driver", "org.netbeans.modules.db.explorer.DbDriverManagerTest$DriverImpl", new URL[] { url });
    }
    
    public static final class DriverImpl implements Driver {
        
        public static final String DEFAULT_URL = "jdbc:DbDriverManagerTest";
        
        private String url;
        
        public DriverImpl() {
            this(DEFAULT_URL);
        }
        
        public DriverImpl(String url) {
            this.url = url;
        }
        
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return new DriverPropertyInfo[0];
        }

        public Connection connect(String url, Properties info) throws SQLException {
            return (Connection)Proxy.newProxyInstance(DriverImpl.class.getClassLoader(), new Class[] { ConnectionEx.class }, new InvocationHandler() {
                public Object invoke(Object proxy, Method m, Object[] args) {
                    String methodName = m.getName();
                    if (methodName.equals("getDriver")) {
                        return DriverImpl.this;
                    } else if (methodName.equals("hashCode")) {
                        Integer i = new Integer(System.identityHashCode(proxy));
                        return i;
                    } else if (methodName.equals("equals")) {
                        return Boolean.valueOf(proxy == args[0]);
                    }
                    return null;
                }
            });
        }

        public boolean acceptsURL(String url) throws SQLException {
            return (this.url.equals(url));
        }

        public boolean jdbcCompliant() {
            return true;
        }

        public int getMinorVersion() {
            return 0;
        }

        public int getMajorVersion() {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    private static interface ConnectionEx extends Connection {
        
        public Driver getDriver();
    }
}
