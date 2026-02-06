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

package org.netbeans.modules.textmate.lexer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.ListenerList;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**Copied from editor.lib2, converts the tokens to AttributeSet in a way that
 * works for TextmateToken.
 * The syntax coloring layer.
 * <br>
 * It excludes newline chars from any colorings so that if e.g. a whitespace highlighting is set
 * the rest of line after newline is not colored.
 * 
 * @author Vita Stejskal
 * @author Miloslav Metelka
 */
public final class SyntaxHighlighting extends AbstractHighlightsContainer
implements TokenHierarchyListener, ChangeListener {
    
    // -J-Dorg.netbeans.modules.editor.lib2.highlighting.SyntaxHighlighting.level=FINEST
    private static final Logger LOG = Logger.getLogger(SyntaxHighlighting.class.getName());
    
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.SyntaxHighlighting"; //NOI18N
    
    /**
     * Static cache for colorings mapping mime-path to coloring info.
     */
    private static final HashMap<String, FCSInfo<?>> globalFCSCache = new HashMap<String, FCSInfo<?>>();

    /**
     * Local cache of items from globalFCSCache.
     */
    private final HashMap<String, FCSInfo<?>> fcsCache = new HashMap<String, FCSInfo<?>>();
    
    private final Document document;

    /**
     * Either null or a mime-type that starts with "test" and it's used
     * for preview in Tools/Options/Fonts-and-Colors.
     */
    private final String mimeTypeForOptions;

    private final TokenHierarchy<? extends Document> hierarchy;

    private long version = 0;
    
    /** Creates a new instance of SyntaxHighlighting */
    public SyntaxHighlighting(Document document) {
        this.document = document;
        String mimeType = (String) document.getProperty("mimeType"); //NOI18N
        if (mimeType != null && mimeType.startsWith("test")) { //NOI18N
            this.mimeTypeForOptions = mimeType;
        } else {
            this.mimeTypeForOptions = null;
        }
        
        // Start listening on changes in global colorings since they may affect colorings for target language
        findFCSInfo("", null);

        hierarchy = TokenHierarchy.get(document);
        hierarchy.addTokenHierarchyListener(WeakListeners.create(TokenHierarchyListener.class, this, hierarchy));
    }

    public @Override HighlightsSequence getHighlights(int startOffset, int endOffset) {
        long lVersion = getVersion();
        ((AbstractDocument) document).readLock();
        try {
            if (hierarchy.isActive()) {
                return new HSImpl(lVersion, hierarchy, startOffset, endOffset);
            } else {
                return HighlightsSequence.EMPTY;
            }
        } finally {
            ((AbstractDocument) document).readUnlock();
        }
    }

    // ----------------------------------------------------------------------
    //  TokenHierarchyListener implementation
    // ----------------------------------------------------------------------

    @Override
    public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
        if (evt.type() == TokenHierarchyEventType.LANGUAGE_PATHS) {
            // ignore
            return;
        }
        
        synchronized (this) {
            version++;
        }

        if (LOG.isLoggable(Level.FINEST)) {
            StringBuilder sb = new StringBuilder();
            TokenSequence<?> ts = hierarchy.tokenSequence();
            
            sb.append("\n"); //NOI18N
            sb.append("Tokens after change: <").append(evt.affectedStartOffset()).append(", ").append(evt.affectedEndOffset()).append(">\n"); //NOI18N
            dumpSequence(ts, sb);
            sb.append("--------------------------------------------\n\n"); //NOI18N
            
            LOG.finest(sb.toString());
        }
        
        fireHighlightsChange(evt.affectedStartOffset(), evt.affectedEndOffset());
//        fireHighlightsChange(0, Integer.MAX_VALUE);
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    // XXX: This hack is here to make sure that preview panels in Tools-Options
    // work. Currently there is no way how to force a particular JTextComponent
    // to use a particular MimeLookup. They all use MimeLookup common for all components
    // and for the mime path of things displayed in that component. The preview panels
    // however need special MimeLookup that loads colorings from a special profile
    // (i.e. not the currently active coloring profile, which is used normally by
    // all the other components).
    //
    // The hack is that Tools-Options modifies mime type of the document loaded
    // in the preview panel and prepend 'textXXXX_' at the beginning. The normal
    // MimeLookup for this mime type and any mime path derived from this mime type
    // is empty. The editor/settings/storage however provides a special handling
    // for these 'test' mime paths and bridge them to the MimeLookup that you would
    // normally get for the mime path without the 'testXXXX_' at the beginning, plus
    // they supply special colorings from the profile called 'testXXXX'. This way
    // the preview panels can have different colorings from the rest of the IDE.
    //
    // This is obviously very fragile and not fully transparent for clients as
    // you can see here. We need a better solution for that. Generally it should
    // be posible to ask somewhere for a component-specific MimeLookup. This would
    // normally be a standard MimeLookup as you know it, but in special cases it
    // could be modified by the client code that created the component - e.g. Tools-Options
    // panel.
    private String languagePathToMimePathOptions(LanguagePath languagePath) {
        if (languagePath.size() == 1) {
            return mimeTypeForOptions;
        } else if (languagePath.size() > 1) {
            return mimeTypeForOptions + "/" + languagePath.subPath(1).mimePath(); //NOI18N
        } else {
            throw new IllegalStateException("LanguagePath should not be empty."); //NOI18N
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireHighlightsChange(0, Integer.MAX_VALUE); // Recompute highlights for whole document
    }

    synchronized long getVersion() {
        return version;
    }
    
    private <T extends TokenId> FCSInfo<T> findFCSInfo(String mimePath, Language<T> language) {
        @SuppressWarnings("unchecked")
        FCSInfo<T> fcsInfo = (FCSInfo<T>) fcsCache.get(mimePath); // Search local cache
        if (fcsInfo == null) { // Search in global cache
            synchronized (globalFCSCache) {
                @SuppressWarnings("unchecked")
                FCSInfo<T> fcsI = (FCSInfo<T>) globalFCSCache.get(mimePath);
                fcsInfo = fcsI;
                if (fcsInfo == null) {
                    fcsInfo = new FCSInfo<T>(mimePath, language);
                    if (mimeTypeForOptions == null) { // Only cache non-test ones globally
                        globalFCSCache.put(mimePath, fcsInfo);
                    }
                }
            }
            fcsInfo.addChangeListener(WeakListeners.change(this, fcsInfo));
            fcsCache.put(mimePath, fcsInfo);
        }
        return fcsInfo;
    }

    private static void dumpSequence(TokenSequence<?> seq, StringBuilder sb) {
        if (seq == null) {
            sb.append("Inactive TokenHierarchy"); //NOI18N
        } else {
            for(seq.moveStart(); seq.moveNext(); ) {
                TokenSequence<?> emSeq = seq.embedded();
                if (emSeq != null) {
                    dumpSequence(emSeq, sb);
                } else {
                    Token<?> token = seq.token();
                    sb.append("<"); //NOI18N
                    sb.append(String.format("%3s", seq.offset())).append(", "); //NOI18N
                    sb.append(String.format("%3s", seq.offset() + token.length())).append(", "); //NOI18N
                    sb.append(String.format("%+3d", token.length())).append("> : "); //NOI18N
                    sb.append(tokenId(token.id(), true)).append(" : '"); //NOI18N
                    sb.append(tokenText(token));
                    sb.append("'\n"); //NOI18N
                }
            }
        }
    }
    
    private static String tokenId(TokenId tokenId, boolean format) {
        if (format) {
            return String.format("%20s.%-15s", tokenId.getClass().getSimpleName(), tokenId.name()); //NOI18N
        } else {
            return tokenId.getClass().getSimpleName() + "." + tokenId.name(); //NOI18N
        }
    }
    
    private static String tokenText(Token<?> token) {
        CharSequence text = token.text();
        StringBuilder sb = new StringBuilder(text.length());
        
        for(int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isISOControl(ch)) {
                switch (ch) {
                case '\n' : sb.append("\\n"); break; //NOI18N
                case '\t' : sb.append("\\t"); break; //NOI18N
                case '\r' : sb.append("\\r"); break; //NOI18N
                default : sb.append("\\").append(Integer.toOctalString(ch)); break; //NOI18N
                }
            } else {
                sb.append(ch);
            }
        }
        
        return sb.toString();
    }

    private static String attributeSet(AttributeSet as) {
        if (as == null) {
            return "AttributeSet is null"; //NOI18N
        }
        
        StringBuilder sb = new StringBuilder();
        
        for(Enumeration<? extends Object> keys = as.getAttributeNames(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = as.getAttribute(key);

            if (key == null) {
                sb.append("null"); //NOI18N
            } else {
                sb.append("'").append(key.toString()).append("'"); //NOI18N
            }

            sb.append(" = "); //NOI18N

            if (value == null) {
                sb.append("null"); //NOI18N
            } else {
                sb.append("'").append(value.toString()).append("'"); //NOI18N
            }

            if (keys.hasMoreElements()) {
                sb.append(", "); //NOI18N
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Checks if the token should be treated as a block. Returns true if the token
     * does NOT representa a block. Blocks are highlighted as a whole, including
     * free space after the trailing whitespaces on the line.
     * 
     * @param t token to check
     * @return true, if token is normal text; false if block.
     */
    private static boolean noBlock(Token<?> t) {
        return t.getProperty("highlight.block") != Boolean.TRUE; // NOI18N
    }
    
    private final class HSImpl implements HighlightsSequence {
        
        private static final int S_INIT = 0;
        private static final int S_TOKEN = 1; // Attempt to branch current token
        private static final int S_NEXT_TOKEN = 2; // Fetch next token
        private static final int S_EMBEDDED_HEAD = 3; // On head of an embedding (in front of its first token)
        private static final int S_EMBEDDED_TAIL = 4; // Just above last token of the embedding
        private static final int S_DONE = 5;

        private final long version;
        private final TokenHierarchy<? extends Document> scanner;
        private final int startOffset;
        private final int endOffset;
        private final CharSequence docText;
        private int newlineOffset;
        private int partsEndOffset; // In case '\n' is in the middle of token this is end offset of the whole token

        // Last found highlight's startOffset, endOffset and attributes
        private int hiStartOffset;
        private int hiEndOffset;
        private AttributeSet hiAttrs;
        
        private List<TSInfo<?>> sequences;
        private int state = S_INIT;
        private LogHelper logHelper;
        
        public HSImpl(long version, TokenHierarchy<? extends Document> scanner, int startOffset, int endOffset) {
            this.version = version;
            this.scanner = scanner;
            startOffset = Math.max(startOffset, 0); // Tests may request Integer.MIN_VALUE for startOffset
            this.startOffset = startOffset;
            this.sequences = new ArrayList<>(4);
            this.hiStartOffset = startOffset;
            this.hiEndOffset = startOffset;
            Document doc = scanner.inputSource();
            this.docText = DocumentUtilities.getText(doc);
            endOffset = Math.min(endOffset, docText.length());
            this.endOffset = endOffset;
            newlineOffset = -1;
            updateNewlineOffset(startOffset);
            @SuppressWarnings("unchecked")
            TokenSequence<TokenId> seq = (TokenSequence<TokenId>) scanner.tokenSequence();
            if (seq != null) {
                seq.move(startOffset);
                TSInfo<TokenId> tsInfo = new TSInfo<TokenId>(seq);
                sequences.add(tsInfo);
                state = S_NEXT_TOKEN;
            } else {
                state = S_DONE;
            }
            if (LOG.isLoggable(Level.FINE)) {
                logHelper = new LogHelper();
                logHelper.startTime = System.currentTimeMillis();
                LOG.fine("SyntaxHighlighting.HSImpl <" + startOffset + "," + endOffset + ">\n"); // NOI18N
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "Highlighting caller", new Exception()); // NOI18N
                }
            }
        }
        
        public @Override boolean moveNext() {
            if (state == S_DONE) {
                return false;
            }

            if (SyntaxHighlighting.this.getVersion() != this.version) {
                finish();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("SyntaxHighlighting: Version changed => HSImpl finished at offset=" + hiEndOffset); // NOI18N
                }
                return false;
            }

            // Check whether processing multiple parts into that token was split due to presence of '\n' char(s)
            if (partsEndOffset != 0) { // Fetch next part
                while (hiEndOffset == newlineOffset) { // Newline at highlight start
                    hiEndOffset++; // Skip newline
                    if (updateNewlineOffset(hiEndOffset)) { // Reached endOffset
                        finish();
                        return false;
                    }
                    if (hiEndOffset >= partsEndOffset) { // Reached end of parts only by newlines
                        finishParts();
                        return moveTheSequence();
                    }
                }
                hiStartOffset = hiEndOffset;
                if (newlineOffset < partsEndOffset) {
                    hiEndOffset = newlineOffset;
                } else {
                    hiEndOffset = partsEndOffset;
                    finishParts();
                }
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("  SH.moveNext(): part-Highlight: <" + hiStartOffset + "," + // NOI18N
                            hiEndOffset + "> attrs=" + hiAttrs + " " + stateToString() + // NOI18N
                            ", pEOffset=" + partsEndOffset + ", seq#=" + sequences.size() + "\n"); // NOI18N
                }
                return true;
            } else if (hiEndOffset >= newlineOffset) {
                updateNewlineOffset(hiEndOffset);
            }

            return moveTheSequence();
        }

        public @Override int getStartOffset() {
            return hiStartOffset;
        }

        public @Override int getEndOffset() {
            return hiEndOffset;
        }

        public @Override AttributeSet getAttributes() {
            return hiAttrs;
        }
        
        private boolean moveTheSequence() {
            boolean done = false;
            boolean log = LOG.isLoggable(Level.FINE);
            do {
                TSInfo<?> tsInfo = sequences.get(sequences.size() - 1);
                switch (state) {
                    case S_TOKEN:
                        TokenSequence<?> embeddedSeq = tsInfo.ts.embedded();
                        if (embeddedSeq != null) {
                            @SuppressWarnings("unchecked")
                            TSInfo<TokenId> embeddedTSInfo = new TSInfo<TokenId>((TokenSequence<TokenId>) embeddedSeq);
                            sequences.add(embeddedTSInfo);
                            if (embeddedTSInfo.moveNextToken(startOffset, endOffset)) { // Embedded token sequence has at least one token
                                int headLen = embeddedTSInfo.tokenOffset - hiEndOffset;
                                if (headLen > 0) {
                                    state = S_EMBEDDED_HEAD;
                                    done = assignHighlightOrPart(embeddedTSInfo.tokenOffset, tsInfo.tokenAttrs, noBlock(tsInfo.ts.token()));
                                    if (log) {
                                        LOG.fine(" S_TOKEN -> S_EMBEDDED_HEAD, token<" + tsInfo.tokenOffset + // NOI18N
                                                "," + tsInfo.tokenEndOffset + "> headLen=" + headLen + "\n"); // NOI18N
                                    }
                                } // else: No head -> attempt further nested embedding on current emb.token
                            } else { // No tokens in embedded sequence
                                state = S_EMBEDDED_TAIL;
                                done = assignHighlightOrPart(tsInfo);
                                if (log) {
                                    LOG.fine(" S_TOKEN -> S_EMBEDDED_TAIL\n");
                                }
                            }
                        } else { // embeddedSeq == null
                            state = S_NEXT_TOKEN;
                            done = assignHighlightOrPart(tsInfo);
                        }
                        break;

                    case S_NEXT_TOKEN:
                        if (tsInfo.moveNextToken(startOffset, endOffset)) {
                            state = S_TOKEN;
                            if (log) {
                                logHelper.tokenCount++;
                            }
                        } else { // No more tokens
                            if (sequences.size() > 1) {
                                TSInfo<?> outerTSInfo = sequences.get(sequences.size() - 2);
                                state = S_EMBEDDED_TAIL;
                                if (tsInfo.tokenEndOffset < outerTSInfo.tokenEndOffset) {
                                    done = assignHighlightOrPart(outerTSInfo);
                                }
                            } else { // Outer sequence => stop processing
                                sequences.clear();
                                finish();
                                if (log) {
                                    LOG.fine("SyntaxHighlighting: " + scanner.inputSource() + //NOI18N
                                            ":\n-> returned " + logHelper.tokenCount + " token highlights for <" + //NOI18N
                                            startOffset + "," + endOffset + //NOI18N
                                            "> in " + //NOI18N
                                            (System.currentTimeMillis() - logHelper.startTime) + " ms.\n"); //NOI18N
                                    LOG.finer(tsInfo.ts.toString());
                                    LOG.fine("\n");
                                }
                                return false;
                            }
                        }
                        break;

                    case S_EMBEDDED_HEAD:
                        // Current token contains embedded language and we have processed its head
                        // First token is fetched already so just use it.
                        state = S_TOKEN;
                        break;

                    case S_EMBEDDED_TAIL:
                        // The current token contains embedded language and we have processed it's tail
                        state = S_NEXT_TOKEN;
                        sequences.remove(sequences.size() - 1);
                        if (log) {
                            LOG.fine("S_EMBEDDED_TAIL -> S_NEXT_TOKEN; sequences.size()=" + sequences.size() + "\n"); // NOI18N
                        }
                        break;

                    default: // Includes S_INIT and S_DONE
                        throw new IllegalStateException("Invalid state: " + state); //NOI18N
                }
            } while (!done);

            if (log) {
                LOG.fine("SH.moveTheSequence(): Highlight: <" + hiStartOffset + "," + hiEndOffset + // NOI18N
                        "> attrs=" + hiAttrs + " " + stateToString() + ", seq#=" + sequences.size() + "\n"); // NOI18N
            }
            return true; // Highlight assigned
        }
        
        private boolean assignHighlightOrPart(TSInfo<?> tsInfo) {
            return assignHighlightOrPart(tsInfo.tokenEndOffset, tsInfo.tokenAttrs, noBlock(tsInfo.ts.token()));
        }

        /**
         * Assign full token or its initial part as a next highlight.
         *
         * @param tokenEndOffset
         * @param attrs
         * @return true if highlight was assigned successfully or false if it could not be assigned
         *  due to token consisting of all newlines.
         */
        private boolean assignHighlightOrPart(int tokenEndOffset, AttributeSet attrs, boolean noBlock) {
            if (noBlock) {
                while (hiEndOffset == newlineOffset) { // Newline at highlight start
                    hiEndOffset++; // Skip newline
                    if (updateNewlineOffset(hiEndOffset) || hiEndOffset >= tokenEndOffset) { // Reached endOffset
                        hiStartOffset = hiEndOffset;
                        return false;
                    }
                }
            } 
            hiStartOffset = hiEndOffset;
            if (hiEndOffset >= tokenEndOffset) {
                hiStartOffset = hiEndOffset;
                return false;
            }
            if (noBlock && newlineOffset >= hiStartOffset && newlineOffset < tokenEndOffset) {
                hiEndOffset = newlineOffset;
                partsEndOffset = tokenEndOffset;
            } else {
                hiEndOffset = tokenEndOffset;
            }
            hiAttrs = attrs;
            return true;
        }
        
        /**
         * Update newlineOffset.
         *
         * @param offset scan start offset. If '\n' right at offset then
         *  newlineOffset will be == offset.
         */
        private boolean updateNewlineOffset(int offset) {
            while (offset < endOffset) {
                if (docText.charAt(offset) == '\n') {
                    newlineOffset = offset;
                    return false;
                }
                offset++;
            }
            newlineOffset = endOffset;
            return true;
        }
        
        private void finishParts() {
            partsEndOffset = 0;
        }

        private void finish() {
            state = S_DONE;
            hiStartOffset = endOffset;
            hiEndOffset = endOffset;
            hiAttrs = null;
        }

        private String stateToString() {
            switch (state) {
                case S_INIT:
                    return "S_INIT"; // NOI18N
                case S_TOKEN:
                    return "S_TOKEN"; // NOI18N
                case S_NEXT_TOKEN:
                    return "S_NEXT_TOKEN"; // NOI18N
                case S_EMBEDDED_HEAD:
                    return "S_EMBEDDED_HEAD"; // NOI18N
                case S_EMBEDDED_TAIL:
                    return "S_EMBEDDED_TAIL"; // NOI18N
                case S_DONE:
                    return "S_DONE"; // NOI18N
                default:
                    throw new IllegalStateException("Unknown state=" + state); // NOI18N
            }
        }

    } // End of HSImpl class

    private static final class LogHelper {

        int tokenCount;
        long startTime;
    }

    //for tests:
    static AttributeSet TEST_FALLBACK_COLORING;

    private static final class FCSInfo<T extends TokenId> implements LookupListener {
        
        private volatile ChangeEvent changeEvent;
        
        private final Language<T> innerLanguage;
        
        private final String mimePath; // Can start with mimeTypeForOptions
    
        private final ListenerList<ChangeListener> listeners;

        private final Lookup.Result<FontColorSettings> result;
        
        private AttributeSet[] tokenId2attrs;
        
        FontColorSettings fcs;
        
        /**
         * @param innerLanguage
         * @param mimePath note it may start with mimeTypeForOptions
         */
        public FCSInfo(String mimePath, Language<T> innerLanguage) {
            this.innerLanguage = innerLanguage;
            this.mimePath = mimePath;
            this.listeners = new ListenerList<ChangeListener>();
            Lookup lookup = MimeLookup.getLookup("text/textmate");
            result = lookup.lookupResult(FontColorSettings.class);
            // Do not listen on font color settings changes in tests
            // since "random" lookup events translate into highlight change events
            // that are monitored by tests and so the tests may then fail
            if (TEST_FALLBACK_COLORING == null) {
                result.addLookupListener(WeakListeners.create(LookupListener.class, this, result));
            }
            updateFCS();
        }
        
        private static final Map<String, AttributeSet> scopeName2Coloring = new HashMap<>();

        /**
         * @param token non-null token.
         * @return attributes for tokenId or null if none found.
         */
        synchronized AttributeSet findAttrs(Token<?> token) {
            if (token.id() != TextmateTokenId.TEXTMATE) {
                return null;
            }
            
            List<AttributeSet> attrs = new ArrayList<>();
            
            // Warning removal requires Token class changes
            @SuppressWarnings("unchecked")
            List<String> categories  = (List<String>)token.getProperty("categories");
            
            for (String category : categories) {
                if (category.startsWith("meta.embedded")) {
                    attrs.clear();
                } else {
                    attrs.add(scopeName2Coloring.computeIfAbsent(category, c -> {
                        String cat = category;

                        while (true) {
                            AttributeSet currentAttrs = fcs.getTokenFontColors(cat);

                            if (currentAttrs != null) {
                                return currentAttrs;
                            }

                            int dot = cat.lastIndexOf('.');

                            if (dot == (-1))
                                break;

                            cat = cat.substring(0, dot);
                        }

                        return SimpleAttributeSet.EMPTY;
                    }));
                }
            }

            return AttributesUtilities.createComposite(attrs.toArray(new AttributeSet[0]));
        }

        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void updateFCS() {
            FontColorSettings newFCS = result.allInstances().iterator().next();
            if (newFCS == null && LOG.isLoggable(Level.WARNING)) {
                // Should not normally happen; see #106337
                LOG.warning("No FontColorSettings for '" + mimePath + "' mime path."); //NOI18N
            }
            synchronized (this) {
                fcs = newFCS;
                if (innerLanguage != null) {
                    tokenId2attrs = new AttributeSet[innerLanguage.maxOrdinal() + 1];
                }
            }
        }
        
        private ChangeEvent createChangeEvent() {
            if (changeEvent == null) {
                changeEvent = new ChangeEvent(this);
            }
            return changeEvent;
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            updateFCS();
            ChangeEvent e = createChangeEvent();
            for (ChangeListener l : listeners.getListeners()) {
                l.stateChanged(e);
            }
        }
   
    }
    
    private final class TSInfo<T extends TokenId> {
        
        final TokenSequence<T> ts;
        
        final FCSInfo<T> fcsInfo;
        
        int tokenOffset;
        
        int tokenEndOffset;
        
        AttributeSet tokenAttrs;
        
        /**
         * @param ts
         */
        public TSInfo(TokenSequence<T> ts) {
            this.ts = ts;
            LanguagePath languagePath = ts.languagePath();
            @SuppressWarnings("unchecked")
            Language<T> innerLanguage = (Language<T>)languagePath.innerLanguage();
            String mimePathExt;
            if (mimeTypeForOptions != null) {
                // First mime-type in mimePath starts with "test"
                mimePathExt = languagePathToMimePathOptions(languagePath);
            } else {
                mimePathExt = languagePath.mimePath();
            }

            fcsInfo = findFCSInfo(mimePathExt, innerLanguage);
        }
        
        boolean moveNextToken(int limitStartOffset, int limitEndOffset) {
            if (ts.moveNext()) {
                Token<T> token = ts.token();
                int nextTokenOffset = ts.offset();
                if (nextTokenOffset < 0) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Invalid token offset=" + nextTokenOffset + " < 0. TokenSequence:\n" + ts); // NOI18N
                    }
                    return false;
                }
                int nextTokenLength = token.length();
                if (nextTokenOffset < 0) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Invalid token length=" + nextTokenLength + " < 0. TokenSequence:\n" + ts); // NOI18N
                    }
                    return false;
                }
                if (nextTokenOffset >= tokenEndOffset || nextTokenLength >= 0) {
                    tokenOffset = nextTokenOffset;
                    tokenEndOffset = tokenOffset + nextTokenLength;
                } else {
                    // Become robust against an invalid lexer's output by returning "no more tokens" here
                    return false;
                }
                if (tokenEndOffset <= limitStartOffset) {
                    // Must move the sequence forward by bin-search
                    ts.move(limitStartOffset);
                    if (!ts.moveNext()) { // limitStartOffset above tokens (in tail section)
                        return false;
                    }
                    token = ts.token();
                    tokenOffset = ts.offset();
                    tokenEndOffset = tokenOffset + token.length();
                }
                tokenOffset = Math.max(tokenOffset, limitStartOffset);
                T id = token.id();
                if (tokenEndOffset > limitEndOffset) {
                    if (tokenOffset >= limitEndOffset) {
                        return false;
                    } else {
                        tokenEndOffset = limitEndOffset;
                    }
                }
                tokenAttrs = fcsInfo.findAttrs(token);

                if (LOG.isLoggable(Level.FINE)) {
                    // Add token info to the tooltip
                    tokenAttrs = AttributesUtilities.createComposite(
                            AttributesUtilities.createImmutable(EditorStyleConstants.Tooltip,
                            "<html>" //NOI18N
                            + "<b>Token:</b> " + token.text() //NOI18N
                            + "<br><b>Id:</b> " + id.name() //NOI18N
                            + "<br><b>Category:</b> " + id.primaryCategory() //NOI18N
                            + "<br><b>Ordinal:</b> " + id.ordinal() //NOI18N
                            + "<br><b>Mimepath:</b> " + ts.languagePath().mimePath() //NOI18N
                            ),
                            tokenAttrs);
                }
                return true;
            } else {
                tokenOffset = tokenEndOffset;
                return false;
            }
        }

    }

    @MimeRegistration(service=HighlightsLayerFactory.class, mimeType="")
    public static class FactoryImpl implements HighlightsLayerFactory {

        @Override
        public HighlightsLayer[] createLayers(Context ctx) {
            Document doc = ctx.getDocument();
            TokenHierarchy<Document> th = TokenHierarchy.get(doc);

            if (th == null) {
                return new HighlightsLayer[0];
            }
            //check the token hierarchy produces the TextmateTokens
            return new HighlightsLayer[] {
                HighlightsLayer.create(SyntaxHighlighting.class.getName(),
                                       ZOrder.SYNTAX_RACK,
                                       true,
                                       new SyntaxHighlighting(doc))
            };
        }
        
    }
}
