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
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;
import org.jetbrains.kotlin.builder.KotlinPsiManager;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author Alexander.Baratynski
 */
@ServiceProvider(service = ActionsImplementationProvider.class, position = 400)
public class KotlinActionsImplementationProvider extends ActionsImplementationProvider {
    
    @Override
    public boolean canRename(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (ec == null) {
            return false;
        }
        StyledDocument doc = ec.getDocument();
        if (doc == null) {
            return false;
        }
        FileObject fo = ProjectUtils.getFileObjectForDocument(doc);
        if (fo == null) {
            return false;
        }
        
        return fo.hasExt("kt");
    }
    
    @Override
    public void doRename(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        JEditorPane pane = ec.getOpenedPanes()[0];
        int caretPosition = pane.getCaretPosition();
        StyledDocument doc = ec.getDocument();
        FileObject fo = ProjectUtils.getFileObjectForDocument(doc);
        final KtFile ktFile = KotlinPsiManager.INSTANCE.getParsedFile(fo);
        final PsiElement psi = ktFile.findElementAt(caretPosition);
        UI.openRefactoringUI(new KotlinRenameRefactoringUI(psi, new RenameRefactoring(Lookups.fixed(psi, doc))),
                TopComponent.getRegistry().getActivated());  
        
    }
    
}