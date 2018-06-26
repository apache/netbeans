/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
