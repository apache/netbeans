package org.black.kotlin.navigation.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtReferenceExpression;

/**
 *
 * @author Александр
 */
public class ReferenceUtils {
    
    @Nullable
    public static KtReferenceExpression getReferenceExpression(PsiElement element) {
        return PsiTreeUtil.getNonStrictParentOfType(element, KtReferenceExpression.class);
    }
    
}
