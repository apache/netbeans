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
import org.netbeans.modules.python.source.NameStyle;
import org.openide.util.NbBundle;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Name;
import org.python.antlr.base.expr;

/**
 * Check direct acces to parent protected variables or methods
 */
public class AccessToProtected extends PythonAstRule {
    private final static String ACCESS_PROTECTED_ID = "AccessProtected"; // NOI18N
    private final static String ACCESS_PROTECTED_VARIABLE = "AccessProtectedVariable"; // NOI18N
    private final static String ACCESS_PROTECTED_DESC = "AccessProtectedDesc"; // NOI18N

    @Override
    public Set<Class> getKinds() {
        return Collections.<Class>singleton(Attribute.class);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        PythonParserResult info = (PythonParserResult) context.parserResult;
        Attribute cur = (Attribute)context.node;
        String curAttr = cur.getInternalAttr();
        if (curAttr == null) {
            return;
        }

        if (NameStyle.isProtectedName(curAttr)) {
            expr curValue = cur.getInternalValue();
            if (curValue instanceof Name) {
                Name nam = (Name)curValue;
                String id = nam.getInternalId();
                if (id.equals("self")) { // NOI18N
                    return; // normal access from class instance
                }
                if (PythonAstUtils.getParentClassFromNode(context.path, null, id) != null) {
                    return; // parent access
                }
                // we should warn here : Access to protected Attributes from non child
                // classes
                OffsetRange range = PythonAstUtils.getRange(cur);
                range = PythonLexerUtils.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    List<HintFix> fixList = Collections.emptyList();
                    String message = NbBundle.getMessage(NameRule.class, ACCESS_PROTECTED_VARIABLE, curAttr);
                    Hint desc = new Hint(this, message, info.getSnapshot().getSource().getFileObject(), range, fixList, 2305);
                    result.add(desc);
                }
            }
        }
    }

    @Override
    public String getId() {
        return ACCESS_PROTECTED_ID;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(RelativeImports.class, ACCESS_PROTECTED_DESC);
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
        return NbBundle.getMessage(AccessToProtected.class, ACCESS_PROTECTED_ID);
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
