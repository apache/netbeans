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
package org.netbeans.modules.editor.java;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.SourceVersion;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;

/**
 * This static class groups the whole aspect of bracket completion. It is
 * defined to clearly separate the functionality and keep actions clean. The
 * methods of the class are called from different typing hooks <br/> This class
 * is similar to old BraceCompletion but works solely with the token hierarchy
 * and typing hooks context. Every method is called 
 * before anything was inserted into the document.
 */
class TypingCompletion {

    /**
     * Returns true if bracket completion is enabled in options.
     */
    static boolean isCompletionSettingEnabled() {
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);
    }

    /**
     * Check for various conditions and possibly remove two quotes.
     *
     * @param context
     * @throws BadLocationException
     */
    static void removeCompletedQuote(DeletedTextInterceptor.Context context) throws BadLocationException {
        TokenSequence<JavaTokenId> ts = javaTokenSequence(context, false);
        if (ts == null) {
            return;
        }
        char removedChar = context.getText().charAt(0);
        int caretOffset = context.isBackwardDelete() ? context.getOffset() - 1 : context.getOffset();
        if (removedChar == '\"') {
            if ((ts.token().id() == JavaTokenId.STRING_LITERAL && ts.offset() == caretOffset) ||
                (ts.token().id() == JavaTokenId.MULTILINE_STRING_LITERAL && ts.offset() == caretOffset - 2)) {
                context.getDocument().remove(caretOffset, 1);
            }
        } else if (removedChar == '\'') {
            if (ts.token().id() == JavaTokenId.CHAR_LITERAL && ts.offset() == caretOffset) {
                context.getDocument().remove(caretOffset, 1);
            }
        }
    }

    /**
     * Check for various conditions and possibly remove two brackets.
     *
     * @param context
     * @throws BadLocationException
     */
    static void removeBrackets(DeletedTextInterceptor.Context context) throws BadLocationException {
        int caretOffset = context.isBackwardDelete() ? context.getOffset() - 1 : context.getOffset();
        TokenSequence<JavaTokenId> ts = javaTokenSequence(context.getDocument(), caretOffset, false);
        if (ts == null) {
            return;
        }

        switch (ts.token().id()) {
            case RPAREN:
                if (tokenBalance(context.getDocument(), JavaTokenId.LPAREN) != 0) {
                    context.getDocument().remove(caretOffset, 1);
                }
                break;
            case RBRACKET:
                if (tokenBalance(context.getDocument(), JavaTokenId.LBRACKET) != 0) {
                    context.getDocument().remove(caretOffset, 1);
                }
                break;
        }
    }

    /**
     * Check for various conditions and possibly skip a closing bracket.
     *
     * @param context
     * @return relative caretOffset change
     * @throws BadLocationException
     */
    static int skipClosingBracket(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(context, false);

        if (javaTS == null) {
            return -1;
        }

        Token<JavaTokenId> currentToken = javaTS.token();
        JavaTokenId currentId = currentToken.id();

        if (isString(currentId)) {
            switch (currentToken.partType()) {
                case MIDDLE: case END:
                    if (currentToken.text().charAt(0) == '}') {
                        context.setText("", 0);  // NOI18N
                        return context.getOffset() + 1;
                    }
                    break;
            }
            return -1;
        }

        if (currentId != JavaTokenId.RPAREN && currentId != JavaTokenId.RBRACKET) {
            return -1;
        }

        JavaTokenId bracketId = bracketCharToId(context.getText().charAt(0));
        if (isSkipClosingBracket(context, javaTS, bracketId)) {
            context.setText("", 0);  // NOI18N
            return context.getOffset() + 1;
        }
        return -1;
    }
 
    /**
     * Check for various conditions and possibly add a pairing bracket.
     *
     * @param context
     * @throws BadLocationException
     */
    static void completeOpeningBracket(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        JavaTokenId currentToken = javaTokenSequence(context, false).token().id();

        if (isComment(currentToken)) {
            return;
        }

        char insChr = context.getText().charAt(0);

        if (isString(currentToken)) {
            if (context.getOffset() >= 1 && insChr == '{') {
                char chr = context.getDocument().getText(context.getOffset() - 1, 1).charAt(0);

                if (chr == '\\') {
                    context.setText("{}", 1);  // NOI18N
                }
            }

            return ;
        }

        if (insChr == '{') {
            //curly brace should only be matched in string templates:
            return ;
        }

        char chr = context.getDocument().getText(context.getOffset(), 1).charAt(0);

        if (chr == ')' || chr == ',' || chr == '\"' || chr == '\'' || chr == ' ' || chr == ']' || chr == '}' || chr == '\n' || chr == '\t' || chr == ';') {
            context.setText("" + insChr + matching(insChr), 1);  // NOI18N
        }
    }

    /**
     * Called to add semicolon after bracket for some conditions
     *
     * @param context
     * @return relative caretOffset change
     * @throws BadLocationException
     */
    static int moveOrSkipSemicolon(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(context, false);
        if (javaTS == null || isStringOrComment(javaTS.token().id())) {
            return -1;
        }
        if (javaTS.token().id() == JavaTokenId.SEMICOLON) {
            context.setText("", 0); // NOI18N
            return javaTS.offset() + 1;
        }
        int lastParenPos = context.getOffset();
        int index = javaTS.index();
        // Move beyond semicolon
        while (javaTS.moveNext()
                && !(javaTS.token().id() == JavaTokenId.WHITESPACE && javaTS.token().text().toString().contains("\n"))
                && javaTS.token().id() != JavaTokenId.RBRACE) {  // NOI18N
            switch (javaTS.token().id()) {
                case RPAREN:
                    lastParenPos = javaTS.offset();
                    break;
                case WHITESPACE:
                    break;
                default:
                    return -1;
            }
        }
        // Restore javaTS position
        javaTS.moveIndex(index);
        javaTS.moveNext();
        if (isForLoopTryWithResourcesOrLambdaSemicolon(javaTS) || posWithinAnyQuote(context, javaTS) || (lastParenPos == context.getOffset() && !javaTS.token().id().equals(JavaTokenId.RPAREN))) {
            return -1;
        }
        context.setText("", 0); // NOI18N
        context.getDocument().insertString(lastParenPos + 1, ";", null); // NOI18N
        return lastParenPos + 2;
    }

    /**
     * Called to insert either single bracket or bracket pair. 
     *
     * @param context
     * @return relative caretOffset change
     * @throws BadLocationException
     */
    static int completeQuote(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        if (isEscapeSequence(context)) {
            return -1;
        }
        // Examine token id at the caret offset
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(context, true);
        JavaTokenId id = (javaTS != null) ? javaTS.token().id() : null;

        // If caret within comment return false
        boolean caretInsideToken = (id != null)
                && (javaTS.offset() + javaTS.token().length() > context.getOffset()
                || javaTS.token().partType() == PartType.START);
        if (caretInsideToken && (id == JavaTokenId.BLOCK_COMMENT || id == JavaTokenId.JAVADOC_COMMENT || id == JavaTokenId.LINE_COMMENT)) {
            return -1;
        }

        boolean completablePosition = isQuoteCompletablePosition(context);
        boolean insideString = caretInsideToken
                && (id == JavaTokenId.STRING_LITERAL
                || id == JavaTokenId.CHAR_LITERAL
                || id == JavaTokenId.MULTILINE_STRING_LITERAL);

        int lastNonWhite = org.netbeans.editor.Utilities.getRowLastNonWhite((BaseDocument) context.getDocument(), context.getOffset());
        // eol - true if the caret is at the end of line (ignoring whitespaces)
        boolean eol = lastNonWhite < context.getOffset();
        if (insideString) {
            if (eol) {
                return -1;
            } else {
                //#69524
                char chr = context.getDocument().getText(context.getOffset(), 1).charAt(0);
                if (chr == context.getText().charAt(0)) {
                    //#83044
                    if (context.getOffset() > 0) {
                        javaTS.move(context.getOffset() - 1);
                        if (javaTS.moveNext()) {
                            id = javaTS.token().id();
                            if (id == JavaTokenId.STRING_LITERAL || id == JavaTokenId.CHAR_LITERAL || id == JavaTokenId.MULTILINE_STRING_LITERAL) {
                                context.setText("", 0); // NOI18N
                                return context.getOffset() + 1;
                            }
                        }
                    }
                }
            }
        }

        if ((completablePosition && !insideString) || eol) {
            if (context.getText().equals("\"") && context.getOffset() >= 2 &&
                    context.getDocument().getText(context.getOffset() - 2, 2).equals("\"\"") &&
                    isTextBlockSupported(getFileObject((BaseDocument) context.getDocument()))) {
                context.setText("\"\n\"\"\"", 2, true);  // NOI18N
            } else {
                context.setText(context.getText() + context.getText(), 1);
            }
        } else if (context.getText().equals("\"") &&
                isTextBlockSupported(getFileObject((BaseDocument) context.getDocument()))) {
            if ((javaTS != null) && javaTS.moveNext()) {
                id = javaTS.token().id();
                if ((id == JavaTokenId.STRING_LITERAL) && (javaTS.token().text().toString().equals("\"\""))) {
                    if (context.getDocument().getText(context.getOffset(), 2).equals("\"\"")) {
                        context.setText("\"\"\"\n\"", 4, true);
                    }
                }
                javaTS.movePrevious();
                id = javaTS.token().id();
            }
        }
        return -1;
    }

    private static boolean isQuoteCompletablePosition(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        if (context.getOffset() == context.getDocument().getLength()) {
            return true;
        } else {
            for (int i = context.getOffset(); i < context.getDocument().getLength(); i++) {
                char chr = context.getDocument().getText(i, 1).charAt(0);
                if (chr == '\n') {
                    break;
                }
                if (!Character.isWhitespace(chr)) {
                    return (chr == ')' || chr == ',' || chr == '+' || chr == '}' || chr == ';');
                }

            }
            return false;
        }
    }

    private static boolean isEscapeSequence(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        if (context.getOffset() <= 0) {
            return false;
        }

        char[] previousChars;
        for (int i = 2; context.getOffset() - i >= 0; i += 2) {
            previousChars = context.getDocument().getText(context.getOffset() - i, 2).toCharArray();
            if (previousChars[1] != '\\') {
                return false;
            }
            if (previousChars[0] != '\\') {
                return true;
            }
        }
        return context.getDocument().getText(context.getOffset() - 1, 1).charAt(0) == '\\';
    }
    
    private static boolean isTextBlockSupported(
            @NullAllowed FileObject fileObject) {
        SpecificationVersion supportedVer = new SpecificationVersion("13"); //NOI18N

        if (fileObject != null) {
            SpecificationVersion sourceVer = new SpecificationVersion(SourceLevelQuery.getSourceLevel(fileObject));
            if (sourceVer.compareTo(supportedVer) < 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Resolve whether pairing right curly should be added automatically
     * at the caret position or not.
     * <br>
     * There must be only whitespace or line comment or block comment
     * between the caret position
     * and the left brace and the left brace must be on the same line
     * where the caret is located.
     * <br>
     * The caret must not be "contained" in the opened block comment token.
     *
     * @param doc document in which to operate.
     * @param caretOffset offset of the caret.
     * @return true if a right brace '}' should be added
     *  or false if not.
     */
    static boolean isAddRightBrace(BaseDocument doc, int caretOffset) throws BadLocationException {
        if (tokenBalance(doc, JavaTokenId.LBRACE) <= 0) {
            return false;
        }
        int caretRowStartOffset = org.netbeans.editor.Utilities.getRowStart(doc, caretOffset);
        TokenSequence<JavaTokenId> ts = javaTokenSequence(doc, caretOffset, true);
        if (ts == null) {
            return false;
        }
        boolean first = true;
        do {
            if (ts.offset() < caretRowStartOffset) {
                return false;
            }
            switch (ts.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                    break;
                case BLOCK_COMMENT:
                case JAVADOC_COMMENT:
                    if (first && caretOffset > ts.offset() && caretOffset < ts.offset() + ts.token().length()) {
                        // Caret contained within block comment -> do not add anything
                        return false;
                    }
                    break; // Skip
                case LBRACE:
                    return true;
            }
            first = false;
        } while (ts.movePrevious());
        return false;
    }
    
   /**
     * Returns position of the first unpaired closing paren/brace/bracket from the caretOffset
     * till the end of caret row. If there is no such element, position after the last non-white
     * character on the caret row is returned.
     */
    static int getRowOrBlockEnd(BaseDocument doc, int caretOffset, boolean[] insert) throws BadLocationException {
        int rowEnd = org.netbeans.editor.Utilities.getRowLastNonWhite(doc, caretOffset);
        if (rowEnd == -1 || caretOffset >= rowEnd) {
            return caretOffset;
        }
        rowEnd += 1;
        int parenBalance = 0;
        int braceBalance = 0;
        int bracketBalance = 0;
        TokenSequence<JavaTokenId> ts = javaTokenSequence(doc, caretOffset, false);
        if (ts == null) {
            return caretOffset;
        }
        boolean firstToken = true;
        while (ts.offset() < rowEnd) {
            switch (ts.token().id()) {
                case SEMICOLON:
                    if (!isForLoopTryWithResourcesOrLambdaSemicolon(ts)) {
                        return ts.offset() + 1;
                    }
                case LPAREN:
                    parenBalance++;
                    break;
                case RPAREN:
                    if (parenBalance-- == 0) {
                        return ts.offset();
                    }
                    break;
                case LBRACE:
                    braceBalance++;
                    break;
                case RBRACE:
                    if (braceBalance-- == 0) {
                        return ts.offset();
                    }
                    break;
                case LBRACKET:
                    bracketBalance++;
                    break;
                case RBRACKET:
                    if (bracketBalance-- == 0) {
                        return ts.offset();
                    }
                    break;
                case COMMA:
                    if (firstToken) {
                        return caretOffset;
                    }
                    break;
            }
            firstToken = false;
            if (!ts.moveNext()) {
                break;
            }
        }

        insert[0] = false;
        return rowEnd;
    }
    
     static boolean blockCommentCompletion(TypedBreakInterceptor.Context context) {
        return blockCommentCompletionImpl(context, false);
    }

    static boolean javadocBlockCompletion(TypedBreakInterceptor.Context context) {
        return blockCommentCompletionImpl(context, true);
    }

    private static boolean blockCommentCompletionImpl(TypedBreakInterceptor.Context context, boolean javadoc) {
            TokenSequence<JavaTokenId> ts = javaTokenSequence(context, false);
            if (ts == null) {
                return false;
            }
            int dotPosition = context.getCaretOffset();
            ts.move(dotPosition);
            if (!((ts.moveNext() || ts.movePrevious()) && ts.token().id() == (javadoc ? JavaTokenId.JAVADOC_COMMENT : JavaTokenId.BLOCK_COMMENT))) {
                return false;
            }

            int jdoffset = dotPosition - (javadoc ? 3 : 2);
            if (jdoffset >= 0) {
                CharSequence content = org.netbeans.lib.editor.util.swing.DocumentUtilities.getText(context.getDocument());
                if (isOpenBlockComment(content, dotPosition - 1, javadoc) && !isClosedBlockComment(content, dotPosition) && isAtRowEnd(content, dotPosition)) {
                    return true;
                }
            }
        return false;
    }
    
    private static boolean isOpenBlockComment(CharSequence content, int pos, boolean javadoc) {
        for (int i = pos; i >= 0; i--) {
            char c = content.charAt(i);
            if (c == '*' && (javadoc ? i - 2 >= 0 && content.charAt(i - 1) == '*' && content.charAt(i - 2) == '/' : i - 1 >= 0 && content.charAt(i - 1) == '/')) {
                // matched /*
                return true;
            } else if (c == '\n') {
                // no javadoc, matched start of line
                return false;
            } else if (c == '/' && i - 1 >= 0 && content.charAt(i - 1) == '*') {
                // matched javadoc enclosing tag
                return false;
            }
        }

        return false;
    }

    private static boolean isClosedBlockComment(CharSequence txt, int pos) {
        int length = txt.length();
        int quotation = 0;
        for (int i = pos; i < length; i++) {
            char c = txt.charAt(i);
            if (c == '*' && i < length - 1 && txt.charAt(i + 1) == '/') {
                if (quotation == 0 || i < length - 2) {
                    return true;
                }
                // guess it is not just part of some text constant
                boolean isClosed = true;
                for (int j = i + 2; j < length; j++) {
                    char cc = txt.charAt(j);
                    if (cc == '\n') {
                        break;
                    } else if (cc == '"' && j < length - 1 && txt.charAt(j + 1) != '\'') {
                        isClosed = false;
                        break;
                    }
                }

                if (isClosed) {
                    return true;
                }
            } else if (c == '/' && i < length - 1 && txt.charAt(i + 1) == '*') {
                // start of another comment block
                return false;
            } else if (c == '\n') {
                quotation = 0;
            } else if (c == '"' && i < length - 1 && txt.charAt(i + 1) != '\'') {
                quotation = ++quotation % 2;
            }
        }

        return false;
    }

    private static boolean isAtRowEnd(CharSequence txt, int pos) {
        int length = txt.length();
        for (int i = pos; i < length; i++) {
            char c = txt.charAt(i);
            if (c == '\n') {
                return true;
            }
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generalized posWithingString to any token and delimiting
     * character. It works for tokens are delimited by *quote* and
     * extend up to the other *quote* or whitespace in case of an
     * incomplete token.
     * @param doc the document
     * @param caretOffset position of typed quote
     */
    static boolean posWithinString(Document doc, int caretOffset) {
        return posWithinQuotes(doc, caretOffset, JavaTokenId.STRING_LITERAL);
    }
    
 static boolean posWithinTextBlock(Document doc, int caretOffset) {
        TokenSequence<JavaTokenId> javaTS=javaTokenSequence(doc,caretOffset, false);
        if(javaTS == null)return false;
        boolean movePrevious = javaTS.movePrevious();
        if(!movePrevious)return false;
        return posWithinQuotes(doc, caretOffset, JavaTokenId.STRING_LITERAL) && javaTS.token().text().toString().equals("\"\"");
    }
    private static boolean posWithinQuotes(Document doc, int caretOffset, JavaTokenId tokenId) {
        TokenSequence<JavaTokenId> javaTS = javaTokenSequence(doc, caretOffset, false);
        if (javaTS != null) {
            if (javaTS.token().id() != tokenId) {
                return false;
            }
            else if (caretOffset > javaTS.offset() && caretOffset < javaTS.offset() + javaTS.token().length()) {
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }
    
    private static boolean posWithinAnyQuote(TypedTextInterceptor.MutableContext context, TokenSequence<JavaTokenId> javaTS) throws BadLocationException {
        if (javaTS.token().id() == JavaTokenId.STRING_LITERAL || javaTS.token().id() == JavaTokenId.CHAR_LITERAL) {
            char chr = context.getDocument().getText(context.getOffset(), 1).charAt(0);
            return (context.getOffset() - javaTS.offset() == 1 || (chr != '"' && chr != '\''));
        }
        return false;
    }

    private static boolean isForLoopTryWithResourcesOrLambdaSemicolon(TokenSequence<JavaTokenId> ts) {
        int parenDepth = 0; // parenthesis depth
        int braceDepth = 0; // brace depth
        boolean semicolonFound = false; // next semicolon
        int tsOrigIndex = ts.index();
        try {
            while (ts.movePrevious()) {
                switch (ts.token().id()) {
                    case LPAREN:
                        if (parenDepth == 0) { // could be a 'for (' or 'try ('
                            while (ts.movePrevious()) {
                                switch (ts.token().id()) {
                                    case WHITESPACE:
                                    case BLOCK_COMMENT:
                                    case JAVADOC_COMMENT:
                                    case LINE_COMMENT:
                                        break; // skip
                                    case FOR:
                                    case TRY:
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                            return false;
                        } else { // non-zero depth
                            parenDepth--;
                        }
                        break;

                    case RPAREN:
                        parenDepth++;
                        break;

                    case LBRACE:
                        if (braceDepth == 0) { // unclosed left brace
                            if (!semicolonFound) {
                                while (ts.movePrevious()) {
                                    switch (ts.token().id()) {
                                        case WHITESPACE:
                                        case BLOCK_COMMENT:
                                        case JAVADOC_COMMENT:
                                        case LINE_COMMENT:
                                            break; // skip
                                        case ARROW:
                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            }
                            return false;
                        }
                        braceDepth--;
                        break;

                    case RBRACE:
                        braceDepth++;
                        break;

                    case SEMICOLON:
                        if (semicolonFound) { // one semicolon already found
                            return false;
                        }
                        semicolonFound = true;
                        break;
                }
            }
        } finally {
            // Restore orig TS's location
            ts.moveIndex(tsOrigIndex);
            ts.moveNext();
        }
        return false;
    }
    
    private static Set<JavaTokenId> STOP_TOKENS_FOR_SKIP_CLOSING_BRACKET = EnumSet.of(JavaTokenId.LBRACE, JavaTokenId.RBRACE, JavaTokenId.SEMICOLON);

    private static boolean isSkipClosingBracket(TypedTextInterceptor.MutableContext context, TokenSequence<JavaTokenId> javaTS, JavaTokenId rightBracketId) {
        if (context.getOffset() == context.getDocument().getLength()) {
            return false;
        }

        boolean skipClosingBracket = false;

        if (javaTS != null && javaTS.token().id() == rightBracketId) {
            JavaTokenId leftBracketId = matching(rightBracketId);
            // Skip all the brackets of the same type that follow the last one
            do {
                if (STOP_TOKENS_FOR_SKIP_CLOSING_BRACKET.contains(javaTS.token().id())
                        || (javaTS.token().id() == JavaTokenId.WHITESPACE && javaTS.token().text().toString().contains("\n"))) {  // NOI18N
                    while (javaTS.token().id() != rightBracketId) {
                        boolean isPrevious = javaTS.movePrevious();
                        if (!isPrevious) {
                            break;
                        }
                    }
                    break;
                }
            } while (javaTS.moveNext());

            // token var points to the last bracket in a group of two or more right brackets
            // Attempt to find the left matching bracket for it
            // Search would stop on an extra opening left brace if found
            int braceBalance = 0; // balance of '{' and '}'
            int bracketBalance = -1; // balance of the brackets or parenthesis
            int numOfSemi = 0;
            boolean finished = false;
            while (!finished && javaTS.movePrevious()) {
                JavaTokenId id = javaTS.token().id();
                switch (id) {
                    case LPAREN:
                    case LBRACKET:
                        if (id == leftBracketId) {
                            bracketBalance++;
                            if (bracketBalance == 1) {
                                if (braceBalance != 0) {
                                    // Here the bracket is matched but it is located
                                    // inside an unclosed brace block
                                    // e.g. ... ->( } a()|)
                                    // which is in fact illegal but it's a question
                                    // of what's best to do in this case.
                                    // We chose to leave the typed bracket
                                    // by setting bracketBalance to 1.
                                    // It can be revised in the future.
                                    bracketBalance = 2;
                                }
                                finished = javaTS.offset() < context.getOffset();
                            }
                        }
                        break;

                    case RPAREN:
                    case RBRACKET:
                        if (id == rightBracketId) {
                            bracketBalance--;
                        }
                        break;

                    case LBRACE:
                        braceBalance++;
                        if (braceBalance > 0) { // stop on extra left brace
                            finished = true;
                        }
                        break;

                    case RBRACE:
                        braceBalance--;
                        break;

                    case SEMICOLON:
                        numOfSemi++;
                        break;
                }
            }

            if (bracketBalance == 1 && numOfSemi < 2) {
                finished = false;
                while (!finished && javaTS.movePrevious()) {
                    switch (javaTS.token().id()) {
                        case WHITESPACE:
                        case LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case JAVADOC_COMMENT:
                            break;
                        case FOR:
                            bracketBalance--;
                        default:
                            finished = true;
                            break;
                    }
                }
            }

            skipClosingBracket = bracketBalance != 1;
        }
        return skipClosingBracket;

    }

    /**
     * Returns for an opening bracket or quote the appropriate closing
     * character.
     */
    private static char matching(char bracket) {
        switch (bracket) {
            case '(':
                return ')';
            case '[':
                return ']';
            case '\"':
                return '\"'; // NOI18N
            case '\'':
                return '\'';
            default:
                return ' ';
        }
    }
    
    private static JavaTokenId matching(JavaTokenId id) {
        switch (id) {
            case LPAREN:
                return JavaTokenId.RPAREN;
            case LBRACKET:
                return JavaTokenId.RBRACKET;
            case RPAREN:
                return JavaTokenId.LPAREN;
            case RBRACKET:
                return JavaTokenId.LBRACKET;
            default:
                return null;
        }
    }

    private static JavaTokenId bracketCharToId(char bracket) {
        switch (bracket) {
            case '(':
                return JavaTokenId.LPAREN;
            case ')':
                return JavaTokenId.RPAREN;
            case '[':
                return JavaTokenId.LBRACKET;
            case ']':
                return JavaTokenId.RBRACKET;
            case '{':
                return JavaTokenId.LBRACE;
            case '}':
                return JavaTokenId.RBRACE;
            default:
                throw new IllegalArgumentException("Not a bracket char '" + bracket + '\'');  // NOI18N
        }
    }

    private static int tokenBalance(Document doc, JavaTokenId leftTokenId) {
        TokenBalance tb = TokenBalance.get(doc);
        if (!tb.isTracked(JavaTokenId.language())) {
            tb.addTokenPair(JavaTokenId.language(), JavaTokenId.LPAREN, JavaTokenId.RPAREN);
            tb.addTokenPair(JavaTokenId.language(), JavaTokenId.LBRACKET, JavaTokenId.RBRACKET);
            tb.addTokenPair(JavaTokenId.language(), JavaTokenId.LBRACE, JavaTokenId.RBRACE);
        }
        int balance = tb.balance(JavaTokenId.language(), leftTokenId);
        assert (balance != Integer.MAX_VALUE);
        return balance;
    }

    private static TokenSequence<JavaTokenId> javaTokenSequence(TypedTextInterceptor.MutableContext context, boolean backwardBias) {
        return javaTokenSequence(context.getDocument(), context.getOffset(), backwardBias);
    }

    private static TokenSequence<JavaTokenId> javaTokenSequence(DeletedTextInterceptor.Context context, boolean backwardBias) {
        return javaTokenSequence(context.getDocument(), context.getOffset(), backwardBias);
    }
    
       private static TokenSequence<JavaTokenId> javaTokenSequence(TypedBreakInterceptor.Context context, boolean backwardBias) {
        return javaTokenSequence(context.getDocument(), context.getCaretOffset(), backwardBias);
    }

    /**
     * Get token sequence positioned over a token.
     *
     * @param doc
     * @param caretOffset
     * @param backwardBias
     * @return token sequence positioned over a token that "contains" the offset
     * or null if the document does not contain any java token sequence or the
     * offset is at doc-or-section-start-and-bwd-bias or
     * doc-or-section-end-and-fwd-bias.
     */
    private static TokenSequence<JavaTokenId> javaTokenSequence(Document doc, int caretOffset, boolean backwardBias) {
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        List<TokenSequence<?>> tsList = hi.embeddedTokenSequences(caretOffset, backwardBias);
        // Go from inner to outer TSes
        for (int i = tsList.size() - 1; i >= 0; i--) {
            TokenSequence<?> ts = tsList.get(i);
            if (ts.languagePath().innerLanguage() == JavaTokenId.language()) {
                TokenSequence<JavaTokenId> javaInnerTS = (TokenSequence<JavaTokenId>) ts;
                return javaInnerTS;
            }
        }
        return null;
    }

    private static FileObject getFileObject(BaseDocument doc) {
        return doc != null ? NbEditorUtilities.getFileObject(doc) : null;
    }

    private static Set<JavaTokenId> STRING_AND_COMMENT_TOKENS = EnumSet.of(JavaTokenId.STRING_LITERAL, JavaTokenId.LINE_COMMENT, JavaTokenId.JAVADOC_COMMENT, JavaTokenId.BLOCK_COMMENT, JavaTokenId.CHAR_LITERAL, JavaTokenId.MULTILINE_STRING_LITERAL);

    private static boolean isStringOrComment(JavaTokenId javaTokenId) {
        return STRING_AND_COMMENT_TOKENS.contains(javaTokenId);
    }

    private static Set<JavaTokenId> STRING_TOKENS = EnumSet.of(JavaTokenId.STRING_LITERAL, JavaTokenId.CHAR_LITERAL, JavaTokenId.MULTILINE_STRING_LITERAL);

    private static boolean isString(JavaTokenId javaTokenId) {
        return STRING_TOKENS.contains(javaTokenId);
    }

    private static Set<JavaTokenId> COMMENT_TOKENS = EnumSet.of(JavaTokenId.LINE_COMMENT, JavaTokenId.JAVADOC_COMMENT, JavaTokenId.BLOCK_COMMENT);

    private static boolean isComment(JavaTokenId javaTokenId) {
        return COMMENT_TOKENS.contains(javaTokenId);
    }
}
