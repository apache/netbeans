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
package org.jetbrains.kotlin.resolve.lang.java.structure;

import static org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.typeParameters;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaConstructor;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;

public class NetBeansJavaConstructor extends NetBeansJavaMember<ExecutableElement> implements JavaConstructor {

    public NetBeansJavaConstructor(@NotNull ExecutableElement methodBinding){
        super(methodBinding);
    }
    
    @Override
    public JavaClass getContainingClass() {
        return new NetBeansJavaClass((TypeElement) getBinding().getEnclosingElement());
    }

    @Override
    public List<JavaValueParameter> getValueParameters() {
        return NetBeansJavaElementUtil.getValueParameters(getBinding());
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        List<? extends TypeParameterElement> valueParameters = getBinding().getTypeParameters();
        return typeParameters(valueParameters.toArray(new TypeParameterElement[valueParameters.size()]));
    }
    
}
