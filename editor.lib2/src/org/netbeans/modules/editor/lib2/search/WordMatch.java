/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.search;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.EditorPreferencesKeys;
import org.netbeans.modules.editor.lib2.document.DocumentCharacterAcceptor;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 * Word matching support enables to fill in the rest of the word when knowing
 * the begining of the word. It is capable to search either only in current file
 * or also in several or all open files.
 *
 * @author Miloslav Metelka
 */
public final class WordMatch {
    
    // -J-Dorg.netbeans.modules.editor.lib2.search.WordMatch.level=FINE
    private static final Logger LOG = Logger.getLogger(WordMatch.class.getName());
    
    public static synchronized WordMatch get(Document doc) {
        WordMatch wordMatch = (WordMatch) doc.getProperty(WordMatch.class);
        if (wordMatch == null) {
            wordMatch = new WordMatch(doc);
        }
        return wordMatch;
    }

    /**
     * Document for which this word match is constructed.
     */
    private final Document doc;

    /**
     * List of currently opened components.
     */
    private List<Reference<Document>> documents = new ArrayList<Reference<Document>>();
    
    private int documentIndex = -1;

    private Map<Document, Boolean> documentSet = new WeakHashMap<Document, Boolean>();
    
    /**
     * First part of matching word. Status of word matching
     * support can be tested by looking if this variable is null. If it is, word
     * matching was reset and it's not initialized yet.
     */
    private WordInfo baseWordInfo;

    /**
     * HashMap for already matched words
     */
    private final TextStorageSet wordSet = new TextStorageSet();

    /**
     * List holding already found words in the order they are found.
     */
    private final List<WordInfo> wordInfoList = new GapList<WordInfo>(4);

    /**
     * Current index in word list.
     */
    private int wordInfoListIndex;
    
    /**
     * Search with case matching
     */
    private boolean matchCase;

    /**
     * Search using smart case
     */
    private boolean smartCase;

    /**
     * This is the flag that really says whether the search is matching case or
     * not. The value is (smartCase ? (is-there-capital-in-base-word?) :
     * matchCase).
     */
    private boolean realMatchCase;

