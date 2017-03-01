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
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType
import org.jetbrains.kotlin.load.java.structure.JavaConstructor
import org.jetbrains.kotlin.load.java.structure.JavaField
import org.jetbrains.kotlin.load.java.structure.JavaMethod
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.resolve.lang.java.*
import javax.lang.model.element.TypeElement

/*

  @author Alexander.Baratynski
  Created on Aug 29, 2016
*/

class NetBeansJavaClass(elementHandle: ElemHandle<TypeElement>, project: Project) :
        NetBeansJavaClassifier<TypeElement>(elementHandle, project), JavaClass {

    override val name = elementHandle.getName(project)

    override val fqName: FqName?
        get() = elementHandle.getFqName()

    override val supertypes: Collection<JavaClassifierType>
        get() = elementHandle.getSuperTypes(project)

    override val innerClasses: Collection<JavaClass>
        get() = elementHandle.getInnerClasses(project)

    override val outerClass: JavaClass?
        get() = elementHandle.getOuterClass(project)

    override val methods: Collection<JavaMethod>
        get() = elementHandle.getMethods(project, this)

    override val constructors: Collection<JavaConstructor>
        get() = elementHandle.getConstructors(project, this)

    override val fields: Collection<JavaField>
        get() = elementHandle.getFields(project, this)

    override val visibility: Visibility
        get() = elementHandle.getVisibility(project)

    override val typeParameters: List<JavaTypeParameter>
        get() = elementHandle.getTypeParameters(project)

    override val isInterface: Boolean = elementHandle.kind == ElementKind.INTERFACE
    override val isAnnotationType: Boolean = elementHandle.kind == ElementKind.ANNOTATION_TYPE
    override val isEnum: Boolean = elementHandle.kind == ElementKind.ENUM
    override val isAbstract: Boolean = elementHandle.isAbstract(project)
    override val isStatic: Boolean = elementHandle.isStatic(project)
    override val isFinal: Boolean = elementHandle.isFinal(project)

    override val lightClassOriginKind = null

    override fun toString(): String = elementHandle.qualifiedName

    fun presentation(): String {
        val visibility = visibility.displayName
        val final = if (isFinal) " final" else ""
        val cl = if (isInterface) " inteface" else if (isEnum) " enum" else " class"
        
        return "$visibility$final$cl ${elementHandle.qualifiedName}"
    }
    
}