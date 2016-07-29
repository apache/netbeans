package org.black.kotlin.highlighter.occurrences;

import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtReferenceExpression;

/**
 *
 * @author Alexander.Baratynski
 */
public class ReferenceFilter implements SearchFilter {

    @Override
    public boolean isApplicable(KtElement ktElement) {
        return ktElement instanceof KtReferenceExpression;
    }
    
}
