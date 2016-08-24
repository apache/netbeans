/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.resolve.lang.java.structure2;

import org.jetbrains.kotlin.load.java.structure.JavaElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaElement implements JavaElement {
    
    private final ElementHandle elementHandle;
    private final TypeMirrorHandle typeHandle;
    
    public NetBeansJavaElement(ElementHandle elementHandle, TypeMirrorHandle typeHandle) {
        this.elementHandle = elementHandle;
        this.typeHandle = typeHandle;
    }
    
    public NetBeansJavaElement(ElementHandle elementHandle) {
        this(elementHandle, null);
    }
    
    public NetBeansJavaElement(TypeMirrorHandle typeHandle) {
        this(null, typeHandle);
    }
    
    public ElementHandle getElementHandle() {
        return elementHandle;
    }
    
    public TypeMirrorHandle getTypeHandle() {
        return typeHandle;
    }
    
    @Override
    public int hashCode() {
        if (elementHandle != null) {
            return elementHandle.hashCode();
        } else return typeHandle.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NetBeansJavaElement)) {
            return false;
        }
        
        if (elementHandle != null) {
            return elementHandle.equals(((NetBeansJavaElement) obj).getElementHandle());
        } else return typeHandle.equals(((NetBeansJavaElement) obj).getTypeHandle());
    } 
    
}
