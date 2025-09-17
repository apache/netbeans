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
package org.netbeans.modules.javascript2.editor.hints;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class IncorrectReturnStatement extends JsAstRule {

    @Override
    void computeHints(JsHintsProvider.JsRuleContext context, List<Hint> hints, int offset, HintsProvider.HintsManager manager) throws BadLocationException {
        Snapshot snapshot = context.getJsParserResult().getSnapshot();
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(snapshot, context.lexOffset);
        OffsetRange returnOffsetRange;
        if (ts != null) {
            while (ts.moveNext()) {
                Token<? extends JsTokenId> token = LexUtilities.findNextIncluding(ts, Arrays.asList(JsTokenId.KEYWORD_RETURN));
                if (token != null) {
                    JsTokenId tokenId = token.id();
                    if (tokenId == JsTokenId.KEYWORD_RETURN) {
                        returnOffsetRange = new OffsetRange(ts.offset(), ts.offset() + token.length());
                        if (ts.moveNext()) {
                            token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT));
                            tokenId = token.id();
                            if (tokenId == JsTokenId.EOL) {
                                addHint(context, hints, offset, JS_OTHER_HINTS, returnOffsetRange);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addHint(JsHintsProvider.JsRuleContext context, List<Hint> hints, int offset, String name, OffsetRange range) throws BadLocationException {
        hints.add(new Hint(this, Bundle.JsIncorrectReturnStatementHintDesc(),
                context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                ModelUtils.documentOffsetRange(context.getJsParserResult(),
                        range.getStart(), range.getEnd()), null, 600));
    }

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(JsAstRule.JS_OTHER_HINTS);
    }

    @Override
    public String getId() {
        return "jsincorrectreturnstatement.hint";
    }

    @NbBundle.Messages({
        "JsIncorrectReturnStatementDesc=Incorrect return statement.",
        "JsIncorrectReturnStatementHintDesc=Incorrect return statement"})
    @Override
    public String getDescription() {
        return Bundle.JsIncorrectReturnStatementDesc();
    }

    @Override
    public String getDisplayName() {
        return Bundle.JsIncorrectReturnStatementHintDesc();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

}
