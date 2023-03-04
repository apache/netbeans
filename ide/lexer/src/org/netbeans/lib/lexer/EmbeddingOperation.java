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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.inc.TokenChangeInfo;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.lib.lexer.inc.TokenHierarchyUpdate;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.spi.lexer.EmbeddingPresence;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;

/**
 * Various static methods for embedding maintenance.
 *
 * @author Miloslav Metelka
 */
public class EmbeddingOperation {
    
    // -J-Dorg.netbeans.lib.lexer.EmbeddingOperation.level=FINER
    private static final Logger LOG = Logger.getLogger(EmbeddingOperation.class.getName());

    /**
     * Get embedded token list.
     *
     * @param tokenList non-null token list in which the token for which the
     * embedding should be obtained resides.
     * @param index &gt;=0 index of the token in the token list where the
     * embedding should be obtained.
     * @param language whether only language embeddding of the particular
     * language was requested. It may be null if any embedding should be
     * returned.
     * @param initTokensInNew true if tokens should be created when a new ETL
     * gets created. False in case this is called from TokenListList to grab
     * ETLs for sections joining.
     */
    public static <T extends TokenId, ET extends TokenId> EmbeddedTokenList<T,ET> embeddedTokenList(
            TokenList<T> tokenList, int index, Language<?> embeddedLanguage, boolean initTokens)
    {
        TokenOrEmbedding<T> tokenOrEmbedding = tokenList.tokenOrEmbedding(index);
        EmbeddedTokenList<T,?> existingEtl = tokenOrEmbedding.embedding();
        if (existingEtl != null) {
            while (true) {
                // embeddedLanguage != null here
                if (embeddedLanguage == null || embeddedLanguage == existingEtl.language()) {
                    @SuppressWarnings("unchecked")
                    EmbeddedTokenList<T, ET> etlUC = (EmbeddedTokenList<T, ET>) existingEtl;
                    return etlUC;
                }
                EmbeddedTokenList<T, ?> next = existingEtl.nextEmbeddedTokenList();
                if (next == null) {
                    break;
                }
                existingEtl = next;
            }
        }

        // Determine whether there were unsuccessful embedding creation attempts
        // for the given language (or for a default embedding)
        AbstractToken<T> token = tokenOrEmbedding.token();
        if (token.isFlyweight()) {
            return null;
        }

        WrapTokenId<T> wid = token.wid();
        LanguageIds failedEmbeddingLanguageIds = wid.languageIds();
        // Current implementation only uses LanguageIds marking for "null" language
        // to determine whether language embedding creation request was called or not.
        if (failedEmbeddingLanguageIds == LanguageIds.NULL_LANGUAGE_ONLY) {
            // Embedding creation already requested previously but did not produce embedding.
            return null;
        }
//        int embeddedLid = (embeddedLanguage != null)
//                ? LexerApiPackageAccessor.get().languageId(embeddedLanguage)
//                : 0;
//        if (failedEmbeddingLanguageIds.containsId(embeddedLid)) {
//            return null;
//        }

        if (token.getClass() == JoinToken.class) {
            // Currently do not allow to create embedding over token that is physically joined
            return null;
        }

        // If the tokenList is removed then do not create the embedding
        if (tokenList.isRemoved()) {
            return null;
        }

        // Attempt to create the embedding
        EmbeddingPresence ep;
        TokenList<?> rootTokenList = tokenList.rootTokenList();
        TokenHierarchyOperation<?,?> op = rootTokenList.tokenHierarchyOperation();
        LanguagePath languagePath = tokenList.languagePath();
        Language<T> language = tokenList.language();
        LanguageHierarchy<T> languageHierarchy = LexerUtilsConstants.languageHierarchy(language);
        LanguageOperation<T> languageOperation = LexerUtilsConstants.languageOperation(language);
        if (existingEtl != null) {
            ep = null;
        } else { // No embedding was created yet
            // Check embedding presence
            ep = languageOperation.embeddingPresence(token.id());
            if (ep == EmbeddingPresence.NONE) {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        LanguageEmbedding<ET> embedding = (LanguageEmbedding<ET>) LexerUtilsConstants.findEmbedding(
                languageHierarchy, token, languagePath, tokenList.inputAttributes());
        if (embedding != null) {
            // Update the embedding presence ALWAYS_QUERY
            if (ep == EmbeddingPresence.CACHED_FIRST_QUERY) {
                languageOperation.setEmbeddingPresence(token.id(), EmbeddingPresence.ALWAYS_QUERY);
            }

            // Check whether the token contains enough text to satisfy embedding's start and end skip lengths
            if (embeddedLanguage != null && embeddedLanguage != embedding.language()) {
                // Do not add info that embedding failed since default embedding may still be created
                // if e.g. language == null.
                return null;
            }
            
            if (embedding.startSkipLength() + embedding.endSkipLength() > token.length()) {
                // Add info that embedding failed
                addFailedEmbedding(op, token, language, wid, failedEmbeddingLanguageIds, 0); // "null" language
//                addFailedEmbedding(op, token, language, wid, failedEmbeddingLanguageIds, embeddedLid);
                return null;
            }

            LanguagePath embeddedLanguagePath = LanguagePath.get(languagePath,
                    embedding.language());
            EmbeddedTokenList<T,ET> etl = new EmbeddedTokenList<T, ET>(rootTokenList, embeddedLanguagePath, embedding);
            int rootModCount = rootTokenList.modCount();
            if (tokenList instanceof EmbeddedTokenList) {
                ((EmbeddedTokenList)tokenList).updateModCount(rootModCount);
            }
            int tokenStartOffset = tokenList.tokenOffset(index);
            etl.reinit(token, tokenStartOffset, rootModCount);

            if (existingEtl == null) {
                tokenList.setTokenOrEmbedding(index, etl);
            } else {
                existingEtl.setNextEmbeddedTokenList(etl);
            }
            
            if (initTokens) {
                if (embedding.joinSections()) {
                    // Init corresponding TokenListList which should find just created ETL too
                    op.tokenListList(embeddedLanguagePath);
                } else { // sections not joined
                    // Check that there is no TLL in this case.
                    // If there would be one it would already have to run through its constructor
                    // which should have collected all the ETLs already (with initTokensInNew==false)
                    // and init tokens explicitly.
                    // Thus the following assert should always pass.
                    TokenListList<?> tll;
                    assert ((tll = op.existingTokenListList(embeddedLanguagePath)) == null) : "TLL exists for languagePath=" + languagePath + ":\n" + tll;
                    etl.initAllTokens();
                }
            }

            if (LOG.isLoggable(Level.FINE)) {
                StringBuilder sb = new StringBuilder(200);
                sb.append("@@@@@@@@@@ NATURAL-EMBEDDING-CREATED ETL-"); // NOI18N
                LexerUtilsConstants.appendIdentityHashCode(sb, etl);
                sb.append(" ROOT-"); // NOI18N
                LexerUtilsConstants.appendIdentityHashCode(sb, rootTokenList);
                sb.append(" for ").append(embeddedLanguagePath.mimePath()). // NOI18N
                        append(", ").append(embedding).append(": "); // NOI18N
                etl.dumpInfo(sb);
                LOG.fine(sb.toString());
                if (LOG.isLoggable(Level.FINER)) { // Include stack trace of the creation
                    LOG.log(Level.INFO, "Natural embedding created by:", new Exception()); // NOI18N
                }
            }

            return etl;

        } else { // No default embedding
            addFailedEmbedding(op, token, language, wid, failedEmbeddingLanguageIds, 0); // "null" language
//            addFailedEmbedding(op, token, language, wid, failedEmbeddingLanguageIds, embeddedLid);

            // Update embedding presence to NONE
            if (ep == EmbeddingPresence.CACHED_FIRST_QUERY) {
                languageOperation.setEmbeddingPresence(token.id(), EmbeddingPresence.NONE);
            }
        }

        return null;
    }

    private static <T extends TokenId> void addFailedEmbedding(
            TokenHierarchyOperation<?,?> op, AbstractToken<T> token, Language<T> language, WrapTokenId<T> wid,
            LanguageIds failedEmbeddingLanguageIds, int failedEmbeddingLanguageId)
    {
        WrapTokenIdCache<T> cache = op.getWrapTokenIdCache(language);
        failedEmbeddingLanguageIds = LanguageIds.get(failedEmbeddingLanguageIds, failedEmbeddingLanguageId);
        wid = cache.findWid(wid.id(), failedEmbeddingLanguageIds);
        token.setWid(wid);
    }

    /**
     * Create custom embedding.
     *
     * @param tokenList non-null token list in which the token for which the
     * embedding should be created resides.
     * @param index &gt;=0 index of the token in the token list where the
     * embedding should be created.
     * @param embeddedLanguage non-null embedded language.
     * @param startSkipLength &gt;=0 number of characters in an initial part of
     * the token for which the language embedding is being create that should be
     * excluded from the embedded section. The excluded characters will not be
     * lexed and there will be no tokens created for them.
     * @param endSkipLength &gt;=0 number of characters at the end of the token
     * for which the language embedding is defined that should be excluded from
     * the embedded section. The excluded characters will not be lexed and there
     * will be no tokens created for them.
     * @param joinSections whether the embedding joins sections. Currently if
     * this value is false but there is a joining token list list for this
     * embedding then this parameter will be updated to true automatically.
     */
    public static <T extends TokenId, ET extends TokenId> boolean createEmbedding(
            TokenList<T> tokenList, int index, Language<ET> embeddedLanguage,
            int startSkipLength, int endSkipLength, boolean joinSections)
    {
        if (embeddedLanguage == null) {
            throw new IllegalArgumentException("embeddedLanguage parameter cannot be null"); // NOI18N
        }
        TokenList<?> rootTokenList = tokenList.rootTokenList();
        // Only create embedddings for valid operations so not e.g. for removed token list
        
        TokenHierarchyEventInfo eventInfo;
        // Check TL.isRemoved() under syncing of rootTokenList
        if (tokenList.isRemoved()) { // Do not create embedding for removed TLs
            return false;
        }
        // If not removed then THO should be non-null
        TokenHierarchyOperation<?,?> tokenHierarchyOperation = tokenList.tokenHierarchyOperation();
        tokenHierarchyOperation.ensureWriteLocked();

        try {
            // Check presence of token list list for the embedded language path
            // If it exists and it's joining sections then the requested embedding must also join sections
            // even if the joinSections parameter would be false.
            LanguagePath languagePath = tokenList.languagePath();
            LanguagePath embeddedLanguagePath = LanguagePath.get(languagePath, embeddedLanguage);
            TokenOrEmbedding<T> tokenOrEmbedding = tokenList.tokenOrEmbedding(index);
            EmbeddedTokenList<T,?> existingEtl = tokenOrEmbedding.embedding();
            if (existingEtl != null) {
                while (true) {
                    if (embeddedLanguagePath == existingEtl.languagePath()) {
                        return true; // Embedding already exists
                    }
                    EmbeddedTokenList<T,?> next = existingEtl.nextEmbeddedTokenList();
                    if (next == null) {
                        break;
                    }
                    existingEtl = next;
                }
            }

            TokenListList<ET> tll = tokenHierarchyOperation.existingTokenListList(embeddedLanguagePath);
            if (tll != null && tll.joinSections()) {
                joinSections = true;
            }
            // Add the new embedding as the first one in the single-linked list
            LanguageEmbedding<ET> embedding = LanguageEmbedding.create(embeddedLanguage,
                    startSkipLength, endSkipLength, joinSections);
            tokenHierarchyOperation.addLanguagePath(embeddedLanguagePath);
            // Make the embedded token list to be the first in the list

            AbstractToken<T> token = tokenOrEmbedding.token();
            if (startSkipLength + endSkipLength > token.length()) { // Check for appropriate size
                return false;
            }
            if (token.isFlyweight()) { // embedding cannot exist for this flyweight token
                return false;
            }

            EmbeddedTokenList<T,ET> etl = new EmbeddedTokenList<T, ET>(rootTokenList, embeddedLanguagePath, embedding);
            int rootModCount = rootTokenList.modCount();
            if (tokenList instanceof EmbeddedTokenList) {
                ((EmbeddedTokenList)tokenList).updateModCount(rootModCount);
            }
            int tokenStartOffset = tokenList.tokenOffset(index);
            etl.reinit(token, tokenStartOffset, rootModCount);

            if (existingEtl == null) {
                tokenList.setTokenOrEmbedding(index, etl);
            } else {
                existingEtl.setNextEmbeddedTokenList(etl);
            }

            // Fire the embedding creation to the clients
            // Threading model may need to be changed if necessary
            eventInfo = new TokenHierarchyEventInfo(
                    tokenHierarchyOperation,
                    TokenHierarchyEventType.EMBEDDING_CREATED,
                    tokenStartOffset, 0, "", 0);
            eventInfo.setMaxAffectedEndOffset(tokenStartOffset + token.length());

            if (tll != null) {
                // Update tll by embedding creation
                // etl.initAllTokens() will be called by TokenListListUpdate.collectAddedEmbeddings()
                new TokenHierarchyUpdate(eventInfo).updateCreateOrRemoveEmbedding(etl, true);
            } else { // tll == null
                if (joinSections) {
                    // Force token list list creation only when joining sections
                    tll = tokenHierarchyOperation.tokenListList(embeddedLanguagePath);
                } else {
                    // Force initialization of the tokens of the ETL
                    etl.initAllTokens();
                }
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("@@@@@@@@@@ EXPLICIT-EMBEDDING-CREATED for " + embeddedLanguagePath.mimePath()
                        + ", " + embedding + ": " + etl.dumpInfo(new StringBuilder(256)) + '\n');
                if (LOG.isLoggable(Level.FINER)) { // Include stack trace of the creation
                    LOG.log(Level.INFO, "Explicit embedding created by:", new Exception());
                }
    //                tokenHierarchyOperation.ensureConsistency();
            }

            // Construct outer token change info
            TokenChangeInfo<T> info = new TokenChangeInfo<T>(tokenList);
            info.setIndex(index);
            info.setOffset(tokenStartOffset);
            //info.setAddedTokenCount(0);
            eventInfo.setTokenChangeInfo(info);

            TokenChangeInfo<ET> embeddedInfo = new TokenChangeInfo<ET>(etl);
            embeddedInfo.setIndex(0);
            embeddedInfo.setOffset(tokenStartOffset + embedding.startSkipLength());
            // Should set number of added tokens directly?
            //  - would prevent further lazy embedded lexing so leave to zero for now
            //info.setAddedTokenCount(0);
            info.addEmbeddedChange(embeddedInfo);

            // Fire the change
            tokenHierarchyOperation.fireTokenHierarchyChanged(eventInfo);
            return true;
        } catch (RuntimeException e) {
            // Log the exception and attempt to recover by recreating the token hierarchy
            throw tokenHierarchyOperation.recreateAfterError(e);
        }
    }

    public static <T extends TokenId, ET extends TokenId> boolean removeEmbedding(
            TokenList<T> tokenList, int index, Language<ET> embeddedLanguage) {
        TokenList<?> rootTokenList = tokenList.rootTokenList();
        // Only create embedddings for valid operations so not e.g. for removed token list
        // Check TL.isRemoved() under syncing of rootTokenList
        if (tokenList.isRemoved()) { // Do not remove embedding for removed TLs
            return false;
        }
        // If TL is not removed then THO should be non-null
        TokenHierarchyOperation<?, ?> tokenHierarchyOperation = tokenList.tokenHierarchyOperation();
        tokenHierarchyOperation.ensureWriteLocked();

        try {
            TokenOrEmbedding<T> tokenOrEmbedding = tokenList.tokenOrEmbedding(index);
            @SuppressWarnings("unchecked")
            EmbeddedTokenList<T,?> etl = tokenOrEmbedding.embedding();
            if (etl != null) {
                int rootModCount = rootTokenList.modCount();
                EmbeddedTokenList<T,?> lastEtl = null;
                while (etl != null) {
                    etl.updateModCount(rootModCount);
                    if (embeddedLanguage == etl.language()) {
                        // The embedding with the given language exists
                        // Remove it from the chain
                        EmbeddedTokenList<T, ?> next = etl.nextEmbeddedTokenList();
                        if (lastEtl == null) { // etl is first in list
                            if (next == null) {
                                tokenList.setTokenOrEmbedding(index, etl.token());
                            } else {
                                tokenList.setTokenOrEmbedding(index, next);
                            }
                        } else { // etl in middle of chain
                            lastEtl.setNextEmbeddedTokenList(next);
                        }
                        etl.setNextEmbeddedTokenList(null);
                        // Do not increase the version of the hierarchy since
                        // all the existing token sequences would be invalidated.
                        // Instead invalidate only TSes for the etl only and all its children.
                        // State that the removed embedding was not default - should not matter anyway
                        etl.markRemoved();
                        etl.markChildrenRemovedDeep();

                        // Fire the embedding creation to the clients
                        int startOffset = etl.branchTokenStartOffset();
                        TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                                tokenHierarchyOperation,
                                TokenHierarchyEventType.EMBEDDING_REMOVED,
                                startOffset, 0, "", 0);
                        eventInfo.setMaxAffectedEndOffset(startOffset + etl.token().length());
                        // Construct outer token change info
                        TokenChangeInfo<T> info = new TokenChangeInfo<T>(tokenList);
                        info.setIndex(index);
                        info.setOffset(startOffset);
                        //info.setAddedTokenCount(0);
                        eventInfo.setTokenChangeInfo(info);

                        @SuppressWarnings("unchecked")
                        EmbeddedTokenList<T,ET> etlET = (EmbeddedTokenList<T,ET>) etl;
                        TokenChangeInfo<ET> embeddedInfo = new TokenChangeInfo<ET>(etlET);
                        embeddedInfo.setIndex(0);
                        embeddedInfo.setOffset(startOffset + etl.languageEmbedding().startSkipLength());
                        // For now do not set the removed contents (requires RemovedTokenList)
                        info.addEmbeddedChange(embeddedInfo);

                        // Check for presence of etl in a token list list
                        TokenListList tll = tokenHierarchyOperation.existingTokenListList(etl.languagePath());
                        if (tll != null) {
                            // update-status already called
                            new TokenHierarchyUpdate(eventInfo).updateCreateOrRemoveEmbedding(etl, false);
                        }

                        // Fire the change
                        tokenHierarchyOperation.fireTokenHierarchyChanged(eventInfo);
                        return true;
                    }
                    etl = etl.nextEmbeddedTokenList();
                }
            }
            return false;
        } catch (RuntimeException e) {
            // Log the exception and attempt to recover by recreating the token hierarchy
            throw tokenHierarchyOperation.recreateAfterError(e);
        }

    }

}
