/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.latte.hints;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteHintsProvider implements HintsProvider {
    public static final String DEFAULT_HINTS = "default.hints"; //NOI18N
    private volatile boolean cancel = false;

    @Override
    public void computeHints(HintsManager manager, RuleContext context, List<Hint> hints) {
        resume();
        Map<?, List<? extends Rule.AstRule>> allHints = manager.getHints(false, context);
        List<? extends Rule.AstRule> defaultHints = allHints.get(DEFAULT_HINTS);
        RulesRunner<Hint> rulesRunner = new RulesRunnerImpl<>(manager, context, hints);
        rulesRunner.run(defaultHints, new ResetCaretOffsetAdjuster());
    }

    @Override
    public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> suggestions, int caretOffset) {
        resume();
    }

    @Override
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> suggestions, int start, int end) {
        resume();
    }

    @Override
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        resume();
        unhandled.addAll(context.parserResult.getDiagnostics());
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
        return Collections.emptyList();
    }

    @Override
    public RuleContext createRuleContext() {
        return new LatteRuleContext();
    }

    public static final class LatteRuleContext extends RuleContext {

    }

    private interface RulesRunner<T> {
        void run(List<? extends Rule> rules, RuleAdjuster adjuster);
    }

    private final class RulesRunnerImpl<T> implements RulesRunner<T> {
        private final HintsManager hintManager;
        private final RuleContext ruleContext;
        private final List<T> result;

        public RulesRunnerImpl(HintsManager hintManager, RuleContext ruleContext, List<T> result) {
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
                if (rule instanceof Rule.AstRule) {
                    Rule.AstRule astRule = (Rule.AstRule) rule;
                    if (hintManager.isEnabled(astRule)) {
                        adjustAndInvoke(rule, adjuster);
                    }
                } else if (rule instanceof Rule.ErrorRule) {
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
                invokableRule.invoke(ruleContext, result);
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
                CaretSensitiveRule caretSensitiveRule = (CaretSensitiveRule) rule;
                caretSensitiveRule.setCaretOffset(caretOffset);
            }
        }

    }

}
