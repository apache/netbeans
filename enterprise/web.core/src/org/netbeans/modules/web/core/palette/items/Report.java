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

import java.util.ResourceBundle;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JspPaletteUtilities;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.NbBundle;


/**
 *
 * @author Libor Kotouc
 */
public class Report implements ActiveEditorDrop {

    public static String QUERY_DEFAULT = "SELECT column_name(s) FROM table_name"; // NOI18N
    private static final String VARIABLE_DEFAULT = "result"; // NOI18N
    SQLStmt stmt = null;
    private String variable = VARIABLE_DEFAULT;
    private int scopeIndex = SQLStmt.SCOPE_DEFAULT;
    private String dataSource = "";  //NOI18N
    private String query = QUERY_DEFAULT;
    private String displayName;
    private String stmtLabel = "";  //NOI18N
    private String stmtACSN = "";  //NOI18N
    private String stmtACSD = "";  //NOI18N

    public Report() {
        try {
            displayName = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.resources.Bundle").getString("NAME_jsp-Report"); // NOI18N
        } catch (Exception e) {
        }

        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.Bundle"); // NOI18N
        try {
            stmtLabel = bundle.getString("LBL_Report_Stmt"); // NOI18N
        } catch (Exception e) {
        }
        try {
            stmtACSN = bundle.getString("ACSN_Report_Stmt"); // NOI18N
        } catch (Exception e) {
        }
        try {
            stmtACSD = bundle.getString("ACSD_Report_Stmt"); // NOI18N
        } catch (Exception e) {
        }

        stmt = new SQLStmt(variable, scopeIndex, dataSource, query, "ReportStmtCustomizer", false); // NOI18N
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        boolean accept = stmt.customize(targetComponent, displayName, stmtLabel, stmtACSN, stmtACSD);
        if (accept) {
            String prefix = JspPaletteUtilities.findSqlPrefix(targetComponent);
            String core = JspPaletteUtilities.findJstlPrefix(targetComponent);
            String body = createBody(prefix, core);
            try {
                JspPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        return accept;
    }

    private String createBody(String prefix, String core) {
        variable = stmt.getVariable();
        dataSource = stmt.getDataSource();

        if (variable.equals("")) {// NOI18N
            variable = JspPaletteUtilities.CARET;
        } else if (dataSource.equals("")) {// NOI18N
            dataSource = JspPaletteUtilities.CARET;
        }
        String strVariable = " var=\"\""; // NOI18N
        if (variable.length() > 0) {
            strVariable = " var=\"" + variable + "\""; // NOI18N
        }
        scopeIndex = stmt.getScopeIndex();
        String strScope = "";  //NOI18N
        if (scopeIndex != SQLStmt.SCOPE_DEFAULT) {
            strScope = " scope=\"" + SQLStmt.scopes[scopeIndex] + "\""; // NOI18N
        }
        String strDS = " dataSource=\"\""; // NOI18N
        if (strDS.length() > 0) {
            strDS = " dataSource=\"" + dataSource + "\""; // NOI18N
        }
        query = stmt.getStmt();
        String strQuery = query;
        if (query.length() > 0) {
            strQuery += "\n"; // NOI18N
        }
        
        return "<"+prefix+":query" + strVariable + strScope + strDS + ">\n" + // NOI18N
                strQuery + "</"+prefix+":query>\n\n" + "<table border=\"1\">\n" + // NOI18N
                "<!-- column headers -->\n" + "<tr>\n" + // NOI18N
                "<"+core+":forEach var=\"columnName\" items=\"${" + variable + ".columnNames}\">\n" + // NOI18N
                "<th><"+core+":out value=\"${columnName}\"/></th>\n" + "</"+core+":forEach>\n" + "</tr>\n" + // NOI18N
                "<!-- column data -->\n" + // NOI18N
                "<"+core+":forEach var=\"row\" items=\"${" + variable + ".rowsByIndex}\">\n" + // NOI18N
                "<tr>\n" + "<"+core+":forEach var=\"column\" items=\"${row}\">\n" + // NOI18N
                "<td><"+core+":out value=\"${column}\"/></td>\n" + "</"+core+":forEach>\n" + "</tr>\n" + // NOI18N
                "</"+core+":forEach>\n" + "</table>"; // NOI18N
    }
}
