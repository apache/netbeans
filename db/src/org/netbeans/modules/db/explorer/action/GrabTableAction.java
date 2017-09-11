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
