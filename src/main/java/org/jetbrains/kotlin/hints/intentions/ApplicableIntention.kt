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
package org.jetbrains.kotlin.hints.intentions

import com.intellij.psi.PsiElement
import javax.swing.text.Document
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.hints.KotlinRule
import org.netbeans.modules.csl.api.*
import org.openide.filesystems.FileObject

abstract class ApplicableIntention(val doc: Document,
                                   val analysisResult: AnalysisResult?,
                                   val psi: PsiElement): HintFix {
    
    abstract fun isApplicable(caretOffset: Int): Boolean
    
    override fun isSafe() = true
    
    override fun isInteractive() = false
    
}

abstract class Inspection(open val element: KtElement) {
    
    abstract val description: String
    
    abstract fun isApplicable(): Boolean
    
    fun hint(fileObject: FileObject) = Hint(
            KotlinRule(HintSeverity.WARNING),
            description,
            fileObject,
            OffsetRange(element.textRange.startOffset, element.textRange.endOffset),
            emptyList(),
            10
    )
    
}