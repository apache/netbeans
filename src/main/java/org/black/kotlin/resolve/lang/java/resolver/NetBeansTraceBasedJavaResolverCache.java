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
package org.black.kotlin.resolve.lang.java.resolver;

import javax.inject.Inject;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor;
import org.jetbrains.kotlin.descriptors.PropertyDescriptor;
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor;
import org.jetbrains.kotlin.load.java.components.JavaResolverCache;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaElement;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.BindingTrace;

/**
 *
 * @author Александр
 */
public class NetBeansTraceBasedJavaResolverCache implements JavaResolverCache {

    private BindingTrace trace;
    
    @Inject
    public void setTrace(BindingTrace trace){
        this.trace = trace;
    }
    
    @Override
    public ClassDescriptor getClassResolvedFromSource(FqName fqName) {
        return trace.get(BindingContext.FQNAME_TO_CLASS_DESCRIPTOR, fqName.toUnsafe());
    }

    @Override
    public void recordMethod(JavaMethod jm, SimpleFunctionDescriptor sfd) {
    }

    @Override
    public void recordConstructor(JavaElement je, ConstructorDescriptor cd) {
    }

    @Override
    public void recordField(JavaField jf, PropertyDescriptor pd) {
    }

    @Override
    public void recordClass(JavaClass jc, ClassDescriptor cd) {
    }
    
}
