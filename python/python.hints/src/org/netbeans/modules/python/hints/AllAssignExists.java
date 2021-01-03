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
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.scopes.ScopeInfo;
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.netbeans.modules.python.source.scopes.SymInfo;
import org.openide.util.NbBundle;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Str;

public class AllAssignExists extends PythonAstRule {
    @Override
    public Set<Class> getKinds() {
        return Collections.<Class>singleton(Module.class);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        PythonParserResult ppr = (PythonParserResult)context.parserResult;
        SymbolTable symbolTable = ppr.getSymbolTable();
        List<Str> publicSymbols = symbolTable.getPublicSymbols();
        if (publicSymbols != null) {
            // Check that we actually have all the symbols called for
            // by the all-list

            ScopeInfo topScope = symbolTable.getScopeInfo(context.node);
            assert topScope != null;

            // Mark all other symbols private!
            for (Str str : publicSymbols) {
                //String name = PythonAstUtils.getExprName(expr);
                String name = PythonAstUtils.getStrContent(str);
                if (name != null) {
                    SymInfo sym = topScope.tbl.get(name);
                    if (sym == null) {
                        // Uh oh -- missing!
                        PythonParserResult info = (PythonParserResult) context.parserResult;
                        OffsetRange range = PythonAstUtils.getNameRange(info, str);
                        range = PythonLexerUtils.getLexerOffsets(info, range);
                        if (range != OffsetRange.NONE) {
                            List<HintFix> fixList = Collections.emptyList();
                            String message = NbBundle.getMessage(AllAssignExists.class, "AllAssignExistsMsg", name);
                            Hint desc = new Hint(this, message, info.getSnapshot().getSource().getFileObject(), range, fixList, 205);
                            result.add(desc);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getId() {
        return "AllAssignExists"; // NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(AllAssignExists.class, "AllAssignExistsDesc");
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AllAssignExists.class, "AllAssignExists");
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }
}
