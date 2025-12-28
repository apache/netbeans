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

package org.netbeans.modules.db.mysql.util;

import org.netbeans.modules.db.mysql.impl.MySQLOptions;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author David
 */
public class DatabaseUtils {
    // Separate request processor for opening connections so that we are safe
    // to wait for it to complete.  If we use the default request processor it's
    // possible waiting will fail with an InterruptedException if the calling thread
    // is also the default request processor.
    private static final RequestProcessor PROCESSOR = new RequestProcessor();

    // MySQL's SQL State for a communication error.  
    public static final String SQLSTATE_COMM_ERROR = "08S01";
    // The SQL State prefix (class) used for client-side exceptions
    private static final String SQLSTATE_CLIENT_PREFIX = "20";
    // Default URL parameters
    private static final String DFLT_CONFIG_OPTIONS =
            "?zeroDateTimeBehavior=CONVERT_TO_NULL";                      //NOI18N
    
    private static final Logger LOGGER = 
            Logger.getLogger(DatabaseUtils.class.getName());
    
    // A cache of the driver class so we don't have to load it each time
    private static Driver driver;

    /**
     * An enumeration indicating the status after attempting to connect to the
     * server
     * 
     * @author David Van Couvering
     */
    public enum ConnectStatus {
        /** The server was not detected at the given host/port */
        NO_SERVER, 

        /** We could establish a connection, but authentication failed with
         * the given user and password
         */
        SERVER_RUNNING, 

        /** We were able to connect and authenticate */
        CONNECT_SUCCEEDED

    }
     
    public static boolean isEmpty(String val) {
        return (val == null || val.length() == 0);
    }
     
    public static JDBCDriver getJDBCDriver() {
        JDBCDriver[]  drivers = JDBCDriverManager.getDefault().
                getDrivers(MySQLOptions.getDriverClass());

        if ( drivers.length == 0 ) {
            return null;
        }

        return drivers[0];
    }
     
    /**
     * Load and return the JDBC Driver for MySQL.  This method
     * gets the search path for the MySQL driver, creates a classloader
     * that uses this search path, and then instantiates the driver
     * class from this classloader
     * 
     * @return an instance of the MySQL driver.
     * @throws org.netbeans.api.db.explorer.DatabaseException
     *      If an error occured while trying to load the driver
     */
    public static Driver getDriver() throws DatabaseException {
        if ( driver != null ) {
            return driver;
        }
        JDBCDriver jdbcDriver = getJDBCDriver();
        
        if ( jdbcDriver == null ) {
            throw new DatabaseException(
                    Utils.getMessage( 
                    "MSG_JDBCDriverNotRegistered"));
        }
                
        try {
            ClassLoader driverLoader = new DriverClassLoader(jdbcDriver);
            driver = (Driver)Class.forName(jdbcDriver.getClassName(), 
                    true, driverLoader).getDeclaredConstructor().newInstance();
        } catch ( Exception e ) {
            DatabaseException dbe = new DatabaseException(
                    Utils.getMessage(
                        "MSG_UnableToLoadJDBCDriver") + e.getMessage());
            dbe.initCause(e);
            throw dbe;
        }
        
        return driver;
    }
    
    /**
     * Get a JDBC connection from a DatabaseConnection, bringing up a dialog
     * to connect if necessary
     * 
     * @param dbconn
     * @return the resulting JDBC connection
     * @throws java.sql.SQLException if there was an error connecting
     */
    public static Connection getConnection(DatabaseConnection dbconn) throws SQLException {
        Connection conn = dbconn.getJDBCConnection();
        
        if ( conn == null || conn.isClosed()) {
            ConnectionManager.getDefault().showConnectionDialog(dbconn);
            conn = dbconn.getJDBCConnection();
        }    
        
        return conn;
    }

