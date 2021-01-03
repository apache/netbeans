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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.For;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Tuple;
import org.python.antlr.base.expr;

/**
 * Detect unused variables
 *
 * @todo Find a more reliable way of detecting return tuples without relying on the
 *  parent reference
 *
 */
public class UnusedDetector extends PythonAstRule {
    /** Default names ignored */
    private static final String DEFAULT_IGNORED_NAMES = "_, dummy";
    private static final String PARAMS_KEY = "params"; // NOI18N
    private static final String SKIP_TUPLE_ASSIGN_KEY = "skipTuples"; // NOI18N
    private static final String IGNORED_KEY = "ignorednames"; // NOI18N

    public UnusedDetector() {
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    public Set<Class> getKinds() {
        return Collections.<Class>singleton(Module.class);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        PythonParserResult info = (PythonParserResult) context.parserResult;
        SymbolTable symbolTable = info.getSymbolTable();

        boolean skipParams = true;
        Preferences pref = context.manager.getPreferences(this);
        if (pref != null) {
            skipParams = getSkipParameters(pref);
        }

        List<PythonTree> unusedNames = symbolTable.getUnused(true, skipParams);
        if (unusedNames.size() == 0) {
            return;
        }

        boolean skipTupleAssigns = true;
        Set<String> ignoreNames = Collections.emptySet();
        if (pref != null) {
            skipParams = getSkipParameters(pref);
            skipTupleAssigns = getSkipTupleAssignments(pref);
            String ignoreNamesStr = getIgnoreNames(pref);
            if (ignoreNamesStr.length() > 0) {
                ignoreNames = new HashSet<>();
                for (String s : ignoreNamesStr.split(",")) { // NOI18N
                    ignoreNames.add(s.trim());
                }
            }
        }

        for (PythonTree node : unusedNames) {
            if (skipTupleAssigns && isTupleAssignment(node)) {
                continue;
            }
            String name = PythonAstUtils.getName(node);
            if (name == null) {
                name = "";
            }
            if (ignoreNames.contains(name)) {
                continue;
            }
            OffsetRange range = PythonAstUtils.getNameRange(info, node);
            range = PythonLexerUtils.getLexerOffsets(info, range);
            if (range != OffsetRange.NONE) {
                List<HintFix> fixList = new ArrayList<>(3);
                String message = NbBundle.getMessage(NameRule.class, "UnusedVariable", name);
                Hint desc = new Hint(this, message, info.getSnapshot().getSource().getFileObject(), range, fixList, 2305);
                result.add(desc);
            }
        }
    }

    private boolean isTupleAssignment(PythonTree node) {
        // This may not work right since the parent pointers often aren't set right;
        // find a more efficient way to do it correctly than a path search for each node
        if (node.getParent() instanceof Tuple) {
            // Allow tuples in tuples
            PythonTree parentParent = node.getParent().getParent();
            while (parentParent instanceof Tuple) {
                parentParent = parentParent.getParent();
                node = node.getParent();
            }
            if (parentParent instanceof Assign) {
                Assign assign = (Assign)parentParent;
                List<expr> targets = assign.getInternalTargets();
                if (targets != null && targets.size() > 0 && targets.get(0) == node.getParent()) {
                    return true;
                }
            }
            if (parentParent instanceof For &&
                    ((For)parentParent).getInternalTarget() == node.getParent()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getId() {
        return "Unused"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(NameRule.class, "Unused");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(NameRule.class, "UnusedDesc");
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return new UnusedDetectorPrefs(node);
    }

    static boolean getSkipParameters(Preferences prefs) {
        return prefs.getBoolean(PARAMS_KEY, true);
    }

    static void setSkipParameters(Preferences prefs, boolean skipParams) {
        if (skipParams) {
            prefs.remove(PARAMS_KEY);
        } else {
            prefs.putBoolean(PARAMS_KEY, false);
        }
    }

    static boolean getSkipTupleAssignments(Preferences prefs) {
        return prefs.getBoolean(SKIP_TUPLE_ASSIGN_KEY, true);
    }

    static void setSkipTupleAssignments(Preferences prefs, boolean skipTupleAssigns) {
        if (skipTupleAssigns) {
            prefs.remove(SKIP_TUPLE_ASSIGN_KEY);
        } else {
            prefs.putBoolean(SKIP_TUPLE_ASSIGN_KEY, false);
        }
    }

    static String getIgnoreNames(Preferences prefs) {
        return prefs.get(IGNORED_KEY, DEFAULT_IGNORED_NAMES);
    }

    static void setIgnoreNames(Preferences prefs, String ignoredNames) {
        if (ignoredNames.length() == 0) {
            prefs.remove(IGNORED_KEY);
        } else {
            prefs.put(IGNORED_KEY, ignoredNames);
        }
    }
}
