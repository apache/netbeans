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
import java.util.List;
import java.util.Map;
import javax.swing.text.StyledDocument;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinRenameRefactoring extends ProgressProviderAdapter implements RefactoringPlugin {

    private final RenameRefactoring refactoring;
    
    public KotlinRenameRefactoring(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
        
    }

    @Override
    public Problem prepare(RefactoringElementsBag bag) {
        String newName = refactoring.getNewName();
        FileObject fo = ProjectUtils.getFileObjectForDocument(refactoring.getRefactoringSource().lookup(StyledDocument.class));
        PsiElement psi = refactoring.getRefactoringSource().lookup(PsiElement.class);
        
        Map<FileObject, List<OffsetRange>> renameMap = RenamePerformer.getRenameRefactoringMap(fo, psi, newName);
        
        bag.registerTransaction(RenamePerformer.getTransaction(renameMap, newName, psi.getText()));
        
        bag.getSession().doRefactoring(true);
        
        return null;
    }
    
}