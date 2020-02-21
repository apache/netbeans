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
package org.netbeans.modules.cnd.editor.indent;

import java.util.MissingResourceException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.spi.editor.CsmDocGeneratorProvider;
import org.netbeans.modules.cnd.spi.editor.CsmDocGeneratorProvider.Function;
import org.netbeans.modules.cnd.spi.editor.CsmDocGeneratorProvider.Parameter;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor.MutableContext;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 */
public class CppIndentTask extends IndentSupport implements IndentTask {

    private Context context;
    private MutableContext TBIcontext;
    private final Document doc;

    public CppIndentTask(Context context) {
        this.context = context;
        doc = context.document();
    }

    public CppIndentTask(MutableContext context) {
        this.TBIcontext = context;
        doc = context.getDocument();
    }

    public boolean doxyGen() throws BadLocationException {
        int caretOffset = TBIcontext.getCaretOffset();
        ts = CndLexerUtilities.getCppTokenSequence(doc, TBIcontext.getCaretOffset(), false, false);
        if (ts == null) {
            return false;
        }
        TokenItem token = new TokenItem(ts, true);
        if (isMultiLineComment(token)) {
            if (caretOffset == token.getTokenSequence().offset()) {
                return false;
            }
            if (codeStyle == null) {
                codeStyle = CodeStyle.getDefault(doc);
            }
            // Indent the inner lines of the multi-line comment by one
            if (getFormatLeadingStarInComment()) {
                int indent = getTokenColumn(token) + 1;
                try {
                    if (caretOffset - token.getTokenSequence().offset() == 3
                            && doc.getLength() > token.getTokenSequence().offset() + 5
                            && "/***/".equals(doc.getText(token.getTokenSequence().offset(), 5))) { // NOI18N
                        Function function = CsmDocGeneratorProvider.getDefault().getFunction(doc, caretOffset);
                        if (function != null) {
                            StringBuilder buf = createDoc(indent, function);
                            TBIcontext.setText("\n"+buf.toString(), 0, indent+3); // NOI18N
                            return true;
                        }
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            if (token.getTokenID() == CppTokenId.NEW_LINE) {
                ts.movePrevious();
                TokenItem prev = new TokenItem(ts, true);

                if (prev.getTokenID() == CppTokenId.DOXYGEN_LINE_COMMENT
                        && caretOffset - token.getTokenSequence().offset() == 3) {
                    Function function = CsmDocGeneratorProvider.getDefault().getFunction(doc, caretOffset);
                    if (function != null) {
                        if (codeStyle == null) {
                            codeStyle = CodeStyle.getDefault(doc);
                        }
                        
                        int indent = getTokenColumn(prev);
                        String spaces = spaces(indent);
                        
                        StringBuilder buf = new StringBuilder();
                        buf.append(" \n"); // NOI18N
                        for (Parameter p : function.getParametes()) {
                            buf.append(spaces);
                            buf.append("/// \\param ").append(p.getName()).append('\n'); // NOI18N
                        }
                        final String returnType = function.getReturnType();
                        if (returnType != null && !"void".equals(returnType)) { // NOI18N
                            buf.append(spaces);
                            buf.append("/// \\return ").append('\n'); // NOI18N
                        }
                        String r = buf.substring(0, buf.length() - 1);
                        TBIcontext.setText(r, 1, 1);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void reindent() throws BadLocationException {
        if (codeStyle == null) {
            codeStyle = CodeStyle.getDefault(doc);
        }
        int caretOffset = context.caretOffset();
        int lineOffset = context.lineStartOffset(caretOffset);
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();
        int lnStart = NbDocument.findLineNumber((StyledDocument)doc, startOffset);
        int lnEnd = NbDocument.findLineNumber((StyledDocument)doc, endOffset);
        if (lnStart == lnEnd) {
            ts = CndLexerUtilities.getCppTokenSequence(doc, lineOffset, false, false);
            if (ts == null) {
                return;
            }
            int index = ts.index();
            int indent = indentLine(new TokenItem(ts, true), caretOffset, true);
            if (indent >= 0) {
                int tokenIndent = -1;
                if (ts.isValid()) {
                    ts.moveIndex(index);
                    ts.moveNext();
                    tokenIndent = getTokenIndent(moveToFirstLineImportantToken(new TokenItem(ts, false)));
                }
                if (indent != tokenIndent) {
                    context.modifyIndent(lineOffset, indent);
                }
            }
        } else {
            for(int i = lnStart; i <= lnEnd; i++) {
                int lnStartOffset = NbDocument.findLineOffset ((StyledDocument)doc, i);
                ts = CndLexerUtilities.getCppTokenSequence(doc, lnStartOffset, false, false);
                if (ts == null) {
                    return;
                }
                int index = ts.index();
                int indent = indentLine(new TokenItem(ts, true), lnStartOffset, false);
                if (indent >= 0) {
                    int tokenIndent = -1;
                    if (ts.isValid()) {
                        ts.moveIndex(index);
                        ts.moveNext();
                        tokenIndent = getTokenIndent(moveToFirstLineImportantToken(new TokenItem(ts, false)));
                    }
                    if (indent != tokenIndent) {
                        context.modifyIndent(lnStartOffset, indent);
                    }
                }
            }
        }
    }

    @Override
    public ExtraLock indentLock() {
        return null;
    }
    
    private String spaces(int i){
        StringBuilder buf = new StringBuilder(i);
        if (!expandTabs()) {
            int tabSize = getTabSize();
            if (tabSize > 1) {
                while (i >= tabSize) {
                    buf.append('\t'); //NOI18N
                    i -= tabSize;
                }
            }
        }
        for(;i>0;i--){
            buf.append(' '); //NOI18N
        }
        return buf.toString();
    }

    private StringBuilder createDoc(int indent, Function function) throws MissingResourceException {
        StringBuilder buf = new StringBuilder();
        buf.append(spaces(indent));
        buf.append("* ").append("\n"); // NOI18N
        for (Parameter p : function.getParametes()) {
            buf.append(spaces(indent));
            buf.append("* @param ").append(p.getName()).append('\n'); // NOI18N
        }
        final String returnType = function.getReturnType();
        if (returnType != null && !"void".equals(returnType)) { // NOI18N
            buf.append(spaces(indent));
            buf.append("* @return ").append('\n'); // NOI18N
        }
        buf.append(spaces(indent));
        return buf;
    }

    private TokenItem moveToFirstLineImportantToken(TokenItem token) {
        TokenItem t = token;
        while (true) {
            if (t == null) {
                return token;
            }
            TokenId tokenID = t.getTokenID();
            if(tokenID instanceof CppTokenId) {
                switch ((CppTokenId)tokenID) {
                    case NEW_LINE:
                    case PREPROCESSOR_DIRECTIVE:
                        if (t.isSkipPP()) {
                            return token;
                        } else {
                            return t;
                        }
                    case WHITESPACE:
                        break;
                    default:
                        return t;
                }
            }
            token = t;
            t = token.getNext();
        }
    }

    private int indentLine(TokenItem token, int caretOffset, boolean singleLine) {
        //if ((dotPos >= 1 && DocumentUtilities.getText(doc).charAt(dotPos-1) != '\\')
        //    || (dotPos >= 2 && DocumentUtilities.getText(doc).charAt(dotPos-2) == '\\')) {
        if (token.getTokenID() == CppTokenId.STRING_LITERAL || token.getTokenID() == CppTokenId.RAW_STRING_LITERAL ||
                token.getTokenID() == CppTokenId.CHAR_LITERAL) {
            int start = token.getTokenSequence().offset();
            Token<TokenId> tok = token.getTokenSequence().token();
            if (start < caretOffset && caretOffset < start + tok.length()) {
                // if insede literal
                if (caretOffset >= start + 2 && tok.text().charAt(caretOffset - start - 2) == '\\') {
                    if (!(caretOffset > start + 2 && tok.text().charAt(caretOffset - start - 3) == '\\')) {
                        return -1;
                    }
                }
            }
        }
        if (token.getTokenID() == CppTokenId.NEW_LINE) {
            TokenItem prev = token.getPrevious();
            if (prev != null && prev.getTokenID() == CppTokenId.ESCAPED_LINE) {
                return -1;
            }
        }

        if (isMultiLineComment(token)) {
            if (caretOffset == token.getTokenSequence().offset()) {
                return findIndent(token);
            }
            // Indent the inner lines of the multi-line comment by one
            if (!getFormatLeadingStarInComment()) {
                return getTokenColumn(token) + 1;
            } else {
                int indent = getTokenColumn(token) + 1;
                try {
                    if (singleLine) {
                        if (caretOffset - token.getTokenSequence().offset() == 4
                                && doc.getLength() > token.getTokenSequence().offset() + 6
                                && "/**\n*/".equals(doc.getText(token.getTokenSequence().offset(), 6))) { // NOI18N
                            Function function = CsmDocGeneratorProvider.getDefault().getFunction(doc, caretOffset);
                            if (function != null) {
                                StringBuilder buf = createDoc(indent, function);
                                doc.insertString(caretOffset, buf.toString(), null);
                                context.setCaretOffset(caretOffset+indent+2);
                            }
                        } else {
                            if (!"*".equals(doc.getText(caretOffset, 1))) { // NOI18N
                                if (caretOffset > 0 && "\n".equals(doc.getText(caretOffset - 1, 1))) { // NOI18N
                                    doc.insertString(caretOffset, "* ", null); // NOI18N
                                }
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return indent;
            }
        }
        TokenItem ppToken = moveToFirstLineImportantToken(new TokenItem(ts, false));
        if (isPreprocessorLine(ppToken)) {
            if (doc instanceof BaseDocument) {
                if (HotCharIndent.isTokenContinue((BaseDocument)doc, caretOffset)) {
                    try {
                        int rowStart = Utilities.getRowStart((BaseDocument)doc, caretOffset);
                        if (rowStart > 1) {
                            rowStart = Utilities.getRowStart((BaseDocument)doc, rowStart-1);
                            String text = doc.getText(rowStart, caretOffset- rowStart);
                            int indent = 0;
                            for(int i = 0; i < text.length(); i++) {
                                if (text.charAt(i) == ' ') {
                                    indent++;
                                } else if (text.charAt(i) == '\t') {
                                    if (getTabSize() > 0) {
                                        indent = (indent/getTabSize()+1)*getTabSize();
                                    }
                                } else {
                                    break;
                                }
                            }
                            return indent;
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    return -1;
                }
            }
            if (codeStyle.sharpAtStartLine()) {
                return 0;
            } else {
                switch (codeStyle.indentPreprocessorDirectives()){
                    case PREPROCESSOR_INDENT:
                        return findPPIndent(ppToken);
                    case START_LINE:
                        return 0;
                    case CODE_INDENT:
                        break;
                }
            }
        }
        return findIndent(moveToFirstLineImportantToken(token));
    }

    /** Is given token a preprocessor **/
    private boolean isPreprocessorLine(TokenItem token) {
        if (token != null) {
            return CppTokenId.PREPROCESSOR_KEYWORD_CATEGORY.equals(token.getTokenID().primaryCategory())
                    || CppTokenId.PREPROCESSOR_CATEGORY.equals(token.getTokenID().primaryCategory());
        }
        return false;
    }

    private boolean isMultiLineComment(TokenItem token) {
        return (token.getTokenID() == CppTokenId.BLOCK_COMMENT || token.getTokenID() == CppTokenId.DOXYGEN_COMMENT);
    }

    /** Check whether the given token is multi-line comment
     * that starts with a slash and an asterisk.
     */
    private boolean isCCDocComment(TokenItem token) {
        return isMultiLineComment(token);
    }

    /**
     * Find the indentation for the first token on the line.
     * The given token is also examined in some cases.
     */
    private int findPPIndent(TokenItem token) {
        int indent = -1; // assign invalid indent
        // First check the given token
        if (token != null) {
            CppTokenId currentId = token.getTokenPPID();
            TokenItem prev = findPreviousPP(token);
            if (prev == null) {
                return 0;
            }
            CppTokenId prevId = prev.getTokenPPID();
            switch (prevId) {
                case PREPROCESSOR_IF:
                case PREPROCESSOR_IFDEF:
                case PREPROCESSOR_IFNDEF:
                case PREPROCESSOR_ELSE:
                case PREPROCESSOR_ELIF:
                    switch (currentId) {
                        case PREPROCESSOR_ELSE:
                        case PREPROCESSOR_ELIF:
                        case PREPROCESSOR_ENDIF:
                            indent = getTokenIndent(prev);
                            break;
                        default:
                            indent = getTokenIndent(prev)+ getShiftWidth();
                            break;
                    }
                    break;
                case PREPROCESSOR_DIRECTIVE:
                case PREPROCESSOR_START:
                case PREPROCESSOR_START_ALT:
                case PREPROCESSOR_DEFINE:
                case PREPROCESSOR_UNDEF:
                case PREPROCESSOR_INCLUDE:
                case PREPROCESSOR_INCLUDE_NEXT:
                case PREPROCESSOR_LINE:
                case PREPROCESSOR_IDENT:
                case PREPROCESSOR_PRAGMA:
                case PREPROCESSOR_WARNING:
                case PREPROCESSOR_ERROR:
                case PREPROCESSOR_DEFINED:
                    switch (currentId) {
                        case PREPROCESSOR_ELSE:
                        case PREPROCESSOR_ELIF:
                        case PREPROCESSOR_ENDIF:
                            // need find correspondend if-else
                            TokenItem prevIf = matchPreviousPP(prev);
                            if (prevIf != null) {
                                indent = getTokenIndent(prevIf);
                            } else {
                                indent = getTokenIndent(prev);
                            }
                            break;
                        default:
                            indent = getTokenIndent(prev);
                            break;
                    }
                    break;
                case PREPROCESSOR_ENDIF:
                    switch (currentId) {
                        case PREPROCESSOR_ELSE:
                        case PREPROCESSOR_ELIF:
                        case PREPROCESSOR_ENDIF:
                            indent = getTokenIndent(prev) - getShiftWidth();;
                            break;
                        default:
                            indent = getTokenIndent(prev);
                            break;
                    }
                    break;
            }
        }
        return indent;
    }

    private TokenItem findPreviousPP(TokenItem token) {
        while (token != null) {
            TokenItem t = token.getPrevious();
            if (t != null) {
                if (isPreprocessorLine(t)){
                    return t;
                }
            }
            token = t;
        }
        return null;
    }

    private TokenItem matchPreviousPP(TokenItem token) {
        int level = 1;
        while (token != null) {
            TokenItem t = findPreviousPP(token);
            if (t != null) {
                switch (t.getTokenPPID()){
                    case PREPROCESSOR_IF:
                    case PREPROCESSOR_IFDEF:
                    case PREPROCESSOR_IFNDEF:
                        level--;
                        if (level <= 0) {
                            return t;
                        }
                        break;
                    case PREPROCESSOR_ELSE:
                    case PREPROCESSOR_ELIF:
                        if (level == 0) {
                            return t;
                        }
                        break;
                    case PREPROCESSOR_ENDIF:
                        level++;
                        break;
                    default:
                        break;
                }
            }
            token = t;
        }
        return null;

    }

    /**
     * Find the indentation for the first token on the line.
     * The given token is also examined in some cases.
     */
    private int findIndent(TokenItem token) {
        int indent = -1; // assign invalid indent

        // First check the given token
        if (token != null) {
            TokenId tokenID = token.getTokenID();
            if(tokenID instanceof CppTokenId) {
                switch ((CppTokenId)tokenID) {
                    case ELSE:
                        TokenItem ifss = findIf(token);
                        if (ifss != null) {
                            indent = getTokenIndent(ifss);
                        }
                        break;

                    case LBRACE:
                        TokenItem stmt = findStatement(token);
                        if (stmt == null) {
                            indent = 0;
                        } else {
                            TokenId stmtTokenID = stmt.getTokenID();
                            if(stmtTokenID instanceof CppTokenId) {
                                switch ((CppTokenId)stmtTokenID) {
                                    case DO:
                                    case FOR:
                                    case IF:
                                    case WHILE:
                                    case ELSE:
                                    case TRY:
                                    case ASM:
                                    case CATCH:
                                        indent = getTokenIndent(stmt);
                                        if (isHalfIndentNewlineBeforeBrace()) {
                                            indent += getShiftWidth() / 2;
                                        } else if (isFullIndentNewlineBeforeBrace()) {
                                            indent += getShiftWidth();
                                        } 
                                        break;
                                    case SWITCH:
                                        indent = getTokenIndent(stmt);
                                        if (isHalfIndentNewlineBeforeBraceSwitch()) {
                                            indent += getShiftWidth() / 2;
                                        } else if (isFullIndentNewlineBeforeBraceSwitch()) {
                                            indent += getShiftWidth();
                                        } 
                                        break;

                                    case LBRACKET:
                                        indent = getTokenIndent(stmt);
                                        if (isHalfIndentNewlineBeforeBraceLambda()) {
                                            indent += getShiftWidth() / 2;
                                        } else if (isFullIndentNewlineBeforeBraceLambda()) {
                                            indent += getShiftWidth();
                                        } 
                                        break;

                                    case LBRACE:
                                        indent = getTokenIndent(stmt) + getShiftWidth();
                                        break;

                                    default:
                                        stmt = findStatementStart(token);
                                        if (stmt == null) {
                                            indent = 0;
                                        } else if (stmt == token) {
                                            stmt = findStatement(token); // search for delimiter
                                            indent = (stmt != null) ? indent = getTokenIndent(stmt) : 0;
                                        } else { // valid statement
                                            indent = getTokenIndent(stmt);
                                            if (stmt.getTokenID() == CppTokenId.LBRACE ) {
                                                indent += getShiftWidth();
                                            } else if (isFullIndentNewlineBeforeBraceDeclaration()) {
                                                indent += getShiftWidth();
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                        break;

                    case RBRACE:
                        TokenItem rbmt = findMatchingToken(token, null, CppTokenId.LBRACE, true);
                        if (rbmt != null) { // valid matching left-brace
                            TokenItem t = findStatement(rbmt);
                            boolean forceFirstNonWhitespace = false;
                            if (t == null) {
                                t = rbmt; // will get indent of the matching brace
                            } else {
                                TokenId tTokenID = t.getTokenID();
                                if(tTokenID instanceof CppTokenId) {
                                    switch ((CppTokenId)tTokenID) {
                                        case SEMICOLON:
                                        case LBRACE:
                                        case RBRACE: {
                                            t = rbmt;
                                            forceFirstNonWhitespace = true;
                                        }
                                    }
                                }
                            }
                            // the right brace must be indented to the first
                            // non-whitespace char - forceFirstNonWhitespace=true
                            if (forceFirstNonWhitespace) {
                                indent = getTokenColumnAfterBrace(t);
                            } else {
                                indent = getTokenIndent(t);
                            }
                            TokenId tTokenID = t.getTokenID();
                            if(tTokenID instanceof CppTokenId) {
                                switch ((CppTokenId)tTokenID) {
                                    case FOR:
                                    case IF:
                                    case WHILE:
                                    case DO:
                                    case ELSE:
                                    case TRY:
                                    case ASM:
                                    case CATCH:
                                        if (isHalfIndentNewlineBeforeBrace()) {
                                            indent += getShiftWidth() / 2;
                                        } else if (isFullIndentNewlineBeforeBrace()) {
                                            indent += getShiftWidth();
                                        } 
                                        break;
                                    case SWITCH:
                                        if (isHalfIndentNewlineBeforeBraceSwitch()) {
                                            indent += getShiftWidth() / 2;
                                        } else if (isFullIndentNewlineBeforeBraceSwitch()) {
                                            indent += getShiftWidth();
                                        } 
                                        break;
                                    case LBRACKET:
                                        if (isHalfIndentNewlineBeforeBraceLambda()) {
                                            indent += getShiftWidth() / 2;
                                        } else if (isFullIndentNewlineBeforeBraceLambda()) {
                                            indent += getShiftWidth();
                                        } 
                                        break;
                                    default:
                                        if (isFullIndentNewlineBeforeBraceDeclaration()) {
                                            indent += getShiftWidth();
                                        }
                                }
                            }
                        } else { // no matching left brace
                            indent = getTokenIndent(token); // leave as is
                        }
                        break;

                    case CASE:
                    case DEFAULT:
                        TokenItem swss = findSwitch(token);
                        if (swss != null) {
                            indent = getTokenIndent(swss);
                            if (indentCasesFromSwitch()) {
                                indent += getShiftWidth();
                            } else if (isHalfIndentNewlineBeforeBraceSwitch()) {
                                indent += getShiftWidth() / 2;
                            }
                        }
                        break;
                    case PUBLIC:
                    case PRIVATE:
                    case PROTECTED:
                        TokenItem cls = findClassifier(token);
                        if (cls != null) {
                            indent = getTokenIndent(cls);
                            if (isHalfIndentVisibility()) {
                                indent += getShiftWidth() / 2;
                            }
                        }
                        break;
                    case CLASS:
                    case STRUCT:
                        TokenItem clsTemplate = findClassifierStart(token);
                        if (clsTemplate != null) {
                            indent = getTokenIndent(clsTemplate);
                        }
                        break;
                }
            }
        }

        // If indent not found, search back for the first important token
        if (indent < 0) { // if not yet resolved
            TokenItem t = findImportantToken(token, null, true);
            if (t != null) { // valid important token
                TokenId tokenID = t.getTokenID();
                if(tokenID instanceof CppTokenId) {
                    switch ((CppTokenId)tokenID) {
                        case SEMICOLON: // semicolon found
                            TokenItem tt = findStatementStart(token);
                            // preprocessor tokens are not important (bug#22570)
                            if (tt != null) {
                                TokenId ttTokenID = tt.getTokenID();
                                if(ttTokenID instanceof CppTokenId) {
                                    switch ((CppTokenId)ttTokenID) {
                                        case PUBLIC:
                                        case PRIVATE:
                                        case PROTECTED:
                                            indent = getTokenIndent(tt) + getShiftWidth();
                                            if (isHalfIndentVisibility()) {
                                                indent -= getShiftWidth() / 2;
                                            }
                                            break;
                                        case FOR:
                                            if (isForLoopSemicolon(t)) {
                                                if (alignMultilineFor()) {
                                                    TokenItem lparen = getLeftParen(t, tt);
                                                    if (lparen != null) {
                                                        indent = getTokenColumn(lparen) + 1;
                                                        break;
                                                    }
                                                }
                                                indent = getTokenIndent(tt) + getFormatStatementContinuationIndent();
                                            } else {
                                                indent = getTokenIndent(tt);
                                            }
                                            break;
                                        default:
                                            indent = getTokenIndent(tt);
                                            break;
                                    }
                                }
                            }
                            break;

                        case LBRACE:
                            TokenItem lbss = findStatementStart(t, false);
                            if (lbss == null) {
                                lbss = t;
                            }
                            TokenId lbssTokenID = lbss.getTokenID();
                            if(lbssTokenID instanceof CppTokenId) {
                                switch ((CppTokenId)lbssTokenID) {
                                    case FOR:
                                    case IF:
                                    case WHILE:
                                    case DO:
                                    case ELSE:
                                    case TRY:
                                    case ASM:
                                    case CATCH:
                                    case SWITCH:
                                        indent = getTokenIndent(lbss) + getShiftWidth();
                                        break;
                                    case NAMESPACE:
                                        if (indentNamespace()) {
                                            indent = getTokenIndent(lbss) + getRightIndentDeclaration();
                                        } else {
                                            indent = getTokenIndent(lbss);
                                        }
                                        break;
                                    default:
                                        indent = getTokenIndent(lbss) + getRightIndentDeclaration();
                                        break;
                                }
                            }
                            break;

                        case RBRACE:
                            TokenItem t3 = findStatementStart(token, true);
                            if (t3 != null) {
                                indent = getTokenIndent(t3);
                            }
                            break;

                        case COLON:
                            TokenItem ttt = getVisibility(t);
                            if (ttt != null) {
                                indent = getTokenIndent(ttt) + getRightIndentDeclaration();
                                if (isHalfIndentVisibility()) {
                                    indent -= getShiftWidth() / 2;
                                }
                            } else {
                                ttt = findAnyToken(t, null,
                                        new CppTokenId[]{CppTokenId.CASE,
                                            CppTokenId.DEFAULT,
                                            CppTokenId.QUESTION,
                                            CppTokenId.PRIVATE,
                                            CppTokenId.PROTECTED,
                                            CppTokenId.PUBLIC}, true);
                                if (ttt != null) {
                                    TokenId tttTokenID = ttt.getTokenID();
                                    if(tttTokenID instanceof CppTokenId) {
                                        switch ((CppTokenId)tttTokenID) {
                                            case QUESTION:
                                                indent = getTokenIndent(ttt) + getShiftWidth();
                                                break;
                                            case CASE:
                                            case DEFAULT:
                                                indent = getTokenIndent(ttt) + getRightIndentSwitch();
                                                break;
                                            default:
                                                // Indent of line with ':' plus one indent level
                                                indent = getTokenIndent(t);// + getShiftWidth();
                                        }
                                    }
                                } else {
                                    // it looks like label
                                    TokenItem previousToken = getPreviousToken(t);
                                    if (previousToken != null) {
                                        TokenItem statStart = findStatement(previousToken);
                                        if (statStart != null) {
                                            TokenId statStartkenID = statStart.getTokenID();
                                            if(statStartkenID instanceof CppTokenId) {
                                                switch ((CppTokenId)statStartkenID) {
                                                    case LBRACE:
                                                        indent = getTokenIndent(statStart) + getShiftWidth();
                                                        break;
                                                    default:
                                                        indent =  getTokenIndent(statStart);
                                                        break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break;

                        case QUESTION:
                            indent = getTokenIndent(t) + getShiftWidth();
                            break;
                        case DO:
                        case ELSE:
                            indent = getTokenIndent(t) + getRightIndent();
                            break;

                        case RPAREN:
                            // Try to find the matching left paren
                            TokenItem rpmt = findMatchingToken(t, null, CppTokenId.LPAREN, true);
                            if (rpmt != null) {
                                rpmt = findImportantToken(rpmt, null, true);
                                // Check whether there are the indent changing kwds
                                if (rpmt != null) {
                                    TokenId rpmtTokenID = rpmt.getTokenID();
                                    if(rpmtTokenID instanceof CppTokenId) {
                                        switch ((CppTokenId)rpmtTokenID) {
                                            case FOR:
                                            case IF:
                                            case WHILE:
                                                // Indent one level
                                                indent = getTokenIndent(rpmt) + getRightIndent();
                                                break;
                                            case RBRACKET:
                                                rpmt = findMatchingToken(t, null, CppTokenId.LBRACKET, true);
                                                if (rpmt != null) {
                                                    indent = getTokenIndent(rpmt) + getRightIndentLambda();
                                                }
                                                break;
                                            case IDENTIFIER:
                                                if (token != null && token.getTokenID() == CppTokenId.IDENTIFIER) {
                                                    indent = getTokenIndent(t);
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                            if (indent < 0) {
                                indent = computeStatementIndent(t);
                            }
                            break;

                        case IDENTIFIER:
                            if (token != null && token.getTokenID() == CppTokenId.IDENTIFIER) {
                                indent = getTokenIndent(t);
                                break;
                            }
                            indent = computeStatementIndent(t);
                            break;

                        case COMMA:
                            if (isEnumComma(t)) {
                                indent = getTokenIndent(t);
                                break;
                            } else if (isFieldComma(t)) {
                                indent = getTokenIndent(t);
                                break;
                            }
                            indent = computeStatementIndent(t);
                            break;
                        default:
                            indent = computeStatementIndent(t);
                            break;
                    }
                }

                if (indent < 0) { // no indent found yet
                    indent = getTokenIndent(t);
                }
            }
        }

        if (indent < 0) { // no important token found
            indent = 0;
        }
        return indent;
    }

    private int computeStatementIndent(final TokenItem t) {
        int indent;
        // Find stmt start and add continuation indent
        TokenItem stmtStart = findStatementStart(t);
        indent = getTokenIndent(stmtStart);
        //int tindent = getTokenIndent(t);
        //if (tindent > indent)
        //    return tindent;

        if (stmtStart != null) {
            // Check whether there is a comma on the previous line end
            // and if so then also check whether the present
            // statement is inside array initialization statement
            // and not inside parents and if so then do not indent
            // statement continuation
            if (t != null && t.getTokenID() == CppTokenId.COMMA) {
                if (isArrayInitializationBraceBlock(t, null)
                        && getLeftParen(t, stmtStart) == null) {
                    return indent;
                }
                TokenItem lparen = getLeftParen(t, stmtStart);
                if (lparen != null) {
                    TokenItem prev = findImportantToken(lparen, null, true);
                    if (prev != null
                            && prev.getTokenID() == CppTokenId.IDENTIFIER) {
                        if (isStatement(stmtStart)) {
                            if (alignMultilineCallArgs()) {
                                return getTokenColumn(lparen) + 1;
                            }
                        } else {
                            if (alignMultilineMethodParams()) {
                                return getTokenColumn(lparen) + 1;
                            }
                        }
                    }
                }
            } else if ((stmtStart.getTokenID() == CppTokenId.IF && alignMultilineIf())
                    || (stmtStart.getTokenID() == CppTokenId.WHILE && alignMultilineWhile())
                    || (stmtStart.getTokenID() == CppTokenId.FOR && alignMultilineFor())) {
                if (t != null) {
                    TokenItem lparen = getLeftParen(t, stmtStart);
                    if (lparen != null) {
                        return getTokenColumn(lparen) + 1;
                    }
                }
            } else if (!isStatement(stmtStart)) {
                return indent;
            }
            indent += getFormatStatementContinuationIndent();
        }
        return indent;
    }

    // for services
    public CppIndentTask(Document doc) {
        this.doc = doc;
    }

    /**
     * returns indentation for line containing given offset
     * @param offset offset on line
     * @return indentation of line containing offset
     */
    public int getLineIndentation(int caretOffset) {
        if (codeStyle == null) {
            codeStyle = CodeStyle.getDefault(doc);
        }
        int lineOffset;
        try {
            lineOffset = IndentUtils.lineStartOffset(doc, caretOffset);
        } catch (BadLocationException ex) {
            return 0;
        }
        ts = CndLexerUtilities.getCppTokenSequence(doc, lineOffset, false, false);
        if (ts == null) {
            return 0;
        }
        int indent = indentLine(new TokenItem(ts, true), caretOffset, true);
        return indent;
    }

    // for testing
    public void reindent(int caretOffset) throws BadLocationException {
        if (codeStyle == null) {
            codeStyle = CodeStyle.getDefault(doc);
        }
        int lineOffset = IndentUtils.lineStartOffset(doc, caretOffset);
        ts = CndLexerUtilities.getCppTokenSequence(doc, lineOffset, false, false);
        if (ts == null) {
            return;
        }
        int indent = indentLine(new TokenItem(ts, true), caretOffset, true);
        if (indent >= 0) {
            modifyIndent(lineOffset, indent);
        }
    }

    // for testing
    private void modifyIndent(int lineStartOffset, int newIndent) throws BadLocationException {
        // Determine old indent first together with oldIndentEndOffset
        int indent = 0;
        int tabSize = -1;
        CharSequence docText = doc.getText(0, doc.getLength());
        int oldIndentEndOffset = lineStartOffset;
        while (oldIndentEndOffset < docText.length()) {
            char ch = docText.charAt(oldIndentEndOffset);
            if (ch == '\n') {
                break;
            } else if (ch == '\t') {
                if (tabSize == -1) {
                    tabSize = IndentUtils.tabSize(doc);
                }
                // Round to next tab stop
                indent = (indent + tabSize) / tabSize * tabSize;
            } else if (Character.isWhitespace(ch)) {
                indent++;
            } else { // non-whitespace
                break;
            }
            oldIndentEndOffset++;
        }

        String newIndentString = IndentUtils.createIndentString(doc, newIndent);
        // Attempt to match the begining characters
        int offset = lineStartOffset;
        for (int i = 0; i < newIndentString.length() && lineStartOffset + i < oldIndentEndOffset; i++) {
            if (newIndentString.charAt(i) != docText.charAt(lineStartOffset + i)) {
                offset = lineStartOffset + i;
                newIndentString = newIndentString.substring(i);
                break;
            }
        }

        // Replace the old indent
        if (offset < oldIndentEndOffset) {
            doc.remove(offset, oldIndentEndOffset - offset);
        }
        if (newIndentString.length() > 0) {
            doc.insertString(offset, newIndentString, null);
        }
    }
}
