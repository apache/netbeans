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
package org.netbeans.lib.profiler.ui.jdbc;

import java.util.ResourceBundle;
import org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider;
import org.netbeans.lib.profiler.ui.results.DataView;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class JDBCView extends DataView {
    
    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.jdbc.Bundle"); // NOI18N
    
    static final String COLUMN_NAME = messages.getString("JDBCView_ColumnName"); // NOI18N
    static final String COLUMN_TOTALTIME = messages.getString("JDBCView_TimeColumnName"); // NOI18N
    static final String COLUMN_INVOCATIONS = messages.getString("JDBCView_InvocationsColumnName"); // NOI18N
    static final String COLUMN_COMMANDS = messages.getString("JDBCView_ColumnCommands"); // NOI18N
    static final String COLUMN_TABLES = messages.getString("JDBCView_ColumnTables"); // NOI18N
    static final String COLUMN_STATEMENTS = messages.getString("JDBCView_ColumnStatements"); // NOI18N
    static final String NAME_COLUMN_TOOLTIP = messages.getString("JDBCView_ColumnToolTip"); // NOI18N
    static final String TOTAL_TIME_COLUMN_TOOLTIP = messages.getString("JDBCView_TimeColumnToolTip"); // NOI18N
    static final String INVOCATIONS_COLUMN_TOOLTIP = messages.getString("JDBCView_InvocationsColumnToolTip"); // NOI18N
    static final String COMMANDS_COLUMN_TOOLTIP = messages.getString("JDBCView_ColumnCommandsToolTip"); // NOI18N
    static final String TABLES_COLUMN_TOOLTIP = messages.getString("JDBCView_ColumnTablesToolTip"); // NOI18N
    static final String STATEMENTS_COLUMN_TOOLTIP = messages.getString("JDBCView_ColumnStatementsToolTip"); // NOI18N
    
    static final String OTHER_COMMAND = messages.getString("JDBCView_OtherCommand"); // NOI18N
    
    static final String STATEMENT_REGULAR = messages.getString("JDBCView_RegularStatement"); // NOI18N
    static final String STATEMENT_PREPARED = messages.getString("JDBCView_PreparedStatement"); // NOI18N
    static final String STATEMENT_CALLABLE = messages.getString("JDBCView_CallableStatement"); // NOI18N
    
    static final String ACTION_VIEWSQLQUERY = messages.getString("JDBCView_ActionViewSqlQuery"); // NOI18N
    
    
    static final String EXPORT_TOOLTIP = messages.getString("JDBCView_ExportTooltip"); // NOI18N
    static final String EXPORT_LBL = messages.getString("JDBCView_ExportLbl"); // NOI18N
    static final String EXPORT_QUERIES = messages.getString("JDBCView_ExportQueries"); // NOI18N
    static final String ACTION_GOTOSOURCE = messages.getString("JDBCView_ActionGoToSource"); // NOI18N
    static final String ACTION_PROFILE_METHOD = messages.getString("JDBCView_ActionProfileMethod"); // NOI18N
    static final String ACTION_PROFILE_CLASS = messages.getString("JDBCView_ActionProfileClass"); // NOI18N
    static final String SEARCH_QUERIES_SCOPE = messages.getString("JDBCView_SearchQueriesScope"); // NOI18N
    static final String SEARCH_CALLERS_SCOPE = messages.getString("JDBCView_SearchCallersScope"); // NOI18N
    static final String SEARCH_SCOPE_TOOLTIP = messages.getString("JDBCView_SearchScopeTooltip"); // NOI18N
    
    static final String EXPAND_MENU = messages.getString("JDBCView_ExpandMenu"); // NOI18N
    static final String EXPAND_PLAIN_ITEM = messages.getString("JDBCView_ExpandPlainItem"); // NOI18N
    static final String EXPAND_TOPMOST_ITEM = messages.getString("JDBCView_ExpandTopmostItem"); // NOI18N
    static final String COLLAPSE_CHILDREN_ITEM = messages.getString("JDBCView_CollapseChildrenItem"); // NOI18N
    static final String COLLAPSE_ALL_ITEM = messages.getString("JDBCView_CollapseAllItem"); // NOI18N
    // -----
    
    static String commandString(int command) {
        switch (command) {
            case JdbcCCTProvider.SQL_COMMAND_ALTER: return "ALTER"; // NOI18N
            case JdbcCCTProvider.SQL_COMMAND_CREATE: return "CREATE"; // NOI18N
            case JdbcCCTProvider.SQL_COMMAND_DELETE: return "DELETE"; // NOI18N
            case JdbcCCTProvider.SQL_COMMAND_DESCRIBE: return "DESCRIBE"; // NOI18N
            case JdbcCCTProvider.SQL_COMMAND_INSERT: return "INSERT"; // NOI18N
            case JdbcCCTProvider.SQL_COMMAND_SELECT: return "SELECT"; // NOI18N
            case JdbcCCTProvider.SQL_COMMAND_SET: return "SET"; // NOI18N
            case JdbcCCTProvider.SQL_COMMAND_UPDATE: return "UPDATE"; // NOI18N
            case JdbcCCTProvider.SQL_COMMAND_BATCH: return "BATCH"; // NOI18N
            default: return OTHER_COMMAND;
        }
    }
    
}
