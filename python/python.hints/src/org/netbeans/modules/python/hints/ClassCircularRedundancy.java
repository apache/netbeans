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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
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
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.openide.util.NbBundle;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Module;

/**
 * check for redundancy cycling in parent child
 */
public class ClassCircularRedundancy extends PythonAstRule {
    private final static String CLASS_CIRCULAR_REDUNDANCY = "ClassCircularRedundancy";
    private final static String CLASS_CIRCULAR_REDUNDANCY_VAR = "ClassCircularRedundancyVariable";
    private final static String CLASS_CIRCULAR_REDUNDANCY_DESC = "ClassCircularRedundancyDesc";

    @Override
    public Set<Class> getKinds() {
        return Collections.<Class>singleton(Module.class);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        PythonParserResult info = (PythonParserResult) context.parserResult;
        SymbolTable symbolTable = info.getSymbolTable();


        HashMap<ClassDef, String> cyclingRedundancies = symbolTable.getClassesCyclingRedundancies(info);
        if (cyclingRedundancies.size() > 0) {
            Set<Entry<ClassDef, String>> wk = cyclingRedundancies.entrySet();
            for (Entry<ClassDef, String> cur : wk) {
                ClassDef curClass = cur.getKey();
                String curCyclingMsg = curClass.getInternalName() + "/" + cur.getValue(); // NOI18N
                OffsetRange range = PythonAstUtils.getNameRange(info, curClass);
                // range = PythonLexerUtils.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    List<HintFix> fixList = Collections.emptyList();
                    String message = NbBundle.getMessage(NameRule.class, CLASS_CIRCULAR_REDUNDANCY_VAR, curCyclingMsg);
                    Hint desc = new Hint(this, message, info.getSnapshot().getSource().getFileObject(), range, fixList, 2305);
                    result.add(desc);
                }
            }
        }
    }

    @Override
    public String getId() {
        return CLASS_CIRCULAR_REDUNDANCY;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(RelativeImports.class, CLASS_CIRCULAR_REDUNDANCY_DESC);
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
        return NbBundle.getMessage(AccessToProtected.class, CLASS_CIRCULAR_REDUNDANCY);
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
