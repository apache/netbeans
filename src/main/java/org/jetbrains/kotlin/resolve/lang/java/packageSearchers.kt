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

import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaPackage
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClass
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaPackage
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.JavaSource.Phase
import org.netbeans.api.java.source.Task
import org.netbeans.api.project.Project

class SubPackagesSearcher(val project: Project,
                          val pack: JavaPackage) : Task<CompilationController> {

    val subPackages = arrayListOf<JavaPackage>()

    fun findPackageFragments(name: String, partialMatch: Boolean,
                             patternMatch: Boolean, info: CompilationController): Array<PackageElement>? {
        val packages = name.getPackages(project)
        val subpackageElements = packages.map { info.elements.getPackageElement(it) }.filterNotNull()
        if (subpackageElements.isEmpty()) return null

        return subpackageElements.toTypedArray()
    }

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val thisPackageName = pack.fqName.asString()
        val pattern = if (thisPackageName.isEmpty()) "*" else thisPackageName + "."

        val packageFragments = findPackageFragments(pattern, true, true, info)
        val thisNestedLevel = thisPackageName.split("\\.").size

        if (packageFragments != null && packageFragments.size > 0) {
            packageFragments.forEach {
                val subNestedLevel = it.qualifiedName.toString().split("\\.").size
                val applicableForRootPackage = thisNestedLevel == 1 && thisNestedLevel == subNestedLevel
                if (!it.qualifiedName.toString().isEmpty() &&
                        (applicableForRootPackage || (thisNestedLevel + 1 == subNestedLevel))) {
                    subPackages.add(NetBeansJavaPackage(ElemHandle.create(it, project), project))
                }
            }
        }
    }
}

class ClassesSearcher(val packages: List<ElemHandle<PackageElement>>,
                      val project: Project,
                      val nameFilter: (Name) -> Boolean) : Task<CompilationController> {

    val classes = arrayListOf<JavaClass>()

    fun isOuterClass(classFile: TypeElement) = !classFile.simpleName.toString().contains("$")

    fun getClassesInPackage(javaPackage: PackageElement, nameFilter: (Name) -> Boolean): List<JavaClass> {
        return javaPackage.enclosedElements
                .filter { isOuterClass(it as TypeElement) }
                .filter {
                    Name.isValidIdentifier(it.simpleName.toString())
                            && nameFilter.invoke(Name.identifier(it.simpleName.toString()))
                }
                .map { NetBeansJavaClass(ElemHandle.create(it as TypeElement, project), project) }
    }

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        for (it in packages) {
            val elem = it.resolve(info) ?: continue
            classes.addAll(getClassesInPackage(elem as PackageElement, nameFilter))
        }
    }
}

class FqNameSearcher(val handle: ElemHandle<PackageElement>) : Task<CompilationController> {
    
    lateinit var fqName: FqName
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        
        val pack = handle.resolve(info) ?: throw UnsupportedOperationException("Couldn''t resolve $handle")
        fqName = FqName((pack as PackageElement).qualifiedName.toString())
    }
}










