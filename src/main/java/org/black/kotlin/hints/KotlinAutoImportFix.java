package org.black.kotlin.hints;

import javax.swing.text.Document;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtImportDirective;
import org.netbeans.modules.csl.api.HintFix;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinAutoImportFix implements HintFix {
    
    private final String fqName;
    private final KotlinParserResult parserResult;
    
    public KotlinAutoImportFix(String fqName, KotlinParserResult parserResult) {
        this.fqName = fqName;
        this.parserResult = parserResult;
    }

    
    @Override
    public String getDescription() {
        return "Add import for " + fqName;
    }

    @Override
    public void implement() throws Exception {
        Document doc = parserResult.getSnapshot().getSource().getDocument(false);
        KtFile ktFile = parserResult.getKtFile();
        
        KtImportDirective importDirective = 
                ktFile.getImportDirectives().get(ktFile.getImportDirectives().size()-1);
        int placeToInsert = importDirective.getTextOffset() + importDirective.getTextLength();
        
        doc.insertString(placeToInsert, "\nimport " + fqName + "\n", null);
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }
    
}
