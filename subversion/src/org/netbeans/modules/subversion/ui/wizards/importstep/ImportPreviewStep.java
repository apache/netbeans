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
package org.netbeans.modules.subversion.ui.wizards.importstep;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.swing.JComponent;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.PanelProgressSupport;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.commit.CommitAction;
import org.netbeans.modules.subversion.ui.commit.CommitOptions;
import org.netbeans.modules.subversion.ui.commit.CommitTable;
import org.netbeans.modules.subversion.ui.commit.CommitTableModel;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.TableSorter;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Tomas Stupka
 */
public class ImportPreviewStep extends AbstractStep {
    
    private PreviewPanel previewPanel;
    private Context context;
    private CommitTable table;
    private PanelProgressSupport support;
    private String importMessage;
    private static final String PANEL_PREFIX = "import"; //NOI18N
    
    public ImportPreviewStep(Context context) {
        this.context = context;
    }
    
    public HelpCtx getHelp() {    
        return new HelpCtx(ImportPreviewStep.class);
    }    

    protected JComponent createComponent() {
        if (previewPanel == null) {
            previewPanel = new PreviewPanel();

            Map<String, Integer> sortingStatus = SvnModuleConfig.getDefault().getSortingStatus(PANEL_PREFIX);
            if (sortingStatus == null) {
                sortingStatus = Collections.singletonMap(CommitTableModel.COLUMN_NAME_PATH, TableSorter.ASCENDING);
            }
            table = new CommitTable(previewPanel.tableLabel, CommitTable.IMPORT_COLUMNS, sortingStatus);
            
            JComponent component = table.getComponent();
            previewPanel.tablePanel.setLayout(new BorderLayout());
            previewPanel.tablePanel.add(component, BorderLayout.CENTER);
        }
        return previewPanel;              
    }

    protected void validateBeforeNext() {
        validateUserInput();
    }       

    public void validateUserInput() {
        Collection<CommitOptions> commitOptions = table.getCommitFiles().values();
        if(table != null && commitOptions.size() > 0) {
            for (CommitOptions option : commitOptions) {
                if(option != CommitOptions.EXCLUDE) {
                    valid();
                    return;
                }
            }
            invalid(null); // NOI18N 
        } else {
            invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(ImportPreviewStep.class, "CTL_Import_NothingToImport"), true)); // NOI18N
        }        
    }    

    /**
     * Prepares files for commit and optionally starts the commit itself
     * @param repositoryPath
     * @param rootLocalPath
     * @param repository
     * @param importMessage 
     * @param startCommitWhenFinished if true then commit task will be started upon this task's finish and progress will be displayed in the progress bar
     */
    public void setup(final String repositoryPath, final String rootLocalPath, final SVNUrl repository, String importMessage, final boolean startCommitWhenFinished) {
        this.importMessage = importMessage;
        support = new PanelProgressSupport(startCommitWhenFinished ? null : previewPanel.progressPanel) {
            @Override
            protected void perform() {
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                final File[] files = cache.listFiles(context, FileInformation.STATUS_LOCAL_CHANGE);

                if (files.length == 0 || isCanceled()) {
                    return;
                }

                if (repositoryPath != null) {
                    table.setRootFile(repositoryPath, rootLocalPath);
                }

                final ArrayList<SvnFileNode> nodesList = new ArrayList<SvnFileNode>(files.length);
                SvnUtils.runWithInfoCache(new Runnable() {
                    @Override
                    public void run () {
                        for (File file : files) {
                            SvnFileNode node = new SvnFileNode(file);
                            node.initializeProperties();
                            nodesList.add(node);
                            if (isCanceled()) {
                                return;
                            }
                        }
                    }
                });
                final SvnFileNode[] nodes = nodesList.toArray(new SvnFileNode[files.length]);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        table.setNodes(nodes);
                        table.getTableModel().addTableModelListener(new TableModelListener() {
                            public void tableChanged(TableModelEvent e) {
                                validateUserInput();
                            }
                        });
                        validateUserInput();
                        if (startCommitWhenFinished) {
                            startCommitTask(repository);
                        }
                    }
                });
            }
        };
        support.start(Subversion.getInstance().getRequestProcessor(repository), repository,  org.openide.util.NbBundle.getMessage(ImportPreviewStep.class, "BK1009")); //NOI18N
    }

    public void stop() {
        if(support != null) {
            support.cancel();
        }
    }

    public Map<SvnFileNode, CommitOptions> getCommitFiles() {
        return table.getCommitFiles();
    }
    
    public void storeTableSorter() {
        SvnModuleConfig.getDefault().setSortingStatus(PANEL_PREFIX, table.getSortingState());
    }

    /**
     * Starts commit task
     * @param repository repository url
     */
    public void startCommitTask (SVNUrl repository) {
        SvnProgressSupport commitTask = new SvnProgressSupport() {
            public void perform() {
                CommitAction.performCommit(importMessage, getCommitFiles(), context, this, true);
            }
        };
        commitTask.start(Subversion.getInstance().getRequestProcessor(repository), repository, org.openide.util.NbBundle.getMessage(ImportPreviewStep.class, "LBL_Import_Progress")); //NOI18N
    }
}

