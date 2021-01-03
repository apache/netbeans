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
package org.netbeans.modules.python.editor;

import org.netbeans.modules.python.source.AstPath;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.PythonAstUtils;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditorOptions;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;

/**
 * A keystroke handler for Python. Responsible for reacting to keystrokes,
 * such as newline (which will handle smart-indent), automatic quote or bracket pairing,
 * etc.
 *
 * @todo Handle Python triple quotes better
 * @todo Newlines: continue comments, split string literals
 * @todo Outdent on typing a colon when you type "elif true:" etc.
 * @todo Track line continuation levels for ( and { - see the
 *     "Blank Lines" section of http://www.python.org/dev/peps/pep-0008/
 * @todo If you press \, the next line should be indented!
 * @todo Deletion should remove the colon too
 *
 */
public class PythonKeystrokeHandler implements KeystrokeHandler {
    // Make this an option
    //private static final boolean FROM_IMPORT = !Boolean.getBoolean("python.nofromimport");
    private static final boolean FROM_IMPORT = Boolean.getBoolean("python.fromimport"); // NOI18N

    @Override
    public int beforeBreak(Document document, int offset, JTextComponent target) throws BadLocationException {

        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument)document;

        // Very simple algorithm for now..
        // Basically, use the same indent as the current line, unless the caret is immediately preceeded by a ":" (possibly with whitespace
        // in between)

        //        boolean insertMatching = isInsertMatchingEnabled(doc);

        int lineBegin = Utilities.getRowStart(doc, offset);
        int lineEnd = Utilities.getRowEnd(doc, offset);

        if (lineBegin == offset && lineEnd == offset) {
            // Pressed return on a blank newline - do nothing
            return -1;
        }

