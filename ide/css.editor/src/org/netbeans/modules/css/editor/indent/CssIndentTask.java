/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.editor.indent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.Context.Region;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.lookup.Lookups;

import static java.util.Arrays.asList;

public class CssIndentTask implements IndentTask, Lookup.Provider {

    private final Context context;
    private final CssIndenter indenter;
    private final Lookup lookup;

    CssIndentTask(Context context) {
        this.context = context;
        this.indenter = new CssIndenter(context);
        this.lookup = Lookups.singleton(indenter.createFormattingContext());
    }

    @Override
    public void reindent() throws BadLocationException {
        if (!context.isIndent()) {
            // The idea here is that reindenting is already handled by the
            // CssIndenter, so in the current implementation, only new line
            // breaks are added where needed.
            doFormat();
        }
        indenter.reindent();
        if (!context.isIndent()) {
            doTrim();
        }
    }

    private void doTrim() throws BadLocationException {
        List<Pair<Integer,Integer>> whitespaceDelete = new ArrayList<>();
        for (Region reg : context.indentRegions()) {
            TokenHierarchy th = TokenHierarchy.get(context.document());
            List<TokenSequence<?>> tslist = th
                .embeddedTokenSequences(reg.getStartOffset(), false);
            TokenSequence<?> ts = tslist.get(tslist.size() - 1);
            ts.move(reg.getStartOffset());
            Token lastToken = null;
            while(ts.moveNext() && ts.offset() < reg.getEndOffset()) {
                if(lastToken != null
                    && lastToken.id() == CssTokenId.WS
                    && ts.token().id() == CssTokenId.NL) {
                    whitespaceDelete.add(Pair.of(lastToken.offset(th), lastToken.length()));
                }
                lastToken = ts.token();
            }
        }
        Collections.reverse(whitespaceDelete);
        for(Pair<Integer,Integer> toDelete: whitespaceDelete) {
            context.document().remove(toDelete.first(), toDelete.second());
        }
    }

    private void doFormat() throws BadLocationException {
        List<Integer> newlinesMissing = new ArrayList<>();
        for (Region reg : context.indentRegions()) {
            List<TokenSequence<?>> tslist = TokenHierarchy
                .get(context.document())
                .embeddedTokenSequences(reg.getStartOffset(), false);
            TokenSequence<?> ts = tslist.get(tslist.size() - 1);

            LinkedList<IndentType> blockLevel = new LinkedList<>();

            // Initialize blockLevel with the indentions created by the
            // outside context
            ts.moveStart();
            Token lastToken = null;
            if (ts.moveNext()) {
                List<TokenSequence<?>> tokenSequences = TokenHierarchy
                        .get(context.document())
                        .tokenSequenceList(ts.languagePath(), 0, ts.offset());
                OUTER:
                for (TokenSequence tsX : tokenSequences) {
                    tsX.moveStart();
                    while (tsX.moveNext()) {
                        if (tsX.offset() >= ts.offset()) {
                            break OUTER;
                        }
                        if (tsX.token().id() == CssTokenId.LBRACE) {
                            if (isStringInterpolation(lastToken)) {
                                // The sequence "@{" and "#{" lead in a strong interpolation
                                // in LESS (former) and SCSS (latter).
                                blockLevel.addLast(IndentType.NONE);
                            } else {
                                blockLevel.addLast(IndentType.BLOCK);
                            }
                        } else if (tsX.token().id() == CssTokenId.RBRACE) {
                            blockLevel.pollLast();
                        }
                        lastToken = tsX.token();
                    }
                }
            }

            ts.moveStart();
            while(ts.moveNext()) {
                if(ts.token().id() == CssTokenId.LBRACE) {
                    if(! isStringInterpolation(lastToken)) {
                        blockLevel.add(IndentType.BLOCK);
                        // Ensure, that there is a newline after an block opening
                        // brace "{"
                        if(LexerUtils.followsToken(ts, CssTokenId.NL, false, true, CssTokenId.WS, CssTokenId.COMMENT) == null) {
                            while(ts.moveNext()) {
                                if (ts.token().id() != CssTokenId.WS && ts.token().id() != CssTokenId.COMMENT) {
                                    newlinesMissing.add(ts.offset());
                                    ts.movePrevious();
                                    break;
                                }
                            }
                        }
                    } else {
                        blockLevel.add(IndentType.NONE);
                    }
                } else if (ts.token().id() == CssTokenId.RBRACE) {
                    IndentType it = blockLevel.removeLast();
                    if (it == IndentType.BLOCK) {
                        // Ensure, that there is a newline before an block closing
                        // brace "}"
                        if (LexerUtils.followsToken(ts, CssTokenId.NL, true, true, CssTokenId.WS, CssTokenId.COMMENT) == null) {
                            newlinesMissing.add(ts.offset());
                        }
                        // Ensure, that there is a newline after an block closing
                        // brace "}"
                        if (LexerUtils.followsToken(ts, CssTokenId.NL, false, true, CssTokenId.WS, CssTokenId.COMMENT) == null) {
                            while (ts.moveNext()) {
                                if (ts.token().id() != CssTokenId.WS && ts.token().id() != CssTokenId.COMMENT) {
                                    newlinesMissing.add(ts.offset());
                                    ts.movePrevious();
                                    break;
                                }
                            }
                        }
                    }
                } else if (ts.token().id() == CssTokenId.SEMI) {
                    // Ensure, that there is a newline after semikolons
                    // (between property definitions). This should only be done
                    // in regular CSS definitions (rules), but not in inline
                    // style definitions
                    if (!blockLevel.isEmpty() && LexerUtils.followsToken(ts, asList(CssTokenId.NL, CssTokenId.RBRACE), false, true, CssTokenId.WS, CssTokenId.COMMENT) == null) {
                        while (ts.moveNext()) {
                            if (ts.token().id() != CssTokenId.WS && ts.token().id() != CssTokenId.COMMENT) {
                                newlinesMissing.add(ts.offset());
                                ts.movePrevious();
                                break;
                            }
                        }
                    }
                }
                lastToken = ts.token();
            }
        }
        // Remove newline insertions if
        for (int i = 0; i < newlinesMissing.size() - 1; i++) {
            int currentPos = newlinesMissing.get(i);
            int nextPos = newlinesMissing.get(i + 1);
            String interText = context.document().getText(currentPos, nextPos - currentPos);
            if (interText.trim().isEmpty()) {
                newlinesMissing.remove(i + 1);
            }
        }
        Collections.reverse(newlinesMissing);
        for (int index : newlinesMissing) {
            if(isPositionInFormatRegions(index)) {
                context.document().insertString(index, "\n", null);
            }
        }
    }

    private static boolean isStringInterpolation(Token lastToken) {
        return lastToken != null && (lastToken.id() == CssTokenId.AT_SIGN || lastToken.id() == CssTokenId.HASH_SYMBOL);
    }

    private boolean isPositionInFormatRegions(int pos) {
        for(Region r: context.indentRegions()) {
            if(r.getStartOffset() <= pos && r.getEndOffset() > pos) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ExtraLock indentLock() {
        return null;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private static enum IndentType {
        NONE,
        BLOCK
    }
}
