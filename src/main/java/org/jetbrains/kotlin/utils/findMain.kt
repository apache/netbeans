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
package org.jetbrains.kotlin.utils

import java.io.File
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction

fun KtFile.hasMain() = declarations.findMainFunction() != null

fun Collection<KtFile>.getKtFilesWithMainFunction() =
        this.filter { it.declarations.findMainFunction() != null }
                .map { File(it.virtualFile.path) }

private fun KtNamedFunction.isMain(): Boolean {
    if (name == "main" && valueParameters.size == 1) {
        val reference = valueParameters.first().typeReference ?: return false

        return reference.text == "Array<String>"
    }

    return false
}

private fun Collection<KtDeclaration>.findMainFunction() =
        this.filterIsInstance(KtNamedFunction::class.java)
                .filter { it.isMain() }
                .firstOrNull()
        
    
    
