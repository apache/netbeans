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
import org.jetbrains.kotlin.resolve.lang.java.NBClassUtils
import org.jetbrains.kotlin.resolve.lang.java.NBMemberUtils
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.project.Project

/*

  @author Alexander.Baratynski
  Created on Aug 29, 2016
*/

class NetBeansJavaClass(elementHandle : ElementHandle<*>?, project : Project) : 
        NetBeansJavaClassifier(elementHandle, null, project), JavaClass {
    
    override val name : Name 
        get() = NBClassUtils.getName(elementHandle, project)
    
    override val fqName : FqName 
        get() = NBClassUtils.getFqName(elementHandle)//FqName(elementHandle!!.qualifiedName)
    
    override val supertypes : Collection<JavaClassifierType>
        get() = NBClassUtils.getSuperTypes(elementHandle, project)
    
    override val innerClasses : Collection<JavaClass> 
        get() = NBClassUtils.getInnerClasses(elementHandle, project)
    
    override val outerClass : JavaClass?
        get() = NBClassUtils.getOuterClass(elementHandle, project)
    
    override val methods : Collection<JavaMethod>
        get() = NBClassUtils.getMethods(elementHandle, project, this)
    
    override val constructors : Collection<JavaConstructor>
        get() = NBClassUtils.getConstructors(elementHandle, project, this)
    
    override val fields : Collection<JavaField>
        get() = NBClassUtils.getFields(elementHandle, project, this)
    
    override val visibility : Visibility
        get() = NBMemberUtils.getVisibility(elementHandle, project)
    
    override val typeParameters : List<JavaTypeParameter>
        get() = NBClassUtils.getTypeParameters(elementHandle, project)
    
    override val isInterface : Boolean = elementHandle!!.kind == ElementKind.INTERFACE
    override val isAnnotationType : Boolean = elementHandle!!.kind == ElementKind.ANNOTATION_TYPE
    override val isEnum : Boolean = elementHandle!!.kind == ElementKind.ENUM
    override val isKotlinLightClass : Boolean = false
    override val isAbstract : Boolean = NBMemberUtils.isAbstract(elementHandle, project)
    override val isStatic : Boolean = NBMemberUtils.isStatic(elementHandle, project) 
    override val isFinal : Boolean = NBMemberUtils.isFinal(elementHandle, project)
    
    override fun toString() : String = elementHandle!!.qualifiedName
    
}