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
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.openide.util.NbBundle;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Module;

public class AttributeDefinedOutsideInit extends PythonAstRule {
    private final static String ATTRIBUTE_DEFINED_OUTSIDE_INIT = "AttributeDefinedOutsideInit";
    private final static String ATTRIBUTE_DEFINED_OUTSITE_INIT_VAR = "AttributeDefinedOutsideInitVariable";
    private final static String ATTRIBUTE_DEFINED_OUTSIDE_INIT_DESC = "AttributeDefinedOutsideInitDesc";

    @Override
    public Set<Class> getKinds() {
        return Collections.<Class>singleton(Module.class);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        PythonParserResult info = (PythonParserResult) context.parserResult;
        PythonParserResult pr = PythonAstUtils.getParseResult(info);
        SymbolTable symbolTable = pr.getSymbolTable();


        List<Attribute> notInIntBound = symbolTable.getNotInInitAttributes(info);
        if (notInIntBound.size() > 0) {
            for (Attribute cur : notInIntBound) {
                OffsetRange range = PythonAstUtils.getRange(cur);
                range = PythonLexerUtils.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    List<HintFix> fixList = Collections.emptyList();
                    String message = NbBundle.getMessage(NameRule.class,
                            ATTRIBUTE_DEFINED_OUTSITE_INIT_VAR,
                            cur.getInternalAttr());
                    Hint desc = new Hint(this, message, info.getSnapshot().getSource().getFileObject(), range, fixList, 2305);
                    result.add(desc);
                }
            }
        }

    }

    @Override
    public String getId() {
        return ATTRIBUTE_DEFINED_OUTSIDE_INIT;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(RelativeImports.class, ATTRIBUTE_DEFINED_OUTSIDE_INIT_DESC);
    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
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
        return NbBundle.getMessage(AccessToProtected.class, ATTRIBUTE_DEFINED_OUTSIDE_INIT);
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }
}
