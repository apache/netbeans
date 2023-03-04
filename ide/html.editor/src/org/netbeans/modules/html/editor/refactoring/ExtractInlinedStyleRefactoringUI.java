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
package org.netbeans.modules.html.editor.refactoring;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.html.editor.refactoring.api.ExtractInlinedStyleRefactoring;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author marekfukala
 */
public class ExtractInlinedStyleRefactoringUI implements RefactoringUI, RefactoringUIBypass {

    private final ExtractInlinedStyleRefactoring refactoring;
    private ExtractInlinedStylePanel panel;
    private final RefactoringContext context;

    public ExtractInlinedStyleRefactoringUI(RefactoringContext context) {
	this.context = context;
	this.refactoring = new ExtractInlinedStyleRefactoring(Lookups.fixed(context));
    }

    @Override
    public String getName() {
	return NbBundle.getMessage(ExtractInlinedStyleRefactoringUI.class, "LBL_ExtractInlinedStyle"); //NOI18N
    }

    @Override
    public String getDescription() {
	return "TODO";
    }

    @Override
    public boolean isQuery() {
	return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
	if (panel == null) {
	    panel = new ExtractInlinedStylePanel(context);
	}
	return panel;
    }

    @Override
    public Problem setParameters() {

        refactoring.setMode(panel.getSelectedMode());
        refactoring.setSelectorType(panel.getSelectorType());
        
        switch(panel.getSelectedMode()) {
            case refactorToExistingEmbeddedSection:
                refactoring.setExistingEmbeddedCssSection(panel.getSelectedEmbeddedSection());
                break;
            case refactorToNewEmbeddedSection:
                break;
            case refactorToReferedExternalSheet:
            case refactorToExistingExternalSheet:
                refactoring.setExternalSheet(panel.getSelectedExternalStyleSheet());
                break;
            default:
                break;
        }

        return null;
    }

    @Override
    public Problem checkParameters() {
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
	return new HelpCtx("ExtractInlinedStyleRefactoringUI"); //NOI18N
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
