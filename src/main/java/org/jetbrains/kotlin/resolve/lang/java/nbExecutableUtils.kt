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

import javax.lang.model.element.ExecutableElement
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.project.Project

fun ElemHandle<ExecutableElement>.getReturnType(project: Project) =
        ReturnTypeSearcher(this, project).execute(project).returnType

fun ElemHandle<ExecutableElement>.hasAnnotationParameterDefaultValue(project: Project) = 
        HasAnnotationParameterDefaultValueSearcher(this).execute(project).hasAnnotationParameterDefaultValue

fun ElemHandle<ExecutableElement>.getTypeParameters(project: Project) =
        ExecutableTypeParametersSearcher(this, project).execute(project).typeParameters

fun ElemHandle<ExecutableElement>.getValueParameters(project: Project) =
        ValueParametersSearcher(this, project).execute(project).valueParameters

fun ElementHandle<ExecutableElement>.getElementHandleValueParameters(project: Project) =
        ElementHandleValueParametersSearcher(this, project).execute(project).valueParameters
