package org.black.kotlin.highlighter.occurrences;

import org.jetbrains.kotlin.psi.KtElement;

/**
 *
 * @author Alexander.Baratynski
 */
public interface SearchFilter {

    public boolean isApplicable(KtElement ktElement);
    
}
