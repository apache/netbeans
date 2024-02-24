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

package org.netbeans.modules.derby;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.derby.spi.support.DerbySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Andrei Badea, Jiri Rechtacek
 *
 */
public final class DerbyDatabasesImpl {
    private static final Logger LOG = Logger.getLogger(DerbyDatabasesImpl.class.getName());
    private static final DerbyDatabasesImpl INSTANCE = new DerbyDatabasesImpl();

    private  Set<ChangeListener> changeListeners = new HashSet<ChangeListener> ();
    private static final String PATH_TO_DATABASE_PREFERENCES = "/org/netbeans/modules/derby/databases/"; // NOI18N
    private static final String USER_KEY = "user"; // NOI18N
    private static final String SCHEMA_KEY = "schema"; // NOI18N
    private static final String PASSWORD_KEY = "password"; // NOI18N

    private DerbyDatabasesImpl() {}

    public static  DerbyDatabasesImpl getDefault() {
        DerbyActivator.activate();
        return INSTANCE;
    }

    /**
     * Checks if the Derby database is registered and the Derby system
     * home is set.
     *
     * @return true if Derby is registered, false otherwise.
     */
    public  boolean isDerbyRegistered() {
        return DerbySupport.getLocation().length() > 0 && DerbySupport.getSystemHome().length() > 0; // NOI18N
    }
    
    /**
     * Returns the Derby system home.
     *
     * @return the Derby system home or null if it is not known.
     */
    public  File getSystemHome() {
        String systemHome = DerbyOptions.getDefault().getSystemHome();
        if (systemHome.length() >= 0) {
            return new File(systemHome);
        }
        return null;
    }

    /**
     * Checks if the given database exists in the Derby system home.
     *
     * @return true if the database exists, false otherwise.
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     */
    public  boolean databaseExists(String databaseName) {
        if (databaseName == null) {
            throw new NullPointerException("The databaseName parameter cannot be null"); // NOI18N
        }
        // just because it makes sense, not really needed anywhere probably
        if ("".equals(databaseName)) { // NOI18N
            return false;
        }

        String systemHome = DerbySupport.getSystemHome();
        if (systemHome.length() <= 0) { // NOI18N
            return false;
        }
        File databaseFile = new File(systemHome, databaseName);
        return databaseFile.exists();
    }

    /**
     * Returns the first free database name using the specified base name.
     * The method attempts to create a database name by appending numbers to
     * the base name, like in "base1", "base2", etc. and returns the
     * first free name found.
     *
     * @return a database name or null if a free database name could not be found.
     *
     * @throws NullPointerException in the <code>baseDatabaseName</code> parameter
     *         could not be found.
     */
    public  String getFirstFreeDatabaseName(String baseDatabaseName) {
        if (baseDatabaseName == null) {
            throw new NullPointerException("The baseDatabaseName parameter cannot be null"); // NOI18N
        }

        String systemHome = DerbySupport.getSystemHome();
        if (systemHome.length() <= 0) { // NOI18N
            return baseDatabaseName;
        }
        File databaseFile = new File(systemHome, baseDatabaseName);
        if (!databaseFile.exists()) {
            return baseDatabaseName;
        }

        int i = 1;
        while (i <= Integer.MAX_VALUE) {
            String databaseName = baseDatabaseName + String.valueOf(i);
            databaseFile = new File(systemHome, databaseName);
            if (!databaseFile.exists()) {
                return databaseName;
            }
            i++;
        }
        return null;
    }

    /**
     * Returns the code point of the first illegal character in the given database
     * name.
     *
     * @return the code point of the first illegal character or -1 if all characters
     *         are valid.
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     */
    public  int getFirstIllegalCharacter(String databaseName) {
        if (databaseName == null) {
            throw new NullPointerException("The databaseName parameter cannot be null"); // NOI18N
        }

        for (int i = 0; i < databaseName.length(); i++) {
            char ch = databaseName.charAt(i);
            if (ch == '/') {
                return (int)ch;
            }
            if (ch == File.separatorChar) {
                return (int)ch;
            }
        }

        return -1;
    }

