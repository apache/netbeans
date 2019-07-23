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
package org.jetbrains.kotlin.highlighter.occurrences

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.navigation.references.resolveToSourceDeclaration
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper.isScanning
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.language.Priorities
import org.netbeans.modules.csl.api.ColoringAttributes
import org.netbeans.modules.csl.api.OccurrencesFinder
import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.parsing.spi.Scheduler
import org.netbeans.modules.parsing.spi.SchedulerEvent

class KotlinOccurrencesFinder : OccurrencesFinder<KotlinParserResult>() {
    
    private var caretPosition = 0
    private var cancel = false
    private val highlighting = hashMapOf<OffsetRange, ColoringAttributes>()

    override fun run(result: KotlinParserResult?, event: SchedulerEvent?) {
        cancel = false
        highlighting.clear()
        if (result == null) return
        
        if (result.project.isScanning()) return
        
        val ktFile = result.ktFile
        
        val psiElement = ktFile.findElementAt(caretPosition)
        val ktElement = PsiTreeUtil.getNonStrictParentOfType(psiElement, KtElement::class.java) ?: return
        findOccurrences(ktElement, ktFile)
    }

    override fun getSchedulerClass(): Class<out Scheduler> = Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER

    override fun getPriority() = Priorities.OCCURRENCES_FINDER_PRIORITY

    override fun getOccurrences() = highlighting
    
    override fun cancel() {
        cancel = true
    }

    override fun setCaretPosition(position: Int) {
        caretPosition = position
    }
    
    private fun findOccurrences(ktElement : KtElement, ktFile: KtFile) {
        val sourceElements = ktElement.resolveToSourceDeclaration()
        if (sourceElements.isEmpty()) return
        
        val searchingElements = getSearchingElements(sourceElements)
        val ranges = search(searchingElements, ktFile)
        ranges.forEach { highlighting.put(it, ColoringAttributes.MARK_OCCURRENCES) }
    }
}