package org.black.kotlin.hints;

import java.util.List;
import javax.swing.text.Document;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtImportDirective;
import org.jetbrains.kotlin.psi.KtPackageDirective;
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
        
        List<KtImportDirective> importDirectives = ktFile.getImportDirectives();
        KtPackageDirective packageDirective = ktFile.getPackageDirective();
        int placeToInsert;
        String newImport;
        
        if (importDirectives.size() > 0) {
            KtImportDirective importDirective = 
                importDirectives.get(importDirectives.size()-1);
            placeToInsert = importDirective.getTextOffset() + importDirective.getTextLength();
            newImport = "\nimport " + fqName;
        } else if (packageDirective != null) {
            placeToInsert = packageDirective.getTextOffset() + packageDirective.getTextLength();
            newImport = "\n\nimport " + fqName;
        } else {
            placeToInsert = 0;
            newImport = "import " + fqName;
        }
        
        
        doc.insertString(placeToInsert, newImport, null);
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
