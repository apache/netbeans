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
import javax.swing.text.BadLocationException
import javax.swing.text.Document
import org.jetbrains.kotlin.formatting.KotlinFormatterUtils
import org.jetbrains.kotlin.formatting.NetBeansDocumentFormattingModel
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.api.project.Project
import org.netbeans.modules.editor.indent.spi.Context
import org.netbeans.modules.editor.indent.spi.ExtraLock
import org.netbeans.modules.editor.indent.spi.ReformatTask
import org.openide.filesystems.FileObject


/*

  @author Alexander.Baratynski
  Created on Sep 9, 2016
*/

class KotlinReformatTask(val context : Context) : ReformatTask {
    
    override fun reformat() {
        val document = context.document()
        
        val file = ProjectUtils.getFileObjectForDocument(document)
        if (file == null) return
        
        val parsedFile = ProjectUtils.getKtFile(context.document().getText(0,context.document().length), file)
        if (parsedFile == null) return
        
        val project = ProjectUtils.getKotlinProjectForFileObject(file)
        val code = parsedFile.text
        KotlinFormatterUtils.formatCode(code, parsedFile.name, project, "\n")
        val formattedCode = NetBeansDocumentFormattingModel.getNewText()
        document.remove(0, document.length)
        document.insertString(0, formattedCode, null)
    }
    
    override fun reformatLock() : ExtraLock? = null
    
}