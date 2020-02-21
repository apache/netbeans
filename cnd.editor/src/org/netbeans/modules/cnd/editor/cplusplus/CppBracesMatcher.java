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

package org.netbeans.modules.cnd.editor.cplusplus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppStringTokenId;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_START;
import static org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_START_ALT;
import static org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;

/**
 * This is the org.netbeans.modules.editor.java.JavaBracesMatcher
 * with IZ118206 fixed to have consistency with Java matcher.
 *
 */
public class CppBracesMatcher implements BracesMatcher, BracesMatcherFactory {

    private static final char [] PAIRS = new char [] { '(', ')' //NOI18N
                                                     , '[', ']' //NOI18N
                                                     , '{', '}' //NOI18N
                                                     //, '<', '>' //NOI18N
                                                     };
    private static final CppTokenId [] PAIR_TOKEN_IDS = new CppTokenId [] { 
          CppTokenId.LPAREN, CppTokenId.RPAREN
        , CppTokenId.LBRACKET, CppTokenId.RBRACKET
        , CppTokenId.LBRACE, CppTokenId.RBRACE
        //, CppTokenId.LT, CppTokenId.GT
        };
    
    // Tokens where we should use standard matcher
    private static final Set<TokenId> SPECIAL_TOKENS = new HashSet(Arrays.asList(new TokenId[]{
        CppTokenId.STRING_LITERAL,
        CppTokenId.RAW_STRING_LITERAL,
        CppTokenId.BLOCK_COMMENT,
        CppTokenId.LINE_COMMENT,
        CppTokenId.DOXYGEN_LINE_COMMENT,
        CppTokenId.DOXYGEN_COMMENT,
        CppStringTokenId.TEXT
    }));

    private final MatcherContext context;

    private int originOffset;
    private char originChar;
    private char matchingChar;
    private boolean backward;
    private List<TokenSequence<?>> sequences;

    public CppBracesMatcher() {
        this(null);
    }

    private CppBracesMatcher(MatcherContext context){
        this.context = context;
    }

    @Override
    public int[] findOrigin() throws BadLocationException, InterruptedException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            int [] origin = BracesMatcherSupport.findChar(
                context.getDocument(), 
                context.getSearchOffset(), 
                context.getLimitOffset(), 
                PAIRS
            );

