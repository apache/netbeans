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
package org.jetbrains.kotlin.resolve.lang.java.resolver;

import javax.lang.model.element.Element;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.descriptors.SourceFile;
import org.jetbrains.kotlin.load.java.sources.JavaSourceElement;
import org.jetbrains.kotlin.load.java.structure.JavaElement;

/**
 *
 * @author Александр
 */
public class NetBeansJavaSourceElement implements JavaSourceElement {
    
    private final JavaElement javaElement;
    
    public NetBeansJavaSourceElement(JavaElement javaElement){
        this.javaElement = javaElement;
    }

    @Override
    @NotNull
    public JavaElement getJavaElement() {
        return javaElement;
    }

    @Override
    @NotNull
    public SourceFile getContainingFile() {
        return SourceFile.NO_SOURCE_FILE;
    }
    
    @NotNull
    public Element getElementBinding() {
        return ((NetBeansJavaElement<?>) javaElement).getBinding();
    }
    
}
