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
package org.netbeans.modules.html.editor.hints.other;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.hints.HtmlRuleContext;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Christian Lenz
 */
public class AddMissingAltAttributeHint extends Hint {

    public AddMissingAltAttributeHint(HtmlRuleContext context, OpenTag openTag) {
        super(AddMissingAltAttributeRule.getInstance(),
            AddMissingAltAttributeRule.getInstance().getDescription(),
            context.getFile(),
            createOffsetRange(context.getSnapshot(), openTag),
            Collections.<HintFix>singletonList(new AddMissingAltAttributeHintFix(context, openTag)),
            10);
    }

    private static OffsetRange createOffsetRange(Snapshot snapshot, OpenTag openTag) {
        int originalFrom = snapshot.getOriginalOffset(openTag.from());
        int originalTo = snapshot.getOriginalOffset(openTag.to());
        if (originalFrom == -1 || originalTo == -1) {
            // Fallback to snapshot offsets if translation fails
            return new OffsetRange(openTag.from(), openTag.to());
        }

        return new OffsetRange(originalFrom, originalTo);
    }

    private static class AddMissingAltAttributeHintFix implements HintFix {

        private static final Logger LOGGER = Logger.getLogger(AddMissingAltAttributeHintFix.class.getSimpleName());

        private final HtmlRuleContext context;
        private final OpenTag openTag;

        public AddMissingAltAttributeHintFix(HtmlRuleContext context, OpenTag openTag) {
            this.context = context;
            this.openTag = openTag;
        }

        @Override
        public String getDescription() {
            return AddMissingAltAttributeRule.getInstance().getDisplayName();
        }

        @Override
        public void implement() throws Exception {
            BaseDocument document = (BaseDocument) context.getSnapshot().getSource().getDocument(true);
            Snapshot snapshot = context.getSnapshot();

            document.runAtomic(() -> {
                try {
                    int insertPosition = -1;
                    boolean isSelfClosing = false;

                    // Convert snapshot offset to original document offset
                    int originalTagStart = snapshot.getOriginalOffset(openTag.from());
                    if (originalTagStart == -1) {
                        return;
                    }

                    // Try lexer tokens first - use original document offsets
                    TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(document, originalTagStart);

                    if (ts != null) {
                        ts.move(originalTagStart);
                        while (ts.moveNext()) {
                            Token<HTMLTokenId> token = ts.token();
                            if (token == null) {
                                break;
                            }

                            if (token.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                                insertPosition = ts.offset();
                                isSelfClosing = "/>".contentEquals(token.text()); // NOI18N
                                break;
                            } else if (token.id() == HTMLTokenId.TAG_OPEN) {
                                // We've hit another tag - stop searching
                                break;
                            }
                        }
                    }

                    // Fallback to parser-based position if lexer didn't work
                    if (insertPosition == -1) {
                        int originalTagEnd = snapshot.getOriginalOffset(openTag.to());
                        if (originalTagEnd == -1) {
                            return;
                        }
                        isSelfClosing = openTag.isEmpty();
                        // tagEnd points after '>', so go back 1 for '>', or 2 for '/>'
                        insertPosition = isSelfClosing ? originalTagEnd - 2 : originalTagEnd - 1;
                    }

                    if (insertPosition <= 0) {
                        return;
                    }

                    // Check whether a space before alt is needed
                    char charBefore = document.getText(insertPosition - 1, 1).charAt(0);
                    boolean needsSpaceBefore = charBefore != ' ';

                    String altAttribute = (needsSpaceBefore ? " " : "") + "alt=\"\"" + (isSelfClosing ? " " : ""); // NOI18N

                    document.insertString(insertPosition, altAttribute, null);
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.WARNING, "Invalid offset: {0}", ex.offsetRequested());
                }
            });
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
