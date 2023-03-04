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

package org.netbeans.lib.ddl;

import java.sql.Connection;
import java.util.Properties;

/**
* Connection information.
* This interface defines information needed for connection to database
* (database and driver url, login name, password and schema name). It can create JDBC
* connection and feels to be a bean (has propertychange support and customizer).
* Instances of this class uses explorer option to store information about
* open connection.
*
* @author Slavek Psenicka, Radko Najman
*/

public interface DBConnection extends java.io.Serializable
{

    /** Returns the name that is displayed for this connection. */
    public String getDisplayName();

    /** Set the name that is displayed for this connection. */
    public void setDisplayName(String name);

    /** Returns driver URL */
    public String getDriver();

    /** Sets driver URL
    * Fires propertychange event.
    * @param driver DNew driver URL
    */
    public void setDriver(String driver);

    /** Returns database URL */
    public String getDatabase();

    /** Sets database URL
    * Fires propertychange event.
    * @param database New database URL
    */
    public void setDatabase(String database);

    /** Returns user login name */
    public String getUser();

    /** Sets user login name
    * Fires propertychange event.
    * @param user New login name
    */
    public void setUser(String user);

    /** Returns schema name */
    public String getSchema();

    /** Sets schema name
    * Fires propertychange event.
    * @param schema Schema name
    */
    public void setSchema(String schema);

    /** Returns connection name */
    public String getName();

    /** Sets connection name
    * Fires propertychange event.
    * @param name Connection name
    */
    public void setName(String name);
    
    /** Returns driver name */
    public String getDriverName();

    /** Sets driver name
    * Fires propertychange event.
    * @param name Driver name
    */
    public void setDriverName(String name);

    /** Returns if password should be remembered */
    public boolean rememberPassword();

    /** Sets password should be remembered
    * @param flag New flag
    */
    public void setRememberPassword(boolean flag);

    /** Returns password */
    public String getPassword();

    /** Sets password
    * Fires propertychange event.
    * @param password New password
    */
    public void setPassword(String password);

    /** Creates JDBC connection
    * Uses DriverManager to create connection to specified database. Throws 
    * DDLException if none of driver/database/user/password is set or if 
    * driver or database does not exist or is inaccessible.
    */
    public Connection createJDBCConnection() throws DDLException;

    /**
     * Set additional (besides "user" and "password") properties of the
     * connection. Use {@link #setUser(String)} and {@link #setPassword(String)}
     * for setting user and password.
     *
     * @param connectionProperties Additional connection properties.
     */
    public void setConnectionProperties(Properties connectionProperties);

    /**
     * Get additional (besides "user" and "password") connection properties. Use
     * {@link #getUser()} and {@link #getPassword()} to get user and password.
     *
     * @return Object containing additional connection properties, or null if it
     * is not available.
     */
    public Properties getConnectionProperties();
}
