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
package org.jetbrains.kotlin.resolve

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.idea.resolve.ResolutionFacade
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.container.getService
import org.netbeans.api.project.Project as NBProject

/*

  @author Alexander.Baratynski
  Created on Sep 9, 2016
*/

class KotlinResolutionFacade(private val kotlinProject: NBProject,
                             private val componentProvider: ComponentProvider,
                             override val moduleDescriptor: ModuleDescriptor) : ResolutionFacade {
    override fun analyze(elements: Collection<KtElement>, bodyResolveMode: BodyResolveMode) = throw UnsupportedOperationException()
    

    override fun resolveToDescriptor(declaration: KtDeclaration, bodyResolveMode: BodyResolveMode) = throw UnsupportedOperationException()
    
    override val project: Project
        get() = KotlinEnvironment.getEnvironment(kotlinProject).project

    override fun analyze(element: KtElement, bodyResolveMode: BodyResolveMode): BindingContext = throw UnsupportedOperationException()

    override fun analyzeFullyAndGetResult(elements: Collection<KtElement>): AnalysisResult = throw UnsupportedOperationException()

    override fun <T : Any> getFrontendService(element: PsiElement, serviceClass: Class<T>): T = throw UnsupportedOperationException()

    override fun <T : Any> getFrontendService(serviceClass: Class<T>): T = componentProvider.getService(serviceClass)

    override fun <T : Any> getFrontendService(moduleDescriptor: ModuleDescriptor, serviceClass: Class<T>): T = throw UnsupportedOperationException()

    override fun <T : Any> getIdeService(serviceClass: Class<T>): T = throw UnsupportedOperationException()

}