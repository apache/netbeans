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

package org.netbeans.modules.db.api.explorer;

import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 * Interface allowing to listen on changes of the database metadata caused
 * by the Database Explorer, such as adding or dropping a table or a column.
 *
 * @author Andrei Badea
 */
public interface MetaDataListener {

    /**
     * Invoked when the list of tables in the database represented by the
     * <code>dbconn</code> parameter has changed.
     *
     * @param dbconn the database connection whose tables have changed
     */
    void tablesChanged(DatabaseConnection dbconn);

    /**
     * Invoked when the structure of a table in the database represented by the
     * <code>dbconn</code> parameter has changed.
     *
     * @param dbconn the database connection whose table have changed
     * @param tableName the name of the table which changed
     */
    void tableChanged(DatabaseConnection dbconn, String tableName);
}
