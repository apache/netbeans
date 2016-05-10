package org.black.kotlin.navigation.references;

import java.util.Collection;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.psi.KtSimpleNameExpression;
import org.jetbrains.kotlin.resolve.BindingContext;

public class KotlinSimpleNameReference implements KotlinReference {

    private final KtSimpleNameExpression expression;
    
    public KotlinSimpleNameReference(KtSimpleNameExpression expression) {
        this.expression = expression;
    }
    
    @Override
    public KtReferenceExpression getReferenceExpression() {
        return expression;
    }

    @Override
    public Collection<? extends DeclarationDescriptor> getTargetDescriptors(BindingContext context) {
        return ReferenceUtils.getReferenceTargets(expression, context);
    }
    
}
