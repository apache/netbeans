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
package org.jetbrains.kotlin.resolve.lang.java

import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.name.FqName
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.project.Project
import javax.lang.model.element.TypeElement


fun ElemHandle<TypeElement>.getName(project: Project) = NameSearcher(this).execute(project).name

fun ElemHandle<TypeElement>.getSuperTypes(project: Project) =
        SuperTypesSearcher(this, project).execute(project).superTypes

fun ElemHandle<TypeElement>.getInnerClasses(project: Project) =
        InnerClassesSearcher(this, project).execute(project).innerClasses

fun ElemHandle<TypeElement>.getOuterClass(project: Project) = 
        OuterClassSearcher(this, project).execute(project).outerClass

fun ElemHandle<TypeElement>.getMethods(project: Project, javaClass: JavaClass) = 
        MethodsSearcher(this, project, javaClass).execute(project).methods

fun ElemHandle<TypeElement>.getConstructors(project: Project, javaClass: JavaClass) =
        ConstructorsSearcher(this, project, javaClass).execute(project).constructors

fun ElemHandle<TypeElement>.getFields(project: Project, javaClass: JavaClass) =
        FieldsSearcher(this, project, javaClass).execute(project).fields

fun ElemHandle<TypeElement>.getTypeParameters(project: Project) = 
        TypeParametersSearcher(this, project).execute(project).typeParameters

fun ElemHandle<TypeElement>.getFqName() = FqName(this.qualifiedName)
 
fun ElementHandle<TypeElement>.getMethodsHandles(project: Project) =
        MethodHandlesSearcher(this).execute(project).methods