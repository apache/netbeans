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




import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.io.ObjectStreamException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.lib.ddl.CommandNotSupportedException;
import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.ExceptionListener;
import org.netbeans.modules.db.explorer.action.ConnectAction;
import org.netbeans.modules.db.explorer.node.ConnectionNode;
import org.netbeans.modules.db.explorer.node.DDLHelper;
import org.netbeans.modules.db.explorer.node.RootNode;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.TopComponent;



/**
 * Connection information
 * This class encapsulates all information needed for connection to database
 * (database and driver url, login name, password and schema name). It can create JDBC
 * connection and feels to be a bean (has propertychange support and customizer).
 * Instances of this class uses explorer option to store information about
 * open connection.
 */
public final class DatabaseConnection implements DBConnection {
    public enum State {
        disconnected,
        connecting,
        connected,
        failed
    }

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    static final long serialVersionUID =4554639187416958735L;

    private final Set<ExceptionListener> exceptionListeners = Collections.synchronizedSet (new HashSet<ExceptionListener> ());
    private Connection jdbcConnection;

    /** Driver URL and name */
    private String drv, drvname;

    /** Database URL */
    private String db;

    /** User login name */
    private String usr;

    /** The default catalog */
    private String defaultCatalog = null;

    /** The default schema */
    private String defaultSchema = null;

    private Set<String> importantSchemas = null;

    private Set<String> importantCatalogs = null;

    /** Schema name */
    private String schema;

    /** User password */
    private String pwd = ""; //NOI18N

    /** Remembers password */
    private Boolean rpwd = Boolean.FALSE;

    private String connectionFileName;

    /** The support for firing property changes */
    private PropertyChangeSupport propertySupport;

    /** Connection name */
    private String name;

    /** The user-specified name that is to be displayed for this connection. */
    private String displayName;
    
    /** Error code */
    private int errorCode = -1;

    /** this is the connector used for performing connect and disconnect processing */
    private final DatabaseConnector connector = new DatabaseConnector(this);

    /** the DatabaseConnection is essentially used as a container for a metadata model
     * created elsewhere.
     */
    private MetadataModel metadataModel = null;

    /** Properties for connection
     */
    private Properties connectionProperties = new Properties();

    private volatile boolean separateSystemTables = false;

    private Boolean useScrollableCursors = null; // null = driver default

    private State state;
    
    /**
     * The API DatabaseConnection (delegates to this instance)
     */
    private transient org.netbeans.api.db.explorer.DatabaseConnection dbconn;

    private static final String SUPPORT = "_schema_support"; //NOI18N
    public static final String PROP_DRIVER = "driver"; //NOI18N
    public static final String PROP_DATABASE = "database"; //NOI18N
    public static final String PROP_USER = "user"; //NOI18N
    public static final String PROP_PASSWORD = "password"; //NOI18N
    public static final String PROP_REMEMBER_PASSWORD = "rememberpwd";
    public static final String PROP_SCHEMA = "schema"; //NOI18N
    public static final String PROP_DEFSCHEMA = "defaultSchema"; //NOI18N
    public static final String PROP_DEFCATALOG = "defaultCatalog"; //NOI18N
    public static final String PROP_DRIVERNAME = "drivername"; //NOI18N
    public static final String PROP_NAME = "name"; //NOI18N
    public static final String PROP_DISPLAY_NAME = "displayName"; //NOI18N
    public static final String PROP_CONNECTIONPROPERTIES = "connectionProperties";
    public static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N
    public static final int DERBY_UNICODE_ERROR_CODE = 20000;
    private OpenConnectionInterface openConnection = null;
    private volatile JDBCDriver jdbcdrv = null;
    private JDBCDriver[] drivers = null;

