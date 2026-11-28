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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.modules.acp.api.AcpPlanEntry;
import org.netbeans.modules.acp.api.AcpToolCall;
import org.netbeans.modules.acp.ui.AcpDiffViewer;
import org.netbeans.modules.acp.ui.AcpLocationNavigator;
import org.netbeans.modules.acp.ui.AcpMarkdownRenderer;
import org.netbeans.modules.acp.ui.HtmlUtils;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "LBL_FileCreated=Created",
    "LBL_FileModified=Modified",
    "LBL_Completed=completed",
    "LBL_ViewFullDiff=View Full Diff",
    "LBL_You=You:",
    "LBL_AgentSays=Agent:",
    "LBL_Plan=Plan:",
    "LBL_ChatTextTruncated=[Text truncated]",
    "LBL_ChatEntryTooLarge=This entry is too large to display.",
    "LBL_FullDiffTooLarge=Full diff is too large to retain.",
    "LBL_ChatHistoryTrimmed=Older entries were discarded to limit chat history."
})
final class ChatRenderer extends JScrollPane {

    private static final Logger LOG = Logger.getLogger(ChatRenderer.class.getName());
    private static final int DISPLAY_DELAY_MS = 50;
    private static final int RENDER_DELAY_MS = 200;
    static final int MAX_HISTORY_ENTRIES = 500;
    static final int MAX_HISTORY_HTML = 1_000_000;
    static final int MAX_MESSAGE_CHARS = 32_768;
    static final int MAX_IMAGES = 24;
    static final int MAX_DIFFS = 64;
    static final int MAX_DIFF_CHARS = 2_000_000;
    private static final int MAX_ENTRY_HTML = MAX_HISTORY_HTML / 4;
    private static final int MAX_PREVIEW_LINES = 5;
    private static final int MAX_PREVIEW_LINE_CHARS = 240;
    private static final int MAX_TOOL_ITEMS = 100;

    private final JEditorPane chatPane;
    private final Deque<ChatEntry> history = new ArrayDeque<>();
    private final Set<ChatEntry> dirty = new LinkedHashSet<>();
    private final Map<String, ImageIcon> chatImages = new HashMap<>();
    private final Map<String, AcpToolCall.DiffContent> diffStore = new HashMap<>();
    private final StringBuilder currentMessageBuffer = new StringBuilder();
    private final StringBuilder currentThoughtBuffer = new StringBuilder();
    private final Timer renderTimer;
    private final Timer displayTimer;
    private ChatEntry activeMessage;
    private boolean messageChanged;
    private boolean messageTruncated;
    private boolean thoughtTruncated;
    private boolean historyTrimmed;
    private boolean rebuildNeeded;
    private long entrySequence;
    private long diffSequence;
    private int historyChars;
    private long diffChars;

