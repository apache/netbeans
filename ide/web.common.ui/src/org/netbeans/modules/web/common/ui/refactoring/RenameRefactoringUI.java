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
package org.netbeans.modules.web.common.ui.refactoring;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author marekfukala
 */
public class RenameRefactoringUI implements RefactoringUI, RefactoringUIBypass {

    private static final RequestProcessor RP
            = new RequestProcessor(RenameRefactoringUI.class);
    private final AbstractRefactoring refactoring;
    private RenamePanel panel;
    private final FileObject file;
    private String newName;

    public RenameRefactoringUI(FileObject file) {
	this.file = file;
        this.newName = file.getName();
	this.refactoring = new RenameRefactoring(Lookups.fixed(file));
    }

    public RenameRefactoringUI(FileObject file, String newName) {
        this.file = file;
        this.newName = newName;
        this.refactoring = new RenameRefactoring(Lookups.fixed(file));
    }

    @Override
    public String getName() {
	return NbBundle.getMessage(RenameRefactoringUI.class, "LBL_Rename"); //NOI18N
    }

    @Override
    public String getDescription() {
	return NbBundle.getMessage(RenameRefactoringUI.class, "LBL_FolderRefactoring");
    }

    @Override
    public boolean isQuery() {
	return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
	if (panel == null) {
	    panel = new RenamePanel(newName, parent, NbBundle.getMessage(RenamePanel.class, "LBL_Rename")); //NOI18N
	}
	return panel;
    }

    @Override
    public Problem setParameters() {
	String newName = panel.getNameValue();
	if (refactoring instanceof RenameRefactoring) {
	    ((RenameRefactoring) refactoring).setNewName(newName);
	}
	return refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
	if (refactoring instanceof RenameRefactoring) {
	    ((RenameRefactoring) refactoring).setNewName(panel.getNameValue());
	}
	return refactoring.fastCheckParameters();
    }

    @Override
    public boolean hasParameters() {
	return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
	return this.refactoring;
    }

    @Override
    public HelpCtx getHelpCtx() {
	return null;
    }

    @Override
    public boolean isRefactoringBypassRequired() {
        return panel.isRenameWithoutRefactoring();
    }

    @Override
    public void doRefactoringBypass() throws IOException {
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    DataObject dob = DataObject.find(file);
                    if (dob != null) {
                        dob.rename(panel.getNameValue());
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
}
