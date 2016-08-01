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
package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaWildcardType;

/**
 *
 * @author Александр
 */
public class NetBeansJavaWildcardType extends NetBeansJavaType<WildcardType> implements JavaWildcardType {
    
    public NetBeansJavaWildcardType(@NotNull WildcardType typeBinding){
        super(typeBinding);
    }

    @Override
    public JavaType getBound() {
        TypeMirror bound = getBinding().getSuperBound();
        return bound != null ? NetBeansJavaType.create(bound) : null;//temp
    }

    @Override
    public boolean isExtends() {
        return getBinding().getExtendsBound() != null;
    }
    
    
}
