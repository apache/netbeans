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

package org.netbeans.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.Position;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.EditorPreferencesKeys;
import org.openide.util.WeakListeners;

/** Word matching support enables to fill in the rest of the word
* when knowing the begining of the word. It is capable to search either
* only in current file or also in several or all open files.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class WordMatch extends FinderFactory.AbstractFinder implements PropertyChangeListener {

    private static final Object NULL_DOC = new Object();

    /** Mapping of kit class to document with the static word */
    private static final HashMap staticWordsDocs = new HashMap();

    /** First part of matching word expressed as char[].
    * Status of word matching support
    * can be tested by looking if this variable is null. If it is,
    * word matching was reset and it's not initialized yet.
    */
    char[] baseWord;

    /** Found characters are accumulated here */
    char[] word = new char[20];

    /** Last word returned */
    String lastWord;

    /** Previous word returned */
    String previousWord;

    /** Current index in word */
    int wordLen;

    /** HashMap for already matched words */
    StringMap wordsMap = new StringMap();

    /** ArrayList holding already found words and their positions. */
    ArrayList wordInfoList = new ArrayList();

    /** Current index in word match vector. Reaching either first or last
    * index of vector means searching backward or forward respectively from
    * position stored in previous vector's element.
    */
    int wordsIndex;

    /** Current search direction */
    boolean forwardSearch;

    /** Pointer to editorUI instance */
    EditorUI editorUI;

    /** Whether the search should be wrapped */
    boolean wrapSearch;

    /** Search with case matching */
    boolean matchCase;

    /** Search using smart case */
    boolean smartCase;

    /** This is the flag that really says whether the search is matching case
    * or not. The value is (smartCase ? (is-there-capital-in-base-word?) : matchCase).
    */
    boolean realMatchCase;

    /** Whether the match should be reported when word is found
    * which is only one char long.
    */
    boolean matchOneChar;

    /** Maximum lenght in chars of the search area.
    * If the number is zero, no search is performed except the static words.
    */
    int maxSearchLen;

    /** Document where to start from */
    BaseDocument startDoc;

    private Preferences prefs = null;
    private final PreferenceChangeListener prefsListener = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt != null) { // real change event
                staticWordsDocs.clear();
            }
            maxSearchLen = prefs.getInt(EditorPreferencesKeys.WORD_MATCH_SEARCH_LEN, EditorPreferencesDefaults.defaultWordMatchSearchLen);
            wrapSearch = prefs.getBoolean(EditorPreferencesKeys.WORD_MATCH_WRAP_SEARCH, EditorPreferencesDefaults.defaultWordMatchWrapSearch);
            matchOneChar = prefs.getBoolean(EditorPreferencesKeys.WORD_MATCH_MATCH_ONE_CHAR, EditorPreferencesDefaults.defaultWordMatchMatchOneChar);
            matchCase = prefs.getBoolean(EditorPreferencesKeys.WORD_MATCH_MATCH_CASE, EditorPreferencesDefaults.defaultWordMatchMatchCase);
            smartCase = prefs.getBoolean(EditorPreferencesKeys.WORD_MATCH_SMART_CASE, EditorPreferencesDefaults.defaultWordMatchSmartCase);
        }
    };
    private PreferenceChangeListener weakListener = null;
    
    /** Construct new word match over given view manager */
    public WordMatch(EditorUI editorUI) {
        this.editorUI = editorUI;

        synchronized (editorUI.getComponentLock()) {
            // if component already installed in EditorUI simulate installation
            JTextComponent component = editorUI.getComponent();
            if (component != null) {
                propertyChange(new PropertyChangeEvent(editorUI, EditorUI.COMPONENT_PROPERTY, null, component));
            }

            editorUI.addPropertyChangeListener(this);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
            if (prefs != null && weakListener != null) {
                prefs.removePreferenceChangeListener(weakListener);
            }
                
            JTextComponent newC = (JTextComponent)evt.getNewValue();
            if (newC != null) { // just installed
                String mimeType = org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(newC);
                prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
                weakListener = WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs);
                prefs.addPreferenceChangeListener(weakListener);
                prefsListener.preferenceChange(null);
            } else { // just deinstalled

            }
        }
    }

    /** Clear word matching, so that it forgots the remembered
    * matching words.
    */
    public synchronized void clear() {
        if (baseWord != null) {
            baseWord = null;
            wordsMap.clear();
            wordInfoList.clear();
            wordsIndex = 0;
        }
    }

    /** Reset this finder before each search */
    public @Override void reset() {
        super.reset();
        wordLen = 0;
    }

    /** Find next matching word and replace it on current cursor position
    * @param forward in which direction should the search be done
    */
    public synchronized String getMatchWord(int startPos, boolean forward) {
        int listSize = wordInfoList.size();
        boolean searchNext = (listSize == 0)
                             || (wordsIndex == (forward ? (listSize - 1) : 0));
        startDoc = (BaseDocument)editorUI.getComponent().getDocument();
        String ret = null;

        // initialize base word if necessary
        if (baseWord == null) {
            try {
                String baseWordString = Utilities.getIdentifierBefore(startDoc, startPos);
                if (baseWordString == null) {
                    baseWordString = ""; // NOI18N
                }
                lastWord = baseWordString;
                baseWord = baseWordString.toCharArray();

                WordInfo info = new WordInfo(baseWordString,
                                             startDoc.createPosition(startPos - baseWord.length), startDoc);
                wordsMap.put(info.word, info);
                wordInfoList.add(info);
            } catch (BadLocationException e) {
                Utilities.annotateLoggable(e);
            }
            if (smartCase && !matchCase) {
                realMatchCase = false;
                for (int i = 0; i < baseWord.length; i++) {
                    if (Character.isUpperCase(baseWord[i])) {
                        realMatchCase = true;
                    }
                }
            } else {
                realMatchCase = matchCase;
            }
            // make lowercase if not matching case
            if (!realMatchCase) {
                for (int i = 0; i < baseWord.length; i++) {
                    baseWord[i] = Character.toLowerCase(baseWord[i]);
                }
            }
        }

        // possibly search next word
        if (searchNext) {
            try {
                // determine start document and position
                BaseDocument doc; // actual document
                int pos; // actual position
                if (listSize > 0) {
                    WordInfo info = (WordInfo)wordInfoList.get(wordsIndex);
                    doc = info.doc;
                    pos = info.pos.getOffset();
                    if (forward) {
                        pos += info.word.length();
                    }
                } else {
                    doc = startDoc;
                    pos = startPos;
                }

                // search for next occurence
                while (doc != null) {
                    if (doc.getLength() > 0) {
                        int endPos;
                        if (doc == startDoc) {
                            if (forward) {
                                endPos = (pos >= startPos) ? -1 : startPos;
                            } else { // bwd
                                endPos = (pos == -1 || pos > startPos) ? startPos : 0;
                            }
                        } else { // not starting doc
                            endPos = -1;
                        }

                        this.forwardSearch = !(!forward && (doc == startDoc));
                        int foundPos = doc.find(this, pos, endPos);
                        if (foundPos != -1) { // found
                            if (forward) {
                                wordsIndex++;
                            }
                            WordInfo info = new WordInfo(new String(word, 0, wordLen),
                                                         doc.createPosition(foundPos), doc);
                            wordsMap.put(info.word, info);
                            wordInfoList.add(wordsIndex, info);
                            previousWord = lastWord;
                            lastWord = info.word;
                            return lastWord;
                        }
                        if (doc == startDoc) {
                            if (forward) {
                                pos = 0;
                                if (endPos != -1 || !wrapSearch) {
                                    doc = getNextDoc(doc);
                                }
                            } else { // bwd
                                if (pos == -1 || !wrapSearch) {
                                    doc = getNextDoc(doc);
                                    pos = 0;
                                } else {
                                    pos = -1; // stay on the same document
                                }
                            }
                        } else { // not starting doc
                            doc = getNextDoc(doc);
                            pos = 0;
                        }
                    } else { // empty document
                        doc = getNextDoc(doc);
                        pos = 0; // should be anyway
                    }
                }
                // Return null in this case
            } catch (BadLocationException e) {
                Utilities.annotateLoggable(e);
            }
        } else { // use word from the list
            wordsIndex += (forward ? 1 : -1);
            previousWord = lastWord;
            lastWord = ((WordInfo)wordInfoList.get(wordsIndex)).word;
            ret = lastWord;
        }

        startDoc = null;
        return ret;
    }

    public String getPreviousWord() {
        return previousWord;
    }

    private void doubleWordSize() {
        char[] tmp = new char[word.length * 2];
        System.arraycopy(word, 0, tmp, 0, word.length);
        word = tmp;
    }

    private boolean checkWord() {
        // check matching of one-char string
        if (!matchOneChar && wordLen == 1) {
            return false;
        }

        // check word start
        if (baseWord.length > 0) {
            if (wordLen < baseWord.length) {
                return false;
            }
            for (int i = 0; i < baseWord.length; i++) {
                if (realMatchCase) {
                    if (word[i] != baseWord[i]) {
                        return false;
                    }
                } else { // case-insensitive
                    if (Character.toLowerCase(word[i]) != baseWord[i]) {
                        return false;
                    }
                }
            }
        }

        // check existing words
        if (wordsMap.containsKey(word, 0, wordLen)) {
            return false;
        }
        return true; // new word found
    }


    public int find(int bufferStartPos, char buffer[],
                    int offset1, int offset2, int reqPos, int limitPos) {
        int offset = reqPos - bufferStartPos;
        if (forwardSearch) {
            int limitOffset = limitPos - bufferStartPos - 1;
            while (offset < offset2) {
                char ch = buffer[offset];
                boolean wp = startDoc.isIdentifierPart(ch);
                if (wp) { // append the char
                    if (wordLen == word.length) {
                        doubleWordSize();
                    }
                    word[wordLen++] = ch;
                }

                if (!wp) {
                    if (wordLen > 0) {
                        if (checkWord()) {
                            found = true;
                            return bufferStartPos + offset - wordLen;
                            
                        } else {
                            wordLen = 0;
                        }
                    }

                } else { // current char is word part
                    if (limitOffset == offset) {
                        if (checkWord()) {
                            found = true;
                            // differs in one char because current is part of word
                            return bufferStartPos + offset - wordLen + 1;

                        } else {
                            wordLen = 0;
                        }
                    }
                }

                offset++;
            }
        } else { // bwd search
            int limitOffset = limitPos - bufferStartPos;
            while (offset >= offset1) {
                char ch = buffer[offset];
                boolean wp = startDoc.isIdentifierPart(ch);
                if (wp) {
                    if (wordLen == word.length) {
                        doubleWordSize();
                    }
                    word[wordLen++] = ch;
                }
                if (!wp || (limitOffset == offset)) {
                    if (wordLen > 0) {
                        Analyzer.reverse(word, wordLen); // reverse word chars
                        if (checkWord()) {
                            found = true;
                            return (wp) ? bufferStartPos + offset + 1
                                : bufferStartPos + offset;
                        } else {
                            wordLen = 0;
                        }
                    }
                }
                offset--;
            }
        }
        return bufferStartPos + offset;
    }


    private BaseDocument getNextDoc(BaseDocument doc) {
        if (doc == getStaticWordsDoc()) {
            return null;
        }
        BaseDocument nextDoc = null;
        Set<BaseDocument> list = new LinkedHashSet<BaseDocument>();
        for(JTextComponent jtc : EditorRegistry.componentList()) {
            list.add(Utilities.getDocument(jtc));
        }
        for(Iterator<? extends BaseDocument> i = list.iterator(); i.hasNext(); ) {
            if (doc == i.next()) {
                if (i.hasNext()) {
                    nextDoc = i.next();
                }
                break;
            }
        }
        if (nextDoc == null) {
            nextDoc = getStaticWordsDoc();
        }
        return nextDoc;
    }

    private BaseDocument getStaticWordsDoc() {
        Class kitClass = Utilities.getKitClass(editorUI.getComponent());
        Object val = staticWordsDocs.get(kitClass);
        if (val == NULL_DOC) {
            return null;
        }
        BaseDocument doc = (BaseDocument)val;
        if (doc == null && prefs != null) {
            String staticWords = prefs.get(EditorPreferencesKeys.WORD_MATCH_STATIC_WORDS, null);
            if (staticWords != null) {
                doc = new BaseDocument(BaseKit.class, false); // don't add to registry
                try {
                    doc.insertString(0, staticWords, null);
                } catch (BadLocationException e) {
                    Utilities.annotateLoggable(e);
                }
                staticWordsDocs.put(kitClass, doc);
            } else { // null static words
                staticWordsDocs.put(kitClass, NULL_DOC);
            }
        }
        return doc;
    }

    /** Word match info - used in previous/next word matching.
    * It contains info found word and next matching position.
    */
    private static final class WordInfo {

        public WordInfo(String word, Position pos, BaseDocument doc) {
            this.word = word;
            this.pos = pos;
            this.doc = doc;
        }

        /** Found word */
        String word;

        /** Position of the word in document.
        * Positions are used so that the marks are removed
        * when they are no longer necessary.
        */
        Position pos;

        /** Document where the word resides */
        BaseDocument doc;

        public @Override boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof WordMatch) {
                WordMatch wm = (WordMatch)o;
                return Analyzer.equals(word, wm.word, 0, wm.wordLen);
            }
            if (o instanceof WordInfo) {
                return word.equals(((WordInfo)o).word);
            }
            if (o instanceof String) {
                return word.equals(o);
            }
            return false;
        }

        public @Override int hashCode() {
            return word.hashCode();
        }

        public @Override String toString() {
            return "{word='" + word + "', pos=" + pos.getOffset() // NOI18N
                   + ", doc=" + doc + "}"; // NOI18N
        }

    } // End of WordInfo class

    public @Override String toString() {
        return "baseWord=" + ((baseWord != null) ? ("'" + baseWord.toString() + "'") // NOI18N
                              : "null") + ", wrapSearch=" + wrapSearch // NOI18N
               + ", matchCase=" + matchCase + ", smartCase=" + smartCase // NOI18N
               + ", matchOneChar=" + matchOneChar + ", maxSearchLen=" + maxSearchLen // NOI18N
               + ", wordsMap=" + wordsMap + "\nwordInfoList=" + wordInfoList // NOI18N
               + "\nwordsIndex=" + wordsIndex; // NOI18N
    }

}
