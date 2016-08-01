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

import javax.lang.model.element.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaElement;
/**
 *
 * @author Александр
 */
public abstract class NetBeansJavaElement<T extends Element> implements JavaElement {
    
    private final T binding;
    
    protected NetBeansJavaElement(@NotNull T binding){
        this.binding = binding;
    }
    
    @NotNull
    public T getBinding(){
        return binding;
    }
    
    @Override
    public int hashCode(){
        return getBinding().hashCode();
    }
    
    @Override
    public boolean equals(Object obj){
        return obj instanceof NetBeansJavaElement && getBinding().equals(((NetBeansJavaElement<?>)obj).getBinding());
    }
    
    
}
