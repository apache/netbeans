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
package org.jetbrains.kotlin.search

import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.api.project.Project

object KotlinTypeSearcher {
    
    private fun findDeclarationsInFile(ktFile: KtFile, 
                                       fqName: String) = ktFile.declarations
            .filter { it.name?.contains(fqName) == true }
    
    fun searchDeclaration(project: Project, fqName: String) = ProjectUtils.getSourceFilesWithDependencies(project)
            .mapNotNull {
                val declarationsInFile = findDeclarationsInFile(it, fqName)
                if (declarationsInFile.isNotEmpty()) {
                    it to findDeclarationsInFile(it, fqName)
                }
                else null
            }
    
}