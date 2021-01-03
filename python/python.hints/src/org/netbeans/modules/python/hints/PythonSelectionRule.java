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

import javax.swing.text.BadLocationException;
import java.util.List;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule.SelectionRule;
import org.netbeans.modules.csl.api.Rule.UserConfigurableRule;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;

/**
 * Represents a rule to be run on text selection
 *
 */
public abstract class PythonSelectionRule implements SelectionRule, UserConfigurableRule {
    protected abstract int getApplicability(PythonRuleContext context, PythonTree root, OffsetRange astRange);

    //public abstract void run(PythonRuleContext context, List<Hint> result);
    public void run(PythonRuleContext context, List<Hint> result) {
        // TODO - decide if this code represents a complete statement...
        // For now - that's true iff there's no code to the left on the
        // start line and code to the right on the end line
        BaseDocument doc = context.doc;
        int originalStart = context.selectionStart;
        int originalEnd = context.selectionEnd;
        int docLength = doc.getLength();

        if (originalEnd > docLength) {
            return;
        }
        OffsetRange narrowed = PythonLexerUtils.narrow(doc, new OffsetRange(originalStart, originalEnd), false);
        if (narrowed == OffsetRange.NONE) {
            return;
        }

        int start = narrowed.getStart();
        int end = narrowed.getEnd();
        try {
            if (start > Utilities.getRowFirstNonWhite(doc, Math.min(docLength, start))) {
                return;
            }
            if (end < Utilities.getRowLastNonWhite(doc, Math.min(docLength, end)) + 1) {
                return;
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        PythonTree root = PythonAstUtils.getRoot(context.parserResult);
        if (root == null) {
            return;
        }

        OffsetRange astRange = PythonAstUtils.getAstOffsets(context.parserResult, new OffsetRange(start, end));
        if (astRange == OffsetRange.NONE) {
            return;
        }

        int applicability = getApplicability(context, root, astRange);
        if (applicability == 0) {
            return;
        }
        // Don't allow extract with if you're inside strings or comments
        Token<? extends PythonTokenId> startToken = PythonLexerUtils.getToken(doc, start);
        Token<? extends PythonTokenId> endToken = PythonLexerUtils.getToken(doc, end);
        if (startToken == null || endToken == null) {
            return;
        }
        TokenId startId = startToken.id();
        if (startId == PythonTokenId.STRING_LITERAL ||
                (startId == PythonTokenId.COMMENT && start > 0 && startToken == PythonLexerUtils.getToken(doc, start - 1))) {
            return;
        }
        TokenId endId = endToken.id();
        if (endId == PythonTokenId.STRING_LITERAL) {
            return;
        }

        // TODO - don't enable inside comments or strings!!
        // TODO - if you are including functions or classes it should probably
        // be disabled!

        OffsetRange range = new OffsetRange(originalStart, originalEnd);

        run(context, result, range, applicability);
    }

    public abstract void run(PythonRuleContext context, List<Hint> result, OffsetRange range, int applicability);
}
