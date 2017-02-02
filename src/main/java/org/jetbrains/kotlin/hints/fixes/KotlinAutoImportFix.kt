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
package org.jetbrains.kotlin.hints.fixes

import javax.swing.text.Document
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinError
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.resolve.lang.java.findFQName
import org.jetbrains.kotlin.hints.KotlinRule
import org.netbeans.modules.csl.api.HintFix
import org.netbeans.modules.csl.api.Hint
import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.csl.api.HintSeverity


fun autoImport(fqName: String, doc: Document) {
    val file = ProjectUtils.getFileObjectForDocument(doc) ?: return
    val ktFile = ProjectUtils.getKtFile(doc.getText(0, doc.length), file) ?: return
    
    insert(fqName, doc, ktFile)
}

private fun insert(fqName: String, doc: Document, ktFile: KtFile) {
    val importDirectives = ktFile.importDirectives
    val packageDirective = ktFile.packageDirective

    if (importDirectives.isNotEmpty()) {
        val offset = getOffsetToInsert(importDirectives, fqName)
        if (offset != null) {
            doc.insertString(offset,
                    "import ${fqName}\n", null)
        } else {
            doc.insertString(importDirectives.last().textRange.endOffset,
                    "\nimport $fqName", null)
        }
    } else if (packageDirective != null) {
        doc.insertString(packageDirective.textOffset + packageDirective.textLength,
                "\n\nimport ${fqName}", null)
    } else {
        doc.insertString(0, "import ${fqName}", null)
    }
}

private fun getOffsetToInsert(importDirectives: List<KtImportDirective>, fqName: String): Int? {
    importDirectives.filter { it.importedFqName != null }
            .forEach {
                if (it.importedFqName!!.asString().compareTo(fqName) > 0) {
                    return it.textRange.startOffset
                }
            }
    return null
}

fun KotlinError.createHintForUnresolvedReference(parserResult: KotlinParserResult): Hint {
        val suggestions = parserResult.project.findFQName(psi.text)
        val fixes = suggestions.map { KotlinAutoImportFix(it, parserResult) }

        return Hint(KotlinRule(HintSeverity.ERROR), "Class not found", parserResult.snapshot.source.fileObject,
                OffsetRange(startPosition, endPosition), fixes, 10)
    }

class KotlinAutoImportFix(val fqName: String, val parserResult: KotlinParserResult) : HintFix {

    override fun getDescription() = "Add import for ${fqName}"
    override fun isSafe() = true
    override fun isInteractive() = false

    override fun implement() {
        val doc = parserResult.snapshot.source.getDocument(false)
        val ktFile = parserResult.ktFile

        insert(fqName, doc, ktFile)
    }

}