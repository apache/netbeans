package org.black.kotlin.hints;

import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinAutoImportRule implements Rule {

    @Override
    public boolean appliesTo(RuleContext context) {
        return context instanceof KotlinRuleContext; 
    }

    @Override
    public String getDisplayName() {
        return "Class not found";
    }

    @Override
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }
    
}
