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
package org.netbeans.modules.versioning.history;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.TextUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.versioning.history.AbstractSummaryView.LogEntry;
import org.netbeans.modules.versioning.history.AbstractSummaryView.LogEntry.Event;
import org.netbeans.modules.versioning.history.AbstractSummaryView.MaxPathWidth;
import org.netbeans.modules.versioning.history.AbstractSummaryView.RevisionItem;
import org.netbeans.modules.versioning.history.AbstractSummaryView.SummaryViewMaster.SearchHighlight;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport.IssueLinker;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import static java.util.Locale.ROOT;

/**
 *
 * @author ondra
 */
class SummaryCellRenderer implements ListCellRenderer {
    private static final double DARKEN_FACTOR = 0.90;
    private static final double DARKEN_FACTOR_UNINTERESTING = 0.95;

    private final AbstractSummaryView summaryView;
    private final VCSHyperlinkSupport linkerSupport;

    private final Color selectionBackgroundColor = new JList().getSelectionBackground();
    private final Color selectionBackground = selectionBackgroundColor;
    private Color selectionForeground = new JList().getSelectionForeground();
    private static final Color LINK_COLOR = UIManager.getColor("nb.html.link.foreground"); //NOI18N

    private final ActionRenderer ar = new ActionRenderer();
    private final MoreRevisionsRenderer mr = new MoreRevisionsRenderer();
    private final DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
    private final ListCellRenderer remainingFilesRenderer = new RemainingFilesRenderer();
    private final ListCellRenderer lessFilesRenderer = new LessFilesRenderer();

    private final AttributeSet searchHiliteAttrs;

    private static final Icon ICON_COLLAPSED = UIManager.getIcon("Tree.collapsedIcon"); //NOI18N
    private static final Icon ICON_EXPANDED = UIManager.getIcon("Tree.expandedIcon"); //NOI18N
    private static final int INDENT = ICON_EXPANDED.getIconWidth() + 3;
    private static final JLabel EMPTY_SPACE_LABEL = new JLabel();
    private static final String PREFIX_PATH_FROM = NbBundle.getMessage(SummaryCellRenderer.class, "MSG_SummaryCellRenderer.pathPrefixFrom"); //NOI18N
    private Collection<VCSHyperlinkProvider> hpInstances;
    
    private final Map<Object, Reference<ListCellRenderer>> renderers = new WeakHashMap<>();

