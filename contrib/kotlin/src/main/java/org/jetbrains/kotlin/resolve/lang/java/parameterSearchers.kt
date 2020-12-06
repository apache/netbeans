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
import javax.lang.model.type.TypeVariable
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClassifierType
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.Task
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project

class TypeParameterNameSearcher(val handle: ElemHandle<*>) : Task<CompilationController> {

    lateinit var name: Name

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info) ?: throw UnsupportedOperationException("Couldn't resolve '$handle")
        name = SpecialNames.safeIdentifier(elem.simpleName.toString())
    }
}

class TypeParameterHashCodeSearcher(val handle: TypeMirrorHandle<*>) : Task<CompilationController> {

    var hashCode = handle.hashCode()

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val type = handle.resolve(info) ?: return
        hashCode = (type as TypeVariable).asElement().hashCode()
    }
}

class TypeMirrorHandleHashCodeSearcher(val handle: TypeMirrorHandle<*>) : Task<CompilationController> {

    var hashCode = handle.hashCode()

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val type = handle.resolve(info) ?: return
        hashCode = type.hashCode()
    }
}

class ElemHandleSearcher(private val typeHandle: TypeMirrorHandle<*>,
                         val project: Project) : Task<CompilationController> {

    lateinit var elemHandle: ElemHandle<TypeParameterElement>

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val mirror = typeHandle.resolve(info) as? TypeVariable ?: throw UnsupportedOperationException("Cpuldn't resolve ${typeHandle}'")
        elemHandle = ElemHandle.create(mirror.asElement() as TypeParameterElement, project)
    }
}

class UpperBoundsSearcher(val handle: ElemHandle<*>, val project: Project) : Task<CompilationController> {

    val upperBounds = arrayListOf<JavaClassifierType>()

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info) ?: return
        val type = (elem as TypeParameterElement).asType()
        upperBounds.add(NetBeansJavaClassifierType(TypeMirrorHandle.create((type as TypeVariable).upperBound), project))
    }
}

class Equals(val handle: TypeMirrorHandle<*>,
             private val handle2: TypeMirrorHandle<*>) : Task<CompilationController> {

    var equals = false

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val type = handle.resolve(info) as? TypeVariable ?: return
        val type2 = handle2.resolve(info) as? TypeVariable ?: return

        equals = type.asElement() == type2.asElement()
    }
}

class TypeMirrorHandleEquals(val handle: TypeMirrorHandle<*>,
                             private val handle2: TypeMirrorHandle<*>) : Task<CompilationController> {
    
    var equals = false
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        
        val type = handle.resolve(info) ?: return
        val type2 = handle2.resolve(info) ?: return
        
        equals = type == type2
    }
}