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
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinError
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.resolve.lang.java.findFQName
import org.jetbrains.kotlin.hints.KotlinRule
import org.netbeans.api.project.Project
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

private fun Project.getPublicFunctions(name: String) = ProjectUtils.getSourceFiles(this)
        .flatMap { ktFile ->
            PublicFunctionsVisitor(name).let {
                ktFile.acceptChildren(it)
                it.publicFunctions
            }
        }

class AutoImportFix(kotlinError: KotlinError,
                    parserResult: KotlinParserResult) : KotlinQuickFix(kotlinError, parserResult) {

    private var fqName: String? = null
    private val types by lazy { parserResult.project.findFQName(kotlinError.psi.text) }
    private val suggestions by lazy { if (types.isNotEmpty()) types else parserResult.project.getPublicFunctions(kotlinError.psi.text) }
    
    constructor(kotlinError: KotlinError,
                parserResult: KotlinParserResult,
                fqName: String) : this(kotlinError, parserResult) {
        this.fqName = fqName
    }

    override val hintSeverity = HintSeverity.ERROR

    override fun isApplicable() = when (kotlinError.diagnostic.factory) {
        Errors.UNRESOLVED_REFERENCE -> suggestions.isNotEmpty()
        else -> false
    }

    override fun createFixes() =  suggestions.map { AutoImportFix(kotlinError, parserResult, it) }

    override fun getDescription() = "Add import for $fqName"

    override fun implement() {
        val doc = parserResult.snapshot.source.getDocument(false)
        val ktFile = parserResult.ktFile

        insert(fqName!!, doc, ktFile)
    }
}

private class PublicFunctionsVisitor(private val name: String) : KtVisitorVoid() {

    val publicFunctions = hashSetOf<String>()

    override fun visitNamedFunction(function: KtNamedFunction) {
        val functionName = function.name ?: return
        val fqName = function.fqName?.asString() ?: return
        
        if (functionName == name && function.modifierList == null) {
            publicFunctions.add("$fqName")
        }
    }

}