    ChatRenderer() {
        HTMLEditorKit kit = new HTMLEditorKit() {
            @Override
            public ViewFactory getViewFactory() {
                ViewFactory fallback = super.getViewFactory();
                return element -> {
                    if (element.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.IMG) {
                        return new ComponentView(element) {
                            @Override
                            protected Component createComponent() {
                                String id = (String) element.getAttributes().getAttribute(HTML.Attribute.SRC);
                                return new JLabel(chatImages.get(id));
                            }
                        };
                    }
                    return fallback.create(element);
                };
            }

            @Override
            public void install(JEditorPane c) {
                super.install(c);
                for (MouseMotionListener mml : c.getMouseMotionListeners()) {
                    if (mml instanceof LinkController) {
                        final MouseMotionListener delegate = mml;
                        c.removeMouseMotionListener(mml);
                        c.addMouseMotionListener(new MouseMotionAdapter() {
                            @Override
                            public void mouseMoved(MouseEvent e) {
                                try { delegate.mouseMoved(e); } catch (ArrayIndexOutOfBoundsException ignored) {}
                            }
                            @Override
                            public void mouseDragged(MouseEvent e) {
                                try { delegate.mouseDragged(e); } catch (ArrayIndexOutOfBoundsException ignored) {}
                            }
                        });
                        break;
                    }
                }
            }
        };
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: sans-serif; font-size: 12px; margin: 8px; }");
        styleSheet.addRule(".user-msg { background-color: #BBDEFB; padding: 8px; margin: 6px 40px 6px 0; border-left: 4px solid #1976D2; }");
        styleSheet.addRule(".agent-msg { background-color: #E8E8E8; padding: 8px; margin: 6px 0 6px 40px; border-left: 4px solid #616161; }");
        styleSheet.addRule(".system-msg { background-color: #FFF9C4; padding: 6px; margin: 4px 0; border-left: 4px solid #F9A825; }");
        styleSheet.addRule(".thought { color: #666; font-style: italic; padding: 4px 8px; margin: 2px 0 2px 40px; background-color: #F5F5F5; }");
        styleSheet.addRule(".tool-call { background-color: #FFF3E0; padding: 6px; margin: 4px 0 4px 40px; border-left: 4px solid #FF9800; }");
        styleSheet.addRule(".plan { background-color: #E8F5E9; padding: 6px; margin: 4px 0 4px 40px; border-left: 4px solid #4CAF50; }");
        styleSheet.addRule(".user-label { color: #1565C0; font-weight: bold; }");
        styleSheet.addRule(".agent-label { color: #424242; font-weight: bold; }");
        styleSheet.addRule(".diff-add { color: #2E7D32; }");
        styleSheet.addRule(".diff-del { color: #C62828; }");
        styleSheet.addRule(".diff-link { color: #1565C0; }");
        styleSheet.addRule(".tc-kind { color: #E65100; font-weight: bold; }");
        styleSheet.addRule(".tc-status-completed { color: #2E7D32; }");
        styleSheet.addRule(".tc-status-failed { color: #C62828; }");
        styleSheet.addRule(".tc-status-progress { color: #F57F17; }");
        styleSheet.addRule(".tc-location { color: #5C6BC0; font-size: 11px; }");
        styleSheet.addRule("pre { background-color: #263238; color: #EEFFFF; padding: 8px; overflow-x: auto; }");
        styleSheet.addRule("code { font-family: monospace; }");
        styleSheet.addRule("h1 { font-size: 16px; margin: 8px 0 4px; }");
        styleSheet.addRule("h2 { font-size: 14px; margin: 6px 0 4px; }");
        styleSheet.addRule("h3 { font-size: 13px; margin: 4px 0 2px; }");
        styleSheet.addRule("table { border-collapse: collapse; margin: 4px 0; }");
        styleSheet.addRule("th, td { border: 1px solid #ccc; padding: 4px 8px; }");
        styleSheet.addRule("th { background-color: #f0f0f0; font-weight: bold; }");
        styleSheet.addRule("blockquote { border-left: 3px solid #ccc; padding-left: 8px; color: #666; margin: 4px 0; }");
        styleSheet.addRule("ul, ol { margin: 4px 0 4px 20px; }");

