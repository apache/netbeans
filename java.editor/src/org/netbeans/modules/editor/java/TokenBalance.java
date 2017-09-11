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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
