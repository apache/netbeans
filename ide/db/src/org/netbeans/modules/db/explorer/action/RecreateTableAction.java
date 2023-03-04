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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dlg.LabeledTextFieldDialog;
import org.netbeans.modules.db.explorer.node.SchemaNameProvider;
import org.netbeans.modules.db.explorer.node.TableListNode;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Rob Englander
 */
public class RecreateTableAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(RecreateTableAction.class.getName());

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;

        if (activatedNodes.length == 1) {
            TableNode tn = activatedNodes[0].getLookup().lookup(TableNode.class);

            DatabaseConnection dbconn = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);

            if (dbconn != null && (tn == null || (!tn.isSystem()))) {
                enabled = dbconn.isVitalConnection();
            }
        }

        return enabled;
    }

    @Override
    public void performAction (Node[] activatedNodes) {
        assert SwingUtilities.isEventDispatchThread();
      
        final BaseNode node = activatedNodes[0].getLookup().lookup(BaseNode.class);
        final DatabaseConnection connection = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);
        final DatabaseConnector connector = connection.getConnector();

        // Get filename
        FileChooserBuilder chooser = new FileChooserBuilder(RecreateTableAction.class);
        chooser.setTitle(NbBundle.getMessage(RecreateTableAction.class, "RecreateTableFileOpenDialogTitle")); //NOI18N
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return (f.isDirectory() || f.getName().endsWith(".grab")); //NOI18N
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(RecreateTableAction.class, "GrabTableFileTypeDescription"); //NOI18N
            }
        });

        final File file = chooser.showOpenDialog();
        
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    Specification spec = connector.getDatabaseSpecification();
                    AbstractCommand cmd;

                    if (file != null && file.isFile()) {
                        FileInputStream fstream = new FileInputStream(file);
                        try (ObjectInputStream istream = new ObjectInputStream(fstream)) {
                            cmd = (AbstractCommand)istream.readObject();
                        }
                        cmd.setSpecification(spec);
                    } else {
                        return;
                    }

                    SchemaNameProvider schemaProvider = node.getLookup().lookup(SchemaNameProvider.class);
                    String schemaName = schemaProvider.getSchemaName();
                    String catName = schemaProvider.getCatalogName();
                    if (schemaName == null) {
                        schemaName = catName;
                    }
                    
                    cmd.setObjectOwner(schemaName);

                    final String newtab = cmd.getObjectName();
                    final String msg = cmd.getCommand();
                    
                    final LabeledTextFieldDialog dlg = Mutex.EVENT.readAccess(
                            new Mutex.Action<LabeledTextFieldDialog>() {

                            @Override
                            public LabeledTextFieldDialog run() {
                                return new LabeledTextFieldDialog(msg);
                            }
                    });
                    
                    final String[] error = new String[1];
                    while (true) {
                        final boolean[] isEditable = new boolean[1];
                        final boolean[] okPressed = new boolean[1];
                        final String[] tableName = new String[] {""};
                        final String[] edittedCmd = new String[] {""};
                        
                        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {

                            @Override
                            public Void run() {
                                assert SwingUtilities.isEventDispatchThread();
                                dlg.setErrors(error[0]);
                                dlg.setStringValue(newtab);
                                okPressed[0] = dlg.run();
                                isEditable[0] = dlg.isEditable();
                                tableName[0] = dlg.getStringValue();
                                edittedCmd[0] = dlg.getEditedCommand();
                                return null;
                            }
                        });
                        
                        if (okPressed[0]) { // OK option
                            if (! isEditable[0]) {
                                error[0] = runCommand(tableName[0], cmd);
                            } else { // from editable text area
                                error[0] = runCommand(connection, edittedCmd[0]);
                            }
                            if (error[0] == null) {
                                break;
                            }
                        } else { // CANCEL option
                            break;
                        }
                    }
                } catch (IOException | ClassNotFoundException | DDLException exc) {
                    LOGGER.log(Level.INFO, exc.getLocalizedMessage(), exc);
                    DbUtilities.reportError(NbBundle.getMessage (RecreateTableAction.class, "ERR_UnableToRecreateTable"), exc.getMessage()); //NOI18N
                }

                // if there's a TableListNode in the parent chain, that's the one
                // we want to refresh, otherwise, refreshing this node will have to do
                Node refreshNode = node;
                while ( !(refreshNode instanceof TableListNode) ) {
                    refreshNode = refreshNode.getParentNode();
                    if (refreshNode == null) {
                        break;
                    }
                }

                if (refreshNode == null) {
                    refreshNode = node;
                }

                SystemAction.get(RefreshAction.class).performAction(new Node[] { refreshNode });
            }
        }, 0);
    }

    private String runCommand(final String newtab, final AbstractCommand cmd) {
        assert ! SwingUtilities.isEventDispatchThread();

        cmd.setObjectName(newtab);
        try {
            cmd.execute();
            return null;
        } catch (Exception exc) {
            LOGGER.log(Level.INFO, null, exc);
            return exc.getMessage();
        }
    }

    private String runCommand(DatabaseConnection connection, String command) {
        assert ! SwingUtilities.isEventDispatchThread();
        try {
            Connection con = connection.getJDBCConnection();
            try (Statement stat = con.createStatement()) {
                stat.execute(command);
                connection.notifyChange();
            }
            return null;
        } catch (Exception exc) {
            return NbBundle.getMessage(
                    RecreateTableAction.class, 
                    "DataViewFetchErrorPrefix", 
                    exc.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage (RecreateTableAction.class, "RecreateTable"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RecreateTableAction.class);
    }
}
