/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.mercurial.remote.ui.diff;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.remotefs.versioning.api.ExportDiffSupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public abstract class ExportDiff extends ExportDiffSupport {

    private ExportDiffPanel panel;
    private VCSFileProxy fileToDiff;
    private AbstractExportDiffPanel aedp;
    private DocumentListener listener;
    private DialogDescriptor dd;
    
    
    /** Creates a new instance of ExportDiff */
    public ExportDiff(VCSFileProxy repository, HgLogMessage repoRev, VCSFileProxy [] roots, VCSFileProxy fileToDiff) {
        super(roots == null ? new VCSFileProxy[] {fileToDiff} : roots, HgModuleConfig.getDefault(repository).getPreferences());
        this.fileToDiff = fileToDiff;

        panel = new ExportDiffPanel(repository, repoRev, roots, fileToDiff);
    } 
    
    public ExportDiff(VCSFileProxy repository, HgLogMessage repoRev, VCSFileProxy [] roots) {
        this(repository, repoRev, roots, null);
    }
    
    public ExportDiff(VCSFileProxy repository, VCSFileProxy [] roots) {
        this(repository, null, roots, null);
    }

    private void nameChange() {
        if (aedp.getOutputFileText().trim().length() > 0) {
            dd.setValid(true);
        } else {
            dd.setValid(false);
        }
    }

    public String getOutputFileName() {
        if (panel == null) {
            return null;
        }
        return panel.getOutputFileName().trim();
    }

    public String getSelectionRevision() {
        if (panel == null) {
            return null;
        }
        return panel.getSelectedRevisionCSetId();
    }

    @Override
    protected AbstractExportDiffPanel createSimpleDialog(String currentFilePath) {
        aedp = new ExportAsFilePanel();
        listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { nameChange(); }
            @Override
            public void removeUpdate(DocumentEvent e) { nameChange(); }
            @Override
            public void changedUpdate(DocumentEvent e) { nameChange(); }
        };
        panel.setInsidePanel(aedp);

        if(fileToDiff != null){
            dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(ExportDiff.class,
                "CTL_ExportFileDialog")); // NOI18N
        } else{
            dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(ExportDiff.class,
                "CTL_ExportDialog")); // NOI18N
        }
        
        aedp.addOutputFileTextDocumentListener(listener);
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(this.getClass()));
        dd.setValid(false);

        return aedp;
    }

    @Override
    protected void createComplexDialog(AbstractExportDiffPanel insidePanel) {
        panel.setInsidePanel(insidePanel);
        if(fileToDiff != null){
            dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(ExportDiff.class,
                "CTL_ExportFileDialog")); // NOI18N
        } else{
            dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(ExportDiff.class,
                "CTL_ExportDialog")); // NOI18N
        }
    }

    @Override
    protected DialogDescriptor getDialogDescriptor() {
        return dd;
    }
}
