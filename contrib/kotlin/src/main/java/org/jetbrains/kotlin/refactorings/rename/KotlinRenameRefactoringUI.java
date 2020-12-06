/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.refactorings.rename;

import com.intellij.psi.PsiElement;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinRenameRefactoringUI implements RefactoringUI {

    private final String name = "Rename refactoring";
    private final RenameRefactoring refactoring;
    private final PsiElement psi;
    private String newName;
    private KotlinRenamePanel panel = null;
    
    public KotlinRenameRefactoringUI(PsiElement psi, RenameRefactoring refactoring) {
        this.psi = psi;
        this.refactoring = refactoring;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new KotlinRenamePanel(psi.getText(), parent);
        }
        return panel;
    }

    @Override
    public Problem setParameters() {
        if (panel != null) {
            newName = panel.getNameValue();
        } else newName = psi.getText();
        refactoring.setNewName(newName);
        return null;
    }

    @Override
    public Problem checkParameters() {
        refactoring.fastCheckParameters();
        return null;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}