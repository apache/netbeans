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
package org.netbeans.modules.css.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssRenameRefactoringUI implements RefactoringUI, RefactoringUIBypass {

    private final AbstractRefactoring refactoring;
    private CssElementContext context;
    private RenamePanel panel;
    private CssRefactoringExtraInfo extraInfo;

    public CssRenameRefactoringUI(CssElementContext context) {
	this.context = context;
        this.extraInfo = new CssRefactoringExtraInfo();
        Collection<Object> lookupContent = new ArrayList<>();
        lookupContent.add(context);
        lookupContent.add(extraInfo);
        if(context instanceof CssElementContext.File ||
                context instanceof CssElementContext.Folder) {
            //put the fileobject to the lookup only if we are renaming a file or folder
            lookupContent.add(context.getFileObject());
        }
	this.refactoring = new RenameRefactoring(Lookups.fixed(lookupContent.toArray()));
    }

    @Override
    public String getName() {
	return NbBundle.getMessage(CssRenameRefactoringUI.class, "LBL_Rename"); //NOI18N
    }

    @Override
    public String getDescription() {
	return NbBundle.getMessage(CssRenameRefactoringUI.class, "LBL_Rename_Descr"); //NOI18N
    }

    @Override
    public boolean isQuery() {
	return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
	if (panel == null) {
	    panel = new RenamePanel(context.getElementName(), parent, NbBundle.getMessage(RenamePanel.class, "LBL_Rename"), true, true); //NOI18N
	}

	return panel;
    }

    @Override
    public Problem setParameters() {
        extraInfo.setRefactorAll(panel.isRefactorAllOccurances());

	String newName = panel.getNameValue();
	if (refactoring instanceof RenameRefactoring) {
	    ((RenameRefactoring) refactoring).setNewName(newName);
	}
	return refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
	if (!panel.isUpdateReferences()) {
	    return null;
	}
        if (refactoring instanceof RenameRefactoring) {
	    ((RenameRefactoring) refactoring).setNewName(panel.getNameValue());
	}
	return refactoring.checkParameters();
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
	return false; //TODO fix this
    }

    @Override
    public void doRefactoringBypass() throws IOException {
	//TODO implement
    }
}
