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

import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.NoType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType
import org.jetbrains.kotlin.load.java.structure.JavaConstructor
import org.jetbrains.kotlin.load.java.structure.JavaField
import org.jetbrains.kotlin.load.java.structure.JavaMethod
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClass
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClassifierType
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaConstructor
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaField
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaMethod
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaTypeParameter
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.java.source.Task
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project
import com.intellij.psi.CommonClassNames

class NameSearcher(val handle: ElemHandle<TypeElement>) : Task<CompilationController> {
    var name: Name = SpecialNames.safeIdentifier(handle.qualifiedName) 
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        val element = handle.resolve(info) ?: return
        
        name = SpecialNames.safeIdentifier(element.simpleName.toString())
    }
}

class SuperTypesSearcher(val handle: ElemHandle<TypeElement>, val project: Project) : Task<CompilationController> {
    val superTypes = arrayListOf<JavaClassifierType>()
    
    fun getSuperTypesMirrors(typeBinding: TypeElement): List<TypeMirror> {
        val superTypesList = typeBinding.interfaces.toMutableList()
        
        val superclass = typeBinding.superclass
        if (superclass !is NoType) superTypesList.add(superclass)
        
        return superTypesList
    }
    
    fun getSuperTypesWithObject(typeBinding: TypeElement, info: CompilationController): Array<TypeMirror> {
        val allSuperTypes = getSuperTypesMirrors(typeBinding).toMutableList()
        val hasObject = !allSuperTypes.none{ it.toString() == CommonClassNames.JAVA_LANG_OBJECT }
        
        if (!hasObject && typeBinding.toString() != CommonClassNames.JAVA_LANG_OBJECT) {
            allSuperTypes.add(info.elements.getTypeElement(CommonClassNames.JAVA_LANG_OBJECT).asType())
        }
        
        return allSuperTypes.toTypedArray()
    }
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        val element = handle.resolve(info) ?: return
        
        getSuperTypesWithObject(element as TypeElement, info)
                .forEach{ superTypes.add(
                        NetBeansJavaClassifierType(TypeMirrorHandle.create(it), project)) }
    }
}
 
class InnerClassesSearcher(val handle: ElemHandle<TypeElement>, 
                           val project: Project) : Task<CompilationController> {
    val innerClasses = arrayListOf<JavaClass>()
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        val element = handle.resolve(info) ?: return
        
        val filteredMembers = element.enclosedElements
                .filter{ it.asType().kind == TypeKind.DECLARED }
                .filterIsInstance(TypeElement::class.java)
                .map{ NetBeansJavaClass(ElemHandle.create(it, project), project) }
        innerClasses.addAll(filteredMembers)
    }
}

class OuterClassSearcher(val handle: ElemHandle<TypeElement>,
                         val project: Project) : Task<CompilationController> {
    var outerClass: JavaClass? = null
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        val element = handle.resolve(info) ?: return
        
        val outer = element.enclosingElement ?: return
        if (outer.asType().kind != TypeKind.DECLARED) return
        
        outerClass = NetBeansJavaClass(ElemHandle.create(outer as TypeElement, project), project)
    }
}

class MethodsSearcher(val handle: ElemHandle<TypeElement>,
                      val project: Project, val containingClass: JavaClass) : Task<CompilationController> {
    val methods = arrayListOf<JavaMethod>()
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        val element = handle.resolve(info) ?: return
        
        val filteredMembers = element.enclosedElements
                .filter{ it.kind == ElementKind.METHOD }
                .map{ NetBeansJavaMethod(ElemHandle.create(
                        it as ExecutableElement, project), containingClass, project) }
        methods.addAll(filteredMembers)
    }
}

class MethodHandlesSearcher(val handle: ElementHandle<TypeElement>) : Task<CompilationController> {
    val methods = arrayListOf<ElementHandle<ExecutableElement>>()
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        val element = handle.resolve(info) ?: return
        
        val filteredMembers = element.enclosedElements
                .filter{ it.kind == ElementKind.METHOD }
                .filterIsInstance(ExecutableElement::class.java)
                .map { ElementHandle.create(it) }
        
        methods.addAll(filteredMembers)
    }
}

class ConstructorsSearcher(val handle: ElemHandle<TypeElement>,
                      val project: Project, val containingClass: JavaClass) : Task<CompilationController> {
    val constructors = arrayListOf<JavaConstructor>()
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        val element = handle.resolve(info) ?: return
        
        val filteredMembers = element.enclosedElements
                .filter{ it.kind == ElementKind.CONSTRUCTOR }
                .map{ NetBeansJavaConstructor(ElemHandle.create(
                        it as ExecutableElement, project), containingClass, project) }
        constructors.addAll(filteredMembers)
    }
}

class FieldsSearcher(val handle: ElemHandle<TypeElement>,
                     val project: Project, val containingClass: JavaClass) : Task<CompilationController> {
    val fields = arrayListOf<JavaField>()
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        val element = handle.resolve(info) ?: return
        
        val filteredMembers = element.enclosedElements
                .filter{ it.kind.isField() }
                .filter{ Name.isValidIdentifier(it.simpleName.toString()) }
                .map { NetBeansJavaField(ElemHandle.create(
                        it as VariableElement, project), containingClass, project) }
        fields.addAll(filteredMembers)
    }
}

class TypeParametersSearcher(val handle: ElemHandle<TypeElement>,
                             val project: Project) : Task<CompilationController> {
    val typeParameters = arrayListOf<JavaTypeParameter>()
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        val element = handle.resolve(info) ?: return
        
        (element as TypeElement).typeParameters
                .forEach{ typeParameters.add(NetBeansJavaTypeParameter(ElemHandle.create(it, project), project)) }
    }
}