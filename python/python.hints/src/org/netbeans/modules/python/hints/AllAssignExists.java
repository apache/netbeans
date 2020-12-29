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
