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
package org.jetbrains.kotlin.resolve.lang.java.structure

import org.jetbrains.kotlin.load.java.structure.JavaElement
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.resolve.lang.java.NBElementUtils

/*

  @author Alexander.Baratynski
  Created on Aug 29, 2016
*/

abstract class NetBeansJavaElement(val elementHandle : ElementHandle<*>?,
                                   val typeHandle : TypeMirrorHandle<*>?,
                                   val project : Project) : JavaElement {
    
    constructor(elementHandle : ElementHandle<*>?, project : Project) : this(elementHandle, null, project)
    constructor(typeHandle : TypeMirrorHandle<*>?, project : Project) : this(null, typeHandle, project)
    
    override fun hashCode() : Int = if (elementHandle != null) elementHandle.hashCode() else NBElementUtils.typeMirrorHandleHashCode(typeHandle, project)
    
    override fun equals(other : Any?) : Boolean {
        if (other !is NetBeansJavaElement) return false
        
        if (elementHandle != null) {
            return elementHandle.equals(other.elementHandle)
        } else return NBElementUtils.typeMirrorHandleEquals(typeHandle, other.typeHandle, project)
    }
    
}