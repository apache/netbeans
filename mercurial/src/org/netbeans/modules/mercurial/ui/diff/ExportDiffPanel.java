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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
