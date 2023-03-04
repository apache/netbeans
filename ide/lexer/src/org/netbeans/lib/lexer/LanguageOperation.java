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

package org.netbeans.lib.lexer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.token.DefaultToken;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.lib.lexer.token.TextToken;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.spi.lexer.TokenValidator;
import org.openide.util.WeakListeners;

/**
 * The operation behind the language hierarchy.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class LanguageOperation<T extends TokenId> implements PropertyChangeListener {
    
    private static final int MAX_START_SKIP_LENGTH_CACHED = 10;
    
    private static final int MAX_END_SKIP_LENGTH_CACHED = 10;
    
    private static final TokenValidator<TokenId> NULL_VALIDATOR
            = new TokenValidator<TokenId>() {
                public Token<TokenId> validateToken(Token<TokenId> token,
                TokenFactory<TokenId> factory,
                CharSequence tokenText, int modRelOffset,
                int removedLength, CharSequence removedText,
                int insertedLength, CharSequence insertedText) {
                    return null;
                }
    };
    
    /**
     * Find the language paths either for this language only
     * or from TokenHierarchyOperation when adding a new default or custom embedding
     * to the token hierarchy.
     * <br/>
     * As a language may finally be embedded in itself (e.g. someone might
     * want to syntax color java code snippet embedded in javadoc)
     * this method must prevent creation of infinite language paths
     * by using exploredLanguages parameter.
     * 
     * @param existingLanguagePaths set of language paths that are already known.
     *  This set is not modified by the method.
     * @param newLanguagePaths newly discovered language paths will be added to this set.
     * @param exploredLanguages used for checking whether the subpaths containing
     *  this language were already discovered.
     * @param lp language path that will be checked. Its innermost language
     *  will be checked for embeddings automatically.
     */
    public static <T extends TokenId> void findLanguagePaths(
    Set<LanguagePath> existingLanguagePaths, Set<LanguagePath> newLanguagePaths,
    Set<Language<?>> exploredLanguages, LanguagePath lp) {
        // Get the complete language path
        if (!existingLanguagePaths.contains(lp)) {
            newLanguagePaths.add(lp);
        }
        Language<T> language = (Language<T>)LexerUtilsConstants.<T>innerLanguage(lp);
        if (!exploredLanguages.contains(language)) {
            exploredLanguages.add(language);
            Set<T> ids = language.tokenIds();
            for (T id : ids) {
                // Create a fake empty token
                DefaultToken<T> emptyToken = new DefaultToken<T>(new WrapTokenId<T>(id));
                // Find embedding for non-flyweight token
                LanguageHierarchy<T> languageHierarchy = LexerUtilsConstants.innerLanguageHierarchy(lp);
                LanguageEmbedding<?> embedding = LexerUtilsConstants.findEmbedding(
                        languageHierarchy, emptyToken, lp, null);
                if (embedding != null) {
                    LanguagePath elp = LanguagePath.get(lp, embedding.language());
                    findLanguagePaths(existingLanguagePaths, newLanguagePaths,
                            exploredLanguages, elp);
                }
            }
        }
    }
    

    private final LanguageHierarchy<T> languageHierarchy;
    
    private final Language<T> language;
    
    /** Embeddings cached by start skip length and end skip length. */
    private LanguageEmbedding<T>[][] cachedEmbeddings;
    
    /**
     * Possibility of embedding presence for token ids.
     */
    private EmbeddingPresence[] embeddingPresences;
    
    private LanguageEmbedding<T>[][] cachedJoinSectionsEmbeddings;
    
    private TokenValidator<T>[] tokenValidators;
    
    private Set<LanguagePath> languagePaths;
    
    private Set<Language<?>> exploredLanguages;
    
    private FlyItem<T>[] flyItems;
    
    public LanguageOperation(LanguageHierarchy<T> languageHierarchy, Language<T> language) {
        this.languageHierarchy = languageHierarchy;
        this.language = language; // Should not be operated during constructor (not inited yet)

        // Listen on changes in language manager
        LanguageManager.getInstance().addPropertyChangeListener(
        WeakListeners.create(PropertyChangeListener.class, this, LanguageManager.getInstance()));
    }
    
    public Language<T> language() {
        return language;
    }
    
    public synchronized TokenValidator<T> tokenValidator(T id) {
        if (tokenValidators == null) {
            tokenValidators = allocateTokenValidatorArray(language.maxOrdinal() + 1);
        }
        // Not synced intentionally (no problem to create dup instances)
        TokenValidator<T> validator = tokenValidators[id.ordinal()];
        if (validator == null) {
            validator = LexerSpiPackageAccessor.get().createTokenValidator(languageHierarchy, id);
            if (validator == null) {
                validator = nullValidator();
            }
            tokenValidators[id.ordinal()] = validator;
        }
        return (validator == nullValidator()) ? null : validator;
    }
    
    public synchronized TextToken<T> getFlyweightToken(WrapTokenId<T> wid, String text) {
        if (flyItems == null) {
            // Create flyItems array
            @SuppressWarnings("unchecked")
            FlyItem<T>[] arr = (FlyItem<T>[])new FlyItem[language.maxOrdinal() + 1];
            flyItems = arr;
        }
        FlyItem<T> item = flyItems[wid.id().ordinal()];
        if (item == null) {
            item = new FlyItem<T>(wid, text);
            flyItems[wid.id().ordinal()] = item;
        }
        return item.flyToken(wid, text);
    }
    
    public synchronized EmbeddingPresence embeddingPresence(T id) {
        if (embeddingPresences == null) {
            embeddingPresences = new EmbeddingPresence[language.maxOrdinal() + 1];
        }
        EmbeddingPresence ep = embeddingPresences[id.ordinal()];
        if (ep == null) { // Not initialized yet
            ep = LexerSpiPackageAccessor.get().embeddingPresence(languageHierarchy, id);
            embeddingPresences[id.ordinal()] = ep;
        }
        return ep;
    }
    
    public synchronized void setEmbeddingPresence(T id, EmbeddingPresence ep) {
        // No check embeddingPresences==null since always called after embeddingPresence(T id)
        embeddingPresences[id.ordinal()] = ep;
    }
    
    /**
     * Get cached or create a new embedding with the language of this operation
     * and the given start and end skip lengths.
     * @return non-null embedding.
     */
    public synchronized LanguageEmbedding<T> getEmbedding(
    int startSkipLength, int endSkipLength, boolean joinSections) {
        LanguageEmbedding<T>[][] ce = joinSections ? cachedJoinSectionsEmbeddings : cachedEmbeddings;
        if (ce == null || startSkipLength >= ce.length) {
            if (startSkipLength > MAX_START_SKIP_LENGTH_CACHED)
                return createEmbedding(startSkipLength, endSkipLength, joinSections);
            @SuppressWarnings("unchecked")
            LanguageEmbedding<T>[][] tmp = (LanguageEmbedding<T>[][])
                    new LanguageEmbedding[startSkipLength + 1][];
            if (ce != null)
                System.arraycopy(ce, 0, tmp, 0, ce.length);
            ce = tmp;
            if (joinSections)
                cachedJoinSectionsEmbeddings = ce;
            else
                cachedEmbeddings = ce;
        }
        LanguageEmbedding<T>[] byESL = ce[startSkipLength];
        if (byESL == null || endSkipLength >= byESL.length) { // given endSkipLength not cached
            if (endSkipLength > MAX_END_SKIP_LENGTH_CACHED)
                return createEmbedding(startSkipLength, endSkipLength, joinSections);
            @SuppressWarnings("unchecked")
            LanguageEmbedding<T>[] tmp = (LanguageEmbedding<T>[])
                    new LanguageEmbedding[endSkipLength + 1];
            if (byESL != null)
                System.arraycopy(byESL, 0, tmp, 0, byESL.length);
            byESL = tmp;
            ce[startSkipLength] = byESL;
        }
        LanguageEmbedding<T> e = byESL[endSkipLength];
        if (e == null) {
            e = createEmbedding(startSkipLength, endSkipLength, joinSections);
            byESL[endSkipLength] = e;
        }
        return e;
    }
    
    private LanguageEmbedding<T> createEmbedding(int startSkipLength, int endSkipLength, boolean joinSections) {
        return LexerSpiPackageAccessor.get().createLanguageEmbedding(
                language, startSkipLength, endSkipLength, joinSections);
    }
    
    /**
     * Get static language paths for this language.
     */
    public Set<LanguagePath> languagePaths() {
        Set<LanguagePath> lps;
        synchronized (this) {
            lps = languagePaths;
        }
        if (lps == null) {
            lps = new HashSet<LanguagePath>();
            Set<LanguagePath> existingLps = Collections.emptySet();
            Set<Language<?>> exploredLangs = new HashSet<Language<?>>();
            findLanguagePaths(existingLps, lps, exploredLangs, LanguagePath.get(language));
            synchronized (this) {
                languagePaths = lps;
                exploredLanguages = exploredLangs;
            }
        }
        return lps;
    }
    
    public Set<Language<?>> exploredLanguages() {
        languagePaths(); // Init exploredLanguages
        return exploredLanguages;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (this) {
            languagePaths = null;
            exploredLanguages = null;
        }
    }

    @SuppressWarnings("unchecked")
    private final TokenValidator<T> nullValidator() {
        return (TokenValidator<T>)NULL_VALIDATOR;
    }

    @SuppressWarnings("unchecked")
    private final TokenValidator<T>[] allocateTokenValidatorArray(int length) {
        return (TokenValidator<T>[]) new TokenValidator[length];
    }

    
    private static final class FlyItem<T extends TokenId> {
        
        private TextToken<T> token; // Most used (first candidate)
        
        private TextToken<T> token2; // Second most used
        
        FlyItem(WrapTokenId<T> wid, String text) {
            newToken(wid, text);
            token2 = token; // Make both item non-null
        }

        TextToken<T> flyToken(WrapTokenId<T> wid, String text) {
            // First do a quick check for equality only in both items
            if (token.text() != text) {
                if (token2.text() == text) {
                    // Swap token and token2 => token will contain the right value
                    swap();
                } else { // token.text() != text && token2.text() != text
                    // Now deep-compare of text of both tokens
                    if (!CharSequenceUtilities.textEquals(token.text(), text)) {
                        if (!CharSequenceUtilities.textEquals(token2.text(), text)) {
                            token2 = token;
                            newToken(wid, text);
                        } else { // swap
                            swap();
                        }
                    }
                }
            }
            return token;
        }
        
        void newToken(WrapTokenId<T> wid, String text) {
            // Create new token
            token = new TextToken<T>(wid, text);
            token.makeFlyweight();
        }
        
        private void swap() {
            TextToken<T> tmp = token;
            token = token2;
            token2 = tmp;
        }
        
    }

}
