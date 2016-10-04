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
package org.jetbrains.kotlin.hints

import javax.swing.text.Document
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.netbeans.modules.csl.api.HintFix


class KotlinAutoImportFix(val fqName: String, val parserResult: KotlinParserResult) : HintFix {
    
    override fun getDescription() = "Add import for ${fqName}"
    override fun isSafe() = true
    override fun isInteractive() = false
    
    override fun implement() {
        val doc = parserResult.snapshot.source.getDocument(false)
        val ktFile = parserResult.ktFile
        
        val importDirectives = ktFile.importDirectives
        val packageDirective = ktFile.packageDirective
        
        if (importDirectives.isNotEmpty()) {
            val importDirective = importDirectives.last()
            doc.insertString(importDirective.textOffset + importDirective.textLength, 
                    "\nimport ${fqName}", null)
        } else if (packageDirective != null) {
            doc.insertString(packageDirective.textOffset + packageDirective.textLength, 
                    "\n\nimport ${fqName}", null)
        } else {
            doc.insertString(0, "import ${fqName}", null)
        }
    }
}