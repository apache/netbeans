package org.black.kotlin.highlighter.occurrences;

import java.util.ArrayList;
import java.util.List;
import org.black.kotlin.navigation.references.ReferenceUtils;
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
