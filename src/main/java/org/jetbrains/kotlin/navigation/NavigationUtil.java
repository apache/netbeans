/*******************************************************************************
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
 *******************************************************************************/
package org.jetbrains.kotlin.navigation;

import com.intellij.psi.PsiElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import kotlin.Pair;
import org.jetbrains.kotlin.builder.KotlinPsiManager;
import org.jetbrains.kotlin.navigation.references.ReferenceUtilsKt;
import org.jetbrains.kotlin.utils.LineEndUtil;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.openide.filesystems.FileObject;

public class NavigationUtil {
    
    private static PsiElement psiExpression;
    
    @Nullable
    public static KtReferenceExpression getReferenceExpression(Document doc, int offset) throws BadLocationException{
        KtFile ktFile = KotlinParser.getFile();
        FileObject fo = ProjectUtils.getFileObjectForDocument(doc);
        if (ktFile == null || !ktFile.getName().equals(fo.getName())){
            ktFile = KotlinPsiManager.INSTANCE.parseText(doc.getText(0, doc.getLength()), fo);
        }
        if (ktFile == null) {
            return null;
        }
        int documentOffset = LineEndUtil.convertCrToDocumentOffset(ktFile.getText(), offset);
        psiExpression = ktFile.findElementAt(documentOffset);
        if (psiExpression == null){
            return null;
        }
        
        return ReferenceUtilsKt.getReferenceExpression(psiExpression);
    }
    
    @Nullable
    public static Pair<Integer, Integer> getSpan(){
        if (psiExpression == null){
            return null;
        }
        
        int start = psiExpression.getTextRange().getStartOffset();
        int end = psiExpression.getTextRange().getEndOffset();
        
        return new Pair<>(start, end);
    }
}
