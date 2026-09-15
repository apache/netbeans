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
/*
 * Contributor(s): Stefan Riha, Roland Poppenreiter
 */
package org.netbeans.modules.spellchecker.bindings.htmlxml;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.openide.ErrorManager;

/**
 * Tokenize Html and Xml text for spell checking. Based on corresponding
 * JavaTokenList by Jan Lahoda.
 *
 * @author Riha, Poppenreiter
 */
public abstract sealed class AbstractTokenList implements TokenList permits HtmlTokenList, XmlTokenList {

    record SpellSpan(int begin, int end) {
        public static final SpellSpan NONE = new SpellSpan(-1, -1);
    };

    protected final BaseDocument doc;
    private CharSequence currentWord;
    private int currentStartOffset;
    protected int nextSearchOffset;
    private int ignoreBefore;

    /** Creates a new instance of HtmlXmlTokenList */
    AbstractTokenList(BaseDocument doc) {
        this.doc = doc;
    }

    @Override
    public void setStartOffset(int offset) {
        currentWord = null;
        currentStartOffset = (-1);
        this.ignoreBefore = offset;
        try {
            this.nextSearchOffset = LineDocumentUtils.getLineStartOffset(doc, offset);
        } catch (BadLocationException ex) {
            Logger.getLogger(AbstractTokenList.class.getName()).log(Level.FINE, null, ex);
            this.nextSearchOffset = offset;
        }
    }

    @Override
    public int getCurrentWordStartOffset() {
        return currentStartOffset;
    }

    @Override
    public CharSequence getCurrentWordText() {
        return currentWord;
    }

    abstract SpellSpan findNextSpellSpan() throws BadLocationException;

    @Override
    public boolean nextWord() {
        boolean next = nextWordImpl();

        while (next && (currentStartOffset + currentWord.length()) < ignoreBefore) {
            next = nextWordImpl();
        }

        return next;
    }

    private boolean nextWordImpl() {
        try {
            SpellSpan span = findNextSpellSpan();

            while (span.begin != -1) {
                int offset = (span.begin < nextSearchOffset) ? nextSearchOffset : span.begin;

                boolean searching = true;

					 /* find next word */
                while (offset < span.end) {
                    String t = doc.getText(offset, 1);
                    char c = t.charAt(0);

                    if (searching) {
                        if (Character.isLetter(c)) {
									 /* word beginn found */
                            searching = false;
                            currentStartOffset = offset;
                        }
                    } else {
                        if (!Character.isLetter(c)) {
                            /* word end found */
                            nextSearchOffset = offset;
                            currentWord = doc.getText(currentStartOffset, offset - currentStartOffset);
                            return true;
                        }
                    }

                    offset++;
                }

                nextSearchOffset = offset;

                if (!searching) {
                    currentWord = doc.getText(currentStartOffset, offset - currentStartOffset);
                    return true;
                }

                span = findNextSpellSpan();
            }

            return false;
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    //ignored...
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    //ignored...
    }
}
