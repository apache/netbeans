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

package org.netbeans.modules.web.core.palette.items;

import javax.swing.text.JTextComponent;


/**
 *
 * @author Libor Kotouc, Petr Slechta
 */
public class SQLStmt {

    public static final String[] scopes = new String[] { "page", "request", "session", "application" }; // NOI18N
    public static final int SCOPE_DEFAULT = 0;

    //user data
    private String variable;
    private int scopeIndex;
    private String dataSource;
    private String stmt;

    private String helpID;
    private boolean mayVariableNameBeEmpty;

    /**************************************************************************/
    public SQLStmt(String variable, int scopeIndex, String dataSource, String stmt,
            String helpID, boolean mayVariableNameBeEmpty)
    {
        this.variable = variable;
        this.scopeIndex = scopeIndex;
        this.dataSource = dataSource;
        this.stmt = stmt;
        this.helpID = helpID;
        this.mayVariableNameBeEmpty = mayVariableNameBeEmpty;
    }

    /**************************************************************************/
    public SQLStmt(String variable, int scopeIndex, String dataSource, String stmt, String helpID) {
        this(variable, scopeIndex, dataSource, stmt, helpID, true);
    }

    /**************************************************************************/
    public boolean customize(JTextComponent target, String displayName, String stmtLabel, String stmtACSN, String stmtACSD) {
        SQLStmtCustomizer c =
                new SQLStmtCustomizer(this, target, displayName, stmtLabel, stmtACSN, stmtACSD, helpID, mayVariableNameBeEmpty);
        return c.showDialog();
    }

    /**************************************************************************/
    public String getVariable() {
        return variable;
    }

    /**************************************************************************/
    public void setVariable(String variable) {
        this.variable = variable;
    }

    /**************************************************************************/
    public int getScopeIndex() {
        return scopeIndex;
    }

    /**************************************************************************/
    public void setScopeIndex(int scopeIndex) {
        this.scopeIndex = scopeIndex;
    }

    /**************************************************************************/
    public String getStmt() {
        return stmt;
    }

    /**************************************************************************/
    public void setStmt(String query) {
        this.stmt = query;
    }

    /**************************************************************************/
    public String getDataSource() {
        return dataSource;
    }

    /**************************************************************************/
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

}
