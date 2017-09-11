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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.action;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.netbeans.modules.db.explorer.dlg.ConnectionDialogMediator;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

import org.netbeans.modules.db.explorer.DatabaseConnection;

import org.netbeans.modules.db.explorer.dlg.SchemaPanel;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.explorer.dlg.AddConnectionWizard;
import org.netbeans.modules.db.explorer.node.DriverNode;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

public class ConnectUsingDriverAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(ConnectUsingDriverAction.class.getName());

    @Override
    public String getName() {
        return NbBundle.getMessage (ConnectUsingDriverAction.class, "ConnectUsing"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ConnectUsingDriverAction.class);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return (activatedNodes != null && activatedNodes.length == 1);
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        Lookup lookup = activatedNodes[0].getLookup();
        DriverNode node = lookup.lookup(DriverNode.class);
        
        if (node != null) {
            JDBCDriver driver = node.getDatabaseDriver().getJDBCDriver();
            new NewConnectionDialogDisplayer().showDialog(driver, null, null, null);
        }
    }
    
    public static final class NewConnectionDialogDisplayer extends ConnectionDialogMediator {
        
        // the most recent task passed to the RequestProcessor
        Task activeTask = null;

        public DatabaseConnection showDialog(JDBCDriver driver, String databaseUrl, String user, String password) {
            return AddConnectionWizard.showWizard(driver, databaseUrl, user, password);
        }

        @Override
        protected Task retrieveSchemasAsync(final SchemaPanel schemaPanel, final DatabaseConnection dbcon, final String defaultSchema)
        {
            activeTask = super.retrieveSchemasAsync(schemaPanel, dbcon, defaultSchema);
            
            return activeTask;
        }
        
        @Override
        protected boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema) {
            fireConnectionStep(NbBundle.getMessage (ConnectUsingDriverAction.class, "ConnectionProgress_Schemas")); // NOI18N
            List<String> schemas = new ArrayList<String>();
            try {
                DatabaseMetaData dbMetaData = dbcon.getJDBCConnection().getMetaData();
                if (dbMetaData.supportsSchemasInTableDefinitions()) {
                    ResultSet rs = dbMetaData.getSchemas();
                    if (rs != null) {
                        while (rs.next()) {
                            schemas.add(rs.getString(1).trim());
                        }
                    }
                }
            } catch (SQLException exc) {
                String message = NbBundle.getMessage(ConnectUsingDriverAction.class, "ERR_UnableObtainSchemas", exc.getMessage()); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            }
            return schemaPanel.setSchemas(schemas, defaultSchema);
        }
    }
}
