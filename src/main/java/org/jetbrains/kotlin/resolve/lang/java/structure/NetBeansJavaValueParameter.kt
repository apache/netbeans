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
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.lang.java.*
import org.netbeans.api.project.Project
import javax.lang.model.element.VariableElement

/*

  @author Alexander.Baratynski
  Created on Sep 7, 2016
*/

class NetBeansJavaValueParameter(elementHandle: ElemHandle<VariableElement>, project: Project, name: String, isVararg: Boolean) :
        NetBeansJavaElement<VariableElement>(elementHandle, project), JavaValueParameter {

    override val name = Name.identifier(name)
    override val isVararg = isVararg
    override val isDeprecatedInJavaDoc = false

    override val type: JavaType
        get() = NetBeansJavaType.create(elementHandle.typeMirrorHandle, project)

    override val annotations: Collection<JavaAnnotation>
        get() = emptyList()

    override fun findAnnotation(fqName: FqName) = null

}