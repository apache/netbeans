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

package org.netbeans.modules.profiler.oql.language;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;

/**
 *
 * @author Jaroslav Bachorik
 */
public class KeywordCompletionItem implements CompletionItem {
    private final String text;
    private final String sortPrefix;
    private final int caret;
    private final int correction;

    private static final Color fieldColor = Color.decode("0x0000B2");

    public KeywordCompletionItem(String sortPrefix, String text, int caretOffset) {
        this(sortPrefix, text, caretOffset, 0);
    }

    public KeywordCompletionItem(String sortPrefix, String text, int caretOffset, int correction) {
        this.text = text;
        this.caret = caretOffset;
        this.correction = correction >= 0 ? correction : 0;
        this.sortPrefix = sortPrefix;
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public void defaultAction(JTextComponent component) {
        final BaseDocument doc = (BaseDocument) component.getDocument();
        doc.runAtomicAsUser(new Runnable() {

            @Override
            public void run() {
                try {
                    if (caret > doc.getLength()) {
                        doc.insertString(caret - 1, text.substring(correction), null);
                    } else {
                        doc.insertString(caret, text.substring(correction), null);
                    }
                } catch (BadLocationException ex) {
                    // shouldn't happen
                }
            }
        });
        //This statement will close the code completion box:
        Completion.get().hideAll();

    }

    public CharSequence getInsertPrefix() {
        return text;
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(text, null, g, defaultFont);
    }

    public int getSortPriority() {
        return 0;
    }

    public CharSequence getSortText() {
        return sortPrefix + "_" + text;
    }

    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }

    public void processKeyEvent(KeyEvent evt) {
        
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(null, text, null, g, defaultFont, (selected ? Color.white : fieldColor), width, height, selected);
    }

}
