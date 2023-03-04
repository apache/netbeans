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

package org.netbeans.modules.db.explorer.action;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.node.ProcedureNode;
import org.netbeans.modules.db.explorer.sql.editor.SQLEditorSupport;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * @author Jiri Rechtacek
 */
public class EditSourceCodeAction extends NodeAction {
    
    @Override
    public String getName() {
        return NbBundle.getMessage(EditSourceCodeAction.class, "LBL_EditSourceCodeAction_Name"); // NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }
    
    @Override
    protected void performAction(final Node[] activatedNodes) {
        final DatabaseConnection connection = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);
        if (connection != null) {
            RequestProcessor.getDefault().post(
                    new Runnable() {
                        @Override
                        public void run() {
                            ProcedureNode pn = activatedNodes[0].getLookup().lookup(ProcedureNode.class);
                            try {
                                SQLEditorSupport.openSQLEditor(connection.getDatabaseConnection(), pn.getDDL(), false);
                            } catch (Exception exc) {
                                Logger.getLogger(EditSourceCodeAction.class.getName()).log(Level.INFO, exc.getLocalizedMessage() + " while executing expression " + pn.getDDL(), exc); // NOI18N
                            }
                        }
                    });
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean isPN = activatedNodes.length == 1
                && activatedNodes[0].getLookup().lookup(ProcedureNode.class) != null;
        if (isPN) {
            ProcedureNode pn = activatedNodes[0].getLookup().lookup(ProcedureNode.class);
            return pn.isEditSourceSupported();
        } else {
            return isPN;
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(EditSourceCodeAction.class);
    }
}
