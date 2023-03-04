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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.node.TableListNode;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Rob Englander
 */
public class GrabTableAction extends BaseAction {

    @Override
    public String getName() {
        return NbBundle.getMessage (GrabTableAction.class, "GrabStructure"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(GrabTableAction.class);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;

        if (activatedNodes.length == 1) {
            TableNode tn = activatedNodes[0].getLookup().lookup(TableNode.class);
            if (tn != null && (!tn.isSystem())) {
                enabled = true;
            }
        }

        return enabled;
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        final TableNode node = activatedNodes[0].getLookup().lookup(TableNode.class);

        try {
            final Specification spec = node.getLookup().lookup(DatabaseConnection.class).getConnector().getDatabaseSpecification();
            String tablename = node.getName();

            // Get filename
            FileChooserBuilder chooserBuilder = new FileChooserBuilder(RecreateTableAction.class);
            chooserBuilder.setTitle(NbBundle.getMessage (GrabTableAction.class, "GrabTableFileSaveDialogTitle")); //NOI18N
            chooserBuilder.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                  return (f.isDirectory() || f.getName().endsWith(".grab")); //NOI18N
                }

                @Override
                public String getDescription() {
                  return NbBundle.getMessage (GrabTableAction.class, "GrabTableFileTypeDescription"); //NOI18N
                }
            });
            JFileChooser chooser = chooserBuilder.createFileChooser();
            chooser.setSelectedFile(new File(tablename+".grab")); //NOI18N

            java.awt.Component par = WindowManager.getDefault().getMainWindow();
            boolean noResult = true;
            File file = null;
            while(noResult) {
                if (chooser.showSaveDialog(par) == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    if (file != null) {
                        if(file.exists()) {
                            Object yesOption = new JButton(NbBundle.getMessage (GrabTableAction.class, "Yes")); // NOI18N
                            Object noOption = new JButton (NbBundle.getMessage (GrabTableAction.class, "No")); // NOI18N
                            Object result = DialogDisplayer.getDefault ().notify (new NotifyDescriptor
                                            (NbBundle.getMessage (GrabTableAction.class, "MSG_ReplaceFileOrNot", // NOI18N
                                                file.getName()), //question
                                             NbBundle.getMessage (GrabTableAction.class, "GrabTableFileSaveDialogTitle"), // title
                                             NotifyDescriptor.YES_NO_OPTION, // optionType
                                             NotifyDescriptor.QUESTION_MESSAGE, // messageType

                                             new Object[] { yesOption, noOption }, // options
                                             yesOption // initialValue
                                            ));
                            if (result.equals(yesOption)) {
                                // the file can be replaced
                                noResult = false;
                            }
                        } else noResult = false;
                    }
                } else return;
            }

            final File theFile = file;
            RequestProcessor.getDefault().post(
                new Runnable() {
                @Override
                    public void run() {
                        try {
                            new GrabTableHelper().execute(node.getLookup().lookup(DatabaseConnection.class),
                                spec, node.getTableHandle(), theFile);
                        } catch (Exception exc) {
                            Logger.getLogger(GrabTableAction.class.getName()).log(Level.INFO, exc.getLocalizedMessage(), exc);
                            DbUtilities.reportError(NbBundle.getMessage (GrabTableAction.class, "ERR_UnableToGrabTable"), exc.getMessage()); // NOI18N
                        }
                    }
                }
            );

        } catch(Exception exc) {
            Logger.getLogger(GrabTableAction.class.getName()).log(Level.INFO, exc.getLocalizedMessage(), exc);
            DbUtilities.reportError(NbBundle.getMessage (GrabTableAction.class, "ERR_UnableToGrabTable"), exc.getMessage()); // NOI18N
        }
    }

}
