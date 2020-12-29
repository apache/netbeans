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
package org.netbeans.modules.python.hints;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.ImportManager;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Module;

/**
 * Detect Unresolved class attributes
 *
 */
public class UnresolvedClassComponents extends PythonAstRule {

    private final static String CLASS_UNRESOLVED_ATTRIBUTES = "UnresolvedAttributes";
    private final static String CLASS_UNRESOLVED_INHERITANCE_VAR = "UnresolvedInheritanceVariable";
    private final static String CLASS_UNRESOLVED_ATTRIBUTES_VAR = "UnresolvedAttributesVariable";
    private final static String CLASS_UNRESOLVED_ATTRIBUTES_DESC = "UnresolvedAttributesDesc";



    public UnresolvedClassComponents() {
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    public Set<Class> getKinds() {
        return Collections.<Class>singleton(Module.class);
    }

    private void populateMessages( PythonParserResult info, List<PythonTree> unresolved , List<Hint> result ,boolean isClass ) {
        if (unresolved.size() > 0) {

            for (PythonTree node : unresolved) {
                // Compute suggestions
                String name = PythonAstUtils.getName(node);
                if (name == null) {
                    name = "";
                }
                List<HintFix> fixList = Collections.emptyList();
                String message ;
                if ( isClass)
                  message = NbBundle.getMessage(NameRule.class, CLASS_UNRESOLVED_INHERITANCE_VAR, name);
                else
                  message = NbBundle.getMessage(NameRule.class, CLASS_UNRESOLVED_ATTRIBUTES_VAR, name);
                OffsetRange range = PythonAstUtils.getRange( node);
                range = PythonLexerUtils.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    Hint desc = new Hint(this, message, info.getSnapshot().getSource().getFileObject(), range, fixList, 2305);
                    result.add(desc);
                }
            }
        }
    }


    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        PythonParserResult info = (PythonParserResult) context.parserResult;
        SymbolTable symbolTable = info.getSymbolTable();

        List<PythonTree> unresolvedAttributes = symbolTable.getUnresolvedAttributes(info);
        populateMessages(info,unresolvedAttributes,result,false) ;
        List<PythonTree> unresolvedParents = symbolTable.getUnresolvedParents(info);
        populateMessages(info,unresolvedParents,result,true) ;
    }

    @Override
    public String getId() {
        return CLASS_UNRESOLVED_ATTRIBUTES; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(NameRule.class, CLASS_UNRESOLVED_ATTRIBUTES);
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(NameRule.class, CLASS_UNRESOLVED_ATTRIBUTES_DESC);
    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    private static class ImportFix implements HintFix {
        private final PythonRuleContext context;
        private final PythonTree node;
        private final String module;

        private ImportFix(PythonRuleContext context, PythonTree node, String module) {
            this.context = context;
            this.node = node;
            this.module = module;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(CreateDocString.class, "FixImport", module);
        }

        @Override
        public void implement() throws Exception {
            String mod = this.module;
            String symbol = null;
            int colon = mod.indexOf(':');
            if (colon != -1) {
                int end = mod.indexOf('(', colon + 1);
                if (end == -1) {
                    end = mod.indexOf(';', colon + 1);
                    if (end == -1) {
                        end = mod.length();
                    }
                }
                symbol = mod.substring(colon + 1, end).trim();
                mod = mod.substring(0, colon).trim();
            }
            new ImportManager((PythonParserResult) context.parserResult).ensureImported(mod, symbol, false, false, true);
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
