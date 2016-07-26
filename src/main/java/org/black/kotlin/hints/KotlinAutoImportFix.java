package org.black.kotlin.hints;

import org.netbeans.modules.csl.api.HintFix;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinAutoImportFix implements HintFix {
    
    private final String fqName;
    
    public KotlinAutoImportFix(String fqName) {
        this.fqName = fqName;
    }

    
    @Override
    public String getDescription() {
        return "Add import for " + fqName;
    }

    @Override
    public void implement() throws Exception {
    
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
