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

import javax.lang.model.type.TypeKind
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationOwner
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.lang.java.*
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project

/*

  @author Alexander.Baratynski
  Created on Sep 7, 2016
*/

abstract class NetBeansJavaType(val handle: TypeMirrorHandle<*>,
                                val project: Project) : JavaType, JavaAnnotationOwner {

    companion object {
        @JvmStatic
        fun create(typeHandle: TypeMirrorHandle<*>, project: Project): NetBeansJavaType = when {
            typeHandle.kind.isPrimitive || typeHandle.kind == TypeKind.VOID -> NetBeansJavaPrimitiveType(typeHandle, project)
            typeHandle.kind == TypeKind.ARRAY -> NetBeansJavaArrayType(typeHandle, project)
            typeHandle.kind == TypeKind.DECLARED || typeHandle.kind == TypeKind.TYPEVAR -> NetBeansJavaClassifierType(typeHandle, project)
            typeHandle.kind == TypeKind.WILDCARD -> NetBeansJavaWildcardType(typeHandle, project)
            else -> throw UnsupportedOperationException("Unsupported NetBeans type ${typeHandle.getName(project)}")
        }
    }

    override val isDeprecatedInJavaDoc = false

    override val annotations: Collection<JavaAnnotation>
        get() = handle.getAnnotations(project)

    override fun findAnnotation(fqName: FqName): JavaAnnotation? = handle.getAnnotation(project, fqName)
    override fun hashCode(): Int = handle.getHashCode(project)
    override fun equals(other: Any?): Boolean = other is NetBeansJavaType && handle.isEqual(other.handle, project)
    override fun toString() = handle.getName(project)
    
}