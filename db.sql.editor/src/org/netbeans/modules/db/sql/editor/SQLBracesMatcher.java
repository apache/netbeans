/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