    public SummaryCellRenderer(AbstractSummaryView summaryView, VCSHyperlinkSupport linkerSupport) {
        this.summaryView = summaryView;
        this.linkerSupport = linkerSupport;
        searchHiliteAttrs = ((FontColorSettings) MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FontColorSettings.class)).getFontColors("highlight-search"); //NOI18N
    }

    private static Color darker (Color c) {
        return darker(c, DARKEN_FACTOR);
    }

    private static Color darkerUninteresting (Color c) {
        return darker(c, DARKEN_FACTOR_UNINTERESTING);
    }

    private static Color darker (Color c, double factor) {
        return new Color(Math.max((int)(c.getRed() * factor), 0),
             Math.max((int)(c.getGreen() * factor), 0),
             Math.max((int)(c.getBlue() * factor), 0));
    }

    // blend into background
    private static Color desaturate(Color f, Color b) {
        float a = 0.80f;
        return new Color(
            (int)(b.getRed()   + a * (f.getRed()   - b.getRed())),
            (int)(b.getGreen() + a * (f.getGreen() - b.getGreen())),
            (int)(b.getBlue()  + a * (f.getBlue()  - b.getBlue()))
        );
    }

    private static Color lessInteresting (Color c, Color bg) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        r += (bg.getRed() - r) / 5;
        g += (bg.getGreen() - g) / 5;
        b += (bg.getBlue() - b) / 5;
        return new Color(r, g, b);
    }

    private static String getColorString(Color c) {
        return "#" + getHex(c.getRed()) + getHex(c.getGreen()) + getHex(c.getBlue()); //NOI18N
    }

    private static String getHex(int i) {
        String hex = Integer.toHexString(i & 0x000000FF);
        if (hex.length() == 1) {
            hex = "0" + hex; //NOI18N
        }
        return hex;
    }

    @Override
    public Component getListCellRendererComponent (JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof AbstractSummaryView.RevisionItem) {
            ListCellRenderer ren = getRenderer(value);
            if (ren == null) {
                ren = new RevisionRenderer();
                renderers.put(value, new SoftReference<>(ren));
            }
            return ren.getListCellRendererComponent(list, value, index, selected, hasFocus);
        } else if (value instanceof AbstractSummaryView.EventItem) {
            ListCellRenderer ren = getRenderer(value);
            if (ren == null) {
                ren = new EventRenderer();
                renderers.put(value, new SoftReference<>(ren));
            }
            return ren.getListCellRendererComponent(list, value, index, selected, hasFocus);
        } else if (value instanceof AbstractSummaryView.LoadingEventsItem) {
            Component comp = dlcr.getListCellRendererComponent(list, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_LoadingEvents"), index, selected, hasFocus); //NOI18N
            if (comp instanceof JComponent) {
                ((JComponent) comp).setBorder(BorderFactory.createEmptyBorder(0, INDENT, 0, 0));
            }
            return comp;
        } else if (value instanceof AbstractSummaryView.ShowAllEventsItem) {
            return remainingFilesRenderer.getListCellRendererComponent(list, value, index, selected, hasFocus);
        } else if (value instanceof AbstractSummaryView.ShowLessEventsItem) {
            return lessFilesRenderer.getListCellRendererComponent(list, value, index, selected, hasFocus);
        } else if (value instanceof AbstractSummaryView.ActionsItem) {
            return ar.getListCellRendererComponent(list, value, index, selected, hasFocus);
        } else if (value instanceof AbstractSummaryView.MoreRevisionsItem) {
            return mr.getListCellRendererComponent(list, value, index, selected, hasFocus);
        }
        return EMPTY_SPACE_LABEL;
    }

    private static final String FIELDS_SEPARATOR = "      "; //NOI18N

    private int getMaxPathWidth(JList list, RevisionItem revision, Graphics g) {
        assert revision.revisionExpanded;
        assert EventQueue.isDispatchThread();

        Collection<LogEntry.Event> events = revision.getUserData().isEventsInitialized()
                ? revision.getUserData().getEvents()
                : revision.getUserData().getDummyEvents();

        String action = null;
        String longestPath = null;
        for (LogEntry.Event event : events) {
            if (!revision.isEventVisible(event)) {
                continue;
            }
            int i = 0;
            for (String path : getInterestingPaths(event)) {
                if (++i == 2) {
                    path = PREFIX_PATH_FROM + path;
                }
                if (longestPath == null || longestPath.length() < path.length()) {
                    longestPath = path;
                    action = event.getAction();
                }
            }
        }
        if (longestPath != null) {
            FontMetrics fm = list.getFontMetrics(list.getFont());
            Rectangle2D rect = fm.getStringBounds(action + " " + longestPath, g);
            return (int) rect.getWidth() + 1;
        } else {
            return -1;
        }
    }
    
    public Collection<VCSHyperlinkProvider> getHyperlinkProviders() {
        if (hpInstances == null) {
            Lookup.Result<VCSHyperlinkProvider> hpResult = Lookup.getDefault().lookupResult(VCSHyperlinkProvider.class);
            hpInstances = (Collection<VCSHyperlinkProvider>) hpResult.allInstances();
        }
        return hpInstances;
    }

    private ListCellRenderer getRenderer (Object value) {
        Reference<ListCellRenderer> ref = renderers.get(value);
        return ref == null ? null : ref.get();
    }

    private class RevisionRenderer extends JPanel implements ListCellRenderer {

        private String id;
        private boolean lastSelection = false;
        private final RevisionItemCell revisionCell = new RevisionItemCell();
        private final JButton expandButton;
        private boolean lastMessageExpanded;
        private boolean lastRevisionExpanded;
        private int lastWidth;
        private Collection<SearchHighlight> lastHighlights;

        public RevisionRenderer() {
            selectionForeground = new JList().getSelectionForeground();
            expandButton = new LinkButton(ICON_COLLAPSED);
            expandButton.setBorder(BorderFactory.createEmptyBorder());

            this.setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, UIManager.getColor("List.background"))); //NOI18N
            this.setLayout(new BorderLayout(3, 0));

            expandButton.setMaximumSize(expandButton.getPreferredSize());
            expandButton.setMinimumSize(expandButton.getPreferredSize());
            this.add(expandButton, BorderLayout.WEST);
            this.add(revisionCell, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean selected, boolean hasFocus) {
            AbstractSummaryView.RevisionItem item = (AbstractSummaryView.RevisionItem) value;
            AbstractSummaryView.LogEntry entry = item.getUserData();

            Collection<SearchHighlight> highlights = summaryView.getMaster().getSearchHighlights();
            if (revisionCell.getRevisionControl().getStyledDocument().getLength() == 0
                    || revisionCell.getDateControl().getStyledDocument().getLength() == 0
                    || revisionCell.getAuthorControl().getStyledDocument().getLength() == 0
                    || revisionCell.getCommitMessageControl().getStyledDocument().getLength() == 0
                    || selected != lastSelection || item.messageExpanded != lastMessageExpanded || item.revisionExpanded != lastRevisionExpanded
                    || !highlights.equals(lastHighlights)) {

                lastSelection = selected;
                lastMessageExpanded = item.messageExpanded;
                lastRevisionExpanded = item.revisionExpanded;
                lastHighlights = highlights;

                Color backgroundColor;

                if (selected) {
                    backgroundColor = selectionBackground;
                } else {
                    backgroundColor = UIManager.getColor("List.background"); //NOI18N
                    backgroundColor = entry.isLessInteresting() ? darkerUninteresting(backgroundColor) : darker(backgroundColor);
                }
                this.setBackground(backgroundColor);
                revisionCell.setBackground(backgroundColor);

                if (item.revisionExpanded) {
                    expandButton.setIcon(ICON_EXPANDED);
                } else {
                    expandButton.setIcon(ICON_COLLAPSED);
                }

                id = item.getItemId();
                if (linkerSupport.getLinker(ExpandLink.class, id) == null) {
                    linkerSupport.add(new ExpandLink(item), id);
                }

                try {
                    addRevision(revisionCell.getRevisionControl(), item, selected, highlights);
                    addCommitMessage(revisionCell.getCommitMessageControl(), item, selected, highlights);
                    addAuthor(revisionCell.getAuthorControl(), item, selected, highlights);
                    addDate(revisionCell.getDateControl(), item, selected);
                } catch (BadLocationException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            lastWidth = resizePane(revisionCell.getCommitMessageControl().getText(), list, lastWidth);

            return this;
        }

        @SuppressWarnings("empty-statement")
        private int resizePane (String text, JList list, int lastWidth) {
            if (text == null) {
                text = ""; //NOI18N
            }
            int width = summaryView.getMaster().getComponent().getWidth();
            if (width > 0 && width != lastWidth) {
                String[] rows = text.split("\n"); //NOI18N
                FontMetrics fm = list.getFontMetrics(list.getFont());
                int lines = 0;
                for (String row : rows) {
                    Rectangle2D rect = fm.getStringBounds(row, revisionCell.getGraphics());
                    lines += (int) (rect.getWidth() / (width - 80) + 1);
                }
                int ph = fm.getHeight() * (lines + 1) + 4;
                revisionCell.setPreferredSize(new Dimension(width - 50 - ICON_COLLAPSED.getIconWidth(), ph));
                setPreferredSize(revisionCell.getPreferredSize());
            }
            return width;
        }

        private void addRevision (JTextPane pane, RevisionItem item, boolean selected, Collection<SearchHighlight> highlights) throws BadLocationException {
            StyledDocument sd = pane.getStyledDocument();
            // clear document
            clearSD(pane, sd);

            Style selectedStyle = createSelectedStyle(pane);
            Style normalStyle = createNormalStyle(pane);
            Style hiliteStyle = createHiliteStyleStyle(pane, normalStyle, searchHiliteAttrs);
            Style style;
            if (selected) {
                style = selectedStyle;
            } else {
                style = normalStyle;
            }


            // add revision
            sd.insertString(0, item.getUserData().getRevision(), style);
            if (!selected) {
                for (AbstractSummaryView.LogEntry.RevisionHighlight highlight : item.getUserData().getRevisionHighlights()) {
                    Style s = pane.addStyle(null, normalStyle);
                    StyleConstants.setForeground(s, highlight.getForeground());
                    StyleConstants.setBackground(s, highlight.getBackground());
                    sd.setCharacterAttributes(highlight.getStart(), highlight.getLength(), s, false);
                }
                for (SearchHighlight highlight : highlights) {
                    if (highlight.getKind() == SearchHighlight.Kind.REVISION) {
                        int doclen = sd.getLength();
                        String highlightMessage = highlight.getSearchText();
                        String revisionText = item.getUserData().getRevision().toLowerCase(ROOT);
                        int idx = revisionText.indexOf(highlightMessage);
                        if (idx > -1) {
                            sd.setCharacterAttributes(doclen - revisionText.length() + idx, highlightMessage.length(), hiliteStyle, false);
                        }
                    }
                }
            }
        }

        private void addAuthor (JTextPane pane, RevisionItem item, boolean selected, Collection<SearchHighlight> highlights) throws BadLocationException {
            LogEntry entry = item.getUserData();
            StyledDocument sd = pane.getStyledDocument();
            clearSD(pane, sd);
            Style selectedStyle = createSelectedStyle(pane);
            Style normalStyle = createNormalStyle(pane);
            Style style;
            if (selected) {
                style = selectedStyle;
            } else {
                style = normalStyle;
            }
            Style hiliteStyle = createHiliteStyleStyle(pane, normalStyle, searchHiliteAttrs);
            String author = entry.getAuthor();
            sd.insertString(sd.getLength(), author, style);
            if (!selected) {
                int pos = sd.getLength();
                for (SearchHighlight highlight : highlights) {
                    if (highlight.getKind() == SearchHighlight.Kind.AUTHOR) {
                        int doclen = sd.getLength();
                        String highlightMessage = highlight.getSearchText();
                        String authorText = sd.getText(pos, doclen - pos).toLowerCase();
                        int idx = authorText.indexOf(highlightMessage);
                        if (idx > -1) {
                            sd.setCharacterAttributes(doclen - authorText.length() + idx, highlightMessage.length(), hiliteStyle, false);
                        }
                    }
                }
            }
        }

        private void addDate (JTextPane pane, RevisionItem item, boolean selected) throws BadLocationException {

            LogEntry entry = item.getUserData();
            StyledDocument sd = pane.getStyledDocument();
            // clear document
            clearSD(pane, sd);

            Style selectedStyle = createSelectedStyle(pane);
            Style normalStyle = createNormalStyle(pane);
            Style style;
            if (selected) {
                style = selectedStyle;
            } else {
                style = normalStyle;
            }

            // add date
            sd.insertString(sd.getLength(), entry.getDate(), style);
        }

        private void addCommitMessage (JTextPane pane, RevisionItem item, boolean selected, Collection<SearchHighlight> highlights) throws BadLocationException {
            LogEntry entry = item.getUserData();
            StyledDocument sd = pane.getStyledDocument();
            clearSD(pane, sd);
            Style selectedStyle = createSelectedStyle(pane);
            Style normalStyle = createNormalStyle(pane);
            Style linkStyle = createLinkStyle(pane, normalStyle);
            Style hiliteStyle = createHiliteStyleStyle(pane, normalStyle, searchHiliteAttrs);
            Style issueHyperlinkStyle = createIssueHyperlinkStyle(pane, normalStyle);
            Style style;
            if (selected) {
                style = selectedStyle;
            } else {
                style = normalStyle;
            }
            boolean messageChanged = !entry.getMessage().isEmpty();
            String commitMessage = entry.getMessage().strip();
            int nlc;
            int i;
            for (i = 0, nlc = -1; i != -1; i = commitMessage.indexOf('\n', i + 1), nlc++);
            
            if (nlc > 0 && !item.messageExpanded) {
                //get first line of comment if collapsed
                commitMessage = commitMessage.substring(0, commitMessage.indexOf("\n")); //NOI18N
            }
            IssueLinker l = linkerSupport.getLinker(VCSHyperlinkSupport.IssueLinker.class, id);
            if (messageChanged) {
                lastWidth = -1;
                if (l != null) {
                    // must reinitialize issue linker to paint the new message
                    linkerSupport.remove(l, id);
                    l = null;
                }
            }
            if(l == null) {
                for (VCSHyperlinkProvider hp : getHyperlinkProviders()) {
                    l = VCSHyperlinkSupport.IssueLinker.create(hp, issueHyperlinkStyle, summaryView.getRoot(), sd, commitMessage);
                    if(l != null) {
                        linkerSupport.add(l, id);
                        break; // get the first one
                    }
                }
            }
            if(l != null) {
                l.insertString(sd, style);
            } else {
                sd.insertString(0, commitMessage, style);
            }

            {
                //make the first line bold
                int lineEnd = sd.getText(0, sd.getLength()).indexOf("\n");
                if (lineEnd == -1) {
                    lineEnd = sd.getLength();
                }
                Style s = pane.addStyle(null, style);
                if (item.getUserData().isLessInteresting()) {
                    StyleConstants.setForeground(s, desaturate(UIManager.getColor("List.foreground"), UIManager.getColor("List.background")));
                } else {
                    StyleConstants.setBold(s, true);
                }
                sd.setCharacterAttributes(0, lineEnd, s, false);
            }
            
            int msglen = commitMessage.length();
            int doclen = sd.getLength();

            
            if (nlc > 0) {
                //insert expand/collapse link
                ExpandMsgHyperlink el = linkerSupport.getLinker(ExpandMsgHyperlink.class, id);
                if (el == null) {
                    el = item.messageExpanded ? createCollapseMsgHyperlink(item, sd.getLength(), id)
                                              : createExpandMsgHyperlink(item, sd.getLength(), id);
                    linkerSupport.add(el, id);
                }
                el.insertString(sd, linkStyle);
            }

            {
                // remove previous tooltips
                MessageTooltip mtt = linkerSupport.getLinker(MessageTooltip.class, id);
                linkerSupport.remove(mtt, id);
                //insert commit message tooltip
                MessageTooltip messageTooltip = new MessageTooltip(entry.getMessage(), 0, sd.getLength());
                linkerSupport.add(messageTooltip, id);
            }
            
            for (SearchHighlight highlight : highlights) {
                if (highlight.getKind() == SearchHighlight.Kind.MESSAGE) {
                    String highlightMessage = highlight.getSearchText();
                    int idx = commitMessage.toLowerCase().indexOf(highlightMessage);
                    if (idx == -1) {
                        if (nlc > 0 && !item.messageExpanded && entry.getMessage().toLowerCase().contains(highlightMessage)) {
                            sd.setCharacterAttributes(doclen, sd.getLength(), hiliteStyle, false);
                        }
                    } else {
                        sd.setCharacterAttributes(doclen - msglen + idx, highlightMessage.length(), hiliteStyle, false);
                    }
                }
            }
        }

        private Style createNormalStyle (JTextPane textPane) {
            Style normalStyle = textPane.addStyle("normal", null); //NOI18N
            StyleConstants.setForeground(normalStyle, UIManager.getColor("List.foreground")); //NOI18N
            return normalStyle;
        }

        private Style createIssueHyperlinkStyle (JTextPane textPane, Style normalStyle) {
            Style issueHyperlinkStyle = textPane.addStyle("issuehyperlink", normalStyle); //NOI18N
            StyleConstants.setForeground(issueHyperlinkStyle, LINK_COLOR == null ? Color.BLUE : LINK_COLOR);
            StyleConstants.setUnderline(issueHyperlinkStyle, true);
            return issueHyperlinkStyle;
        }

        private Style createAuthorStyle (JTextPane textPane, Style normalStyle) {
            Style authorStyle = textPane.addStyle("author", normalStyle); //NOI18N
            StyleConstants.setForeground(authorStyle, LINK_COLOR == null ? Color.BLUE : LINK_COLOR);
            return authorStyle;
        }

        private Style createLinkStyle (JTextPane textPane, Style normalStyle) {
            Style linkStyle = textPane.addStyle("link", normalStyle); //NOI18N
            StyleConstants.setForeground(linkStyle, LINK_COLOR == null ? Color.BLUE : LINK_COLOR);
            StyleConstants.setBold(linkStyle, true);
            return linkStyle;
        }

        private Style createNoindentStyle (JTextPane textPane) {
            Style noindentStyle = textPane.addStyle("noindent", null); //NOI18N
            StyleConstants.setLeftIndent(noindentStyle, 0);
            return noindentStyle;
        }

        private Style createSelectedStyle (JTextPane textPane) {
            Style selectedStyle = textPane.addStyle("selected", null); //NOI18N
            StyleConstants.setForeground(selectedStyle, selectionForeground);
            StyleConstants.setBackground(selectedStyle, selectionBackground);
            return selectedStyle;
        }

        private Style createHiliteStyleStyle (JTextPane textPane, Style normalStyle, AttributeSet searchHiliteAttrs) {
            Style hiliteStyle = textPane.addStyle("hilite", normalStyle); //NOI18N

            Color c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Background);
            if (c != null) {
                StyleConstants.setBackground(hiliteStyle, c);
            }
            c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Foreground);
            if (c != null) {
                StyleConstants.setForeground(hiliteStyle, c);
            }

            return hiliteStyle;
        }
        
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            IssueLinker issue = linkerSupport.getLinker(IssueLinker.class, id);
            if (issue != null) {
                issue.computeBounds(revisionCell.getCommitMessageControl(), revisionCell);
            }
            ExpandMsgHyperlink expandMsg = linkerSupport.getLinker(ExpandMsgHyperlink.class, id);
            if (expandMsg != null) {
                expandMsg.computeBounds(revisionCell.getCommitMessageControl(), revisionCell);
            }
            MessageTooltip tt = linkerSupport.getLinker(MessageTooltip.class, id);
            if (tt != null) {
                tt.computeBounds(revisionCell.getCommitMessageControl(), revisionCell);
            }
            ExpandLink link = linkerSupport.getLinker(ExpandLink.class, id);
            if (link != null) {
                link.computeBounds(expandButton);
            }
        }

        private void clearSD (JTextPane pane, StyledDocument sd) {
            try {
                Style noindentStyle = createNoindentStyle(pane);
                sd.remove(0, sd.getLength());
                sd.setParagraphAttributes(0, sd.getLength(), noindentStyle, false);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private static class MessageTooltip extends VCSHyperlinkSupport.Hyperlink {
        private Rectangle[] bounds;
        private final int start;
        private final int end;
        private final String text;

        private MessageTooltip (String text, int start, int end) {
            this.start = start;
            this.end = end;
            this.text = prepareText(text);
        }
        
        @Override
        public boolean mouseMoved (Point p, JComponent component) {
            if (bounds != null && component.getToolTipText() == null) {
                for (Rectangle b : bounds) {
                    if (b.contains(p)) {
                        component.setToolTipText(text);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean mouseClicked (Point p) {
            return false;
        }
        
        @Override
        public void computeBounds(JTextPane textPane) {
            computeBounds(textPane, null);
        }
        
        public void computeBounds(JTextPane textPane, VCSHyperlinkSupport.BoundsTranslator translator) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            try {
                int lastY = -1;
                Rectangle rec = null;
                List<Rectangle> rects = new LinkedList<>();
                // get bounds for every line
                for (int pos = start; pos <= end; ++pos) {
                    Rectangle startr = tui.modelToView(textPane, pos, Position.Bias.Forward);
                    Rectangle endr = tui.modelToView(textPane, pos + 1, Position.Bias.Backward);
                    //prevent NPE if width is too small
                    if (null == startr) {continue;}
                    if (null == endr) {continue;}
                    if (startr.y > lastY) {
                        rects.add(rec);
                        rec = new Rectangle(tpBounds.x + startr.x, startr.y, endr.x - startr.x, startr.height);
                        lastY = rec.y;
                    } else {
                        rec.setSize(rec.width + endr.x - startr.x, rec.height);
                    }
                }
                // NOTE the textPane is positioned within a parent panel so the relative bound has to be modified too
                if (null != translator){
                    translator.correctTranslation(textPane, rec);
                }
                rects.add(rec);
                rects.remove(0);
                bounds = rects.toArray(Rectangle[]::new);
            } catch (BadLocationException ex) {
                bounds = null;
            }
        }

        private String prepareText (String text) {
            return "<html><body>" + text.replace("\n", "<br>") + "</body></html>"; //NOI18N
        }
        
    }

    private class EventRenderer extends JPanel implements ListCellRenderer {
        
        private boolean lastSelection = false;
        private String lastSearch = null;
        private int lastShowingFiles = -1;
        private final JLabel pathLabel;
        private final JLabel actionLabel;
        private final JButton actionButton;
        private String id;
        private final String PATH_COLOR = getColorString(lessInteresting(UIManager.getColor("List.foreground"), UIManager.getColor("List.background"))); //NOI18N
        private final String SEARCH_COLOR_BG = getColorString((Color) Objects.requireNonNullElse(searchHiliteAttrs.getAttribute(StyleConstants.Background), Color.BLUE)); //NOI18N
        private final String SEARCH_COLOR_FG = getColorString((Color) Objects.requireNonNullElse(searchHiliteAttrs.getAttribute(StyleConstants.Foreground), UIManager.getColor("List.foreground"))); //NOI18N

        public EventRenderer () {
            pathLabel = new JLabel();
            actionLabel = new JLabel();
            actionButton = new LinkButton("=>"); //NOI18N
            actionButton.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

            FlowLayout l = new FlowLayout(FlowLayout.LEFT, 0, 0);
            l.setAlignOnBaseline(true);
            setLayout(l);
            add(actionLabel);
            actionLabel.setBorder(BorderFactory.createEmptyBorder(0, INDENT, 0, 10));
            add(pathLabel);
            add(actionButton);
        }
        
        @Override
        @SuppressWarnings("NestedAssignment")
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean selected, boolean hasFocus) {
            AbstractSummaryView.EventItem item = (AbstractSummaryView.EventItem) value;
            String search = getSearchString();
            if (pathLabel.getText().isEmpty() || lastSelection != selected || seearchUpdate(item, lastSearch, search)) {
                lastSelection = selected;
                lastSearch = search;
                Color foregroundColor, backgroundColor;
                if (selected) {
                    foregroundColor = selectionForeground;
                    backgroundColor = selectionBackground;
                } else {
                    foregroundColor = UIManager.getColor("List.foreground"); //NOI18N
                    backgroundColor = UIManager.getColor("List.background"); //NOI18N
                }
                id = item.getItemId();
                if (linkerSupport.getLinker(ExpandLink.class, id) == null) {
                    linkerSupport.add(new EventActionsLink(item), id);
                }
                pathLabel.setFont(list.getFont());
                pathLabel.setForeground(foregroundColor);
                pathLabel.setBackground(backgroundColor);
                actionLabel.setBackground(backgroundColor);
                setBackground(backgroundColor);

                StringBuilder sb = new StringBuilder(80).append("<html><body>"); //NOI18N
                sb.append("<b>"); //NOI18N
                String action = item.getUserData().getAction();
                String color = summaryView.getActionColors().get(action);
                if (color != null && !selected) {
                    sb.append("<font color=\"").append(color).append("\">").append(action).append("</font>"); //NOI18N
                } else  {
                    actionLabel.setForeground(foregroundColor);
                    sb.append(action);
                }
                sb.append("</b></body></html>"); //NOI18N
                actionLabel.setText(sb.toString());

                sb = new StringBuilder(180).append("<html><body>"); //NOI18N
                int i = 0;
                for (String path : getInterestingPaths(item.getUserData())) {
                    if (++i == 2 && path == null) {
                        continue;
                    }
                    int idx = path.lastIndexOf("/"); //NOI18N
                    if (i == 2) {
                        // additional path information (like replace from, copied from, etc.)
                        sb.append("<br>").append(PREFIX_PATH_FROM); //NOI18N
                    }
                    if (idx < 0 || selected || search != null) {
                        int matchStart;
                        if (search != null && (matchStart = path.toLowerCase(ROOT).lastIndexOf(search)) != -1) {
                            int matchEnd = matchStart + search.length();
                            sb.append(path.substring(0, matchStart));
                            sb.append("<span style=\"background-color:").append(SEARCH_COLOR_BG).append(";color:").append(SEARCH_COLOR_FG).append(";\">");
                            sb.append(path.substring(matchStart, matchEnd));
                            sb.append("</span>");
                            sb.append(path.substring(matchEnd, path.length()));
                        } else {
                            sb.append(path);
                        }
                    } else {
                        ++idx;
                        sb.append("<font color=\"").append(PATH_COLOR).append("\">").append(path.substring(0, idx)).append("</font>"); //NOI18N
                        sb.append(path.substring(idx));
                    }
                }
                pathLabel.setText(sb.append("</body></html>").toString()); //NOI18N
            }
            RevisionItem rev = item.getParent();
            if (rev.showingFiles == -1 || rev.showingFiles != lastShowingFiles) {
                lastShowingFiles = rev.showingFiles;
                if (rev.maxPathWidth == null || rev.maxPathWidth.visiblePaths != rev.showingFiles) {
                    rev.maxPathWidth = new MaxPathWidth(rev.showingFiles, getMaxPathWidth(list, rev, pathLabel.getGraphics()));
                }
                int width = rev.maxPathWidth.maxPathWidth;
                if (width > -1) {
                    width = width + 15 + INDENT - actionLabel.getPreferredSize().width;
                    pathLabel.setPreferredSize(new Dimension(width, pathLabel.getPreferredSize().height));
                }
            }
            return this;
        }

        @SuppressWarnings("AssignmentToForLoopParameter")
        private boolean seearchUpdate(AbstractSummaryView.EventItem item, String oldsearch, String newsearch) {
            if (Objects.equals(oldsearch, newsearch)) {
                return false;
            } else {
                for (String path : getInterestingPaths(item.getUserData())) {
                    path = path.toLowerCase(ROOT);
                    if (oldsearch != null && path.contains(oldsearch)) {
                        return true;
                    }
                    if (newsearch != null && path.contains(newsearch)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private String getSearchString() {
            for (SearchHighlight search : summaryView.getMaster().getSearchHighlights()) {
                if (search.getKind() == SearchHighlight.Kind.FILE) {
                    return search.getSearchText().isBlank() ? null : search.getSearchText();
                }
            }
            return null;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            EventActionsLink link = linkerSupport.getLinker(EventActionsLink.class, id);
            if (link != null) {
                link.computeBounds(actionButton);
            }
        }

    }

    private static List<String> getInterestingPaths(Event event) {
        String path = event.getPath();
        String original = event.getOriginalPath();
        return original != null && !path.equals(original) ? List.of(path, original) : List.of(path);
    }
    
    private class RemainingFilesRenderer extends JPanel implements ListCellRenderer{
        private String id;
        private Component comp;

        public RemainingFilesRenderer () {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBorder(BorderFactory.createEmptyBorder(0, INDENT, 3, 0));
        }

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            AbstractSummaryView.ShowAllEventsItem item = (AbstractSummaryView.ShowAllEventsItem) value;
            id = item.getItemId();
            if (linkerSupport.getLinker(ShowRemainingFilesLink.class, id) == null) {
                linkerSupport.add(new ShowRemainingFilesLink(item.getParent()), id);
            }
            StringBuilder sb = new StringBuilder("<html><a href=\"expand\">"); //NOI18N
            int i = item.getParent().getNextFilesToShowCount();
            String label;
            if (i > 0) {
                label = NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowMoreFiles", i); //NOI18N
            } else {
                label = NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowAllFiles"); //NOI18N
            }
            if (isSelected) {
                Component c = dlcr.getListCellRendererComponent(list, "<html><a href=\"expand\">ACTION_NAME</a>", index, isSelected, cellHasFocus); //NOI18N
                sb.append("<font color=\"").append(getColorString(c.getForeground())).append("\">") //NOI18N
                        .append(label).append("</font>"); //NOI18N
            } else if (LINK_COLOR != null) {
                sb.append("<font color=\"").append(getColorString(LINK_COLOR)).append("\">") //NOI18N
                        .append(label).append("</font>"); //NOI18N
            } else {
                sb.append(label);
            }
            sb.append("</a></html>"); //NOI18N
            comp = dlcr.getListCellRendererComponent(list, sb.toString(), index, isSelected, cellHasFocus);
            removeAll();
            add(comp);
            comp.setMaximumSize(comp.getPreferredSize());
            setBackground(comp.getBackground());
            return this;
        }

        @Override
        public void paint (Graphics g) {
            super.paint(g);
            ShowRemainingFilesLink link = linkerSupport.getLinker(ShowRemainingFilesLink.class, id);
            if (link != null) {
                link.computeBounds(comp);
            }
        }
        
    }
    
    private class LessFilesRenderer extends JPanel implements ListCellRenderer{
        private String id;
        private Component comp;

        public LessFilesRenderer () {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBorder(BorderFactory.createEmptyBorder(0, INDENT, 3, 0));
        }

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            AbstractSummaryView.ShowLessEventsItem item = (AbstractSummaryView.ShowLessEventsItem) value;
            id = item.getItemId();
            if (linkerSupport.getLinker(ShowLessFilesLink.class, id) == null) {
                linkerSupport.add(new ShowLessFilesLink(item.getParent()), id);
            }
            StringBuilder sb = new StringBuilder("<html><a href=\"expand\">"); //NOI18N
            String label = NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowLessFiles"); //NOI18N
            if (isSelected) {
                Component c = dlcr.getListCellRendererComponent(list, "<html><a href=\"expand\">ACTION_NAME</a>", index, isSelected, cellHasFocus); //NOI18N
                sb.append("<font color=\"").append(getColorString(c.getForeground())).append("\">") //NOI18N
                        .append(label).append("</font>"); //NOI18N
            } else if (LINK_COLOR != null) {
                sb.append("<font color=\"").append(getColorString(LINK_COLOR)).append("\">") //NOI18N
                        .append(label).append("</font>"); //NOI18N
            } else {
                sb.append(label);
            }
            sb.append("</a></html>"); //NOI18N
            comp = dlcr.getListCellRendererComponent(list, sb.toString(), index, isSelected, cellHasFocus);
            removeAll();
            add(comp);
            comp.setMaximumSize(comp.getPreferredSize());
            setBackground(comp.getBackground());
            return this;
        }

        @Override
        public void paint (Graphics g) {
            super.paint(g);
            ShowLessFilesLink link = linkerSupport.getLinker(ShowLessFilesLink.class, id);
            if (link != null) {
                link.computeBounds(comp);
            }
        }
        
    }

    private class ActionRenderer extends JPanel implements ListCellRenderer{
        private String id;
        private Map<Component, Action> labels;
        private final Map<String, JLabel> ACTION_LABELS = new HashMap<>();

        public ActionRenderer () {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            setBorder(BorderFactory.createEmptyBorder(3, INDENT - 5, 5, 0));
        }
        
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Action[] actions = ((AbstractSummaryView.ActionsItem) value).getParent().getUserData().getActions();
            id = ((AbstractSummaryView.ActionsItem) value).getItemId();
            removeAll();
            labels = new HashMap<>(actions.length);
            Component comp = dlcr.getListCellRendererComponent(list, "<html><a href=\"action\">ACTION_NAME</a>", index, isSelected, cellHasFocus); //NOI18N
            setBackground(comp.getBackground());
            for (Action a : actions) {
                JLabel label = getLabelFor((String) a.getValue(Action.NAME), isSelected ? comp.getForeground() : LINK_COLOR);
                label.setForeground(comp.getForeground());
                label.setBackground(comp.getBackground());
                label.setBorder(BorderFactory.createEmptyBorder());
                labels.put(label, a);
                add(label);
            }
            if (linkerSupport.getLinker(ActionHyperlink.class, id) == null) {
                linkerSupport.add(new ActionHyperlink(), id);
            }
            return this;
        }

        @Override
        public void paint (Graphics g) {
            super.paint(g);
            ActionHyperlink link = linkerSupport.getLinker(ActionHyperlink.class, id);
            if (link != null) {
                link.computeBounds(labels);
            }
        }

        private JLabel getLabelFor (String actionName, Color fontColor) {
            JLabel lbl = ACTION_LABELS.computeIfAbsent(actionName, k -> new JLabel());
            StringBuilder sb = new StringBuilder("<html><a href=\"action\">"); //NOI18N
            if (fontColor == null) {
                sb.append(actionName);
            } else {
                sb.append("<font color=\"").append(getColorString(fontColor)).append("\">").append(actionName).append("</font>"); //NOI18N
            }
            sb.append("</a></html>"); //NOI18N
            lbl.setText(sb.toString());
            return lbl;
        }
        
    }

    private class MoreRevisionsRenderer extends JPanel implements ListCellRenderer{
        private String id;
        private final List<JLabel> labels;
        private final Color backgroundColor;
        private final JLabel more10Label;
        private final JLabel allLabel;
        private final JLabel more100Label;
        private final JLabel more50Label;
        private final Map<Component, String> tooltips;
        private final Map<Component, Integer> moreLabelValues;

        @SuppressWarnings("NestedAssignment")
        public MoreRevisionsRenderer () {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 3));
            setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, UIManager.getColor("List.background"))); //NOI18N
            labels = new ArrayList<>();
            labels.add(new JLabel(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowMore"))); //NOI18N
            labels.add(more10Label = new JLabel());
            labels.add(new JLabel("/")); //NOI18N
            labels.add(more50Label = new JLabel());
            labels.add(new JLabel("/")); //NOI18N
            labels.add(more100Label = new JLabel());
            labels.add(new JLabel("/")); //NOI18N
            labels.add(allLabel = new JLabel());
            for (JLabel lbl : labels) {
                lbl.setBorder(BorderFactory.createEmptyBorder());
                add(lbl);
            }
            labels.get(0).setBorder(BorderFactory.createEmptyBorder(0, INDENT, 0, 0));
            backgroundColor = darker(UIManager.getColor("List.background")); //NOI18N
            
            moreLabelValues = Map.of(
                more10Label, 10,
                more50Label, 50,
                more100Label, 100,
                allLabel, -1
            );
            
            tooltips = Map.of(
                more10Label, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_Show10MoreRevisions"), //NOI18N
                more50Label, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_Show50MoreRevisions"), //NOI18N
                more100Label, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_Show100MoreRevisions"), //NOI18N
                allLabel, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowMoreRevisionsAll") //NOI18N
            );
        }
        
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            id = ((AbstractSummaryView.MoreRevisionsItem) value).getItemId();
            if (linkerSupport.getLinker(MoreRevisionsHyperlink.class, id) == null) {
                linkerSupport.add(new MoreRevisionsHyperlink(), id);
            }
            Component comp = dlcr.getListCellRendererComponent(list, "<html><a href=\"more\">MORE</a>", index, isSelected, cellHasFocus); //NOI18N
            setLabelLinkText(more10Label, "10", isSelected ? comp.getForeground() : LINK_COLOR); //NOI18N
            setLabelLinkText(more50Label, "50", isSelected ? comp.getForeground() : LINK_COLOR); //NOI18N
            setLabelLinkText(more100Label, "100", isSelected ? comp.getForeground() : LINK_COLOR); //NOI18N
            setLabelLinkText(allLabel, NbBundle.getMessage(SummaryCellRenderer.class, "MSG_AllRevisions"), isSelected ? comp.getForeground() : LINK_COLOR); //NOI18N
            for (JLabel lbl : labels) {
                lbl.setForeground(comp.getForeground());
                lbl.setBackground(isSelected ? comp.getBackground() : backgroundColor);
            }
            setBackground(isSelected ? comp.getBackground() : backgroundColor);
            return this;
        }

        @Override
        public void paint (Graphics g) {
            super.paint(g);
            MoreRevisionsHyperlink link = linkerSupport.getLinker(MoreRevisionsHyperlink.class, id);
            if (link != null) {
                link.computeBounds();
            }
        }

        private JLabel setLabelLinkText (JLabel lbl, String text, Color fgColor) {
            StringBuilder sb = new StringBuilder("<html><a href=\"more\">"); //NOI18N
            if (fgColor == null) {
                sb.append(text);
            } else {
                sb.append("<font color=\"").append(getColorString(fgColor)).append("\">").append(text).append("</font>"); //NOI18N
            }
            sb.append("</a></html>"); //NOI18N
            lbl.setText(sb.toString());
            return lbl;
        }

        private class MoreRevisionsHyperlink extends VCSHyperlinkSupport.Hyperlink {
            private Map<Component, Rectangle> bounds = Collections.emptyMap();

            @Override
            public void computeBounds (JTextPane textPane) {

            }

            public void computeBounds () {
                bounds = Map.of(
                    more10Label, more10Label.getBounds(),
                    more50Label, more50Label.getBounds(),
                    more100Label, more100Label.getBounds(),
                    allLabel, allLabel.getBounds()
                );
            }

            @Override
            public boolean mouseMoved (Point p, JComponent component) {
                for (Map.Entry<Component, Rectangle> e : bounds.entrySet()) {
                    if (e.getValue().contains(p)) {
                        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        component.setToolTipText(tooltips.get(e.getKey()));
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean mouseClicked (Point p) {
                for (Map.Entry<Component, Rectangle> e : bounds.entrySet()) {
                    if (e.getValue().contains(p)) {
                        summaryView.moreRevisions(moreLabelValues.get(e.getKey()));
                        return true;
                    }
                }
                return false;
            }
        }
    }

    private class ActionHyperlink extends VCSHyperlinkSupport.Hyperlink {
        private Map<Component, Rectangle> bounds = Collections.<Component, Rectangle>emptyMap();
        private Map<Component, Action> labels;

        public ActionHyperlink () {
        }

        @Override
        public void computeBounds (JTextPane textPane) {
            
        }
        
        public void computeBounds (Map<Component, Action> labels) {
            this.labels = labels;
            bounds = new HashMap<>(labels.size());
            for (Map.Entry<Component, Action> e : labels.entrySet()) {
                bounds.put(e.getKey(), e.getKey().getBounds());
            }
        }

        @Override
        public boolean mouseMoved (Point p, JComponent component) {
            for (Map.Entry<Component, Rectangle> e : bounds.entrySet()) {
                if (e.getValue().contains(p)) {
                    component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    component.setToolTipText((String) labels.get(e.getKey()).getValue(Action.NAME));
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseClicked (Point p) {
            for (Map.Entry<Component, Rectangle> e : bounds.entrySet()) {
                if (e.getValue().contains(p)) {
                    Utils.setWaitCursor(true);
                    try {
                        labels.get(e.getKey()).actionPerformed(new ActionEvent(labels.get(e.getKey()), ActionEvent.ACTION_PERFORMED, null));
                    } finally {
                        Utils.setWaitCursor(false);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public ExpandMsgHyperlink createExpandMsgHyperlink(AbstractSummaryView.RevisionItem item, int startoffset, String revision) {
        return new ExpandMsgHyperlink(item, startoffset, revision, " (...)", NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ExpandCommitMessage")); //NOI18N
    }

    public ExpandMsgHyperlink createCollapseMsgHyperlink(AbstractSummaryView.RevisionItem item, int startoffset, String revision) {
        return new ExpandMsgHyperlink(item, startoffset, revision, " ^^^", NbBundle.getMessage(SummaryCellRenderer.class, "MSG_CollapseCommitMessage")); //NOI18N
    }

    private class ExpandMsgHyperlink extends VCSHyperlinkSupport.StyledDocumentHyperlink {
        private Rectangle bounds;
        private final int startoffset;
        private final AbstractSummaryView.RevisionItem item;
        private final String revision;
        private final String linkString;
        private final String toolTip;

        private ExpandMsgHyperlink(RevisionItem item, int startoffset, String revision, String linkString, String toolTip) {
            this.startoffset = startoffset;
            this.revision = revision;
            this.item = item;
            this.linkString = linkString;
            this.toolTip = toolTip;
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                component.setToolTipText(toolTip);
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                item.messageExpanded = !item.messageExpanded;
                linkerSupport.remove(this, revision);
                summaryView.itemChanged(p);
                return true;
            }
            return false;
        }
        
        @Override
        public void computeBounds(JTextPane textPane) {
            computeBounds(textPane, null);
        }
        
        public void computeBounds(JTextPane textPane, VCSHyperlinkSupport.BoundsTranslator translator) {
            Rectangle tpBounds = textPane.getBounds();
            TextUI tui = textPane.getUI();
            bounds = new Rectangle();
            try {
                Rectangle mtv = tui.modelToView(textPane, startoffset, Position.Bias.Forward);
                if(mtv == null) return;
                Rectangle startr = mtv.getBounds();
                mtv = tui.modelToView(textPane, startoffset + linkString.length(), Position.Bias.Backward);
                if(mtv == null) return;
                Rectangle endr = mtv.getBounds();

                bounds = new Rectangle(tpBounds.x + startr.x, startr.y, endr.x - startr.x, startr.height);
                if (null != translator) {
                    translator.correctTranslation(textPane, bounds);
                }
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void insertString (StyledDocument sd, Style style) throws BadLocationException {
            sd.insertString(startoffset, linkString, style);
        }
    }

    public class ExpandLink extends VCSHyperlinkSupport.Hyperlink {

        private Rectangle bounds;
        private final AbstractSummaryView.RevisionItem item;

        private ExpandLink (AbstractSummaryView.RevisionItem item) {
            this.item = item;
        }

        @Override
        public void computeBounds (JTextPane textPane) {
            
        }

        public void computeBounds (JButton button) {
            bounds = button.getBounds();
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                if (item.revisionExpanded) {
                    component.setToolTipText(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_CollapseRevision")); //NOI18N
                } else {
                    component.setToolTipText(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ExpandRevision")); //NOI18N
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                item.getUserData().cancelExpand();
                item.setExpanded(!item.revisionExpanded);
                summaryView.itemChanged(p);
                return true;
            }
            return false;
        }
    }

    public class EventActionsLink extends VCSHyperlinkSupport.Hyperlink {

        private Rectangle bounds;
        private final AbstractSummaryView.EventItem item;

        private EventActionsLink (AbstractSummaryView.EventItem item) {
            this.item = item;
        }

        @Override
        public void computeBounds (JTextPane textPane) {
            
        }

        public void computeBounds (JButton button) {
            bounds = button.getBounds();
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                component.setToolTipText(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowActions")); //NOI18N
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                item.actionsToPopup(p);
                return true;
            }
            return false;
        }
    }
    
    public class ShowRemainingFilesLink extends VCSHyperlinkSupport.Hyperlink {

        private Rectangle bounds;
        private final AbstractSummaryView.RevisionItem item;

        private ShowRemainingFilesLink (AbstractSummaryView.RevisionItem item) {
            this.item = item;
        }

        @Override
        public void computeBounds (JTextPane textPane) {
            
        }

        public void computeBounds (Component component) {
            bounds = component.getBounds();
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                int i = item.getNextFilesToShowCount();
                String tooltip;
                if (i > 0) {
                    tooltip = NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowMoreFiles", i); //NOI18N
                } else {
                    tooltip = NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowAllFiles"); //NOI18N
                }
                component.setToolTipText(tooltip);
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                summaryView.showRemainingFiles(item, true);
                return true;
            }
            return false;
        }
    }
    
    public class ShowLessFilesLink extends VCSHyperlinkSupport.Hyperlink {

        private Rectangle bounds;
        private final AbstractSummaryView.RevisionItem item;

        private ShowLessFilesLink (AbstractSummaryView.RevisionItem item) {
            this.item = item;
        }

        @Override
        public void computeBounds (JTextPane textPane) {
            
        }

        public void computeBounds (Component component) {
            bounds = component.getBounds();
        }

        @Override
        public boolean mouseMoved(Point p, JComponent component) {
            if (bounds != null && bounds.contains(p)) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                component.setToolTipText(NbBundle.getMessage(SummaryCellRenderer.class, "MSG_ShowLessFiles")); //NOI18N
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(Point p) {
            if (bounds != null && bounds.contains(p)) {
                summaryView.showRemainingFiles(item, false);
                return true;
            }
            return false;
        }
    }

}
