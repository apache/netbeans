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

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinError
import org.jetbrains.kotlin.hints.KotlinRule
import org.netbeans.modules.csl.api.Hint
import org.netbeans.modules.csl.api.HintFix
import org.netbeans.modules.csl.api.HintSeverity
import org.netbeans.modules.csl.api.OffsetRange

class RemoveUselessElvisFix(val error: KotlinError,
                            val parserResult: KotlinParserResult) : HintFix {

    override fun getDescription() = "Remove useless elvis operator"
    override fun isSafe() = true
    override fun isInteractive() = false

    override fun implement() {
        val doc = parserResult.snapshot.source.getDocument(false)
        
        doc.remove(error.startPosition, error.endPosition - error.startPosition)
    }
}

fun KotlinError.createRemoveUselessElvisFix(parserResult: KotlinParserResult) =  Hint(
        KotlinRule(HintSeverity.WARNING), 
        "Remove useless elvis operator", 
        parserResult.snapshot.source.fileObject,
        OffsetRange(startPosition, endPosition), 
        listOf(RemoveUselessElvisFix(this, parserResult)), 
        10
)