    /**
     * Creates a new empty database in the Derby system and registers
     * it in the Database Explorer. A <code>DatabaseException</code> is thrown
     * if a database with the given name already exists.
     *
     * <p>This method requires at least the Derby network driver to be registered.
     * Otherwise it will throw an IllegalStateException.</p>
     *
     * <p>This method might take a long time to perform. It is advised that
     * clients do not call this method from the event dispatching thread,
     * where it would block the UI.</p>
     *
     * @param  databaseName the name of the database to created; cannot be nul.
     * @param  user the user to set up authentication for. No authentication
     *         will be set up if <code>user</code> is null or an empty string.
     * @param  password the password for authentication.
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     * @throws IllegalStateException if the Derby network driver is not registered.
     * @throws DatabaseException if an error occurs while creating the database
     *         or registering it in the Database Explorer.
     * @throws IOException if the Derby system home directory does not exist
     *         and it cannot be created.
     */
    public  DatabaseConnection createDatabase(String databaseName, String user, String password) throws DatabaseException, IOException, IllegalStateException {
        if (databaseName == null) {
            throw new NullPointerException("The databaseName parameter cannot be null"); // NOI18N
        }

        ensureSystemHome();
        if (!RegisterDerby.getDefault().ensureStarted(true)) {
            throw new DatabaseException("The Derby server did not start"); // NOI18N
        }

        Driver driver = loadDerbyNetDriver();
        Properties props = new Properties();
        boolean setupAuthentication = (user != null && user.length() >= 0);

        try {
            String url = "jdbc:derby://localhost:" + RegisterDerby.getDefault().getPort() + "/" + databaseName; // NOI18N
            String urlForCreation = url + ";create=true"; // NOI18N
            Connection connection = driver.connect(urlForCreation, props);


            try {
                if (setupAuthentication) {
                    setupDatabaseAuthentication(connection, user, password);
                }
            } finally {
                connection.close();
            }

            if (setupAuthentication) {
                // we have to reboot the database for the authentication properties
                // to take effect
                try {
                    connection = driver.connect(url + ";shutdown=true", props); // NOI18N
                } catch (SQLException e) {
                    // OK, will always occur
                }
            }
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }

        return registerDatabase(databaseName, user,
                setupAuthentication ? user.toUpperCase() : "APP", // NOI18N
                setupAuthentication ? password : null, setupAuthentication);
    }

    /**
     * Creates the sample database in the Derby system home
     * using the default user and password ("app", resp. "app") and registers
     * it in the Database Explorer. If the sample database already exists
     * it is just registered.
     *
     * <p>This method requires at least the Derby network driver to be registered.
     * Otherwise it will throw an IllegalStateException.</p>
     *
     * <p>This method might take a long time to perform. It is advised that
     * clients do not call this method from the event dispatching thread,
     * where it would block the UI.</p>
     *
     * @throws IllegalStateException if the Derby network driver is not registered.
     * @throws DatabaseException if an error occurs while creating the database
     *         or registering it in the Database Explorer.
     * @throws IOException if the Derby system home directory does not exist
     *         and it cannot be created.
     */
    public  DatabaseConnection createSampleDatabase() throws DatabaseException, IOException, IllegalStateException {
        extractSampleDatabase("sample", false); // NOI18N
        return registerDatabase("sample", "app", "APP", "app", true); // NOI18N
    }

    /**
     * Creates the sample database in the Derby system home using the
     * given database name and the default user and password ("app", resp. "app") and registers
     * it in the Database Explorer. A <code>DatabaseException</code> is thrown
     * if a database with the given name already exists.
     *
     * <p>This method requires at least the Derby network driver to be registered.
     * Otherwise it will throw an IllegalStateException.</p>
     *
     * <p>This method might take a long time to perform. It is advised that
     * clients do not call this method from the event dispatching thread,
     * where it would block the UI.</p>
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     * @throws IllegalStateException if the Derby network driver is not registered.
     * @throws DatabaseException if an error occurs while registering
     *         the new database in the Database Explorer.
     * @throws IOException if the Derby system home directory does not exist
     *         and it cannot be created.
     */
    public  DatabaseConnection createSampleDatabase(String databaseName, boolean existingDBisError) throws DatabaseException, IOException {
        if (databaseName == null) {
            throw new NullPointerException("The databaseName parameter cannot be null"); // NOI18N
        }

        extractSampleDatabase(databaseName, existingDBisError);
        return registerDatabase(databaseName, "app", "APP", "app", true); // NOI18N
    }