        chatPane = new JEditorPane();
        chatPane.setEditorKit(kit);
        chatPane.setEditable(false);
        chatPane.setContentType("text/html");
        chatPane.setText("<html><body></body></html>");
        chatPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String url = e.getDescription();
                if (url != null && url.startsWith("acp-diff:")) {
                    String diffId = url.substring("acp-diff:".length());
                    AcpToolCall.DiffContent diff = diffStore.get(diffId);
                    if (diff != null) {
                        AcpDiffViewer.showDiff(diff);
                    }
                } else if (url != null && url.startsWith("acp-nav:")) {
                    AcpLocationNavigator.handleNavUrl(url);
                }
            }
        });
        setViewportView(chatPane);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);

        renderTimer = new Timer(RENDER_DELAY_MS, e -> flushMessageBuffers());
        renderTimer.setRepeats(false);
        displayTimer = new Timer(DISPLAY_DELAY_MS, e -> renderChatDisplay());
        displayTimer.setRepeats(false);
    }

    void appendUserMessage(String text) {
        String html = "<span class='user-label'>" + Bundle.LBL_You() + "</span><br/>"
                + escapePreview(text, MAX_MESSAGE_CHARS).replace("\n", "<br/>");
        append(new ChatEntry("user-msg", html));
    }

    void appendSystemMessage(String html) {
        append(new ChatEntry("system-msg", html));
    }

    void appendAgentChunk(String text) {
        if (text == null || text.isEmpty() || messageTruncated) {
            return;
        }
        messageTruncated = appendChunk(currentMessageBuffer, text);
        messageChanged = true;
        scheduleMessageRender();
    }

    void appendThoughtChunk(String text) {
        if (text != null && !text.isEmpty() && !thoughtTruncated) {
            thoughtTruncated = appendChunk(currentThoughtBuffer, text);
            scheduleMessageRender();
        }
    }

    private void scheduleMessageRender() {
        if (!renderTimer.isRunning()) {
            renderTimer.start();
        }
    }

    private static boolean appendChunk(StringBuilder buffer, String text) {
        int remaining = MAX_MESSAGE_CHARS - buffer.length();
        if (text.length() <= remaining) {
            buffer.append(text);
            return false;
        }
        buffer.append(text, 0, prefixLength(text, remaining));
        return true;
    }

    private static int prefixLength(String text, int limit) {
        return limit > 0 && limit < text.length() && Character.isHighSurrogate(text.charAt(limit - 1))
                ? limit - 1 : limit;
    }

    void appendImage(ImageIcon image) {
        finishAgentMessage();
        ChatEntry entry = new ChatEntry("agent-msg", "");
        entry.image = image;
        entry.html = "<img src='" + entry.id + "'>";
        append(entry);
    }

    void appendFileChange(String path, String oldContent, String newContent) {
        AcpToolCall.DiffContent diff = new AcpToolCall.DiffContent(
                path, oldContent == null ? "" : oldContent, newContent == null ? "" : newContent);
        finishAgentMessage();
        String fileName = path;
        int sep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (sep >= 0) {
            fileName = path.substring(sep + 1);
        }
        String label = oldContent == null ? Bundle.LBL_FileCreated() : Bundle.LBL_FileModified();
        StringBuilder html = new StringBuilder("&#x270F; <b>").append(label).append(": ")
                .append(escapePreview(fileName, 500)).append("</b>")
                .append(" <span class='tc-status-completed'>&#x2705; ")
                .append(Bundle.LBL_Completed()).append("</span>");
        appendLocation(html.append("<br/>"), path, null);
        ChatEntry entry = new ChatEntry("tool-call", "");
        appendDiffPreview(html, diff);
        appendDiffLink(html, entry, diff);
        entry.html = html.toString();
        append(entry);
    }

    void disconnect() {
        renderTimer.stop();
        displayTimer.stop();
        resetAgentMessage();
        currentThoughtBuffer.setLength(0);
        thoughtTruncated = false;
    }

    void clear() {
        disconnect();
        history.clear();
        dirty.clear();
        diffStore.clear();
        chatImages.clear();
        historyChars = 0;
        diffChars = 0;
        historyTrimmed = false;
        rebuildNeeded = false;
        chatPane.setText("<html><body></body></html>");
    }

    void finishAgentMessage() {
        flushMessageBuffers();
        resetAgentMessage();
    }

    private void resetAgentMessage() {
        currentMessageBuffer.setLength(0);
        activeMessage = null;
        messageChanged = false;
        messageTruncated = false;
    }

    void flushMessageBuffers() {
        if (currentThoughtBuffer.length() > 0) {
            String html = HtmlUtils.escapeHtml(currentThoughtBuffer.toString());
            append(new ChatEntry("thought", html + (thoughtTruncated ? truncatedNotice() : "")));
            currentThoughtBuffer.setLength(0);
            thoughtTruncated = false;
        }
        if (messageChanged) {
            String rendered = AcpMarkdownRenderer.renderToHtml(currentMessageBuffer.toString());
            String html = limitEntryHtml("<span class='agent-label'>" + Bundle.LBL_AgentSays()
                    + "</span><br/>" + rendered + (messageTruncated ? truncatedNotice() : ""));
            if (activeMessage == null) {
                activeMessage = new ChatEntry("agent-msg", html);
                append(activeMessage);
            } else {
                historyChars += html.length() - activeMessage.html.length();
                activeMessage.html = html;
                markDirty(activeMessage);
            }
            messageChanged = false;
        }
    }

    private static String truncatedNotice() {
        return "<br/><i>" + Bundle.LBL_ChatTextTruncated() + "</i>";
    }

    void appendToolCall(AcpToolCall toolCall) {
        finishAgentMessage();
        renderToolCallCard(toolCall);
    }

    private void renderToolCallCard(AcpToolCall toolCall) {
        String kindIcon = switch (toolCall.getKind()) {
            case "read" -> "&#x1F4C4;";
            case "edit", "write" -> "&#x270F;";
            case "execute" -> "&#x2699;";
            case "search" -> "&#x1F50D;";
            case "think" -> "&#x1F4AD;";
            case "delete" -> "&#x1F5D1;";
            case "fetch" -> "&#x1F310;";
            default -> "&#x1F527;";
        };
        String statusClass = switch (toolCall.getStatus()) {
            case AcpToolCall.STATUS_COMPLETED -> "tc-status-completed";
            case AcpToolCall.STATUS_FAILED -> "tc-status-failed";
            default -> "tc-status-progress";
        };
        String statusIcon = switch (toolCall.getStatus()) {
            case AcpToolCall.STATUS_COMPLETED -> "&#x2705;";
            case AcpToolCall.STATUS_FAILED -> "&#x274C;";
            case AcpToolCall.STATUS_IN_PROGRESS -> "&#x23F3;";
            default -> "&#x25CB;";
        };

        ChatEntry entry = new ChatEntry("tool-call", "");
        StringBuilder html = new StringBuilder()
                .append(kindIcon).append(" <b>").append(escapePreview(toolCall.getTitle(), 500)).append("</b>")
                .append(" <span class='tc-kind'>[").append(escapePreview(toolCall.getKind(), 100)).append("]</span>")
                .append(" ").append(statusIcon)
                .append(" <span class='").append(statusClass).append("'>")
                .append(escapePreview(toolCall.getStatus(), 100)).append("</span>");

        if (toolCall.getName() != null && !toolCall.getName().equals(toolCall.getTitle())) {
            html.append("<br/><i>").append(escapePreview(toolCall.getName(), 500)).append("</i>");
        }

        List<AcpToolCall.Location> locations = toolCall.getLocations();
        if (!locations.isEmpty()) {
            html.append("<br/>");
            for (int i = 0; i < Math.min(locations.size(), MAX_TOOL_ITEMS); i++) {
                AcpToolCall.Location location = locations.get(i);
                appendLocation(html, location.path(), location.line());
                html.append(' ');
            }
            if (locations.size() > MAX_TOOL_ITEMS) {
                html.append(truncatedNotice());
            }
        }

        List<AcpToolCall.DiffContent> diffs = toolCall.getDiffs();
        for (int i = 0; i < Math.min(diffs.size(), MAX_DIFFS) && html.length() < MAX_ENTRY_HTML / 2; i++) {
            AcpToolCall.DiffContent diff = diffs.get(i);
            html.append("<br/>");
            if (diff.path() != null) {
                html.append("<b>").append(escapePreview(diff.path(), 500)).append("</b><br/>");
            }
            appendDiffPreview(html, diff);
            appendDiffLink(html, entry, diff);
            if (i + 1 < diffs.size() && (i + 1 >= MAX_DIFFS || html.length() >= MAX_ENTRY_HTML / 2)) {
                html.append(truncatedNotice());
            }
        }

        if (toolCall.getTextContent() != null && !toolCall.getTextContent().isEmpty()) {
            html.append("<br/><code>").append(escapePreview(toolCall.getTextContent(), 200)).append("</code>");
        }

        entry.html = html.toString();
        append(entry);
    }

    private static void appendLocation(StringBuilder html, String path, Integer line) {
        String suffix = line == null ? "" : ":" + line;
        if (path != null && path.length() <= 2_048) {
            html.append("<a class='tc-location' href=\"acp-nav:")
                    .append(HtmlUtils.escapeHtml(path)).append(suffix).append("\">")
                    .append(escapePreview(path, 500)).append(suffix).append("</a>");
        } else {
            html.append(escapePreview(path, 500)).append(suffix);
        }
    }

    private void appendDiffLink(StringBuilder html, ChatEntry entry, AcpToolCall.DiffContent diff) {
        long size = length(diff.oldText()) + length(diff.newText()) + length(diff.path());
        if (size + entry.diffChars > MAX_DIFF_CHARS) {
            html.append("<br/><i>").append(Bundle.LBL_FullDiffTooLarge()).append("</i>");
            return;
        }
        String id = "diff-" + diffSequence++;
        entry.diffs.put(id, diff);
        entry.diffChars += size;
        html.append("<br/><a class='diff-link' href='acp-diff:")
                .append(id).append("'>").append(Bundle.LBL_ViewFullDiff()).append("</a>");
    }

    private static long length(String text) {
        return text == null ? 0 : text.length();
    }

    private static void appendDiffPreview(StringBuilder html, AcpToolCall.DiffContent diff) {
        appendPreviewLines(html, diff.oldText(), "diff-del", "- ");
        appendPreviewLines(html, diff.newText(), "diff-add", "+ ");
    }

    private static void appendPreviewLines(StringBuilder html, String text, String css, String prefix) {
        if (text == null) {
            return;
        }
        int start = 0;
        for (int line = 0; line < MAX_PREVIEW_LINES; line++) {
            int next = text.indexOf('\n', start);
            int end = next < 0 ? text.length() : next;
            int limit = start + Math.min(end - start, MAX_PREVIEW_LINE_CHARS);
            html.append("<span class='").append(css).append("'>").append(prefix)
                    .append(HtmlUtils.escapeHtml(text.substring(start, limit)));
            if (limit < end) {
                html.append("...");
            }
            html.append("</span><br/>");
            if (next < 0) {
                return;
            }
            start = next + 1;
        }
        html.append("<span class='").append(css).append("'>...</span><br/>");
    }

    void appendPlan(List<AcpPlanEntry> entries) {
        finishAgentMessage();
        StringBuilder html = new StringBuilder("<b>").append(Bundle.LBL_Plan()).append("</b><ul>");
        for (int i = 0; i < Math.min(entries.size(), MAX_TOOL_ITEMS); i++) {
            AcpPlanEntry entry = entries.get(i);
            String icon = switch (entry.status()) {
                case "completed" -> "&#x2705;";
                case "in_progress" -> "&#x23F3;";
                default -> "&#x25CB;";
            };
            html.append("<li>").append(icon).append(" ")
                    .append(escapePreview(entry.content(), 300))
                    .append("</li>");
        }
        html.append("</ul>");
        if (entries.size() > MAX_TOOL_ITEMS) {
            html.append(truncatedNotice());
        }
        append(new ChatEntry("plan", html.toString()));
    }

    private static String escapePreview(String text, int limit) {
        if (text == null || text.length() <= limit) {
            return HtmlUtils.escapeHtml(text);
        }
        return HtmlUtils.escapeHtml(text.substring(0, prefixLength(text, limit))) + "...";
    }

    private static String limitEntryHtml(String html) {
        return html.length() <= MAX_ENTRY_HTML ? html : Bundle.LBL_ChatEntryTooLarge();
    }

    private void append(ChatEntry entry) {
        if (entry.html.length() > MAX_ENTRY_HTML) {
            entry.html = Bundle.LBL_ChatEntryTooLarge();
            entry.diffs.clear();
            entry.diffChars = 0;
        }
        history.addLast(entry);
        historyChars += entry.html.length();
        diffStore.putAll(entry.diffs);
        diffChars += entry.diffChars;
        if (entry.image != null) {
            chatImages.put(entry.id, entry.image);
        }
        markDirty(entry);
    }

    private void markDirty(ChatEntry entry) {
        dirty.add(entry);
        trimHistory();
        updateChatDisplay();
    }

    private void trimHistory() {
        if (history.size() <= MAX_HISTORY_ENTRIES && historyChars <= MAX_HISTORY_HTML
                && chatImages.size() <= MAX_IMAGES && diffStore.size() <= MAX_DIFFS && diffChars <= MAX_DIFF_CHARS) {
            return;
        }
        // Leave room for new entries so full document rebuilds caused by eviction are infrequent.
        int entryLimit = history.size() > MAX_HISTORY_ENTRIES ? MAX_HISTORY_ENTRIES * 3 / 4 : MAX_HISTORY_ENTRIES;
        int htmlLimit = historyChars > MAX_HISTORY_HTML ? MAX_HISTORY_HTML * 3 / 4 : MAX_HISTORY_HTML;
        int imageLimit = chatImages.size() > MAX_IMAGES ? MAX_IMAGES * 3 / 4 : MAX_IMAGES;
        int diffLimit = diffStore.size() > MAX_DIFFS ? MAX_DIFFS * 3 / 4 : MAX_DIFFS;
        long diffCharsLimit = diffChars > MAX_DIFF_CHARS ? MAX_DIFF_CHARS * 3L / 4 : MAX_DIFF_CHARS;
        // Retain the newest entry, even if it alone consumes most of an attachment budget.
        while (history.size() > 1 && (history.size() > entryLimit || historyChars > htmlLimit
                || chatImages.size() > imageLimit || diffStore.size() > diffLimit || diffChars > diffCharsLimit)) {
            Iterator<ChatEntry> it = history.iterator();
            ChatEntry oldest = it.next();
            if (oldest == activeMessage) {
                oldest = it.next();
            }
            it.remove();
            historyChars -= oldest.html.length();
            oldest.diffs.keySet().forEach(diffStore::remove);
            diffChars -= oldest.diffChars;
            if (oldest.image != null) {
                chatImages.remove(oldest.id);
            }
            dirty.remove(oldest);
            rebuildNeeded |= !historyTrimmed || oldest.element != null;
            historyTrimmed = true;
        }
    }

    // Coalesces bursts of updates into a single re-render.
    private void updateChatDisplay() {
        if (!displayTimer.isRunning()) {
            displayTimer.start();
        }
    }

    void renderChatDisplay() {
        if (dirty.isEmpty() && !rebuildNeeded) {
            return;
        }
        if (rebuildNeeded) {
            rebuildDocument();
        } else {
            HTMLDocument document = (HTMLDocument) chatPane.getDocument();
            Element body = body(document);
            try {
                for (ChatEntry entry : dirty) {
                    if (entry.element == null) {
                        document.insertBeforeEnd(body, entry.markup());
                        Element last = body.getElement(body.getElementCount() - 1);
                        entry.element = entry.id.equals(last.getAttributes().getAttribute(HTML.Attribute.ID))
                                ? last : document.getElement(entry.id);
                    } else {
                        document.setInnerHTML(entry.element, entry.html);
                    }
                }
            } catch (BadLocationException | IOException | RuntimeException ex) {
                LOG.log(Level.FINE, "Could not update the chat document incrementally", ex);
                rebuildDocument();
            }
        }
        dirty.clear();
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void rebuildDocument() {
        StringBuilder html = new StringBuilder("<html><body>");
        if (historyTrimmed) {
            html.append("<div class='system-msg'>").append(Bundle.LBL_ChatHistoryTrimmed()).append("</div>");
        }
        for (ChatEntry entry : history) {
            html.append(entry.markup());
        }
        chatPane.setText(html.append("</body></html>").toString());
        HTMLDocument document = (HTMLDocument) chatPane.getDocument();
        Element body = body(document);
        int index = historyTrimmed ? 1 : 0;
        for (ChatEntry entry : history) {
            Element candidate = index < body.getElementCount() ? body.getElement(index) : null;
            entry.element = candidate != null && entry.id.equals(candidate.getAttributes().getAttribute(HTML.Attribute.ID))
                    ? candidate : document.getElement(entry.id);
            index++;
        }
        rebuildNeeded = false;
    }

    private static Element body(HTMLDocument document) {
        return document.getDefaultRootElement().getElement(1);
    }

    private final class ChatEntry {

        private final String id = "acp-entry-" + entrySequence++;
        private final String css;
        private final Map<String, AcpToolCall.DiffContent> diffs = new HashMap<>();
        private String html;
        private long diffChars;
        private ImageIcon image;
        private Element element;

        ChatEntry(String css, String html) {
            this.css = css;
            this.html = html;
        }

        String markup() {
            return "<div id='" + id + "' class='" + css + "'>" + html + "</div>";
        }
    }
}
