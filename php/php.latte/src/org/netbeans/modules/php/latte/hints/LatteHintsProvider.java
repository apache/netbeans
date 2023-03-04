/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
