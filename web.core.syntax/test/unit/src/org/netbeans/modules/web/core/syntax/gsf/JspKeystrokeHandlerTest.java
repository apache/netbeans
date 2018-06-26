/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
