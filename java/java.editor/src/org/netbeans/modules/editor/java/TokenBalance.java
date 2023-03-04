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
package org.netbeans.modules.editor.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 * Token balance computes balance of certain tokens - mainly braces.
 */
class TokenBalance implements TokenHierarchyListener {

    public static <T extends TokenId> TokenBalance get(Document doc) {
        TokenBalance tb = (TokenBalance)doc.getProperty(TokenBalance.class);
        if (tb == null) {
            tb = new TokenBalance(doc);
            doc.putProperty(TokenBalance.class, tb);
        }
        return tb;
    }

    private final Document doc;

    private final Map<Language<?>,LanguageHandler<?>> lang2handler;

    private boolean scanDone;

    private TokenBalance(Document doc) {
        this.doc = doc;
        lang2handler = new HashMap<Language<?>, LanguageHandler<?>>();
        TokenHierarchy hi = TokenHierarchy.get(doc);
        hi.addTokenHierarchyListener(this);
    }

    public boolean isTracked(Language<?> language) {
        return (handler(language, false) != null);
    }

    public <T extends TokenId> void addTokenPair(Language<T> language, T left, T right) {
        synchronized (lang2handler) {
            handler(language, true).addTokenPair(left, right);
            scanDone = false;
        }
    }

    public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
        synchronized (lang2handler) {
            if (evt.type() == TokenHierarchyEventType.ACTIVITY ||
                    evt.type() == TokenHierarchyEventType.REBUILD)
            {
                scanDone = false;
            } else {
                if (scanDone) { // Only update if the full scan was already done
                    for (LanguageHandler<?> handler : lang2handler.values()) {
                        handler.handleEvent(evt);
                    }
                }
            }
        }
    }

    /**
     * Get balance for the given left id.
     *
     * @param left left-id
     * @return balance value above zero means more lefts than rights and vice versa.
     *  Returns Integer.MAX_VALUE if the particular id is not tracked (or is tracked
     *  as non-left id e.g. '[' would return balance but ']' would return Integer.MAX_VALUE).
     */
    public <T extends TokenId> int balance(Language<T> language, T left) {
        synchronized (lang2handler) {
            checkScanDone();
            LanguageHandler<T> handler = handler(language, false);
            return (handler != null) ? handler.balance(left) : Integer.MAX_VALUE;
        }
    }
    
    private <T extends TokenId> LanguageHandler<T> handler(Language<T> language, boolean forceCreation) {
        // Should always be called under lang2handler sync section
        @SuppressWarnings("unchecked")
        LanguageHandler<T> handler = (LanguageHandler<T>) lang2handler.get(language);
        if (handler == null && forceCreation) {
            handler = new LanguageHandler<T>(language);
            lang2handler.put(language, handler);
        }
        return handler;
    }

    private void checkScanDone() {
        synchronized (lang2handler) {
            if (!scanDone) {
                TokenHierarchy hi = TokenHierarchy.get(doc);
                for (LanguageHandler<?> handler : lang2handler.values()) {
                    handler.scan(hi);
                }
                scanDone = true;
            }
        }
    }
    
    private static final class LanguageHandler<T extends TokenId> {
        
        private final Language<T> language;

        private final Map<T, TokenIdPair<T>> id2Pair;

        LanguageHandler(Language<T> language) {
            this.language = language;
            id2Pair = new HashMap<T, TokenIdPair<T>>();
        }
        
        public final Language<T> language() {
            return language;
        }

        public void addTokenPair(T left, T right) {
            TokenIdPair<T> pair = new TokenIdPair<T>(left, right);
            synchronized (id2Pair) {
                id2Pair.put(left, pair);
                id2Pair.put(right, pair);
            }
        }

        public void scan(TokenHierarchy hi) {
            for (TokenIdPair pair : id2Pair.values()) {
                pair.balance = 0;
            }
            // Clear balances first
            TokenSequence<?> ts = hi.tokenSequence();
            if (ts != null) {
                processTokenSequence(ts, ts.tokenCount(), true, +1);
            }

        }

        public void processTokenSequence(TokenSequence<?> ts, int tokenCount, boolean checkEmbedded, int diff) {
            while (--tokenCount >= 0) {
                boolean moved = ts.moveNext();
                assert (moved);
                if (ts.language() == language) {
                    T id = (T)ts.token().id();
                    TokenIdPair pair = id2Pair.get(id);
                    if (pair != null) {
                        pair.updateBalance(id, diff);
                    }
                }
                if (checkEmbedded) {
                    TokenSequence<?> embeddedTS = ts.embedded();
                    if (embeddedTS != null)
                        processTokenSequence(embeddedTS, embeddedTS.tokenCount(), true, diff);
                }
            }
        }

        public void handleEvent(TokenHierarchyEvent evt) {
            for (TokenChange<T> tokenChange : collectTokenChanges(evt.tokenChange(), new ArrayList<TokenChange<T>>())) {
                if (tokenChange.removedTokenCount() > 0) {
                    processTokenSequence(tokenChange.removedTokenSequence(), tokenChange.removedTokenCount(), false, -1);
                }
                if (tokenChange.addedTokenCount() > 0) {
                    processTokenSequence(tokenChange.currentTokenSequence(), tokenChange.addedTokenCount(), false, +1);
                }
            }
        }

        public int balance(T left) {
            TokenIdPair pair = id2Pair.get(left);
            return (pair.left == left) ? pair.balance : Integer.MAX_VALUE;
        }

        private List<TokenChange<T>> collectTokenChanges(TokenChange<?> change, List<TokenChange<T>> changes) {
            if (change.language() == language)
                changes.add((TokenChange<T>)change);
            for (int i = 0; i < change.embeddedChangeCount(); i++) {
                collectTokenChanges(change.embeddedChange(i), changes);
            }
            return changes;
        }
    } 

    private static final class TokenIdPair<T extends TokenId> {

        T left;

        T right;

        int balance;

        public TokenIdPair(T left, T right) {
            this.left = left;
            this.right = right;
        }

        public void updateBalance(T id, int diff) {
            if (id == left) {
                balance += diff;
            } else {
                assert (id == right);
                balance -= diff;
            }
        }

    }

}
