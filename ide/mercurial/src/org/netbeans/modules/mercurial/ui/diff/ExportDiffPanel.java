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
package org.netbeans.modules.mercurial.ui.diff;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JPanel;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.RepositoryRevision;
import org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel;
import org.netbeans.modules.versioning.util.ExportDiffSupport.AbstractExportDiffPanel;


/**
 *
 * @author  Padraig O'Briain
 */
public class ExportDiffPanel extends ChangesetPickerPanel {

    private HgLogMessage              repoRev;
    private File fileToDiff;
    private final File repo;

    AbstractExportDiffPanel p;

    /** Creates new form ExportDiffPanel */
    public ExportDiffPanel(File repo, HgLogMessage repoRev, File [] roots, File fileToDiff) {
        super(repo, roots);
        this.fileToDiff = fileToDiff;
        this.repoRev = repoRev;
        this.repo = repo;
        initComponents();
    }

    public String getOutputFileName() {
        return p.getOutputFileText();
    }

    void setInsidePanel(AbstractExportDiffPanel insidePanel) {
        p = insidePanel;
        setDefaultOutputFile();
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        optionsPanel.add(p, BorderLayout.NORTH);
        setOptionsPanel(optionsPanel, new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        loadRevisions();
    }

    @Override
    protected HgLogMessage getDisplayedRevision() {
        return repoRev;
    }

    @Override
    protected String getRevisionLabel(RepositoryRevision repoRev) {
        String revStr = super.getRevisionLabel(repoRev);
        if (fileToDiff != null) {
            revStr = new StringBuilder(fileToDiff.getName()).append(" - ").append(revStr).toString(); //NOI18N
        }
        return revStr.toString();
    }

    @Override
    protected String getRefreshLabel() {
        return NbBundle.getMessage(ExportDiffPanel.class, "MSG_Fetching_Revisions"); //NOI18N
    }

    private void initComponents () {
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.jLabel1.text")); // NOI18N
        if(fileToDiff != null){
            org.openide.awt.Mnemonics.setLocalizedText(revisionsLabel, NbBundle.getMessage(ExportDiffPanel.class,
                    "ExportDiffPanel.revisionsLabel.text.forFileDiff")); // NOI18N
            jLabel2.setText(NbBundle.getMessage(ExportDiffPanel.class,
                    "ExportDiffPanel.exportHintLabel.text.forFileDiff")); // NOI18N
        } else {
            org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "LBL_EXPORT_INFO")); // NOI18N
        }
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }

    private void setDefaultOutputFile() {
        String folderName = HgModuleConfig.getDefault().getPreferences().get("ExportDiff.saveFolder", HgModuleConfig.getDefault().getExportFolder()); // NOI18N
        String fileName;
        if (fileToDiff != null && repoRev != null && repo != null) { //"<filename-ext>_%b_%r_%h"
            fileName = fileToDiff.getName().replace('.', '-') + "_" +  //NOI18N
                    repoRev.getRevisionNumber() + "_" +  //NOI18N
                    repoRev.getCSetShortID(); //NOI18N
        } else if (repoRev != null && repo != null) {
            fileName = HgModuleConfig.getDefault().getExportFilename().replace("%b", repo.getName()); //NOI18N
            fileName = fileName.replace("%r", repoRev.getRevisionNumber()); //NOI18N
            fileName = fileName.replace("%h", repoRev.getCSetShortID()); //NOI18N
        }else if (repo != null){
            fileName = HgModuleConfig.getDefault().getExportFilename().replace("%b", repo.getName()); //NOI18N
        }else{
            fileName = HgModuleConfig.getDefault().getExportFilename();            
        }
        File file = new File(folderName, fileName + ".patch");  //NOI18N
        p.setOutputFileText(file.getAbsolutePath());
    }
}
