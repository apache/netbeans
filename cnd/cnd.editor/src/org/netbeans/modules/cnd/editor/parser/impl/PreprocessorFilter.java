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
package org.netbeans.modules.cnd.editor.parser.impl;

import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;

/**
 *
 */
public class PreprocessorFilter implements TokenFilter {

    private static final int IFDEF_FOLD = CppFoldRecord.IFDEF_FOLD;
    private static final int INCLUDES_FOLD = CppFoldRecord.INCLUDES_FOLD;

    private final TokenSequence ts;
    private final Stack<Token<CppTokenId>> ppStartDirectives = new Stack<Token<CppTokenId>>();
    private final List<CppFoldRecord> includeFolds = new ArrayList<CppFoldRecord>();
    private final List<CppFoldRecord> ifdefFolds = new ArrayList<CppFoldRecord>();

    private Token<CppTokenId> firstInclude = null;
    private Token<CppTokenId> firstEmbeddedInclude = null;
    private Token<CppTokenId> lastInclude = null;

    public PreprocessorFilter(TokenSequence ts) {
        this.ts = ts;
    }

    @Override
    public void visit(Token<CppTokenId> token) {
        assert ts.token() == token : ts.token() + " vs. " + token;
        if (token.id() != CppTokenId.PREPROCESSOR_DIRECTIVE) {
            // reset and flush includes
            addIncludesIfNeeded();
        }
        else {
            TokenSequence<CppTokenId> embedded = ts.embedded(CppTokenId.languagePreproc());
            while (embedded.moveNext()) {
                Token<CppTokenId> embeddedToken = embedded.offsetToken();
                Token<CppTokenId> ppToken = embedded.token();
                switch (ppToken.id()) {
                    case PREPROCESSOR_INCLUDE:
                    case PREPROCESSOR_INCLUDE_NEXT:
                        include(ts.offsetToken(), embeddedToken);
                        break;
                    case PREPROCESSOR_IF:
                    case PREPROCESSOR_IFDEF:
                    case PREPROCESSOR_IFNDEF:
                        onStartPreprocNode(ts.offsetToken());
                        break;
                    case PREPROCESSOR_ENDIF:
                        createEndifFold(ts.offsetToken());
                        break;
//                  case TOKEN_STREAM?
                    case PREPROCESSOR_DEFINE:
                    case PREPROCESSOR_UNDEF:
                    case PREPROCESSOR_ELIF:
                    case PREPROCESSOR_ELSE:
                    case PREPROCESSOR_ERROR:
                    case PREPROCESSOR_PRAGMA:
                        onOtherPreprocNode(ts.offsetToken());
                        break;
                    default:
                        /* 
                         do nothing (because WHITESPACE, PREPROC_START 
                         can appear and reset a sequence of line includes 
                         */
//                        onOtherPreprocNode(embedded.offsetToken());
                        break;
                }
            }
        }
    }

    @Override
    public void visitEof() {
        addIncludesIfNeeded();
    }

    @Override
    public boolean consumes(CppTokenId id) {
        if (CppTokenId.PREPROCESSOR_CATEGORY.equals(id.primaryCategory())) {
            return true;
        }
        return false;
    }

    @Override
    public List<CppFoldRecord> getFolders() {
        List<CppFoldRecord> out = new ArrayList<CppFoldRecord>(includeFolds.size() + ifdefFolds.size());
        out.addAll(includeFolds);
        out.addAll(ifdefFolds);
        return out;
    }

    private boolean onStartPreprocNode(Token<CppTokenId> apt) {
        addIncludesIfNeeded();
        ppStartDirectives.push(apt);
        return true;
    }

    private void createEndifFold(Token<CppTokenId> end) {
        addIncludesIfNeeded();
        // there could be errors with unbalanced directives => check 
        if (!ppStartDirectives.empty()) {
            Token<CppTokenId> start = ppStartDirectives.pop();
            // we want fold after full "#if A" directive
            ifdefFolds.add(new CppFoldRecord(IFDEF_FOLD, start.offset(null) + effectiveLength(start), end.offset(null) + effectiveLength(end)));
        }
    }

    private void include(Token<CppTokenId> top, Token<CppTokenId> embedded) {
        if (firstInclude == null) {
            firstInclude = top;
            firstEmbeddedInclude = embedded;
        }
        lastInclude = top;
    }

    private void addIncludesIfNeeded() {
        if (lastInclude != firstInclude) {
            assert (lastInclude != null);
            assert (firstInclude != null);
            assert (firstEmbeddedInclude != null);
            // we want fold after #include string
            int start = (firstEmbeddedInclude.offset(null) + firstEmbeddedInclude.length());
            int end = lastInclude.offset(null) + effectiveLength(lastInclude);
            if (start < end) {
                includeFolds.add(new CppFoldRecord(INCLUDES_FOLD, start, end));
            }
        }
        lastInclude = null;
        firstInclude = null;
        firstEmbeddedInclude = null;
    }

    private void onOtherPreprocNode(Token<CppTokenId> apt) {
        addIncludesIfNeeded();
    }

    private int effectiveLength(Token<CppTokenId> ppToken) {
        assert ppToken.id() == CppTokenId.PREPROCESSOR_DIRECTIVE : "" + ppToken;
        CharSequence str = ppToken.text();
        int len = str.length();
        if (str.charAt(len - 1) <= ' ') {
            len--;
        }
        return len;
    }
}
