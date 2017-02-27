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

import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaConstructor
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.resolve.lang.java.*
import javax.lang.model.element.ExecutableElement

/*

  @author Alexander.Baratynski
  Created on Aug 29, 2016
*/

class NetBeansJavaConstructor(elementHandle: ElemHandle<ExecutableElement>, containingClass: JavaClass, project: Project) :
        NetBeansJavaMember<ExecutableElement>(elementHandle, containingClass, project), JavaConstructor {

    override val valueParameters: List<JavaValueParameter>
        get() = elementHandle.getValueParameters(project)

    override val typeParameters: List<JavaTypeParameter>
        get() = elementHandle.getTypeParameters(project)

}