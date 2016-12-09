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
package org.jetbrains.kotlin.navigation

import com.intellij.psi.PsiElement
import javax.swing.text.Document
import kotlin.Pair
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.utils.LineEndUtil
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.openide.filesystems.FileObject

fun getReferenceExpression(doc: Document, offset: Int): PsiElement? {
    var ktFile = KotlinParser.file
    val fo = ProjectUtils.getFileObjectForDocument(doc) ?: return null
    if (ktFile == null || !ktFile.name.equals(fo.name)) {
        ktFile = KotlinPsiManager.parseText(doc.getText(0, doc.length), fo) ?: return null
    }
    
    val documentOffset = LineEndUtil.convertCrToDocumentOffset(ktFile.text, offset)
    return ktFile.findElementAt(documentOffset)
}

fun getSpan(expression: PsiElement?): Pair<Int, Int>? {
    expression ?: return null
    
    val start = expression.textRange.startOffset
    val end = expression.textRange.endOffset
    
    return Pair(start, end)
}
