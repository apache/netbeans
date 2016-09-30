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

import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.BoundSearcher
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.ComponentTypeSearcher
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.IsExtendsSearcher
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.IsRawSearcher
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.TypeArgumentsSearcher
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.TypeNameSearcher
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project

fun TypeMirrorHandle<*>.getName(project: Project) =
        TypeNameSearcher(this).execute(project).name

fun TypeMirrorHandle<*>.getBound(project: Project) =
        BoundSearcher(this, project).execute(project).bound

fun TypeMirrorHandle<*>.isExtends(project: Project) =
        IsExtendsSearcher(this).execute(project).isExtends 

fun TypeMirrorHandle<*>.getComponentType(project: Project) =
        ComponentTypeSearcher(this, project).execute(project).componentType

fun TypeMirrorHandle<*>.isRaw(project: Project) =
        IsRawSearcher(this).execute(project).isRaw

fun TypeMirrorHandle<*>.getTypeArguments(project: Project) =
        TypeArgumentsSearcher(this, project).execute(project).typeArguments