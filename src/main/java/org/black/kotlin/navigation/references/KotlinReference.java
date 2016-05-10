package org.black.kotlin.navigation.references;

import java.util.Collection;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.resolve.BindingContext;

public interface KotlinReference {
    public KtReferenceExpression getReferenceExpression();
    public Collection<? extends DeclarationDescriptor> getTargetDescriptors(BindingContext context);
}
