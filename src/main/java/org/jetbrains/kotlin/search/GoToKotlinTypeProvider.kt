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

import java.io.File
import org.jetbrains.kotlin.navigation.netbeans.openFileAtOffset
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.api.project.ui.OpenProjects
import org.netbeans.spi.quicksearch.SearchProvider
import org.netbeans.spi.quicksearch.SearchRequest
import org.netbeans.spi.quicksearch.SearchResponse
import org.openide.filesystems.FileUtil

class GoToKotlinTypeProvider : SearchProvider {

    override fun evaluate(request: SearchRequest, response: SearchResponse) {
        OpenProjects.getDefault().openProjects
                .flatMap { KotlinTypeSearcher.searchDeclaration(it, request.text) }
                .forEach {
                    val file = it.first
                    it.second.forEach {
                        val html = "${it.name} (${file.packageFqName.asString()})"
                        response.addResult(action@{
                            val f = File(file.virtualFile.path)
                            val fo = FileUtil.toFileObject(f) ?: return@action

                            val doc = ProjectUtils.getDocumentFromFileObject(fo) ?: return@action
                            openFileAtOffset(doc, it.textRange.startOffset)
                        }, html)
                    }
                }
    }
}