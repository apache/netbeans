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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.lib.lexer.inc.IncTokenList;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.inc.TokenHierarchyUpdate;
import org.netbeans.lib.lexer.inc.TokenListChange;

/**
 * Token hierarchy operation services tasks of its associated token hierarchy.
 * <br/>
 * There is one-to-one relationship between token hierarchy and its operation.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyOperation<I, T extends TokenId> { // "I" stands for input

    // -J-Dorg.netbeans.lib.lexer.TokenHierarchyOperation.level=FINE
    static final Logger LOG = Logger.getLogger(TokenHierarchyOperation.class.getName());
    
    // -J-Dorg.netbeans.spi.lexer.MutableTextInput.level=FINE
    private static final Logger LOG_LOCK = Logger.getLogger(MutableTextInput.class.getName()); // Logger for read/write-lock

    // -J-Dorg.netbeans.api.lexer.TokenHierarchyEvent.level=FINE
    private static final Logger LOG_EVENT = Logger.getLogger(TokenHierarchyEvent.class.getName()); // Logger for firing events

    private static final Set<StackElementArray> missingLockStacks = StackElementArray.createSet();

    /**
     * Input source of this token hierarchy.
     */
    private final I inputSource;
    
    /**
     * The token hierarchy delegating to this operation.
     * <br>
     * There is one-to-one relationship between token hierarchy and its operation.
     */
    private TokenHierarchy<I> tokenHierarchy;
    
    /**
     * Mutable text input for mutable token hierarchy or null otherwise.
     */
    private MutableTextInput<I> mutableTextInput;

    /**
     * Root token list of this hierarchy. It is created in constructor and never changed
     * during the whole lifetime of the token hierarchy.
     */
    private final TokenList<T> rootTokenList;
    
    /**
     * The hierarchy can be made inactive to release the tokens
     * and the memory that they consume temporarily.
     * <br>
     * By default the hierarchy is active for immutable inputs and unitialized
     * for mutable inputs (will become active upon first ask for TH.tokenSequence()
     * when MTI.language() will provide a valid language).
     */
    private Activity activity;
    
    private LanguagePath lastActiveLanguagePath;

    /**
     * Listener list solely for token change listeners.
     */
    private EventListenerList listenerList;
    
    private Set<LanguagePath> languagePaths;
    
    private Set<Language<?>> exploredLanguages;

    /**
     * Mapping of language path to token list lists.
     * <br/>
     * If a token list list is contained then all its parents
     * with the shorter language path are also mandatorily maintained.
     */
    private Map<LanguagePath,TokenListList<?>> path2tokenListList;
    
    private Object rootChildrenLanguages; // null or language or Language[]
    
    private int maxTokenListListPathSize;
    
    /**
     * Actively hold wrap tokenid caches for the lifetime of this token hierarchy.
     */
    private WrapTokenIdCache<?>[] wrapTokenIdCaches;

    private Language<?> lastQueryLanguage;
    
    private WrapTokenIdCache<?> lastQueryCache;
    
    private int recreateAttempts;

    /**
     * Constructor for reader as input.
     */
    public TokenHierarchyOperation(Reader inputReader,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {
        if (inputReader == null)
            throw new IllegalArgumentException("inputReader cannot be null"); // NOI18N
        if (language == null)
            throw new IllegalArgumentException("language cannot be null");

        @SuppressWarnings("unchecked")
        I input = (I)inputReader;
        this.inputSource = input;

        // Instead of using an original CopyTextTokenList that allowed to skip
        // individual characters of all flyweight tokens do just a copy of all chars
        // from the Reader. TBD - do a lazy reading instead of pre-reading.
        char[] chars = new char[LexerUtilsConstants.READER_TEXT_BUFFER_SIZE];
        int offset = 0;
        try {
            while (true) {
                int readLen = inputReader.read(chars, offset, chars.length - offset);
                if (readLen == -1) // End of stream
                    break;
                offset += readLen;
                if (offset == chars.length) { // Full buffer
                    chars = ArrayUtilities.charArray(chars); // Double array size
                }
            }
        } catch (IOException e) {
            // Ignored silently - there should be a wrapping reader catching and properly handling
            // this IOException.
        } finally {
            // Attempt to close the Reader
            try {
                inputReader.close();
            } catch (IOException e) {
                // Ignored silently - there should be a wrapping reader catching and properly handling
                // this IOException.
            }
        }
        String inputText = new String(chars, 0, offset); // Copy of reader's whole text

        this.rootTokenList = new BatchTokenList<T>(this, inputText,
                language, skipTokenIds, inputAttributes);
        init();
        activity = Activity.ACTIVE;
    }

    /**
     * Constructor for character sequence as input.
     */
    public TokenHierarchyOperation(CharSequence inputText, boolean copyInputText,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {
        if (inputText == null)
            throw new IllegalArgumentException("inputText cannot be null"); // NOI18N
        if (language == null)
            throw new IllegalArgumentException("language cannot be null");

        @SuppressWarnings("unchecked")
        I input = (I)inputText;
        this.inputSource = input;
        if (copyInputText) {
            // Instead of using an original CopyTextTokenList (that allowed to skip
            // individual characters of all flyweight tokens) do just a copy of the full text
            // and use regular BatchTokenList.
            inputText = inputText.toString();
        }
        this.rootTokenList = new BatchTokenList<T>(this, inputText,
                        language, skipTokenIds, inputAttributes);
        init();
        activity = Activity.ACTIVE;
    }

    /**
     * Constructor for mutable input.
     */
    public TokenHierarchyOperation(MutableTextInput<I> mutableTextInput) {
        this.inputSource = LexerSpiPackageAccessor.get().inputSource(mutableTextInput);
        this.mutableTextInput = mutableTextInput;
        this.rootTokenList = new IncTokenList<T>(this);
        init();
        activity = Activity.NOT_INITED;
    }

    private void init() {
        assert (tokenHierarchy == null);
        tokenHierarchy = LexerApiPackageAccessor.get().createTokenHierarchy(this);
        // Create listener list even for non-mutable hierarchies as there may be
        // custom embeddings created that need to be notified
        listenerList = new EventListenerList();
        rootChildrenLanguages = null;
    }
    
    public TokenHierarchy<I> tokenHierarchy() {
        return tokenHierarchy;
    }
    
    public TokenList<T> rootTokenList() {
        return rootTokenList;
    }

    public int modCount() {
        return rootTokenList.modCount();
    }

    public boolean isMutable() {
        return (mutableTextInput != null);
    }

    public MutableTextInput mutableTextInput() {
        return mutableTextInput;
    }
    
    public I inputSource() {
        return inputSource;
    }
    
    public CharSequence text() {
        if (mutableTextInput != null) {
            return LexerSpiPackageAccessor.get().text(mutableTextInput);
        }
        return null;
    }
    
    public void setActive(boolean active) {
        ensureWriteLocked();
        synchronized (rootTokenList) {
            setActiveImpl(active);
        }
    }

    public void setActiveImpl(boolean active) {
        assert (isMutable()) : "Activity changes only allowed for mutable input sources";
        // Check whether the state has changed
        Activity newActivity = active ? Activity.ACTIVE : Activity.INACTIVE;
        if (activity != newActivity) {
            boolean notInited = (activity == Activity.NOT_INITED);
            IncTokenList<T> incTokenList = (IncTokenList<T>)rootTokenList;
            boolean doFire = (!notInited && listenerList.getListenerCount() > 0);
            TokenListChange<T> change = null;
            TokenHierarchyEventInfo eventInfo = notInited
                    ? null
                    : new TokenHierarchyEventInfo(this, TokenHierarchyEventType.ACTIVITY, 0, 0, "", 0);
            if (!notInited) { // Increase modCount if not doing init
                incTokenList.incrementModCount();
            }
            if (active) { // Wishing to be active
                if (lastActiveLanguagePath != null) {
                    incTokenList.setLanguagePath(lastActiveLanguagePath);
                    change = TokenListChange.createRebuildChange(incTokenList);
                    incTokenList.replaceTokens(change, eventInfo, true);
                    CharSequence text = LexerSpiPackageAccessor.get().text(mutableTextInput);
                    eventInfo.setMaxAffectedEndOffset(text.length());
                    invalidatePath2TokenListList();
                } else { // Was not yet active
                    Language<?> language = LexerSpiPackageAccessor.get().language(mutableTextInput());
                    if (language != null) {
                        incTokenList.setLanguagePath((language != null) ? LanguagePath.get(language) : null);
                        if (!notInited) {
                            change = TokenListChange.createEmptyChange(incTokenList);
                        }
                    } else { // No valid top language => no change in activity
                        return; // It will continue to ask mutableTextInput.language() until it returns non-null result
                    }
                }
                incTokenList.reinit(); // Initialize lazy lexing
            } else { // Wishing to be inactive
                if (!notInited) {
                    change = TokenListChange.createEmptyChange(incTokenList);
                }
            }
            if (change != null) {
                eventInfo.setTokenChangeInfo(change.tokenChangeInfo());
            }

            activity = newActivity;

            if (doFire) { // Only if there are already listeners
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Firing ACTIVITY change to " + listenerList.getListenerCount() + " listeners: " + activity); // NOI18N
                }
                fireTokenHierarchyChanged(eventInfo); // Retain valid languagePath during firing if becoming inactive
                if (!active) { // Becoming inactive
                    lastActiveLanguagePath = incTokenList.languagePath();
                    incTokenList.setLanguagePath(null); // Make incTokenList inactive by assigning null LP
                }
            }
        }
    }

    private void invalidatePath2TokenListList() {
        path2tokenListList = null; // Drop all token list lists
        rootChildrenLanguages = null;
        maxTokenListListPathSize = 0;
    }

    /**
     * Check whether the hierarchy is active doing initialization (an attempt to activate the hierarchy)
     * if it's not active yet (and it was not set to be inactive).
     *
     * @return true if the hierarchy is currently active or false otherwise.
     */
    public boolean isActive() {
        ensureReadLocked();
        synchronized (rootTokenList) {
            return isActiveImpl();
        }
    }
    
    public boolean isActiveImpl() {
        // Activate if possible
        if (activity == Activity.NOT_INITED) {
            setActiveImpl(true); // Attempt to activate
        }
        return isActiveNoInit();
    }
    
    public boolean isActiveNoInit() { // BTW used by tests to check if hierarchy is active or not
        return (activity == Activity.ACTIVE);
    }

    /**
     * Recreate token hierarchy if there was a runtime exception thrown during token hierarchy recreation.
     *
     * @param e runtime exception that was thrown.
     */
    public RuntimeException recreateAfterError(RuntimeException e) {
        if (!TokenList.LOG.isLoggable(Level.FINE)) { // Running tests or strict mode
            LOG.log(Level.INFO, "Runtime exception occurred during token hierarchy updating. Token hierarchy will be rebuilt from scratch.", e);
            if (isActiveNoInit()) {
                recreateAttempts++;
                if (recreateAttempts < 2) { // Prevent an infinite loop
                    rebuild(); // Might fail too
                    recreateAttempts = 0; // If recreate succeeded allow future recreates again
                }
            }
        }
        return e;
    }

    public void ensureReadLocked() {
        if (isMutable() &&
                // LOG_LOCK.isLoggable(Level.FINE) &&
                !LexerSpiPackageAccessor.get().isReadLocked(mutableTextInput)
        ) { // Not read-locked
            if (StackElementArray.addStackIfNew(missingLockStacks, 4)) {
                LOG_LOCK.log(Level.INFO, "!!WARNING!! Missing READ-LOCK when accessing TokenHierarchy: input-source:"
                        + LexerSpiPackageAccessor.get().inputSource(mutableTextInput),
                        new Exception());
            }
        }
    }
    
    public void ensureWriteLocked() {
        if (isMutable() &&
                // LOG_LOCK.isLoggable(Level.FINE) &&
                !LexerSpiPackageAccessor.get().isWriteLocked(mutableTextInput)
        ) { // Not write-locked
            if (StackElementArray.addStackIfNew(missingLockStacks, 4)) {
                LOG_LOCK.log(Level.INFO, "!!WARNING!! Missing WRITE-LOCK when accessing TokenHierarchy: input-source:"
                        + LexerSpiPackageAccessor.get().inputSource(mutableTextInput),
                        new Exception());
            }
        }
    }
    
    public TokenSequence<T> tokenSequence() {
        return tokenSequence(null);
    }
    
    public TokenSequence<T> tokenSequence(Language<?> language) {
        ensureReadLocked();
        synchronized (rootTokenList) {
            TokenSequence<T> ts;
            if (isActiveImpl() && (language == null || rootTokenList.language() == language)) {
                ts = LexerApiPackageAccessor.get().createTokenSequence(rootTokenList);
            } else {
                ts = null;
            }
            return ts;
        }
    }
    
    public List<TokenSequence<?>> tokenSequenceList(
    LanguagePath languagePath, int startOffset, int endOffset) {
        if (languagePath == null)
            throw new IllegalArgumentException("languagePath cannot be null"); // NOI18N
        ensureReadLocked();
        synchronized (rootTokenList) {
            return isActiveImpl()
                ? new TokenSequenceList(rootTokenList, languagePath, startOffset, endOffset)
                : null;
        }
    }

    public List<TokenSequence<?>> embeddedTokenSequences(
        int offset, boolean backwardBias
    ) {
        synchronized (rootTokenList) {
            TokenSequence<?> seq = tokenSequence();
            List<TokenSequence<?>> sequences = new ArrayList<TokenSequence<?>>();

            while (seq != null) {
                seq.move(offset);
                if (seq.moveNext()) {
                    if (seq.offset() == offset && backwardBias) {
                        if (seq.movePrevious()) {
                            sequences.add(seq);
                            seq = seq.embedded();
                        } else {
                            seq = null;
                        }
                    } else {
                        sequences.add(seq);
                        seq = seq.embedded();
                    }
                } else if (backwardBias && seq.movePrevious()) {
                    sequences.add(seq);
                    seq = seq.embedded();
                } else {
                    seq = null;
                }
            }
            
            return sequences;
        }
    }
    
    /**
     * Get the token list list for the given language path.
     * <br/>
     * If the list needs to be created or it was non-mandatory.
     */
    public <ET extends TokenId> TokenListList<ET> tokenListList(LanguagePath languagePath) {
        assert isActiveNoInit() : "Token hierarchy expected to be active.";
        @SuppressWarnings("unchecked")
        TokenListList<ET> tll = (TokenListList<ET>) path2tokenListList().get(languagePath);
        if (tll == null) {
            tll = new TokenListList<ET>(rootTokenList, languagePath);
            path2tokenListList.put(languagePath, tll);
            maxTokenListListPathSize = Math.max(languagePath.size(), maxTokenListListPathSize);
            // Also create parent token list lists if they don't exist yet
            Language<?> innerLanguage = languagePath.innerLanguage();
            if (languagePath.size() >= 3) { // Top-level token list list not maintained
                tokenListList(languagePath.parent()).notifyChildAdded(innerLanguage);
            } else {
                assert (languagePath.size() == 2);
                assert (languagePath.parent() == rootTokenList.languagePath());
                rootChildrenLanguages = LexerUtilsConstants.languageOrArrayAdd(rootChildrenLanguages, innerLanguage);
            }
        }
        return tll;
    }
    
    /**
     * Get existing token list list or null if the TLL does not exist yet.
     */
    public <ET extends TokenId> TokenListList<ET> existingTokenListList(LanguagePath languagePath) {
        @SuppressWarnings("unchecked")
        TokenListList<ET> tll = (path2tokenListList != null) 
                ? (TokenListList<ET>) path2tokenListList.get(languagePath)
                : null;
        return tll;
    }

    public Object rootChildrenLanguages() { // Not used from API
        return rootChildrenLanguages;
    }

    private Map<LanguagePath,TokenListList<?>> path2tokenListList() {
        if (path2tokenListList == null) {
            path2tokenListList = new HashMap<LanguagePath,TokenListList<?>>(4, 0.5f);
        }
        return path2tokenListList;
    }
    
    public int maxTokenListListPathSize() {
        return maxTokenListListPathSize;
    }

    public void rebuild() {
        ensureWriteLocked();
        synchronized (rootTokenList) {
            if (isActiveNoInit()) {
                IncTokenList<T> incTokenList = (IncTokenList<T>)rootTokenList;
                incTokenList.incrementModCount();
                CharSequence text = LexerSpiPackageAccessor.get().text(mutableTextInput);
                TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                    this, TokenHierarchyEventType.REBUILD, 0, 0, "", 0);
                TokenListChange<T> change = TokenListChange.createRebuildChange(incTokenList);
                incTokenList.replaceTokens(change, eventInfo, true);
                incTokenList.reinit(); // Will relex tokens lazily

                eventInfo.setTokenChangeInfo(change.tokenChangeInfo());
                eventInfo.setMaxAffectedEndOffset(text.length());

                invalidatePath2TokenListList();
                fireTokenHierarchyChanged(eventInfo);
            } // not active - no changes fired
        }
    }

    public void fireTokenHierarchyChanged(TokenHierarchyEventInfo eventInfo) {
        TokenHierarchyEvent evt = LexerApiPackageAccessor.get().createTokenChangeEvent(eventInfo);
        Object[] listeners = listenerList.getListenerList();
        int listenersLength = listeners.length;
        boolean loggable = LOG_EVENT.isLoggable(Level.FINE);
        long tm = 0;
        if (loggable) {
            LOG_EVENT.fine("Firing " + evt + " to " + listenersLength/2 + " listeners:\n");
        }
        for (int i = 1; i < listenersLength; i += 2) {
            if (loggable) {
                tm = System.currentTimeMillis();
            }
            ((TokenHierarchyListener)listeners[i]).tokenHierarchyChanged(evt);
            if (loggable) {
                LOG_EVENT.fine(String.valueOf(System.currentTimeMillis() - tm) + "ms: Fired to " + listeners[i] + "\n");
            }
        }
        if (loggable) {
            LOG_EVENT.fine("----- Finished firing of " + evt + "\n");
        }
    }
    
    public void addTokenHierarchyListener(TokenHierarchyListener listener) {
        listenerList.add(TokenHierarchyListener.class, listener);
    }
    
    public void removeTokenHierarchyListener(TokenHierarchyListener listener) {
        listenerList.remove(TokenHierarchyListener.class, listener);
    }
    
    public void textModified(int offset, int removedLength, CharSequence removedText, int insertedLength) {
        ensureWriteLocked();
        synchronized (rootTokenList) {
            // Attempt to activate the hierarchy in case there are active listeners
            boolean active = isActiveNoInit();
            if (!active && listenerList.getListenerCount() > 0) {
                active = isActive(); // Attempt to activate the hierarchy
            }
            if (active) {
                TokenHierarchyEventInfo eventInfo = new TokenHierarchyEventInfo(
                        this, TokenHierarchyEventType.MODIFICATION,
                        offset, removedLength, removedText, insertedLength);
                new TokenHierarchyUpdate(eventInfo).update();
                fireTokenHierarchyChanged(eventInfo);
            }
        }
    }

    public Set<LanguagePath> languagePaths() {
        ensureReadLocked();
        Set<LanguagePath> lps;
        synchronized (rootTokenList) {
            lps = languagePaths;
            if (lps == null) {
                if (!isActiveImpl())
                    return Collections.emptySet();
                Language<?> lang = rootTokenList.language();
                LanguageOperation<?> langOp = LexerApiPackageAccessor.get().languageOperation(lang);
                @SuppressWarnings("unchecked")
                Set<LanguagePath> clps = (Set<LanguagePath>)
                        ((HashSet<LanguagePath>)langOp.languagePaths()).clone();
                lps = clps;

                @SuppressWarnings("unchecked")
                Set<Language<?>> cel = (Set<Language<?>>)
                        ((HashSet<Language<?>>)langOp.exploredLanguages()).clone();
                exploredLanguages = cel;
                languagePaths = lps;
            }
        }
        return lps;
    }
    
    public void addLanguagePath(LanguagePath lp) {
        Set<LanguagePath> elps = languagePaths(); // init if not inited yet
        if (!elps.contains(lp)) {
            // Add the new language path
            Set<LanguagePath> lps = new HashSet<LanguagePath>();
            LanguageOperation.findLanguagePaths(elps, lps, exploredLanguages, lp);
            elps.addAll(lps);
            // Fire the token hierarchy change event
        }
    }

    public <T extends TokenId> WrapTokenIdCache<T> getWrapTokenIdCache(Language<T> language) {
        // Since syncing on root TL there should be no need for additional synching
        if (language == lastQueryLanguage) {
            @SuppressWarnings("unchecked")
            WrapTokenIdCache<T> cache = (WrapTokenIdCache<T>) lastQueryCache;
            return cache;
        } else {
            int lid = LexerApiPackageAccessor.get().languageId(language);
            if (wrapTokenIdCaches == null || lid >= wrapTokenIdCaches.length) {
                WrapTokenIdCache<?>[] n = (WrapTokenIdCache<?>[]) new WrapTokenIdCache<?>[lid + 1];
                if (wrapTokenIdCaches != null) {
                    System.arraycopy(wrapTokenIdCaches, 0, n, 0, wrapTokenIdCaches.length);
                }
                wrapTokenIdCaches = n;
            }
            @SuppressWarnings("unchecked")
            WrapTokenIdCache<T> cache = (WrapTokenIdCache<T>) wrapTokenIdCaches[lid];
            if (cache == null) {
                cache = WrapTokenIdCache.get(language);
                wrapTokenIdCaches[lid] = cache;
            }
            
            lastQueryLanguage = language;
            lastQueryCache = cache;
            return cache;
        }
    }

    
