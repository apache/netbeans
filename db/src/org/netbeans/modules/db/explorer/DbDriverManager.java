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

package org.netbeans.modules.db.explorer;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.JDBCDriver;

/**
 * Class to load drivers and create connections. It can find drivers and connections from
 * several sources: previously registered drivers, URLs from a JDBCDriver instance or
 * drivers registered to java.sql.DriverManager, exactly this order. DriverManager
 * has the lowest priority since we should always try to use the drivers defined by the
 * user in the DB Explorer, even if the same driver class is on the IDE's classpath.
 * (since the driver on the IDE's classpath could be a wrong/old version).
 *
 * <p>The advantage of this class over DriverManager is that it can work in a multi-class-loader
 * environment. That is, registered drivers can retrieved regardless of the class loader of the
 * caller of the getDriver() method.</p>
 *
 * <p>This class also caches and reuses the class loaders used to load the drivers' JAR files.
 * It is not perfect, since when the JDBC driver properties are changed in the UI a new
 * JDBCDriver instance is created, thus a new class loader is created for it. This
 * has multiple implications, see issue 63957 and issue 76922.</p>
 *
 * @author Andrei Badea
 */
public class DbDriverManager {
    
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.db.explorer.DbDriverManager"); // NOI18N
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    private static final DbDriverManager DEFAULT = new DbDriverManager();
    
    private Set registeredDrivers;
    
    /**
     * Maps each connection to the driver used to create that connection.
     */
    private Map/*<Connection, Driver>*/ conn2Driver = new WeakHashMap();
    
    /**
     * Maps each driver to the class loader for that driver.
     */
    private Map/*<JDBCDriver, ClassLoader>*/ driver2Loader = new WeakHashMap();
    
    private DbDriverManager() {
    }
    
    /**
     * Returns the singleton instance.
     */
    public static DbDriverManager getDefault() {
        return DEFAULT;
    }
    
    /**
     * Gets a connection to databaseURL using jdbcDriver as a fallback.
     *
     * @param databaseURL
     * @param props
     * @param jdbcDriver the fallback JDBCDriver; can be null
     */
    public Connection getConnection(String databaseURL, Properties props, JDBCDriver jdbcDriver) throws SQLException {
        if (LOG) {
            LOGGER.log(Level.FINE, "Attempting to connect to \'" + databaseURL + "\'"); // NOI18N
        }
        
        // try to find a registered driver or use the supplied jdbcDriver
        // we'll look ourselves in DriverManager, don't look there
        Driver driver = getDriverInternal(databaseURL, jdbcDriver, false);
        if (driver != null) {
            // Issue XXXX - If this is MySQL, set up the connection to be
            // a Unicode/utf8 connection
            if ( driver.getClass().getName().equals("com.mysql.jdbc.Driver") ) { // NOI18N
                props.put("useUnicode", "true");
                props.put("characterEncoding", "utf8");
            }
            Connection conn = driver.connect(databaseURL, props);
            if (conn == null) {
                if (LOG) {
                    LOGGER.log(Level.FINE, driver.getClass().getName() + ".connect() returned null"); // NOI18N
                }
                throw createDriverNotFoundException();
            }
            synchronized (conn2Driver) {
                conn2Driver.put(conn, driver);
            }
            return conn;
        }
        
        // try to find a connection using DriverManager 
        try {
            Connection conn = DriverManager.getConnection(databaseURL, props);
            synchronized (conn2Driver) {
                conn2Driver.put(conn, null);
            }
            return conn;
        } catch (SQLException e) {
            // ignore it, we throw our own exceptions
        }
        
        throw createDriverNotFoundException();
    }
    
