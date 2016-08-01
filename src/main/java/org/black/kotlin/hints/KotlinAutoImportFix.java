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
package org.black.kotlin.hints;

import java.util.List;
import javax.swing.text.Document;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtImportDirective;
import org.jetbrains.kotlin.psi.KtPackageDirective;
import org.netbeans.modules.csl.api.HintFix;

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
