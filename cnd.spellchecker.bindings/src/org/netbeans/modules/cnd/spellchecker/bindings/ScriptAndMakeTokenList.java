/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.spellchecker.bindings;

import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.cnd.api.script.BatTokenId;
import org.netbeans.modules.cnd.api.script.CMakeTokenId;
import org.netbeans.modules.cnd.api.script.MakefileTokenId;
import org.netbeans.modules.cnd.api.script.ShTokenId;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class ScriptAndMakeTokenList implements TokenList {

    private Document doc;
    private boolean hidden = false;

    /** Creates a new instance of CndTokenList */
    public ScriptAndMakeTokenList(Document doc) {
        this.doc = doc;
    }

    public void setStartOffset(int offset) {
        currentBlockText = null;
        currentOffsetInComment = (-1);
        this.startOffset = offset;
        this.nextBlockStart = offset;
        FileObject fileObject = FileUtil.getConfigFile("Spellcheckers/ScriptComments"); //NOI18N
        Boolean b = (Boolean) fileObject.getAttribute("Hidden");//NOI18N
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
        TokenHierarchy<Document> h = TokenHierarchy.get(doc);
        TokenSequence<?> ts = h.tokenSequence(MakefileTokenId.language());
        if (ts == null) {
            ts = h.tokenSequence(BatTokenId.language());
        }
        if (ts == null) {
            ts = h.tokenSequence(ShTokenId.language());
        }
        if (ts == null) {
            ts = h.tokenSequence(CMakeTokenId.languageMake());
        }
        if (ts == null) {
            ts = h.tokenSequence(CMakeTokenId.languageInc());
        }
        if (ts == null) {
            return new int[]{-1, -1};
        }

        int diff = ts.move(nextBlockStart);

        while (ts.moveNext()) {
            TokenId id = ts.token().id();
            if ("comment".equals(id.primaryCategory())) {// NOI18N 
                return new int[]{ts.offset(), ts.offset() + ts.token().length()};
            }
        }

        return new int[]{-1, -1};
    }

    private boolean nextWordImpl() {
        try {
            while (true) {
                if (currentBlockText == null) {
                    int[] span = findNextComment();

                    if (span[0] == (-1)) {
                        return false;
                    }

                    currentBlockStart = span[0];
                    currentBlockText = doc.getText(span[0], span[1] - span[0]);
                    currentOffsetInComment = 0;

                    nextBlockStart = span[1];
                }

                String pairTag = null;
                Pair<CharSequence, Integer> data = wordBroker(currentBlockText, currentOffsetInComment);

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
                            case '{':
                                pairTag = "}";//NOI18N
                                break;
                        }
                    } else {
                        if (pairTag.contentEquals(data.a)) {
                            pairTag = null;
                        }
                    }

                    data = wordBroker(currentBlockText, currentOffsetInComment);
                }

                currentBlockText = null;
            }
        } catch (BadLocationException e) {
            // skip
            return false;
        }
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

    private boolean isLetter(char c) {
        return Character.isLetter(c) || c == '\'';
    }

    private enum State {
        INIT,
        AFTER_LETTER,
        AFTER_LESS,
        AFTER_LCURLY,
    }
    private Pair<CharSequence, Integer> wordBroker(CharSequence start, int offset) {
        State state = State.INIT;
        int offsetStart = offset;

        while (start.length() > offset) {
            char current = start.charAt(offset);

            switch (state) {
                case INIT:
                    if (isLetter(current)) {
                        state = State.AFTER_LETTER;
                        offsetStart = offset;
                        break;
                    }
                    if (current == '<') {
                        state = State.AFTER_LESS;
                        offsetStart = offset;
                        break;
                    }
                    if (current == '\n' || current == '}') {
                        return new Pair<CharSequence, Integer>(start.subSequence(offset, offset + 1), offset);
                    }
                    if (current == '{') {
                        state = State.AFTER_LCURLY;
                        offsetStart = offset;
                        break;
                    }
                    break;

                case AFTER_LETTER:
                    if (!isLetter(current)) {
                        return new Pair<CharSequence, Integer>(start.subSequence(offsetStart, offset), offsetStart);
                    }
                    break;

                case AFTER_LESS:
                    if (current == '>') {
                        return new Pair<CharSequence, Integer>(start.subSequence(offsetStart, offset + 1), offsetStart);
                    }
                    break;

                case AFTER_LCURLY:
                    offset--;
                    state = State.INIT;
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
}
