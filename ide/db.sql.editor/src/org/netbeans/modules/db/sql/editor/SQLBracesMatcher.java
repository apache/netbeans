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
package org.netbeans.modules.db.sql.editor;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.db.sql.lexer.LexerUtilities;
import org.netbeans.modules.db.sql.lexer.SQLLexer;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Braces matcher for SQL files. See issue 155167.
 *
 * @author Matthias42
 */
public class SQLBracesMatcher implements BracesMatcher {

    @MimeRegistrations({
        @MimeRegistration(mimeType = "text/x-sql",
                service = BracesMatcherFactory.class, position = 50)
    })
    public static class Factory implements BracesMatcherFactory {

        @Override
        public SQLBracesMatcher createMatcher(MatcherContext context) {
            return new SQLBracesMatcher(context);
        }
    }
    private MatcherContext context;

    public SQLBracesMatcher(MatcherContext context) {
        this.context = context;
    }

    @Override
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        int[] ret = null;
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getSearchOffset();
            TokenSequence<? extends SQLTokenId> ts = LexerUtilities.getTokenSequence(doc, offset);

            if (ts != null) {
                ts.move(offset);

                if (ts.moveNext()) {

                    Token<? extends SQLTokenId> token = ts.token();

                    if (token != null) {
                        TokenId id = token.id();

                        if (id == SQLTokenId.LPAREN || id == SQLTokenId.RPAREN) {
                            ret = new int[]{ts.offset(), ts.offset() + token.length()};
                        } else if (id == SQLTokenId.IDENTIFIER || id == SQLTokenId.STRING) {
                            int startOffset = ts.offset();
                            int endOffset = ts.offset() + token.length() - 1;
                            char startChar = token.text().charAt(0);
                            char endChar = token.text().charAt(token.length() - 1);
                            if (offset == startOffset && SQLLexer.isEndIdentifierQuoteChar(startChar, endChar)) {
                                ret = new int[]{startOffset, startOffset + 1};
                            } else if (offset == endOffset && SQLLexer.isEndIdentifierQuoteChar(startChar, endChar)) {
                                ret = new int[]{endOffset, endOffset + 1};
                            } else if (startChar == '\'' && endChar == '\'') {
                                if (offset == startOffset) {
                                    ret = new int[]{startOffset, startOffset + 1};
                                } else if (offset == endOffset) {
                                    ret = new int[]{endOffset, endOffset + 1};
                                }
                            }
                        }
                    }
                }
            }

        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
        return ret;
    }

    @Override
    public int[] findMatches() throws InterruptedException, BadLocationException {
        int[] ret = null;
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getSearchOffset();
            TokenSequence<? extends SQLTokenId> ts = LexerUtilities.getTokenSequence(doc, offset);

            if (ts != null) {
                ts.move(offset);

                if (ts.moveNext()) {

                    Token<? extends SQLTokenId> token = ts.token();

                    if (token != null) {
                        TokenId id = token.id();

                        if (id == SQLTokenId.LPAREN) {
                            OffsetRange r = LexerUtilities.findFwd(ts, SQLTokenId.LPAREN.ordinal(),
                                    SQLTokenId.RPAREN.ordinal());
                            ret = new int[]{r.getStart(), r.getEnd()};
                        } else if (id == SQLTokenId.RPAREN) {
                            OffsetRange r = LexerUtilities.findBwd(ts, SQLTokenId.LPAREN.ordinal(),
                                    SQLTokenId.RPAREN.ordinal());
                            ret = new int[]{r.getStart(), r.getEnd()};
                        } else if (id == SQLTokenId.IDENTIFIER || id == SQLTokenId.STRING) {
                            int startOffset = ts.offset();
                            int endOffset = ts.offset() + token.length() - 1;
                            char startChar = token.text().charAt(0);
                            char endChar = token.text().charAt(token.length() - 1);
                            if (offset == startOffset && SQLLexer.isEndIdentifierQuoteChar(startChar, endChar)) {
                                ret = new int[]{endOffset, endOffset + 1};
                            } else if (offset == endOffset && SQLLexer.isEndIdentifierQuoteChar(startChar, endChar)) {
                                ret = new int[]{startOffset, startOffset + 1};
                            } else if (startChar == '\'' && endChar == '\'') {
                                if (offset == startOffset) {
                                    ret = new int[]{endOffset, endOffset + 1};
                                } else if (offset == endOffset) {
                                    ret = new int[]{startOffset, startOffset + 1};
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
        return ret;
    }
}
