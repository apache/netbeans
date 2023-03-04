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

package org.netbeans.modules.web.jsf.editor.hints;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.Rule.ErrorRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.editor.api.gsf.ErrorBadgingRule;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public abstract class HintsProvider {
    
    /**
     * Gets the actual Source's (document/file) source code.
     */
    protected static CharSequence getSourceText(Source source) {
        final AtomicReference<CharSequence> sourceTextRef = new AtomicReference<>();
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    sourceTextRef.set(resultIterator.getSnapshot().getText());
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return sourceTextRef.get();
    }
    
    public abstract List<Hint> compute(RuleContext context);

    protected static final ErrorRule DEFAULT_ERROR_RULE = new Rule(HintSeverity.ERROR, true);
    protected static final ErrorRule DEFAULT_WARNING_RULE = new Rule(HintSeverity.WARNING, true);
    protected static final ErrorRule ERROR_RULE_BADGING = new BadgingRule(HintSeverity.ERROR, true);

    protected static final int DEFAULT_ERROR_HINT_PRIORITY = 50;

    private static class Rule implements ErrorRule {

        private HintSeverity severity;
        private boolean showInTasklist;

        protected Rule(HintSeverity severity, boolean showInTaskList) {
            this.severity = severity;
            this.showInTasklist = showInTaskList;
        }

        @Override
        public Set<?> getCodes() {
            return Collections.emptySet();
        }

        @Override
        public boolean appliesTo(RuleContext context) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "jsf"; //NOI18N //does this show up anywhere????
        }

        @Override
        public boolean showInTasklist() {
            return showInTasklist;
        }

        @Override
        public HintSeverity getDefaultSeverity() {
            return severity;
        }
        
    }

    private static final class BadgingRule extends Rule implements ErrorBadgingRule {

        public BadgingRule(HintSeverity severity, boolean showInTaskList) {
            super(severity, showInTaskList);
        }
        
    }
}