            if (origin != null) {
                originOffset = origin[0];
                originChar = PAIRS[origin[1]];
                matchingChar = PAIRS[origin[1] + origin[2]];
                backward = origin[2] < 0;
                // Filter out block and line comments
                TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
                sequences = th.embeddedTokenSequences(originOffset, backward);
                if (!sequences.isEmpty()) {
                    // Check special tokens
                    TokenSequence<?> seq = getTokenSequence();
                    if (seq == null) {
                        return null;
                    }
                }
                return new int [] { originOffset, originOffset + 1 };
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    private TokenSequence<?> getTokenSequence(){
        if (sequences.isEmpty()) {
            return null;
        }
        TokenSequence<?> seq = sequences.get(sequences.size() - 1);
        seq.move(originOffset);
        if (!seq.moveNext()) {
            if (sequences.size()>1) {
                seq = sequences.get(sequences.size() - 2);
                seq.move(originOffset);
                if (seq.moveNext()) {
                    return seq;
                }
            } else {
                return null;
            }
        } else {
            return seq;
        }
        return seq;
    }
    
    @Override
    public int[] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            TokenSequence<?> seq = getTokenSequence();
            if (seq == null) {
                return null;
            }
            // Check special tokens
            seq.move(originOffset);
            if (seq.moveNext() && SPECIAL_TOKENS.contains(seq.token().id())) {
                int offset = BracesMatcherSupport.matchChar(
                    context.getDocument(),
                    backward ? originOffset : originOffset + 1,
                    backward ? seq.offset() : seq.offset() + seq.token().length(),
                    originChar,
                    matchingChar);
                if (offset != -1) {
                    return new int [] { offset, offset + 1 };
                } else {
                    return null;
                }
            }
            // We are in plain c/c++
            CppTokenId originId = getTokenId(originChar);
            CppTokenId lookingForId = getTokenId(matchingChar);
            seq.move(originOffset);
            LinkedList<StackEntry> stack = new LinkedList<StackEntry>();
            stack.push(new StackEntry(0));
            //int counter = 0;
            if (backward) {
                while(seq.movePrevious()) {
                    processPreprocessor(seq, stack);
                    if (originId == seq.token().id()) {
                        stack.peek().dep++;
                    } else if (lookingForId == seq.token().id()) {
                        if (stack.peek().dep == 0) {
                            return new int [] { seq.offset(), seq.offset() + seq.token().length() };
                        } else {
                            stack.peek().dep--;
                        }
                    }
                }
            } else {
                seq.moveNext();
                while(seq.moveNext()) {
                    processPreprocessor(seq, stack);
                    if (originId == seq.token().id()) {
                        stack.peek().dep++;
                    } else if (lookingForId == seq.token().id()) {
                        if (stack.peek().dep == 0) {
                            return new int [] { seq.offset(), seq.offset() + seq.token().length() };
                        } else {
                            stack.peek().dep--;
                        }
                    }
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    private void processPreprocessor(TokenSequence<?> ts,LinkedList<StackEntry> stack) {
        TokenSequence<CppTokenId> prep = ts.embedded(CppTokenId.languagePreproc());
        if (prep == null){
            return;
        }
        prep.moveStart();
        while (prep.moveNext()) {
            if (!(prep.token().id() == WHITESPACE ||
                    prep.token().id() == PREPROCESSOR_START ||
                    prep.token().id() == PREPROCESSOR_START_ALT)) {
                break;
            }
        }
        Token<CppTokenId> directive = null;
        if (prep.token() != null) {
            directive = prep.token();
        }
        if (directive == null) {
            return;
        } 
        if (backward) {
            switch (directive.id()) {
                case PREPROCESSOR_IF:
                case PREPROCESSOR_IFDEF:
                case PREPROCESSOR_IFNDEF:
                {
                    if (stack.size() > 1) {
                        int current = stack.peek().dep;
                        if (stack.peek().matched) {
                            stack.poll();
                            stack.peek().dep = current;
                        } else {
                            stack.poll();
                        }
                    }
                    break;
                }
                case PREPROCESSOR_ELSE:
                case PREPROCESSOR_ELIF:
                {
                    boolean matched = false;
                    if (stack.size() > 1) {
                        stack.poll();
                        matched = true;
                    }
                    int current = stack.peek().dep;
                    stack.push(new StackEntry(current));
                    stack.peek().matched = matched;
                    break;
                }
                case PREPROCESSOR_ENDIF:
                    int current = stack.peek().dep;
                    stack.push(new StackEntry(current));
                    break;
            }
        } else {
            switch (directive.id()) {
                case PREPROCESSOR_IF:
                case PREPROCESSOR_IFDEF:
                case PREPROCESSOR_IFNDEF:
                {
                    int current = stack.peek().dep;
                    stack.push(new StackEntry(current));
                    break;
                }
                case PREPROCESSOR_ELSE:
                case PREPROCESSOR_ELIF:
                {
                    boolean matched = false;
                    if (stack.size() > 1) {
                        if (stack.peek().matched) {
                            stack.poll();
                            matched = true;
                        } else {
                            stack.poll();
                        }
                    }
                    int current = stack.peek().dep;
                    stack.push(new StackEntry(current));
                    stack.peek().matched = matched;
                    break;
                }
                case PREPROCESSOR_ENDIF:
                    if (stack.size() > 1) {
                        int current = stack.peek().dep;
                        if (stack.peek().matched) {
                            stack.poll();
                            stack.peek().dep = current;
                        } else {
                            stack.poll();
                        }
                    }
                    break;
            }
        }
    }

    
    private CppTokenId getTokenId(char ch) {
        for(int i = 0; i < PAIRS.length; i++) {
            if (PAIRS[i] == ch) {
                return PAIR_TOKEN_IDS[i];
            }
        }
        return null;
    }

    @Override
    public BracesMatcher createMatcher(MatcherContext context) {
        return new CppBracesMatcher(context);
    }
    
    private static final class StackEntry {
        int dep;
        boolean matched = true;
        private StackEntry(int dep) {
            this.dep = dep;
        }
    }
}
