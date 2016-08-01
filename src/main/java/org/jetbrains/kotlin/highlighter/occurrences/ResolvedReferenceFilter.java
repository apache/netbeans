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

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.kotlin.navigation.references.ReferenceUtils;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement;

/**
 *
 * @author Alexander.Baratynski
 */
public class ResolvedReferenceFilter implements SearchFilterAfterResolve {

    @Override
    public boolean isApplicable(KtElement sourceElement, KtElement originElement) {
        boolean equals = sourceElement == originElement;
        
        if (!equals) {
            for (KtElement ktElement : getKotlinElements(
                    ReferenceUtils.resolveToSourceDeclaration(originElement))) {
                if (sourceElement == ktElement) {
                    equals = true;
                    break;
                }
            }
        }
        
        return equals;
    }

    @Override
    public boolean isApplicable(List<? extends SourceElement> sourceElements, KtElement originElement) {
        
        for (KtElement element : getKotlinElements(sourceElements)) {
            if (isApplicable(element, originElement)) {
                return true;
            }
        }
        return false;
    }
    
    public List<KtElement> getKotlinElements(List<? extends SourceElement> sourceElements) {
        List<KtElement> elements = new ArrayList<KtElement>();
        
        for (SourceElement element : sourceElements) {
            if (element instanceof KotlinSourceElement) {
                elements.add(((KotlinSourceElement) element).getPsi());
            }
        }
        
        return elements;
    }
        
    
}
