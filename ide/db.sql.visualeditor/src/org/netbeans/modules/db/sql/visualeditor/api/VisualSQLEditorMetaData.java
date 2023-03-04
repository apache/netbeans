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

import java.sql.SQLException;
import java.util.List;

/**
 * Implements a cache for metadata that is supplied to the Visual SQL Editor
 *
 * @author Joel Brown, Jim Davidson
 */
public interface VisualSQLEditorMetaData {

    /**
     * Returns the schemas that are included in this DataSource/DatabaseConnection
     * Used during Add Table and similar operations.
     *
     * @return the List of schema names
     */
    public List<String> getSchemas() ;

    /**
     * Returns the tables (and views) in this DataSource/DatabaseConnection
     * 
     * @return the List of tables/views, each in the form of a {@literal List<schema, table>}
     */
    // public List<List<String>> getTables(String schema) throws SQLException ;
    public List<List<String>> getTables() throws SQLException ;

    /**
     * Returns the columns in the specified schema/table.
     * @return a List of column names
     */
    public List<String> getColumns(String schema, String table) throws SQLException ;

    /****
     * Returns the primary key columns for the given schema/table combination.
     *
     * @return the List of columns
     */
    public List<String> getPrimaryKeys(String schema, String table) throws SQLException ;

    /***
     * Returns the imported keys for the given schema/table.
     * @return the List of imported Keys.  Each key is a List of the form
     * <br> {@literal <foreign schema, foreign table, foreign column, primary schema, primary table, primary column>}
     */
    public List<List<String>> getImportedKeys(String schema, String table) throws SQLException ;

    /***
     * Returns the exported keys for the given schema/table.
     * @return the List of exported keys.  Each key is a List of the form
     * <br> {@literal <foreign schema, foreign table, foreign column, primary schema, primary table, primary column>}
     */
    public List<List<String>> getExportedKeys(String schema, String table) throws SQLException ;

    /***
     * Returns the string used t quote SQL identifiers.
     * @return the quoting string or a space if quoting is not supported
     */
    public String getIdentifierQuoteString() throws SQLException ;
}
