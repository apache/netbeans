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
package org.netbeans.modules.web.core.syntax.gsf;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import junit.framework.TestCase;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.web.core.syntax.JspKit;
import org.netbeans.test.web.core.syntax.TestBase;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JspKeystrokeHandlerTest extends TestBase {

    public JspKeystrokeHandlerTest(String name) {
        super(name);
    }

    private Typing typing(String code) {
        return new Typing(new JspKit(), code);
    }

    public void testHandleElExpressionCompletion() {
        // issue #234702
        Typing t = typing("$");
        t.typeChar('{');
        t.assertDocumentTextEquals("${}");

        t = typing("#");
        t.typeChar('{');
        t.assertDocumentTextEquals("#{}");
    }

    public void testHandleElExpressionEndingBracket() {
        // issue #244635
        Typing t = typing("${|}");
        t.typeChar('}');
        t.assertDocumentTextEquals("${}");

        t = typing("#{|}");
        t.typeChar('}');
        t.assertDocumentTextEquals("#{}");
    }

    public void testHandleElExpressionEndingNextBracket() {
        Typing t = typing("${}");
        t.typeChar('}');
        t.assertDocumentTextEquals("${}}");

        t = typing("#{}");
        t.typeChar('}');
        t.assertDocumentTextEquals("#{}}");
    }

    /**
     * Copy & paste from the HTML editor.
     */
    private static class Typing {

        private JEditorPane pane;

        public Typing(final EditorKit kit, final String textWithPipe) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        pane = new JEditorPane();
                        pane.setEditorKit(kit);
                        Document doc = pane.getDocument();
                        // Required by Java's default key typed
                        doc.putProperty(Language.class, JspTokenId.language());
                        doc.putProperty("mimeType", "text/x-jsp");
                        int caretOffset = textWithPipe.indexOf('|');
                        String text;
                        if (caretOffset != -1) {
                            text = textWithPipe.substring(0, caretOffset) + textWithPipe.substring(caretOffset + 1);
                        } else {
                            text = textWithPipe;
                        }
                        pane.setText(text);
                        pane.setCaretPosition((caretOffset != -1) ? caretOffset : doc.getLength());
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getCause());
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }

        public JEditorPane pane() {
            return pane;
        }

        public Document document() {
            return pane.getDocument();
        }

        public void typeChar(final char ch) {
            KeyEvent keyEvent;
            switch (ch) {
                case '\n':
                    keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED, EventQueue.getMostRecentEventTime(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of Enter
                    break;
                case '\b':
                    keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED, EventQueue.getMostRecentEventTime(), 0, KeyEvent.VK_BACK_SPACE, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of BackSpace
                    break;
                case '\f':
                    keyEvent = new KeyEvent(pane, KeyEvent.KEY_PRESSED, EventQueue.getMostRecentEventTime(), 0, KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED); // Simulate pressing of Delete
                    break;
                default:
                    keyEvent = new KeyEvent(pane, KeyEvent.KEY_TYPED, EventQueue.getMostRecentEventTime(), 0, KeyEvent.VK_UNDEFINED, ch);
            }
            SwingUtilities.processKeyBindings(keyEvent);
        }

        public void typeText(String text) {
            for (int i = 0; i < text.length(); i++) {
                typeChar(text.charAt(i));
            }
        }

        public void assertDocumentTextEquals(final String textWithPipe) {
            int caretOffset = textWithPipe.indexOf('|');
            String text;
            if (caretOffset != -1) {
                text = textWithPipe.substring(0, caretOffset) + textWithPipe.substring(caretOffset + 1);
            } else {
                text = textWithPipe;
            }
            try {
                // Use debug text to prefix special chars for easier readability
                text = CharSequenceUtilities.debugText(text);
                String docText = document().getText(0, document().getLength());
                docText = CharSequenceUtilities.debugText(docText);
                if (!text.equals(docText)) {
                    int diffIndex = 0;
                    int minLen = Math.min(docText.length(), text.length());
                    while (diffIndex < minLen) {
                        if (text.charAt(diffIndex) != docText.charAt(diffIndex)) {
                            break;
                        }
                        diffIndex++;
                    }
                    TestCase.fail("Invalid document text - diff at index " + diffIndex + "\nExpected: \"" + text + "\"\n  Actual: \"" + docText + "\"");
                }
            } catch (BadLocationException e) {
                throw new IllegalStateException(e);
            }
            if (caretOffset != -1) {
                TestCase.assertEquals("Invalid caret offset", caretOffset, pane.getCaretPosition());
            }
        }

    }

}