    public  List<String> getDatabases() {
        String databaseHome = DerbyOptions.getDefault().getSystemHome();
        if (databaseHome == null || databaseHome.length() == 0) {
            Logger.getLogger(DerbyServerNode.class.getName()).fine("No JavaDB location set.");
            return Collections.emptyList();
        }
        File databaseHomeFile = new File(databaseHome);
        if (! databaseHomeFile.exists()) {
            Logger.getLogger(DerbyServerNode.class.getName()).log(Level.WARNING, "No JavaDB location found on " + databaseHomeFile);
            return Collections.emptyList();
        }
        FileObject databaseHomeFO = FileUtil.toFileObject(databaseHomeFile);
        try {
            databaseHomeFO.getFileSystem().refresh(false);
        } catch (FileStateInvalidException ex) {
            // This should be part of the real filesystem - it is doubtful, that
            // this case is ever reached - just log it
            LOG.log(Level.FINE, "Failed to refresh filesystem", ex);
        }
        Enumeration<? extends FileObject> children = databaseHomeFO.getChildren(false);
        List<String> res = new ArrayList<String>();
        while (children.hasMoreElements()) {
            FileObject candidate = children.nextElement();
            if (Util.isDerbyDatabase(candidate)) {
                Logger.getLogger(DerbyServerNode.class.getName()).fine(candidate.getName() + " added into Databases in " + databaseHome);
                res.add(candidate.getName());
            }
        }
        return res;
    }

