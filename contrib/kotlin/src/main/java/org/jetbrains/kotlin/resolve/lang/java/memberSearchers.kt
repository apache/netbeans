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

import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.load.java.JavaVisibilities
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaType
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.java.source.Task
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project

class IsAbstractSearcher(val handle: ElemHandle<*>) : Task<CompilationController> {

    var isAbstract = false

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info) ?: return
        isAbstract = elem.modifiers.contains(Modifier.ABSTRACT)
    }
}

class IsStaticSearcher(val handle: ElemHandle<*>) : Task<CompilationController> {

    var isStatic = false

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info) ?: return
        isStatic = elem.modifiers.contains(Modifier.STATIC)
    }
}

class IsFinalSearcher(val handle: ElemHandle<*>) : Task<CompilationController> {

    var isFinal = false

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info) ?: return
        isFinal = elem.modifiers.contains(Modifier.FINAL)
    }
}

class MemberNameSearcher(val handle: ElemHandle<*>) : Task<CompilationController> {

    lateinit var name: Name

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info)
        name = Name.identifier(elem.simpleName.toString())
    }
}

class MemberElementHandleNameSearcher(val handle: ElementHandle<*>) : Task<CompilationController> {

    lateinit var name: Name

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info)
        name = Name.identifier(elem.simpleName.toString())
    }
}

class ElementHandleNameSearcher(val handle: ElementHandle<*>) : Task<CompilationController> {

    lateinit var name: Name

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info)
        name = Name.identifier(elem.simpleName.toString())
    }
}

class VisibilitySearcher(val handle: ElemHandle<*>) : Task<CompilationController> {

    var visibility = JavaVisibilities.PACKAGE_VISIBILITY

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info) ?: return
        val modifiers = elem.modifiers
        visibility = when {
            modifiers.contains(Modifier.PUBLIC) -> Visibilities.PUBLIC
            modifiers.contains(Modifier.PRIVATE) -> Visibilities.PRIVATE
            modifiers.contains(Modifier.PROTECTED) -> {
                if (modifiers.contains(Modifier.STATIC)) {
                    JavaVisibilities.PROTECTED_STATIC_VISIBILITY
                } else {
                    JavaVisibilities.PROTECTED_AND_PACKAGE
                }
            }
            else -> JavaVisibilities.PACKAGE_VISIBILITY
        }
    }
}

class FieldTypeSearcher(val handle: ElemHandle<*>,
                        val project: Project) : Task<CompilationController> {

    lateinit var type: JavaType

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info)
        val mirror = (elem as VariableElement).asType()
        type = NetBeansJavaType.create(TypeMirrorHandle.create(mirror), project)
    }
}

class FieldContainingClassSearcher(val handle: ElemHandle<*>,
                                   val project: Project) : Task<CompilationController> {

    lateinit var containingClass: ElemHandle<TypeElement>

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info)
        containingClass = ElemHandle.create(elem.enclosingElement as TypeElement, project)
    }
}

class ElementHandleFieldContainingClassSearcher(val handle: ElementHandle<*>) : Task<CompilationController> {
    
    lateinit var containingClass: ElementHandle<*>
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        
        val elem = handle.resolve(info)
        containingClass = ElementHandle.create(elem.enclosingElement)
    }
}