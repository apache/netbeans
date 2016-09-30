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

import javax.lang.model.type.TypeKind
import org.jetbrains.kotlin.load.java.structure.JavaClassifier
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.resolve.lang.java.*

/*

  @author Alexander.Baratynski
  Created on Sep 7, 2016
*/

class NetBeansJavaClassifierType(handle : TypeMirrorHandle<*>, project : Project) : 
        NetBeansJavaType(handle, project), JavaClassifierType {

    override val presentableText : String = handle.getName(project)
    override val canonicalText : String = handle.getName(project)
    
    override val isRaw : Boolean
        get() = if (handle.kind == TypeKind.DECLARED) handle.isRaw(project) else false
    
    override val typeArguments : List<JavaType>
        get() = if (handle.kind == TypeKind.DECLARED) handle.getTypeArguments(project) else emptyList()
    
    override val classifier : JavaClassifier? = when (handle.kind) {
            TypeKind.DECLARED -> NBElementUtils.getNetBeansJavaClassFromType(handle, project)
            TypeKind.TYPEVAR -> NetBeansJavaTypeParameter(handle.toElemHandle(project), project)
            else -> null
        }
    
}