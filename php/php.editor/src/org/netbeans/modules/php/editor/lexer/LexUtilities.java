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
package org.netbeans.modules.php.editor.lexer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;


/**
 * Utilities associated with lexing or analyzing the document at the
 * lexical level, unlike AstUtilities which is contains utilities
 * to analyze parsed information about a document.
 *
 * @author Tor Norbye
 * @author Petr Pisl
 */
public final class LexUtilities {

    private LexUtilities() {
    }

    /**
     *
     *
     * @param doc
     * @param offset
     * @param runUnderLock Runs under it's own document readlock if true
     * @return Most embedded TokenSequence on the given offset. If there is no
     * embedding, returns the top level sequence. The TokenSequence is not positioned!
     */
    public static TokenSequence<? extends TokenId> getMostEmbeddedTokenSequence(final Document doc, final int offset, boolean runUnderLock) {
        final AtomicReference<TokenSequence<? extends TokenId>> ref = new AtomicReference<>();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                TokenHierarchy th = TokenHierarchy.get(doc);
                List<TokenSequence<? extends TokenId>> sequences = th.embeddedTokenSequences(offset, false);
                if (sequences.isEmpty()) {
                    //no embedding, return top level sequence;
                    ref.set(th.tokenSequence());
                } else {
                    ref.set(sequences.get(sequences.size() - 1)); //return the most embedded one
                }
            }
        };

        if (runUnderLock) {
            doc.render(r);
        } else {
            r.run();
        }

        return ref.get();
    }


    @SuppressWarnings("unchecked")
    @CheckForNull
    public static TokenSequence<PHPTokenId> getPHPTokenSequence(Document doc, int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(doc);
        return getPHPTokenSequence(th, offset);
    }

    public static TokenSequence<PHPTokenId> getPHPTokenSequence(TokenHierarchy<?> th, int offset) {
        TokenSequence<PHPTokenId> ts = th == null ? null : th.tokenSequence(PHPTokenId.language());
        if (ts == null) {
            // Possibly an embedding scenario such as an RHTML file
            // First try with backward bias true
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);
            for (TokenSequence t : list) {
                if (t.language() == PHPTokenId.language()) {
                    ts = t;
                    break;
                }
            }
            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);
                for (TokenSequence t : list) {
                    if (t.language() == PHPTokenId.language()) {
                        ts = t;
                        break;
                    }
                }
            }
        }
        return ts;
    }

    public static TokenSequence<?extends PHPTokenId> getPositionedSequence(BaseDocument doc, int offset) {
        TokenSequence<?extends PHPTokenId> ts = getPHPTokenSequence(doc, offset);

        if (ts != null) {
            try {
                ts.move(offset);
            } catch (AssertionError e) {
                DataObject dobj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);

                if (dobj != null) {
                    Exceptions.attachMessage(e, FileUtil.getFileDisplayName(dobj.getPrimaryFile()));
                }

                throw e;
            }

            if (!ts.moveNext() && !ts.movePrevious()) {
                return null;
            }

            return ts;
        }

        return null;
    }


    public static Token<?extends PHPTokenId> getToken(BaseDocument doc, int offset) {
        TokenSequence<?extends PHPTokenId> ts = getPositionedSequence(doc, offset);

        if (ts != null) {
            return ts.token();
        }

        return null;
    }

    public static char getTokenChar(BaseDocument doc, int offset) {
        Token<?extends PHPTokenId> token = getToken(doc, offset);

        if (token != null) {
            if (token.text().length() > 0) { // Usually true, but I could have gotten EOF right?
                return token.text().charAt(0);
            }
        }

        return 0;
    }

    /* Search forwards in the token sequence until a token of type <code>down</code> is found */
    public static OffsetRange findFwd(BaseDocument doc, TokenSequence<?extends PHPTokenId> ts, PHPTokenId tokenUpId, char up, PHPTokenId tokenDownId, char down) {
        int balance = 0;

        while (ts.moveNext()) {
            Token<?extends PHPTokenId> token = ts.token();

            if ((token.id() == tokenUpId && textEquals(token.text(), up))
                    || (tokenUpId == PHPTokenId.PHP_CURLY_OPEN && token.id() == PHPTokenId.PHP_TOKEN && token.text().charAt(token.text().length() - 1) == '{')
                    || (up == '[' && token.id() == PHPTokenId.PHP_ATTRIBUTE)) /* #[ */ {
                balance++;
            } else if (token.id() == tokenDownId && textEquals(token.text(), down)) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance--;
            }
        }

        return OffsetRange.NONE;
    }

    /* Search backwards in the token sequence until a token of type <code>up</code> is found */
    public static OffsetRange findBwd(BaseDocument doc, TokenSequence<?extends PHPTokenId> ts, PHPTokenId tokenUpId, char up, PHPTokenId tokenDownId, char down) {
        int balance = 0;

        while (ts.movePrevious()) {
            Token<?extends PHPTokenId> token = ts.token();
            TokenId id = token.id();

            if (token.id() == tokenUpId && textEquals(token.text(), up)
                    || (tokenUpId == PHPTokenId.PHP_CURLY_OPEN && token.id() == PHPTokenId.PHP_TOKEN && token.text().charAt(token.text().length() - 1) == '{')
                    || (up == '[' && token.id() == PHPTokenId.PHP_ATTRIBUTE)) /* #[ */ {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance++;
            } else if (token.id() == tokenDownId && textEquals(token.text(), down)) {
                balance--;
            }
        }

        return OffsetRange.NONE;
    }

    public static OffsetRange findFwdAlternativeSyntax(BaseDocument doc, TokenSequence<?extends PHPTokenId> ts, Token<?extends PHPTokenId> upToken) {
        int balance = 0;
        Token<?extends PHPTokenId> beginToken = LexUtilities.findPreviousToken(ts,
                Arrays.asList(PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF,
                PHPTokenId.PHP_WHILE, PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH,
                PHPTokenId.PHP_SWITCH, PHPTokenId.PHP_CASE));

        PHPTokenId beginTokenId = beginToken.id();

        if (beginTokenId == PHPTokenId.PHP_ELSE || beginTokenId == PHPTokenId.PHP_ELSEIF) {
            beginTokenId = PHPTokenId.PHP_IF;
        }

        List<PHPTokenId> possibleEnd;
        if (beginTokenId == PHPTokenId.PHP_IF) {
            possibleEnd = Arrays.asList(PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF, PHPTokenId.PHP_ENDIF);
        } else if (beginTokenId == PHPTokenId.PHP_WHILE) {
            possibleEnd = Arrays.asList(PHPTokenId.PHP_WHILE, PHPTokenId.PHP_ENDWHILE);
        } else if (beginTokenId == PHPTokenId.PHP_FOR) {
            possibleEnd = Arrays.asList(PHPTokenId.PHP_FOR, PHPTokenId.PHP_ENDFOR);
        } else if (beginTokenId == PHPTokenId.PHP_FOREACH) {
            possibleEnd = Arrays.asList(PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_ENDFOREACH);
        } else if (beginTokenId == PHPTokenId.PHP_SWITCH) {
            possibleEnd = Arrays.asList(PHPTokenId.PHP_SWITCH, PHPTokenId.PHP_ENDSWITCH);
        } else if (beginTokenId == PHPTokenId.PHP_CASE) {
            possibleEnd = Arrays.asList(PHPTokenId.PHP_BREAK, PHPTokenId.PHP_ENDSWITCH);
        } else {
            return OffsetRange.NONE;
        }

        while (ts.moveNext()) {
            Token<?extends PHPTokenId> token = LexUtilities.findNextToken(ts, possibleEnd);

            if (token.id() == beginTokenId) {
                balance++;
            } else if (possibleEnd.contains(token.id())) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }
                if (beginTokenId != PHPTokenId.PHP_IF || (beginTokenId == PHPTokenId.PHP_IF && token.id() == PHPTokenId.PHP_ENDIF)) {
                    balance--;
                }
            }
        }

        return OffsetRange.NONE;
    }


    public static OffsetRange findBwdAlternativeSyntax(BaseDocument doc, TokenSequence<?extends PHPTokenId> ts, Token<?extends PHPTokenId> downToken) {

        int balance = 0;

        PHPTokenId endTokenId = downToken.id();

        if (endTokenId == PHPTokenId.PHP_ELSE || endTokenId == PHPTokenId.PHP_ELSEIF) {
            endTokenId = PHPTokenId.PHP_ENDIF;
        }

        List<PHPTokenId> possibleBegin;
        if (endTokenId == PHPTokenId.PHP_ENDIF) {
            possibleBegin = Arrays.asList(PHPTokenId.PHP_TOKEN, PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF, PHPTokenId.PHP_ENDIF);
        } else if (endTokenId == PHPTokenId.PHP_ELSE) {
            possibleBegin = Arrays.asList(PHPTokenId.PHP_TOKEN, PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF, PHPTokenId.PHP_ENDIF);
        } else if (endTokenId == PHPTokenId.PHP_ENDWHILE) {
            possibleBegin = Arrays.asList(PHPTokenId.PHP_TOKEN, PHPTokenId.PHP_WHILE, PHPTokenId.PHP_ENDWHILE);
        } else if (endTokenId == PHPTokenId.PHP_ENDFOR) {
            possibleBegin = Arrays.asList(PHPTokenId.PHP_TOKEN, PHPTokenId.PHP_FOR, PHPTokenId.PHP_ENDFOR);
        } else if (endTokenId == PHPTokenId.PHP_ENDFOREACH) {
            possibleBegin = Arrays.asList(PHPTokenId.PHP_TOKEN, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_ENDFOREACH);
        } else if (endTokenId == PHPTokenId.PHP_ENDSWITCH) {
            possibleBegin = Arrays.asList(PHPTokenId.PHP_TOKEN, PHPTokenId.PHP_SWITCH, PHPTokenId.PHP_ENDSWITCH);
        } else if (endTokenId == PHPTokenId.PHP_BREAK) {
            possibleBegin = Arrays.asList(PHPTokenId.PHP_TOKEN, PHPTokenId.PHP_BREAK, PHPTokenId.PHP_ENDSWITCH);
        } else {
            return OffsetRange.NONE;
        }


        int columnOffset = 0;
        while (ts.movePrevious()) {
            Token<?extends PHPTokenId> token = LexUtilities.findPreviousToken(ts, possibleBegin);

            if (token.id() == PHPTokenId.PHP_TOKEN) {
                if (TokenUtilities.textEquals(token.text(), ":")) { // NOI18N
                    columnOffset = ts.offset();
                }
            } else if (token.id() == endTokenId) {
                balance--;
            } else {
                if (balance == 0) {
                    return new OffsetRange(columnOffset, columnOffset + 1);
                }
                if (endTokenId != PHPTokenId.PHP_ENDIF || (endTokenId == PHPTokenId.PHP_ENDIF && token.id() == PHPTokenId.PHP_IF)) {
                    balance++;
                }
            }
        }
        return OffsetRange.NONE;
    }

    /** Find the token that begins a block terminated by "end". This is a token
     * in the END_PAIRS array. Walk backwards and find the corresponding token.
     * It does not use indentation for clues since this could be wrong and be
     * precisely the reason why the user is using pair matching to see what's wrong.
     */
    public static OffsetRange findBegin(BaseDocument doc, TokenSequence<?extends PHPTokenId> ts) {
        return OffsetRange.NONE;
    }

    /* Compute the balance of begin/end tokens on the line */
    public static int getLineBalance(BaseDocument doc, int offset, TokenId up, TokenId down, LineBalance lineBalance) {
        try {
            int begin = LineDocumentUtils.getLineStart(doc, offset);
            int end = LineDocumentUtils.getLineEnd(doc, offset);

            TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, begin);
            if (ts == null) {
                return 0;
            }

            ts.move(begin);

            if (!ts.moveNext()) {
                return 0;
            }

            int upCount = 0;
            int downCount = 0;

            do {
                Token<?extends PHPTokenId> token = ts.token();
                TokenId id = token.id();

                if (id == up) {
                    if (lineBalance.equals(LineBalance.DOWN_FIRST)) {
                        if (upCount > 0) {
                            upCount++;
                        }
                    } else {
                        upCount++;
                    }
                } else if (id == down) {
                    if (lineBalance.equals(LineBalance.UP_FIRST)) {
                        if (upCount > 0) {
                            downCount++;
                        }
                    } else {
                        downCount++;
                    }
                }
            } while (ts.moveNext() && (ts.offset() <= end));

            return (upCount - downCount);
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);

            return 0;
        }
    }

    public static enum LineBalance {

        PLAIN,
        UP_FIRST, // } keyword {
        DOWN_FIRST
    };

    /**
     * The same as braceBalance but generalized to any pair of matching
     * tokens.
     * @param open the token that increses the count
     * @param close the token that decreses the count
     */
    public static int getTokenBalance(BaseDocument doc, char open, char close, int offset) throws BadLocationException {
        TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, 0);
        if (ts == null) {
            return 0;
        }

        // XXX Why 0? Why not offset?
        ts.moveIndex(0);

        if (!ts.moveNext()) {
            return 0;
        }

        int balance = 0;

        do {
            Token t = ts.token();

            if (textEquals(t.text(), open)) {
                balance++;
            } else if (textEquals(t.text(), close)) {
                balance--;
            }
        } while (ts.moveNext());

        return balance;
    }

    /**
     * Return true iff the line for the given offset is a JavaScript comment line.
     * This will return false for lines that contain comments (even when the
     * offset is within the comment portion) but also contain code.
     */
    public static boolean isCommentOnlyLine(BaseDocument doc, int offset)
        throws BadLocationException {
        int begin = Utilities.getRowFirstNonWhite(doc, offset);

        if (begin == -1) {
            return false; // whitespace only
        }

        Token<? extends PHPTokenId> token = LexUtilities.getToken(doc, begin);
        if (token != null) {
            return token.id() == PHPTokenId.PHP_LINE_COMMENT;
        }

        return false;
    }

    public static boolean textEquals(CharSequence text1, char... text2) {
        int len = text1.length();
        if (len == text2.length) {
            for (int i = len - 1; i >= 0; i--) {
                if (text1.charAt(i) != text2[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static Token<?extends PHPTokenId> findNext(TokenSequence<?extends PHPTokenId> ts, List<PHPTokenId> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.moveNext() && ignores.contains(ts.token().id())) {
            }
        }
        return ts.token();
    }

    public static Token<?extends PHPTokenId> findPrevious(TokenSequence<?extends PHPTokenId> ts, List<PHPTokenId> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.movePrevious() && ignores.contains(ts.token().id())) {
            }
        }
        return ts.token();
    }

    public static Token<?extends PHPTokenId> findNextToken(TokenSequence<?extends PHPTokenId> ts, List<PHPTokenId> lookfor) {
        if (!lookfor.contains(ts.token().id())) {
            while (ts.moveNext() && !lookfor.contains(ts.token().id())) {
            }
        }
        return ts.token();
    }

    public static Token<?extends PHPTokenId> findPreviousToken(TokenSequence<?extends PHPTokenId> ts, List<PHPTokenId> lookfor) {
        if (!lookfor.contains(ts.token().id())) {
            while (ts.movePrevious() && !lookfor.contains(ts.token().id())) {
            }
        }
        return ts.token();
    }

    /**
     * The method returns the last token on the line.
     * @param ts
     * @return
     */
    public static Token<?extends PHPTokenId> findEndOfLine(TokenSequence<?extends PHPTokenId> ts) {
        do {
            Token<?extends PHPTokenId> token = findNextToken(ts,
                    Arrays.asList(PHPTokenId.WHITESPACE, PHPTokenId.PHP_LINE_COMMENT));
            for (int i = token.text().length() - 1; i > -1; i--) {
                if (token.text().charAt(i) == '\n') {
                    return token;
                }
            }
        } while (ts.moveNext());
        return ts.token();
    }

    public static int findStartTokenOfExpression(TokenSequence ts) {
        int start = -1;
        int origOffset = ts.offset();

        Token token;
        int balance = 0;
        int curlyBalance = 0;
        boolean isInQuotes = false; // GH-6731 for checking a variable in string
        do {
            token = ts.token();
            if (token.id() == PHPTokenId.PHP_TOKEN) {
                switch (token.text().charAt(0)) {
                    case ')':
                        balance--;
                        break;
                    case '(':
                        balance++;
                        break;
                    default:
                        //no-op
                }
            } else if (token.id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING) {
                // GH-6731 for checking a variable in string
                // e.g. "example {$example}"
                if ((token.text().length() == 1 && TokenUtilities.textEquals(token.text(), "\"")) // NOI18N
                        || (!TokenUtilities.startsWith(token.text(), "\"") && TokenUtilities.endsWith(token.text(), "\"")) // NOI18N
                        || (TokenUtilities.startsWith(token.text(), "\"") && !TokenUtilities.endsWith(token.text(), "\""))) { // NOI18N
                    isInQuotes = !isInQuotes;
                }
            } else if ((token.id() == PHPTokenId.PHP_SEMICOLON || token.id() == PHPTokenId.PHP_OPENTAG)
                    && ts.moveNext()) {
                // we found previous end of expression => find begin of the current.
                LexUtilities.findNext(ts, Arrays.asList(
                        PHPTokenId.WHITESPACE,
                        PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                        PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                        PHPTokenId.PHP_LINE_COMMENT));
                start = ts.offset();
                break;
            } else if (token.id() == PHPTokenId.PHP_IF) {
                // we are at a beginning of if .... withouth curly?
                // need to find end of the condition.
                int offsetIf = ts.offset(); // remember the if offset
                token = LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN));
                if (ts.offset() < origOffset && token.text().charAt(0) == '(') {
                    // we have the start of the condition and now find the end
                    int parentBalance = 1;
                    while (start == -1 && parentBalance > 0 && ts.offset() < origOffset && ts.moveNext()) {
                        token = LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN));
                        if (token.text().charAt(0) == '(') {
                            parentBalance++;
                        } else if (token.text().charAt(0) == ')') {
                            parentBalance--;
                        }
                    }
                    if (parentBalance == 0 && ts.moveNext() && ts.offset() < origOffset) {
                        start = offsetIf;
                        break;
                    } else if (parentBalance > 0) {
                        // probably we are in a function in the condition
                        // and we need to find a line where is the function invocation
                        parentBalance = 0;
                        while (parentBalance < 1 && ts.offset() > offsetIf && ts.movePrevious()) {
                            token = LexUtilities.findPreviousToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN));
                            if (token.text().charAt(0) == '(') {
                                parentBalance++;
                            } else if (token.text().charAt(0) == ')') {
                                parentBalance--;
                            }
                        }
                        if (parentBalance == 1 && ts.movePrevious()) {
                            LexUtilities.findPrevious(ts, Arrays.asList(
                                    PHPTokenId.WHITESPACE,
                                    PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                                    PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                                    PHPTokenId.PHP_LINE_COMMENT));
                            start = ts.offset();
                        }
                        break;
                    } else if (parentBalance == 0) {
                        // before the end of condition
                        start = offsetIf;
                        break;
                    }
                } else {
                    ts.move(offsetIf);
                    ts.movePrevious();
                }
            } else if (token.id() == PHPTokenId.PHP_CASE || token.id() == PHPTokenId.PHP_DEFAULT) {
                start = ts.offset();
                break;
            } else if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                curlyBalance--;
                if (!isInQuotes && curlyBalance == -1 && ts.moveNext()) {
                    // we are after previous blog close
                    LexUtilities.findNext(ts, Arrays.asList(
                            PHPTokenId.WHITESPACE,
                            PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                            PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                            PHPTokenId.PHP_LINE_COMMENT));
                    if (ts.offset() <= origOffset) {
                        start = ts.offset();
                    } else {
                        start = origOffset;
                    }
                    break;
                }
            } else if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                curlyBalance++;
                if (!isInQuotes && curlyBalance == 1 && ts.moveNext()) {
                    // we are at the begining of a blog
                    LexUtilities.findNext(ts, Arrays.asList(
                            PHPTokenId.WHITESPACE,
                            PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                            PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                            PHPTokenId.PHP_LINE_COMMENT));
                    if (ts.offset() <= origOffset) {
                        start = ts.offset();
                    } else {
                        start = origOffset;
                    }
                    break;
                }
            } else if (balance == 1 && token.id() == PHPTokenId.PHP_STRING) {
                // probably there is a function call insede the expression
                start = ts.offset();
                break;
            }
        } while (ts.movePrevious());

        if (!ts.movePrevious()) {
            // we are at the first php line
            LexUtilities.findNext(ts, Arrays.asList(
                    PHPTokenId.WHITESPACE,
                    PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                    PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                    PHPTokenId.PHP_LINE_COMMENT, PHPTokenId.PHP_OPENTAG));
            start = ts.offset();
        }

        ts.move(origOffset);
        ts.moveNext();

        return start;
    }

    /**
     * Check whether the token id is an operator(PHP_OPERATOR or
     * PHP_TEXTUAL_OPERATOR). PHP_TEXTUAL_OPERATOR is "AND", "OR", or "XOR".
     *
     * @param id the token id
     * @return {@code true} the token id is an operator, otherwise {@code false}
     */
    public static boolean isPHPOperator(PHPTokenId id) {
        return id == PHPTokenId.PHP_OPERATOR || id == PHPTokenId.PHP_TEXTUAL_OPERATOR;
    }

}
