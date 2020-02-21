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

import java.nio.CharBuffer;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CppTokenId;

/**
 *
 */
public class CommentTokenFilter implements TokenFilter {

    private Token<CppTokenId> firstLineComment = null;
    private Token<CppTokenId> lastLineComment = null;

    // states
    private static final int BEFORE_FIRST_TOKEN_STATE = 0;
    private static final int INIT_STATE = 1;
    private static final int AFTER_LINE_COMMENT = 3;

    private int state = BEFORE_FIRST_TOKEN_STATE;

    // folds
    private CppFoldRecord initialCommentFold = null;
    private final List<CppFoldRecord> blockCommentFolds = new ArrayList<CppFoldRecord>();
    private final List<CppFoldRecord> lineCommentFolds = new ArrayList<CppFoldRecord>();

    @Override
    public void visit(Token<CppTokenId> token) {
        if (CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory())) {
            return;
        }
        switch (token.id()) {
            case DOXYGEN_COMMENT:
            case BLOCK_COMMENT:
                if (isMultilineToken(token)) {
                    createBlockCommentsFold(token);
                }
            case DOXYGEN_LINE_COMMENT:
            case LINE_COMMENT:
//                if (lastTokenLine != next.getLine()) {
                if (firstLineComment == null) {
                    firstLineComment = token;
                    state = AFTER_LINE_COMMENT;
                }
                lastLineComment = token;
//                }
                break;
            case EOF:
                // can't get this token. Use visitEof instead
//                needNext = false;
                createLineCommentsFoldIfNeeded();
                break;
            case PREPROCESSOR_DIRECTIVE:
                onPreprocNode();
                break;
            default:
//                needNext = false;
//                lastTokenLine = next.getEndLine();
                createLineCommentsFoldIfNeeded();
        }
    }

    @Override
    public void visitEof() {
        createLineCommentsFoldIfNeeded();
    }

    @Override
    public boolean consumes(CppTokenId id) {
        String primaryCategory = id.primaryCategory();
        if (CppTokenId.WHITESPACE_CATEGORY.equals(primaryCategory)
                || CppTokenId.COMMENT_CATEGORY.equals(primaryCategory)) {
            return true;
        }
        return false;
    }

    public void onPreprocNode() {
        switch (state) {
            case BEFORE_FIRST_TOKEN_STATE:
                // first block comment wasn't found, switch to init state
                state = INIT_STATE;
                break;
            case AFTER_LINE_COMMENT:
                // met #-directive, flush line comments
                createLineCommentsFoldIfNeeded();
                break;
        }
    }

    @Override
    public List<CppFoldRecord> getFolders() {
        List<CppFoldRecord> out = new ArrayList<CppFoldRecord>(blockCommentFolds.size() + lineCommentFolds.size() + 1);
        if (initialCommentFold != null) {
            out.add(initialCommentFold);
        }
        out.addAll(blockCommentFolds);
        out.addAll(lineCommentFolds);
        return out;
    }

    private void createLineCommentsFoldIfNeeded() {
        if (state == AFTER_LINE_COMMENT) {
            if (firstLineComment != lastLineComment) {
                assert (firstLineComment != null);
                assert (lastLineComment != null);
//                if (firstLineComment.getLine() != lastLineComment.getEndLine()) {
                lineCommentFolds.add(createFoldRecord(CppFoldRecord.COMMENTS_FOLD, firstLineComment, lastLineComment));
//                }
            }
            firstLineComment = null;
            lastLineComment = null;
            state = INIT_STATE;
        }
    }

    private void createBlockCommentsFold(Token<CppTokenId> token) {
        createLineCommentsFoldIfNeeded();
//        if (token.getLine() != token.getEndLine()) {
        if (state == BEFORE_FIRST_TOKEN_STATE) {
            // this is the copyright
            assert (initialCommentFold == null) : "how there could be two copyrights?";
            initialCommentFold = createFoldRecord(CppFoldRecord.INITIAL_COMMENT_FOLD, token, token);
        } else {
            blockCommentFolds.add(createFoldRecord(CppFoldRecord.BLOCK_COMMENT_FOLD, token, token));
        }
//        }
        state = INIT_STATE;
    }

    private CppFoldRecord createFoldRecord(int folderKind, Token<CppTokenId> begin, Token<CppTokenId> end) {
        return new CppFoldRecord(folderKind, begin.offset(null), end.offset(null) + end.length());
    }

    /**
     * A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     */
    private boolean isMultilineToken(Token token) {
        CharSequence text = token.text();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n' || c == '\r') {
                return true;
            }
        }
        return false;
    }
}
