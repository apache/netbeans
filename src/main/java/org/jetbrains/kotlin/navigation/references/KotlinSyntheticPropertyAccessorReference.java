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
package org.jetbrains.kotlin.navigation.references;

import com.intellij.util.SmartList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.FunctionDescriptor;
import org.jetbrains.kotlin.psi.KtNameReferenceExpression;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.jetbrains.kotlin.resolve.BindingContext;
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
        
        List<FunctionDescriptor> result = new SmartList<>();
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
