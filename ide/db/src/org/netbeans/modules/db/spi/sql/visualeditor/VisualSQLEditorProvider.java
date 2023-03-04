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

package org.netbeans.modules.db.spi.sql.visualeditor;

import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 * This interface provides a Visual SQL editor. It is used when the Database
 * Explorer needs to open a Visual SQL editor, such as from the
 * Design Query action. The implementation should be placed in the default lookup.
 *
 * @author Jim Davidson
 */
public interface VisualSQLEditorProvider {

    /**
     * Opens a new SQL editor for the specified connection and containing the
     * specified SQL statments and possibly executes them.
     *
     * @param dbconn the databaseconnection set as active in the SQL editor. The
     *        statements are also executed against this connection.
     * @param sql the SQL statements to be put in the editor
     * @param execute whether to execute the SQL statements.
     */
    public void openVisualSQLEditor(DatabaseConnection dbconn, String sql);
}
