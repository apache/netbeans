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

import javax.lang.model.element.TypeElement
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.FieldContainingClassSearcher
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.FieldTypeSearcher
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsAbstractSearcher
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsFinalSearcher
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsStaticSearcher
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.NameSearcher
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.VisibilitySearcher
import org.netbeans.api.project.Project
import javax.lang.model.element.VariableElement

fun ElemHandle<*>.isAbstract(project: Project) =
        IsAbstractSearcher(this).execute(project).isAbstract

fun ElemHandle<*>.isStatic(project: Project) =
        IsStaticSearcher(this).execute(project).isStatic

fun ElemHandle<*>.isFinal(project: Project) =
        IsFinalSearcher(this).execute(project).isFinal

fun ElemHandle<*>.getName(project: Project) =
        NameSearcher(this).execute(project).name

fun ElemHandle<*>.getVisibility(project: Project) = 
        VisibilitySearcher(this).execute(project).visibility

fun ElemHandle<VariableElement>.getFieldType(project: Project) =
        FieldTypeSearcher(this, project).execute(project).type

fun ElemHandle<VariableElement>.getContainingClass(project: Project) =
        FieldContainingClassSearcher(this, project).execute(project).containingClass