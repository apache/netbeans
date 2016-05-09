package org.black.kotlin.navigation;

import com.intellij.psi.PsiElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.black.kotlin.navigation.references.ReferenceUtils;
import org.black.kotlin.utils.LineEndUtil;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtReferenceExpression;
import org.openide.filesystems.FileObject;

public class NavigationUtil {
    
    @Nullable
    public static KtReferenceExpression getReferenceExpression(Document doc, int offset) throws BadLocationException{
        FileObject file = ProjectUtils.getFileObjectForDocument(doc);
        if (file == null){
            return null;
        }
        
        KtFile ktFile = ProjectUtils.getKtFile(doc.getText(0, doc.getLength()), file);
        if (ktFile == null){
            return null;
        }
        
        int documentOffset = LineEndUtil.convertCrToDocumentOffset(ktFile.getText(), offset);
        PsiElement psiExpression = ktFile.findElementAt(documentOffset);
        if (psiExpression == null){
            return null;
        }
        
        return ReferenceUtils.getReferenceExpression(psiExpression);
    }
    
}
