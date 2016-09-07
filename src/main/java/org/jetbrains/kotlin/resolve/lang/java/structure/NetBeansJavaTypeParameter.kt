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

import org.jetbrains.kotlin.load.java.structure.JavaAnnotation
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.lang.java.NBParameterUtils
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project

/*

  @author Alexander.Baratynski
  Created on Sep 7, 2016
*/

class NetBeansJavaTypeParameter(typeHandle : TypeMirrorHandle<*>, project : Project) : 
        NetBeansJavaClassifier(null, typeHandle, project), JavaTypeParameter {

    override val name : Name
        get() = NBParameterUtils.getNameOfTypeParameter(typeHandle, project)
    
    override val upperBounds : Collection<JavaClassifierType>
        get() = NBParameterUtils.getUpperBounds(typeHandle, project)
    
    override val annotations : Collection<JavaAnnotation>
        get() = emptyList()
    
    override fun findAnnotation(fqName : FqName) : JavaAnnotation? = null
    override fun toString() : String = name.asString()
    override fun hashCode() : Int = NBParameterUtils.hashCode(typeHandle, project)
    
    override fun equals(other : Any?) : Boolean {
        if (other !is NetBeansJavaTypeParameter) return false
        
        return NBParameterUtils.equals(typeHandle, other.typeHandle, project)
    }
    
}