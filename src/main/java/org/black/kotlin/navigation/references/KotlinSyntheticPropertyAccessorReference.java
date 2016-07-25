package org.black.kotlin.navigation.references;

import com.intellij.util.SmartList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.FunctionDescriptor;
import org.jetbrains.kotlin.psi.KtNameReferenceExpression;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedSimpleFunctionDescriptor;
import org.jetbrains.kotlin.synthetic.SyntheticJavaPropertyDescriptor;

public class KotlinSyntheticPropertyAccessorReference extends KotlinSimpleNameReference {

    private final boolean getter;
    
    public KotlinSyntheticPropertyAccessorReference(KtNameReferenceExpression expression, boolean getter){
        super(expression);
        this.getter = getter;
    }
    
    @Override
    public KtReferenceExpression getReferenceExpression() {
        return super.getReferenceExpression();
    }

    @Override
    public Collection<? extends DeclarationDescriptor> getTargetDescriptors(BindingContext context) {
        Collection<? extends DeclarationDescriptor> descriptors = 
                super.getTargetDescriptors(context);
        
        List<FunctionDescriptor> result = new SmartList<FunctionDescriptor>();
        for (DeclarationDescriptor descriptor : descriptors){
            if (descriptor instanceof SyntheticJavaPropertyDescriptor){
                if (getter){
                    result.add(((SyntheticJavaPropertyDescriptor) descriptor).getGetMethod());
                } else {
                    FunctionDescriptor setMethod = ((SyntheticJavaPropertyDescriptor) descriptor).getSetMethod();
                    if (setMethod != null){
                        result.add(setMethod);
                    }
                }
            } 
        }
        
        return result;
    }
    
    public static class Getter extends KotlinSyntheticPropertyAccessorReference {
        public Getter(KtNameReferenceExpression expression){
            super(expression, true);
        }
    }
    
    public static class Setter extends KotlinSyntheticPropertyAccessorReference {
        public Setter(KtNameReferenceExpression expression){
            super(expression, false);
        }
    }
}
