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

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument
import org.jetbrains.kotlin.load.java.structure.JavaArrayAnnotationArgument
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaAnnotation
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaAnnotationAsAnnotationArgument
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaArrayAnnotationArgument
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClassObjectAnnotationArgument
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaLiteralAnnotationArgument
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaReferenceAnnotationArgument
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.JavaSource.Phase
import org.netbeans.api.java.source.Task
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project

private fun getMirrorArguments(mirror: AnnotationMirror, 
                               info: CompilationController, project: Project) =
        mirror.elementValues.map{create(it.value.value, 
                Name.identifier(it.key.simpleName.toString()), info, project) }
        

private fun create(value: Any, name: Name, info: CompilationController, project: Project): JavaAnnotationArgument = 
        when(value) {
            is AnnotationMirror -> {
                val typeHandle = TypeMirrorHandle.create(value.annotationType)
                NetBeansJavaAnnotationAsAnnotationArgument(project, name, typeHandle,
                    getMirrorArguments(value, info, project))
            }
            is VariableElement -> NetBeansJavaReferenceAnnotationArgument(ElemHandle.create(value, project), project)
            is String -> NetBeansJavaLiteralAnnotationArgument(value, name)
            is Class<*> -> NetBeansJavaClassObjectAnnotationArgument(value, name, project)
            is Collection<*> -> getArrayAnnotationArgument(value, name, info, project)
            is AnnotationValue -> create(value.value, name, info, project)
            else -> throw UnsupportedOperationException()
        }

private fun getArrayAnnotationArgument(values: Collection<*>, name: Name,
                                       info: CompilationController, 
                                       project: Project): JavaArrayAnnotationArgument {
    val args = arrayListOf<JavaAnnotationArgument>()
    for (value in values) {
        if (value is Collection<*>) {
            args.add(getArrayAnnotationArgument(value, name, info, project))
        } else args.add(create(value!!, name, info, project))
    }
    return NetBeansJavaArrayAnnotationArgument(args, name)
}

class AnnotationsSearcher(val handle: ElemHandle<*>, val project: Project) : Task<CompilationController> {
    val annotations = arrayListOf<JavaAnnotation>()
    
    override fun run(info: CompilationController) {
        info.toPhase(Phase.RESOLVED)
        val element = handle.resolve(info) ?: return
        
        for (mirror in element.annotationMirrors) {
            val mirrorHandle = TypeMirrorHandle.create(mirror.annotationType)
            annotations.add(NetBeansJavaAnnotation(project, mirrorHandle,
                        getMirrorArguments(mirror, info, project)))
        }
    }
}

class AnnotationsForTypeMirrorHandleSearcher(val handle: TypeMirrorHandle<*>,
                                             val project: Project) : Task<CompilationController> {
    val annotations = arrayListOf<JavaAnnotation>()
    
    override fun run(info: CompilationController) {
        info.toPhase(Phase.RESOLVED)
        val element = handle.resolve(info) ?: return
        
        for (mirror in element.annotationMirrors) {
            val mirrorHandle = TypeMirrorHandle.create(mirror.annotationType)
            annotations.add(NetBeansJavaAnnotation(project, mirrorHandle,
                        getMirrorArguments(mirror, info, project)))
        }
    }
}

class AnnotationSearcher(val handle: ElemHandle<*>, val project: Project,
                         val fqName: FqName) : Task<CompilationController> {
    var annotation: JavaAnnotation? = null
    
    override fun run(info: CompilationController) {
        info.toPhase(Phase.RESOLVED)
        val element = handle.resolve(info) ?: return
        
        for (mirror in element.annotationMirrors) {
            val annotationFqName = mirror.annotationType.toString()
            if (fqName.asString() == annotationFqName) {
                val mirrorHandle = TypeMirrorHandle.create(mirror.annotationType)
                annotation = NetBeansJavaAnnotation(project, mirrorHandle, 
                        getMirrorArguments(mirror, info, project))
            }
        }
    }
}

class AnnotationForTypeMirrorHandleSearcher(val handle: TypeMirrorHandle<*>, val project: Project,
                         val fqName: FqName) : Task<CompilationController> {
    var annotation: JavaAnnotation? = null
    
    override fun run(info: CompilationController) {
        info.toPhase(Phase.RESOLVED)
        val element = handle.resolve(info) ?: return
        
        for (mirror in element.annotationMirrors) {
            val annotationFqName = mirror.annotationType.toString()
            if (fqName.asString() == annotationFqName) {
                val mirrorHandle = TypeMirrorHandle.create(mirror.annotationType)
                annotation = NetBeansJavaAnnotation(project, mirrorHandle, 
                        getMirrorArguments(mirror, info, project))
            }
        }
    }
}