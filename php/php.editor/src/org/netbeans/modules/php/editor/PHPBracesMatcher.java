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

package org.netbeans.modules.php.editor;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.editor.bracesmatching.BraceContext;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Implementation of BracesMatcher interface for PHP. It is based on original code
 * from PHPBracketCompleter.findMatching
 *
 * @author Marek Slama
 */
public final class PHPBracesMatcher implements BracesMatcher, BracesMatcher.ContextLocator {

    private static final Logger LOGGER = Logger.getLogger(PHPBracesMatcher.class.getName());

    private final MatcherContext context;

    private boolean findBackward;
    private int originOffset;
    private int matchingOffset;
    private String matchingText;

    public PHPBracesMatcher(MatcherContext context) {
        this.context = context;
    }

    @Override
    public int [] findOrigin() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getSearchOffset();

            TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);

            if (ts != null) {
                // #240157
                if (searchForward(ts, offset)){
                    offset--;
                    if (offset < 0) {
                        return null;
                    }
                }

                ts.move(offset);

                if (!ts.moveNext()) {
                    return null;
                }

                Token<?extends PHPTokenId> token = ts.token();

                if (token == null) {
                    return null;
                }

                TokenId id = token.id();

                originOffset = ts.offset();
                if (LexUtilities.textEquals(token.text(), '(')) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (LexUtilities.textEquals(token.text(), ')')) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (id == PHPTokenId.PHP_CURLY_OPEN) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (id == PHPTokenId.PHP_CURLY_CLOSE) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (LexUtilities.textEquals(token.text(), '[')) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (LexUtilities.textEquals(token.text(), ']')) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (LexUtilities.textEquals(token.text(), '$', '{')) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (LexUtilities.textEquals(token.text(), '#', '[')) { // [NETBEANS-4443] PHP 8.0
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (LexUtilities.textEquals(token.text(), ':')) {
                    do {
                        ts.movePrevious();
                        token = LexUtilities.findPreviousToken(ts,
                                Arrays.asList(PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF,
                                PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_WHILE, PHPTokenId.PHP_SWITCH,
                                PHPTokenId.PHP_OPENTAG, PHPTokenId.PHP_CURLY_CLOSE, PHPTokenId.PHP_CASE,
                                PHPTokenId.PHP_TOKEN));
                        id = token.id();
                    } while (id == PHPTokenId.PHP_TOKEN && !TokenUtilities.textEquals(token.text(), ":")); // NOI18N
                    if (id == PHPTokenId.PHP_IF || id == PHPTokenId.PHP_ELSE || id == PHPTokenId.PHP_ELSEIF
                            || id == PHPTokenId.PHP_FOR || id == PHPTokenId.PHP_FOREACH || id == PHPTokenId.PHP_WHILE
                            || id == PHPTokenId.PHP_SWITCH) {
                        ts.move(offset);
                        ts.moveNext();
                        token = ts.token();
                        return new int [] {ts.offset(), ts.offset() + token.length()};
                    }
                } else if (id == PHPTokenId.PHP_ENDFOR || id == PHPTokenId.PHP_ENDFOREACH
                        || id == PHPTokenId.PHP_ENDIF || id == PHPTokenId.PHP_ENDSWITCH
                        || id == PHPTokenId.PHP_ENDWHILE) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (id == PHPTokenId.PHP_ELSEIF || id == PHPTokenId.PHP_ELSE) {
                    while (token.id() != PHPTokenId.PHP_CURLY_OPEN && !":".equals(token.text().toString()) && ts.moveNext()) {
                            token = LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN, PHPTokenId.PHP_CURLY_OPEN));
                    }
                    if (token.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(token.text(), ":") && ts.moveNext()) { // NOI18N
                        ts.move(offset);
                        ts.moveNext();
                        token = ts.token();
                        return new int [] {ts.offset(), ts.offset() + token.length()};
                    }
                } else {
                    originOffset = -1;
                }

            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    @Override
    public int [] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getSearchOffset();

            TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);

            if (ts != null) {
                // #240157
                if (searchForward(ts, offset)){
                    offset--;
                    if (offset < 0) {
                        return null;
                    }
                }

                ts.move(offset);

                if (!ts.moveNext()) {
                    return null;
                }

                Token<?extends PHPTokenId> token = ts.token();

                if (token == null) {
                    return null;
                }

                TokenId id = token.id();

                OffsetRange r = null;
                matchingText = ""; // NOI18N
                findBackward = false;
                try {
                    if (LexUtilities.textEquals(token.text(), '(')) {
                        matchingText = ")"; // NOI18N
                        r = LexUtilities.findFwd(doc, ts, PHPTokenId.PHP_TOKEN, '(', PHPTokenId.PHP_TOKEN, ')');
                        return new int [] {r.getStart(), r.getEnd() };
                    } else if (LexUtilities.textEquals(token.text(), ')')) {
                        findBackward = true;
                        matchingText = "("; // NOI18N
                        r = LexUtilities.findBwd(doc, ts, PHPTokenId.PHP_TOKEN, '(', PHPTokenId.PHP_TOKEN, ')');
                        return new int [] {r.getStart(), r.getEnd() };
                    } else if (id == PHPTokenId.PHP_CURLY_OPEN) {
                        matchingText = "}"; // NOI18N
                        r = LexUtilities.findFwd(doc, ts, PHPTokenId.PHP_CURLY_OPEN, '{', PHPTokenId.PHP_CURLY_CLOSE, '}');
                        return new int [] {r.getStart(), r.getEnd() };
                    } else if (id == PHPTokenId.PHP_CURLY_CLOSE) {
                        findBackward = true;
                        r = LexUtilities.findBwd(doc, ts, PHPTokenId.PHP_CURLY_OPEN, '{', PHPTokenId.PHP_CURLY_CLOSE, '}');
                        matchingText = r.getLength() == 1 ? "{" : "${"; // NOI18N
                        return new int [] {r.getStart(), r.getEnd() };
                    } else if (LexUtilities.textEquals(token.text(), '[')) {
                        matchingText = "]"; // NOI18N
                        r = LexUtilities.findFwd(doc, ts, PHPTokenId.PHP_TOKEN, '[', PHPTokenId.PHP_TOKEN, ']');
                        return new int [] {r.getStart(), r.getEnd() };
                    } else if (LexUtilities.textEquals(token.text(), ']')) {
                        findBackward = true;
                        r = LexUtilities.findBwd(doc, ts, PHPTokenId.PHP_TOKEN, '[', PHPTokenId.PHP_TOKEN, ']');
                        matchingText = r.getLength() == 1 ? "[" : "#["; // NOI18N
                        return new int[]{r.getStart(), r.getEnd()};
                    } else if (LexUtilities.textEquals(token.text(), '$', '{')) {
                        matchingText = "}"; // NOI18N
                        r = LexUtilities.findFwd(doc, ts, PHPTokenId.PHP_TOKEN, '{', PHPTokenId.PHP_CURLY_CLOSE, '}');
                        return new int[]{r.getStart(), r.getEnd()};
                    } else if (LexUtilities.textEquals(token.text(), '#', '[')) { // attribute
                        matchingText = "]"; // NOI18N
                        r = LexUtilities.findFwd(doc, ts, PHPTokenId.PHP_TOKEN, '[', PHPTokenId.PHP_TOKEN, ']');
                        return new int[]{r.getStart(), r.getEnd()};
                    } else if (LexUtilities.textEquals(token.text(), ':')) {
                        r = LexUtilities.findFwdAlternativeSyntax(doc, ts, token);
                        Token<? extends PHPTokenId> t = ts.token();
                        matchingText = t == null ? "" : t.text().toString(); // NOI18N
                        return new int [] {r.getStart(), r.getEnd() };
                    } else if (id == PHPTokenId.PHP_ENDFOR || id == PHPTokenId.PHP_ENDFOREACH
                            || id == PHPTokenId.PHP_ENDIF || id == PHPTokenId.PHP_ENDSWITCH
                            || id == PHPTokenId.PHP_ENDWHILE || id == PHPTokenId.PHP_ELSEIF
                            || id == PHPTokenId.PHP_ELSE) {
                        findBackward = true;
                        r = LexUtilities.findBwdAlternativeSyntax(doc, ts, token);
                        matchingText = ":"; // NOI18N
                        return new int [] {r.getStart(), r.getEnd() };
                    }
                } finally {
                    matchingOffset = r != null ? r.getStart() : -1;
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    private boolean searchForward(TokenSequence<? extends PHPTokenId> ts, int offset) {
        // if there is a brace token just before a caret position, search foward
        // e.g. if (isSomething()^), if (isSomething())^{
        // "^" is the caret
        if (context.isSearchingBackward()) {
            ts.move(offset);
            if (ts.movePrevious()) {
                Token<? extends PHPTokenId> previousToken = ts.token();
                if (previousToken != null && isBraceToken(previousToken)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isBraceToken(Token<? extends PHPTokenId> token) {
        PHPTokenId id = token.id();
        return LexUtilities.textEquals(token.text(), '(') // NOI18N
                || LexUtilities.textEquals(token.text(), ')') // NOI18N
                || id == PHPTokenId.PHP_CURLY_OPEN
                || id == PHPTokenId.PHP_CURLY_CLOSE
                || LexUtilities.textEquals(token.text(), '[') // NOI18N
                || LexUtilities.textEquals(token.text(), ']') // NOI18N
                || LexUtilities.textEquals(token.text(), '$', '{') // NOI18N
                || LexUtilities.textEquals(token.text(), '#', '[') // [NETBEANS-4443] PHP 8.0
                || LexUtilities.textEquals(token.text(), ':') // NOI18N
                || id == PHPTokenId.PHP_ENDFOR
                || id == PHPTokenId.PHP_ENDFOREACH
                || id == PHPTokenId.PHP_ENDIF
                || id == PHPTokenId.PHP_ENDSWITCH
                || id == PHPTokenId.PHP_ENDWHILE
                || id == PHPTokenId.PHP_ELSEIF
                || id == PHPTokenId.PHP_ELSE;
    }

    @Override
    public BraceContext findContext(int originOrMatchPosition) {
        if (findBackward && (matchingText.equals("{") || matchingText.equals(":"))) { // NOI18N
            if (originOffset != originOrMatchPosition) {
                return null;
            }
            try {
                return findContextBackwards();
            } catch (BadLocationException ex) {
                LOGGER.log(Level.WARNING, "incorrect offset: " + ex.offsetRequested(), ex);
            }
        }
        return null;
    }

    private BraceContext findContextBackwards() throws BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, matchingOffset);
            if (ts == null) {
                return null;
            }
            ts.move(matchingOffset);
            if (!ts.moveNext()) {
                return null;
            }
            List<PHPTokenId> lookfor = Arrays.asList(
                    PHPTokenId.PHP_CURLY_OPEN, // terminator e.g. ${a} GH-7124
                    PHPTokenId.PHP_CURLY_CLOSE, // terminator
                    PHPTokenId.PHP_CLASS, PHPTokenId.PHP_INTERFACE, PHPTokenId.PHP_TRAIT, PHPTokenId.PHP_FUNCTION,
                    PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH,
                    PHPTokenId.PHP_DO, PHPTokenId.PHP_WHILE,
                    PHPTokenId.PHP_TRY, PHPTokenId.PHP_CATCH, PHPTokenId.PHP_FINALLY,
                    PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF,
                    PHPTokenId.PHP_SWITCH, PHPTokenId.PHP_USE, PHPTokenId.PHP_MATCH, PHPTokenId.PHP_ENUM
            );
            if (!ts.movePrevious()) {
                // consume the current token("{" or ":")
                return null;
            }
            Token<? extends PHPTokenId> previousToken = LexUtilities.findPreviousToken(ts, lookfor);
            if (previousToken == null
                    || previousToken.id() == PHPTokenId.PHP_CURLY_OPEN
                    || previousToken.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                return null;
            }

            PHPTokenId id = previousToken.id();
            switch (id) {
                case PHP_ELSE:
                    return getBraceContextForIfStatement(ts);
                default:
                    return getBraceContext(ts.offset());
            }
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    private BraceContext getBraceContextForIfStatement(TokenSequence<? extends PHPTokenId> ts) throws BadLocationException {
        // find "if"
        int elseStart = ts.offset();
        if (elseStart < 0 || matchingOffset < elseStart) {
            return null;
        }
        int balance = 0;
        int ifStart = -1;
        int ifEnd = -1;
        String lastBrace = null;
        boolean found = false;
        boolean isAlternative = ":".equals(matchingText); // NOI18N
        while(ts.movePrevious()) {
            Token<? extends PHPTokenId> token = ts.token();
            PHPTokenId id = token.id();
            switch (id) {
                case PHP_ENDIF:
                    if (isAlternative) {
                        balance++;
                    }
                    break;
                case PHP_ELSEIF: // fall-through
                case PHP_IF:
                    if (matchingText.equals(lastBrace)) {
                        if (balance == 0) {
                            ifStart = ts.offset();
                            found = true;
                        }
                        if (isAlternative && id == PHPTokenId.PHP_IF) {
                            balance--;
                        }
                    }
                    break;
                case PHP_CURLY_CLOSE:
                    if (!isAlternative) {
                        balance++;
                    }
                    break;
                case PHP_CURLY_OPEN:
                    if (!isAlternative) {
                        balance--;
                        ifEnd = ts.offset();
                    }
                    lastBrace = token.text().toString();
                    break;
                case PHP_TOKEN:
                    if (isColon(token)) {
                        if (isAlternative) {
                            ifEnd = ts.offset();
                        }
                        lastBrace = token.text().toString();
                    }
                    if (isComplexSyntaxOpen(token)) {
                        if (!isAlternative) {
                            balance--;
                        }
                        lastBrace = token.text().toString();
                    }
                    break;
                default:
                    break;
            }
            if (found) {
                break;
            }
        }
        if (!found || ifStart == -1 || ifEnd == -1) {
            // broken code
            return getBraceContext(elseStart);
        }
        BraceContext braceContext = BraceContext.create(
                context.getDocument().createPosition(ifStart),
                context.getDocument().createPosition(ifEnd + 1) // + "{" or ":"
        );
        return braceContext.createRelated(
                context.getDocument().createPosition(elseStart),
                context.getDocument().createPosition(matchingOffset + 1) // + "{" or ":"
        );
    }

    private BraceContext getBraceContext(int start) throws BadLocationException {
        return BraceContext.create(
                context.getDocument().createPosition(start),
                context.getDocument().createPosition(matchingOffset + 1) // + "{" or ":"
        );
    }

    private static boolean isColon(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(token.text(), ":"); // NOI18N
    }

    private static boolean isComplexSyntaxOpen(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(token.text(), "${"); // NOI18N
    }

}
