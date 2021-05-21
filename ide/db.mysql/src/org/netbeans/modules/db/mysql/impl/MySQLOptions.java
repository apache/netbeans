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

package org.netbeans.modules.db.mysql.impl;

import org.netbeans.modules.db.mysql.util.Utils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.keyring.Keyring;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Storage and retrieval of options for MySQL support.  These options are
 * stored persistently using NbPreferences API.
 * 
 * @author David Van Couvering
 */
public final class MySQLOptions {
    private String adminPassword;

    private static final Logger LOGGER = Logger.getLogger(MySQLOptions.class.getName());

    private static final MySQLOptions DEFAULT = new MySQLOptions();

    public static final String PROP_MYSQL_LOCATION = "location"; // NOI18N
    public static final String PROP_HOST = "host"; // NO18N
    public static final String PROP_PORT = "port"; // NO18N
    public static final String PROP_ADMINUSER = "adminuser"; // NO18N
    public static final String PROP_SAVEPWD = "savepwd"; // NO18N
    public static final String PROP_DBDIR = "dbdir"; // NO18N
    public static final String PROP_CONN_REGISTERED = "conn-registered"; // NOI18N
    public static final String PROP_PROVIDER_REGISTERED = "provider-registered"; // NOI18N
    public static final String PROP_PROVIDER_REMOVED = "provider-removed"; // NO18N
    public static final String PROP_ADMIN_PATH = "admin-path"; // NOI18N
    public static final String PROP_START_PATH = "start-path"; // NOI18N
    public static final String PROP_STOP_PATH = "stop-path"; // NOI18N
    public static final String PROP_ADMIN_ARGS = "admin-args"; // NOI18N
    public static final String PROP_START_ARGS = "start-args"; // NOI18N
    public static final String PROP_STOP_ARGS = "stop-args"; // NOI18N
    
    // These options are not currently visible in the properties dialog, but
    // can be set by users through direct editing of the preferences file
    
    // How long to wait on the network before giving up on an attempt to
    // connect, in milliseconds
    public static final String PROP_CONNECT_TIMEOUT = "connect-timeout"; // NOII18N
    public static final String PROP_REFRESH_THREAD_SLEEP_INTERVAL =
            "refresh-thread-sleep-interval"; // NOI18N
    
    // Currently not modifiable...
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_ADMIN_USER = "root";
    private static final String DEFAULT_ADMIN_PASSWORD = "";
    // In milliseconds
    private static final String DEFAULT_CONNECT_TIMEOUT = "5000";
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public static MySQLOptions getDefault() {
        return DEFAULT;
    }
    
    private MySQLOptions() {
        if ( Utils.isEmpty(getConnectTimeout()) ) {
            setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        }
    }
    
    protected final void putProperty(String key, String value) {
        String oldval;
        synchronized(this) {
            oldval = getProperty(key);
                if (value != null) {
                    NbPreferences.forModule(MySQLOptions.class).put(key, value);
                } else {
                    NbPreferences.forModule(MySQLOptions.class).remove(key);
                }
            }
        notifyPropertyChange(key, oldval, value);
    }

    protected final void putProperty(String key, boolean value) {
        boolean oldval = getBooleanProperty(key);
        NbPreferences.forModule(MySQLOptions.class).putBoolean(key, value);
        notifyPropertyChange(key, oldval, value);
    }
    
    protected final void putProperty(String key, long value, long def) {
        long oldval = getLongProperty(key, def);
        NbPreferences.forModule(MySQLOptions.class).putLong(key, value);
        notifyPropertyChange(key, oldval, value);
    }
    
    protected final void clearProperty(String key) {
        String oldval = getProperty(key);
        NbPreferences.forModule(MySQLOptions.class).remove(key);
        notifyPropertyChange(key, oldval, null);
    }
    
    protected final String getProperty(String key) {
        return NbPreferences.forModule(MySQLOptions.class).get(key, "");
    }
    
    protected final boolean getBooleanProperty(String key) {
        return NbPreferences.forModule(MySQLOptions.class).getBoolean(key, false); 
    }
    
    protected final long getLongProperty(String key, long def) {
        return NbPreferences.forModule(MySQLOptions.class).getLong(key, def);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    private void notifyPropertyChange(String key, Object oldval, Object newval) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, key, oldval, newval);