    private Preferences prefs = null;

    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            matchCase = prefs.getBoolean(EditorPreferencesKeys.WORD_MATCH_MATCH_CASE, EditorPreferencesDefaults.defaultWordMatchMatchCase);
            smartCase = prefs.getBoolean(EditorPreferencesKeys.WORD_MATCH_SMART_CASE, EditorPreferencesDefaults.defaultWordMatchSmartCase);
        }
    };
    private PreferenceChangeListener weakListener = null;

    private WordMatch(Document doc) {
        this.doc = doc;
    }

    private void checkInitPrefs() {
        if (weakListener == null) {
            String mimeType = org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(doc);
            if (mimeType != null) {
                prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
                weakListener = WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs);
                prefs.addPreferenceChangeListener(weakListener);
                prefsListener.preferenceChange(null);
            }
        }
    }

    /**
     * Reset word matching, so that it forgets current word list.
     */
    public synchronized void reset() {
        if (baseWordInfo != null) {
            baseWordInfo = null;
            documents.clear();
            documentIndex = -1;
            documentSet.clear();
            wordSet.clear();
            wordInfoList.clear();
            wordInfoListIndex = 0;
        }
    }

    /**
     * Find next matching word and replace it on current cursor position
     *
     * @param forward in which direction should the search be done
     */
    public synchronized void matchWord(int caretOffset, boolean forward) {
        // Initialize base word if necessary
        int baseWordStartOffset;
        int baseWordEndOffset;
        if (baseWordInfo == null) {
            checkInitPrefs();
            // [TODO] replace with EditorDocumentUtils.getIdentifierEnd()
            int identStartOffset = org.netbeans.modules.editor.lib2.document.EditorDocumentHandler.getIdentifierEnd(doc, caretOffset, true);
            CharSequence docText = DocumentUtilities.getText(doc);
            String baseWord = docText.subSequence(identStartOffset, caretOffset).toString();
            baseWordStartOffset = identStartOffset;
            baseWordEndOffset = caretOffset;
            if (smartCase && !matchCase) {
                realMatchCase = false;
                for (int i = baseWord.length() - 1; i >= 0; i--) {
                    if (Character.isUpperCase(baseWord.charAt(i))) {
                        realMatchCase = true;
                    }
                }
            } else {
                realMatchCase = matchCase;
            }
            // make lowercase if not matching case
            if (!realMatchCase) {
                baseWord = baseWord.toLowerCase();
            }
            baseWordInfo = new WordInfo(baseWord);
            baseWordInfo.pos = createPosition(doc, identStartOffset);
            wordSet.add(baseWord);
            wordInfoList.add(baseWordInfo);
        } else {
            baseWordStartOffset = baseWordInfo.pos.getOffset();
            baseWordEndOffset = baseWordStartOffset + baseWordInfo.word.length();
        }

        // Decide whether search next word or use existing WordInfo entries
        // For an initial case wordInfos already has 1 item and wordInfosIndex == 0.
        WordInfo origWordInfo = wordInfoList.get(wordInfoListIndex);
        WordInfo wordInfo = null;
        boolean searchWord = (forward ? wordInfoListIndex == wordInfoList.size() - 1 : wordInfoListIndex == 0);
        if (searchWord) {
            Document d = null;
            int searchStartOffset = 0;
            int searchEndOffset = 0;
            // Whether searching initial part of document (above baseword for forward search or below baseword
            // for backward search) or whether searching the rest of document area.
            boolean searchRest = false;
            CharSequence docText = null;
            if (documentSet.isEmpty()) { // getNextDocument() not called yet (after clear())
                d = doc;
                docText = DocumentUtilities.getText(d);
                int offset = origWordInfo.pos.getOffset();
                if (forward) {
                    int endOffset = offset + origWordInfo.word.length();
                    if (offset >= baseWordStartOffset) {
                        searchStartOffset = endOffset;
                        searchEndOffset = docText.length();
                    } else {
                        searchStartOffset = endOffset;
                        searchEndOffset = baseWordStartOffset;
                        searchRest = true;
                    }
                } else { // Backward search
                    if (offset <= baseWordStartOffset) {
                        searchStartOffset = 0;
                        searchEndOffset = offset;
                    } else {
                        searchStartOffset = baseWordEndOffset;
                        searchEndOffset = offset;
                        searchRest = true;
                    }
                }
            }
            DocumentCharacterAcceptor charAcceptor = DocumentCharacterAcceptor.get(doc);

            // Find word(s).
            // For current document search for next occurrence for other documents search for all
            // words in that doc at once.
            while (true) {
                if (d == null) {
                    d = getNextDocument();
                    if (d == null) {
                        break; // Nothing found word == null
                    }
                    docText = DocumentUtilities.getText(d);
                    searchStartOffset = 0;
                    searchEndOffset = docText.length();
                }

                int wordStartOffset = -1;
                int wordEndOffset = -1;
                if (forward || d != doc) {
                    while (searchStartOffset < searchEndOffset) {
                        char ch = docText.charAt(searchStartOffset++);
                        if (charAcceptor.isIdentifier(ch)) {
                            if (wordStartOffset == -1) {
                                wordStartOffset = searchStartOffset - 1;
                            }
                            wordEndOffset = searchStartOffset;
                        } else {
                            if (wordStartOffset != -1) {
                                break;
                            }
                        }
                    }

                } else { // Backward search
                    while (searchStartOffset < searchEndOffset) {
                        char ch = docText.charAt(--searchEndOffset);
                        if (charAcceptor.isIdentifier(ch)) {
                            if (wordEndOffset == -1) {
                                wordEndOffset = searchEndOffset + 1;
                            }
                            wordStartOffset = searchEndOffset;
                        } else {
                            if (wordStartOffset != -1) {
                                break;
                            }
                        }
                    }
                }
                
                if (wordStartOffset != -1) {
                    if (checkWord(docText, wordStartOffset, wordEndOffset - wordStartOffset)) {
                        String word = docText.subSequence(wordStartOffset, wordEndOffset).toString();
                        wordInfo = new WordInfo(word);
                        wordInfo.pos = createPosition(doc, wordStartOffset);
                        wordSet.add(word);
                        if (forward) {
                            wordInfoList.add(wordInfo);
                        } else {
                            wordInfoList.add(0, wordInfo);
                            wordInfoListIndex++;
                        }
                        // Stop when word found in first doc but search all words at once for other docs
                        if (d == doc) {
                            break;
                        }
                    }
                } else { // No word found
                    if (d == doc) {
                        if (forward) {
                            if (searchRest) {
                                d = null;
                            } else {
                                searchStartOffset = 0;
                                searchEndOffset = baseWordStartOffset;
                                searchRest = true;
                            }
                        } else { // Backward search
                            if (searchRest) {
                                d = null;
                            } else {
                                searchStartOffset = baseWordEndOffset;
                                searchEndOffset = docText.length();
                                searchRest = true;
                            }
                        }
                    } else { // Searching in other document
                        if (wordInfo != null) { // Break if found at least one word in this doc
                            break;
                        }
                        d = null; // Fetch next doc if possible
                    }
                }
            }
        }
        if (!searchWord || wordInfo != null) {
            if (forward) {
                wordInfoListIndex++;
            } else {
                wordInfoListIndex--;
            }
        }
        wordInfo = wordInfoList.get(wordInfoListIndex);
        
        // Replace word
        if (wordInfo != null && wordInfo != origWordInfo) {
            try {
                int offset = baseWordStartOffset;
                int len = origWordInfo.word.length();
                if (doc.getLength() >= offset + len) {
                    String origWord = doc.getText(offset, len);
                    if (origWord.equals(origWordInfo.word)) {
                        doc.remove(offset, len);
                        doc.insertString(offset, wordInfo.word, null);
                        baseWordInfo.pos = doc.createPosition(offset);
                    } else {
                        LOG.info("Cannot replace word: origWord=\"" + CharSequenceUtilities.debugText(origWord) + // NOI18N
                                "\" != \"" + CharSequenceUtilities.debugText(origWordInfo.word) + "\"\n"); // NOI18N
                    }
                } else {
                    LOG.info("Cannot replace word: offset=" + offset + ", len=" + len + // NOI18N
                            ", docLen=" + doc.getLength() + '\n'); // NOI18N
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private Position createPosition(Document doc, int offset) {
        try {
            return doc.createPosition(offset);
        } catch (BadLocationException ex) {
            throw new IndexOutOfBoundsException("Position creation failed at offset=" + offset + // NOI18N
                    ", doc=" + doc + "\n" + ex.getLocalizedMessage()); // NOI18N
        }
    }

    private boolean checkWord(CharSequence text, int index, int wordLen) {
        String baseWord = baseWordInfo.word;
        int baseWordLen = baseWord.length();
        if (baseWordLen > 0) {
            if (wordLen < baseWordLen) {
                return false;
            }
            for (int i = 0; i < baseWordLen; i++) {
                char ch = text.charAt(index + i);
                if (realMatchCase) {
                    if (ch != baseWord.charAt(i)) {
                        return false;
                    }
                } else { // case-insensitive (baseWord already in lowercase)
                    if (Character.toLowerCase(ch) != baseWord.charAt(i)) {
                        return false;
                    }
                }
            }
        }
        // check existing words
        if (wordSet.get(text, index, index + wordLen) != null) {
            return false;
        }
        return true; // new word found
    }

    private Document getNextDocument() {
        // Initially documentIndex == -1
        if (documentIndex == documents.size() - 1) { // Check adding
            if (documentSet.isEmpty()) { // documents list not inited yet -> add 'doc'
                documentSet.put(doc, Boolean.TRUE);
            }
            for (JTextComponent tc : EditorRegistry.componentList()) {
                Document d = tc.getDocument();
                if (!documentSet.containsKey(d)) {
                    documentSet.put(d, Boolean.TRUE);
                    documents.add(new WeakReference<Document>(d));
                }
            }
        }
        Document retDoc = null;
        while (documentIndex < documents.size() - 1) {
            documentIndex++;
            retDoc = documents.get(documentIndex).get();
            if (retDoc != null) {
                break;
            }
        }
        return retDoc;
    }

    public @Override
    String toString() {
        return "baseWordInfo=" + baseWordInfo + // NOI18N
                ", matchCase=" + matchCase + ", smartCase=" + smartCase + ", realMatchCase=" + realMatchCase + // NOI18N
                ", wordSet=" + wordSet + "\nwordInfoList=" + wordInfoList + // NOI18N
                "\nwordInfoListIndex=" + wordInfoListIndex; // NOI18N
    }

    private static final class WordInfo {

        public WordInfo(String word) {
            this.word = word;
        }
        
        final String word;
        
        Position pos;

        @Override
        public String toString() {
            return "word=\"" + CharSequenceUtilities.debugText(word) + "\", pos=" + pos; // NOI18N
        }
        
    }

}
