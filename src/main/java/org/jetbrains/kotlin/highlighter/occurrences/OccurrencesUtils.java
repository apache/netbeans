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
package org.jetbrains.kotlin.highlighter.occurrences;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.ArrayList;
import java.util.List;
import kotlin.Pair;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.navigation.references.ReferenceUtils;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtNamedDeclaration;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author Alexander.Baratynski
 */
public class OccurrencesUtils {

    private static Pair<Integer, Integer> getLengthOfIdentifier(KtElement ktElement) {
        if (ktElement instanceof KtNamedDeclaration) {
            KtNamedDeclaration declaration = ((KtNamedDeclaration) ktElement);
            return new Pair<>(declaration.getNameIdentifier().getTextRange().getStartOffset(),
                    declaration.getNameIdentifier().getTextRange().getEndOffset());
        } else if (ktElement instanceof KtReferenceExpression) {
            return new Pair<>(ktElement.getTextRange().getStartOffset(), ktElement.getTextRange().getEndOffset());
        } else {
            return null;
        }
    }

    public static List<OffsetRange> search(List<? extends SourceElement> searchingElements, KtFile ktFile) {
        List<OffsetRange> offsets = Lists.newArrayList();
        List<KtElement> searchElements = getKotlinElements(searchingElements);
        if (searchElements.isEmpty()) {
            return offsets;
        }

        KtElement searchElement = searchElements.get(0);
        List<KtElement> occurrences = searchTextOccurrences(ktFile, searchElement);
        for (KtElement ktElement : occurrences) {
            Pair<Integer, Integer> range = getLengthOfIdentifier(ktElement);
            if (range != null) {
                offsets.add(new OffsetRange(range.getFirst(), range.getSecond()));
            }
        }

        return offsets;
    }

    private static List<KtElement> getKotlinElements(List<? extends SourceElement> sourceElements) {
        List<KtElement> kotlinElements = Lists.newArrayList();

        for (SourceElement sourceElement : sourceElements) {
            if (sourceElement instanceof KotlinSourceElement) {
                kotlinElements.add(((KotlinSourceElement) sourceElement).getPsi());
            }
        }

        return kotlinElements;
    }

    public static List<KtElement> searchTextOccurrences(KtFile ktFile, KtElement sourceElement) {
        List<KtElement> elements = new ArrayList<>();
        List<KtElement> elementsToReturn = new ArrayList<>();

        String elementName = sourceElement.getName();
        if (elementName == null) {
            return elementsToReturn;
        }

        for (PsiElement psi : getAllOccurrencesInFile(ktFile, elementName)) {
            KtElement el = PsiTreeUtil.getNonStrictParentOfType(psi, KtElement.class);
            if (el != null) {
                elements.add(el);
            }
        }

        List<SearchFilter> beforeResolveFilters = SearchFiltersKt.getBeforeResolveFilters();
        List<? extends SearchFilterAfterResolve> afterResolveFilters = SearchFiltersKt.getAfterResolveFilters();

        for (KtElement element : elements) {
            boolean beforeResolveCheck = true;
            for (SearchFilter filter : beforeResolveFilters) {
                if (!filter.isApplicable(element)) {
                    beforeResolveCheck = false;
                    break;
                }
            }
            if (!beforeResolveCheck) {
                continue;
            }

            List<? extends SourceElement> sourceElements = ReferenceUtils.resolveToSourceDeclaration(element);
            if (sourceElements.isEmpty()) {
                continue;
            }

            for (SearchFilterAfterResolve filter : afterResolveFilters) {
                if (filter.isApplicable(sourceElements, sourceElement)) {
                    elementsToReturn.add(element);
                }
            }

        }

        return elementsToReturn;
    }

    private static List<PsiElement> getAllOccurrencesInFile(KtFile ktFile, String text) {
        List<PsiElement> elements = Lists.newArrayList();

        String source = ktFile.getText();
        int start = 0;

        while (true) {
            int index = source.indexOf(text, start);
            if (index == -1) {
                break;
            }
            elements.add(ktFile.findElementAt(index));
            start = index + text.length();
        }

        return elements;
    }

    public static List<? extends SourceElement> getSearchingElements(List<? extends SourceElement> sourceElements) {
        List<SourceElement> classOrObjects = ReferenceUtils.getContainingClassOrObjectForConstructor(sourceElements);
        return classOrObjects.isEmpty() ? sourceElements : classOrObjects;
    }

}