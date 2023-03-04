/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javadoc;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;

/**
 *
 * @author Vita Stejskal
 */
public final class JavadocBracesMatcher implements BracesMatcher, BracesMatcherFactory {

    private static final Logger LOG = Logger.getLogger(JavadocBracesMatcher.class.getName());
    private static final Collection<String> VOID_TAGS = new HashSet<>(Arrays.asList(new String[]{
        "<area>", "<base>", "<br>", "<col>", "<command>", "<embed>", "<hr>", "<img>", //NOI18N
        "<input>", "<keygen>", "<link>", "<meta>", "<param>", "<source>", "<track>", "<wbr>" //NOI18N
    }));
    
    private final MatcherContext context;
    
    private TokenSequence<? extends TokenId> jdocSeq;
    private int jdocStart;
    private int jdocEnd;

//    private int [] matchingArea;
    
    private BracesMatcher defaultMatcher;
    
    public JavadocBracesMatcher() {
        this(null);
    }

    private JavadocBracesMatcher(MatcherContext context) {
        this.context = context;
    }
    
    // -----------------------------------------------------
    // BracesMatcher implementation
    // -----------------------------------------------------
    
    public int[] findOrigin() throws BadLocationException, InterruptedException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            int caretOffset = context.getSearchOffset();
            boolean backward = context.isSearchingBackward();

            TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
            List<TokenSequence<?>> sequences = th.embeddedTokenSequences(caretOffset, backward);

            for(int i = sequences.size() - 1; i >= 0; i--) {
                TokenSequence<? extends TokenId> seq = sequences.get(i);
                if (seq.language() == JavadocTokenId.language()) {
                    jdocSeq = seq;
                    if (i > 0) {
                        TokenSequence<? extends TokenId> javaSeq = sequences.get(i - 1);
                        jdocStart = javaSeq.offset();
                        jdocEnd = javaSeq.offset() + javaSeq.token().length();
                    } else {
                        // jdocSeq is the top level sequence, ie the whole document is just javadoc
                        jdocStart = 0;
                        jdocEnd = context.getDocument().getLength();
                    }
                    break;
                }
            }

