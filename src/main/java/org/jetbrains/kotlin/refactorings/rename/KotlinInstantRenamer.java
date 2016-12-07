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

import com.google.common.collect.Sets;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.kotlin.highlighter.occurrences.OccurrencesUtilsKt;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinInstantRenamer implements InstantRenamer {

    @Override
    public boolean isRenameAllowed(ParserResult info, int caretOffset, String[] explanationRetValue) {
        FileObject fo = info.getSnapshot().getSource().getFileObject();
        KtFile ktFile = ProjectUtils.getKtFile(fo);
        if (ktFile == null) {
            return false;
        }
        
        PsiElement psiElement = ktFile.findElementAt(caretOffset);
        return psiElement != null;
    }

    @Override
    public Set<OffsetRange> getRenameRegions(ParserResult info, int caretOffset) {
        Set<OffsetRange> ranges = new HashSet<>();
        FileObject fo = info.getSnapshot().getSource().getFileObject();
        KtFile ktFile = ProjectUtils.getKtFile(fo);
        if (ktFile == null) {
            return Sets.newHashSet();
        }
        
        PsiElement psiElement = ktFile.findElementAt(caretOffset);
        KtElement ktElement = PsiTreeUtil.getNonStrictParentOfType(psiElement, KtElement.class);
        if (ktElement == null) {
            return Sets.newHashSet();
        }
        
        List<KtElement> occurrences = OccurrencesUtilsKt.searchTextOccurrences(ktFile, ktElement);
        for (KtElement element : occurrences) {
            OffsetRange range = new OffsetRange(element.getTextRange().getStartOffset(), 
                    element.getTextRange().getEndOffset());
            ranges.add(range);
        }
        
        return ranges;
    }
    
}