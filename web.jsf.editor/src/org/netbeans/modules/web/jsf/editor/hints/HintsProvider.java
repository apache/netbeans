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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
