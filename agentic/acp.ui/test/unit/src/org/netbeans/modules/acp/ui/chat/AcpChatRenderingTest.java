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
package org.netbeans.modules.acp.ui.chat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import junit.framework.TestCase;
import org.netbeans.modules.acp.ui.AcpMarkdownRenderer;

public class AcpChatRenderingTest extends TestCase {

    public void testMarkdownSurvivesMultipleFlushes() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ChatRenderer renderer = new ChatRenderer();
            renderer.appendUserMessage("existing history");
            renderer.renderChatDisplay();
            JEditorPane pane = (JEditorPane) renderer.getViewport().getView();
            HTMLDocument document = (HTMLDocument) pane.getDocument();
            Element previous = document.getElement("acp-entry-0");
            assertNotNull(previous);
            renderer.appendAgentChunk(null);
            renderer.appendAgentChunk("```java\nint");
            renderer.flushMessageBuffers();
            renderer.renderChatDisplay();
            renderer.appendThoughtChunk("thought following the reply");
            renderer.appendAgentChunk(" value = 1;\n```");
            renderer.flushMessageBuffers();
            renderer.renderChatDisplay();
            assertSame(document, pane.getDocument());
            assertSame(previous, document.getElement("acp-entry-0"));
            assertTrue(documentText(pane).contains("thought following the reply"));
            ElementIterator elements = new ElementIterator(pane.getDocument());
            List<Element> codeBlocks = new ArrayList<>();
            for (Element element = elements.first(); element != null; element = elements.next()) {
                if (element.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.PRE) {
                    codeBlocks.add(element);
                }
            }
            assertEquals(1, codeBlocks.size());
            Element block = codeBlocks.get(0);
            try {
                String code = pane.getDocument().getText(block.getStartOffset(), block.getEndOffset() - block.getStartOffset());
                assertEquals("int value = 1;", code.strip());
            } catch (BadLocationException ex) {
                throw new AssertionError(ex);
            }
            renderer.disconnect();
        });
    }

    public void testHistoryEvictsOldEntriesAndTheirLinks() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ChatRenderer renderer = new ChatRenderer();
            renderer.appendFileChange("old.txt", "old text", "new text");
            renderer.renderChatDisplay();
            JEditorPane pane = (JEditorPane) renderer.getViewport().getView();
            assertTrue(pane.getText().contains("acp-diff:diff-0"));
            for (int i = 0; i < ChatRenderer.MAX_HISTORY_ENTRIES; i++) {
                renderer.appendUserMessage("prompt-" + i);
            }
            renderer.renderChatDisplay();
            HTMLDocument document = (HTMLDocument) pane.getDocument();
            assertNull(document.getElement("acp-entry-0"));
            assertFalse(pane.getText().contains("acp-diff:diff-0"));
            assertTrue(pane.getText().contains("prompt-" + (ChatRenderer.MAX_HISTORY_ENTRIES - 1)));
            assertTrue(pane.getText().contains(Bundle.LBL_ChatHistoryTrimmed()));
            renderer.disconnect();
        });
    }

    public void testDiffAndImageBudgetsPreserveRecentEntries() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ChatRenderer renderer = new ChatRenderer();
            String large = "a".repeat(ChatRenderer.MAX_DIFF_CHARS / 2 + 1);
            renderer.appendFileChange("oversize.txt", large, large);
            renderer.renderChatDisplay();
            JEditorPane pane = (JEditorPane) renderer.getViewport().getView();
            assertTrue(documentText(pane).contains(Bundle.LBL_FullDiffTooLarge()));
            assertFalse(pane.getText().contains("acp-diff:"));
            renderer.appendFileChange("previous.txt", large, "previous");
            renderer.appendFileChange("recent.txt", large, "recent");
            renderer.renderChatDisplay();
            assertFalse(pane.getText().contains("previous.txt"));
            assertTrue(pane.getText().contains("recent.txt"));
            ImageIcon icon = new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
            int nextId = 3;
            for (int i = 0; i <= ChatRenderer.MAX_IMAGES; i++) {
                renderer.appendImage(icon);
            }
            renderer.renderChatDisplay();
            HTMLDocument document = (HTMLDocument) pane.getDocument();
            assertNull(document.getElement("acp-entry-" + nextId));
            assertNotNull(document.getElement("acp-entry-" + (nextId + ChatRenderer.MAX_IMAGES)));
            renderer.disconnect();
        });
    }

    public void testHistoryCharacterBudgetAppliesBeforeEntryLimit() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ChatRenderer renderer = new ChatRenderer();
            renderer.appendUserMessage("oldest prompt");
            String body = "word ".repeat(ChatRenderer.MAX_MESSAGE_CHARS / 5);
            int prompts = ChatRenderer.MAX_HISTORY_HTML / body.length() + 2;
            assertTrue(prompts < ChatRenderer.MAX_HISTORY_ENTRIES);
            for (int i = 0; i < prompts; i++) {
                renderer.appendUserMessage("prompt " + i + " " + body);
            }
            renderer.renderChatDisplay();
            JEditorPane pane = (JEditorPane) renderer.getViewport().getView();
            String text = documentText(pane);
            assertFalse(text.contains("oldest prompt"));
            assertTrue(text.contains("prompt " + (prompts - 1)));
            assertTrue(text.length() < ChatRenderer.MAX_HISTORY_HTML);
            renderer.disconnect();
        });
    }

    public void testBoundsAStreamingMessage() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ChatRenderer renderer = new ChatRenderer();
            renderer.appendAgentChunk("word ".repeat(ChatRenderer.MAX_MESSAGE_CHARS));
            renderer.appendAgentChunk("suffix outside the limit");
            renderer.flushMessageBuffers();
            renderer.renderChatDisplay();
            JEditorPane pane = (JEditorPane) renderer.getViewport().getView();
            assertTrue(pane.getText().contains(Bundle.LBL_ChatTextTruncated()));
            assertFalse(pane.getText().contains("suffix outside the limit"));
            renderer.disconnect();
        });
    }

    private static String documentText(JEditorPane pane) {
        try {
            return pane.getDocument().getText(0, pane.getDocument().getLength());
        } catch (BadLocationException ex) {
            throw new AssertionError(ex);
        }
    }

    public void testEscapesRawMarkup() {
        String html = AcpMarkdownRenderer.renderToHtml("<div>Text</div>");
        assertFalse(html, html.contains("<div>"));
        assertTrue(html, html.contains("&lt;div&gt;"));
    }

    public void testImageValidationAndScaling() throws Exception {
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(800, 400, BufferedImage.TYPE_INT_RGB), "png", bytes);
        String data = Base64.getEncoder().encodeToString(bytes.toByteArray());
        ImageIcon icon = AcpChatImage.decode(data, "image/png");
        assertEquals(400, icon.getIconWidth());
        assertEquals(200, icon.getIconHeight());
        try {
            AcpChatImage.decode(data, "image/jpeg");
            fail("The declared image type must match the image contents");
        } catch (IOException expected) {
            assertEquals("Image format does not match its MIME type", expected.getMessage());
        }
    }

}
