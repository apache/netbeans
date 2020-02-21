/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.editor.fortran.indent;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import static org.netbeans.cnd.api.lexer.FortranTokenId.*;
import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;

/**
 *
 */
public class FortranIndentTask extends FortranIndentSupport implements IndentTask {
    private Context context;
    private Document doc;

    public FortranIndentTask(Context context) {
        this.context = context;
        doc = context.document();
    }

    @Override
    public void reindent() throws BadLocationException {
        if (codeStyle == null) {
            codeStyle = FortranCodeStyle.get(doc);
        }
        codeStyle.setupLexerAttributes(doc);
        int caretOffset = context.caretOffset();
        int lineOffset = context.lineStartOffset(caretOffset);
        ts = CndLexerUtilities.getFortranTokenSequence(doc, lineOffset);
        if (ts == null || !ts.moveNext()) {
            return;
        }
        int indent = indentLine(new TokenItem(ts), caretOffset);
        if (indent >= 0) {
            context.modifyIndent(lineOffset, indent);
        }
    }

    @Override
    public ExtraLock indentLock() {
        return null;
    }

    public int indentLine(TokenItem token, int caretOffset) {
        token = moveToFirstLineImportantToken(token);
        if (isFixedFormatComment(token) || isPreprocessor(token)) {
            return 0;
        } else if (isFreeFormatComment(token)) {
            // comment is first on the line
            // this will do for now XXX
            return findIndent(token);

        } else if (isFixedFormatLabel(token)) { // fixed format label
            // indent after label token
            TokenItem nexToken = token.getNext();
            // remove spaces
            while (nexToken.getTokenID() == getWhitespaceTokenID()) {
                TokenItem nt = nexToken.getNext();
                removeToken(nexToken);
                nexToken = nt;
            }
            int indent = findInlineSpacing(token);
            // add spaces
            for (int i = 0; i < indent - 5; ++i) {
                insertToken(nexToken, getWhitespaceTokenID(), " "); // NOI18N
            }                // the line's real indent
            return 1;

        } else if (isFixedFormatLineContinuation(token)) { // fixed format line continuation
            // indent after line cont. token
            TokenItem nexToken = token.getNext();
            // remove spaces
            while (nexToken.getTokenID() == getWhitespaceTokenID()) {
                TokenItem nt = nexToken.getNext();
                removeToken(nexToken);
                nexToken = nt;
            }
            int indent = findInlineSpacing(token);
            // add spaces
            for (int i = 0; i < indent - 5; ++i) {
                insertToken(nexToken, getWhitespaceTokenID(), " "); // NOI18N
            }                // the line's real indent
            return 5;

        } else if (!getFreeFormat() // subroutine, entry, and function always at 6 for fixed format
                && token.getTokenID() == KW_SUBROUTINE || token.getTokenID() == KW_ENTRY || token.getTokenID() == KW_FUNCTION) {
            return 6;

        } else { // first non-WS char is not comment

            int indent = findIndent(token);
            if (!getFreeFormat() && indent < 6) {
                indent = 6;
            }
            return indent;
        }
    }

    /** Delegation to the same method in format-writer. */
    public void insertToken(TokenItem beforeToken, FortranTokenId tokenID, String tokenImage) {
        //TODO
    }

    /** Remove the token-item from the chain. It can be removed
    * only in case it doesn't come from the document's text
    * and it wasn't yet written to the underlying writer.

    */
    public void removeToken(TokenItem token) {
        //TODO
    }

