package org.black.kotlin.highlighter.occurrences;

import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtSimpleNameExpression;
import org.jetbrains.kotlin.psi.psiUtil.KtPsiUtilKt;

/**
 *
 * @author Alexander.Baratynski
 */
public class NonImportFilter implements SearchFilter {

    @Override
    public boolean isApplicable(KtElement ktElement) {
        boolean isSimpleNameExpression = ktElement instanceof KtSimpleNameExpression;
        
        return isSimpleNameExpression == false || !KtPsiUtilKt.isImportDirectiveExpression((KtSimpleNameExpression) ktElement);
    }
    
}
