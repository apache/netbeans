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

package org.netbeans.modules.web.el.hints;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.web.el.CompilationContext;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Hints provider for Expression Language.
 *
 * @author Erno Mononen
 */
public final class ELHintsProvider implements HintsProvider {

    enum Kind {
        DEFAULT
    }

    @Override
    public void computeHints(final HintsManager manager, final RuleContext context, final List<Hint> hints) {
        // computing the all hints - not just errors - due to #189590
        Map<?, List<? extends AstRule>> allHints = manager.getHints(false, context);
        final List<? extends ELRule> ids = (List<? extends ELRule>) allHints.get(Kind.DEFAULT);
        if (ids == null) {
            return;
        }
        final FileObject file = context.parserResult.getSnapshot().getSource().getFileObject();
        JavaSource jsource = JavaSource.create(ELTypeUtilities.getElimplExtendedCPI(file));
        try {
            jsource.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController info) throws Exception {
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationContext ccontext = CompilationContext.create(file, info);
                    for (ELRule rule : ids) {
                        if (manager.isEnabled(rule)) {
                            rule.run(ccontext, context, hints);
                        }
                    }

                }

            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> suggestions, int caretOffset) {
    }

    @Override
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> suggestions, int start, int end) {
    }

    @Override
    public void computeErrors(final HintsManager manager, final RuleContext context, final List<Hint> hints, List<Error> unhandled) {
        // parse errors are not handled here, so let the infrastructure just display
        // them as they are
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
    public ELRuleContext createRuleContext() {
        return new ELRuleContext();
    }

}
