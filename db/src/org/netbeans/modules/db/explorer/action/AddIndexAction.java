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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.action;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dlg.AddIndexDialog;
import org.netbeans.modules.db.explorer.node.IndexListNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Rob Englander
 */
public class AddIndexAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(AddIndexAction.class.getName());

    @Override
    public String getName() {
        return NbBundle.getMessage (AddIndexAction.class, "AddIndex"); // NOI18N
    }


    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;

        if (activatedNodes.length == 1) {
            IndexListNode node = activatedNodes[0].getLookup().lookup(IndexListNode.class);
            enabled = node != null;
        }

        return enabled;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        final IndexListNode node = activatedNodes[0].getLookup().lookup(IndexListNode.class);
        RequestProcessor.getDefault().post(
            new Runnable() {
            @Override
                public void run() {
                    perform(node);
                }
            }
        );
    }

    private void perform(final IndexListNode node) {
        try {
            DatabaseConnection dbConn = node.getLookup().lookup(DatabaseConnection.class);
            DatabaseConnector connector = dbConn.getConnector();

            final String tablename = node.getTableName();
            String schemaName = node.getSchemaName();
            String catalogName = node.getCatalogName();

            if (schemaName == null) {
                schemaName = catalogName;
            }

            if (catalogName == null) {
                catalogName = schemaName;
            }

            Specification spec = connector.getDatabaseSpecification();

            final DriverSpecification drvSpec = connector.getDriverSpecification(catalogName);

            // List columns not present in current index
            List<String> cols = new ArrayList<String> (5);

            drvSpec.getColumns(tablename, "%");
            ResultSet rs = drvSpec.getResultSet();
            Map<Integer, String> rset = new HashMap<Integer, String>();
            while (rs.next()) {
                rset = drvSpec.getRow();
                cols.add(rset.get(new Integer(4)));
                rset.clear();
            }
            rs.close();

            if (cols.isEmpty())
                throw new Exception(NbBundle.getMessage (AddIndexAction.class, "EXC_NoUsableColumnInPlace")); // NOI18N

            // Create and execute command
            final AddIndexDialog dlg = new AddIndexDialog(cols, spec, tablename, schemaName);
            dlg.setIndexName(tablename + "_idx"); // NOI18N
            if (dlg.run()) {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        Node refreshNode = node.getParentNode();
                        if (refreshNode == null) {
                            refreshNode = node;
                        }

                        SystemAction.get(RefreshAction.class).performAction(new Node[] { refreshNode } );
                    }
                });
            }
        } catch(Exception exc) {
            LOGGER.log(Level.INFO, exc.getLocalizedMessage(), exc);
            DbUtilities.reportError(NbBundle.getMessage (AddIndexAction.class, "ERR_UnableToAddIndex"), exc.getMessage()); // NOI18N
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AddIndexAction.class);
    }
}
