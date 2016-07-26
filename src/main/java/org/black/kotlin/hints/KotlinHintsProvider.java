package org.black.kotlin.hints;

import com.google.common.collect.Lists;
import java.util.List;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinHintsProvider implements HintsProvider{

    @Override
    public void computeHints(HintsManager hm, RuleContext rc, List<Hint> list) {
        KotlinParserResult parserResult = (KotlinParserResult) rc.parserResult;
        List<? extends Error> errors = parserResult.getDiagnostics();
        for (Error error : errors) {
            String name = error.toString();
            System.out.println();
        }
    }

    @Override
    public void computeSuggestions(HintsManager hm, RuleContext rc, List<Hint> list, int i) {
    }

    @Override
    public void computeSelectionHints(HintsManager hm, RuleContext rc, List<Hint> list, int i, int i1) {
    }

    @Override
    public void computeErrors(HintsManager hm, RuleContext rc, List<Hint> list, List<Error> list1) {
    }

    @Override
    public void cancel() {
    }

    @Override
    public List<Rule> getBuiltinRules() {
        return Lists.newArrayList();
    }

    @Override
    public RuleContext createRuleContext() {
        return new KotlinRuleContext();
    }
    
}
