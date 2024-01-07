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

import com.sun.source.tree.IfTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.bracesmatching.BraceContext;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Vita Stejskal
 */
public final class JavaBracesMatcher implements BracesMatcher, BracesMatcherFactory, BracesMatcher.ContextLocator {

    private static final char [] PAIRS = new char [] { '(', ')', '[', ']', '{', '}' }; //NOI18N
    private static final JavaTokenId [] PAIR_TOKEN_IDS = new JavaTokenId [] { 
        JavaTokenId.LPAREN, JavaTokenId.RPAREN, 
        JavaTokenId.LBRACKET, JavaTokenId.RBRACKET, 
        JavaTokenId.LBRACE, JavaTokenId.RBRACE
    };
    
    private final MatcherContext context;
    
    private int originOffset;
    private char originChar;
    private char matchingChar;
    private boolean backward;
    private List<TokenSequence<?>> sequences;
    
    public JavaBracesMatcher() {
        this(null);
    }

    private JavaBracesMatcher(MatcherContext context) {
        this.context = context;
    }
    
    private JavaBracesMatcher(MatcherContext context, int searchOffset) {
        this(context);
        this.searchOffset = searchOffset;
    }
    
    private int searchOffset = -1;
    
    private int getSearchOffset() {
        return searchOffset >=0 ? searchOffset : context.getSearchOffset();
    }
    
    // -----------------------------------------------------
    // BracesMatcher implementation
    // -----------------------------------------------------
    
