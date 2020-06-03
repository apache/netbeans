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
