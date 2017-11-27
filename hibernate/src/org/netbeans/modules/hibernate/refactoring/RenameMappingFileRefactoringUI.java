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
package org.netbeans.modules.hibernate.refactoring;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * 
 */
final class RenameMappingFileRefactoringUI implements RefactoringUI, RefactoringUIBypass {
    private RenameRefactoring refactoring;
    private String originalName;
    private String oldName;
    private String dispOldName;
    private String newName;
    private RenameMappingFilePanel panel;
    private boolean newNameSpecified;
    private FileObject byPassFolder;

    RenameMappingFileRefactoringUI(FileObject jspFileObject, String newName) {
        refactoring = new RenameRefactoring(Lookups.singleton(jspFileObject));
        originalName = jspFileObject.getName();
        if (newName == null) {
            dispOldName = oldName = jspFileObject.getName();
        } else {
            dispOldName = oldName = newName;
            newNameSpecified = true;
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(RenameMappingFilePanel.class, "LBL_Rename");
    }
    
    public String getDescription() {
        return NbBundle.getMessage(RenameMappingFilePanel.class, "DSC_RenameMappingFile", dispOldName, newName);
    }

    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new RenameMappingFilePanel(
                            parent,
                            oldName,
                            NbBundle.getMessage(RenameMappingFilePanel.class, "DSC_RenameMappingFile", originalName),
                            !newNameSpecified);
        }
        return panel;
    }
    
    public boolean hasParameters() {
        return true;
    }

    public Problem setParameters() {
        newName = panel.getNameValue();
        refactoring.setNewName(newName);
        return refactoring.checkParameters();
    }
    
    public Problem checkParameters() {
        newName = panel.getNameValue();
        refactoring.setNewName(newName);
        return refactoring.fastCheckParameters();
    }
    
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    
    public void doRefactoringBypass() throws IOException {
        DataObject dob = null;
        if (byPassFolder != null) {
            dob = DataFolder.findFolder(byPassFolder);
        } else {
            dob = DataObject.find(refactoring.getRefactoringSource().lookup(FileObject.class));
        }
        dob.rename(panel.getNameValue());
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