    @Override
    public int[] findOrigin() throws BadLocationException, InterruptedException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            int [] origin = BracesMatcherSupport.findChar(
                context.getDocument(), 
                getSearchOffset(), 
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
                sequences = getEmbeddedTokenSequences(th, originOffset, backward, JavaTokenId.language());

                if (!sequences.isEmpty()) {
                    // Check special tokens
                    TokenSequence<?> seq = sequences.get(sequences.size() - 1);
                    seq.move(originOffset);
                    if (seq.moveNext()) {
                        if (seq.token().id() == JavaTokenId.BLOCK_COMMENT ||
                            seq.token().id() == JavaTokenId.LINE_COMMENT
                        ) {
                            return null;
                        }
                    }
                }

                return new int [] { originOffset, originOffset + 1 };
            } else {
                return null;
            }
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    @Override
    public int[] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            if (!sequences.isEmpty()) {
                TokenSequence<?> seq = sequences.get(sequences.size() - 1);

                TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
                List<TokenSequence<?>> list;
                if (backward) {
                    list = th.tokenSequenceList(seq.languagePath(), 0, originOffset);
                } else {
                    list = th.tokenSequenceList(seq.languagePath(), originOffset + 1, context.getDocument().getLength());
                }
                int counter = 0;

                seq.move(originOffset);
                if (seq.moveNext()) {
                    Token<?> token = seq.token();

                    if (token.id() == JavaTokenId.STRING_LITERAL ||
                        token.id() == JavaTokenId.MULTILINE_STRING_LITERAL) {

                        for(TokenSequenceIterator tsi = new TokenSequenceIterator(list, backward); tsi.hasMore(); ) {
                            TokenSequence<?> sq = tsi.getSequence();

                            if (sq.token().id() == JavaTokenId.STRING_LITERAL ||
                                sq.token().id() == JavaTokenId.MULTILINE_STRING_LITERAL) {

                                CharSequence text = sq.token().text();
                                if (backward) {
                                    // check the character at the left from the caret
                                    int bound = originOffset - sq.offset();
                                    if (bound >= 0)
                                        bound = Math.min(text.length() - 1, bound);
                                    for(int i = bound - 1; i > 0; i--) {
                                        if (originChar == text.charAt(i)) {
                                            counter++;
                                        } else if (matchingChar == text.charAt(i)) {
                                            if (counter == 0) {
                                                return new int [] {sq.offset() + i, sq.offset() + i + 1};
                                            } else {
                                                counter--;
                                            }
                                        }
                                    }
                                } else {
                                    // check the character at the right from the caret
                                    int bound = originOffset - sq.offset() + 1;
                                    if (bound < 0 || bound > text.length())
                                        bound = 1;
                                    for(int i = bound; i < text.length() - 1; i++) {
                                        if (originChar == text.charAt(i)) {
                                            counter++;
                                        } else if (matchingChar == text.charAt(i)) {
                                            if (counter == 0) {
                                                return new int [] {sq.offset() + i, sq.offset() + i + 1};
                                            } else {
                                                counter--;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if ((token.partType() == PartType.START || token.partType() == PartType.MIDDLE) && (seq.offset() + token.length()) == originOffset + (backward ? 0 : 1)) {
                            while (seq.moveNext()) {
                                Token<?> t = seq.token();

                                if (t.id() == JavaTokenId.STRING_LITERAL ||
                                    t.id() == JavaTokenId.MULTILINE_STRING_LITERAL) {

                                    switch (t.partType()) {
                                        case START: counter++; break;
                                        case END:
                                            if (counter > 0) {
                                                counter--;
                                                break;
                                            }
                                        case MIDDLE:
                                            if (counter == 0) {
                                                return new int[] {seq.offset(), seq.offset() + 1};
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                        if ((token.partType() == PartType.END || token.partType() == PartType.MIDDLE) && seq.offset() == originOffset - (backward ? 0 : 1)) {
                            while (seq.movePrevious()) {
                                Token<?> t = seq.token();

                                if (t.id() == JavaTokenId.STRING_LITERAL ||
                                    t.id() == JavaTokenId.MULTILINE_STRING_LITERAL) {

                                    switch (t.partType()) {
                                        case END: counter++; break;
                                        case START:
                                            if (counter > 0) {
                                                counter--;
                                                break;
                                            }
                                        case MIDDLE:
                                            if (counter == 0) {
                                                int endPos = seq.offset() + seq.token().length();
                                                return new int[] {endPos - 1, endPos};
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                        return null;                        
                    }
                }

                JavaTokenId originId = getTokenId(originChar);
                JavaTokenId lookingForId = getTokenId(matchingChar);

                for(TokenSequenceIterator tsi = new TokenSequenceIterator(list, backward); tsi.hasMore(); ) {
                    TokenSequence<?> sq = tsi.getSequence();

                    if (originId == sq.token().id()) {
                        counter++;
                    } else if (lookingForId == sq.token().id()) {
                        if (counter == 0) {
                            matchStart = sq.offset();
                            return new int [] { sq.offset(), sq.offset() + sq.token().length() };
                        } else {
                            counter--;
                        }
                    }
                }
            }

            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }
    
    /**
     * Start of the matched brace/bracket
     */
    private int matchStart;
    
    /**
     * Provides better context if the matched counterpart character is the opening curly brace.
     */
    @Override
    public BraceContext findContext(int originOrMatchPosition) {
        if (backward && matchingChar == '{') {
            try {
                return findContextBackwards(originOrMatchPosition);
            } catch (BadLocationException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
    public BraceContext findContextBackwards(final int p2) throws BadLocationException, IOException {
        // sanity check, do not accept anything but the original offset for now.
        if (p2 != originOffset) {
            return null;
        }
        final int position = matchStart;
        
        JavaSource src = JavaSource.forDocument(context.getDocument());
        if (src == null) {
            return null;
        }
        final BraceContext[] ret = new BraceContext[1];
        src.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController ctrl) throws Exception {
                ctrl.toPhase(JavaSource.Phase.PARSED);
                TreePath path = ctrl.getTreeUtilities().pathFor(position + 1);
                if (path == null) {
                    return;
                }
                StatementTree block;
                // unwrap compound statements, so path.getLeaf() can be matched to statement parts below
                if (path.getLeaf().getKind() == Tree.Kind.BLOCK) {
                    block = ((StatementTree)path.getLeaf());
                    path = path.getParentPath();
                } else {
                    block = null;
                }
                
                switch (path.getLeaf().getKind()) {
                    case IF: {
                        IfTree ifTree = (IfTree)path.getLeaf();
                        // the path may be the else branch of the if
                        if (block == ifTree.getElseStatement()) {
                            // the related region is the if statement up to the 'then' statement
                            final int[] elseStart = { (int)ctrl.getTrees().getSourcePositions().getStartPosition(
                                    ctrl.getCompilationUnit(), ifTree.getElseStatement())};

                            // must use lexer to iterate backwards from block start to 'else' keyword. The keyword position
                            // is not a part of the Tree
                            context.getDocument().render(new Runnable() {
                                @Override
                                public void run() {
                                    TokenHierarchy h = TokenHierarchy.get(context.getDocument());
                                    TokenSequence seq = h.tokenSequence();
                                    int off = seq.move(elseStart[0]);
                                    if (off == 0 && seq.moveNext() && seq.token().id() == JavaTokenId.LBRACE) {
                                        while (seq.movePrevious()) {
                                            TokenId id = seq.token().id();
                                            if (!(id == JavaTokenId.WHITESPACE || id == JavaTokenId.BLOCK_COMMENT ||
                                                id == JavaTokenId.LINE_COMMENT)) {
                                                break;
                                            }
                                        }
                                        if (seq.token().id() == JavaTokenId.ELSE) {
                                            elseStart[0] = seq.offset();
                                        }
                                    }
                                }
                            });
                            // the context is the else statement up to the brace position
                            int ifStart = (int)ctrl.getTrees().getSourcePositions().getStartPosition(
                                    ctrl.getCompilationUnit(), ifTree);
                            int ifEnd;
                            
                            if (ifTree.getThenStatement().getKind() == Tree.Kind.BLOCK) {
                                ifEnd = (int)ctrl.getTrees().getSourcePositions().getStartPosition(
                                    ctrl.getCompilationUnit(), ifTree.getThenStatement());
                            } else {
                                ifEnd = (int)ctrl.getTrees().getSourcePositions().getEndPosition(
                                    ctrl.getCompilationUnit(), ifTree.getCondition());
                            }
                            BraceContext rel = BraceContext.create(
                                    context.getDocument().createPosition(ifStart),
                                    context.getDocument().createPosition(ifEnd + 1));
                            ret[0] = rel.createRelated(
                                    context.getDocument().createPosition(elseStart[0]), 
                                    context.getDocument().createPosition(position + 1));
                            return;
                        }
                    }
                    // fall through
                    case SWITCH:
                    case WHILE_LOOP: 
                    case METHOD:
                    case NEW_CLASS:
                    case CASE:
                    {
                        // take start of the command as the context
                        long start = ctrl.getTrees().getSourcePositions().getStartPosition(
                                ctrl.getCompilationUnit(), path.getLeaf());
                        ret[0] = BraceContext.create(
                            context.getDocument().createPosition((int)start),
                            context.getDocument().createPosition(position));
                        return;
                    }
                    case CLASS:
                    {
                        long start = ctrl.getTrees().getSourcePositions().getStartPosition(
                                ctrl.getCompilationUnit(), block != null ? block : path.getLeaf());
                        ret[0] = BraceContext.create(
                            context.getDocument().createPosition((int)start),
                            context.getDocument().createPosition(position));
                        return;
                    }
                    default:
                        return;
                        
                }
            }
        }, true);
        
        return ret[0];
    }
    
    // -----------------------------------------------------
    // private implementation
    // -----------------------------------------------------
    
    private JavaTokenId getTokenId(char ch) {
        for(int i = 0; i < PAIRS.length; i++) {
            if (PAIRS[i] == ch) {
                return PAIR_TOKEN_IDS[i];
            }
        }
        return null;
    }
    
    public static List<TokenSequence<?>> getEmbeddedTokenSequences(
        TokenHierarchy<?> th, int offset, boolean backwardBias, Language<?> language
    ) {
        List<TokenSequence<?>> sequences = th.embeddedTokenSequences(offset, backwardBias);

        for(int i = sequences.size() - 1; i >= 0; i--) {
            TokenSequence<?> seq = sequences.get(i);
            if (seq.language() == language) {
                break;
            } else {
                sequences.remove(i);
            }
        }
        
        return sequences;
    }
    
    private static final class TokenSequenceIterator {
        
        private final List<TokenSequence<?>> list;
        private final boolean backward;
        
        private int index;
        
        public TokenSequenceIterator(List<TokenSequence<?>> list, boolean backward) {
            this.list = list;
            this.backward = backward;
            this.index = -1;
        }
        
        public boolean hasMore() {
            return backward ? hasPrevious() : hasNext();
        }

        public TokenSequence<?> getSequence() {
            assert index >= 0 && index < list.size() : "No sequence available, call hasMore() first."; //NOI18N
            return list.get(index);
        }
        
        private boolean hasPrevious() {
            boolean anotherSeq = false;
            
            if (index == -1) {
                index = list.size() - 1;
                anotherSeq = true;
            }
            
            for( ; index >= 0; index--) {
                TokenSequence<?> seq = list.get(index);
                if (anotherSeq) {
                    seq.moveEnd();
                }
                
                if (seq.movePrevious()) {
                    return true;
                }
                
                anotherSeq = true;
            }
            
            return false;
        }
        
        private boolean hasNext() {
            boolean anotherSeq = false;
            
            if (index == -1) {
                index = 0;
                anotherSeq = true;
            }
            
            for( ; index < list.size(); index++) {
                TokenSequence<?> seq = list.get(index);
                if (anotherSeq) {
                    seq.moveStart();
                }
                
                if (seq.moveNext()) {
                    return true;
                }
                
                anotherSeq = true;
            }
            
            return false;
        }
    } // End of TokenSequenceIterator class
    
    // -----------------------------------------------------
    // BracesMatcherFactory implementation
    // -----------------------------------------------------
    
    /** */
    @Override
    public BracesMatcher createMatcher(MatcherContext context) {
        return new JavaBracesMatcher(context);
    }

}
