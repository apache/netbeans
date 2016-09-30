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

import javax.lang.model.element.TypeParameterElement
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.lang.java.ParameterSearchers.ElemHandleSearcher
import org.jetbrains.kotlin.resolve.lang.java.ParameterSearchers.Equals
import org.jetbrains.kotlin.resolve.lang.java.ParameterSearchers.TypeParameterHashCodeSearcher
import org.jetbrains.kotlin.resolve.lang.java.ParameterSearchers.TypeParameterNameSearcher
import org.jetbrains.kotlin.resolve.lang.java.ParameterSearchers.UpperBoundsSearcher
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project
import javax.lang.model.type.TypeVariable

fun ElemHandle<TypeParameterElement>.getName(project: Project) =
        TypeParameterNameSearcher(this).execute(project).name

fun TypeMirrorHandle<*>.toElemHandle(project: Project) = 
        ElemHandleSearcher(this, project).execute(project).elemHandle

fun ElemHandle<TypeParameterElement>.getUpperBounds(project: Project) = 
        UpperBoundsSearcher(this, project).execute(project).upperBounds

fun TypeMirrorHandle<TypeVariable>.getHashCode(project: Project) = 
        TypeParameterHashCodeSearcher(this).execute(project).hashCode

fun TypeMirrorHandle<TypeVariable>.isEqual(handle: TypeMirrorHandle<*>, project: Project) =
        Equals(this, handle).execute(project).equals()