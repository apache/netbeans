/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.source.lexer;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.python.source.PythonParserResult;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;

/**
 * Utility functions around the Python lexer
 * 
 */
public class PythonLexerUtils {
    /**
     * Try to produce a more accurate location for the given name in the given import statement, located
     * at the lexRange provided
     */
    public static OffsetRange getImportNameOffset(BaseDocument doc, OffsetRange lexRange, PythonTree node, String name) {
        int docLength = doc.getLength();
        int start = Math.min(docLength, lexRange.getStart());
        int end = Math.min(docLength, lexRange.getEnd());
        try {
            String s = doc.getText(start, end - start);

            Pattern p = Pattern.compile(".*import\\s+\\b(" + name + ")\\b.*");
            Matcher m = p.matcher(s);
            if (m.matches()) {
                int offset = start + m.start(1);
                return new OffsetRange(offset, offset + name.length());
            }

            // Lame
            int searchIndex = s.indexOf("import ");
            if (searchIndex == -1) {
                searchIndex = 0;
            } else {
                searchIndex += 7;
            }
            int match = s.indexOf(name, searchIndex + 7);
            if (match != -1) {
                int offset = start + match;
                return new OffsetRange(offset, offset + name.length());
            }

        // Give up - use the whole range
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return lexRange;
    }

    /** For a possibly generated offset in an AST, return the corresponding lexing/true document offset */
    public static int getLexerOffset(PythonParserResult result, int astOffset) {
        return result.getSnapshot().getOriginalOffset(astOffset);
    }

    public static OffsetRange getLexerOffsets(PythonParserResult result, OffsetRange astRange) {
        if (result != null) {
            int rangeStart = astRange.getStart();
            int start = result.getSnapshot().getOriginalOffset(rangeStart);
            if (start == rangeStart) {
                return astRange;
            } else if (start == -1) {
                return OffsetRange.NONE;
            } else {
                // Assumes the translated range maintains size
                return new OffsetRange(start, start + astRange.getLength());
            }
        }

        return astRange;
    }

    /**
     * Narrow a given lexical offset range to the closest AST-relevant offsets.
     * This means it will pass over things like comments and whitespace.
     * @param doc The document containing the range
     * @param range The start/end lexical range we want to narrow
     * @return An OffsetRange where the offsets begin and end at AST-relevant tokens
     */
    public static OffsetRange narrow(BaseDocument doc, OffsetRange range, boolean skipComments) {
        try {
            doc.readLock(); // For token hiearchy use
            // For token hiearchy use
            int start = range.getStart();
            TokenSequence<? extends PythonTokenId> ts = getPythonSequence(doc, start);
            if (ts != null) {
                int delta = ts.move(start);
                while (ts.moveNext()) {
                    Token<? extends PythonTokenId> token = ts.token();
                    PythonTokenId id = token.id();
                    if (id != PythonTokenId.NEWLINE && (!skipComments || id != PythonTokenId.COMMENT) && id != PythonTokenId.WHITESPACE) {
                        if (delta != 0) {
                            return OffsetRange.NONE;
                        }
                        start = ts.offset();
                        break;
                    } else {
                        delta = 0;
                    }
                }
            }
            int end = range.getEnd();
            ts = getPositionedSequence(doc, end);
            if (ts != null) {
                int delta = ts.move(end);
                while (delta > 0 ? ts.moveNext() : ts.movePrevious()) {
                    Token<? extends PythonTokenId> token = ts.token();
                    PythonTokenId id = token.id();
                    if (id != PythonTokenId.NEWLINE && (!skipComments || id != PythonTokenId.COMMENT) && id != PythonTokenId.WHITESPACE) {
                        if (delta != 0) {
                            return OffsetRange.NONE;
                        }
                        end = ts.offset() + token.length();
                        break;
                    } else {
                        delta = 0;
                    }
                }
            }

            if (end < start) {
                return OffsetRange.NONE;
            }

            return new OffsetRange(start, end);
        } finally {
            doc.readUnlock();
        }
    }

    /** Find the ruby token sequence (in case it's embedded in something else at the top level */
    @SuppressWarnings("unchecked")
    public static TokenSequence<? extends PythonTokenId> getPythonSequence(BaseDocument doc, int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        return getPythonSequence(th, offset);
    }

    @SuppressWarnings("unchecked")
    public static TokenSequence<? extends PythonTokenId> getPythonSequence(TokenHierarchy<Document> th, int offset) {
        TokenSequence<? extends PythonTokenId> ts = th.tokenSequence(PythonTokenId.language());

        if (ts == null) {
            // Possibly an embedding scenario such as an RHTML file
            // First try with backward bias true
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);

            for (TokenSequence t : list) {
                if (t.language() == PythonTokenId.language()) {
                    ts = t;

                    break;
                }
            }

            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);

                for (TokenSequence t : list) {
                    if (t.language() == PythonTokenId.language()) {
                        ts = t;

                        break;
                    }
                }
            }
        }

        return ts;
    }

    public static TokenSequence<? extends PythonTokenId> getPositionedSequence(BaseDocument doc, int offset) {
        return getPositionedSequence(doc, offset, true);
    }

    public static TokenSequence<? extends PythonTokenId> getPositionedSequence(BaseDocument doc, int offset, boolean lookBack) {
        TokenSequence<? extends PythonTokenId> ts = getPythonSequence(doc, offset);

        if (ts != null) {
            try {
                ts.move(offset);
            } catch (AssertionError e) {
                DataObject dobj = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);

                if (dobj != null) {
                    Exceptions.attachMessage(e, FileUtil.getFileDisplayName(dobj.getPrimaryFile()));
                }

                throw e;
            }

            if (!lookBack && !ts.moveNext()) {
                return null;
            } else if (lookBack && !ts.moveNext() && !ts.movePrevious()) {
                return null;
            }

            /* TODO - allow Python inside strings
            if (ts.token().id() == PythonTokenId.STRING_LITERAL) {
            TokenSequence<? extends PythonStringTokenId> ets = ts.embedded(PythonStringTokenId.language());
            if (ets != null) {
            ets.move(offset);
            if ((!lookBack && ets.moveNext()) || (lookBack && ets.movePrevious())) {
            TokenSequence<?extends PythonTokenId> epts = ets.embedded(PythonTokenId.language());
            if (epts != null) {
            epts.move(offset);
            if (!lookBack && !epts.moveNext()) {
            return null;
            } else if (lookBack && !epts.moveNext() && !epts.movePrevious()) {
            return null;
            }
            return epts;
            }
            }
            }
            }
             */

            return ts;
        }

        return null;
    }

    public static Token<? extends PythonTokenId> getToken(BaseDocument doc, int offset) {
        TokenSequence<? extends PythonTokenId> ts = getPositionedSequence(doc, offset);

        if (ts != null) {
            return ts.token();
        }

        return null;
    }

    public static char getTokenChar(BaseDocument doc, int offset) {
        Token<? extends PythonTokenId> token = getToken(doc, offset);

        if (token != null) {
            String text = token.text().toString();

            if (text.length() > 0) { // Usually true, but I could have gotten EOF right?

                return text.charAt(0);
            }
        }

        return 0;
    }

    public static Token<? extends PythonTokenId> findNextNonWsNonComment(TokenSequence<? extends PythonTokenId> ts) {
        return findNext(ts, Arrays.asList(PythonTokenId.WHITESPACE, PythonTokenId.NEWLINE, PythonTokenId.COMMENT));
    }

    public static Token<? extends PythonTokenId> findPreviousNonWsNonComment(TokenSequence<? extends PythonTokenId> ts) {
        return findPrevious(ts, Arrays.asList(PythonTokenId.WHITESPACE, PythonTokenId.NEWLINE, PythonTokenId.COMMENT));
    }

    public static Token<? extends PythonTokenId> findNext(TokenSequence<? extends PythonTokenId> ts, List<PythonTokenId> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.moveNext() && ignores.contains(ts.token().id())) {
            }
        }
        return ts.token();
    }

    public static Token<? extends PythonTokenId> findNextIncluding(TokenSequence<? extends PythonTokenId> ts, List<PythonTokenId> includes) {
        while (ts.moveNext() && !includes.contains(ts.token().id())) {
        }
        return ts.token();
    }

    public static Token<? extends PythonTokenId> findPreviousIncluding(TokenSequence<? extends PythonTokenId> ts, List<PythonTokenId> includes) {
        while (ts.movePrevious() && !includes.contains(ts.token().id())) {
        }
        return ts.token();
    }

    public static Token<? extends PythonTokenId> findPrevious(TokenSequence<? extends PythonTokenId> ts, List<PythonTokenId> ignores) {
        if (ignores.contains(ts.token().id())) {
            while (ts.movePrevious() && ignores.contains(ts.token().id())) {
            }
        }
        return ts.token();
    }

    static boolean skipParenthesis(TokenSequence<? extends PythonTokenId> ts) {
        return skipParenthesis(ts, false);
    }

    /**
     * Tries to skip parenthesis
     */
    public static boolean skipParenthesis(TokenSequence<? extends PythonTokenId> ts, boolean back) {
        int balance = 0;

        Token<? extends PythonTokenId> token = ts.token();
        if (token == null) {
            return false;
        }

        TokenId id = token.id();

        if (id == PythonTokenId.WHITESPACE || id == PythonTokenId.NEWLINE) {
            while ((back ? ts.movePrevious() : ts.moveNext()) && (ts.token().id() == PythonTokenId.WHITESPACE || ts.token().id() == PythonTokenId.NEWLINE)) {
            }
        }

        // if current token is not left parenthesis
        if (ts.token().id() != (back ? PythonTokenId.RPAREN : PythonTokenId.LPAREN)) {
            return false;
        }

        do {
            token = ts.token();
            id = token.id();

            if (id == (back ? PythonTokenId.RPAREN : PythonTokenId.LPAREN)) {
                balance++;
            } else if (id == (back ? PythonTokenId.LPAREN : PythonTokenId.RPAREN)) {
                if (balance == 0) {
                    return false;
                } else if (balance == 1) {
                    //int length = ts.offset() + token.length();
                    if (back) {
                        ts.movePrevious();
                    } else {
                        ts.moveNext();
                    }
                    return true;
                }

                balance--;
            }
        } while (back ? ts.movePrevious() : ts.moveNext());

        return false;
    }

    /** Search forwards in the token sequence until a token of type <code>down</code> is found */
    public static OffsetRange findFwd(BaseDocument doc, TokenSequence<? extends PythonTokenId> ts, TokenId up,
            TokenId down) {
        int balance = 0;

        while (ts.moveNext()) {
            Token<? extends PythonTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == up) {
                balance++;
            } else if (id == down) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance--;
            }
        }

        return OffsetRange.NONE;
    }

    /** Search backwards in the token sequence until a token of type <code>up</code> is found */
    public static OffsetRange findBwd(BaseDocument doc, TokenSequence<? extends PythonTokenId> ts, TokenId up,
            TokenId down) {
        int balance = 0;

        while (ts.movePrevious()) {
            Token<? extends PythonTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == up) {
                if (balance == 0) {
                    return new OffsetRange(ts.offset(), ts.offset() + token.length());
                }

                balance++;
            } else if (id == down) {
                balance--;
            }
        }

        return OffsetRange.NONE;
    }

    /** Compute the balance of begin/end tokens on the line */
    public static int getLineBalance(BaseDocument doc, int offset, TokenId up, TokenId down) {
        try {
            int begin = Utilities.getRowStart(doc, offset);
            int end = Utilities.getRowEnd(doc, offset);

            TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, begin);
            if (ts == null) {
                return 0;
            }

            ts.move(begin);

            if (!ts.moveNext()) {
                return 0;
            }

            int balance = 0;

            do {
                Token<? extends PythonTokenId> token = ts.token();
                TokenId id = token.id();

                if (id == up) {
                    balance++;
                } else if (id == down) {
                    balance--;
                }
            } while (ts.moveNext() && (ts.offset() <= end));

            return balance;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);

            return 0;
        }
    }

    /**
     * The same as braceBalance but generalized to any pair of matching
     * tokens.
     * @param open the token that increses the count
     * @param close the token that decreses the count
     */
    public static int getTokenBalance(BaseDocument doc, TokenId open, TokenId close, int offset)
            throws BadLocationException {
        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, 0);
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

            if (t.id() == open) {
                balance++;
            } else if (t.id() == close) {
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

        Token<? extends PythonTokenId> token = PythonLexerUtils.getToken(doc, begin);
        if (token != null) {
            return token.id() == PythonTokenId.COMMENT;
        }

        return false;
    }

    /**
     * Back up to the first space character prior to the given offset - as long as
     * it's on the same line!  If there's only leading whitespace on the line up
     * to the lex offset, return the offset itself
     * @todo Rewrite this now that I have a separate newline token, EOL, that I can
     *   break on - no need to call Utilities.getRowStart.
     */
    public static int findSpaceBegin(BaseDocument doc, int lexOffset) {
        TokenSequence ts = getPythonSequence(doc, lexOffset);
        if (ts == null) {
            return lexOffset;
        }
        boolean allowPrevLine = false;
        int lineStart;
        try {
            lineStart = Utilities.getRowStart(doc, Math.min(lexOffset, doc.getLength()));
            int prevLast = lineStart - 1;
            if (lineStart > 0) {
                prevLast = Utilities.getRowLastNonWhite(doc, lineStart - 1);
                if (prevLast != -1) {
                    char c = doc.getText(prevLast, 1).charAt(0);
                    if (c == ',') {
                        // Arglist continuation? // TODO : check lexing
                        allowPrevLine = true;
                    }
                }
            }
            if (!allowPrevLine) {
                int firstNonWhite = Utilities.getRowFirstNonWhite(doc, lineStart);
                if (lexOffset <= firstNonWhite || firstNonWhite == -1) {
                    return lexOffset;
                }
            } else {
                // Make lineStart so small that Math.max won't cause any problems
                int firstNonWhite = Utilities.getRowFirstNonWhite(doc, lineStart);
                if (prevLast >= 0 && (lexOffset <= firstNonWhite || firstNonWhite == -1)) {
                    return prevLast + 1;
                }
                lineStart = 0;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            return lexOffset;
        }
        ts.move(lexOffset);
        if (ts.moveNext()) {
            if (lexOffset > ts.offset()) {
                // We're in the middle of a token
                return Math.max((ts.token().id() == PythonTokenId.WHITESPACE) ? ts.offset() : lexOffset, lineStart);
            }
            while (ts.movePrevious()) {
                Token token = ts.token();
                if (token.id() != PythonTokenId.WHITESPACE) {
                    return Math.max(ts.offset() + token.length(), lineStart);
                }
            }
        }

        return lexOffset;
    }

     public static boolean isKeywordOrBuiltin(CharSequence name) {
        return PythonLexer.isKeywordOrBuiltin(name);
    }
}