        pcs.firePropertyChange(event);
    }

    public String getHost() {
        return getProperty(PROP_HOST);
    }

    public void setHost(String host) {
        putProperty(PROP_HOST, host);
    }

    public String getPort() {
        return getProperty(PROP_PORT);
    }

    public void setPort(String port) {
        putProperty(PROP_PORT, port);
    }

    public String getAdminUser() {
        return getProperty(PROP_ADMINUSER);
    }

    public void setAdminUser(String adminUser) {
        putProperty(PROP_ADMINUSER, adminUser);
    }

    public synchronized String getAdminPassword() {
        // read old settings
        String pwd = NbPreferences.forModule(MySQLOptions.class).get("adminpwd", null); // NOI18N
        // don't store a password anymore
        NbPreferences.forModule(MySQLOptions.class).remove("adminpwd"); // NOI18N
        if (pwd != null) {
            // store using Keyring API
            Keyring.save(MySQLOptions.class.getName(), pwd.toCharArray(), NbBundle.getMessage(MySQLOptions.class, "MySQLOptions_AdminPassword")); // NOI18N
        }
        if ( isSavePassword() ) {
            LOGGER.log(Level.FINE, "Reading a Admin Password from Keyring.");
            char[] chars = Keyring.read(MySQLOptions.class.getName());
            adminPassword = chars == null ? "" : String.copyValueOf(chars);
        }
        return adminPassword;
    }

    public synchronized void setAdminPassword(String adminPassword) {
        // 'null' is a valid password, but if we save as null
        // it will actually clear the property.  So convert it to
        // an empty string.
        if ( adminPassword == null ) {
            adminPassword = "";
        }
        
        // Cache password for this session whether we save it or not.
        this.adminPassword = adminPassword;
        
        if ( isSavePassword() ) {
            LOGGER.log(Level.FINE, "Storing a Admin Password to Keyring.");
            Keyring.save(MySQLOptions.class.getName(), adminPassword.toCharArray(), NbBundle.getMessage(MySQLOptions.class, "MySQLOptions_AdminPassword")); // NOI18N
        } else {
            LOGGER.log(Level.FINE, "Removing a Admin Password from Keyring.");
            Keyring.delete(MySQLOptions.class.getName());
        }
    }
    
    public void clearAdminPassword() {
        LOGGER.log(Level.FINE, "Removing a Admin Password from Keyring.");
        Keyring.delete(MySQLOptions.class.getName());
    }

    public boolean isSavePassword() {
        return getBooleanProperty(PROP_SAVEPWD);
    }

    public void setSavePassword(boolean savePassword) {
        putProperty(PROP_SAVEPWD, savePassword);
        
        // Clear the password from the persistent file if saving
        // passwords is turned off; save the password to the persistent
        // file if saving passwords is turned on
        if (adminPassword == null) {
            // nothing for save
            return ;
        }
        if ( ! savePassword ) {
            clearAdminPassword();
        } else {
            LOGGER.log(Level.FINE, "Storing a Admin Password to Keyring.");
            Keyring.save(MySQLOptions.class.getName(), adminPassword.toCharArray(), NbBundle.getMessage(MySQLOptions.class, "MySQLOptions_AdminPassword")); // NOI18N
        }
    }
    
    public void setConnectionRegistered(boolean registered) {
        putProperty(PROP_CONN_REGISTERED, registered);
    }

    public boolean isConnectionRegistered() {
        return getBooleanProperty(PROP_CONN_REGISTERED);
    }
    
    public void setProviderRegistered(boolean registered) {
        // If the user is unregistering the provider and it was
        // previously registered, mark it as removed so we don't keep
        // trying to auto-register it - very annoying.
        if ( isProviderRegistered() && ! registered ) {
            setProviderRemoved(true);
        } else if ( registered ) {
            setProviderRemoved(false);
        }
        putProperty(PROP_PROVIDER_REGISTERED, registered);
    }
    
    public boolean isProviderRegistered() {
        return getBooleanProperty(PROP_PROVIDER_REGISTERED);
    }
    
    private void setProviderRemoved(boolean removed) {
        putProperty(PROP_PROVIDER_REMOVED, removed);
    }
    
    public boolean isProviderRemoved() {
        return getBooleanProperty(PROP_PROVIDER_REMOVED);
    }
    
    public String getStartPath() {
        return getProperty(PROP_START_PATH);
    }
    
    public void setStartPath(String path) {
        putProperty(PROP_START_PATH, path);
    }
    
    public String getStartArgs() {
        return getProperty(PROP_START_ARGS);
    }
    
    public void setStartArgs(String args) {
        putProperty(PROP_START_ARGS, args);
    }
    
    public String getStopPath() {
        return getProperty(PROP_STOP_PATH);
    }
    
    public void setStopPath(String path) {
        putProperty(PROP_STOP_PATH, path);
    }
    
    public String getStopArgs() {
        return getProperty(PROP_STOP_ARGS);
    }
    
    public void setStopArgs(String args) {
        putProperty(PROP_STOP_ARGS, args);
    }
    public String getAdminPath() {
        return getProperty(PROP_ADMIN_PATH);
    }
    
    public void setAdminPath(String path) {
        putProperty(PROP_ADMIN_PATH, path);
    }

    public String getAdminArgs() {
        return getProperty(PROP_ADMIN_ARGS);
    }
    
    public void setAdminArgs(String args) {
        putProperty(PROP_ADMIN_ARGS, args);
    }

    public String getConnectTimeout() {
        return getProperty(PROP_CONNECT_TIMEOUT);
    }
    
    public void setConnectTimeout(String timeout) {
        putProperty(PROP_CONNECT_TIMEOUT, timeout);
    }
    
    public static String getDriverClass() {
        return DRIVER_CLASS;
    }
    public static String getDefaultPort() {
        return DEFAULT_PORT;
    }

    public static String getDefaultAdminPassword() {
        return DEFAULT_ADMIN_PASSWORD;
    }

    public static String getDefaultAdminUser() {
        return DEFAULT_ADMIN_USER;
    }

    public static String getDefaultHost() {
        return DEFAULT_HOST;
    }


}
