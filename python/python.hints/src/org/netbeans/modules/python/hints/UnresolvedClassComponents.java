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
