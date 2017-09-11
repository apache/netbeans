/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.action;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.sql.editor.SQLEditorSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Open an sql editor as an sql console.
 * 
 * @author Rob Englander
 */
@ActionID(id = "org.netbeans.modules.db.explorer.action.ExecuteCommandAction", category = "Database")
@ActionRegistration(displayName = "#ExecuteCommand", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Databases/Explorer/Connection/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/TableList/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/SystemTableList/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/Table/Actions", position = 125),
    @ActionReference(path = "Databases/Explorer/ViewList/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/View/Actions", position = 200),
    @ActionReference(path = "Databases/Explorer/ProcedureList/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/Procedure/Actions", position = 100),
    @ActionReference(path = "Databases/Explorer/ProcedureParam/Actions", position = 100),
    @ActionReference(path = "Databases/Explorer/IndexList/Actions", position = 300),
    @ActionReference(path = "Databases/Explorer/Index/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/ForeignKeyList/Actions", position = 100),
    @ActionReference(path = "Databases/Explorer/ForeignKey/Actions", position = 100),
    @ActionReference(path = "Databases/Explorer/Column/Actions", position = 200),
    @ActionReference(path = "Databases/Explorer/IndexColumn/Actions", position = 100),
    @ActionReference(path = "Databases/Explorer/ForeignKeyColumn/Actions", position = 100)})
public class ExecuteCommandAction extends BaseAction {

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        // If exactly one DatabaseConnection can be detected via the activated
        // nodes, that database connection is set as the default connection
        Set<DatabaseConnection> dbconnSet = new HashSet<>();
        if (activatedNodes != null) {
            for (Node node : activatedNodes) {
                DatabaseConnection dbconn = node.getLookup().lookup(DatabaseConnection.class);
                if (dbconn != null) {
                    dbconnSet.add(dbconn);
                }
            }
        }
        if (dbconnSet.size() == 1) {
            DatabaseConnection dbconn = dbconnSet.iterator().next();
            SQLEditorSupport.openSQLEditor(dbconn.getDatabaseConnection(), "", false); // NOI18N
        } else {
            SQLEditorSupport.openSQLEditor(null, "", false); // NOI18N
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ExecuteCommandAction.class, "ExecuteCommand"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ExecuteCommandAction.class);
    }
}
