package org.black.kotlin.diagnostics.netbeans.reformatter;

import com.intellij.psi.PsiFile;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.black.kotlin.diagnostics.netbeans.indentation.AlignmentStrategy;
import org.black.kotlin.utils.ProjectUtils;
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
            
            String formattedCode = AlignmentStrategy.alignCode(parsedFile.getNode(), "\n");
            document.remove(0, document.getLength());
            document.insertString(0, formattedCode, null);
            
        }
        
    }

    @Override
    public ExtraLock reformatLock() {
        return null;
    }
    
}
