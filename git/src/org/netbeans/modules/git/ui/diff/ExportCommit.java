/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
