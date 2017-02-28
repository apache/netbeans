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
package org.jetbrains.kotlin.reformatting

import com.intellij.psi.PsiFile
import javax.swing.text.Document
import org.jetbrains.kotlin.formatting.KotlinFormatterUtils
import org.jetbrains.kotlin.formatting.NetBeansDocumentFormattingModel
import org.jetbrains.kotlin.navigation.netbeans.moveCaretToOffset
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.api.project.Project
import javax.swing.text.StyledDocument
import javax.swing.SwingUtilities


fun format(doc: Document, offset: Int, proj: Project? = null) {
    val file = ProjectUtils.getFileObjectForDocument(doc)
    if (file == null) return

    val parsedFile = ProjectUtils.getKtFile(doc.getText(0, doc.length), file) ?: return

    val project = proj ?: ProjectUtils.getKotlinProjectForFileObject(file)
    val code = parsedFile.text
    val formattedCode = KotlinFormatterUtils.formatCode(code, parsedFile.name, project, "\n")
    doc.remove(0, doc.length)
    doc.insertString(0, formattedCode, null)
    SwingUtilities.invokeLater(Runnable { moveCaretToOffset(doc as StyledDocument, offset) })
}