/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.editor.parser.impl;

import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import java.util.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.spi.lexer.TokenFactory;
import org.openide.filesystems.FileObject;

public class FoldingParserImpl {

    private final List<CppFoldRecord> parserFolders = new ArrayList<CppFoldRecord>();
    private final List<TokenFilter> filters;
    private final TokenSequence<CppTokenId> ts;

    public FoldingParserImpl(TokenSequence<CppTokenId> ts) {
        this.ts = ts;
        this.filters = new ArrayList<TokenFilter>();
    }

    private void createFolder(int folderKind, Token<CppTokenId> begin, Token<CppTokenId> end) {
        parserFolders.add(new CppFoldRecord(folderKind, begin.offset(null), end.offset(null) + end.length()));
    }

    protected List<CppFoldRecord> getFolders() {
        List<CppFoldRecord> out = new ArrayList<CppFoldRecord>(parserFolders.size());
        for (TokenFilter filter : filters) {
            out.addAll(filter.getFolders());
        }
        out.addAll(parserFolders);
        return out;
    }

    private static FoldingParserImpl getParser(TokenSequence ts) {
        FoldingParserImpl parser = new FoldingParserImpl(ts);
        return parser;
    }

    public static List<CppFoldRecord> parse(FileObject fo, TokenSequence ts) {
        try {
            FoldingParserImpl parser = getParser(ts);
            parser.filters.clear();
            parser.filters.add(new CommentTokenFilter());
            parser.filters.add(new PreprocessorFilter(ts));
            parser.translation_unit();
            return new ArrayList<CppFoldRecord>(parser.getFolders());
        } catch (Exception e) {
            if (reportErrors) {
                System.err.println("exception: " + e); // NOI18N
                e.printStackTrace(System.err);
            }
        }
        return null;
    }

    private final static boolean reportErrors = Boolean.getBoolean("folding.parser.report.errors"); // NOI18N

