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

package org.netbeans.modules.db.sql.visualeditor.api;

import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 * Factory class for creating VisualSQLEditor instances.
 *
 * @author Jim Davidson
 */
public final class VisualSQLEditorFactory {
    
    /**
     * Creates and returns a new VisualSQLEditor.
     *
     * @param dbconn the DatabaseConnection
     * @param statement the initial SQL query to be loaded into the editor
     * @param metadata metadata cache maintained by the client, or null.  If null, the VisualSQLEditor will
     * fetch and manage its own metadata, using the DatabaseConnection
     * @return the new VisualSQLEditor instance
     *
     */
    public static VisualSQLEditor createVisualSQLEditor(DatabaseConnection dbconn, String statement, VisualSQLEditorMetaData metadata) {
        return new VisualSQLEditor(dbconn, statement, metadata);
    }
    
    // Private constructor
    private VisualSQLEditorFactory(){};
}

