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
package org.netbeans.modules.cnd.spellchecker.bindings;

import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * based on JavaTokenList
 */
public class CndTokenList implements TokenList {

    private Document doc;
    private boolean hidden = false;
    private String category;
    private Kind aKind = Kind.Comment;

    /** Creates a new instance of CndTokenList */
    public CndTokenList(Document doc) {
        this.doc = doc;
    }

    public void setStartOffset(int offset) {
        currentBlockText = null;
        currentOffsetInComment = (-1);
        this.startOffset = offset;
        this.nextBlockStart = offset;
        FileObject fileObject = FileUtil.getConfigFile("Spellcheckers/CndComments"); // NOI18N
        Boolean b = (Boolean) fileObject.getAttribute("Hidden");// NOI18N
        hidden = Boolean.TRUE.equals(b);
    }

    public int getCurrentWordStartOffset() {
        return currentWordOffset;
    }

    public CharSequence getCurrentWordText() {
        return currentWord;
    }

    public boolean nextWord() {
        if (hidden) {
            return false;
        }
        boolean hasNext = nextWordImpl();

        while (hasNext && (currentWordOffset + currentWord.length()) < startOffset) {
            hasNext = nextWordImpl();
        }

        return hasNext;
    }

    private int[] findNextComment() throws BadLocationException {
        final int[] out = new int[]{-1, -1};
        Runnable r = new Runnable() {
            @Override
            public void run() {
                TokenSequence<?> ts = CndLexerUtilities.getCppTokenSequence(doc, nextBlockStart, false, false);
                if (ts == null) {
                    ts = CndLexerUtilities.getFortranTokenSequence(doc, nextBlockStart);
                }
                if (ts != null) {
                    int diff = ts.move(nextBlockStart);
                    while (ts.moveNext()) {
                        category = ts.token().id().primaryCategory();
                        if (category != null) {
                            if (CppTokenId.COMMENT_CATEGORY.equals(category)
                                    || FortranTokenId.COMMENT_CATEGORY.equals(category)
                                    || CppTokenId.STRING_CATEGORY.equals(category)
                                    || FortranTokenId.STRING_CATEGORY.equals(category)) {
                                out[0] = ts.offset();
                                out[1] = ts.offset() + ts.token().length();
                                break;
                            }
                        }
                    }
                }
            }
        };
        doc.render(r);
        return out;
    }

    private void handleDoxygenTag(CharSequence tag) {
        if ("@see".contentEquals(tag) || "@throws".contentEquals(tag)) {// NOI18N
            //ignore next "word", possibly dotted and hashed
            Pair<CharSequence, Integer> data = wordBroker(currentBlockText, currentOffsetInComment, true, Kind.Doc);
            if (data != null) {
                currentOffsetInComment = data.b + data.a.length();
            }
            return;
        }

        if ("@param".contentEquals(tag)) {// NOI18N
            //ignore next word
            Pair<CharSequence, Integer> data = wordBroker(currentBlockText, currentOffsetInComment, false, Kind.Doc);
            if (data != null) {
                currentOffsetInComment = data.b + data.a.length();
            }
            return;
        }

        if ("@author".contentEquals(tag)) {// NOI18N
            //ignore everything till the end of the line:
            Pair<CharSequence, Integer> data = wordBroker(currentBlockText, currentOffsetInComment, false, Kind.Doc);

            while (data != null) {
                currentOffsetInComment = data.b + data.a.length();

                if ('\n' == data.a.charAt(0)) {// NOI18N
                    //continue
                    return;
                }

                data = wordBroker(currentBlockText, currentOffsetInComment, false, Kind.Doc);
            }

            return;
        }
    }