    private static final Lookup.Result<OpenConnectionInterface> openConnectionLookupResult;
    private static Collection<? extends OpenConnectionInterface> openConnectionServices = null;
    static {
        openConnectionLookupResult = Lookup.getDefault().lookup(new Lookup.Template<>(OpenConnectionInterface.class));
        openConnectionLookupResult.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                synchronized (DatabaseConnection.class) {
                    openConnectionServices = null;
                }
            }
        });
    }
    
    private static final RequestProcessor RP = new RequestProcessor(DatabaseConnection.class.getName(), 10);

    /** Default constructor */
    @SuppressWarnings("LeakingThisInConstructor")
    public DatabaseConnection() {
        dbconn = DatabaseConnectionAccessor.DEFAULT.createDatabaseConnection(this);
        propertySupport = new PropertyChangeSupport(this);
    }

    /** Advanced constructor
     * Allows to specify all needed information.
     * @param driver Driver URL
     * @param database Database URL
     * @param user User login name
     * @param password User password
     */
    public DatabaseConnection(String driver, String database, String user, String password) {
        this(driver, null, database, null, user, password, null, null);
    }

    public DatabaseConnection(String driver, String driverName, String database,
            String theschema, String user, String password) {
        this(driver, driverName, database, theschema, user, password, null, null);
    }

    public DatabaseConnection(String driver, String driverName, String database, 
            String theschema, String user) {
        this(driver, driverName, database, theschema, user, null, null, null);
    }

    public DatabaseConnection(String driver, String driverName, String database,
            String theschema, String user, Properties connectionProperties) {
        this(driver, driverName, database, theschema, user, null, null, connectionProperties);
    }

    public DatabaseConnection(String driver, String driverName, String database,
            String theschema, String user, String password,
            Boolean rememberPassword) {
        this(driver, driverName, database, theschema, user, password,
                rememberPassword, null);
    }

    public DatabaseConnection(String driver, String driverName, String database,
            String theschema, String user, String password,
            Boolean rememberPassword, Properties connectionProperties) {
        this();
        drv = driver;
        drvname = driverName;
        db = database;
        usr = user;
        pwd = password;
        rpwd = rememberPassword;
        schema = theschema;
        name = getName();
        setConnectionProperties(connectionProperties);
    }

    /**
     * Find a registered JDBC driver matching this connection. The function
     * makes a best effort search, if at least a driver with a matching classname
     * is present this function will succeed. If a driver with a matching name is
     * present this will be returned.
     * 
     * @return matching JDBC driver for connection or NULL if no match is found
     */
    public JDBCDriver findJDBCDriver() {
        JDBCDriver[] drvs = JDBCDriverManager.getDefault().getDrivers(drv);
        if (drivers == null || !Arrays.equals(drvs, drivers)) {
            drivers = drvs;

            JDBCDriver useDriver = null;

            if (drvs.length > 0) {
                // Fallback - potentially false driver (by name), but at least matches requested class
                useDriver = drvs[0];
            for (int i = 0; i < drvs.length; i++) {
                if (drvs[i].getName().equals(getDriverName())) {
                    useDriver = drvs[i];
                    break;
                }
            }
            }
            
            jdbcdrv = useDriver;
        }
        return jdbcdrv;
    }

    public Connection getJDBCConnection(boolean test) {
        Connection conn = getJDBCConnection();
        if (test) {
            if (! test()) {
                try {
                    disconnect();
                } catch (DatabaseException e) {
                    LOGGER.log(Level.FINE, null, e);
                }

                return null;
            }
        }

        return conn;
    }

    public void setMetadataModel(MetadataModel model) {
        metadataModel = model;
    }

    public MetadataModel getMetadataModel() {
        return metadataModel;
    }

    public boolean isVitalConnection() {
        if (this.getJDBCConnection() == null) {
            return false;
        }
        try {
            return !checkClosedWithTimeout(this.getJDBCConnection());
        } catch (Exception ex) {
            if (dbconn != null) {
                try {
                    this.disconnect();
                } catch (DatabaseException ex1) {
                    LOGGER.log(Level.FINE, "While trying vitality of connection: " + ex1.getLocalizedMessage(), ex1);
                }
            }
            LOGGER.log(Level.FINE, "While trying vitality of connection: " + ex.getLocalizedMessage(), ex);
            return false;
        }
    }
    
    /**
     * Check whether a connection is closed, using a reasonable timeout. See bug
     * #221602.
     */
    private static boolean checkClosedWithTimeout(final Connection connection) {
        Callable<Boolean> task = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    SQLWarning warnings = connection.getWarnings();
                    if (LOGGER.isLoggable(Level.FINE) && warnings != null) {
                        LOGGER.log(
                                Level.FINE, 
                                "Warnings while trying vitality of connection: {0}",
                                warnings);
                    }
                    return connection.isClosed();
                } catch (SQLException ex) {
                    LOGGER.log(Level.FINE,
                            "While trying vitality of connection: " //NOI18N
                            + ex.getLocalizedMessage(), ex);
                    return false;
                }
            }
        };
        Future<Boolean> future = RP.submit(task);
        try {
            return future.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException e) {
            return false;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean test() {
        try {
            if (! this.isVitalConnection()) {
                return false;
            }
            
            // Send a command to the server, if it fails we know the connection is invalid.
            try {
                return getJDBCConnection().isValid(10 * 1000);
            } catch (Throwable err) {
                // In case JDBC driver does not implement method 
                getJDBCConnection().getMetaData().getTables(null, null, " ", new String[] { "TABLE" }).close();
            }
        } catch (SQLException | NullPointerException e) {
            if("net.sourceforge.jtds.jdbc.Driver".equals(getDriver()) 
                    && e instanceof SQLException
                    && "07009".equals(((SQLException) e).getSQLState())) {
                // This state is reached when "set showplan_* ON" is run
                // in this case metadata is broken on sql server
                return true;
            }
            LOGGER.log(Level.INFO, NbBundle.getMessage(DatabaseConnection.class,
                    "MSG_TestFailed", getName(), e.getMessage()));
            LOGGER.log(Level.FINE, null, e);
            return false;
        }
        return true;

    }

     private Collection<? extends OpenConnectionInterface> getOpenConnections() {
         if (openConnectionServices == null) {
             openConnectionServices = openConnectionLookupResult.allInstances();
         }
         return openConnectionServices;
     }

     private OpenConnectionInterface getOpenConnection() {
         if (openConnection != null) {
            return openConnection;
        }

         openConnection = new OpenConnection();
         String driver = getDriver();
         if (driver == null) {
             return openConnection;
         }

         // For Java Studio Enterprise. Create instanceof OpenConnection
         try {
             for (OpenConnectionInterface oci : getOpenConnections()) {
                 if (oci.isFor(driver)) {
                     openConnection = oci;
                     break;
                 }
             }
         } catch(Exception ex) {
             LOGGER.log(Level.INFO, null, ex);
         }
         return openConnection;
     }

    /** Returns driver class */
    @Override
    public String getDriver() {
        return drv;
    }

    /** Sets driver class
     * Fires propertychange event.
     * @param driver DNew driver URL
     */
    @Override
    public void setDriver(String driver) {
        if (driver == null || driver.equals(drv)) {
            return;
        }

        String olddrv = drv;
        drv = driver;
        propertySupport.firePropertyChange(PROP_DRIVER, olddrv, drv);
        openConnection = null;
    }

    @Override
    public String getDriverName() {
        return drvname;
    }

    @Override
    public void setDriverName(String name) {
        if (name == null || name.equals(drvname)) {
            return;
        }

        String olddrv = drvname;
        drvname = name;
        if (propertySupport != null) {
            propertySupport.firePropertyChange(PROP_DRIVERNAME, olddrv, drvname);
        }
    }

    /** Returns database URL */
    @Override
    public String getDatabase() {
        if (db == null) {
            db = "";
        }

        return db;
    }

    /** Sets database URL
     * Fires propertychange event.
     * @param database New database URL
     */
    @Override
    public void setDatabase(String database) {
        if (database == null || database.equals(db)) {
            return;
        }

        String oldDisplayName = getDisplayName();
        String oldName = getName();
        String olddb = db;
        db = database;
        name = null;
        name = getName();
        String newDisplayName = getDisplayName();
        if (propertySupport != null) {
            propertySupport.firePropertyChange(PROP_DATABASE, olddb, db);
            propertySupport.firePropertyChange(PROP_NAME, oldName, name);
            if(! oldDisplayName.equals(newDisplayName)) {
                propertySupport.firePropertyChange(PROP_DISPLAY_NAME, oldDisplayName, newDisplayName);
        }
    }
    }

    /** Returns user login name */
    @Override
    public String getUser() {
        if (usr == null) {
            usr = "";
        }

        return usr;
    }

    /** Sets user login name
     * Fires propertychange event.
     * @param user New login name
     */
    @Override
    public void setUser(String user) {
        if (user == null || user.equals(usr)) {
            return;
        }

        String oldDisplayName = getDisplayName();
        String oldName = getName();
        String oldusr = usr;
        usr = user;
        name = null;
        name = getName();
        String newDisplayName = getDisplayName();
        if (propertySupport != null) {
            propertySupport.firePropertyChange(PROP_USER, oldusr, usr);
            propertySupport.firePropertyChange(PROP_NAME, oldName, name);
            if(! oldDisplayName.equals(displayName)) {
                propertySupport.firePropertyChange(PROP_DISPLAY_NAME, oldDisplayName, newDisplayName);
        }
    }
    }

    /** Returns name of the connection */
    @Override
    public String getName() {
        if(name == null) {
            if((getSchema()==null)||(getSchema().length()==0)) {
                name = NbBundle.getMessage (DatabaseConnection.class, "ConnectionNodeUniqueName", getDatabase(), getUser(),
                        NbBundle.getMessage (DatabaseConnection.class, "SchemaIsNotSet")); //NOI18N
            } else {
                name = NbBundle.getMessage (DatabaseConnection.class, "ConnectionNodeUniqueName", getDatabase(), getUser(), getSchema()); //NOI18N
            }
        }
        return name;
    }

    /** Sets user name of the connection
     * Fires propertychange event.
     * @param value New connection name
     */
    @Override
    public void setName(String value) {
        if (name == null || getName().equals(value)) {
            return;
        }

        String old = name;
        name = value;
        if (propertySupport != null) {
            propertySupport.firePropertyChange(PROP_NAME, old, name);
        }
    }

    @Override
    public String getDisplayName() {
        return (displayName != null && displayName.length() > 0) ? displayName : getName();
    }

    @Override
    public void setDisplayName(String value) {
        if ((displayName == null && value == null) || getDisplayName().equals(value)) {
            return;
        }

        String old = displayName;
        displayName = value;
        if (propertySupport != null) {
            propertySupport.firePropertyChange(PROP_DISPLAY_NAME, old, displayName);
        }
    }

    @Override
    public Properties getConnectionProperties() {
        return (Properties) connectionProperties.clone();
    }

    @Override
    public void setConnectionProperties(Properties connectionProperties) {
        Properties old = this.connectionProperties;
        if (connectionProperties == null) {
            this.connectionProperties = new Properties();
        } else {
            this.connectionProperties = (Properties) connectionProperties.clone();
        }
        propertySupport.firePropertyChange(PROP_CONNECTIONPROPERTIES, old, connectionProperties);
    }

    /** Returns user schema name */
    @Override
    public String getSchema() {
        if (schema == null) {
            schema = "";
        }

        if (schema.length() == 0) {
            return defaultSchema == null ? "" : defaultSchema;
        }

        return schema;
    }

    /** Sets user schema name
     * Fires propertychange event.
     * @param schema_name New login name
     */
    @Override
    public void setSchema(String schema_name) {
        if (schema_name == null || schema_name.equals(schema)) {
            return;
        }
        String oldDisplayName = getDisplayName();
        String oldName = getName();
        String oldschema = schema;
        name = null;
        schema = schema_name;
        name = getName();
        String newDisplayName = getDisplayName();
        if (propertySupport != null) {
            propertySupport.firePropertyChange(PROP_SCHEMA, oldschema, schema);
            propertySupport.firePropertyChange(PROP_NAME, oldName, name);
            if(! oldDisplayName.equals(displayName)) {
                propertySupport.firePropertyChange(PROP_DISPLAY_NAME, oldDisplayName, newDisplayName);
        }
    }
    }

    public void setDefaultCatalog(String val) throws CommandNotSupportedException, DDLException {
        DDLHelper.setDefaultDatabase(getConnector().getDatabaseSpecification(), val);
        String oldVal = defaultCatalog;
        defaultCatalog = val;

        if (propertySupport != null) {
            propertySupport.firePropertyChange(PROP_DEFCATALOG, oldVal, defaultCatalog);
        }
    }

    public String getDefaultCatalog() {
        return defaultCatalog;
    }

    public void setDefaultSchema(String newDefaultSchema) throws Exception {
        DDLHelper.setDefaultSchema(getConnector().getDatabaseSpecification(), newDefaultSchema);

        String oldName = name;
        name = null;

        String oldDefaultSchema = defaultSchema;
        defaultSchema = newDefaultSchema;
        
        name = getName();

        if (propertySupport != null) {
            propertySupport.firePropertyChange(PROP_DEFSCHEMA, oldDefaultSchema, defaultSchema);
            propertySupport.firePropertyChange(PROP_SCHEMA, schema, getSchema());
            propertySupport.firePropertyChange(PROP_NAME, oldName, name);
        }
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setConnectionFileName(String connectionFileName) {
        this.connectionFileName = connectionFileName;
    }
    
    private void restorePassword() {
        if (this.connectionFileName == null) {
            LOGGER.log(Level.FINE, "No connectionFileName for {0}", this);
            pwd = "";
            rpwd = false;
            return ;
        }
        final String key = this.connectionFileName;
        // If the password was saved, then it means the user checked
        // the box to say the password should be remembered.
        char[] chars = Keyring.read(key);
        if (chars != null) {
            LOGGER.log(Level.FINE, "A password read for {0}", key);
            pwd = String.valueOf(chars);
            rpwd = true;
        } else {
            LOGGER.log(Level.FINE, "No password read for {0}", key);
            pwd = "";
            rpwd = false;
        }
    }

    public static void storePassword(final String key, final char[] pwd) {
        Parameters.notNull("key", key);
        Parameters.notNull("pwd", pwd);

        LOGGER.log(Level.FINE, "Storing password for {0}", key);
        Keyring.save(key,
                pwd,
                NbBundle.getMessage(DatabaseConnectionConvertor.class,
                    "DatabaseConnectionConvertor.password_description", key)); //NOI18N
    }
    
    public static void deletePassword(final String key) {
        Parameters.notNull("key", key);

        LOGGER.log(Level.FINE, "Deleting password for {0}", key);
        Keyring.delete(key);
    }
    
    /** Returns if password should be remembered */
    @Override
    public boolean rememberPassword() {
        if (rpwd == null) {
            restorePassword();
        }
        assert rpwd != null : "rpwd must be set to true or false";
        return rpwd == null ? false : rpwd;
    }

    /** Sets password should be remembered
     * @param flag New flag
     */
    @Override
    public void setRememberPassword(boolean flag) {
        Boolean oldrpwd = rpwd;
        rpwd = flag;
        if (propertySupport != null) {
            propertySupport.firePropertyChange(PROP_REMEMBER_PASSWORD, oldrpwd, rpwd);
        }
    }

    /** Returns password */
    @Override
    public String getPassword() {
        if (pwd == null) {
            restorePassword();
        }
        return pwd;
    }
    
    /** Sets password
     * Fires propertychange event.
     * @param password New password
     */
    @Override
    public void setPassword(String password) {
        if (password == null || password.equals(pwd)) {
            return;
        }
        String oldpwd = pwd;
        if ( password.length() == 0 ) {
            pwd = null;
        } else {
            pwd = password;
        }
        if (propertySupport != null) {
            propertySupport.firePropertyChange(PROP_PASSWORD, oldpwd, pwd);
        }
    }
    
    /** Creates JDBC connection
     * Uses DriverManager to create connection to specified database. Throws
     * DDLException if none of driver/database/user/password is set or if
     * driver or database does not exist or is inaccessible.
     */
    @Override
    public Connection createJDBCConnection() throws DDLException {
        LOGGER.log(Level.FINE, "createJDBCConnection()");

        if (drv == null || db == null || usr == null ) {
            throw new DDLException(NbBundle.getMessage(DatabaseConnection.class, "EXC_InsufficientConnInfo")); // NOI18N
        }

        Properties dbprops;
        if (connectionProperties != null) {
            dbprops = getConnectionProperties();
        } else {
            dbprops = new Properties();
        }
        if ((usr != null) && (usr.length() > 0)) {
            dbprops.put("user", usr); //NOI18N
        }
        if ((pwd != null) && (pwd.length() > 0)) {
            dbprops.put("password", pwd); //NOI18N
        }

        try {
            setState(State.connecting);

            // For Java Studio Enterprise.
            getOpenConnection().enable();
            startRuntimes();

            // hack for Derby
            DerbyConectionEventListener.getDefault().beforeConnect(this);

            JDBCDriver useDriver = findJDBCDriver();
            if (useDriver == null) {
                // will be loaded through DriverManager, make sure it is loaded
                Class.forName(drv);
            }

            Connection connection = DbDriverManager.getDefault().getConnection(db, dbprops, useDriver);
            setJDBCConnection(connection);

            DatabaseUILogger.logConnection(drv);

            setState(State.connected);

            // For Java Studio Enterprise.
            getOpenConnection().disable();

            return connection;
        } catch (SQLException e) {
            String message = NbBundle.getMessage (DatabaseConnection.class, "EXC_CannotEstablishConnection", db, drv, e.getMessage()); // NOI18N

            setState(State.failed);

            // For Java Studio Enterprise.
            getOpenConnection().disable();

            initSQLException(e);
            throw new DDLException(message, e);
        } catch (ClassNotFoundException | RuntimeException exc) {
            String message = NbBundle.getMessage (DatabaseConnection.class, "EXC_CannotEstablishConnection", db, drv, exc.getMessage()); // NOI18N

            setState(State.failed);

            // For Java Studio Enterprise.
            getOpenConnection().disable();
            throw new DDLException(message, exc);
        }
    }

        public void connectSync() throws DatabaseException {
        try {
            doConnect();
        } catch (Exception exc) {
            try {
                if (getJDBCConnection() != null) {
                    getJDBCConnection().close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.FINE, null, e);
            }
            throw new DatabaseException(exc);
        }
    }

        /* return Error code for unit test */
        public int getErrorCode() {
            return errorCode;
        }

    @SuppressWarnings("deprecation")
    private void doConnect() throws DDLException {
        if (drv == null || db == null || usr == null ) {
            sendException(new DDLException(NbBundle.getMessage(DatabaseConnection.class, "EXC_InsufficientConnInfo")));
        }

        Properties dbprops;
        if (connectionProperties != null) {
            dbprops = getConnectionProperties();
        } else {
            dbprops = new Properties();
        }
        if ((usr != null) && (usr.length() > 0)) {
            dbprops.put("user", usr); //NOI18N
        }
        if ((pwd != null) && (pwd.length() > 0)) {
            dbprops.put("password", pwd); //NOI18N
        }

        Connection conn = null;
        try {
            setState(State.connecting);

            // For Java Studio Enterprise.
            getOpenConnection().enable();

            startRuntimes();

            // hack for Derby
            DerbyConectionEventListener.getDefault().beforeConnect(DatabaseConnection.this);

            JDBCDriver useDriver = findJDBCDriver();
            if (useDriver == null) {
                // will be loaded through DriverManager, make sure it is loaded
                Class.forName(drv);
            }

            conn = DbDriverManager.getDefault().getConnection(db, dbprops, useDriver);
            setJDBCConnection(conn);

            DatabaseUILogger.logConnection(drv);

            connector.finishConnect(null);
            
            setState(State.connected);
            
            if (getConnector().getDatabaseSpecification() != null && getConnector().supportsCommand(Specification.DEFAULT_SCHEMA)) {
                try {
                    setDefaultSchema(getSchema());
                } catch (DDLException | CommandNotSupportedException x) {
                    LOGGER.log(Level.INFO, x.getLocalizedMessage(), x);
                }
            }
        } catch (Exception e) {
            String message = NbBundle.getMessage (DatabaseConnection.class, "EXC_CannotEstablishConnection", // NOI18N
                        db, drv, e.getMessage());
            // Issue 69265
            if (drv.equals(DRIVER_CLASS_NET)) {
                if (e instanceof SQLException) {
                    errorCode = ((SQLException) e).getErrorCode();
                    if (errorCode == DERBY_UNICODE_ERROR_CODE) {
                        message = MessageFormat.format(NbBundle.getMessage(DatabaseConnection.class, "EXC_DerbyCreateDatabaseUnicode"),message, db); // NOI18N
                    }
                }
            }

            setState(State.failed);

            if (e instanceof SQLException) {
                initSQLException((SQLException)e);
            }

            DDLException ddle = new DDLException(message);
            ddle.initCause(e);

            if (conn != null) {
                setJDBCConnection(null);
                try {
                    conn.close();
                } catch (SQLException sqle) {
                    LOGGER.log(Level.WARNING, null, sqle); // NOI18N
                }
            }

            throw ddle;
        } catch (Throwable t) {
            // Log JDBC errors that extend from Error rather than Exception, e.g. a NoClassDefFoundError originating from a JDBC driver.
            LOGGER.log(Level.WARNING, "JDBC connection error", t); // NOI18N
            String message = NbBundle.getMessage (DatabaseConnection.class, "EXC_CannotEstablishConnection", // NOI18N
                        db, drv, t.getMessage());
            DialogDisplayer.getDefault ().notifyLater (new NotifyDescriptor.Exception (t, message));
            setState(State.failed);
        } finally {
            getOpenConnection().disable();
        }
    }

    public Task connectAsync() {
        LOGGER.log(Level.FINE, "connect()");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    doConnect();
                } catch (Exception e) {
                    sendException(e);

                }
            }
        };

        Task task = RP.post(runnable, 0);
        return task;
    }

    public boolean isConnected() {
        return jdbcConnection != null;
    }

    /** Calls the initCause() for SQLException with the value
      * of getNextException() so this exception's stack trace contains
      * the complete data.
      */
    private void initSQLException(SQLException e) {
        SQLException current = e;
        SQLException next = current.getNextException();
        while (next != null) {
            try {
                current.initCause(next);
            } catch (IllegalStateException e2) {
                // do nothing, already initialized
            }
            current = next;
            next = current.getNextException();
        }
    }

    private void startRuntimes() {
        DatabaseRuntime[] runtimes = DatabaseRuntimeManager.getDefault().getRuntimes(drv);

        for (int i = 0; i < runtimes.length; i++) {
            DatabaseRuntime runtime = runtimes[i];
            if (runtime.isRunning()) {
                continue;
            }
            if (runtime.canStart() && runtime.acceptsDatabaseURL(db)) {
                runtime.start();
            }
        }
    }

    public void addExceptionListener(ExceptionListener l) {
        if (l != null) {
            exceptionListeners.add(l);
        }
    }

    public void removeExceptionListener(ExceptionListener l) {
        exceptionListeners.remove(l);
    }

    private void sendException(Exception exc) {
        List<ExceptionListener> listeners = new ArrayList<>();
        synchronized (exceptionListeners) {
            for (ExceptionListener l : exceptionListeners) {
                listeners.add(l);
            }
        }

        for (ExceptionListener listener : listeners) {
            listener.exceptionOccurred(exc);
        }
    }

    private void setJDBCConnection(Connection c) {
        jdbcConnection = c;
    }

    public Connection getJDBCConnection() {
        return jdbcConnection;
    }

    /** Add property change listener
     * Registers a listener for the PropertyChange event. The connection object
     * should fire a PropertyChange event whenever somebody changes driver, database,
     * login name or password.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener(l);
    }

    /** Remove property change listener
     * Remove a listener for the PropertyChange event.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener(l);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(drv) + Objects.hashCode(db) + Objects.hashCode(usr);
    }

    /** Compares two connections.
     * Returns true if driver, database and login name equals.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DatabaseConnection) {
            DatabaseConnection conn = (DatabaseConnection) obj;
            return Objects.equals(drv, conn.drv)
                    && Objects.equals(drvname, conn.drvname)
                    && Objects.equals(db, conn.db)
                    && Objects.equals(usr, conn.usr)
                    && Objects.equals(getSchema(), conn.getSchema())
                    && Objects.equals(getConnectionProperties(), conn.getConnectionProperties());
        }

        return false;
    }

    /** Reads object from stream */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        drv = (String) in.readObject();
        db = (String) in.readObject();
        usr = (String) in.readObject();
        schema = (String) in.readObject();
        rpwd = Boolean.FALSE;
        name = (String) in.readObject();

        try {
            drvname = (String) in.readObject();
            displayName = (String) in.readObject();
        } catch (Exception exc) {
            //IGNORE - drvname not stored in 3.6 and earlier
            //IGNORE - displayName not stored in 6.7 and earlier
        }
        try {
            connectionProperties = (Properties) in.readObject();
        } catch (Exception ex) {
            //IGNORE - connectionProperties not stored in 7.3 and earlier
        }

        // boston setting/pilsen setting?
        if ((name != null) && (name.equals(DatabaseConnection.SUPPORT))) {
            // pilsen
        } else {
            // boston
            schema = null;
        }
        name = null;
        name = getName();

        dbconn = DatabaseConnectionAccessor.DEFAULT.createDatabaseConnection(this);
    }

    /** Writes object to stream */
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.writeObject(drv);
        out.writeObject(db);
        out.writeObject(usr);
        out.writeObject(schema);
        out.writeObject(DatabaseConnection.SUPPORT);
        out.writeObject(drvname);
        out.writeObject(displayName);
        out.writeObject(connectionProperties);
    }

    @Override
    public String toString() {
        return "Driver:" + getDriver() + "Database:" + getDatabase().toLowerCase() + "User:" + getUser().toLowerCase() + "Schema:" + getSchema().toLowerCase(); // NOI18N
    }

    /**
     * Gets the API DatabaseConnection which corresponds to this connection.
     */
    public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection() {
        return dbconn;
    }

    public void selectInExplorer() {
        selectInExplorer(true);
    }

    public void selectInExplorer(final boolean activateTopComponent) {
        TopComponent servicesTab = null;
        ExplorerManager explorer = null;
        for (TopComponent component : TopComponent.getRegistry().getOpened()) {
            if (component.getClass().getName().equals("org.netbeans.core.ide.ServicesTab")) {  //NOI18N
                servicesTab = component;
                assert servicesTab instanceof ExplorerManager.Provider;
                explorer = ((ExplorerManager.Provider) servicesTab).getExplorerManager();
                break;
            }
        }
        if (explorer == null) {
            // Services tab not open
            return;
        }
        // find connection node in explorer
        Node root = explorer.getRootContext();
        Node databasesNode = null;
        Node connectionNode = null;
        Node[] children = root.getChildren().getNodes();
        for (Node node : children) {
            if (node.getName().equals("Databases")) {  //NOI18N
                databasesNode = node;
                break;
            }
        }
        if (databasesNode == null) {
            return ;
        }
        children = databasesNode.getChildren().getNodes();
        for (Node node : children) {
            if (node.getDisplayName().equals(getDisplayName())) {
                connectionNode = node;
                break;
            }
        }
        // select node
        try {
            if (connectionNode != null) {
                explorer.setSelectedNodes(new Node[] { connectionNode });
                if (activateTopComponent && servicesTab != null) {
                    servicesTab.requestActive();
                }
            }
        } catch (PropertyVetoException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    public void refreshInExplorer() throws DatabaseException {
        final ConnectionNode connectionNode = findConnectionNode(getDisplayName());
        if (connectionNode != null) {
            RP.post(
                new Runnable() {
                    @Override
                    public void run() {
                        MetadataModel model = getMetadataModel();
                        if (model != null) {
                            try {
                                model.runReadAction(
                                    new Action<Metadata>() {
                                        @Override
                                        public void run(Metadata metaData) {
                                            metaData.refresh();
                                        }
                                    }
                                );
                            } catch (MetadataModelException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                        connectionNode.refresh();
                    }
                }
            );
        }
    }

    public void showConnectionDialog() {
        try {
            final ConnectionNode cni = findConnectionNode(getDisplayName());
            assert cni != null : "DatabaseConnection node not found for " + this;
            if (cni != null && (! isConnected())) {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        new ConnectAction.ConnectionDialogDisplayer().showDialog(DatabaseConnection.this, false);
                    }
                });
            }
        } catch (DatabaseException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public DatabaseConnector getConnector() {
        return connector;
    }

    public void notifyChange() {
        propertySupport.firePropertyChange("changed", null, null);
    }

    public void disconnect() throws DatabaseException {
        if (jdbcConnection != null) {
            try {
                jdbcConnection.close();
            } catch (Exception ex) {
    }

            DerbyConectionEventListener.getDefault().afterDisconnect(this, jdbcConnection);
            connector.performDisconnect();
            jdbcConnection = null;
            setState(State.disconnected);
        }
    }

    /**
     * Find a connection node using the supplied name.
     *
     * <p>
     * Assumption: the name of the connection node is the display name of the
     * connection</p>
     *
     * <p>
     * Needed by unit tests as well as internally</p>
     *
     * @param connection display name of the connection for which the connection
     * node should be found
     * @return
     * @throws DatabaseException
     */
    public static ConnectionNode findConnectionNode(String connection) throws DatabaseException {
        assert connection != null;

        RootNode root = RootNode.instance();
        Collection<? extends Node> children = root.getChildNodes();
        for (Node node : children) {
            if (node instanceof ConnectionNode) {
                ConnectionNode cnode = (ConnectionNode)node;
                if (cnode.getName().equals(connection)) {
                    return cnode;
                }
            }
        }

        return null;
    }

    private Object readResolve() throws ObjectStreamException {
        // sometimes deserialized objects have a null propertySuppport, not sure why
        if (propertySupport == null) {
            propertySupport = new PropertyChangeSupport(this);
        }
        return this;
    }

    public Set<String> getImportantSchemas() {
        if (importantSchemas == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(importantSchemas);
        }
    }

    public void addImportantSchema(String schema) {
        if (importantSchemas == null) {
            importantSchemas = new HashSet<>();
        }
        List<String> oldList = new ArrayList<>(importantSchemas);
        importantSchemas.add(schema);
        propertySupport.firePropertyChange("importantSchemas", oldList, importantSchemas); //NOI18N
    }

    public void removeImportantSchema(String schema) {
        if (importantSchemas != null) {
            List<String> oldList = new ArrayList<>(importantSchemas);
            importantSchemas.remove(schema);
            propertySupport.firePropertyChange("importantSchemas", oldList, importantSchemas); //NOI18N
        }
    }

    public boolean isImportantSchema(String schema) {
        return importantSchemas != null && importantSchemas.contains(schema);
    }

    public Set<String> getImportantCatalogs() {
        if (importantCatalogs == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(importantCatalogs);
        }
    }

    public void addImportantCatalog(String database) {
        if (importantCatalogs == null) {
            importantCatalogs = new HashSet<>();
        }
        List<String> oldList = new ArrayList<>(importantCatalogs);
        importantCatalogs.add(database);
        propertySupport.firePropertyChange("importantCatalogs", oldList, importantCatalogs); //NOI18N
    }

    public void removeImportantCatalog(String database) {
        if (importantCatalogs != null) {
            List<String> oldList = new ArrayList<>(importantCatalogs);
            importantCatalogs.remove(database);
            propertySupport.firePropertyChange("importantCatalogs", oldList, importantCatalogs); //NOI18N
        }
    }

    public boolean isImportantCatalog(String database) {
        return importantCatalogs != null && importantCatalogs.contains(database);
    }

    public boolean isSeparateSystemTables() {
        return separateSystemTables;
    }

    public void setSeparateSystemTables(boolean separateSystemTables) {
        boolean oldVal = this.separateSystemTables;
        this.separateSystemTables = separateSystemTables;
        propertySupport.firePropertyChange("separateSystemTables", oldVal, separateSystemTables); //NOI18N
    }

    /**
     * Decide whether scrollable cursors should be used by the connection.
     */
    private boolean isUseScrollableCursorsByDefault() {
        return false;
    }

    public boolean isUseScrollableCursors() {
        return useScrollableCursors == null
                ? isUseScrollableCursorsByDefault()
                : useScrollableCursors;
    }

    public void setUseScrollableCursors(boolean useScrollableCursors) {
        boolean oldVal = isUseScrollableCursors();
        this.useScrollableCursors = useScrollableCursors;
        propertySupport.firePropertyChange("useScrollableCursors", oldVal, useScrollableCursors); //NOI18N
    }

    public State getState() {
        return state;
}

    private void setState(State state) {
        State oldState = this.state;
        this.state = state;
        propertySupport.firePropertyChange("state", oldState, state);
    }
}
