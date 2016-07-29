package org.black.kotlin.highlighter.occurrences;

import java.util.List;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.psi.KtElement;

/**
 *
 * @author Alexander.Baratynski
 */
public class ResolvedReferenceFilter implements SearchFilterAfterResolve {

    @Override
    public boolean isApplicable(KtElement sourceElement, KtElement originElement) {
        return sourceElement == originElement;
    }

    @Override
    public boolean isApplicable(List<? extends SourceElement> sourceElements, KtElement originElement) {
        return false;
    }
    
}
