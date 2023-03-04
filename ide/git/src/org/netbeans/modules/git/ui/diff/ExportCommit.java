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
package org.netbeans.modules.git.ui.diff;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.netbeans.modules.versioning.util.ExportDiffSupport;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author ondra
 */
abstract class ExportCommit extends ExportDiffSupport {

    private final ExportCommitPanel panel;
    private AbstractExportDiffPanel aedp;
    private DocumentListener listener;
    private DialogDescriptor dd;
    private final RevisionDialogController controller;
    
    
    /** Creates a new instance of ExportDiff */
    public ExportCommit (File repository, String preselectedRevision) {
        super(new File[] { repository }, GitModuleConfig.getDefault().getPreferences());
        controller = new RevisionDialogController(repository, new File[] { repository }, preselectedRevision);
        panel = new ExportCommitPanel(controller.getPanel());
    } 

    private void nameChange () {
        if (aedp.getOutputFileText().trim().length() > 0) {
            dd.setValid(true);
        } else {
            dd.setValid(false);
        }
    }

    public String getOutputFileName () {
        if (aedp == null) {
            return null;
        } else {
            return aedp.getOutputFileText().trim();
        }
    }

    public String getSelectionRevision() {
        return controller.getRevision().getRevision();
    }

    @Override
    protected AbstractExportDiffPanel createSimpleDialog (String currentFilePath) {
        aedp = new ExportAsFilePanel();
        listener = new DocumentListener() {
            @Override
            public void insertUpdate (DocumentEvent e) {
                nameChange();
            }

            @Override
            public void removeUpdate (DocumentEvent e) {
                nameChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                nameChange();
            }
        };
        setInsidePanel(aedp);

        dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(ExportCommit.class, "CTL_ExportDialog")); // NOI18N
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(this.getClass()));
        dd.setValid(false);
        aedp.addOutputFileTextDocumentListener(listener);
        return aedp;
    }

    @Override
    protected void createComplexDialog (AbstractExportDiffPanel insidePanel) {
        setInsidePanel(insidePanel);
        aedp = insidePanel;
        dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(ExportCommit.class, "CTL_ExportDialog")); // NOI18N
    }

    @Override
    protected DialogDescriptor getDialogDescriptor () {
        return dd;
    }

    private void setInsidePanel (AbstractExportDiffPanel aedp) {
        panel.insidePanel.removeAll();
        panel.insidePanel.setLayout(new BorderLayout());
        panel.insidePanel.add(aedp, BorderLayout.CENTER);
    }
}
