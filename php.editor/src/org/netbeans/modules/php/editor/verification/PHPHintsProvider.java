/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.Rule.ErrorRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.parser.PHPParseResult;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPHintsProvider implements HintsProvider {
    public static final String DEFAULT_HINTS = "default.hints"; //NOI18N
    public static final String DEFAULT_SUGGESTIONS = "default.suggestions"; //NOI18N
    volatile boolean cancel = false;

    enum ErrorType {
        UNHANDLED_ERRORS,
        HINT_ERRORS
    }

    @Override
    public void computeHints(HintsManager mgr, RuleContext context, List<Hint> hints) {
        resume();
        Map<?, List<? extends Rule.AstRule>> allHints = mgr.getHints(false, context);
        List<? extends AstRule> modelHints = allHints.get(DEFAULT_HINTS);
        if (cancel) {
            return;
        }
        RulesRunner<Hint> rulesRunner = new RulesRunnerImpl<>(mgr, initializeContext(context), hints);
        if (cancel) {
            return;
        }
        RuleAdjuster forAllAdjusters = new ForAllAdjusters(Arrays.asList(new PreferencesAdjuster(mgr), new ResetCaretOffsetAdjuster()));
        if (cancel) {
            return;
        }
        rulesRunner.run(modelHints, forAllAdjusters);
    }

    @Override
    public void computeSuggestions(HintsManager mgr, RuleContext context, List<Hint> suggestions, int caretOffset) {
        resume();
        RulesRunner<Hint> rulesRunner = new RulesRunnerImpl<>(mgr, initializeContext(context), suggestions);
        if (cancel) {
            return;
        }
        RuleAdjuster forAllAdjusters = new ForAllAdjusters(Arrays.asList(new PreferencesAdjuster(mgr), new CaretOffsetAdjuster(caretOffset)));
        Map<?, List<? extends AstRule>> hintsOnLine = mgr.getHints(true, context);
        if (cancel) {
            return;
        }
        List<? extends AstRule> defaultHintsOnLine = hintsOnLine.get(DEFAULT_HINTS);
        if (cancel) {
            return;
        }
        if (defaultHintsOnLine != null) {
            rulesRunner.run(defaultHintsOnLine, forAllAdjusters);
        }
        Map<?, List<? extends Rule.AstRule>> allHints = mgr.getSuggestions();
        if (cancel) {
            return;
        }
        List<? extends AstRule> modelHints = allHints.get(DEFAULT_SUGGESTIONS);
        if (cancel) {
            return;
        }
        if (modelHints != null) {
            rulesRunner.run(modelHints, forAllAdjusters);
        }
    }

    @Override
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> selections, int start, int end) {
    }

    @Override
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        resume();
        List<? extends Error> errors = context.parserResult.getDiagnostics();
        unhandled.addAll(errors);
        if (cancel) {
            return;
        }
        Map<?, List<? extends ErrorRule>> allErrors = manager.getErrors();
        if (cancel) {
            return;
        }
        List<? extends ErrorRule> unhandledErrors = allErrors.get(ErrorType.UNHANDLED_ERRORS);
        if (cancel) {
            return;
        }
        if (unhandledErrors != null) {
            RulesRunner<Error> rulesRunner = new RulesRunnerImpl<>(manager, initializeContext(context), unhandled);
            rulesRunner.run(unhandledErrors, RuleAdjuster.NONE);
        }
        if (cancel) {
            return;
        }
        List<? extends ErrorRule> hintErrors = allErrors.get(ErrorType.HINT_ERRORS);
        if (cancel) {
            return;
        }
        if (hintErrors != null) {
            RulesRunner<Hint> rulesRunner = new RulesRunnerImpl<>(manager, initializeContext(context), hints);
            rulesRunner.run(hintErrors, RuleAdjuster.NONE);
        }
    }

    private PHPRuleContext initializeContext(RuleContext context) {
        PHPRuleContext phpRuleContext = (PHPRuleContext) context;
        ParserResult info = context.parserResult;
        PHPParseResult result = (PHPParseResult) info;
        if (cancel) {
            return phpRuleContext;
        }
        final Model model = result.getModel();
        if (cancel) {
            return phpRuleContext;
        }
        FileScope modelScope = model.getFileScope();
        phpRuleContext.fileScope = modelScope;
        return phpRuleContext;
    }

    @Override
    public void cancel() {
        cancel = true;
    }

    private void resume() {
        cancel = false;
    }

    @Override
    public List<Rule> getBuiltinRules() {
        return Collections.<Rule>emptyList();
    }

    @Override
    public RuleContext createRuleContext() {
        return new PHPRuleContext();
    }

    private interface RulesRunner<T> {
        void run(List<? extends Rule> rules, RuleAdjuster adjuster);
    }

    private final class RulesRunnerImpl<T> implements RulesRunner<T> {
        private final HintsManager hintManager;
        private final PHPRuleContext ruleContext;
        private final List<T> result;

        public RulesRunnerImpl(HintsManager hintManager, PHPRuleContext ruleContext, List<T> result) {
            this.hintManager = hintManager;
            this.ruleContext = ruleContext;
            this.result = result;
        }

        @Override
        public void run(List<? extends Rule> rules, RuleAdjuster adjuster) {
            for (Rule rule : rules) {
                if (cancel) {
                    break;
                }
                if (rule instanceof AstRule) {
                    AstRule astRule = (AstRule) rule;
                    if (hintManager.isEnabled(astRule)) {
                        adjustAndInvoke(rule, adjuster);
                    }
                } else if (rule instanceof ErrorRule) {
                    adjustAndInvoke(rule, adjuster);
                }
            }
        }

        private void adjustAndInvoke(Rule rule, RuleAdjuster adjuster) {
            if (cancel) {
                return;
            }
            if (rule instanceof InvokableRule) {
                adjuster.adjust(rule);
                InvokableRule<T> invokableRule = (InvokableRule<T>) rule;
                if (cancel) {
                    return;
                }
                List<T> tempResult = new ArrayList<>();
                invokableRule.invoke(ruleContext, tempResult);
                boolean checkResult = false;
                assert checkResult = true;
                if (checkResult) {
                    for (T item : tempResult) {
                        assert item != null : rule;
                    }
                }
                result.addAll(tempResult);
            }
        }

    }

    private interface RuleAdjuster {
        RuleAdjuster NONE = new RuleAdjuster() {

            @Override
            public void adjust(Rule rule) {
            }
        };

        void adjust(Rule rule);
    }

    private final class ForAllAdjusters implements RuleAdjuster {
        private final Collection<RuleAdjuster> adjusters;

        public ForAllAdjusters(Collection<RuleAdjuster> adjusters) {
            this.adjusters = adjusters;
        }

        @Override
        public void adjust(Rule rule) {
            for (RuleAdjuster hintAdjuster : adjusters) {
                if (cancel) {
                    return;
                }
                hintAdjuster.adjust(rule);
            }
        }

    }

    private static final class ResetCaretOffsetAdjuster implements RuleAdjuster {
        private final RuleAdjuster caretOffsetAdjuster;

        public ResetCaretOffsetAdjuster() {
            caretOffsetAdjuster = new CaretOffsetAdjuster(-1);
        }

        @Override
        public void adjust(Rule rule) {
            caretOffsetAdjuster.adjust(rule);
        }

    }

    private static final class CaretOffsetAdjuster implements RuleAdjuster {
        private final int caretOffset;

        public CaretOffsetAdjuster(int caretOffset) {
            this.caretOffset = caretOffset;
        }

        @Override
        public void adjust(Rule rule) {
            if (rule instanceof CaretSensitiveRule) {
                CaretSensitiveRule icm = (CaretSensitiveRule) rule;
                icm.setCaretOffset(caretOffset);
            }
        }

    }

    private static final class PreferencesAdjuster implements RuleAdjuster {
        private final HintsManager hintManager;

        public PreferencesAdjuster(HintsManager hintManager) {
            this.hintManager = hintManager;
        }

        @Override
        public void adjust(Rule rule) {
            if (rule instanceof CustomisableRule) {
                CustomisableRule icm = (CustomisableRule) rule;
                Preferences preferences = hintManager.getPreferences(icm);
                assert preferences != null : rule;
                icm.setPreferences(preferences);
            }
        }

    }

}