    private boolean nextWordImpl() {
        try {
            while (true) {
                if (currentBlockText == null) {
                    int[] span = findNextComment();

                    if (span[0] == (-1)) {
                        return false;
                    }
                    if (CppTokenId.STRING_CATEGORY.equals(category)) {
                        aKind = Kind.String;
                    } else {
                        aKind = Kind.Comment;
                    }

                    currentBlockStart = span[0];
                    currentBlockText = doc.getText(span[0], span[1] - span[0]);
                    currentOffsetInComment = 0;

                    nextBlockStart = span[1];
                }

                String pairTag = null;
                Pair<CharSequence, Integer> data = wordBroker(currentBlockText, currentOffsetInComment, false, aKind);

                while (data != null) {
                    currentOffsetInComment = data.b + data.a.length();

                    if (pairTag == null) {
                        if (Character.isLetter(data.a.charAt(0)) && !isIdentifierLike(data.a)) {
                            //TODO: check for identifiers:
                            currentWordOffset = currentBlockStart + data.b;
                            currentWord = data.a;
                            return true;
                        }

                        switch (data.a.charAt(0)) {
                            case '@':// NOI18N
                                handleDoxygenTag(data.a);
                                break;
                            case '<':// NOI18N
                                if (startsWith(data.a, "<a ")) {// NOI18N
                                    pairTag = "</a>";// NOI18N
                                }
                                if (startsWith(data.a, "<code>")) {// NOI18N
                                    pairTag = "</code>";// NOI18N
                                }
                                if (startsWith(data.a, "<pre>")) {// NOI18N
                                    pairTag = "</pre>";// NOI18N
                                }
                                break;
                            case '{':// NOI18N
                                pairTag = "}";// NOI18N
                                break;
                        }
                    } else {
                        if (pairTag.contentEquals(data.a)) {
                            pairTag = null;
                        }
                    }

                    data = wordBroker(currentBlockText, currentOffsetInComment, false, aKind);
                }

                currentBlockText = null;
            }
        } catch (BadLocationException e) {
            // skip
            return false;
        }
    }

    private static boolean startsWith(CharSequence where, String withWhat) {
        if (where.length() >= withWhat.length()) {
            return withWhat.contentEquals(where.subSequence(0, withWhat.length()));
        }

        return false;
    }

    static boolean isIdentifierLike(CharSequence s) {
        boolean hasCapitalsInside = false;
        int offset = 1;

        while (offset < s.length() && !hasCapitalsInside) {
            hasCapitalsInside |= Character.isUpperCase(s.charAt(offset));

            offset++;
        }

        return hasCapitalsInside;
    }
    private int currentBlockStart;
    private int nextBlockStart;
    private String currentBlockText;
    private int currentOffsetInComment;
    private int currentWordOffset;
    private CharSequence currentWord;
    private int startOffset;
    private static final Pattern commentPattern = Pattern.compile("/\\*\\*([^*]*(\\*[^/][^*]*)*)\\*/", Pattern.MULTILINE | Pattern.DOTALL); //NOI18N
    private static final Pattern wordPattern = Pattern.compile("[A-Za-z]+"); //NOI18N

    private boolean isLetter(char c) {
        return Character.isLetter(c) || c == '\'';
    }

    private Pair<CharSequence, Integer> wordBroker(CharSequence start, int offset, boolean treatSpecialCharactersAsLetterInsideWords, Kind kind) {
        int state = 0;
        int offsetStart = offset;

        while (start.length() > offset) {
            char current = start.charAt(offset);

            switch (state) {
                case 0:
                    if (isLetter(current)) {
                        if (Kind.String == kind &&
                            offset > 0 && start.charAt(offset-1) == '\\') {
                            break;
                        }
                        state = 1;
                        offsetStart = offset;
                        break;
                    }
                    if (current == '@' || current == '#') {// NOI18N
                        state = 2;
                        offsetStart = offset;
                        break;
                    }
                    if (current == '<') {// NOI18N
                        state = 3;
                        offsetStart = offset;
                        break;
                    }
                    if (current == '\n' || current == '}') {// NOI18N
                        return new Pair<CharSequence, Integer>(start.subSequence(offset, offset + 1), offset);
                    }
                    if (current == '{') {// NOI18N
                        state = 4;
                        offsetStart = offset;
                        break;
                    }
                    break;

                case 1:
                    if (!isLetter(current) && ((current != '.' && current != '#') || !treatSpecialCharactersAsLetterInsideWords)) {// NOI18N
                        return new Pair<CharSequence, Integer>(start.subSequence(offsetStart, offset), offsetStart);
                    }

                    break;

                case 2:
                    if (!isLetter(current)) {
                        return new Pair<CharSequence, Integer>(start.subSequence(offsetStart, offset), offsetStart);
                    }

                    break;

                case 3:
                    if (current == '>') {// NOI18N
                        return new Pair<CharSequence, Integer>(start.subSequence(offsetStart, offset + 1), offsetStart);
                    }

                    break;

                case 4:
                    if (current == '@') {// NOI18N
                        state = 2;
                        break;
                    }

                    offset--;
                    state = 0;
                    break;
            }

            offset++;
        }

        if (offset > offsetStart) {
            return new Pair<CharSequence, Integer>(start.subSequence(offsetStart, offset), offsetStart);
        } else {
            return null;
        }
    }

    public void addChangeListener(ChangeListener l) {
        //ignored...
    }

    public void removeChangeListener(ChangeListener l) {
        //ignored...
    }

    private static class Pair<A, B> {

        private final A a;
        private final B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }
    
    private static enum Kind {
        String,
        Comment,
        Doc
    }
}
