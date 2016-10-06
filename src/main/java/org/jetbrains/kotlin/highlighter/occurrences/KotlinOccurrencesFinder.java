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
package org.jetbrains.kotlin.highlighter.occurrences;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult;
import org.jetbrains.kotlin.navigation.references.ReferenceUtils;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author Александр
 */
public class KotlinOccurrencesFinder extends OccurrencesFinder<KotlinParserResult> {

    private int caretPosition = 0;
    boolean cancel = false;
    Map<OffsetRange, ColoringAttributes> highlighting = new HashMap<OffsetRange, ColoringAttributes>();
    
    @Override
    public void setCaretPosition(int position) {
        caretPosition = position;
    }

    @Override
    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return highlighting;
    }
    
    private void findOccurrences(KtElement ktElement, KtFile ktFile) {
        List<? extends SourceElement> sourceElements = ReferenceUtils.resolveToSourceDeclaration(ktElement);
        if (sourceElements.isEmpty()) {
            return;
        }
        
        List<? extends SourceElement> searchingElements = OccurrencesUtils.getSearchingElements(sourceElements);
        List<OffsetRange> ranges = OccurrencesUtils.search(searchingElements, ktFile);
        for (OffsetRange range : ranges) {
            highlighting.put(range, ColoringAttributes.MARK_OCCURRENCES);
        }
    }
    
    @Override
    public void run(KotlinParserResult result, SchedulerEvent event) {
        cancel = false;
        highlighting.clear();
        if (result == null) {
            return;
        }
        KtFile ktFile = result.getKtFile();
        if (ktFile == null) {
            return;
        }
        
        PsiElement psiElement = ktFile.findElementAt(caretPosition);
        KtElement ktElement = PsiTreeUtil.getNonStrictParentOfType(psiElement, KtElement.class);
        if (ktElement == null) {
            return;
        }
        findOccurrences(ktElement, ktFile);
    }
    
    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancel = true;
    }
    
}