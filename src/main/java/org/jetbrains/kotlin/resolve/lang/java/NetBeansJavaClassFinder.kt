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

import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import org.jetbrains.kotlin.load.java.JavaClassFinder
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaPackage
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClass
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaPackage
import org.jetbrains.kotlin.resolve.lazy.KotlinCodeAnalyzer
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.CodeAnalyzerInitializer
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.netbeans.api.project.Project

class NetBeansJavaClassFinder : JavaClassFinder {
    
    private lateinit var project: Project
    
    @Inject fun setProjectScope(project: Project) {
        this.project = project
    }
    
    @PostConstruct fun initialize(trace: BindingTrace, codeAnalyzer: KotlinCodeAnalyzer) {
        val ideaProject = KotlinEnvironment.getEnvironment(project).project
        CodeAnalyzerInitializer.Companion.getInstance(ideaProject).initialize(trace, codeAnalyzer.moduleDescriptor, codeAnalyzer)
    }
    
    
    override fun findClass(classId: ClassId): JavaClass? {
        val element = project.findType(classId.asSingleFqName().asString()) ?: return null
        
        return NetBeansJavaClass(element, project)
    }
    
    override fun findPackage(fqName: FqName): JavaPackage? {
        val pack = project.findPackage(fqName.asString()) ?: return null
        
        return NetBeansJavaPackage(pack, project)
    }

    override fun knownClassNamesInPackage(packageFqName: FqName) = null
}