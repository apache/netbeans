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
package org.jetbrains.kotlin.diagnostics.netbeans.reformatter;

import com.intellij.psi.PsiFile;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jetbrains.kotlin.diagnostics.netbeans.indentation.AlignmentStrategy;
import org.jetbrains.kotlin.formatting.KotlinFormatterUtils;
import org.jetbrains.kotlin.formatting.NetBeansDocumentFormattingModel;
import org.jetbrains.kotlin.formatting.NetBeansFormattingModel;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.openide.filesystems.FileObject;

public class KotlinReformatTask implements ReformatTask {

    private final Context context;

    public KotlinReformatTask(Context context) {
        this.context = context;
    }

    @Override
    public void reformat() throws BadLocationException {
        Document document = context.document();
        FileObject file = ProjectUtils.getFileObjectForDocument(document);
        
        if (file != null){
            PsiFile parsedFile = ProjectUtils.getKtFile(context.document().
                    getText(0, context.document().getLength()), file);
            if (parsedFile == null) {
                return;
            }
            Project project = ProjectUtils.getKotlinProjectForFileObject(file);
            String code = parsedFile.getText();
            KotlinFormatterUtils.formatCode(code, parsedFile.getName(), project, "\n");
            String formattedCode = NetBeansDocumentFormattingModel.getNewText();
            document.remove(0, document.getLength());
            document.insertString(0, formattedCode, null);
            
        }
        
    }

    @Override
    public ExtraLock reformatLock() {
        return null;
    }
    
}