        int indent = GsfUtilities.getLineIndent(doc, offset);
        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, offset);
        if (ts != null) {
            int firstChar = Utilities.getRowFirstNonWhite(doc, offset);
            boolean dedented = false;
            if (firstChar != -1) {
                ts.move(firstChar);
                if (ts.moveNext()) {
                    Token<? extends PythonTokenId> token = ts.token();
                    PythonTokenId id = token.id();
                    if (id == PythonTokenId.RETURN || id == PythonTokenId.PASS || id == PythonTokenId.RAISE) {
                        dedented = true;
                        int indentLevelSize = IndentUtils.indentLevelSize(doc);
                        indent -= indentLevelSize;
                    }
                }
            }

            int lastChar = Utilities.getRowLastNonWhite(doc, offset);
            if (lastChar != -1) {
                lastChar++; // points to beginning of last char
                int delta = ts.move(Math.min(offset, lastChar));
                if ((delta > 0 && ts.moveNext()) || ts.movePrevious()) {
                    Token<? extends PythonTokenId> token = ts.token();
                    TokenId id = token.id();
                    if (id == PythonTokenId.ESC || id == PythonTokenId.COLON) {
                        if (offset >= lastChar) {
                            int indentLevelSize = IndentUtils.indentLevelSize(doc);
                            indent += indentLevelSize;
                            if (dedented && id == PythonTokenId.ESC) { // For \ continuations, don't outdent beginning of line since we're still on it
                                indent += indentLevelSize;
                            }
                        }
                    }
                    if (id == PythonTokenId.STRING_BEGIN) {
                        if ((ts.moveNext() && ts.token().id() == PythonTokenId.ERROR) || offset == doc.getLength()) {
                            // Beginning a ''' or """ sequence
                            String str = IndentUtils.createIndentString(doc, indent);
                            int newPos = offset + str.length();
                            CharSequence marker = token.text();
                            marker = stripStringModifiers(marker);
                            doc.insertString(offset, str + "\n" + str + marker, null); // NOI18N
                            caret.setDot(offset);
                            return newPos + 1;
                        }
                    } else if (id == PythonTokenId.ERROR && offset >= lastChar) {
                        if (ts.movePrevious()) {
                            TokenId prev = ts.token().id();
                            if (prev == PythonTokenId.STRING_BEGIN) {
                                CharSequence marker = ts.token().text();
                                marker = stripStringModifiers(marker);

                                // Insert on the -same- line (this isn't a multiline docstring since
                                // we have text on the same line (eh, check that we're n the same line
                                // as the string begin!)

                                String str = IndentUtils.createIndentString(doc, indent);
                                String insert = marker + str; // NOI18N
                                doc.insertString(offset, insert, null);
                                int newOffset = offset + insert.length();
                                caret.setDot(offset+marker.length()); // insert \n here
                                return newOffset+1;
                            }
                        }
                    } else if (ts.moveNext()) {
                        if (ts.token().id() == PythonTokenId.RPAREN && ts.moveNext()) {
                            if (ts.token().id() == PythonTokenId.COLON && ((ts.offset() + ts.token().length() == doc.getLength()) || ts.moveNext() &&
                                    ts.offset() == lastChar)) {
                                // You just pressed return where the caret is in ^):<whitespace*>\n
                                // and in this case we want to just skip past the ):
                                // (assuming the previous character isn't a comma
                                ts.move(offset);
                                boolean isAfterComma = false;
                                if (ts.movePrevious()) {
                                    id = ts.token().id();
                                    if (id == PythonTokenId.WHITESPACE) {
                                        if (ts.movePrevious()) {
                                            id = ts.token().id();
                                        }
                                    }
                                    if (id == PythonTokenId.COMMA) {
                                        isAfterComma = true;
                                    }
                                }
                                if (!isAfterComma) {
                                    int indentLevelSize = IndentUtils.indentLevelSize(doc);
                                    indent += indentLevelSize;
                                    String str = IndentUtils.createIndentString(doc, indent);
                                    int rowEnd = Utilities.getRowEnd(doc, lastChar);
                                    if (rowEnd >= doc.getLength()) {
                                        rowEnd = doc.getLength();
                                    }
                                    int newPos = rowEnd + str.length();
                                    doc.insertString(rowEnd, str, null);
                                    caret.setDot(rowEnd);
                                    return newPos + 1;
                                }
                            }
                        }

                    }
                }
            }
        }

        // Also remove the whitespace from the caret up to the first nonspace character on the current line
        int remove = 0;
        String line = doc.getText(lineBegin, lineEnd + 1 - lineBegin);
        for (int n = line.length(), i = offset - lineBegin; i < n; i++) {
            char c = line.charAt(i);
            if (c == ' ' || c == '\t') {
                remove++;
            } else {
                break;
            }
        }
        if (remove > 0) {
            doc.remove(offset, remove);
        }

        if (indent < 0) {
            indent = 0;
        }

        String str = IndentUtils.createIndentString(doc, indent);
        int newPos = offset + str.length();
        doc.insertString(offset, str, null);
        caret.setDot(offset);
        return newPos + 1;
    }

    private CharSequence stripStringModifiers(CharSequence delimiter) {
        for (int i = 0; i < delimiter.length(); i++) {
            char c = delimiter.charAt(i);

            if (c == '\'' || c == '\"') {
                if (i == 0) {
                    return delimiter;
                } else {
                    return delimiter.subSequence(i, delimiter.length());
                }
            }
        }

        return delimiter;
    }
    /** Tokens which indicate that we're within a literal string */
    private final static TokenId[] STRING_TOKENS = // XXX What about PythonTokenId.STRING_BEGIN or QUOTED_STRING_BEGIN?
            {
        PythonTokenId.STRING_LITERAL, PythonTokenId.STRING_END
    };
    /** When != -1, this indicates that we previously adjusted the indentation of the
     * line to the given offset, and if it turns out that the user changes that token,
     * we revert to the original indentation
     */
    private int previousAdjustmentOffset = -1;
    /** True iff we're processing bracket matching AFTER the key has been inserted rather than before  */
    private boolean isAfter;
    /**
     * The indentation to revert to when previousAdjustmentOffset is set and the token
     * changed
     */
    private int previousAdjustmentIndent;

    public boolean isInsertMatchingEnabled(BaseDocument doc) {
        // The editor options code is calling methods on BaseOptions instead of looking in the settings map :(
        //Boolean b = ((Boolean)Settings.getValue(doc.getKitClass(), SettingsNames.PAIR_CHARACTERS_COMPLETION));
        //return b == null || b.booleanValue();
        EditorOptions options = EditorOptions.get(PythonMIMEResolver.PYTHON_MIME_TYPE);
        if (options != null) {
            return options.getMatchBrackets();
        }

        return true;
    }

    @Override
    public boolean beforeCharInserted(Document document, int caretOffset, JTextComponent target, char ch)
            throws BadLocationException {
        isAfter = false;
        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument)document;

        if (!isInsertMatchingEnabled(doc)) {
            return false;
        }

        if (target.getSelectionStart() != -1) {
            if (GsfUtilities.isCodeTemplateEditing(doc)) {
                int start = target.getSelectionStart();
                int end = target.getSelectionEnd();
                if (start < end) {
                    target.setSelectionStart(start);
                    target.setSelectionEnd(start);
                    caretOffset = start;
                    caret.setDot(caretOffset);
                    doc.remove(start, end - start);
                }
                // Fall through to do normal insert matching work
            } else if (ch == '"' || ch == '\'' || ch == '(' || ch == '{' || ch == '[') {
                // Bracket the selection
                String selection = target.getSelectedText();
                if (selection != null && selection.length() > 0) {
                    char firstChar = selection.charAt(0);
                    if (firstChar != ch) {
                        int start = target.getSelectionStart();
                        int end = target.getSelectionEnd();
                        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPositionedSequence(doc, start);
                        if (ts != null && ts.token().id() != PythonTokenId.STRING_LITERAL) { // Not inside strings!
                            int lastChar = selection.charAt(selection.length() - 1);
                            // Replace the surround-with chars?
                            if (selection.length() > 1 &&
                                    ((firstChar == '"' || firstChar == '\'' || firstChar == '(' ||
                                    firstChar == '{' || firstChar == '[') &&
                                    lastChar == matching(firstChar))) {
                                doc.remove(end - 1, 1);
                                doc.insertString(end - 1, "" + matching(ch), null);
                                doc.remove(start, 1);
                                doc.insertString(start, "" + ch, null);
                                target.getCaret().setDot(end);
                            } else {
                                // No, insert around
                                doc.remove(start, end - start);
                                doc.insertString(start, ch + selection + matching(ch), null);
                                target.getCaret().setDot(start + selection.length() + 2);
                            }

                            return true;
                        }
                    }
                }
            }
        }

        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<? extends PythonTokenId> token = ts.token();
        TokenId id = token.id();
        TokenId[] stringTokens = null;
        TokenId beginTokenId = null;

        // "/" is handled AFTER the character has been inserted since we need the lexer's help
        if (ch == '\"' || ch == '\'') {
            stringTokens = STRING_TOKENS;
            beginTokenId = PythonTokenId.STRING_BEGIN;
        } else if (id == PythonTokenId.ERROR) {
            //String text = token.text().toString();

            ts.movePrevious();

            TokenId prevId = ts.token().id();

            if (prevId == PythonTokenId.STRING_BEGIN) {
                stringTokens = STRING_TOKENS;
                beginTokenId = prevId;
            }
        } else if ((id == PythonTokenId.STRING_BEGIN) &&
                (caretOffset == (ts.offset() + 1))) {
            if (!Character.isLetter(ch)) { // %q, %x, etc. Only %[], %!!, %<space> etc. is allowed
                stringTokens = STRING_TOKENS;
                beginTokenId = id;
            }
        } else if (((id == PythonTokenId.STRING_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
                (id == PythonTokenId.STRING_END)) {
            stringTokens = STRING_TOKENS;
            beginTokenId = PythonTokenId.STRING_BEGIN;
        } else if (id == PythonTokenId.COLON && ch == ':') {
            // Was the previous character a )?
            ts.movePrevious();

            TokenId prevId = ts.token().id();

            if (prevId == PythonTokenId.RPAREN) {
                // Yes, it was )^: - just skip the : since we're typing through a def foo(): segment
                caret.setDot(caretOffset + 1);
                return true;
            }
        } else if (FROM_IMPORT && (ch == ' ' && (id == PythonTokenId.WHITESPACE || id == PythonTokenId.NEWLINE ||
                caretOffset == doc.getLength()))) {
            // Replace "from foo^" with "from foo import ^"
            int rowStart = Utilities.getRowStart(doc, caretOffset);
            if (doc.getText(rowStart, 1).charAt(0) == 'f' && caretOffset == Utilities.getRowLastNonWhite(doc, caretOffset) + 1) {
                TokenSequence<? extends PythonTokenId> pts = PythonLexerUtils.getPythonSequence(doc, caretOffset);
                if (pts != null) {
                    pts.move(caretOffset);
                    if (pts.movePrevious()) {
                        if (pts.token().id() == PythonTokenId.IDENTIFIER && pts.movePrevious()) {
                            if (pts.token().id() == PythonTokenId.WHITESPACE && pts.movePrevious()) {
                                if (pts.token().id() == PythonTokenId.FROM) {
                                    String importString = " import "; // NOI18N
                                    doc.insertString(caretOffset, importString, null);
                                    caret.setDot(caretOffset + importString.length());
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (stringTokens != null) {
            boolean inserted =
                    completeQuote(doc, caretOffset, caret, ch, stringTokens, beginTokenId);

            if (inserted) {
                caret.setDot(caretOffset + 1);

                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    /**
     * A hook method called after a character was inserted into the
     * document. The function checks for special characters for
     * completion ()[]'"{} and other conditions and optionally performs
     * changes to the doc and or caret (complets braces, moves caret,
     * etc.)
     * @param document the document where the change occurred
     * @param dotPos position of the character insertion
     * @param target The target
     * @param ch the character that was inserted
     * @return Whether the insert was handled
     * @throws BadLocationException if dotPos is not correct
     */
    @Override
    public boolean afterCharInserted(Document document, int dotPos, JTextComponent target, char ch)
            throws BadLocationException {
        isAfter = true;
        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument)document;

        // See if our automatic adjustment of indentation when typing (for example) "end" was
        // premature - if you were typing a longer word beginning with one of my adjustment
        // prefixes, such as "endian", then put the indentation back.
        if (previousAdjustmentOffset != -1) {
            if (dotPos == previousAdjustmentOffset) {
                // Revert indentation iff the character at the insert position does
                // not start a new token (e.g. the previous token that we reindented
                // was not complete)
                TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, dotPos);

                if (ts != null) {
                    ts.move(dotPos);

                    if (ts.moveNext() && (ts.offset() < dotPos)) {
                        GsfUtilities.setLineIndentation(doc, dotPos, previousAdjustmentIndent);
                    }
                }
            }

            previousAdjustmentOffset = -1;
        }

        switch (ch) {
        case '}':
        case '{':
        case ')':
        case ']':
        case '(':
        case '[': {

            if (!isInsertMatchingEnabled(doc)) {
                return false;
            }


            Token<? extends PythonTokenId> token = PythonLexerUtils.getToken(doc, dotPos);
            if (token == null) {
                return true;
            }
            TokenId id = token.id();

            if (id == PythonTokenId.ANY_OPERATOR) {
                int length = token.length();
                String s = token.text().toString();
                if ((length == 2) && "[]".equals(s) || "[]=".equals(s)) { // Special case
                    skipClosingBracket(doc, caret, ch, PythonTokenId.RBRACKET);

                    return true;
                }
            }

            if (((id == PythonTokenId.IDENTIFIER) && (token.length() == 1)) ||
                    (id == PythonTokenId.LBRACKET) || (id == PythonTokenId.RBRACKET) ||
                    (id == PythonTokenId.LBRACE) || (id == PythonTokenId.RBRACE) ||
                    (id == PythonTokenId.LPAREN) || (id == PythonTokenId.RPAREN)) {
                if (ch == ']') {
                    skipClosingBracket(doc, caret, ch, PythonTokenId.RBRACKET);
                } else if (ch == ')') {
                    skipClosingBracket(doc, caret, ch, PythonTokenId.RPAREN);
                } else if (ch == '}') {
                    skipClosingBracket(doc, caret, ch, PythonTokenId.RBRACE);
                } else if ((ch == '[') || (ch == '(') || (ch == '{')) {
                    completeOpeningBracket(doc, dotPos, caret, ch);
                }
            }

            // Reindent blocks (won't do anything if } is not at the beginning of a line
            if (ch == '}') {
                reindent(doc, dotPos, PythonTokenId.RBRACE, caret);
            } else if (ch == ']') {
                reindent(doc, dotPos, PythonTokenId.RBRACKET, caret);
            }
        }

        break;

        case ':': {
            int lineBegin = Utilities.getRowFirstNonWhite(doc, dotPos);
            int lineEnd = Utilities.getRowLastNonWhite(doc, dotPos);
            if (dotPos == lineEnd && lineBegin != -1) {
                Token<? extends PythonTokenId> token = PythonLexerUtils.getToken(doc, lineBegin);
                if (token != null) {
                    PythonTokenId id = token.id();
                    if (id == PythonTokenId.EXCEPT || id == PythonTokenId.FINALLY || id == PythonTokenId.ELIF ||
                            id == PythonTokenId.ELSE) {
                        // See if it's the end of an "else" or "elseif" - if so, reindent
                        reindent(doc, lineBegin, id, caret);
                        return true;
                    }
                }
            }

        }
        break;

        }

        return true;
    }

    private void reindent(BaseDocument doc, int offset, TokenId id, Caret caret)
            throws BadLocationException {
        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, offset);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }

            Token<? extends PythonTokenId> token = ts.token();

            if ((token.id() == id)) {
                final int rowFirstNonWhite = Utilities.getRowFirstNonWhite(doc, offset);
                // Ensure that this token is at the beginning of the line
                if (ts.offset() > rowFirstNonWhite) {
                    return;
                }

                OffsetRange begin;

                if (id == PythonTokenId.RBRACE) {
                    begin = PythonLexerUtils.findBwd(doc, ts, PythonTokenId.LBRACE, PythonTokenId.RBRACE);
                } else if (id == PythonTokenId.RBRACKET) {
                    begin = PythonLexerUtils.findBwd(doc, ts, PythonTokenId.LBRACKET, PythonTokenId.RBRACKET);
                } else if (id == PythonTokenId.EXCEPT || id == PythonTokenId.FINALLY) {
                    // XXX this isn't quite right - it would be more accurate for me to
                    // look at one indentation level out. E.g. if I find it's already matched, don't
                    // reindent, and if I don't, then match exactly a one level outdented try further back.
                    begin = PythonLexerUtils.findBwd(doc, ts, PythonTokenId.TRY, PythonTokenId.TRY);
                } else if (id == PythonTokenId.ELIF || id == PythonTokenId.ELSE) {
                    // XXX this isn't quite right - it would be more accurate for me to
                    // look at one indentation level out. E.g. if I find it's already matched, don't
                    // reindent, and if I don't, then match exactly a one level outdented try further back.
                    begin = PythonLexerUtils.findBwd(doc, ts, PythonTokenId.IF, PythonTokenId.IF);
                } else {
                    // TODO: Look for matching if/elsif/else pairs!
                    //    begin = PythonLexerUtils.findBegin(doc, ts);
                    begin = OffsetRange.NONE;
                }

                if (begin != OffsetRange.NONE) {
                    int beginOffset = begin.getStart();
                    int indent = GsfUtilities.getLineIndent(doc, beginOffset);
                    previousAdjustmentIndent = GsfUtilities.getLineIndent(doc, offset);
                    GsfUtilities.setLineIndentation(doc, offset, indent);
                    previousAdjustmentOffset = caret.getDot();
                }
            }
        }
    }

    @Override
    public OffsetRange findMatching(Document document, int offset /*, boolean simpleSearch*/) {
        BaseDocument doc = (BaseDocument)document;

        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, offset);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext()) {
                return OffsetRange.NONE;
            }

            Token<? extends PythonTokenId> token = ts.token();

            if (token == null) {
                return OffsetRange.NONE;
            }

            TokenId id = token.id();

            if (id == PythonTokenId.WHITESPACE) {
                // ts.move(offset) gives the token to the left of the caret.
                // If you have the caret right at the beginning of a token, try
                // the token to the right too - this means that if you have
                //  "   |def" it will show the matching "end" for the "def".
                offset++;
                ts.move(offset);

                if (ts.moveNext() && (ts.offset() <= offset)) {
                    token = ts.token();
                    id = token.id();
                }
            }

            if (id == PythonTokenId.STRING_BEGIN) {
                return PythonLexerUtils.findFwd(doc, ts, PythonTokenId.STRING_BEGIN, PythonTokenId.STRING_END);
            } else if (id == PythonTokenId.STRING_END) {
                return PythonLexerUtils.findBwd(doc, ts, PythonTokenId.STRING_BEGIN, PythonTokenId.STRING_END);
            } else if (id == PythonTokenId.LPAREN) {
                return PythonLexerUtils.findFwd(doc, ts, PythonTokenId.LPAREN, PythonTokenId.RPAREN);
            } else if (id == PythonTokenId.RPAREN) {
                return PythonLexerUtils.findBwd(doc, ts, PythonTokenId.LPAREN, PythonTokenId.RPAREN);
            } else if (id == PythonTokenId.LBRACE) {
                return PythonLexerUtils.findFwd(doc, ts, PythonTokenId.LBRACE, PythonTokenId.RBRACE);
            } else if (id == PythonTokenId.RBRACE) {
                return PythonLexerUtils.findBwd(doc, ts, PythonTokenId.LBRACE, PythonTokenId.RBRACE);
            } else if (id == PythonTokenId.LBRACKET) {
                return PythonLexerUtils.findFwd(doc, ts, PythonTokenId.LBRACKET, PythonTokenId.RBRACKET);
            } else if (id == PythonTokenId.RBRACKET) {
                return PythonLexerUtils.findBwd(doc, ts, PythonTokenId.LBRACKET, PythonTokenId.RBRACKET);
            }
        }

        return OffsetRange.NONE;
    }

    /**
     * Hook called after a character *ch* was backspace-deleted from
     * *doc*. The function possibly removes bracket or quote pair if
     * appropriate.
     * @param doc the document
     * @param dotPos position of the change
     * @param caret caret
     * @param ch the character that was deleted
     */
    @SuppressWarnings("fallthrough")
    @Override
    public boolean charBackspaced(Document document, int dotPos, JTextComponent target, char ch)
            throws BadLocationException {
        BaseDocument doc = (BaseDocument)document;

        // Try to handle backspace such that if you backspace for indentation, we interpret it
        // as an attempt to outdent
        if (ch == ' ') {
            int textBegin = Utilities.getRowFirstNonWhite(doc, dotPos);
            if (textBegin == -1 || textBegin >= dotPos) {
                int indentSize = IndentUtils.indentLevelSize(doc);
                int lineIndent = GsfUtilities.getLineIndent(doc, dotPos);
                if (lineIndent > 0) {
                    int mod = lineIndent % indentSize;
                    if (mod == indentSize - 1) {
                        // Yes, we just deleted out of a whole multiple of the indent size, so remove space
                        // down to the next indent step
                        int deleteOffset = dotPos - mod;
                        int deleteSize = mod;
                        document.remove(deleteOffset, deleteSize);
                        return true;
                    }
                }
            }
        }


        switch (ch) {
        case ' ': {
            // Backspacing over "// " ? Delete the "//" too!
            TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPositionedSequence(doc, dotPos);
            if (ts != null && ts.token().id() == PythonTokenId.COMMENT) {
                if (ts.offset() == dotPos - 2) {
                    doc.remove(dotPos - 2, 2);
                    target.getCaret().setDot(dotPos - 2);

                    return true;
                }
            }
            break;
        }

        case '{':
        case '(':
        case '[': { // and '{' via fallthrough
            char tokenAtDot = PythonLexerUtils.getTokenChar(doc, dotPos);

            if (((tokenAtDot == ']') &&
                    (PythonLexerUtils.getTokenBalance(doc, PythonTokenId.LBRACKET, PythonTokenId.RBRACKET, dotPos) != 0)) ||
                    ((tokenAtDot == ')') &&
                    (PythonLexerUtils.getTokenBalance(doc, PythonTokenId.LPAREN, PythonTokenId.RPAREN, dotPos) != 0)) ||
                    ((tokenAtDot == '}') &&
                    (PythonLexerUtils.getTokenBalance(doc, PythonTokenId.LBRACE, PythonTokenId.RBRACE, dotPos) != 0))) {
                doc.remove(dotPos, 1);
                if (ch == '(') {
                    if (dotPos < doc.getLength()) {
                        tokenAtDot = PythonLexerUtils.getTokenChar(doc, dotPos);
                        if (tokenAtDot == ':') {
                            doc.remove(dotPos, 1);
                        }
                    }
                }
            }
            break;
        }

        case '\"':
        case '\'': {
            char[] match = doc.getChars(dotPos, 1);

            if ((match != null) && (match[0] == ch)) {
                doc.remove(dotPos, 1);
            }
        } // TODO: Test other auto-completion chars, like %q-foo-
        }
        return true;
    }

    /**
     * A hook to be called after closing bracket ) or ] was inserted into
     * the document. The method checks if the bracket should stay there
     * or be removed and some exisitng bracket just skipped.
     *
     * @param doc the document
     * @param dotPos position of the inserted bracket
     * @param caret caret
     * @param bracket the bracket character ']' or ')'
     */
    private void skipClosingBracket(BaseDocument doc, Caret caret, char bracket, TokenId bracketId)
            throws BadLocationException {
        int caretOffset = caret.getDot();

        if (isSkipClosingBracket(doc, caretOffset, bracketId)) {
            doc.remove(caretOffset - 1, 1);
            caret.setDot(caretOffset); // skip closing bracket
        }
    }

    /**
     * Check whether the typed bracket should stay in the document
     * or be removed.
     * <br>
     * This method is called by <code>skipClosingBracket()</code>.
     *
     * @param doc document into which typing was done.
     * @param caretOffset
     */
    private boolean isSkipClosingBracket(BaseDocument doc, int caretOffset, TokenId bracketId)
            throws BadLocationException {
        // First check whether the caret is not after the last char in the document
        // because no bracket would follow then so it could not be skipped.
        if (caretOffset == doc.getLength()) {
            return false; // no skip in this case
        }

        boolean skipClosingBracket = false; // by default do not remove

        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        ts.move(caretOffset);

        if (!ts.moveNext()) {
            return false;
        }

        Token<? extends PythonTokenId> token = ts.token();

        // Check whether character follows the bracket is the same bracket
        if ((token != null) && (token.id() == bracketId)) {
            int bracketIntId = bracketId.ordinal();
            int leftBracketIntId =
                    (bracketIntId == PythonTokenId.RPAREN.ordinal()) ? PythonTokenId.LPAREN.ordinal()
                    : PythonTokenId.LBRACKET.ordinal();

            // Skip all the brackets of the same type that follow the last one
            ts.moveNext();

            Token<? extends PythonTokenId> nextToken = ts.token();

            while ((nextToken != null) && (nextToken.id() == bracketId)) {
                token = nextToken;

                if (!ts.moveNext()) {
                    break;
                }

                nextToken = ts.token();
            }

            // token var points to the last bracket in a group of two or more right brackets
            // Attempt to find the left matching bracket for it
            // Search would stop on an extra opening left brace if found
            int braceBalance = 0; // balance of '{' and '}'
            int bracketBalance = -1; // balance of the brackets or parenthesis
            Token<? extends PythonTokenId> lastRBracket = token;
            ts.movePrevious();
            token = ts.token();

            boolean finished = false;

            while (!finished && (token != null)) {
                int tokenIntId = token.id().ordinal();

                if ((token.id() == PythonTokenId.LPAREN) || (token.id() == PythonTokenId.LBRACKET)) {
                    if (tokenIntId == bracketIntId) {
                        bracketBalance++;

                        if (bracketBalance == 0) {
                            if (braceBalance != 0) {
                                // Here the bracket is matched but it is located
                                // inside an unclosed brace block
                                // e.g. ... ->( } a()|)
                                // which is in fact illegal but it's a question
                                // of what's best to do in this case.
                                // We chose to leave the typed bracket
                                // by setting bracketBalance to 1.
                                // It can be revised in the future.
                                bracketBalance = 1;
                            }

                            finished = true;
                        }
                    }
                } else if ((token.id() == PythonTokenId.RPAREN) ||
                        (token.id() == PythonTokenId.RBRACKET)) {
                    if (tokenIntId == bracketIntId) {
                        bracketBalance--;
                    }
                } else if (token.id() == PythonTokenId.LBRACE) {
                    braceBalance++;

                    if (braceBalance > 0) { // stop on extra left brace
                        finished = true;
                    }
                } else if (token.id() == PythonTokenId.RBRACE) {
                    braceBalance--;
                }

                if (!ts.movePrevious()) {
                    break;
                }

                token = ts.token();
            }

            if (bracketBalance != 0) { // not found matching bracket
                // Remove the typed bracket as it's unmatched
                skipClosingBracket = true;
            } else { // the bracket is matched
                // Now check whether the bracket would be matched
                // when the closing bracket would be removed
                // i.e. starting from the original lastRBracket token
                // and search for the same bracket to the right in the text
                // The search would stop on an extra right brace if found
                braceBalance = 0;
                bracketBalance = 1; // simulate one extra left bracket

                //token = lastRBracket.getNext();
                TokenHierarchy<BaseDocument> th = TokenHierarchy.get(doc);

                int ofs = lastRBracket.offset(th);

                ts.move(ofs);
                ts.moveNext();
                token = ts.token();
                finished = false;

                while (!finished && (token != null)) {
                    //int tokenIntId = token.getTokenID().getNumericID();
                    if ((token.id() == PythonTokenId.LPAREN) || (token.id() == PythonTokenId.LBRACKET)) {
                        if (token.id().ordinal() == leftBracketIntId) {
                            bracketBalance++;
                        }
                    } else if ((token.id() == PythonTokenId.RPAREN) ||
                            (token.id() == PythonTokenId.RBRACKET)) {
                        if (token.id().ordinal() == bracketIntId) {
                            bracketBalance--;

                            if (bracketBalance == 0) {
                                if (braceBalance != 0) {
                                    // Here the bracket is matched but it is located
                                    // inside an unclosed brace block
                                    // which is in fact illegal but it's a question
                                    // of what's best to do in this case.
                                    // We chose to leave the typed bracket
                                    // by setting bracketBalance to -1.
                                    // It can be revised in the future.
                                    bracketBalance = -1;
                                }

                                finished = true;
                            }
                        }
                    } else if (token.id() == PythonTokenId.LBRACE) {
                        braceBalance++;
                    } else if (token.id() == PythonTokenId.RBRACE) {
                        braceBalance--;

                        if (braceBalance < 0) { // stop on extra right brace
                            finished = true;
                        }
                    }

                    //token = token.getPrevious(); // done regardless of finished flag state
                    if (!ts.movePrevious()) {
                        break;
                    }

                    token = ts.token();
                }

                // If bracketBalance == 0 the bracket would be matched
                // by the bracket that follows the last right bracket.
                skipClosingBracket = (bracketBalance == 0);
            }
        }

        return skipClosingBracket;
    }

    private boolean isMethodInClass(BaseDocument doc, int defOffset) {
        // Search backwards from the def to see if we're inside a class.
        // The algorithm is to look backwards at every line and and the
        // first line we find that has a smaller indent level had better
        // be a class

        try {
            int defLineBegin = Utilities.getRowStart(doc, defOffset);
            if (defLineBegin == 0) {
                return false;
            }
            int defIndent = GsfUtilities.getLineIndent(doc, defLineBegin);
            if (defIndent == 0) {
                return false;
            }

            int offset = defLineBegin - 1;
            while (offset >= 0) {
                offset = Utilities.getRowStart(doc, offset);
                if (!(Utilities.isRowWhite(doc, offset) || Utilities.isRowEmpty(doc, offset))) {
                    int lineIndent = GsfUtilities.getLineIndent(doc, offset);
                    if (lineIndent < defIndent) {
                        int begin = Utilities.getRowFirstNonWhite(doc, offset);
                        if (begin != -1) {
                            Token<? extends PythonTokenId> token = PythonLexerUtils.getToken(doc, begin);
                            if (token != null && token.id() == PythonTokenId.CLASS) {
                                return true;
                            }
                        }
                        return false;
                    }
                }
                offset = offset - 1;
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }

        return false;
    }

    /**
     * Check for various conditions and possibly add a pairing bracket
     * to the already inserted.
     * @param doc the document
     * @param dotPos position of the opening bracket (already in the doc)
     * @param caret caret
     * @param bracket the bracket that was inserted
     */
    private void completeOpeningBracket(BaseDocument doc, int dotPos, Caret caret, char bracket)
            throws BadLocationException {
        int newCaretOffset = dotPos + 1;
        if (isCompletablePosition(doc, newCaretOffset)) {
            String matchingBracket = "" + matching(bracket);

            if (bracket == '(') {
                // Go back and see if we have
                //    def foo(
                // or
                //    class foo(
                // and if so make it into def foo():
                TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, dotPos);
                ts.move(dotPos);
                if (ts.moveNext()) {
                    if (ts.token().id() == PythonTokenId.LPAREN && ts.movePrevious()) {
                        if (ts.token().id() == PythonTokenId.IDENTIFIER && ts.movePrevious()) {
                            if (ts.token().id() == PythonTokenId.WHITESPACE && ts.movePrevious()) {
                                TokenId id = ts.token().id();
                                if (id == PythonTokenId.DEF || id == PythonTokenId.CLASS) {
                                    if (isMethodInClass(doc, ts.offset())) {
                                        matchingBracket = "self):"; // NOI18N
                                        newCaretOffset += 4; // Skip "self"
                                    } else {
                                        matchingBracket = "):"; // NOI18N
                                    }
                                }
                            }
                        }
                    }
                }
            }

            doc.insertString(dotPos + 1, matchingBracket, null);

            caret.setDot(newCaretOffset);
        }
    }

    // XXX TODO Use embedded string sequence here and see if it
    // really is escaped. I know where those are!
    // TODO Adjust for Python
    private boolean isEscapeSequence(BaseDocument doc, int dotPos)
            throws BadLocationException {
        if (dotPos <= 0) {
            return false;
        }

        char previousChar = doc.getChars(dotPos - 1, 1)[0];

        return previousChar == '\\';
    }

    /**
     * Check for conditions and possibly complete an already inserted
     * quote .
     * @param doc the document
     * @param dotPos position of the opening bracket (already in the doc)
     * @param caret caret
     * @param bracket the character that was inserted
     */
    private boolean completeQuote(BaseDocument doc, int dotPos, Caret caret, char bracket,
            TokenId[] stringTokens, TokenId beginToken) throws BadLocationException {
        // Special handle for triple """ in Python: do nothing (yet). Do better handling later.
        if (dotPos >= 2 && (bracket == '"' || bracket == '\'')) {
            String s = doc.getText(dotPos - 2, 2);
            if ((bracket == '"' && "\"\"".equals(s)) || (bracket == '\'' && "''".equals(s))) { // NOI18N
                return false;
            }
        }

        if (isEscapeSequence(doc, dotPos)) { // \" or \' typed

            return false;
        }

        // Examine token at the caret offset
        if (doc.getLength() < dotPos) {
            return false;
        }

        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, dotPos);

        if (ts == null) {
            return false;
        }

        ts.move(dotPos);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<? extends PythonTokenId> token = ts.token();
        Token<? extends PythonTokenId> previousToken = null;

        if (ts.movePrevious()) {
            previousToken = ts.token();
        }

        int lastNonWhite = Utilities.getRowLastNonWhite(doc, dotPos);

        // eol - true if the caret is at the end of line (ignoring whitespaces)
        boolean eol = lastNonWhite < dotPos;

        if (token.id() == PythonTokenId.COMMENT || (token.id() == PythonTokenId.NEWLINE &&
                previousToken != null && previousToken.id() == PythonTokenId.COMMENT)) {
            return false;
        } else if ((token.id() == PythonTokenId.WHITESPACE) && eol && ((dotPos - 1) > 0)) {
            // check if the caret is at the very end of the line comment
            token = PythonLexerUtils.getToken(doc, dotPos - 1);

            if (token.id() == PythonTokenId.COMMENT) {
                return false;
            }
        }

        boolean completablePosition = isQuoteCompletablePosition(doc, dotPos);

        boolean insideString = false;
        TokenId id = token.id();

        for (TokenId currId : stringTokens) {
            if (id == currId) {
                insideString = true;
                break;
            }
        }

        if ((id == PythonTokenId.ERROR) && (previousToken != null) &&
                (previousToken.id() == beginToken)) {
            insideString = true;
        }

        if (id == PythonTokenId.NEWLINE && previousToken != null) {
            if (previousToken.id() == beginToken) {
                insideString = true;
            } else if (previousToken.id() == PythonTokenId.ERROR) {
                if (ts.movePrevious()) {
                    if (ts.token().id() == beginToken) {
                        insideString = true;
                    }
                }
            }
        }

        if (!insideString) {
            // check if the caret is at the very end of the line and there
            // is an unterminated string literal
            if ((token.id() == PythonTokenId.WHITESPACE) && eol) {
                if ((dotPos - 1) > 0) {
                    token = PythonLexerUtils.getToken(doc, dotPos - 1);
                    // XXX TODO use language embedding to handle this
                    insideString = (token.id() == PythonTokenId.STRING_LITERAL);
                }
            }
        }

        if (insideString) {
            if (eol) {
                return false; // do not complete
            } else {
                //#69524
                char chr = doc.getChars(dotPos, 1)[0];

                if (chr == bracket) {
                    if (!isAfter) {
                        doc.insertString(dotPos, "" + bracket, null); //NOI18N
                    } else {
                        if (!(dotPos < doc.getLength() - 1 && doc.getText(dotPos + 1, 1).charAt(0) == bracket)) {
                            return true;
                        }
                    }

                    doc.remove(dotPos, 1);

                    return true;
                }
            }
        }

        if ((completablePosition && !insideString) || eol) {
            doc.insertString(dotPos, "" + bracket + (isAfter ? "" : matching(bracket)), null); //NOI18N

            return true;
        }

        return false;
    }

    /**
     * Checks whether dotPos is a position at which bracket and quote
     * completion is performed. Brackets and quotes are not completed
     * everywhere but just at suitable places .
     * @param doc the document
     * @param dotPos position to be tested
     */
    private boolean isCompletablePosition(BaseDocument doc, int dotPos)
            throws BadLocationException {
        if (dotPos == doc.getLength()) { // there's no other character to test

            return true;
        } else {
            // test that we are in front of ) , " or '
            char chr = doc.getChars(dotPos, 1)[0];

            return ((chr == ')') || (chr == ',') || (chr == '\"') || (chr == '\'') || (chr == ' ') ||
                    (chr == ']') || (chr == '}') || (chr == '\n') || (chr == '\t') || (chr == ';'));
        }
    }

    private boolean isQuoteCompletablePosition(BaseDocument doc, int dotPos)
            throws BadLocationException {
        if (dotPos == doc.getLength()) { // there's no other character to test

            return true;
        } else {
            // test that we are in front of ) , " or ' ... etc.
            int eol = Utilities.getRowEnd(doc, dotPos);

            if ((dotPos == eol) || (eol == -1)) {
                return false;
            }

            int firstNonWhiteFwd = Utilities.getFirstNonWhiteFwd(doc, dotPos, eol);

            if (firstNonWhiteFwd == -1) {
                return false;
            }

            char chr = doc.getChars(firstNonWhiteFwd, 1)[0];

//            if (chr == '%' && RubyUtils.isRhtmlDocument(doc)) {
//                return true;
//            }

            return ((chr == ')') || (chr == ',') || (chr == '+') || (chr == '}') || (chr == ';') ||
                    (chr == ']') || (chr == '/'));
        }
    }

    /**
     * Returns for an opening bracket or quote the appropriate closing
     * character.
     */
    private char matching(char bracket) {
        switch (bracket) {
        case '(':
            return ')';

        case '[':
            return ']';

        case '\"':
            return '\"'; // NOI18N

        case '\'':
            return '\'';

        case '{':
            return '}';

        case '}':
            return '{';

        default:
            return bracket;
        }
    }

    @Override
    public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
        PythonTree root = PythonAstUtils.getRoot(info);
        if (root != null) {
            List<OffsetRange> ranges = new ArrayList<>();
            AstPath path = AstPath.get(root, caretOffset);
            OffsetRange prevRange = OffsetRange.NONE;
            for (PythonTree node : path) {
                OffsetRange astRange = PythonAstUtils.getRange(node);
                OffsetRange lexRange = PythonLexerUtils.getLexerOffsets((PythonParserResult) info, astRange);
                if (lexRange != OffsetRange.NONE) {
                    if (prevRange == OffsetRange.NONE ||
                            prevRange.getStart() > lexRange.getStart() ||
                            prevRange.getEnd() < lexRange.getEnd()) {
                        ranges.add(lexRange);
                    }
                    prevRange = lexRange;
                }
            }

            int docLength = info.getSnapshot().getSource().getDocument(false).getLength();
            if (prevRange == OffsetRange.NONE || prevRange.getStart() > 0 ||
                    prevRange.getEnd() < docLength) {
                ranges.add(new OffsetRange(0, docLength));
            }

            return ranges;
        }

        return null;
    }

    // Camel case logic for identifier jumps. A bit ugly, rewrite.
    @Override
    public int getNextWordOffset(Document document, int offset, boolean reverse) {
        BaseDocument doc = (BaseDocument)document;
        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, offset);
        if (ts == null) {
            return -1;
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }
        if (reverse && ts.offset() == offset) {
            if (!ts.movePrevious()) {
                return -1;
            }
        }

        Token<? extends PythonTokenId> token = ts.token();
        TokenId id = token.id();

        if (id == PythonTokenId.WHITESPACE) {
            // Just eat up the space in the normal IDE way
            if ((reverse && ts.offset() < offset) || (!reverse && ts.offset() > offset)) {
                return ts.offset();
            }
            while (id == PythonTokenId.WHITESPACE) {
                if (reverse && !ts.movePrevious()) {
                    return -1;
                } else if (!reverse && !ts.moveNext()) {
                    return -1;
                }

                token = ts.token();
                id = token.id();
            }
            if (reverse) {
                int start = ts.offset() + token.length();
                if (start < offset) {
                    return start;
                }
            } else {
                int start = ts.offset();
                if (start > offset) {
                    return start;
                }
            }

        }

        if (id == PythonTokenId.IDENTIFIER) {
            String s = token.text().toString();
            int length = s.length();
            int wordOffset = offset - ts.offset();
            if (reverse) {
                // Find previous
                int offsetInImage = offset - 1 - ts.offset();
                if (offsetInImage < 0) {
                    return -1;
                }
                if (offsetInImage < length && Character.isUpperCase(s.charAt(offsetInImage))) {
                    for (int i = offsetInImage - 1; i >= 0; i--) {
                        char charAtI = s.charAt(i);
                        if (charAtI == '_') {
                            // return offset of previous uppercase char in the identifier
                            return ts.offset() + i + 1;
                        } else if (!Character.isUpperCase(charAtI)) {
                            // return offset of previous uppercase char in the identifier
                            return ts.offset() + i + 1;
                        }
                    }
                    return ts.offset();
                } else {
                    for (int i = offsetInImage - 1; i >= 0; i--) {
                        char charAtI = s.charAt(i);
                        if (charAtI == '_') {
                            return ts.offset() + i + 1;
                        }
                        if (Character.isUpperCase(charAtI)) {
                            // now skip over previous uppercase chars in the identifier
                            for (int j = i; j >= 0; j--) {
                                char charAtJ = s.charAt(j);
                                if (charAtJ == '_') {
                                    return ts.offset() + j + 1;
                                }
                                if (!Character.isUpperCase(charAtJ)) {
                                    // return offset of previous uppercase char in the identifier
                                    return ts.offset() + j + 1;
                                }
                            }
                            return ts.offset();
                        }
                    }

                    return ts.offset();
                }
            } else {
                // Find next
                int start = wordOffset + 1;
                if (wordOffset < 0 || wordOffset >= s.length()) {
                    // Probably the end of a token sequence, such as this:
                    // <%s|%>
                    return -1;
                }
                if (Character.isUpperCase(s.charAt(wordOffset))) {
                    // if starting from a Uppercase char, first skip over follwing upper case chars
                    for (int i = start; i < length; i++) {
                        char charAtI = s.charAt(i);
                        if (!Character.isUpperCase(charAtI)) {
                            break;
                        }
                        if (s.charAt(i) == '_') {
                            return ts.offset() + i;
                        }
                        start++;
                    }
                }
                for (int i = start; i < length; i++) {
                    char charAtI = s.charAt(i);
                    if (charAtI == '_' || Character.isUpperCase(charAtI)) {
                        return ts.offset() + i;
                    }
                }
            }
        }

        // Default handling in the IDE
        return -1;
    }
}