//    public boolean isSnapshot() {
//        return (liveTokenHierarchyOperation != null);
//    }
//
//    public TokenHierarchy<I> snapshotOf() {
//        return (isSnapshot() ? liveTokenHierarchyOperation.tokenHierarchy() : null);
//    }
//
//    private void checkIsSnapshot() {
//        if (!isSnapshot()) {
//            throw new IllegalStateException("Not a snapshot");    
//        }
//    }
//
//    private void checkSnapshotNotReleased() {
//        if (snapshotReleased) {
//            throw new IllegalStateException("Snapshot already released"); // NOI18N
//        }
//    }
//
//    public TokenHierarchy<I> createSnapshot() {
//        if (isMutable()) {
//            TokenHierarchyOperation<I,T> snapshot = new TokenHierarchyOperation<I,T>(this);
//            snapshotRefs.add(new SnapshotRef(snapshot));
//            return snapshot.tokenHierarchy();
//        }
//        return null;
//    }
//
//    public void snapshotRelease() {
//        checkIsSnapshot();
//        checkSnapshotNotReleased();
//
//        snapshotReleased = true;
//        if (liveTokenHierarchyOperation != null) { // only when "real" snapshot for mutable hierarchies
//            // Remove the reference from the snapshots array
//            liveTokenHierarchyOperation.removeSnapshot(this);
//        }
//    }
//
//    public boolean isSnapshotReleased() {
//        return snapshotReleased;
//    }
//
//    void removeSnapshot(SnapshotRef snapshotRef) {
//        synchronized (snapshotRefs) {
//            snapshotRefs.remove(snapshotRef);
//        }
//    }
//    
//    void removeSnapshot(TokenHierarchyOperation<I,T> snapshot) {
//        synchronized (snapshotRefs) {
//            for (int i = snapshotRefs.size() - 1; i >= 0; i--) {
//                Reference ref = (Reference)snapshotRefs.get(i);
//                if (ref.get() == snapshot) {
//                    snapshotRefs.remove(i);
//                    break;
//                }
//            }
//        }
//    }
//
//    private int snapshotCount() {
//        synchronized (snapshotRefs) {
//            return snapshotRefs.size();
//        }
//    }
//
//    public boolean canModifyToken(int index, AbstractToken token) {
//        synchronized (snapshotRefs) {
//            for (int i = snapshotCount() - 1; i >= 0; i--) {
//                TokenHierarchyOperation op = snapshotRefs.get(i).get();
//                
//                if (op != null && ((SnapshotTokenList) op.rootTokenList()).canModifyToken(index, token)) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    public TokenHierarchyOperation<I,T> liveTokenHierarchyOperation() {
//        return liveTokenHierarchyOperation;
//    }
//
//    public <TT extends TokenId> int tokenOffset(AbstractToken<TT> token, TokenList<TT> tokenList, int rawOffset) {
//        if (this.rootTokenList.getClass() == SnapshotTokenList.class) {
//            if (tokenList != null) {
//                @SuppressWarnings("unchecked")
//                SnapshotTokenList<TT> tlUC = (SnapshotTokenList<TT>)this.rootTokenList;
//                return tlUC.tokenOffset(token, tokenList, rawOffset);
//            } else { // passed tokenList is null => token removed from EmbeddedTokenList
//                return rawOffset;
//            }
//        } else { // not a snapshot - regular situation
//            return (tokenList != null)
//                    ? tokenList.childTokenOffset(rawOffset)
//                    : rawOffset;
//        }
//    }
//
//    public int tokenShiftStartOffset() {
//        return isSnapshot() ? ((SnapshotTokenList)rootTokenList).tokenShiftStartOffset() : -1;
//    }
//
//    public int tokenShiftEndOffset() {
//        return isSnapshot() ? ((SnapshotTokenList)rootTokenList).tokenShiftEndOffset() : -1;
//    }
//    
    @Override
    public String toString() {
        synchronized (rootTokenList) {
            StringBuilder sb = toStringNoTokens(null);
            sb.append(":\n"); // NOI18N
            LexerUtilsConstants.appendTokenList(sb, rootTokenList);
            if (path2tokenListList != null && path2tokenListList.size() > 0) {
                sb.append(path2tokenListList.size());
                sb.append(" TokenListList(s) maintained:\n"); // NOI18N
                for (TokenListList tll : path2tokenListList.values()) {
                    sb.append(tll).append('\n');
                }
            }
            String errors = checkConsistency();
            if (errors != null) {
                sb.append("!!! CONSISTENCY ERRORS FOUND in TOKEN HIERARCHY:\n");
                sb.append(errors);
                sb.append('\n');
            }
            return sb.toString();
        }
    }

    public StringBuilder toStringNoTokens(StringBuilder sb) {
        synchronized (rootTokenList) {
            if (sb == null)
                sb = new StringBuilder(200);
            sb.append("TOKEN HIERARCHY"); // NOI18N
            if (inputSource() != null) {
                sb.append(" for " + inputSource());
            }
            if (!isActive()) {
                sb.append(" is NOT ACTIVE.");
            } else {
                CharSequence inputSourceText = rootTokenList.inputSourceText();
                sb.append("\nText: ").append(inputSourceText.getClass());
                sb.append(", length=").append(inputSourceText.length());
            }
            return sb;
        }
    }

    /**
     * Check consistency of the whole token hierarchy.
     * @return string describing the problem or null if the hierarchy is consistent.
     */
    public String checkConsistency() {
        synchronized (rootTokenList) {
            // Check root token list first
            String error = LexerUtilsConstants.checkConsistencyTokenList(rootTokenList(), true);
            // Check token-list lists
            if (error == null && path2tokenListList != null) {
                for (TokenListList<?> tll : path2tokenListList.values()) {
                    // Check token-list list consistency
                    error = tll.checkConsistency();
                    if (error != null)
                        return error;
                    // Check each individual token list in token-list list
                    for (TokenList<?> tl : tll) {
                        error = LexerUtilsConstants.checkConsistencyTokenList(tl, false);
                        if (error != null) {
                            return error;
                        }
                    }
                }
            }
            return error;
        }
    }

    public void ensureConsistency() {
        String errors = checkConsistency();
        if (errors != null) {
            throw new IllegalStateException("!!! CONSISTENCY ERRORS FOUND in TOKEN HIERARCHY:\n" + errors);
        }
    }

//    private final class SnapshotRef extends WeakReference<TokenHierarchyOperation<I,T>> implements Runnable {
//        
//        SnapshotRef(TokenHierarchyOperation<I,T> snapshot) {
//            super(snapshot, org.openide.util.Utilities.activeReferenceQueue());
//        }
//
//        public void run() {
//            if (liveTokenHierarchyOperation != null) {
//                // Remove the reference from the snapshots array
//                liveTokenHierarchyOperation.removeSnapshot(this);
//            }
//        }
//
//    }

    static enum Activity {
        
        NOT_INITED, // Initial state for mutable inputs
        INACTIVE, // Explicitly set to inactive
        ACTIVE; // Default for immutable hierarchies; mutable THs once MTI.language() is non-null
        
    }

}
