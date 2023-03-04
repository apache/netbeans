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

package org.netbeans.modules.db.api.sql.execute;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.nodes.Node;

/**
 * Cookie for executing SQL files.
 *
 * <p>This interface allows a client to execute the SQL statement(s)
 * contained in the implementing object (currently the
 * DataObject for SQL files). Therefore calling
 * the {@link #execute} method will execute the statement(s) contained
 * in the respective file and display the results.</p>
 *
 * @author Andrei Badea
 */
public interface SQLExecuteCookie extends Node.Cookie {

    // XXX this should not be a cookie, just a plain interface;
    // will be fixed when lookups are added to DataObjects

    /**
     * Call this set the current database connection for this cookie.
     * The database connection will be used by the {@link #execute} method.
     */
    public void setDatabaseConnection(DatabaseConnection dbconn);

    /** Allow to set database connection.
     * The database connection will be used by the {@link #execute} method.
     *
     * @return a database connection or null
     * @since 1.10
     */
    public DatabaseConnection getDatabaseConnection();

    /**
     * Call this to execute the statements in the object implementing the
     * cookie and display them in the result window.
     */
    public void execute();
}