    public void reportError(Exception e) {
        if (reportErrors) {
            System.err.println("exception: " + e); // NOI18N
            e.printStackTrace(System.err);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // help methods
    protected final void balanceParens() {
        assert (LA(0) == CppTokenId.LPAREN);
        balanceBracket(CppTokenId.LPAREN, CppTokenId.RPAREN);
        assert (matchError || LA(1) == CppTokenId.RPAREN);
    }

    protected final void balanceCurlies() {
        assert (LA(0) == CppTokenId.LBRACE);
        balanceBracket(CppTokenId.LBRACE, CppTokenId.RBRACE);
        assert (matchError || LA(1) == CppTokenId.RBRACE);
    }

    protected final void balanceTemplateParams() {
        assert (LA(0) == CppTokenId.LT);
        balanceBracket(CppTokenId.LT, CppTokenId.GT);
        assert (matchError || LA(1) == CppTokenId.GT);
    }

    protected final void balanceCurliesAndCreateFolders(int folderType, int level, boolean foldCurrent) {
        if (LA(0) != CppTokenId.LBRACE) {
            matchError = true;
            return;
        }

        CppTokenId startType = CppTokenId.LBRACE;
        CppTokenId endType = CppTokenId.RBRACE;

        Token<CppTokenId> startToken = LT(0);

        CppTokenId LA1 = LA(1);
        for (; LA1 != CppTokenId.EOF; LA1 = LA(1)) { // true eof
            if (LA1 == endType) {
                Token<CppTokenId> endToken = LT(1);
                if (foldCurrent) {
                    createFolder(folderType, startToken, endToken);
                }
                break;
            } else if (LA1 == startType) {
                moveNext();
                balanceCurliesAndCreateFolders(folderType, level + 1, true);
                continue;
            } else {
                // eat token
            }
            moveNext();
        }
        match(CppTokenId.RBRACE);
    }

    private void balanceBracket(CppTokenId startType, CppTokenId endType) {
        int level = 0;
        CppTokenId LA1 = LA(1);
        for (; LA1 != CppTokenId.EOF; LA1 = LA(1)) {
            if (LA1 == endType) {
                if (level <= 0) {
                    break;
                } else {
                    level--;
                }
            } else if (LA1 == startType) {
                level++;
            } else {
                // eat element
            }
            moveNext();
        }
        if (level != 0 || LA1 == CppTokenId.EOF) {
            matchError = true;
            matchException = new Exception("unbalanced bracket " + startType.name()); // NOI18N
        }
    }

    protected final void createCurlyFolder(int folderKind) {
        do {
            Token<CppTokenId> begin = LT(1);
            match(CppTokenId.LBRACE);
            if (matchError) {
                break;
            }
            balanceCurlies();
            if (matchError) {
                break;
            }
            Token<CppTokenId> end = LT(1);
            match(CppTokenId.RBRACE);
            if (matchError) {
                break;
            }
            createFolder(folderKind, begin, end);
        } while (false);
        if (matchError) {
            reportError(matchException);
//            recover(matchException,_tokenSet_1);
            resetMatchError();
        }
    }

    // state machine
    public final void translation_unit() {
        // translation_unit:    external_declarations EOF;
        do {
            external_declarations();
            if (matchError) {
                break;
            }
            if (moveNext()) { //EOF should be reached
                matchError = true;
                matchException = new Exception("EOF should be reached here: " + ts.token());//NOI18N
            }
        } while (false);
        if (matchError) {
            reportError(matchException);
//            recover(matchException,_tokenSet_0);
            resetMatchError();
        }
    }

    @SuppressWarnings("fallthrough")
    protected final void external_declarations() {
        // external_declarations : (external_declaration)*
        // external_declaration
        //    :
        //                //linkage specification
        //                (LITERAL_extern StringLiteral)=> linkage_specification
        //        |
        //                declaration
        //    ;
        main_loop:
        while (true) {
            // Local LA Cache for 2 element(s):
            CppTokenId LA1 = LA(1);
            CppTokenId LA2 = LA(2);
            switch (LA1) {
                case RBRACE:
                    match(CppTokenId.RBRACE);
                    break main_loop;
                case EOF:
                    break main_loop;
                case EXTERN:
                    if (LA2 == CppTokenId.STRING_LITERAL) {
                        linkage_specification();
                        if (matchError) {
                            break main_loop;
                        }
                        break; // break LA1 switch                        
                    }
                // nobreak
                default:
                    if (LA(1) == CppTokenId.EOF) {
                        matchError = true;
                        matchException = new FolderException("EOF unexpected", LT(1)); //NOI18N
                        break;
                    }
                    declaration();
                    if (matchError) {
                        break main_loop;
                    }
            }
        } // End of loop
        if (matchError) {
            reportError(matchException);
            //           recover(matchException,_tokenSet_1);
            resetMatchError();
        }
    }

    protected final void linkage_specification() {
        //linkage_specification
        //	:	LITERAL_extern StringLiteral
        //		(
        //                    bb:LCURLY
        //                        (options {greedy=false;}:external_declaration)*
        //                    be:RCURLY
        //                |
        //                    external_declaration
        //		)
        //	;
        main_loop:
        do {
            match(CppTokenId.EXTERN);
            if (matchError) {
                break main_loop;
            }
            match(CppTokenId.STRING_LITERAL);
            if (matchError) {
                break main_loop;
            }
            // Local LA Cache for 2 element(s):
            CppTokenId LA1 = LA(1);
            CppTokenId LA2 = LA(2);

            if ((LA1 == CppTokenId.LBRACE) /* &&  ((LA2 >= FIRST_TOKEN && LA2 <= LAST_TOKEN)) */) {
                Token<CppTokenId> begin = LT(1);
                // already checked LCURLY, just skip
                moveNext();
                ext_decl_loop:
                do {
                    // nongreedy exit test
                    if (LA(1) == CppTokenId.RBRACE) {
                        break ext_decl_loop;
                    }
                    if (LA(1) == CppTokenId.EOF) {
                        break ext_decl_loop;
                    }
                    external_declarations();
                    if (matchError) {
                        break main_loop;
                    }
                } while (true);
                Token<CppTokenId> end = LT(1);
                match(CppTokenId.RBRACE);
                if (matchError) {
                    break main_loop;
                }
                // create folder
                createFolder(CppFoldRecord.CLASS_FOLD, begin, end);
            } else if (LA1 != CppTokenId.EOF) {
                external_declarations();
            } else { //EOF
                break main_loop;
            }
        } while (false);
        if (matchError) {
            reportError(matchException);
            resetMatchError();
        }
    }

    private void eat2Token(CppTokenId type, boolean checkLeftCurly, boolean checkRightCurly) {
        do {
            // Local LA Cache for 2 element(s):
            CppTokenId LA1 = LA(1);
            // nongreedy exit test
            if (LA1 == type) {
                break;
            }
            // error handling test
            if (checkLeftCurly && (LA1 == CppTokenId.LBRACE)) {
                break;
            }
            if (checkRightCurly && (LA1 == CppTokenId.RBRACE)) {
                break;
            }
            matchNot(CppTokenId.EOF);
            if (matchError) {
                break;
            }
        } while (true);
    }

    private void eatDeclPrefix() {
        loop:
        do {
            // Local LA Cache for 2 element(s):
            switch (LA(1)) {
                case EXTERN:
                case STRUCT:
                case TYPEDEF:
                    moveNext();
                    // eat
                    break;
                case TEMPLATE:
                    moveNext();
                    if (LA(1) == CppTokenId.LT) {
                        moveNext();
                        balanceTemplateParams();
                        if (matchError) {
                            break loop;
                        }
                        match(CppTokenId.GT);
                        if (matchError) {
                            break loop;
                        }
                    }
                    break;
                default:
                    break loop;
            }
        } while (true);
    }

    @SuppressWarnings("fallthrough")
    protected final void declaration() {

        main_loop:
        do {
            // eat typedef, template, extern template
            eatDeclPrefix();
            if (matchError) {
                break main_loop;
            }
            boolean ns = false;
            // Local LA Cache for 2 element(s):
            CppTokenId LA1 = LA(1);
            switch (LA1) {
                case RBRACE: // RCURLY is necessary above
                    break;
                case ENUM: // handle enum
                {
                    // enum is LITERAL_enum (ID)? { elems } IDs ;
                    moveNext();
                    if (LA(1) == CppTokenId.IDENTIFIER) { // ?
                        // already checked token ref, just skip
                        moveNext();
                    }
                    createCurlyFolder(CppFoldRecord.CLASS_FOLD);
                    if (matchError) {
                        break main_loop;
                    }
                    eat2Token(CppTokenId.SEMICOLON, true, true);
                    // allow errors
                    if (LA(1) == CppTokenId.SEMICOLON) {
                        moveNext();
                    }
                    if (matchError) {
                        break main_loop;
                    }
                    break;
                }
                case NAMESPACE: // handle namespace the same way as classes
                    ns = true;
                // nobreak
                case CLASS:
                case UNION:
                case STRUCT: // handle class, union, struct
                {
                    moveNext();
                    eat2Token(CppTokenId.SEMICOLON, true, false);
                    if (matchError) {
                        break main_loop;
                    }
                    if (LA(1) == CppTokenId.LBRACE) {
                        // TODO: for now we just use one level of folding
                        declarationsFold(ns ? CppFoldRecord.NAMESPACE_FOLD : CppFoldRecord.CLASS_FOLD);
                        if (matchError) {
                            break main_loop;
                        }
                    }
                    if (!ns) {
                        eat2Token(CppTokenId.SEMICOLON, true, true);
                    }
                    // allow errors
                    if (LA(1) == CppTokenId.SEMICOLON) {
                        moveNext();
                    }
                    if (matchError) {
                        break main_loop;
                    }
                    break;
                }
                default: // handle functions and other elements
                {
                    eat2Token(CppTokenId.SEMICOLON, true, true);
                    if (matchError) {
                        break main_loop;
                    }
                    switch (LA(1)) {
                        case LBRACE: {
                            blockFold(CppFoldRecord.FUNCTION_FOLD);
                            if (matchError) {
                                break main_loop;
                            }
                            break;
                        }
                        case RBRACE:
                            // RCURLY is expected somewhere outside
                            break main_loop;
                    }
                    // allow errors
                    if (LA(1) == CppTokenId.SEMICOLON) {
                        moveNext();
                    }
                    break;
                }
            }
        } while (false);
        if (matchError) {
            reportError(matchException);
//            recover(matchException,_tokenSet_1);
            resetMatchError();
        }
    }

    protected final void declarationsFold(int folderKind) {

        main_loop:
        while (true) {
            Token<CppTokenId> begin = LT(1);
            match(CppTokenId.LBRACE);
            if (matchError) {
                break main_loop;
            }

            // declarations loop
            do {
                // nongreedy exit test
                if (LA(1) == CppTokenId.RBRACE) {
                    break;
                }

                if (LA(1) == CppTokenId.EOF) {
                    break;
                }
                declaration();
                if (matchError) {
                    break main_loop;
                }
            } while (true);

            Token<CppTokenId> end = LT(1);
            match(CppTokenId.RBRACE);
            if (matchError) {
                break main_loop;
            }

            createFolder(folderKind, begin, end);

            break;
        } // End of loop main_loop
        if (matchError) {
            reportError(matchException);
            //           recover(matchException,_tokenSet_1);
            resetMatchError();
        }
    }

    protected final void blockFold(int foldType) {
        // LA(1) = LCURLY
        main_loop:
        while (true) {
            Token<CppTokenId> begin = LT(1);
            /* LCURLY - left balance (enter) */
            match(CppTokenId.LBRACE);
            if (matchError) {
                break main_loop;
            }

            do {
                // TODO ??? memory.cc, GetCategory
                if (LA(0) == CppTokenId.RBRACE) {
                    break;
                }
                // nongreedy exit test
                if (LA(1) == CppTokenId.RBRACE) {
                    /* RCURLY - right balance (exit) */
                    moveNext();
                    break;
                }

                // here is an error
                if (LA(1) == CppTokenId.EOF) {
                    break;
                }
                balanceCurliesAndCreateFolders(CppFoldRecord.COMPOUND_BLOCK_FOLD, 0, false);
                if (matchError) {
                    break main_loop;
                }
            } while (true);

            Token<CppTokenId> end = LT(0);

            if (LA(0) != CppTokenId.RBRACE) {
                matchError = true;
                break main_loop;
            }

            createFolder(foldType, begin, end);

            break;
        }
        if (matchError) {
            reportError(matchException);
            resetMatchError();
        }
    }

    private CppTokenId LA(int lookahead) {
        assert lookahead >= 0;

        int index = ts.index();
        boolean between = (ts.token() == null);
        boolean eof = false;
        while (lookahead > 0) {
            if (!moveNext(true)) {
                eof = true;
                break;
            }
            lookahead--;
        }
        CppTokenId id = eof ? CppTokenId.EOF : ts.token().id();
        ts.moveIndex(index);
        if (!between) {
            ts.moveNext();
        }
        return id;
    }

    private Token<CppTokenId> LT(int lookahead) {
        assert lookahead >= 0;

        int index = ts.index();
        boolean between = (ts.token() == null);
        
        boolean eof = false;
        while (lookahead > 0) {
            if (!moveNext(true)) {
                break;
            }
            lookahead--;
        }
        Token<CppTokenId> token = eof ? null : ts.offsetToken();
        ts.moveIndex(index);
        if (!between) {
            ts.moveNext();
        }
        return token;
    }

    private void match(TokenId id) {
        if (LA(1) == id) {
            moveNext();
        } else {
            matchError = true;
        }
    }

    public void matchNot(TokenId id) {
        if (LA(1) != id) {
            moveNext();
        } else {
            matchError = true;
        }
    }

    private boolean moveNext() {
        return moveNext(false);
    }

    private boolean moveNext(boolean lookahead) {
        int index = ts.index();
        boolean between = (ts.token() == null);

        while (true) {
            boolean moved = ts.moveNext();
            CppTokenId id = null;

            if (moved) {
                id = ts.token().id();
            }

            if (!moved) {
                if (!lookahead) {
                    visitEof();
                }
                ts.moveIndex(index);
                if (!between) {
                    ts.moveNext();
                }
                return false;
            } else {
                if (!lookahead) {
                    visit(ts.token());
                }
                if (!consume(ts.token())) {
                    break;
                }
            } 
        }

        // was moved successfully
        return true;
    }

    private void visit(Token<CppTokenId> token) {
        for (TokenFilter filter : filters) {
            filter.visit(token);
        }
    }
    
      private void visitEof() {
        for (TokenFilter filter : filters) {
            filter.visitEof();
        }
    }
    
    private boolean consume(Token<CppTokenId> token) {
        boolean consumed = false;
        for (TokenFilter filter : filters) {
            consumed |= filter.consumes(token.id());
        }
        return consumed;
    }

    private boolean matchError = false;
    private Exception matchException = defaultException;

    public static final Exception defaultException = new Exception("Exceptions turned off, so you are unable to see error description here"); //NOI18N

    // Options
    // Throw recognitionExceptions if needed (original behaviour)
    protected static final boolean throwRecExceptions = false;

    public void resetMatchError() {
        matchError = false;
        matchException = defaultException;
    }
}
