/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
