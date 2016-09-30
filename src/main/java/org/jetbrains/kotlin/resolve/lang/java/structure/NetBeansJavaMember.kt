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

import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaMember
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.lang.java.NBAnnotationUtils
import org.jetbrains.kotlin.resolve.lang.java.NBMemberUtils
import org.jetbrains.kotlin.resolve.lang.java.NBElementUtils
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.resolve.lang.java.ElemHandle

/*

  @author Alexander.Baratynski
  Created on Aug 29, 2016
*/

abstract class NetBeansJavaMember(elementHandle : ElemHandle<*>, override val containingClass : JavaClass, project : Project) : 
        NetBeansJavaElement(elementHandle, project), JavaMember {
    
    override val annotations : Collection<JavaAnnotation>
        get() = NBAnnotationUtils.getAnnotations(elementHandle, project)

    override val visibility : Visibility 
        get() = NBMemberUtils.getVisibility(elementHandle, project)
    
    override val name : Name
        get() = NBMemberUtils.getName(elementHandle, project)
    
    override val isDeprecatedInJavaDoc : Boolean = NBElementUtils.isDeprecated(elementHandle, project)
    override val isAbstract : Boolean = NBMemberUtils.isAbstract(elementHandle, project)
    override val isStatic : Boolean = NBMemberUtils.isStatic(elementHandle, project)
    override val isFinal : Boolean = NBMemberUtils.isFinal(elementHandle, project)
    
    override fun findAnnotation(fqName : FqName) : JavaAnnotation? = NBAnnotationUtils.getAnnotation(elementHandle, project, fqName)
    
}