    /** Find the indentation for the first token on the line.
     * The given token is also examined in some cases.
     */
    public int findIndent(TokenItem token) {
        int indent = -1; // assign invalid indent
        // First check the given token
        if (token == null) {
            return 0;
        }
        TokenItem nextToken;
        TokenItem matchToken = null;
        switch (token.getTokenID()) {

            case KW_CASE:
            case KW_DEFAULT:
                matchToken = findMatchingToken(token, KW_SELECT, KW_ENDSELECT);
                if (matchToken == null) {
                    matchToken = findMatchingToken(token, KW_SELECTCASE, KW_ENDSELECT);
                }
                if (matchToken != null) {
                    indent = getTokenIndent(matchToken) + getShiftWidth();
                }
                break;

            case KW_PROGRAM:
            case KW_ENDPROGRAM:
                indent = 0;
                break;

            case KW_END:
                // usually the next token after an "END" token is
                // the type of block this is.
                nextToken = token.getNext();
                if (nextToken == null) {
                    if (getFreeFormat()) {
                        indent = 0;
                    } else {
                        indent = 6;
                    }
                    break;
                }
                if (nextToken.getTokenID() == WHITESPACE) {
                    nextToken = nextToken.getNext();
                }
                if (nextToken == null) {
                    indent = 0;
                    break;
                }
                if (nextToken.getTokenID() == NEW_LINE) {
                    // We're at the end of the program
                    indent = 0;
                } else {
                    // We're at a two word end statement,ie, end if
                    switch (nextToken.getTokenID()) {
                        case KW_IF:
                        case KW_ELSE:
                            matchToken = findMatchingToken(token, KW_IF, KW_ENDIF);
                            break;

                        case KW_BLOCK:
                            matchToken = findMatchingToken(token, KW_BLOCK, KW_ENDBLOCK);
                            if (matchToken == null) {
                                matchToken = findMatchingToken(token, KW_BLOCKDATA, KW_ENDBLOCKDATA);
                            }
                            break;

                        case KW_BLOCKDATA:
                            matchToken = findMatchingToken(token, KW_BLOCKDATA, KW_ENDBLOCKDATA);
                            break;

                        case KW_DO:
                            matchToken = findMatchingToken(token, KW_DO, KW_ENDDO);
                            break;

                        case KW_FORALL:
                            matchToken = findMatchingToken(token, KW_FORALL, KW_ENDFORALL);
                            break;

                        case KW_FUNCTION:
                            matchToken = findMatchingToken(token, KW_FUNCTION, KW_ENDFUNCTION);
                            break;

                        case KW_INTERFACE:
                            matchToken = findMatchingToken(token, KW_INTERFACE, KW_ENDINTERFACE);
                            break;

                        case KW_MAP:
                            matchToken = findMatchingToken(token, KW_MAP, KW_ENDMAP);
                            break;

                        case KW_MODULE:
                            matchToken = findMatchingToken(token, KW_MODULE, KW_ENDMODULE);
                            break;

                        case KW_PROGRAM:
                            matchToken = findMatchingToken(token, KW_PROGRAM, KW_ENDPROGRAM);
                            break;

                        case KW_SELECT:
                            matchToken = findMatchingToken(token, KW_SELECT, KW_ENDSELECT);
                            if (matchToken == null) {
                                matchToken = findMatchingToken(token, KW_SELECTCASE, KW_ENDSELECT);
                            }
                            break;

                        case KW_STRUCTURE:
                            matchToken = findMatchingToken(token, KW_STRUCTURE, KW_ENDSTRUCTURE);
                            break;

                        case KW_SUBROUTINE:
                            matchToken = findMatchingToken(token, KW_SUBROUTINE, KW_ENDSUBROUTINE);
                            break;

                        case KW_TYPE:
                            matchToken = findMatchingToken(token, KW_TYPE, KW_ENDTYPE);
                            break;

                        case KW_ENUM:
                            matchToken = findMatchingToken(token, KW_ENUM, KW_ENDENUM);
                            break;

                        case KW_UNION:
                            matchToken = findMatchingToken(token, KW_UNION, KW_ENDUNION);
                            break;

                        case KW_WHERE:
                            matchToken = findMatchingToken(token, KW_WHERE, KW_ENDWHERE);
                            break;

                        case KW_WHILE:
                            matchToken = findMatchingToken(token, KW_WHILE, KW_ENDWHILE);
                            break;
                    }// END SWITCH
                    if (matchToken != null) {
                        indent = getTokenIndent(matchToken);
                    }
                }//end else
                break;

            default:
                switch (token.getTokenID()) {
                    case KW_ELSE:
                    case KW_ELSEIF:
                    case KW_ENDIF:
                        matchToken = findMatchingToken(token, KW_IF, KW_ENDIF);
                        if (matchToken == null) {
                            matchToken = findMatchingToken(token, KW_WHERE, KW_ENDWHERE);
                        }
                        break;

                    case KW_ENDBLOCK:
                        matchToken = findMatchingToken(token, KW_BLOCK, KW_ENDBLOCK);
                        if (matchToken == null) {
                            matchToken = findMatchingToken(token, KW_BLOCKDATA, KW_ENDBLOCKDATA);
                        }
                        break;

                    case KW_ENDBLOCKDATA:
                        matchToken = findMatchingToken(token, KW_BLOCKDATA, KW_ENDBLOCKDATA);
                        break;

                    case KW_ENDDO:
                        matchToken = findMatchingToken(token, KW_DO, KW_ENDDO);
                        break;

                    case KW_ENDFORALL:
                        matchToken = findMatchingToken(token, KW_FORALL, KW_ENDFORALL);
                        break;

                    case KW_ENDFUNCTION:
                        matchToken = findMatchingToken(token, KW_FUNCTION, KW_ENDFUNCTION);
                        break;

                    case KW_ENDINTERFACE:
                        matchToken = findMatchingToken(token, KW_INTERFACE, KW_ENDINTERFACE);
                        break;

                    case KW_ENDMAP:
                        matchToken = findMatchingToken(token, KW_MAP, KW_ENDMAP);
                        break;

                    case KW_ENDMODULE:
                        matchToken = findMatchingToken(token, KW_MODULE, KW_ENDMODULE);
                        break;

                    case KW_ENDSELECT:
                        matchToken = findMatchingToken(token, KW_SELECT, KW_ENDSELECT);
                        if (matchToken == null) {
                            matchToken = findMatchingToken(token, KW_SELECTCASE, KW_ENDSELECT);
                        }
                        break;

                    case KW_ENDSTRUCTURE:
                        matchToken = findMatchingToken(token, KW_STRUCTURE, KW_ENDSTRUCTURE);
                        break;

                    case KW_ENDSUBROUTINE:
                        matchToken = findMatchingToken(token, KW_SUBROUTINE, KW_ENDSUBROUTINE);
                        break;

                    case KW_ENDTYPE:
                        matchToken = findMatchingToken(token, KW_TYPE, KW_ENDTYPE);
                        break;

                    case KW_ENDENUM:
                        matchToken = findMatchingToken(token, KW_ENUM, KW_ENDENUM);
                        break;

                    case KW_ENDUNION:
                        matchToken = findMatchingToken(token, KW_UNION, KW_ENDUNION);
                        break;

                    case KW_ENDWHERE:
                    case KW_ELSEWHERE:
                        matchToken = findMatchingToken(token, KW_WHERE, KW_ENDWHERE);
                        break;

                    case KW_ENDWHILE:
                        matchToken = findMatchingToken(token, KW_WHILE, KW_ENDWHILE);
                        break;
                }//end second switch
                if (matchToken != null) {
                    indent = getTokenIndent(matchToken);
                }
                break; //end default case
        }//end first switch
        //}//end if

        // If indent not found, search back for the first important token
        if (indent < 0) { // if not yet resolved
            //TokenItem matchToken;
            TokenItem impToken = findImportantToken(token, null, true);
            TokenItem startToken = findLineStartToken(impToken);
            // in fixed format: line cont. and preprocessors are not treated as important tokens
            if (startToken == null) {
                return 0;
            }
            while (isFixedFormatLineContinuation(startToken) || isPreprocessor(startToken) || startToken.getTokenID() == KW_ENTRY) {
                impToken = findImportantToken(startToken, null, true);
                startToken = findLineStartToken(impToken);
                if (startToken == null) {
                    return 0;
                }
            }
            if (impToken != null) { // valid important token
                // in fixed format: labels are not treated as start tokens
                while (isFixedFormatLabel(startToken) || startToken.getTokenID() == WHITESPACE) {
                    startToken = startToken.getNext();
                    if (startToken == null) {
                        return 0;
                    }
                }
                //startToken = findLineStartToken(impToken);
                switch (startToken.getTokenID()) {

                    case KW_DO:
                        if (!getFreeFormat()) {
                            // DO ITERATOR or DO LABEL
                            TokenItem nexToken = startToken.getNext();
                            while (nexToken.getTokenID() == WHITESPACE) {
                                nexToken = nexToken.getNext();
                            }
                            if (nexToken.getTokenID() == NUM_LITERAL_INT) {
                                // Don't indent inside DO-LABEL for now
                                indent = getTokenIndent(startToken);
                                break;
                            }
                        }
                        indent = getTokenIndent(startToken) + getShiftWidth();
                        break;

                    case KW_FORALL:
                    case KW_RECURSIVE:
                    case KW_DOUBLEPRECISION:
                    case KW_ELSE:
                    case KW_CASE:
                    case KW_WHERE:
                    case KW_ELSEWHERE:
                    case KW_BLOCK:
                    case KW_BLOCKDATA:
                    case KW_SELECT:
                    case KW_SELECTCASE:
                    case KW_PROGRAM:
                    case KW_SUBROUTINE:
                    case KW_STRUCTURE:
                    case KW_INTERFACE:
                    case KW_FUNCTION:
                    case KW_UNION:
                    case KW_ENUM:
                    case KW_MAP:
                        indent = getTokenIndent(startToken) + getShiftWidth();
                        break;
                    case KW_TYPE:
                    {
                        TokenItem next = findImportantToken(startToken.getNext(), null, false);
                        if (next != null && next.getTokenID() == LPAREN) {
                            indent = getTokenIndent(startToken);
                        } else {
                            indent = getTokenIndent(startToken) + getShiftWidth();
                        }
                        break;
                    }
                    case KW_MODULE:
                    {
                        TokenItem next = findImportantToken(startToken.getNext(), null, false);
                        if (next != null) {
                            switch (next.getTokenID()) {
                                case KW_PROCEDURE:
                                case KW_FUNCTION:
                                case KW_SUBROUTINE:
                                    indent = getTokenIndent(startToken);
                                    break;
                                default:
                                    indent = getTokenIndent(startToken) + getShiftWidth();
                                    break;
                            }
                            break;
                        } else {
                           indent = getTokenIndent(startToken) + getShiftWidth();
                            break;
                        }
                    }
                    case KW_ELSEIF:
                        indent = getTokenIndent(startToken) + getShiftWidth();
                        break;
                    case KW_IF:
                        if (isIfThenStatement(startToken)) {
                            indent = getTokenIndent(startToken) + getShiftWidth();
                        } else {
                            indent = getTokenIndent(startToken);
                        }
                        break;

                    default:
                        indent = getTokenIndent(startToken);
                        break;
                }

                if (indent < 0) { // no indent found yet
                    indent = getTokenIndent(impToken);
                }
            } // end if (impToken != null)
        }

        if (indent < 0) { // no important token found
            indent = 0;
        }
        return indent;
    }

    // for testing
    public FortranIndentTask(Document doc) {
        this.doc = doc;
    }

    // for testing
    public void reindent(int caretOffset) throws BadLocationException {
        if (codeStyle == null) {
            codeStyle = FortranCodeStyle.get(doc);
        }
        int lineOffset = IndentUtils.lineStartOffset(doc, caretOffset);
        ts = CndLexerUtilities.getFortranTokenSequence(doc, lineOffset);
        if (ts == null || (!ts.moveNext() && !ts.movePrevious())) {
            return;
        }
        int indent = indentLine(new TokenItem(ts), caretOffset);
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
