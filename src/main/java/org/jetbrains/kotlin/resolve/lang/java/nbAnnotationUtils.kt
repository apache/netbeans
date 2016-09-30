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

import org.jetbrains.kotlin.load.java.structure.JavaAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.lang.java.AnnotationSearchers.AnnotationForTypeMirrorHandleSearcher
import org.jetbrains.kotlin.resolve.lang.java.AnnotationSearchers.AnnotationSearcher
import org.jetbrains.kotlin.resolve.lang.java.AnnotationSearchers.AnnotationsForTypeMirrorHandleSearcher
import org.jetbrains.kotlin.resolve.lang.java.AnnotationSearchers.AnnotationsSearcher
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project

fun ElemHandle<*>.getAnnotations(project: Project) = 
        AnnotationsSearcher(this, project).execute(project).annotations

fun ElemHandle<*>.getAnnotation(project: Project, fqName: FqName) =
        AnnotationSearcher(this, project, fqName).execute(project).annotation

fun TypeMirrorHandle<*>.getAnnotations(project: Project) =
        AnnotationsForTypeMirrorHandleSearcher(this, project).execute(project).annotations

fun TypeMirrorHandle<*>.getAnnotation(project: Project, fqName: FqName) =
        AnnotationForTypeMirrorHandleSearcher(this, project, fqName).execute(project).annotation