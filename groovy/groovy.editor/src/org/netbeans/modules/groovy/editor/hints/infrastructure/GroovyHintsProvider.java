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

package org.netbeans.modules.groovy.editor.hints.infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.compiler.error.CompilerErrorID;
import org.netbeans.modules.groovy.editor.compiler.error.GroovyError;

/**
 *
 * @author schmidtm
 */
public class GroovyHintsProvider implements HintsProvider {
    
    public static final Logger LOG = Logger.getLogger(GroovyHintsProvider.class.getName()); // NOI18N
    private volatile boolean cancelled;

    @Override
    public RuleContext createRuleContext() {
        return new GroovyRuleContext();
    }

    @Override
    public void computeHints(HintsManager manager, RuleContext context, List<Hint> hints) {
        Map<?, List<? extends Rule.AstRule>> allHints = manager.getHints(false, context);
        for (Map.Entry<?,List<? extends Rule.AstRule>> hintsEntry : allHints.entrySet()) {
            for (Rule.AstRule rule : hintsEntry.getValue()) {
                if (rule instanceof GroovyAstRule) {
                    ((GroovyAstRule)rule).computeHints((GroovyRuleContext)context, hints);
                }
            }

        }
    }

    private void invokeHint(GroovyAstRule rule, HintsManager manager, RuleContext context, List<Hint> hints) {
        rule.computeHints((GroovyRuleContext) context, hints);
    }

    @Override
    public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> suggestions, int caretOffset) {
    }

    @Override
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> result, int start, int end) {
        cancelled = false;
        ParserResult parserResult = context.parserResult;
        if (parserResult == null) {
            return;
        }
        
        ASTNode root = ASTUtils.getRoot(parserResult);

        if (root == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<GroovySelectionRule> hints = (List)manager.getSelectionHints();

        if (hints.isEmpty()) {
            return;
        }
        
        if (isCancelled()) {
            return;
        }
        
        applyRules(context, hints, start, end, result);
    }

    @Override
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> result, List<Error> unhandled) {
        // Return all the errors we -haven't- added custom error hints for:
        
        // LOG.setLevel(Level.FINEST);
        LOG.log(Level.FINEST, "@@@ computeErrors()");

        ParserResult info = context.parserResult;
        GroovyParserResult rpr = ASTUtils.getParseResult(info);
        
        if (rpr == null) {
            return;
        }

        List<? extends Error> errors = rpr.getDiagnostics();
        LOG.log(Level.FINEST, "@@@ errors.size() : {0}", errors.size());

        if (errors.isEmpty()) {
            return;
        }

        cancelled = false;
        
        @SuppressWarnings("unchecked")
        Map<CompilerErrorID,List<GroovyErrorRule>> hints = (Map)manager.getErrors();

        if (hints.isEmpty() || isCancelled()) {
            unhandled.addAll(errors);
            return;
        }
        LOG.log(Level.FINEST, "@@@ hints.size() : {0}", hints.size());
        
        for (Error error : errors) {
            if (error instanceof GroovyError) {
                LOG.log(Level.FINEST, "@@@ ----------------------------------------------------\n");
                LOG.log(Level.FINEST, "@@@ thread name   : {0}\n", Thread.currentThread().getName());
                LOG.log(Level.FINEST, "@@@ error.getDescription()   : {0}\n", error.getDescription());
                LOG.log(Level.FINEST, "@@@ error.getKey()           : {0}\n", error.getKey());
                LOG.log(Level.FINEST, "@@@ error.getDisplayName()   : {0}\n", error.getDisplayName());
                LOG.log(Level.FINEST, "@@@ error.getStartPosition() : {0}\n", error.getStartPosition());
                LOG.log(Level.FINEST, "@@@ error.getEndPosition()   : {0}\n", error.getEndPosition());
                boolean applyRet = applyRules((GroovyError) error, context, hints, result);
                LOG.log(Level.FINEST, "@@@ apply   : {0}\n", applyRet);
                if (!applyRet) {
                    LOG.log(Level.FINEST, "@@@ Adding error to unhandled");
                    unhandled.add(error);
                }
            }
        }
        LOG.log(Level.FINEST, "@@@ result.size() =  {0}", result.size());
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public List<Rule> getBuiltinRules() {
        return null;
    }

    private boolean isCancelled() {
        return cancelled;
    }
    
    private void applyRules(RuleContext context, List<GroovySelectionRule> rules, int start, int end, 
            List<Hint> result) {

        for (GroovySelectionRule rule : rules) {
            if (!rule.appliesTo(context)) {
                continue;
            }

            rule.run(context, result);
        }
    }

    /** Apply error rules and return true iff somebody added an error description for it */
    private boolean applyRules(GroovyError error, RuleContext context, Map<CompilerErrorID,List<GroovyErrorRule>> hints,
            List<Hint> result) {
        
       // LOG.setLevel(Level.FINEST);
       LOG.log(Level.FINEST, "applyRules(...)");
        
        CompilerErrorID code = error.getId();
        if (code != null) {
            List<GroovyErrorRule> rules = hints.get(code);

            if (rules != null) {
                int countBefore = result.size();
                
                for (GroovyErrorRule rule : rules) {
                    if (!rule.appliesTo(context)) {
                        continue;
                    }
                    rule.run(context, error, result);
                }
                
                return countBefore < result.size();
            }
        }
        
        return false;
    }    

    public class GroovyRuleContext extends RuleContext {

        private GroovyParserResult groovyParserResult = null;

        public GroovyParserResult getGroovyParserResult() {
            if (groovyParserResult == null) {
                groovyParserResult = (GroovyParserResult)parserResult;
            }
            return groovyParserResult;
        }

        public boolean isCancelled() {
            return cancelled;
        }

    }
}
