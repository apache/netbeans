/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.languages.env.hints;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.languages.env.EnvFileResolver;
import org.netbeans.modules.languages.env.parser.EnvParserResult;
import org.openide.util.NbBundle;

public class EnvHintsProvider implements HintsProvider {

    public static final String ENV_HINTS_GROUP_KIND = "env.option.duplicatekey.hints";  //NOI18N

    @Override
    public void computeHints(HintsManager manager, RuleContext context, List<Hint> hints) {
        if (!(context.parserResult instanceof EnvParserResult)) {
            return;
        }

        EnvParserResult parserResult = (EnvParserResult) context.parserResult;

        Map<?, List<? extends Rule.AstRule>> allHints = manager.getHints(false, context);
        List<? extends Rule.AstRule> hintRules = allHints.get(ENV_HINTS_GROUP_KIND);

        if (hintRules == null) {
            return;
        }

        for (Rule.AstRule astRule : hintRules) {
            if (!manager.isEnabled(astRule)) {
                continue;
            }
            if (astRule instanceof DuplicateKeyAssignment) {
                for (Map.Entry<String, List<OffsetRange>> entry : parserResult.getKeyDefinitions().entrySet()) {
                    int listSize = entry.getValue().size();

                    if (listSize == 1) {
                        continue;
                    }

                    for (OffsetRange range : entry.getValue().subList(1, listSize)) {
                        hints.add(new Hint(astRule,
                                NbBundle.getMessage(EnvHintsProvider.class, "DuplicateKeyHintMsg"), //NOI18N
                                context.parserResult.getSnapshot().getSource().getFileObject(),
                                range,
                                configHintsFixList(),
                                10));
                    }
                }
            }
        }
    }

    @Override
    public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> suggestions, int caretOffset) {

    }

    @Override
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> suggestions, int start, int end) {

    }

    @Override
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        unhandled.addAll(context.parserResult.getDiagnostics());
    }

    @Override
    public void cancel() {

    }

    @Override
    public List<Rule> getBuiltinRules() {
        return null;
    }

    @Override
    public RuleContext createRuleContext() {
        return new EnvRuleContext();
    }

    private List<HintFix> configHintsFixList() {
        List<HintFix> fixes = new LinkedList<>();

        fixes.add(new ConfigHintFix());

        return fixes;
    }

    public class EnvRuleContext extends RuleContext {

        public boolean isCancelled() {
            return false;
        }

    }

    private static class ConfigHintFix implements HintFix {

        public ConfigHintFix() {
        }

        @Override
        public String getDescription() {
            return "Configure Hints"; //NOI18N
        }

        @Override
        public void implement() throws Exception {
            OptionsDisplayer displayer = OptionsDisplayer.getDefault();
            displayer.open("Editor/Hints/" + EnvFileResolver.MIME_TYPE); //NOI18N
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
}
