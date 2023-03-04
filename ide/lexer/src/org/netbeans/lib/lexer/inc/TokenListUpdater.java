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

package org.netbeans.lib.lexer.inc;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.JoinLexerInputOperation;
import org.netbeans.lib.lexer.JoinTokenList;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.JoinToken;
import org.netbeans.lib.lexer.token.PartToken;


/**
 * Token list updater fixes a list of tokens constructed for a document
 * after text of the document gets modified.
 * <br>
 * Subclasses need to define all the abstract methods
 * so that the updating method can work on real token sequences.
 *
 * <p>
 * Updater looks similar to list iterator
 * but there are differences in the semantics
 * of iterator's modification operations.
 * <br/>
 * The algorithm used in the {@link #update(int, int)}
 * is based on "General Incremental Lexical Analysis" written
 * by Tim A. Wagner and Susan L. Graham, University
 * of California, Berkeley. It's available online
 * at <a href="http://www.cs.berkeley.edu/Research/Projects/harmonia/papers/twagner-lexing.pdf">
 * twagner-lexing.pdf</a>.
 * <br/>
 * Ending <code>EOF</code> token is not used but the lookahead
 * of the ending token(s) is increased by one (past the end of the input)
 * if they have reached the EOF.
 * <br/>
 * Non-startable tokens are not supported.
 * <br/>
 * When updating a token with lookback one as a result
 * of modification the lookahead of the preceding token is inspected
 * to find out whether the modification has really affected it.
 * This can often save the previous token from being relexed.
 * <br/>
 * Currently the algorithm computes the lookback values on the fly
 * and it does not store the lookback in the tokens. For typical languages
 * the lookback is reasonably small (0, 1 or 2) so it's usually not worth
 * to consume extra space in token instances for storing of the lookback.
 * There would also be an additional overhead of updating the lookback
 * values in the tokens after the modification and the algorithm code would
 * be somewhat less readable.
 * </p>
 *
 * <p>
 * The algorithm removes the affected tokens in the natural order as they
 * follow in the token stream. That can be used when the removed tokens
 * need to be collected (e.g. in an array).
 * <br/>
 * If the offset and state after token recognition matches
 * the end offset and state after recognition of the originally present
 * token then the relexing is stopped because a match was found and the newly
 * produced tokens would match the present ones.
 * <br/>
 * Otherwise the token(s) in the list are removed and replaced
 * by the relexed token and the relexing continues until a match is reached.
 * </p>
 * 
 * <p>
 * When using token list updater with JoinTokenList.Mutable there is a special treatment
 * of offsets independent of the underlying JoinTokenListChange and LexerInputOperation.
 * The updater treats the modOffset to be relative (in the number of characters)
 * to the relexOffset point (which is a real first relexed token's offset; it's necessary
 * for restarting of the lexer input operation) so when going over a JoinToken
 * the modOffset must be recomputed to not contain the gaps between individual join token parts.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenListUpdater {

    // -J-Dorg.netbeans.lib.lexer.inc.TokenListUpdater.level=FINE
    private static final Logger LOG = Logger.getLogger(TokenListUpdater.class.getName());

    /**
     * Use incremental algorithm to update a regular list of tokens (IncTokenList or EmbeddedTokenList)
     * after a modification done in the underlying storage.
     * 
     * @param change non-null change that will incorporate the performed chagnes.
     * @param eventInfo non-null info about modification offset and inserted and removed length.
     */
    static <T extends TokenId> void updateRegular(TokenListChange<T> change, TokenHierarchyEventInfo eventInfo) {
        MutableTokenList<T> tokenList = change.tokenList();
        int tokenCount = tokenList.tokenCountCurrent();
        boolean loggable = LOG.isLoggable(Level.FINE);
        if (loggable) {
            logModification(tokenList, eventInfo, false);
        }
        
        // Find modified token by binary search in existing tokens
        // Use LexerUtilsConstants.tokenIndexBinSearch() to NOT lazily create new tokens here
        int[] indexAndTokenOffset = LexerUtilsConstants.tokenIndexBinSearch(tokenList, eventInfo.modOffset(), tokenCount);
        // Index and offset from which the relexing will start
        int relexIndex = indexAndTokenOffset[0];
        // relexOffset points to begining of a token in which the modification occurred
        // or which is affected by a modification (its lookahead points beyond modification point).
        int relexOffset = indexAndTokenOffset[1];
        if (relexIndex == -1) { // No tokens at all
            relexIndex = 0;
            relexOffset = tokenList.startOffset();
        }

        // Index of token before which the relexing will end (or == tokenCount)
        int matchIndex = relexIndex;
        // Offset of token at matchIndex
        int matchOffset = relexOffset;

        if (relexIndex == tokenCount) { // Change right at end of last token or beyond it (if not fully lexed)
            // relexOffset set to end offset of the last token
            if (!tokenList.isFullyLexed() && eventInfo.modOffset() >= relexOffset +
                    ((relexIndex > 0) ? tokenList.lookahead(relexIndex - 1) : 0)
            ) { // Do nothing if beyond last token's lookahed
                // Check whether the last token could be affected at all
                // by checking whether the modification was performed
                // in the last token's lookahead.
                // For fully lexed inputs the characters added to the end
                // must be properly lexed and notified (even if the last present
                // token has zero lookahead).
                if (loggable) {
                    LOG.log(Level.FINE, "UPDATE-REGULAR FINISHED: Not fully lexed yet. rOff=" +
                            relexOffset + ", modOff=" + eventInfo.modOffset() + "\n");
                }
                change.setIndex(relexIndex);
                change.setOffset(relexOffset);
                change.setMatchIndex(matchIndex); // matchIndex == relexIndex
                change.setMatchOffset(matchOffset); // matchOffset == relexOffset
                tokenList.replaceTokens(change, eventInfo, true);
                return; // not affected at all
            } // change.setIndex() will be performed later in relex()

            // Leave matchOffset as is (will possibly end relexing at tokenCount and unfinished relexing
            // will be continued by replaceTokens()).
            // For fully lexed lists it is necessary to lex till the end of input.
            if (tokenList.isFullyLexed())
                matchOffset = Integer.MAX_VALUE;

        } else { // relexIndex < tokenCount
            // Possibly increase matchIndex and matchOffset by skipping the tokens in the removed area
            if (eventInfo.removedLength() > 0) { // At least remove token at relexOffset
                matchOffset += tokenList.tokenOrEmbeddingDirect(matchIndex++).token().length();
                int removedEndOffset = eventInfo.modOffset() + eventInfo.removedLength();
                while (matchOffset < removedEndOffset && matchIndex < tokenCount) {
                    matchOffset += tokenList.tokenOrEmbeddingDirect(matchIndex++).token().length();
                }
            } else { // For inside-token inserts match on the next token
                if (matchOffset < eventInfo.modOffset()) { // (matchOffset == mod-token-start-offset)
                    matchOffset += tokenList.tokenOrEmbeddingDirect(matchIndex++).token().length();
                }
            }
            // Update the matchOffset so that it corresponds to the state
            // after the modification
            matchOffset += eventInfo.diffLength();
        }

        // Check whether modification affected previous token
        while (relexIndex > 0 && relexOffset + tokenList.lookahead(relexIndex - 1) > eventInfo.modOffset()) {
            relexIndex--;
            if (loggable) {
                LOG.log(Level.FINE, "    Token at reInd=" + relexIndex + " affected (la=" + // NOI18N
                        tokenList.lookahead(relexIndex) + ") => relex it\n"); // NOI18N
            }
            AbstractToken<T> token = tokenList.tokenOrEmbeddingDirect(relexIndex).token();
            relexOffset -= token.length();
        }
        
        // Check whether actual relexing is necessary
        // State from which the lexer can be started
        Object relexState = (relexIndex > 0) ? tokenList.state(relexIndex - 1) : null;
        change.setIndex(relexIndex);
        change.setOffset(relexOffset);
        change.setMatchIndex(matchIndex);
        change.setMatchOffset(matchOffset);
        
        // Check whether relexing is necessary.
        // Necessary condition for no-relexing is a removal at token's boundary
        // and the token right before modOffset must have zero lookahead (if lookahead would be >0 
        // then the token would be affected) and the states before relexIndex must equal
        // to the state before matchIndex.
        boolean relex = (relexOffset != matchOffset)
                || (eventInfo.insertedLength() > 0)
                || (matchIndex == 0) // ensure the tokenList.state(matchIndex - 1) will not fail with IOOBE
                || !LexerUtilsConstants.statesEqual(relexState, tokenList.state(matchIndex - 1));

        // There is an extra condition that the lookahead of the matchToken
        // must not span the next (retained) token. This condition helps to ensure
        // that the lookaheads will be the same like during regular batch lexing.
        // As the empty tokens are not allowed the situation may only occur
        // for lookahead > 1.
        int lookahead;
        if (!relex && (lookahead = tokenList.lookahead(matchIndex - 1)) > 1 && matchIndex < tokenCount) {
            // Check whether lookahead of the token before match point exceeds the whole token right after match point
            relex = (lookahead > tokenList.tokenOrEmbeddingDirect(matchIndex).token().length()); // check next token
        }

        if (loggable) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("  BEFORE-RELEX:\n");
            sb.append("  relex=").append(relex);
            sb.append(", reInd=").append(relexIndex).append(", reOff=").append(relexOffset);
            sb.append(", reSta=").append(relexState).append('\n');
            sb.append("  maInd=").append(matchIndex).append(", maOff=").append(matchOffset);
//            sb.append(", tokenList-part:\n");
//            LexerUtilsConstants.appendTokenList(sb, tokenList, matchIndex, matchIndex - 3, matchIndex + 3, false, 4, false);
            sb.append('\n');
            LOG.log(Level.FINE, sb.toString());
        }

        assert (relexIndex >= 0);
        if (relex) {
            // Create lexer input operation for the given token list
            if (relexOffset < 0) { // Invalid value
                // Log modification unconditionally due to #144258
                logModification(tokenList, eventInfo, false);
                LOG.info("relexIndex=" + relexIndex + ", relexOffset=" + relexOffset +
                        ", relexState=" + relexState + ", indexAndTokenOffset: [" +
                        indexAndTokenOffset[0] + ", " + indexAndTokenOffset[1] + "]\n"
                        );
                LOG.info("\n\n" + eventInfo.modificationDescription(true) + "\n");

            }
            LexerInputOperation<T> lexerInputOperation
                    = tokenList.createLexerInputOperation(relexIndex, relexOffset, relexState);
            relex(change, lexerInputOperation, tokenCount);
        }

        tokenList.replaceTokens(change, eventInfo, true);
        if (loggable) {
            LOG.log(Level.FINE, "\nTLChange: " + change + "\nMods:" + change.toStringMods(4) + // NOI18N
                    "UPDATE-REGULAR FINISHED\n"); // NOI18N
        }
    }


    /**
     * Use incremental algorithm to update a JoinTokenList after a modification done in the underlying storage.
     * <br>
     * The assumption is that there may only be two states:
     * <ul>
     *   <li> There is a local input source modification bounded to a particular ETL.
     *        In such case there should be NO token lists removed/added.
     *   </li>
     *   <li> The modification spans multiple ETLs and all the affected ETLs will be removed
     *        and possibly new ones inserted.
     *        The modification is "bounded" by the removed ETLs i.e.
     *            modOffset &gt;= first-removed-ETL.startOffset()
     *        and modOffset + removedLength &lt;= last-removed-ETL.endOffset()
     *   </li>
     * </ul>
     * 
     * @param change non-null change that will incorporate the performed chagnes.
     * @param eventInfo non-null info about modification offset and inserted and removed length.
     */
    static <T extends TokenId> void updateJoined(JoinTokenListChange<T> change, TokenHierarchyEventInfo eventInfo) {
        JoinTokenList<T> jtl = (JoinTokenList<T>) change.tokenList();
        TokenListListUpdate<T> tokenListListUpdate = change.tokenListListUpdate();
        int tokenCount = jtl.tokenCountCurrent();
        boolean loggable = LOG.isLoggable(Level.FINE);
        if (loggable) {
            logModification(jtl, eventInfo, true);
        }
        
        // First determine what area is affected by removed/added ETLs
        int relexIndex;
        int relexLocalIndex;
        int relexTokenListIndex;
        int relexOffset;
        int matchIndex;
        int matchOffset;
        int modOffset = eventInfo.modOffset();
        // Relative distance of mod point against begining of the modified token.
        // It is used to determine whether previous token's lookahead reaches it
        // and thus must be relexed too.
        int relModOffset;
        boolean checkPrevTokenListJoined;
        boolean relex = true;
        int modTokenListIndex = tokenListListUpdate.modTokenListIndex;
        // Find values for relex-variables and relModOffset - need to traverse manually.
        if (tokenListListUpdate.isTokenListsMod()) {
            // relModOffset must be assigned too for token list modification since
            // token list mod may affect preceding tokens reaching the mod point with their lookaheads.
            relModOffset = 0; // may be updated later
            if (modTokenListIndex < jtl.tokenListCount()) {
                jtl.setActiveTokenListIndex(modTokenListIndex);
                relexIndex = jtl.activeStartJoinIndex();
            } else {
                relexIndex = jtl.tokenCountCurrent();
            }
            relexLocalIndex = 0;
            relexTokenListIndex = modTokenListIndex;
            checkPrevTokenListJoined = true;
            // Set matchIndex and matchOffset
            int modEndTokenListIndex = modTokenListIndex + tokenListListUpdate.removedTokenListCount;
            boolean lastRemovedJoined = false;
            if (modEndTokenListIndex > 0) {
                jtl.setActiveTokenListIndex(modEndTokenListIndex - 1);
                matchIndex = jtl.activeEndJoinIndex();
                matchOffset = jtl.activeTokenList().endOffset();
                // matchOffset must be updated by subtraction of the removed text length.
                // Otherwise the following case fails (see JoinRandomTest):
                // "tt<g[h>ab<i]jk>[y]<{>x}[]o[<[x]<[><]>" and remove ">[y]<"
                // The non-updated matchOffset would be 18 (orig-end of "[y]")
                // but the relexed join-token would end at 18 too (in the new-offsets).
                // The new join token would include "x}" as last part and "x}" token would be duplicated
                // first token in the ETL that contains it.
                // BTW it's possible that matchOffset < modOffset e.g. for addition or even in case of removal
                // when the mod would require relex of an embedding before the modification point.
                if (matchOffset > modOffset) {
                    matchOffset = Math.max(matchOffset - eventInfo.removedLength(), modOffset);
                }
                if (jtl.activeTokenList().joinInfo().joinTokenLastPartShift() > 0) {
                    // Note: JoinToken.endOffset() calls EC.updateStatus() otherwise there could be obsolete start-offset.
                    matchOffset = ((JoinToken<T>) jtl.tokenOrEmbeddingDirect(matchIndex).token()).endOffset();
                    matchIndex++;
                    lastRemovedJoined = true;
                }
            } else { // modEndTokenListIndex == 0
                matchIndex = 0;
                matchOffset = -1; // JoinTokenListChange.increaseMatchIndex() will overwrite
            }
            if (!lastRemovedJoined) {
                if (tokenListListUpdate.addedTokenListCount() > 0) { // Use last added's end
                    matchOffset = tokenListListUpdate.afterUpdateTokenList(jtl, modTokenListIndex +
                            tokenListListUpdate.addedTokenListCount() - 1).endOffset();
                }
            }
            // Note jtl.activeTokenListIndex() may be > modTokenListIndex for join token (located to JT's last part)
            int afterUpdateTokenListCount = tokenListListUpdate.afterUpdateTokenListCount(jtl);
            // Check whether the mod point is preceded by join token
            if (relexTokenListIndex < afterUpdateTokenListCount) {
                relexOffset = tokenListListUpdate.afterUpdateTokenList(jtl, modTokenListIndex).startOffset();
            } else {
                relex = false;
                relexOffset = Integer.MAX_VALUE; // No relexing expected unless checkPrevTokenListJoined finds valid area
            }
            if (matchOffset < relexOffset && relexTokenListIndex > 0) {
                relex = false;
            }
            
        } else { // Not token list mod
            jtl.setActiveTokenListIndex(modTokenListIndex); // Index of ETL where a change occurred.
            EmbeddedTokenList<?,T> modEtl = jtl.activeTokenList();
            assert ((eventInfo.insertedLength() > 0) || (eventInfo.removedLength() > 0)) : "No modification"; // NOI18N
            assert (modOffset >= modEtl.startOffset()) :
                "modOffset=" + modOffset + " < etlStartOffset=" + modEtl.startOffset(); // NOI18N
            assert (modOffset + eventInfo.diffLengthOrZero() <= modEtl.endOffset()) :
                "modOffset=" + modOffset + " + diffLength=" + eventInfo.diffLength() + // NOI18N
                " > etlEndOffset=" + modEtl.endOffset();
            // Mark that the list has character modification inside
            change.charModTokenList = modEtl;
            // Search within releEtl only - can use binary search safely (unlike on JTL with removed ETLs)
            int[] indexAndTokenOffset = modEtl.tokenIndex(modOffset);
            relexTokenListIndex = modTokenListIndex;
            relexLocalIndex = indexAndTokenOffset[0];
            relexOffset = indexAndTokenOffset[1];
            if (relexLocalIndex == -1) {
                relexLocalIndex = 0;
                relexOffset = modEtl.startOffset();
            }
            relModOffset = modOffset - relexOffset;
            relexIndex = jtl.activeStartJoinIndex() + relexLocalIndex;
            matchIndex = relexIndex;
            matchOffset = relexOffset;
            int matchLocalIndex = relexLocalIndex;
            checkPrevTokenListJoined = false;
            // Possibly increase matchIndex and matchOffset by skipping the tokens in the removed area
            // Search locally in modEtl - do not use jtl operations yet.
            int modEtlTokenCount = modEtl.tokenCountCurrent();
            if (eventInfo.removedLength() > 0) { // At least remove token at relexOffset
                matchIndex++;
                matchOffset += modEtl.tokenOrEmbeddingDirect(matchLocalIndex++).token().length();
                int removedEndOffset = eventInfo.modOffset() + eventInfo.removedLength();
                // XXX review '&& (matchLocalIndex < modEtl.tokenCount())'
                while (matchOffset < removedEndOffset && (matchLocalIndex < modEtlTokenCount)) {
                    matchIndex++;
                    matchOffset += modEtl.tokenOrEmbeddingDirect(matchLocalIndex++).token().length();
                }
            } else { // For inside-token inserts match on the next token
                if (matchOffset < modOffset) { // (matchOffset == mod-token-start-offset)
                    matchIndex++;
                    matchOffset += modEtl.tokenOrEmbeddingDirect(matchLocalIndex++).token().length();
                }
            }
            // Update the matchOffset so that it corresponds to the state
            // after the modification
            matchOffset += eventInfo.diffLength();
            // Now the following cases must be checked:
            // 1. relexIndex == 0 => check joining from prev token list and adjust to join-token start
            // 2. relexIndex == modEtlTokenCount (for insert-only at ETL's end)
            //      => check if token at (modEtlTokenCount-1) is not joined and relex from its start
            // 3. matchIndex == 0 => check joining from prev token list and adjust to join-token's end
            // 4. matchIndex == modEtlTokenCount (for insert-only at ETL's end)
            //      => check if token at (modEtlTokenCount-1) is not joined and adjust to its end
            // Note that modEtlTokenCount may also be 1 or even 0 (for insert-only).
            // Note that last token may be part of a join token and so if relex/match local index
            // points to modEtlTokenCount it may need to be corrected accordingly.
            assert (relexLocalIndex <= matchLocalIndex);
            assert (matchLocalIndex <= modEtlTokenCount);
            if (matchLocalIndex == modEtlTokenCount) {
                if (modEtl.joinInfo().joinTokenLastPartShift() > 0) {
                    matchIndex = jtl.activeEndJoinIndex() + 1; // above join token
                    // Get join token but do not move active ETL
                    JoinToken<T> joinToken = ((JoinToken<T>) jtl.tokenOrEmbeddingDirect(matchIndex - 1).token()); 
                    jtl.setActiveTokenListIndex(modTokenListIndex);
                    matchOffset = joinToken.endOffset();
                    // Note matchLocalIndex not updated
                }
            }

            if (relexLocalIndex == 0) {
                checkPrevTokenListJoined = true;
            }
            if (relexLocalIndex == matchLocalIndex) {
                // Special case when inserting right at end boundary of a last token with zero lookahead.
                // In that case relexLocalIndex == modEtlTokenCount.
                // Alternatively modEtl may be empty (no tokens at all).
                if (relexLocalIndex == modEtlTokenCount) {
                    //   Token is last in an ETL => ensure the relexing starts at end of modEtl
                    //     where the textual modification happened (instead of the begining of the next ETL).
                    //   Example:
                    //       doc:"{x}<a>y" and insert(3,"u") becomes "{x}u<a>y"
                    //   Then the relexIndex would be 1 and relexOffset would normally point to "y"
                    //     but it has to point to the inserted 'u' instead.
                    if (modEtl.joinInfo().joinTokenLastPartShift() > 0) { // Adjust to last token's start
                        // Or there is a possibility that the modEtl has no tokens at all
                        assert (eventInfo.removedLength() == 0) : "Insert only expected";
                        if (modEtlTokenCount > 0) { // At least one token
                            assert (relexIndex == jtl.activeEndJoinIndex() + 1);
                            relexIndex--;
                            relexLocalIndex--;
                            relexOffset = modEtl.tokenOffset(relexLocalIndex);
                        }
                        if (relexLocalIndex == 0) { // Only a single token in ETL - may be joined by prev
                            checkPrevTokenListJoined = true;
                        }
                    }
                }
            }
        }
        
        // Check whether the ETL preceding relexTokenListIndex is part (or start) of join token.
        // If so the relexTokenListIndex, relexOffset and relexLocalIndex must be updated.
        // Also relModOffset must be set properly.
        // Also matchIndex and matchOffset are updated in case matchIndex == relexIndex.
        if (checkPrevTokenListJoined && relexTokenListIndex > 0) {
            jtl.setActiveTokenListIndex(relexTokenListIndex - 1);
            while (true) { // Must cycle in order to assign relModOffset properly.
                int lps = jtl.activeTokenList().joinInfo().joinTokenLastPartShift();
                if (lps > 0) {
                    if (jtl.activeTokenList().tokenCountCurrent() > 0) {
                        relexLocalIndex = jtl.activeTokenList().tokenCountCurrent() - 1;
                        relexTokenListIndex = jtl.activeTokenListIndex();
                        // relexIndex stays the same
                        PartToken<T> partToken = (PartToken<T>) jtl.activeTokenList().tokenOrEmbeddingDirect(
                                relexLocalIndex).token();
                        relModOffset = partToken.partTextEndOffset();
                        relexOffset = partToken.joinToken().offset(null);
                        // Locate to begining of join token
                        if (partToken.partTokenIndex() != 0) { // Must relex from join token's begining
                            // Notice that the following will change jtl's activeTokenListIndex
                            relexLocalIndex = jtl.tokenStartLocalIndex(relexIndex);
                            relexTokenListIndex = jtl.activeTokenListIndex();
                        }
                        // If relexing was not expected now there's text to relex
                        relex = true;
                        if (relexIndex == matchIndex) { // Fix matchIndex to join-token's end
                            matchIndex++;
                            matchOffset = partToken.joinToken().endOffset();
                            // Recompute lps since it may be obsolete after possible activeTokenListIndex change
                            lps = jtl.activeTokenList().joinInfo().joinTokenLastPartShift();
                            // If the last part is located inside modEtl then it must be adjusted
                            if (jtl.activeTokenListIndex() + lps == modTokenListIndex) {
                                matchOffset += eventInfo.diffLength();
                            }
                        }
                        break;
                    } // otherwise relModOffset stays zero
                } else { // Not a joined token
                    break;
                }
                // Goto previous ETL
                if (jtl.activeTokenListIndex() > 0) {
                    jtl.setPrevActiveTokenListIndex();
                } else {
                    break;
                }
            }
        }

        // Update relex-vars according to lookahead of tokens before relexIndex
        int origRelexIndex = relexIndex;
        while (relexIndex > 0 && jtl.lookahead(relexIndex - 1) > relModOffset) {
            AbstractToken<T> relexToken = jtl.tokenOrEmbeddingDirect(--relexIndex).token();
            relModOffset += relexToken.length(); // User regular token.length() here
            if (loggable) {
                LOG.log(Level.FINE, "    Token at reInd=" + relexIndex + " affected (la=" + // NOI18N
                        jtl.lookahead(relexIndex) + ") => relex it\n"); // NOI18N
            }
        }
        if (relexIndex != origRelexIndex) { // relexIndex can only be <= origRelexIndex
            relexOffset = jtl.tokenOffset(relexIndex);
            relexLocalIndex = jtl.tokenStartLocalIndex(relexIndex);
            relexTokenListIndex = jtl.activeTokenListIndex();
            relex = true;
        }
        change.setMatchIndex(matchIndex);
        change.setMatchOffset(matchOffset);

        // Create lexer input operation now since JTL should be positioned before removed ETLs
        // and JLIO needs to scan tokens backwards for fly sequence length.
        Object relexState = (relexIndex > 0) ? jtl.state(relexIndex - 1) : null;
        JoinLexerInputOperation<T> lexerInputOperation = null;
        if (loggable) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("  BEFORE-RELEX:\n");
            sb.append("  relex=").append(relex);
            sb.append(", reInd=").append(relexIndex).append(", reOff=").append(relexOffset);
            sb.append(", reSta=").append(relexState).append('\n');
            sb.append(", maInd=").append(matchIndex).append(", maOff=").append(matchOffset);
//            sb.append(", tokenList-part:\n");
//            LexerUtilsConstants.appendTokenList(sb, tokenList, matchIndex, matchIndex - 3, matchIndex + 3, false, 4, false);
            sb.append('\n');
            LOG.log(Level.FINE, sb.toString());
        }
        change.setIndex(relexIndex);
        change.setOffset(relexOffset);
        if (relex) {
            lexerInputOperation = new MutableJoinLexerInputOperation<T>(
                    jtl, relexIndex, relexState, relexTokenListIndex, relexOffset, tokenListListUpdate);
            lexerInputOperation.init();
            change.setStartInfo(lexerInputOperation, relexLocalIndex);
            // setMatchIndex() and setMatchOffset() called later below
            relex(change, lexerInputOperation, tokenCount);

        } else { // No relexing
            // Only possibly replace token lists and stop
            change.setNoRelexStartInfo();
        }
        
        jtl.replaceTokens(change, eventInfo, true);
        if (loggable) {
            LOG.log(Level.FINE, "\nTLChange:" + change + "\nMods:" + change.toStringMods(4) + // NOI18N
                    "UPDATE-JOINED FINISHED\n"); // NOI18N
        }
    }


    /**
     * Relex part of input to create new tokens. This method may sometimes be skipped e.g. for removal of chars
     * corresponding to a single token preceded by a token with zero lookahead.
     * <br/>
     * This code is common for both updateRegular() and updateJoined().
     * 
     * @param change non-null token list change.
     * @param lexerInputOperation non-null lexer input operation by which the new tokens
     *  will be produced.
     * @param change non-null token list change into which the created tokens are being added.
     * @param tokenCount current token count in tokenList.
     */
    private static <T extends TokenId> void relex(TokenListChange<T> change,
            LexerInputOperation<T> lexerInputOperation, int tokenCount
    ) {
        boolean loggable = LOG.isLoggable(Level.FINE);
        MutableTokenList<T> tokenList = change.tokenList();
        // Remember the match index below which the comparison of extra relexed tokens
        // (matching the original ones) cannot go.
        int lowestMatchIndex = change.matchIndex;

        AbstractToken<T> token;
        int relexOffset;
        while ((token = lexerInputOperation.nextToken()) != null) {
            // Get lookahead and state; Will certainly use them both since updater runs for inc token lists only
            int lookahead = lexerInputOperation.lookahead();
            Object state = lexerInputOperation.lexerState();
            if (loggable) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("    LEXED-TOKEN: ");
                int tokenEndOffset = lexerInputOperation.lastTokenEndOffset();
                CharSequence inputSourceText = tokenList.inputSourceText();
                if (tokenEndOffset > inputSourceText.length()) {
                    sb.append(tokenEndOffset).append("!! => ");
                    tokenEndOffset = inputSourceText.length();
                    sb.append(tokenEndOffset);
                }
                sb.append('"');
                token.dumpText(sb, inputSourceText);
                sb.append('"');
                token.dumpInfo(sb, null, false, false, 0);
                sb.append("\n");
                LOG.log(Level.FINE, sb.toString());
            }

            change.addToken(token, lookahead, state);
            // Here add regular token length even for JoinToken instances
            // since this is used solely for comparing with matchOffset which
            // also uses the per-input-chars coordinates. Real token's offset is independent value
            // assigned by the underlying TokenListChange and LexerInputOperation.
            relexOffset = lexerInputOperation.lastTokenEndOffset();
            // Marks all original tokens that would cover the area of just lexed token as removed.
            // 'matchIndex' will point right above the last token that was removed
            // 'matchOffset' will point to the end of the last removed token
            if (relexOffset > change.matchOffset) {
                do { // Mark all tokens below
                    if (change.matchIndex == tokenCount) { // index == tokenCount
                        if (tokenList.isFullyLexed()) {
                            change.matchOffset = Integer.MAX_VALUE; // Force lexing till end of input
                        } else { // Not fully lexed -> stop now
                            // Fake the conditions to break the relexing loop
                            change.matchOffset = relexOffset;
                            state = tokenList.state(change.matchIndex - 1);
                        }
                        break;
                    }
                    // Skip the token at matchIndex and also increase matchOffset
                    // The default (increasing matchOffset by token.length()) is overriden for join token list.
                    change.increaseMatchIndex();
                } while (relexOffset > change.matchOffset);
            }

            // Check whether the new token ends at matchOffset with the same state
            // like the original which typically means end of relexing
            if (relexOffset == change.matchOffset
                && LexerUtilsConstants.statesEqual(state, 
                    (change.matchIndex > 0) ? tokenList.state(change.matchIndex - 1) : null)
            ) {
                // Here it's a potential match and the relexing could end.
                // However there are additional SAME-LOOKAHEAD requirements
                // that are checked here and if not satisfied the relexing will continue.
                // SimpleLexerRandomTest.test() contains detailed description.
                
                // If there are no more original tokens to be removed then stop since
                // there are no tokens ahead that would possibly have to be relexed because of LA differences.
                if (change.matchIndex == tokenCount)
                    break;

                int matchPointOrigLookahead = (change.matchIndex > 0)
                        ? tokenList.lookahead(change.matchIndex - 1)
                        : 0;
                // If old and new LAs are the same it should be safe to stop relexing.
                // Also since all tokens are non-empty it's enough to just check
                // LA > 1 (because LA <= 1 cannot span more than one token).
                // The same applies for current LA.
                if (lookahead == matchPointOrigLookahead ||
                    matchPointOrigLookahead <= 1 && lookahead <= 1
                ) {
                    break;
                }
                
                int afterMatchPointTokenLength = tokenList.tokenOrEmbeddingDirect(change.matchIndex).token().length();
                if (matchPointOrigLookahead <= afterMatchPointTokenLength &&
                    lookahead <= afterMatchPointTokenLength
                ) {
                    // Here both the original and relexed before-match-point token
                    // have their LAs ending within bounds of the after-match-point token so it's OK
                    break;
                }

                // It's true that nothing can be generally predicted about LA if the token after match point
                // would be relexed (compared to the original's token LA). However the following criteria
                // should possibly suffice.
                int afterMatchPointOrigTokenLookahead = tokenList.lookahead(change.matchIndex);
                if (lookahead - afterMatchPointTokenLength <= afterMatchPointOrigTokenLookahead &&
                    (matchPointOrigLookahead <= afterMatchPointTokenLength ||
                        lookahead >= matchPointOrigLookahead)
                ) {
                    // The orig LA of after-match-point token cannot be lower than the currently lexed  LA's projection into it.
                    // Also check that the orig lookahead ended in the after-match-point token
                    // or otherwise require the relexed before-match-point token to have >= lookahead of the original
                    // before-match-point token).
                    break;
                }

                // The token at matchIndex must be relexed
                if (loggable) {
                    LOG.log(Level.FINE, "    EXTRA-RELEX: maInd=" + change.matchIndex + ", LA=" + lookahead + "\n");
                }
                // Skip the token at matchIndex
                change.increaseMatchIndex();
                // Continue by fetching next token
            }
        }
        lexerInputOperation.release();

        // If at least two tokens were lexed it's possible that e.g. the last added token
        // will be the same like the last removed token and in such case
        // the addition of the last token should be 'undone'.
        // This all may happen due to the fact that for larger lookaheads
        // the algorithm must relex the token(s) within lookahead (see the code above).
        int lastAddedTokenIndex = change.addedTokenOrEmbeddingsCount() - 1;
        // There should remain at least one added token since that one
        // may not be the same like the original removed one because
        // token lengths would differ because of the input source modification.
        
        if (change.matchOffset != Integer.MAX_VALUE) { // would not make sense when lexing past end of existing tokens
            while (lastAddedTokenIndex >= 1 && // At least one token added
                    change.matchIndex > lowestMatchIndex // At least one token removed
            ) {
                AbstractToken<T> lastAddedToken = change.addedTokenOrEmbeddings().get(lastAddedTokenIndex).token();
                AbstractToken<T> lastRemovedToken = tokenList.tokenOrEmbeddingDirect(change.matchIndex - 1).token();
                if (lastAddedToken.id() != lastRemovedToken.id()
                    || lastAddedToken.length() != lastRemovedToken.length()
                    || change.laState().lookahead(lastAddedTokenIndex) != tokenList.lookahead(change.matchIndex - 1)
                    || !LexerUtilsConstants.statesEqual(change.laState().state(lastAddedTokenIndex),
                        tokenList.state(change.matchIndex - 1))
                ) {
                    break;
                }
                // Last removed and added tokens are the same so undo the addition
                if (loggable) {
                    LOG.log(Level.FINE, "    RETAIN-ORIGINAL at (maInd-1)=" + (change.matchIndex-1) +
                            ", id=" + lastRemovedToken.id() + "\n");
                }
                lastAddedTokenIndex--;
                // Includes decreasing of matchIndex and matchOffset
                change.removeLastAddedToken();
                relexOffset = change.addedEndOffset;
            }
        } else { // matchOffset == Integer.MAX_VALUE
            // Fix matchOffset to point to end of last token since it's used
            //   as last-added-token-end-offset in event notifications
            change.setMatchOffset(tokenList.endOffset());
        }
    }

    private static <T extends TokenId> void logModification(MutableTokenList<T> tokenList,
            TokenHierarchyEventInfo eventInfo, boolean updateJoined
    ) {
        int modOffset = eventInfo.modOffset();
        int removedLength = eventInfo.removedLength();
        int insertedLength = eventInfo.insertedLength();
        CharSequence inputSourceText = tokenList.inputSourceText();
        String insertedText = "";
        if (insertedLength > 0) {
            insertedText = ", insTxt:\"" + CharSequenceUtilities.debugText(
                    inputSourceText.subSequence(modOffset, modOffset + insertedLength)) + '"';
        }
        // Debug 10 chars around modOffset
        int afterInsertOffset = modOffset + insertedLength;
        CharSequence beforeText = inputSourceText.subSequence(Math.max(afterInsertOffset - 5, 0), afterInsertOffset);
        CharSequence afterText = inputSourceText.subSequence(afterInsertOffset,
                Math.min(afterInsertOffset + 5, inputSourceText.length()));
        StringBuilder sb = new StringBuilder(200);
        sb.append(updateJoined ? "JOINED" : "REGULAR");
        sb.append("-UPDATE: \"");
        sb.append(tokenList.languagePath().mimePath()).append("\"\n");
        sb.append("  modOff=").append(modOffset);
        sb.append(", text-around:\"").append(beforeText).append('|');
        sb.append(afterText).append("\", insLen=");
        sb.append(insertedLength).append(insertedText);
        sb.append(", remLen=").append(removedLength);
        sb.append(", tCnt=").append(tokenList.tokenCountCurrent()).append('\n');
        // Use INFO level to allow to log the modification in case of failure
        // when FINE level is not enabled. The "loggable" var should be checked
        // for regular calls of this method.
        LOG.log(Level.INFO, sb.toString());
    }

}
