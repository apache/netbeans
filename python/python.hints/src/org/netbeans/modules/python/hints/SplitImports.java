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
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.CodeStyle;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.alias;

/**
 * Import statements should be one per line. This quickfix
 * offers to make it so.
 *
 */
public class SplitImports extends PythonAstRule {
    @Override
    public Set<Class> getKinds() {
        return Collections.singleton((Class)Import.class);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        Import imp = (Import)context.node;
        List<alias> names = imp.getInternalNames();
        if (names != null && names.size() > 1) {
            PythonTree node = context.node;
            PythonParserResult info = (PythonParserResult)context.parserResult;
            OffsetRange astOffsets = PythonAstUtils.getNameRange(info, node);
            OffsetRange lexOffsets = PythonLexerUtils.getLexerOffsets(info, astOffsets);
            BaseDocument doc = context.doc;
            try {
                if (lexOffsets != OffsetRange.NONE && lexOffsets.getStart() < doc.getLength() &&
                        (context.caretOffset == -1 ||
                        Utilities.getRowStart(doc, context.caretOffset) == Utilities.getRowStart(doc, lexOffsets.getStart()))) {
                    List<HintFix> fixList = new ArrayList<>();
                    fixList.add(new SplitImportsFix(context, imp));
                    String displayName = getDisplayName();
                    Hint desc = new Hint(this, displayName, info.getSnapshot().getSource().getFileObject(), lexOffsets, fixList, 1500);
                    result.add(desc);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public String getId() {
        return "SplitImports"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SplitImports.class, "SplitImports");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(SplitImports.class, "SplitImportsDesc");
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
        CodeStyle codeStyle = CodeStyle.getDefault(context.doc);
        return codeStyle == null || codeStyle.oneImportPerLine();
    }

    @Override
    public boolean showInTasklist() {
        return true;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    private static class SplitImportsFix implements PreviewableFix {
        private final PythonRuleContext context;
        private final Import imp;

        private SplitImportsFix(PythonRuleContext context, Import imp) {
            this.context = context;
            this.imp = imp;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SplitImports.class, "SplitImportsFix");
        }

        @Override
        public boolean canPreview() {
            return true;
        }

        @Override
        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);

            OffsetRange astRange = PythonAstUtils.getRange(imp);
            if (astRange != OffsetRange.NONE) {
                OffsetRange lexRange = PythonLexerUtils.getLexerOffsets((PythonParserResult) context.parserResult, astRange);
                if (lexRange != OffsetRange.NONE) {
                    int indent = GsfUtilities.getLineIndent(doc, lexRange.getStart());
                    StringBuilder sb = new StringBuilder();
                    List<alias> names = imp.getInternalNames();
                    if (names != null) {
                        for (alias at : names) {
                            if (indent > 0 && sb.length() > 0) {
                                sb.append(IndentUtils.createIndentString(doc, indent));
                            }
                            sb.append("import "); // NOI18N
                            sb.append(at.getInternalName());
                            if (at.getInternalAsname() != null && at.getInternalAsname().length() > 0) {
                                sb.append(" as "); // NOI18N
                                sb.append(at.getInternalAsname());
                            }
                            sb.append("\n");
                        }
                    }
                    // Remove the final newline since Import doesn't include it
                    sb.setLength(sb.length() - 1);

                    edits.replace(lexRange.getStart(), lexRange.getLength(), sb.toString(), false, 0);
                }
            }

            return edits;
        }

        @Override
        public void implement() throws Exception {
            EditList edits = getEditList();
            edits.apply();
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
