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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