    /** XXX - should be part of API, add into DerbyDatabases
     *
     * Drop an existing database from the server. This runs <b>asynchronously</b>
     * This method also removes any Database Connections from the Database Explorer
     * that are for this database.
     *
     * @param dbname the name of the database to drop.
     * @return true if the database dropped, false otherwise.
     * @since ???
     */
    public  boolean dropDatabase(String dbname) {
        if (dbname == null) {
            throw new IllegalArgumentException("The databaseName parameter cannot be null"); // NOI18N
        }
        if (dbname.length() == 0) {
            throw new IllegalArgumentException("The databaseName parameter cannot be empty"); // NOI18N
        }
        String systemHome = DerbyOptions.getDefault().getSystemHome();
        assert systemHome.length() > 0 : "JavaDB SystemHome must be valid, but was " + systemHome;
        if (systemHome.length() <= 0) { // NOI18N
            return false;
        }
        String username = null;
        String password = null;
        // remove all connections first
        for (DatabaseConnection conn : findDatabaseConnections(dbname)) {
            username = conn.getUser();
            if(username != null) {
                password = conn.getPassword();
            }
            try {
                ConnectionManager.getDefault().removeConnection(conn);
            } catch (DatabaseException ex) {
                Logger.getLogger(DerbyServerNode.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }

        // Try to shutdown the dbserver (see
        // http://db.apache.org/derby/docs/10.7/devguide/tdevdvlp40464.html).
        try {
            Driver driver = loadDerbyNetDriver();
            try {
                if (username != null && username.length() > 0) {
                    driver.connect(
                            String.format("jdbc:derby://localhost:%d/%s;user=%s;password=%s;shutdown=true", //NOI18N
                            RegisterDerby.getDefault().getPort(),
                            dbname,
                            username,
                            password),
                            new Properties());
                } else {
                    driver.connect(
                            String.format("jdbc:derby://localhost:%d/%s;shutdown=true", //NOI18N
                            RegisterDerby.getDefault().getPort(),
                            dbname),
                            new Properties());
                }
            } catch (SQLException e) {
                // OK, will always occur
            }
        } catch (DatabaseException ex) {
            Logger.getLogger(DerbyDatabasesImpl.class.getName()).log(Level.INFO,
                    ex.getLocalizedMessage(), ex);
        }

        // remove database from disk
        File databaseFile = new File(systemHome, dbname);
        FileObject fo = FileUtil.toFileObject(databaseFile);
        try {
            if (fo != null) {
                fo.delete();
            } else {
                Logger.getLogger(DerbyServerNode.class.getName()).log(Level.WARNING, databaseFile + " has no corresponding FileObject.");
                return false;
            }
        } catch (IOException ex) {
            Logger.getLogger(DerbyServerNode.class.getName()).log(Level.WARNING, ex.getLocalizedMessage());
            return false;
        }

        // notify change
        notifyChange();
        return true;
    }

    /**
     * Extracts the sample database under the given name in the Derby system home.
     * Does not overwrite an existing database.
     *
     * <p>Not public because used in tests.</p>
     */
     public synchronized void extractSampleDatabase(String databaseName, boolean existingDBisError) throws IOException{
        File systemHomeFile = ensureSystemHome();
        File sourceFO = InstalledFileLocator.getDefault().locate("modules/ext/derbysampledb.zip", "org.netbeans.modules.derby", false); // NOI18N
        FileObject systemHomeFO = FileUtil.toFileObject(systemHomeFile);
        FileObject sampleFO = systemHomeFO.getFileObject(databaseName);
        if (sampleFO == null) {
            sampleFO = systemHomeFO.createFolder(databaseName);
            Util.extractZip(sourceFO, sampleFO);
        } else {
            if(! Util.isDerbyDatabase(sampleFO)) {
                if(sampleFO.getChildren().length != 0) {
                    throw new IOException(String.format(
                            "Directory for sample database already exists and is not empty: '%s'",
                            FileUtil.toFile(sampleFO).getAbsolutePath()
                    ));
                } else {
                    Util.extractZip(sourceFO, sampleFO);
                }
            } else {
                if (existingDBisError) {
                    throw new IOException(String.format(
                            "Target database already exists: '%s'",
                            FileUtil.toFile(sampleFO).getAbsolutePath()
                    ));
                }
            }
        }
    }

    /**
     * Tries to ensure the Derby system home exists (attempts to create it if necessary).
     */
    private  File ensureSystemHome() throws IOException {
        String systemHome = DerbySupport.getSystemHome();
        boolean noSystemHome = false;
        if (systemHome.length() <= 0) { // NOI18N
            noSystemHome = true;
            systemHome = DerbySupport.getDefaultSystemHome();
        }
        File systemHomeFile = new File(systemHome);
        if (!systemHomeFile.exists()){
            // issue 113747: if mkdirs() fails, it can be caused by another thread having succeeded,
            // since there are a few places where sample databases are created at first startup
            if (!systemHomeFile.mkdirs() && !systemHomeFile.exists()) {
                throw new IOException("Could not create the derby.system.home directory " + systemHomeFile); // NOI18N
            }
        }
        if (noSystemHome) {
            DerbySupport.setSystemHome(systemHome);
        }
        return systemHomeFile;
    }

    /**
     * Registers in the Database Explorer the specified database
     * on the local Derby server.
     */
    private  synchronized DatabaseConnection registerDatabase(String databaseName, String user, String schema, String password, boolean rememberPassword) throws DatabaseException {
        JDBCDriver drivers[] = JDBCDriverManager.getDefault().getDrivers(DerbyOptions.DRIVER_CLASS_NET);
        if (drivers.length == 0) {
            throw new IllegalStateException("The " + DerbyOptions.DRIVER_DISP_NAME_NET + " driver was not found"); // NOI18N
        }
        Preferences pref = NbPreferences.root().node(PATH_TO_DATABASE_PREFERENCES + databaseName);
        pref.put(USER_KEY, user == null ? "" : user);
        pref.put(SCHEMA_KEY, schema == null ? "" : schema);
        pref.put(PASSWORD_KEY, password == null ? "" : password);
        DatabaseConnection dbconn = DatabaseConnection.create(drivers[0], "jdbc:derby://localhost:" + RegisterDerby.getDefault().getPort() + "/" + databaseName, user, schema, password, rememberPassword); // NOI18N
        if (ConnectionManager.getDefault().getConnection(dbconn.getName()) == null) {
            ConnectionManager.getDefault().addConnection(dbconn);
        }
        notifyChange();
        return dbconn;
    }

    /**
     * Sets up authentication for the database to which the given connection
     * is connected.
     */
    private  void setupDatabaseAuthentication(Connection conn, String user, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("{call SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, ?)}"); // NOI18N
        try {
            stmt.setString(1, "derby.connection.requireAuthentication"); // NOI18N
            stmt.setString(2, "true"); // NOI18N
            stmt.execute();

            stmt.clearParameters();
            stmt.setString(1, "derby.authentication.provider"); // NOI18N
            stmt.setString(2, "BUILTIN"); // NOI18N
            stmt.execute();

            stmt.clearParameters();
            stmt.setString(1, "derby.user." + user); // NOI18N
            stmt.setString(2, password); // NOI18N
            stmt.execute();
            
        } finally {
            stmt.close();
        }
        
        if (! "APP".equalsIgnoreCase(user)) { // NOI18N
            stmt = conn.prepareStatement("CREATE SCHEMA " + user); // NOI18N
            try {
                stmt.execute();
            } finally {
                stmt.close();
            }
        }
    }

    /**
     * Loads the Derby network driver.
     */
    private  Driver loadDerbyNetDriver() throws DatabaseException, IllegalStateException {
        Exception exception = null;
        try {
            File derbyClient = Util.getDerbyFile("lib/derbyclient.jar"); // NOI18N
            if (derbyClient == null || !derbyClient.exists()) {
                throw new IllegalStateException("The " + DerbyOptions.DRIVER_DISP_NAME_NET + " driver was not found"); // NOI18N
            }
            URL[] driverURLs = new URL[] { derbyClient.toURI().toURL() }; // NOI18N
            DbURLClassLoader l = new DbURLClassLoader(driverURLs);
            Class<?> c = Class.forName(DerbyOptions.DRIVER_CLASS_NET, true, l);
            return (Driver)c.getDeclaredConstructor().newInstance();
        } catch (MalformedURLException | ReflectiveOperationException e) {
            exception = e;
        }
        if (exception != null) {
            throw new DatabaseException(exception);
        }
        // should never get here
        return null;
    }

    public  void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public  void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    void notifyChange() {
        ChangeEvent evt = new ChangeEvent(this);

        for ( ChangeListener listener : changeListeners ) {
            listener.stateChanged(evt);
        }
    }

    List<DatabaseConnection> findDatabaseConnections(String databaseName) {
        String url = "jdbc:derby://localhost:" + RegisterDerby.getDefault().getPort() + "/" + databaseName;
        List<DatabaseConnection> result = new ArrayList<DatabaseConnection>();

        DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();

        for (DatabaseConnection conn : connections) {
            // If there's already a connection registered, we're done
            if (conn.getDriverClass().equals(DerbyOptions.DRIVER_CLASS_NET)
                    && conn.getDatabaseURL().equals(url)) {
                result.add(conn);
            }
        }

        return result;
    }

    String getUser(String databaseName) {
        Preferences pref = NbPreferences.root().node(PATH_TO_DATABASE_PREFERENCES + databaseName);
        return pref.get(USER_KEY, "");
   }

    String getSchema(String databaseName) {
        Preferences pref = NbPreferences.root().node(PATH_TO_DATABASE_PREFERENCES + databaseName);
        return pref.get(SCHEMA_KEY, "");
    }

    String getPassword(String databaseName) {
        Preferences pref = NbPreferences.root().node(PATH_TO_DATABASE_PREFERENCES + databaseName);
        return pref.get(PASSWORD_KEY, "");
    }

}
