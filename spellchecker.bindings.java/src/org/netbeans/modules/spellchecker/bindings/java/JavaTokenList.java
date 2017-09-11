/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.spellchecker.bindings.java;

import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class JavaTokenList implements TokenList {

    private final Document doc;
    private boolean hidden = false;

    /** Creates a new instance of JavaTokenList */
    public JavaTokenList(Document doc) {
        this.doc = doc;
    }

    public void setStartOffset(int offset) {
        currentBlockText = null;
        currentOffsetInComment = (-1);
        this.startOffset = this.nextBlockStart = offset;
        FileObject fileObject = FileUtil.getConfigFile ("Spellcheckers/Javadoc");
        Boolean b = (Boolean) fileObject.getAttribute ("Hidden");
        hidden = Boolean.TRUE.equals (b);
    }

    public int getCurrentWordStartOffset() {
        return currentWordOffset;
    }

    public CharSequence getCurrentWordText() {
        return currentWord;
    }

    public boolean nextWord() {
        if (hidden) return false;
        boolean hasNext = nextWordImpl();

        while (hasNext && (currentWordOffset + currentWord.length()) < startOffset) {
            hasNext = nextWordImpl();
        }

        return hasNext;
    }

    private int[] findNextJavaDocComment() throws BadLocationException {
        TokenHierarchy h  = TokenHierarchy.get(doc);
        TokenSequence  ts = h.tokenSequence(JavaTokenId.language());
        
        if (ts == null) {
            return new int[] {-1, -1};
        }
        
        int diff = ts.move(nextBlockStart);
        
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.JAVADOC_COMMENT) {
                return new int[] {ts.offset(), ts.offset() + ts.token().length()};
            }
        } while (ts.moveNext());
        
        return new int[] {-1, -1};
    }
    
    private void handleJavadocTag(CharSequence tag) {
        if ("@see".contentEquals(tag) || "@throws".contentEquals(tag)) {
            //ignore next "word", possibly dotted and hashed
            Pair<CharSequence, Integer> data = wordBroker(currentBlockText, currentOffsetInComment, true);
            
            currentOffsetInComment = data.b + data.a.length();
            return ;
        }
        
        if ("@param".contentEquals(tag)) {
            //ignore next word
            Pair<CharSequence, Integer> data = wordBroker(currentBlockText, currentOffsetInComment, false);
            
            currentOffsetInComment = data.b + data.a.length();
            return ;
        }
        
        if ("@author".contentEquals(tag)) {
            //ignore everything till the end of the line:
            Pair<CharSequence, Integer> data = wordBroker(currentBlockText, currentOffsetInComment, false);
            
            while (data != null) {
                currentOffsetInComment = data.b + data.a.length();
                
                if ('\n' == data.a.charAt(0)) {
                    //continue
                    return ;
                }
                
                data = wordBroker(currentBlockText, currentOffsetInComment, false);
            }
            
            return ;
        }
    }

    private boolean nextWordImpl() {
        try {
            while (true) {
                if (currentBlockText == null) {
                    int[] span = findNextJavaDocComment();

                    if (span[0] == (-1))
                        return false;

                    currentBlockStart = span[0];
                    currentBlockText = doc.getText(span[0], span[1] - span[0]);
                    currentOffsetInComment = 0;

                    nextBlockStart = span[1];
                }

                String pairTag = null;
                Pair<CharSequence, Integer> data = wordBroker(currentBlockText, currentOffsetInComment, false);

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
                            case '@':
                                handleJavadocTag(data.a);
                                break;
                            case '<':
                                if (startsWith(data.a, "<a "))
                                    pairTag = "</a>";
                                if (startsWith(data.a, "<code>"))
                                    pairTag = "</code>";
                                if (startsWith(data.a, "<pre>"))
                                    pairTag = "</pre>";
                                break;
                            case '{':
                                pairTag = "}";
                                break;
                        }
                    } else {
                        if (pairTag.contentEquals(data.a))
                            pairTag = null;
                    }

                    data = wordBroker(currentBlockText, currentOffsetInComment, false);
                }
                
                currentBlockText = null;
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
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

    private int   currentBlockStart;
    private int   nextBlockStart;
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
    
    private Pair<CharSequence, Integer> wordBroker(CharSequence start, int offset, boolean treatSpecialCharactersAsLetterInsideWords) {
        int state = 0;
        int offsetStart = offset;

        while (start.length() > offset) {
            char current = start.charAt(offset);

            switch (state) {
                case 0:
                    if (isLetter(current)) {
                        state = 1;
                        offsetStart = offset;
                        break;
                    }
                    if (current == '@' || current == '#') {
                        state = 2;
                        offsetStart = offset;
                        break;
                    }
                    if (current == '<') {
                        state = 3;
                        offsetStart = offset;
                        break;
                    }
                    if (current == '\n' || current == '}') {
                        return new Pair<CharSequence, Integer>(start.subSequence(offset, offset + 1), offset);
                    }
                    if (current == '{') {
                        state = 4;
                        offsetStart = offset;
                        break;
                    }
                    if (current == '&') {
                        state = 5;
                        offsetStart = offset;
                        break;
                    }
                    break;

                case 1:
                    if (!isLetter(current) && ((current != '.' && current != '#') || !treatSpecialCharactersAsLetterInsideWords)) {
                        return new Pair<CharSequence, Integer>(start.subSequence(offsetStart, offset), offsetStart);
                    }

                    break;

                case 2:
                    if (!isLetter(current)) {
                        return new Pair<CharSequence, Integer>(start.subSequence(offsetStart, offset), offsetStart);
                    }

                    break;

                case 3:
                    if (current == '>') {
                        return new Pair<CharSequence, Integer>(start.subSequence(offsetStart, offset + 1), offsetStart);
                    }

                    break;
                    
                case 4:
                    if (current == '@') {
                        state = 2;
                        break;
                    }
                    
                    offset--;
                    state = 0;
                    break;
                case 5:
                    if (current == ';') {
                        return new Pair<CharSequence, Integer>(start.subSequence(offsetStart, offset + 1), offsetStart);
                    }
                    if (!isLetter(current) && current != '#' && !Character.isDigit(current)) {
                        return new Pair<CharSequence, Integer>(start.subSequence(offsetStart, offset), offsetStart);
                    }

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

        private A a;
        private B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }
}
