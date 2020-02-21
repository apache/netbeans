/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.refactoring.elements;

import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.text.CloneableEditorSupport;

/**
 *
 */
public class ReadWriteTokenProcessor {
    
    //TODO extract commol logic for BodyFinder.VariableInfo.isWriteAccess()

    private final CsmReference ref;
    private CsmRefactoringElementImpl.RW rw = CsmRefactoringElementImpl.RW.Read;

    ReadWriteTokenProcessor(CsmReference ref) {
        this.ref = ref;
    }

    CsmRefactoringElementImpl.RW process() {
        CsmFile csmFile = ref.getContainingFile();
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
        StyledDocument stDoc = CsmUtilities.openDocument(ces);
        if (stDoc instanceof BaseDocument) {
            final BaseDocument doc = (BaseDocument) stDoc;
            doc.render(new Runnable() {
                @Override
                public void run() {
                    int offset = ref.getStartOffset();
                    TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset, true, false);
                    if (cppTokenSequence == null) {
                        return;
                    }
                    CppTokenId nextImportantToken = getNextImportantToken(cppTokenSequence, offset);
                    if (nextImportantToken != null) {
                        switch (nextImportantToken) {
                            // asignments
                            case EQ: {
                                CppTokenId prevImportantToken = getPreviousImportantToken(cppTokenSequence, offset);
                                if (prevImportantToken == CppTokenId.STAR) { // *x=
                                    rw = CsmRefactoringElementImpl.RW.Read;
                                    return;
                                } else if (prevImportantToken == CppTokenId.PLUSPLUS || prevImportantToken == CppTokenId.MINUSMINUS) { //++x=
                                    rw = CsmRefactoringElementImpl.RW.ReadWrite;
                                    return;
                                } else { // x=
                                    rw = CsmRefactoringElementImpl.RW.Write;
                                    return;
                                }
                            }
                            case PLUSEQ:
                            case MINUSEQ:
                            case STAREQ:
                            case SLASHEQ:
                            case AMPEQ:
                            case BAREQ:
                            case CARETEQ:
                            case PERCENTEQ:
                            case LTLTEQ:
                            case GTGTEQ: {
                                CppTokenId prevImportantToken = getPreviousImportantToken(cppTokenSequence, offset);
                                if (prevImportantToken == CppTokenId.STAR) { // *x=
                                    rw = CsmRefactoringElementImpl.RW.Read;
                                    return;
                                } else { // x=
                                    rw = CsmRefactoringElementImpl.RW.ReadWrite;
                                    return;
                                }
                            }
                            // increments
                            case PLUSPLUS:
                            case MINUSMINUS: // x++
                                rw = CsmRefactoringElementImpl.RW.ReadWrite;
                                return;
                            // supose that binary operators have a read access
                            case PLUS:
                            case MINUS:
                            case GT:
                            case LT:
                            case QUESTION:
                            case EQEQ:
                            case LTEQ:
                            case GTEQ:
                            case NOTEQ:
                            case AMPAMP:
                            case BARBAR:
                            case STAR:
                            case SLASH:
                            case BAR:
                            case CARET:
                            case PERCENT:
                            case LTLT:
                            case GTGT: {
                                CppTokenId prevImportantToken = getPreviousImportantToken(cppTokenSequence, offset);
                                if (prevImportantToken == CppTokenId.PLUSPLUS || prevImportantToken == CppTokenId.MINUSMINUS) { //++x+
                                    rw = CsmRefactoringElementImpl.RW.ReadWrite;
                                    return;
                                } else { // x+ or *x+
                                    rw = CsmRefactoringElementImpl.RW.Read;
                                    return;
                                }
                            }
                            case DOT:
                            case DOTMBR:
                            case ARROW:
                            case ARROWMBR:
                            case SCOPE: // x.
                                rw = CsmRefactoringElementImpl.RW.Read;
                                return;
                            case LPAREN: // x(
                                rw = CsmRefactoringElementImpl.RW.Write;
                                return;
                            case COLON:
                            case SEMICOLON:
                            case RBRACKET:
                            case COMMA:
                            case RPAREN: 
                            default: {
                                CppTokenId prevImportantToken = getPreviousImportantToken(cppTokenSequence, offset);
                                if (prevImportantToken != null) {
                                    switch (prevImportantToken) {
                                        //TODO needs further investigation
                                        case AMP: // &x)
                                            rw = CsmRefactoringElementImpl.RW.Read;
                                            return;
                                        // pre-increments
                                        case PLUSPLUS:
                                        case MINUSMINUS: // ++x;
                                            rw = CsmRefactoringElementImpl.RW.ReadWrite;
                                            return;
                                        case EQ:
                                        case PLUSEQ:
                                        case MINUSEQ:
                                        case STAREQ:
                                        case SLASHEQ:
                                        case AMPEQ:
                                        case BAREQ:
                                        case CARETEQ:
                                        case PERCENTEQ:
                                        case LTLTEQ:
                                        case GTGTEQ: // =x
                                            rw = CsmRefactoringElementImpl.RW.Read;
                                            return;
                                        // supose that unary operators have a read access
                                        case DELETE:
                                            rw = CsmRefactoringElementImpl.RW.Write;
                                            return;
                                        case NOT:
                                        case TILDE:
                                            rw = CsmRefactoringElementImpl.RW.Read;
                                            return;
                                        // supose that unary/binary operators have a read access
                                        case PLUS:
                                        case MINUS: // +x)
                                            rw = CsmRefactoringElementImpl.RW.Read;
                                            return;
                                        // supose that binary operators have a read access
                                        case GT:
                                        case LT:
                                        case QUESTION:
                                        case EQEQ:
                                        case LTEQ:
                                        case GTEQ:
                                        case NOTEQ:
                                        case AMPAMP:
                                        case BARBAR:
                                        case STAR:
                                        case SLASH:
                                        case BAR:
                                        case CARET:
                                        case PERCENT:
                                        case LTLT:
                                        case GTGT: // >x)
                                            rw = CsmRefactoringElementImpl.RW.Read;
                                            return;
                                        //TODO needs further investigation
                                        case COMMA:
                                        case LPAREN: // ,x)
                                            rw = CsmRefactoringElementImpl.RW.Read;
                                            return;
                                        //TODO needs further investigation
                                        case DOT:
                                        case DOTMBR:
                                        case ARROW:
                                        case ARROWMBR:
                                        case SCOPE: { // .x;
                                            CppTokenId firstImportantToken = getPreviousImportantToken(cppTokenSequence, offset, new CppTokenId[]{
                                                CppTokenId.IDENTIFIER, CppTokenId.DOT, CppTokenId.DOTMBR, CppTokenId.ARROW, CppTokenId.ARROWMBR, CppTokenId.SCOPE
                                            });
                                            if (firstImportantToken == CppTokenId.PLUSPLUS || firstImportantToken == CppTokenId.MINUSMINUS) { //++y.x;
                                                rw = CsmRefactoringElementImpl.RW.ReadWrite;
                                                return;
                                            } else { // *y.x; or y.x;
                                                rw = CsmRefactoringElementImpl.RW.Read;
                                                return;
                                            }
                                        }
                                        //needs further investigation
                                        default:
                                            rw = CsmRefactoringElementImpl.RW.Read;
                                            return;
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
        return rw;
    }

    private CppTokenId getNextImportantToken(TokenSequence<TokenId> cppTokenSequence, int offset) {
        cppTokenSequence.move(offset);
        if (cppTokenSequence.moveNext()) {
            next:
            while (cppTokenSequence.moveNext()) {
                Token<TokenId> token = cppTokenSequence.token();
                TokenId id = token.id();
                if (id instanceof CppTokenId) {
                    switch ((CppTokenId) id) {
                        // skip unimportant
                        case WHITESPACE:
                        case ESCAPED_LINE:
                        case ESCAPED_WHITESPACE:
                        case NEW_LINE:
                        case LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case DOXYGEN_COMMENT:
                        case DOXYGEN_LINE_COMMENT:
                            break;
                        default:
                            return (CppTokenId) id;
                    }
                }
            }
        }
        return null;
    }

    private CppTokenId getPreviousImportantToken(TokenSequence<TokenId> cppTokenSequence, int offset) {
        cppTokenSequence.move(offset);
        prev:
        while (cppTokenSequence.movePrevious()) {
            Token<TokenId> token = cppTokenSequence.token();
            TokenId id = token.id();
            if (id instanceof CppTokenId) {
                switch ((CppTokenId) id) {
                    // skip unimportant
                    case WHITESPACE:
                    case ESCAPED_LINE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                        break;
                    default:
                        return (CppTokenId) id;
                }
            }
        }
        return null;
    }

    private CppTokenId getPreviousImportantToken(TokenSequence<TokenId> cppTokenSequence, int offset, CppTokenId[] ignore) {
        cppTokenSequence.move(offset);
        prev:
        while (cppTokenSequence.movePrevious()) {
            Token<TokenId> token = cppTokenSequence.token();
            TokenId id = token.id();
            if (id instanceof CppTokenId) {
                loop: switch ((CppTokenId) id) {
                    // skip unimportant
                    case WHITESPACE:
                    case ESCAPED_LINE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                        break;
                    default:
                        for(CppTokenId t : ignore) {
                            if (t == id) {
                                break loop;
                            }
                        }
                        return (CppTokenId) id;
                }
            }
        }
        return null;
    }
}
