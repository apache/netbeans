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

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinError;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult;
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.resolve.AnalyzingUtils;
import org.jetbrains.kotlin.resolve.lang.java.*;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager


class KotlinHintsProvider : HintsProvider {
    
    override fun computeSuggestions(hintsManager: HintsManager, ruleContext: RuleContext, 
                                    list: List<Hint>, i: Int) {}
    
    override fun computeSelectionHints(hintsManager: HintsManager, ruleContext: RuleContext, 
                                    list: List<Hint>, i: Int, i2: Int) {}
    
    override fun cancel() {}
    override fun getBuiltinRules() = emptyList<Rule>()
    override fun createRuleContext() = KotlinRuleContext()
    
    override fun computeHints(hintsManager: HintsManager, ruleContext: RuleContext, hints: MutableList<Hint>) {
        val parserResult = ruleContext.parserResult as KotlinParserResult
        val file = parserResult.snapshot.source.fileObject
        
        val errors = parserResult.diagnostics
        for (error in errors) {
            if (error.toString().startsWith("UNRESOLVED_REFERENCE")) {
                val psi = (error as KotlinError).psi
                val simpleName = psi.text
                
                val suggestions = ProjectUtils.getKotlinProjectForFileObject(file).findFQName(simpleName)
                val fixes = suggestions.map { KotlinAutoImportFix(it, parserResult) }
                
                val hint = Hint(KotlinRule(), "Class not found", file, 
                        OffsetRange(error.startPosition, error.endPosition), fixes, 10)
                hints.add(hint)
            } else if (error.toString().startsWith("ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED")) {
                val psi = (error as KotlinError).psi
                val fix = KotlinImplementMembersFix(parserResult, psi)
                val hint = Hint(KotlinRule(), "Implement members", file,
                        OffsetRange(error.startPosition, error.endPosition), listOf(fix), 10)
                hints.add(hint)
            }
        }
    }
    
    override fun computeErrors(hintsManager: HintsManager, ruleContext: RuleContext, 
                               list: List<Hint>, errors: MutableList<Error>) {
        val parserResult = ruleContext.parserResult as KotlinParserResult
        val file = parserResult.snapshot.source.fileObject
        
        val analysisResult = parserResult.analysisResult ?: return
        errors.addAll(analysisResult.analysisResult.bindingContext.diagnostics.all()
                .filter { it.psiFile.virtualFile.path == file.path }
                .map { KotlinParser.KotlinError(it, file) })
        errors.addAll(AnalyzingUtils.getSyntaxErrorRanges(parserResult.ktFile)
                .map { KotlinParser.KotlinSyntaxError(it, file) })
    }
    
}