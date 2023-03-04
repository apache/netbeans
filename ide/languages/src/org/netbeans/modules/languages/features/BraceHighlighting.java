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

package org.netbeans.modules.languages.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;

/**
 *
 * @author Dan Prusa, Vita Stejskal
 */
public class BraceHighlighting implements BracesMatcher, BracesMatcherFactory {

    public BraceHighlighting(String topLevelMimeType) {
        this(topLevelMimeType, null);
    }

    public BraceHighlighting(String topLevelMimeType, MatcherContext context) {
        this.topLevelMimeType = topLevelMimeType;
        this.context = context;
    }

    // --------------------------------------------
    // BracesMatcher implementation
    // --------------------------------------------

    public int[] findOrigin() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            Language language = null;
            try {
                language = LanguagesManager.getDefault().getLanguage(topLevelMimeType);
            } catch (LanguageDefinitionNotFoundException e) {
                // ignore, handled later
            }
            TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());

            if (language == null || th == null) {
                // ?? no lexer for the language, all Schliemann languages should have
                // a lexer
                return defaultFindOrigin(context);
            }

            int caretOffset = context.getSearchOffset();
            boolean searchBack = context.isSearchingBackward();
            List<TokenSequence<?>> sequences = th.embeddedTokenSequences(caretOffset, searchBack);

            for(int i = sequences.size() - 1; i >= 0; i--) {
                TokenSequence<?> ts = sequences.get(i);
                /** @todo can ts.language() equals language ?*/
                //if (ts.language().equals(language)) {
                if (ts.language().mimeType().equals(language.getMimeType())) {
                    seq = ts;
                    if (i > 0) {
                        TokenSequence<?> outerSeq = sequences.get(i - 1);
                        seqStart = outerSeq.offset();
                        seqEnd = outerSeq.offset() + outerSeq.token().length();
                    } else {
                        // seq is the top level sequence, ie the whole document is just javadoc
                        seqStart = 0;
                        seqEnd = context.getDocument().getLength();
                    }
                    break;
                }
            }

            if (seq == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("TokenSequence with wrong language " + language); //NOI18N
                }
                return null;
            }

            Map<String, Set<String>>[] pairsMaps = getPairsMap(language);
            if (pairsMaps == null) {
                return defaultFindOrigin(context);
            }

            seq.move(caretOffset);
            if (seq.moveNext()) {
                boolean [] bckwrd = new boolean[1];

                String tokenText = seq.token().text().toString();
                String trimedTokenText = tokenText.trim();
                if (isOrigin(pairsMaps, trimedTokenText, bckwrd)) {
                    if (seq.offset() < caretOffset || !searchBack) {
                        originText = trimedTokenText;
                        backwards = bckwrd[0];
                        pairsMap = backwards ? pairsMaps[1] : pairsMaps[0];
                        int offset = seq.offset() + tokenText.indexOf(trimedTokenText);
                        int length = trimedTokenText.length();
                        return new int [] { offset, offset + length };
                    }
                }

                while(moveTheSequence(seq, searchBack, context.getLimitOffset())) {
                    tokenText = seq.token().text().toString();
                    trimedTokenText = tokenText.trim();
                    if (isOrigin(pairsMaps, trimedTokenText, bckwrd)) {
                        originText = trimedTokenText;
                        backwards = bckwrd[0];
                        pairsMap = backwards ? pairsMaps[1] : pairsMaps[0];
                        int offset = seq.offset() + tokenText.indexOf(trimedTokenText);
                        int length = trimedTokenText.length();
                        return new int [] { offset, offset + length };
                    }
                }
            }

            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    public int[] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            // Use the default matcher if no better was available
            if (defaultMatcher != null) {
                return defaultMatcher.findMatches();
            }

            // Proper matching using the pairs supplied by the language definition
            assert seq != null : "No token sequence"; //NOI18N

            List<String> unresolved = new ArrayList<String>();
            unresolved.add(originText);
            while(moveTheSequence(seq, backwards, -1)) {
                String tokenText = seq.token().text().toString();
                String trimedTokenText = tokenText.trim();
                int depth = unresolved.size() - 1;
                String currentOrigin = unresolved.get(depth);
                Set<String> matchingTexts = pairsMap.get(currentOrigin);
                if (matchingTexts != null && matchingTexts.contains(trimedTokenText)) {
                    unresolved.remove(depth);
                    if (unresolved.size() == 0) {                    
                        int offset = seq.offset() + tokenText.indexOf(trimedTokenText);
                        int length = trimedTokenText.length();
                        return new int [] { offset, offset + length };
                    }
                } else if (pairsMap.containsKey(trimedTokenText)) {
                    unresolved.add(trimedTokenText);
                }
            }

            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }
    
    // --------------------------------------------
    // BracesMatcherFactory implementation
    // --------------------------------------------
    
    public BracesMatcher createMatcher(MatcherContext context) {
        return new BraceHighlighting(topLevelMimeType, context);
    }

    // --------------------------------------------
    // Private implementation
    // --------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(BraceHighlighting.class.getName());
    private static final Map<Language, Map<String, Set<String>>[]> PAIRS = new WeakHashMap<Language, Map<String, Set<String>>[]>();

    private static Map<String, Set<String>>[] getPairsMap(Language l) {
        if (!PAIRS.containsKey(l)) {
            Map<String, Set<String>> startToEnd = new HashMap<String, Set<String>>();
            Map<String, Set<String>> endToStart = new HashMap<String, Set<String>>();

            List<Feature> indents = l.getFeatureList().getFeatures("BRACE"); //NOI18N
            Iterator<Feature> it = indents.iterator();
            while (it.hasNext()) {
                Feature indent = it.next();
                String s = (String) indent.getValue ();
                int i = s.indexOf(':'); //NOI18N
                String start = s.substring(0, i);
                String end = s.substring(i + 1);
                Set<String> matchTextsEnd = startToEnd.get(start);
                if (matchTextsEnd == null) {
                    matchTextsEnd = new HashSet<String>();
                    startToEnd.put(start, matchTextsEnd);

                }
                matchTextsEnd.add(end);
                Set<String> matchTextsStart = endToStart.get(end); 
                if (matchTextsStart == null) {
                    matchTextsStart = new HashSet<String>();
                    endToStart.put(end, matchTextsStart);
                }
                matchTextsStart.add(start);
            }
            @SuppressWarnings("unchecked")
            Map<String, Set<String>> [] arr = new Map [] { startToEnd, endToStart };
            PAIRS.put(l, arr);
        }
        return PAIRS.get(l);
    }
    
    private static boolean moveTheSequence(TokenSequence<?> seq, boolean backward, int offsetLimit) {
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

    private static boolean isOrigin(Map<String, Set<String>>[] pairsMaps, String originText, boolean backwards[]) {
        Set<String> s = pairsMaps[0].get(originText);
        if (s != null && s.size() > 0) {
            backwards[0] = false;
            return true;
        } else {
            s = pairsMaps[1].get(originText);
            if (s != null && s.size() > 0) {
                backwards[0] = true;
                return true;
            }
        }
        return false;
    }
    
    private int [] defaultFindOrigin(MatcherContext context) throws InterruptedException, BadLocationException {
        defaultMatcher = BracesMatcherSupport.defaultMatcher(context, -1, -1);
        return defaultMatcher.findOrigin();
    }
    
    private final MatcherContext context;
    private final String topLevelMimeType;
    
    private TokenSequence<?> seq;
    private int seqStart;
    private int seqEnd;
    private String originText;
    private Map<String, Set<String>> pairsMap;
    private boolean backwards;
    private BracesMatcher defaultMatcher;
    
}