            if (jdocSeq == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Not javadoc TokenSequence."); //NOI18N
                }
                return null;
            }

    //        if (caretOffset >= jdocStart && 
    //            ((backward && caretOffset <= jdocStart + 3) ||
    //            (!backward && caretOffset < jdocStart + 3))
    //        ) {
    //            matchingArea = new int [] { jdocEnd - 2, jdocEnd };
    //            return new int [] { jdocStart, jdocStart + 3 };
    //        }
    //
    //        if (caretOffset <= jdocEnd && 
    //            ((backward && caretOffset > jdocEnd - 2) ||
    //            (!backward && caretOffset >= jdocEnd - 2))
    //        ) {
    //            matchingArea = new int [] { jdocStart, jdocStart + 3 };
    //            return new int [] { jdocEnd - 2, jdocEnd };
    //        }

            // look for tags first
            jdocSeq.move(caretOffset);
            if (jdocSeq.moveNext()) {
                if (isTag(jdocSeq.token()) && !isTypeParameterTag(jdocSeq) && !isUninterpretedTag(jdocSeq)) {
                    if (jdocSeq.offset() < caretOffset || !backward) {
                        return prepareOffsets(jdocSeq, true);
                    }
                }

                while(moveTheSequence(jdocSeq, backward, context.getLimitOffset())) {
                    if (isTag(jdocSeq.token())) {
                        if (isTypeParameterTag(jdocSeq) || isUninterpretedTag(jdocSeq)) {
                            // do not treat type parameter and {@code} and {@literal} content as HTML tag
                            break;
                        }
                        return prepareOffsets(jdocSeq, true);
                    }
                }
            }

            defaultMatcher = BracesMatcherSupport.defaultMatcher(context, jdocStart, jdocEnd);
            return defaultMatcher.findOrigin();
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    public int[] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            if (defaultMatcher != null) {
                return defaultMatcher.findMatches();
            }

    //        if (matchingArea != null) {
    //            return matchingArea;
    //        }

            assert jdocSeq != null : "No javadoc token sequence"; //NOI18N

            Token<? extends TokenId> tag = jdocSeq.token();
            assert tag.id() == JavadocTokenId.HTML_TAG : "Wrong token"; //NOI18N

            if (isSingleTag(tag) || isVoidTag(tag)) {
                return new int [] { jdocSeq.offset(), jdocSeq.offset() + jdocSeq.token().length() };
            }

            boolean backward = !isOpeningTag(tag);
            int cnt = 0;

            while(moveTheSequence(jdocSeq, backward, -1)) {
                if (!isTag(jdocSeq.token())) {
                    continue;
                }

                if (matchTags(tag, jdocSeq.token())) {
                    if ((backward && !isOpeningTag(jdocSeq.token())) ||
                        (!backward && isOpeningTag(jdocSeq.token()))
                    ) {
                        cnt++;
                    } else {
                        if (cnt == 0) {
                            return prepareOffsets(jdocSeq, false);
                        } else {
                            cnt--;
                        }
                    }
                }
            }

            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    // -----------------------------------------------------
    // private implementation
    // -----------------------------------------------------

    private boolean moveTheSequence(TokenSequence<? extends TokenId> seq, boolean backward, int offsetLimit) {
        if (backward) {
            if (seq.movePrevious()) {
                int e = seq.offset() + seq.token().length();
                return offsetLimit == -1 ? true : e > offsetLimit;
            }
        } else {
            if (seq.moveNext()) {
                int s = seq.offset();
                return offsetLimit == -1 ? true : s < offsetLimit;
            }
        }
        return false;
    }

    private static boolean isTag(Token<? extends TokenId> tag) {
        CharSequence s = tag.text();
        int l = s.length();
        
        boolean b = tag.id() == JavadocTokenId.HTML_TAG &&
            l >= 3 &&
            s.charAt(0) == '<' && //NOI18N
            s.charAt(l - 1) == '>'; //NOI18N
        
        if (b) {
            if (s.charAt(1) == '/') { //NOI18N
                b = l >= 4 && Character.isLetterOrDigit(s.charAt(2));
            } else {
                b = Character.isLetterOrDigit(s.charAt(1));
            }
        }
        
        return b;
    }
    
    private static boolean isSingleTag(Token<? extends TokenId> tag) {
        return TokenUtilities.endsWith(tag.text(), "/>"); //NOI18N
    }
    
    private static boolean isVoidTag(Token<? extends TokenId> tag) {
        return VOID_TAGS.contains(tag.text().toString());
    }
    
    private static boolean isOpeningTag(Token<? extends TokenId> tag) {
        return !TokenUtilities.startsWith(tag.text(), "</"); //NOI18N
    }

    /**
     * simple check whether selected token is type parameter {@code @param <T>}
     * @param seq token sequence with selected token
     * @return {@code true} when the token should not be interpreted.
     */
    private static boolean isTypeParameterTag(TokenSequence<? extends TokenId> seq) {
        int index = seq.index();
        try {
            if (!seq.movePrevious() || seq.token().id() != JavadocTokenId.OTHER_TEXT) {
                return false;
            }

            return seq.movePrevious() && seq.token().id() == JavadocTokenId.TAG
                    && "@param".contentEquals(seq.token().text()); // NOI18N
        } finally {
            seq.moveIndex(index);
            seq.moveNext();
        }
    }

    /**
     * simple check whether selected token is part of {@code {@code} or {@literal}}
     * @param seq token sequence with selected token
     * @return {@code true} when the token should not be interpreted.
     */
    private static boolean isUninterpretedTag(TokenSequence<? extends TokenId> seq) {
        int index = seq.index();
        try {
            boolean lastCheck = false;
            while (seq.movePrevious()) {
                Token<? extends TokenId> token = seq.token();
                if (token.id() == JavadocTokenId.OTHER_TEXT) {
                    if (lastCheck) {
                        return token.text().charAt(token.length() - 1) == '{';
                    } else if (TokenUtilities.indexOf(token.text(), '}') >= 0) {
                        return false;
                    }
                }

                if (token.id() == JavadocTokenId.TAG) {
                    CharSequence text = token.text();
                    lastCheck = "@literal".contentEquals(text) || "@code".contentEquals(text); // NOI18N
                    continue;
                }
            }
            return false;
        } finally {
            seq.moveIndex(index);
            seq.moveNext();
        }
    }

    private static boolean matchTags(Token<? extends TokenId> t1, Token<? extends TokenId> t2) {
        assert t1.length() >= 2 && t1.text().charAt(0) == '<' : t1 + " is not a tag."; //NOI18N
        assert t2.length() >= 2 && t2.text().charAt(0) == '<' : t2 + " is not a tag."; //NOI18N
        
        int idx1 = 1;
        int idx2 = 1;
        
        if (t1.text().charAt(1) == '/') {
            idx1++;
        } 
        
        if (t2.text().charAt(1) == '/') {
            idx2++;
        }
        
        for( ; idx1 < t1.length() && idx2 < t2.length(); idx1++, idx2++) {
            char ch1 = t1.text().charAt(idx1);
            char ch2 = t2.text().charAt(idx2);
            
            if (ch1 != ch2) {
                return !Character.isLetterOrDigit(ch1) || !Character.isLetterOrDigit(ch2);
            }
            
            if (!Character.isLetterOrDigit(ch1)) {
                return true;
            }
        }
        
        return false;
    }

    private static int [] prepareOffsets(TokenSequence<? extends TokenId> seq, boolean includeToken) {
        int s = seq.offset();
        int e = seq.offset() + seq.token().length();
        CharSequence token = seq.token().text();
        
        if (token.charAt(1) == '/') { //NOI18N
            return new int [] { s, e };
        } else {
            int he = e;
            
            for(int i = 1; i < token.length(); i++) {
                char ch = token.charAt(i);
                if (!Character.isLetterOrDigit(ch) && ch != '>') { //NOI18N
                    he = s + i;
                    break;
                }
            }
            
            if (includeToken) {
                // first the boundaries, than the highlight
                return new int [] { s, e, s, he };
            } else {
                return new int [] { s, he };
            }
        }
    }
    
    // -----------------------------------------------------
    // BracesMatcherFactory implementation
    // -----------------------------------------------------
    
    /** */
    public BracesMatcher createMatcher(MatcherContext context) {
        return new JavadocBracesMatcher(context);
    }

}
