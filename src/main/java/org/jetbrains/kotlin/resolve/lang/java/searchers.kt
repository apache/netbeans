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

import com.sun.javadoc.Doc
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.netbeans.api.java.source.ClasspathInfo
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.java.source.SourceUtils
import org.netbeans.api.java.source.Task
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project
import org.openide.filesystems.FileObject

class TypeElementSearcher(val fqName: String, val project: Project) : Task<CompilationController> {

    var element: ElemHandle<TypeElement>? = null

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = info.elements.getTypeElement(fqName) ?: return
        element = ElemHandle.create(elem, project)
    }
}

class TypeElementHandleSearcher(val fqName: String, val project: Project) : Task<CompilationController> {

    var element: ElementHandle<TypeElement>? = null

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = info.elements.getTypeElement(fqName) ?: return
        element = ElementHandle.create(elem)
    }
}

class TypeMirrorHandleSearcher(val fqName: String) : Task<CompilationController> {

    lateinit var handle: TypeMirrorHandle<*>

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = info.elements.getTypeElement(fqName) ?: throw UnsupportedOperationException("Couldn't resolve $fqName'")
        handle = TypeMirrorHandle.create(elem.asType())
    }
}

class PackageElementSearcher(val fqName: String, val project: Project) : Task<CompilationController> {

    var `package`: ElemHandle<PackageElement>? = null

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = info.elements.getPackageElement(fqName) ?: return
        `package` = ElemHandle.create(elem, project)
    }
}

class ClassIdComputer(val handle: ElemHandle<TypeElement>) : Task<CompilationController> {

    var classId: ClassId? = null

    fun computeClassId(classBinding: TypeElement): ClassId? {
        val container = classBinding.enclosingElement

        if (container.getKind() != ElementKind.PACKAGE) {
            val parentClassId = computeClassId(container as TypeElement) ?: return null
            return parentClassId.createNestedClassId(Name.identifier(classBinding.simpleName.toString()))
        }

        val fqName = classBinding.qualifiedName.toString()
        return ClassId.topLevel(FqName(fqName))
    }

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info) ?: return
        classId = computeClassId(elem as TypeElement)
    }
}

class ElementSearcher(val offset: Int) : Task<CompilationController> {

    var element: ElementHandle<*>? = null

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val treePath = info.treeUtilities.pathFor(offset)
        val elem = info.trees.getElement(treePath) ?: return

        if (elem.kind != ElementKind.LOCAL_VARIABLE) element = ElementHandle.create(elem)
    }
}

class ElementSimpleNameSearcher(val element: ElemHandle<*>) : Task<CompilationController> {

    var simpleName: String = ""

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = element.resolve(info) ?: return
        simpleName = elem.simpleName.toString()
    }
}

class ElementHandleSimpleNameSearcher(val element: ElementHandle<*>) : Task<CompilationController> {

    var simpleName: String? = null

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = element.resolve(info) ?: return
        simpleName = elem.simpleName.toString()
    }
}

class FileObjectForFqNameSearcher(val fqName: String, val cpInfo: ClasspathInfo) : Task<CompilationController> {

    var fileObject: FileObject? = null

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val te = info.elements.getTypeElement(fqName) ?: return
        val handle = ElementHandle.create(te)
        fileObject = SourceUtils.getFile(handle, cpInfo)
    }
}

class IsDeprecatedSearcher(val element: ElemHandle<*>?) : Task<CompilationController> {

    var isDeprecated = false

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        if (element == null) return
        val elem = element.resolve(info) ?: return

        isDeprecated = info.elements.isDeprecated(elem)
    }
}

class JavaDocSearcher(val element: ElemHandle<*>?) : Task<CompilationController> {
    
    var javaDoc: Doc? = null
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        
        if (element == null) return
        val elem = element.resolve(info) ?: return
        
        javaDoc = info.elementUtilities.javaDocFor(elem)
    }
}