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
package org.jetbrains.kotlin.diagnostics.netbeans.parser

import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.netbeans.modules.csl.api.Error.Badging
import org.netbeans.modules.csl.api.Severity
import org.openide.filesystems.FileObject
import com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.diagnostics.Severity as KotlinSeverity

class KotlinError(val diagnostic: Diagnostic, val fileObject: FileObject) : Badging {

    val psi = diagnostic.psiElement

    override fun toString() = diagnostic.toString()

    override fun showExplorerBadge() = diagnostic.severity == KotlinSeverity.ERROR

    override fun getDisplayName() = DefaultErrorMessages.render(diagnostic)

    override fun getDescription() = ""

    override fun getKey() = ""

    override fun getFile() = fileObject

    override fun getStartPosition() = diagnostic.textRanges[0].startOffset

    override fun getEndPosition() = diagnostic.textRanges[0].endOffset

    override fun isLineError() = startPosition - endPosition == 0

    override fun getSeverity() = when (diagnostic.severity) {
        KotlinSeverity.ERROR -> Severity.ERROR
        KotlinSeverity.WARNING -> Severity.WARNING
        KotlinSeverity.INFO -> Severity.INFO
        else -> null
    }


    override fun getParameters() = null
}

class KotlinSyntaxError(val psiError: PsiErrorElement, val fileObject: FileObject) : Badging {
    override fun showExplorerBadge() = true

    override fun getDisplayName() = psiError.errorDescription

    override fun getDescription() = ""

    override fun getKey() = ""

    override fun getFile() = fileObject

    override fun getStartPosition() = psiError.textRange.startOffset

    override fun getEndPosition() = psiError.textRange.endOffset

    override fun isLineError() = false

    override fun getSeverity() = Severity.ERROR

    override fun getParameters() = null
}