    /**
     * Returns a connection coming from the same driver as the conn parameter.
     */
    public Connection getSameDriverConnection(Connection existingConn, String databaseURL, Properties props) throws SQLException {
        if (existingConn == null) {
            throw new NullPointerException();
        }
        Driver driver = null;
        synchronized (conn2Driver) {
            if (!conn2Driver.containsKey(existingConn)) {
                throw new IllegalArgumentException("A connection not obtained through DbDriverManager was passed."); // NOI18N
            }
            driver = (Driver)conn2Driver.get(existingConn);
        }
        if (driver != null) {
            Connection newConn = driver.connect(databaseURL, props);
            if (newConn == null) {
                throw new SQLException("Unable to connect using existingConn's original driver", "08001"); // NOI18N
            }
            synchronized (conn2Driver) {
                conn2Driver.put(newConn, driver);
            }
            return newConn;
        } else {
            return DriverManager.getConnection(databaseURL, props);
        }
    }
    
    /**
     * Register a new driver.
     */
    public synchronized void registerDriver(Driver driver) {
        if (registeredDrivers == null) {
            registeredDrivers = new HashSet();
        }
        registeredDrivers.add(driver);
    }
    
    /**
     * Deregister a previously registered driver.
     */
    public synchronized void deregisterDriver(Driver driver) {
        if (registeredDrivers == null) {
            return;
        }
        registeredDrivers.remove(driver);
    }
    
    /**
     * Gets a driver which accepts databaseURL using jdbcDriver as a fallback.
     * 
     * <p>No checks are made as if the driver loaded from jdbcDriver accepts
     * databaseURL.</p>
     */
    public Driver getDriver(String databaseURL, JDBCDriver jdbcDriver) throws SQLException {
        Driver d = getDriverInternal(databaseURL, jdbcDriver, true);
        if (d == null) {
            throw createDriverNotFoundException();
        }
        return d;
    }

    /**
     * Get the driver for a JDBCDriver.  It only tries to load it using Class.forName() -
     * there is no URL to work with
     */
    public Driver getDriver(JDBCDriver jdbcDriver) throws SQLException {
        ClassLoader l = getClassLoader(jdbcDriver);
        Object driver;
        try {
            driver = Class.forName(jdbcDriver.getClassName(), true, l).newInstance();
        } catch (Exception e) {
            SQLException sqlex = createDriverNotFoundException();
            sqlex.initCause(e);
            throw sqlex;
        }
        if (driver instanceof Driver) {
            return (Driver) driver;
        } else {
            throw new SQLException(driver.getClass().getName()
                    + " is not a driver");                              //NOI18N
        }
    }
    
    /**
     * Gets a driver, but can skip DriverManager and doesn't throw SQLException if a driver can't be found.
     */
    private Driver getDriverInternal(String databaseURL, JDBCDriver jdbcDriver, boolean lookInDriverManager) throws SQLException {
        // try the registered drivers first
        synchronized (this) {
            if (registeredDrivers != null) {
                for (Iterator i = registeredDrivers.iterator(); i.hasNext();) {
                    Driver d = (Driver)i.next();
                    try {
                        if (d.acceptsURL(databaseURL)) {
                            return d;
                        }
                    } catch (SQLException e) {
                        // ignore it, we don't want to exit prematurely
                    }
                }
            }
        }
        
        // didn't find it, try to load it from jdbcDriver, if any
        if (jdbcDriver != null) {
            Driver d = getDriver(jdbcDriver);
            if (d != null) {
                return d;
            }
        }
        
        // still nothing, try DriverManager 
        if (lookInDriverManager) {
            try {
                return DriverManager.getDriver(databaseURL);
            } catch (SQLException e) {
                // ignore it, we don't throw exceptions
            }
        }
        
        return null;
    }
    
    private ClassLoader getClassLoader(JDBCDriver driver) {
        ClassLoader loader = null;
        synchronized (driver2Loader) {
            loader = (ClassLoader)driver2Loader.get(driver);
            if (loader == null) {
                loader = new DbURLClassLoader(driver.getURLs());
                if (LOG) {
                    LOGGER.log(Level.FINE, "Creating " + loader); // NOI18N
                }
                driver2Loader.put(driver, loader);
            } else {
                if (LOG) {
                    LOGGER.log(Level.FINE, "Reusing " + loader); // NOI18N
                }
            }
        }
        return loader;
    }
    
    private SQLException createDriverNotFoundException() {
        return new SQLException("Unable to find a suitable driver", "08001"); // NOI18N
    }
}
