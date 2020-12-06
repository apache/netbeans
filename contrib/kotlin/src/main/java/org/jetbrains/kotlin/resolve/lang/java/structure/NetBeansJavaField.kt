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

import javax.lang.model.element.ElementKind
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaField
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.resolve.lang.java.*
import org.netbeans.api.project.Project
import javax.lang.model.element.VariableElement

/*

  @author Alexander.Baratynski
  Created on Aug 29, 2016
*/

class NetBeansJavaField(elementHandle: ElemHandle<VariableElement>, containingClass: JavaClass, project: Project) :
        NetBeansJavaMember<VariableElement>(elementHandle, containingClass, project), JavaField {

    override val isEnumEntry 
        get() = elementHandle.kind == ElementKind.ENUM_CONSTANT
    override val type: JavaType
        get() = elementHandle.getFieldType(project)

    override fun presentation() = "$type $name"
    
}