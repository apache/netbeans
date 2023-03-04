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

package org.netbeans.api.db.explorer;

import java.net.URL;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import org.netbeans.modules.db.explorer.DbDriverManager;

/**
 * Encapsulates a JDBC driver.
 */
public final class JDBCDriver {

    private URL[] urls;
    private String clazz;
    private String displayName;
    private String name;

    JDBCDriver(String name, String displayName, String clazz, URL[] urls) {
        assert name != null && displayName != null && clazz != null && urls != null;
        this.name = name;
        this.displayName = displayName;
        this.clazz = clazz;
        this.urls = urls;
    }

    /**
     * Creates a new JDBCDriver instance.
     *
     * @param name the programmatic name of the driver; must not be null.
     * @param displayName the display name of the driver (used for example to display the driver in the UI); must not be null.
     * @param clazz the JDBC driver class; must not be null.
     * @param urls the array of the JDBC driver files URLs; must not be null.
     * 
     * @throws NullPointerException if any of the parameters is null.
     */
    public static JDBCDriver create(String name, String displayName, String clazz, URL[] urls) {
        if (name == null || displayName == null || clazz == null || urls == null) {
            throw new NullPointerException();
        }
        return new JDBCDriver(name, displayName, clazz, urls);
    }
    
    /**
     * Returns the array of the JDBC driver files URLs.
     *
     * @return the non-null array of the JDBC driver files URLs.
     */
    public URL[] getURLs() {
        return urls;
    }
    
    /**
     * Returns the JDBC driver class name.
     *
     * @return the JDBC driver class name.
     */
    public String getClassName() {
        return clazz;
    }
    
    /**
     * Returns the display name of the driver (used for example to display the driver in the UI).
     *
     * @return the display name of the driver.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Return the programmatic driver name.
     *
     * @return the programmatic driver name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get a reference to the underlying java.sql.Driver for this JDBCDriver.
     * This can be useful if you want to use the registered drivers to manage
     * your own JDBC connections independent of the Database Explorer
     *
     * @return an instance of the java.sql.Driver for this JDBCDriver
     *
     * @throws DatabaseException if there was an error trying to get the driver instance
     * 
     * @since 1.28
     */
    public Driver getDriver() throws DatabaseException {
        try {
            return DbDriverManager.getDefault().getDriver(this);
        } catch (SQLException sqle) {
            throw new DatabaseException(sqle);
        }
    }
    
    public String toString() {
        return "JDBCDriver[name='" + name + // NOI18N
                "',displayName='" + displayName + // NOI18N
                "',className='" + clazz + // NOI18N
                "',urls=" + Arrays.asList(urls) + "]"; // NOI18N
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JDBCDriver other = (JDBCDriver) obj;
        if (!Arrays.deepEquals(this.urls, other.urls)) {
            return false;
        }
        if (!Objects.equals(this.clazz, other.clazz)) {
            return false;
        }
        if (!Objects.equals(this.displayName, other.displayName)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
}
