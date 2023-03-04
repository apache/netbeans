/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.db.mysql;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.nodes.Node.Cookie;

/**
 * An interface to a database server
 * @author David
 */
public interface DatabaseServer extends Cookie {
    /**
     * Current state of the server
     */
    public static enum ServerState {CONFIGERR, DISCONNECTED , CONNECTED};

    /**
     * Ask the server to validate it's configuration.  If it's invalid,
     * the state is changed to CONFIGERR and the display name, icon and
     * short description are updated appropriately.
     */
    public void checkConfiguration() throws DatabaseException;

    /**
     * Get the icon for this server
     */
    public Image getIcon();

    /**
     * Does the server have a configuration error
     */
    public boolean hasConfigurationError();

    /**
     * Connect to the server.  If we already have a connection, close
     * it and open a new one.  NOTE this is synchronous and can not be
     * called on the AWT thread.
     * 
     * This method will time out after waiting for the default connection
     * timeout.
     *
     * @throws DatabaseException if an error occurs when connecting
     * @throws TimeoutException if we timed out waiting for the connect
     */
    public void reconnect() throws DatabaseException, TimeoutException;

    /**
     * Connect to the server.  If we already have a connection, close
     * it and open a new one.
     *
     * @param timeToWait how long to wait, in milliseconds, before giving up the attempt
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     * @throws TimeoutException if we timed out waiting for the connect
     */
    public void reconnect(long timeToWait) throws DatabaseException, TimeoutException;

    /**
     * Create a database on the server.  This runs <b>asynchronously</b>
     *
     * @param dbname the name of the database to create
     */
    public void createDatabase(String dbname);

    /**
     * Disconnect from the database.  This runs <b>asynchronously</b>
     */
    public void disconnect();

    /**
     * Disconnect from the database synchronously.  Can not be run
     * on the event dispatch thread.
     */
    public void disconnectSync();

    /**
     * Drop an existing database from the server.  This runs <b>asynchronously</b>
     * This method also removes any Database Connections from the Database Explorer
     * that are for this database.
     *
     * @param dbname the name of the database to drop.
     */
    void dropDatabase(String dbname);

    /**
     * Drop an existing database from the server.  This runs
     * <b>asynchronously</b>
     *
     * @param deleteConnections set to false if you want to delete
     *   associated connections from the Database Explorer
     *
     */
    void dropDatabase(String dbname, boolean deleteConnections);

    /**
     * Get the argument string for running the admin tool
     */
    public String getAdminArgs();

    /**
     * Get the path for the admin tool (could be a URL if the tool is web-based)
     */
    public String getAdminPath();

    /**
     * Get the list of databases.  NOTE that the list is retrieved from
     * a cache to improve performance.  If you want to ensure that the
     * list is up-to-date, call <i>refreshDatabaseList</i>
     *
     * @return
     * @see #refreshDatabaseList()
     * @throws org.netbeans.api.db.explorer.DatabaseException
     */
    public Collection<Database> getDatabases() throws DatabaseException;

    public String getDisplayName();

    public String getHost();

    public String getPassword();

    public String getPort();

    public String getShortDescription();

    public String getStartArgs();

    public String getStartPath();

    public String getStopArgs();

    public String getStopPath();

    public String getURL();

    public String getURL(String databaseName);

    public String getUser();

    /**
     * Get the list of users defined for this server.  This runs
     * <b>synchronously</b> so may cause some delay.
     *
     * @return the list of users
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     * if some problem occurred
     */
    public List<DatabaseUser> getUsers() throws DatabaseException;

    /**
     * Grant full rights to the database to the specified user.  This runs
     * <b>asynchronously</b>
     *
     * @param dbname the database whose rights we are granting
     * @param grantUser the name of the user to grant the rights to
     */
    public void grantFullDatabaseRights(String dbname, DatabaseUser grantUser);

    public boolean isConnected();

    /**
     * Returns true if the server is running last time we checked.  Does not actually
     * perform a check, so can be called on the AWT event thread.  To update this state
     * call reconnect() or checkRunning().
     */
    public ServerState getState();

    public boolean isSavePassword();

    /**
     * Refresh the list of databases for the server.  This runs
     * <b>asynchronously</b>
     */
    public void refreshDatabaseList();

    public void setAdminArgs(String args);

    public void setAdminPath(String path);

    public void setHost(String host);

    public void setPassword(String adminPassword);

    public void setPort(String port);

    public void setSavePassword(boolean savePassword);

    public void setStartArgs(String args);

    public void setStartPath(String path);

    public void setStopArgs(String args);

    public void setStopPath(String path);

    public void setUser(String adminUser);

    public void addChangeListener(ChangeListener listener);

    public void removeChangeListener(ChangeListener listener);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Launch the admin tool.  If the specified admin path is a URL,
     * a browser is launched with the URL.  If the specified admin path
     * is a file, the file path is executed.
     *
     * @return a process object for the executed command if the admin
     * path was a file.  Returns null if the browser was launched.
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     */
    void startAdmin() throws DatabaseException;

    /**
     * Run the start command.  Display stdout and stderr to an output
     * window.  Wait the configured wait time, attempt to connect, and
     * then return.
     *
     * @return true if the server is definitely started, false otherwise (the server is
     * not started or the status is unknown).
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     *
     * @see #getStartWaitTime()
     */
    void start() throws DatabaseException;

    /**
     * Run the stop command.  Display stdout and stderr to an output window.
     * This also disconnects the database server.
     *
     * @throws org.netbeans.api.db.explorer.DatabaseException
     */
    void stop() throws DatabaseException;

    /**
     * Return true if the database exists, false otherwise.  This is run
     * against the cached list of database so may not be completely accurate.
     */
    boolean databaseExists(String dbname) throws DatabaseException;

    /**
     * Throws a DatabaseException if the database connection is invalid.
     * This may result in a communication with the database server
     * and can be slow, particularly with remote servers, so use with caution.
     */
    void validateConnection() throws DatabaseException;
}
