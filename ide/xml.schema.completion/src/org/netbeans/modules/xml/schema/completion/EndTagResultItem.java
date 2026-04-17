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

package org.netbeans.modules.xml.schema.completion;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Alex Petrov (Alexey.Petrov@Sun.com)
 */
public class EndTagResultItem extends CompletionResultItem {
    private static final Logger _logger = Logger.getLogger(EndTagResultItem.class.getName());

    private int endTagSortPriority = -1;

    public EndTagResultItem(String tagName, TokenSequence tokenSequence) {
        super(null, null);
        this.itemText = tagName;
        setTokenSequence(tokenSequence);
    }

    @Override
    public String getDisplayText() {
        return (CompletionUtil.END_TAG_PREFIX + 
               (itemText != null ? itemText : NbBundle.getMessage(EndTagResultItem.class,
                   "UNKNOWN_TAG_NAME")) +
                CompletionUtil.TAG_LAST_CHAR);
    }

    @Override
    public String getReplacementText(){
        return getDisplayText();
    }

    @Override
    public int getCaretPosition() {
        return 0;
    }

    @Override
    public CompletionPaintComponent getPaintComponent() {
        if (component == null) {
            component = new CompletionPaintComponent.DefaultCompletionPaintComponent(this);
        }
        return component;
    }

    public void setSortPriority(int sortPriority) {
        this.endTagSortPriority = sortPriority;
    }

    @Override
    public int getSortPriority() {
        return endTagSortPriority;
    }

    private void reindent(JTextComponent component) {
        final BaseDocument doc = (BaseDocument) component.getDocument();
        final int dotPos = component.getCaretPosition();
        final Indent indent = Indent.get(doc);
        indent.lock();
        try {
            doc.runAtomic(new Runnable() {

                @Override
                public void run() {
                    try {
                        int startOffset = LineDocumentUtils.getLineStartOffset(doc, dotPos);
                        int endOffset = LineDocumentUtils.getLineEndOffset(doc, dotPos);
                        indent.reindent(startOffset, endOffset);
                    } catch (BadLocationException ex) {
                        //ignore
                        }
                }
            });
        } finally {
            indent.unlock();
        }

    }

    @Override
    protected void replaceText(final JTextComponent component, final String text,
        final int offset, final int len) {
        final BaseDocument doc = (BaseDocument) component.getDocument();
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    // we cannot rely on the stored one - see #181711. Performance suffers a little.
                    TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
                    tokenSequence = tokenHierarchy.tokenSequence();
                    String insertingText = getInsertingText(component, offset, text, len);
                    if (len > 0) doc.remove(offset, len);
                    doc.insertString(offset, insertingText, null);
                    // fix for issue #186916
                    if ((! text.isEmpty()) && (! insertingText.isEmpty())) {
                        tokenHierarchy = TokenHierarchy.get(doc);
                        tokenSequence = tokenHierarchy.tokenSequence();
                        tokenSequence.move(offset);
                        tokenSequence.movePrevious();
                        Token token = tokenSequence.token();
                        if (CompletionUtil.isTagLastChar(token)) {
                            int caretPos = component.getCaretPosition() - text.length();
                            if (caretPos > -1) component.setCaretPosition(caretPos);
                        }
                    }
                } catch (Exception e) {
                    _logger.log(Level.WARNING,
                        e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
                }
            }
        });
        reindent(component);
    }
}