    public static Connection connect(final String url, String user, String password, long timeToWait)
            throws DatabaseException, TimeoutException {
        final Driver theDriver = getDriver();

        final Properties props = new Properties();
        props.put("user", user);

        if (password != null) {
            props.put("password", password);
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Connection> future = executor.submit(new Callable<Connection>() {
            public Connection call() throws Exception {
                props.put("connectTimeout", MySQLOptions.getDefault().getConnectTimeout());

                try {
                    return theDriver.connect(url, props);
                } catch (SQLException sqle) {
                    if (DatabaseUtils.isCommunicationsException(sqle)) {
                        // On a communications failure (e.g. the server's not running)
                        // the message horribly includes the entire stack trace of the
                        // exception chain.  We don't want to display this to our users,
                        // so let's provide our own message...
                        //
                        // If other MySQL exceptions exhibit this behavior we'll have to
                        // address this in a more general way...
                        String msg = Utils.getMessage("ERR_MySQLCommunicationFailure");

                        DatabaseException dbe = new DatabaseException(msg);
                        dbe.initCause(sqle);
                        throw dbe;
                    } else {
                        throw new DatabaseException(sqle);
                    }
                }
            }
        });

        try {
            return future.get(timeToWait, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new DatabaseException(ie);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DatabaseException) {
                throw new DatabaseException(e.getCause());
            } else {
                throw Utils.launderThrowable(e.getCause());
            }
        } catch (TimeoutException te) {
            future.cancel(true);
            throw new TimeoutException(NbBundle.getMessage(DatabaseUtils.class, "MSG_ConnectTimedOut"));
        }
    }

    /** 
     * Open a JDBC connection directly from the MySQL driver
     * 
     * @ return a live JDBC connection
     * @throws SQLException if there was a problem connecting
     * @throws DatabaseException if there were issues getting the MySQL driver
     */
    public static Connection connect(String url, String user, String password)
            throws DatabaseException, TimeoutException {
        return connect(url, user, password, 5000);
    }

    /**
     * Handles all that annoying try/catch stuff around closing a connection
     * 
     * @param conn the connection to close
     */
    public static void closeConnection(Connection conn) {
        try {
            if ( conn != null ) {
                conn.close();
            } 
        } catch (SQLException e) {
            LOGGER.log(Level.FINE, null, e);
        }
    }
        
    /**
     * Find a registered database connection.
     * 
     * @param the database URL to use
     * @param user the user name for the connection
     * @param password the password for the connection; can be null
     * 
     * @return the database connection
     */
    public static DatabaseConnection findDatabaseConnection(String url, String user) {
        List<DatabaseConnection> conns =
                findDatabaseConnections(url);
        
        for ( DatabaseConnection conn : conns ) {
            if ( conn.getUser().equals(user)) {
                return conn;
            }
        }
        
        return null;
    }
    
    /** 
     * Find all registered database connections that match the given URL
     * (there could be multiple ones, for different users
     * 
     * @param host
     * @param port
     * @return
     */
    public static List<DatabaseConnection> findDatabaseConnections(String url) {
        ArrayList<DatabaseConnection> result =
                new ArrayList<DatabaseConnection>();
                
        DatabaseConnection[] connections = 
            ConnectionManager.getDefault().getConnections();

        for ( DatabaseConnection conn : connections ) {
            // If there's already a connection registered, we're done
            if ( conn.getDriverClass().equals(MySQLOptions.getDriverClass()) &&
                 conn.getDatabaseURL().equals(url) ) {
                result.add(conn);
            }
        }
        
        return result;
    }
    
    public static String getURL(String host, String port) {
        return getURL(host, port, null);
    }
    
    public static String getURL(String host, String port, String database) {
        // Format is jdbc:mysql://<HOST>:<PORT>
        // No database is specified for an admin connection.
        StringBuffer url = new StringBuffer("jdbc:mysql://"); // NOI18N
        url.append( host == null || host.equals("") ? "localhost" : host); // NO18N
        if ( port != null && (! port.equals("")) ) {
            url.append(":" + port);
        }
        if ( database != null && (! database.equals(""))) {
            url.append("/" + database); // NOI18N
        }
        url.append(DFLT_CONFIG_OPTIONS); //#183440
        return url.toString();
    }

    public static boolean ensureConnected(DatabaseConnection dbconn) {
        try {
            Connection conn = dbconn.getJDBCConnection();
            if ( conn == null || conn.isClosed() ) {
                ConnectionManager.getDefault().showConnectionDialog(dbconn);
            }

            conn = dbconn.getJDBCConnection();
            
            if ( conn == null || conn.isClosed() ) {
                return false;
            }

            return true;
        } catch ( SQLException e ) {
            Exceptions.printStackTrace(e);
            return false;
        }
    }

    public static boolean isCommunicationsException(DatabaseException dbe) {
        if (!(dbe.getCause() instanceof SQLException)) {
            return false;
        }

        return isCommunicationsException((SQLException)dbe.getCause());
    }

    public static boolean isCommunicationsException(SQLException sqle) {
        // Using string so we don't have to depend directly on MySQL JDBC driver
        return sqle.getClass().getName().equals("com.mysql.jdbc.CommunicationsException"); // NOI18n
    }
    
    public static boolean isServerException(SQLException e) {
        //
        // See http://dev.mysql.com/doc/refman/5.0/en/error-handling.html
        // for info on MySQL errors and sql states.
        //
        String sqlstate = e.getSQLState();
        SQLException nexte = e.getNextException();

        if ( SQLSTATE_COMM_ERROR.equals(sqlstate)) { // Communications exception
            return false;
        }

        if ( sqlstate.startsWith(SQLSTATE_CLIENT_PREFIX))  {
            // An exception whose SQL state starts with this prefix is
            // client side-only.  So any SQL state that *doesn't*
            // start with this prefix must have come from a live server
            return false;
        }

        if ( nexte != null ) {
            return ( isServerException(nexte) );
        } 

        return true;
    }
    
    public static class URLParser {
        private static final String MYSQL_PROTOCOL = "jdbc:mysql://";
        private String host;
        private String port;
                
        private final String url;
        public URLParser(String url) {
            assert(url != null && url.startsWith(MYSQL_PROTOCOL));
            
            this.url = url.replaceFirst(MYSQL_PROTOCOL, "");
        }
        
        public String getHost() {
            if ( host == null ) {
                if ( url.indexOf(":") >= 0 ) {
                    host = url.split(":")[0];
                } else {
                   host = url.split("/")[0]; 
                }
            }
            
            return host;
        }
        
        public String getPort() {
            if ( port == null ) {
                if ( url.indexOf(":") >= 0 ) {
                    port = url.split(":")[1];
                    if ( port.indexOf("/") >= 0) {
                        port = port.split("/")[0];
                    }
                } else {
                   port = "";
                }
            }
            
            return port;            
        }
    }
}
