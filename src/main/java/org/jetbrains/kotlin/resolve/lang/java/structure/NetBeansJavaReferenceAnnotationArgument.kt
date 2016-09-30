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

import org.jetbrains.kotlin.load.java.structure.JavaEnumValueAnnotationArgument
import org.jetbrains.kotlin.load.java.structure.JavaField
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.lang.java.NBElementUtils
import org.jetbrains.kotlin.resolve.lang.java.NBMemberUtils
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.resolve.lang.java.ElemHandle
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/*

  @author Alexander.Baratynski
  Created on Sep 7, 2016
*/

class NetBeansJavaReferenceAnnotationArgument(val handle : ElemHandle<VariableElement>, val project : Project) : 
        NetBeansJavaAnnotationArgument(FqName(NBElementUtils.getSimpleName(handle, project))), JavaEnumValueAnnotationArgument {
    override fun resolve() : JavaField {
        val containingClass = NBMemberUtils.getContainingClass(handle, project)
        return NetBeansJavaField(handle, NetBeansJavaClass(containingClass, project), project)
    }
}