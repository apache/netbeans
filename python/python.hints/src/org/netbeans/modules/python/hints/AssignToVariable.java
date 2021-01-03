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
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.python.source.PythonParserResult;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Str;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

/**
 * Assign an expression to a variable
 *
 */
public class AssignToVariable extends PythonAstRule {
    @Override
    public Set<Class> getKinds() {
        return Collections.singleton((Class)Expr.class);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        PythonTree node = context.node;
        Expr expr = (Expr)node;
        expr exprValue = expr.getInternalValue();
        if (exprValue instanceof Str) {
            // Skip triple-quoted strings (typically doc strings)
            Str str = (Str)exprValue;
            String s = str.getText();
            if (s.startsWith("'''") || s.startsWith("\"\"\"")) { // NOI18N
                return;
            }
            PythonTree grandParent = context.path.leafGrandParent();
            if (grandParent instanceof FunctionDef) {
                FunctionDef def = (FunctionDef)grandParent;
                List<stmt> body = def.getInternalBody();
                if (body != null && body.size() > 0 && body.get(0) == expr) {
                    // First string in a function -- it's a docstring
                    return;
                }
            }
        }
        if (exprValue instanceof Call) {
            // Skip calls - they may have side effects
            // ...unless it looks like a "getter" style Python method
            Call call = (Call)exprValue;
            if (!PythonAstUtils.isGetter(call, false)) {
                return;
            }
        }
        PythonParserResult info = (PythonParserResult) context.parserResult;
        OffsetRange astOffsets = PythonAstUtils.getNameRange(info, node);
        OffsetRange lexOffsets = PythonLexerUtils.getLexerOffsets(info, astOffsets);
        BaseDocument doc = context.doc;
        try {
            if (lexOffsets != OffsetRange.NONE && lexOffsets.getStart() < doc.getLength() &&
                    (context.caretOffset == -1 ||
                    Utilities.getRowStart(doc, context.caretOffset) == Utilities.getRowStart(doc, lexOffsets.getStart()))) {
                List<HintFix> fixList = new ArrayList<>();
                fixList.add(new AssignToVariableFix(context, node));
                String displayName = getDisplayName();
                Hint desc = new Hint(this, displayName, info.getSnapshot().getSource().getFileObject(), lexOffsets, fixList, 1500);
                result.add(desc);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public String getId() {
        return "AssignToVariable"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(AssignToVariable.class, "AssignToVariable");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(AssignToVariable.class, "AssignToVariableDesc");
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
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }

    private static class AssignToVariableFix implements PreviewableFix {
        private final PythonRuleContext context;
        private final PythonTree node;
        private int varOffset;
        private String varName;

        private AssignToVariableFix(PythonRuleContext context, PythonTree node) {
            this.context = context;
            this.node = node;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(AssignToVariable.class, "AssignToVariableFix");
        }

        @Override
        public boolean canPreview() {
            return true;
        }

        @Override
        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);

            OffsetRange astRange = PythonAstUtils.getRange(node);
            if (astRange != OffsetRange.NONE) {
                OffsetRange lexRange = PythonLexerUtils.getLexerOffsets((PythonParserResult) context.parserResult, astRange);
                if (lexRange != OffsetRange.NONE) {
                    int offset = lexRange.getStart();
                    StringBuilder sb = new StringBuilder();
                    varName = NbBundle.getMessage(AssignToVariable.class, "VarName");
                    sb.append(varName);
                    sb.append(" = "); // NOI18N
                    varOffset = offset;
                    edits.replace(offset, 0, sb.toString(), false, 0);
                }
            }

            return edits;
        }

        @Override
        public void implement() throws Exception {
            EditList edits = getEditList();

            Position pos = edits.createPosition(varOffset);
            edits.apply();
            if (pos != null && pos.getOffset() != -1) {
                JTextComponent target = GsfUtilities.getPaneFor(context.parserResult.getSnapshot().getSource().getFileObject());
                if (target != null) {
                    int start = pos.getOffset();
                    int end = start + varName.length();
                    target.select(start, end);
                }
            }
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
