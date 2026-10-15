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
package org.netbeans.modules.editor.bracesmatching;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.view.ViewHierarchy;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyEvent;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyListener;
import org.netbeans.spi.editor.bracesmatching.BraceContext;
import org.openide.text.NbDocument;
import org.openide.text.NbDocument.CustomEditor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;

/**
 *
 * @author sdedic
 */
public class BraceMatchingSidebarComponent extends JComponent implements 
        MatchListener, FocusListener, ViewHierarchyListener, ChangeListener, Runnable, LookupListener {
    
    private static final int TOOLTIP_CHECK_DELAY = 700;
    
    public static final String BRACES_COLORING = "nbeditor-bracesMatching-sidebar"; // NOI18N

    /**
     * Client property of the JEditorPane, which orders this Component to show certain brace matches.
     * The value is 2-item array of int[]; first of which contains "origins" and the second item contains
     * "matches" as returned from the brace matching algorithm.
     */
    private static final String MATCHED_BRACES = "showMatchedBrace"; // NOI18N
            
    /**
     * The editor component
     */
    private final JTextComponent editor;
    
    /**
     * The editor pane encapsulating the text component
     */
    private final JEditorPane editorPane;
    
    /**
     * MIME type of the edited file.
     */
    private final String mimeType;
    
    /**
     * TextUI of the editor
     */
    private final BaseTextUI baseUI;

    /**
     * Line width for the outline, default 2.
     */
    private int lineWidth = 2;
    
    /**
     * Blank margin left of the outline
     */
    private int leftMargin = 1;
    
    /**
     * Width of the sidebar. Computed in {@link #updatePreferredSize() 
     */
    private int barWidth;

    /**
     * Origin + matches from the last highlight event.
     */
    private Position[]   origin;
    private Position[]   matches;
    
    /**
     * Extended context for the brace matching. Access is permitted only in EDT (otherwise unsynchronized)
     */
    private BraceContext braceContext;
    
    private int     lineHeight;
    
    private boolean showOutline;
    
    private boolean showToolTip;
    
    private Preferences prefs;

    /**
     * Prevent listeners from being reclaimed before this Component
     */
    private PreferenceChangeListener prefListenerGC;
    private JViewport viewport;

    /**
     * Coloring from user settings. Updated in {@link #updateColors}.
     */
    private Coloring coloring;
    
    /**
     * Background color handled specially, inherit means inherit from
     * parent JComponent, not from default Coloring.
     */
    private Color    backColor;
    
    private static final RequestProcessor RP = new RequestProcessor(BraceMatchingSidebarComponent.class);
    
    private final Lookup.Result colorResult;
        
    @SuppressWarnings("LeakingThisInConstructor")
    public BraceMatchingSidebarComponent(JTextComponent editor) {
        this.editor = editor;
        this.mimeType = DocumentUtilities.getMimeType(editor);
        this.prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        
        final Lookup.Result r = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(editor)).lookupResult(
                FontColorSettings.class);
        prefListenerGC = new PrefListener();
        this.colorResult = r;
        r.addLookupListener(WeakListeners.create(LookupListener.class, this , r));
        prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefListenerGC, prefs));
        loadPreferences();
        
        editorPane = findEditorPane(editor);
        Component parent = editor.getParent();
        if (parent instanceof JLayeredPane) {
            parent = parent.getParent();
        }
        if (parent instanceof JViewport) {
            this.viewport = (JViewport)parent;
            // see #219015; need to listen on viewport change to show/hide the tooltip
            viewport.addChangeListener(WeakListeners.change(this, viewport));
        }
        TextUI ui = editor.getUI();
        if (ui instanceof BaseTextUI) {
            baseUI = (BaseTextUI)ui;
            MasterMatcher.get(editor).addMatchListener(this);
        } else {
            baseUI = null;
        }
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        updatePreferredSize();
    }
    
    private void loadPreferences() {
        showOutline = prefs.getBoolean(SimpleValueNames.BRACE_SHOW_OUTLINE, true);
        showToolTip = prefs.getBoolean(SimpleValueNames.BRACE_FIRST_TOOLTIP, true);
    }
    
    /**
     * Updates the tooltip as a response to viewport scroll event. The actual
     * update is yet another runnable replanned to AWT, the Updater only 
     * provides coalescing of the scroll events.
     */
    private Task scrollUpdater = RP.create(new Runnable() {
        @Override
        // delayed runnable
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
               // runnable that can access visual hierearchy
               public void run() {
                    Rectangle visible = getVisibleRect();
                    if (tooltipYAnchor < Integer.MAX_VALUE) {
                        if (visible.y <= tooltipYAnchor) {
                            hideToolTip(true);
                        } else if (autoHidden) {
                            showTooltip();
                        }
                    }
               } 
            });
        }
    });

    @Override
    public void resultChanged(LookupEvent ev) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this);
        } else {
            run();
        }
    }
    
    public void run() {
        Iterator<FontColorSettings> fcsIt = colorResult.allInstances().iterator();
        if (fcsIt.hasNext()) {
            updateColors(fcsIt.next());
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        scrollUpdater.schedule(TOOLTIP_CHECK_DELAY);
    }
    
    private class PrefListener implements PreferenceChangeListener {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String prefName = evt == null ? null : evt.getKey();
            
            if (SimpleValueNames.BRACE_SHOW_OUTLINE.equals(prefName)) {
                showOutline = prefs.getBoolean(SimpleValueNames.BRACE_SHOW_OUTLINE, true);
                updatePreferredSize();
                BraceMatchingSidebarComponent.this.repaint();
            } else if (SimpleValueNames.BRACE_FIRST_TOOLTIP.equals(prefName)) {
                showToolTip = prefs.getBoolean(SimpleValueNames.BRACE_FIRST_TOOLTIP, true);
            }
        }
    }
    
    private static JEditorPane findEditorPane(JTextComponent editor) {
        Container c = editor.getUI().getRootView(editor).getContainer();
        return c instanceof JEditorPane ? (JEditorPane)c : null;
    }

    @Override
    public void focusGained(FocusEvent e) {}

    @Override
    public void focusLost(FocusEvent e) {
        hideToolTip(false);
    }

    @Override
    public void viewHierarchyChanged(ViewHierarchyEvent evt) {
        checkRepaint(evt);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        run();
        ViewHierarchy.get(editor).addViewHierarchyListener(this);
        editor.addFocusListener(this);
    }

    @Override
    public void removeNotify() {
        ViewHierarchy.get(editor).removeViewHierarchyListener(this);
        editor.removeFocusListener(this);
        super.removeNotify();
    }
    
    
    private void updateColors(final FontColorSettings fcs) {
        if (getParent() == null) {
            return;
        }
        AttributeSet as = fcs.getFontColors(BRACES_COLORING);
        if (as == null) {
            as = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
            this.backColor = (Color)as.getAttribute(StyleConstants.ColorConstants.Background);
        } else {
            this.backColor = (Color)as.getAttribute(StyleConstants.ColorConstants.Background);
            as = AttributesUtilities.createComposite(
                    as, 
                    fcs.getFontColors(FontColorNames.DEFAULT_COLORING));
        }
        this.coloring = Coloring.fromAttributeSet(as);
        int w = 0;
        
        if (coloring.getFont() != null) {
            w = coloring.getFont().getSize();
        } else if (baseUI != null) {
            w = baseUI.getEditorUI().getLineNumberDigitWidth();
        }
        this.barWidth = Math.max(4, w / 2);
        updatePreferredSize();
    }
    
    private void updatePreferredSize() {
        if (showOutline && baseUI != null) {
            setPreferredSize(new Dimension(barWidth, editor.getHeight()));
            lineHeight = baseUI.getEditorUI().getLineHeight();
        } else {
            setPreferredSize(new Dimension(0,0));
        }
    }
    
    public void checkRepaint(ViewHierarchyEvent evt) {
        if (!evt.isChangeY() || baseUI == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updatePreferredSize();
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!showOutline) {
            return;
        }
        int[] points;
        
        Rectangle clip = getVisibleRect();//g.getClipBounds();
        Color backColor = this.backColor;
        if (backColor == null) {
            // assume get from parent
            backColor = getBackground();
        }
        g.setColor(backColor);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        g.setColor(coloring.getForeColor());

        AbstractDocument adoc = (AbstractDocument)editor.getDocument();
        adoc.readLock();
        try {
            points = findLinePoints(origin, matches);
            if (points == null) {
                return;
            }
        
            // brace outline starts/ends in the middle of the line, just in the middle of
            // the fold mark (if it is present)
            int dist = lineHeight / 2; //(getFontMetrics(coloring.getFont()).getDescent() + 1) / 2;

            Graphics2D g2d = (Graphics2D)g;

            Stroke s = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
            if (coloring.getForeColor() != null) {
                g.setColor(coloring.getForeColor());
            }
            int start = points[0] + dist;
            int end = points[points.length - 1] - dist;
            int x = leftMargin + (lineWidth + 1) / 2;

            g2d.setStroke(s);
            g2d.drawLine(barWidth, start, x, start);

            g2d.drawLine(x, start, x, end);

            g2d.drawLine(x, end, barWidth, end);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return;
        } finally {
            adoc.readUnlock();
        }
    }

    @Override
    public void matchHighlighted(final MatchEvent evt) {
        final BraceContext[] ctx = new BraceContext[1];
        if (evt.getLocator() != null) {
            ctx[0] = evt.getLocator().findContext(evt.getOrigin()[0].getOffset());
        }
        
        editor.getDocument().render(new Runnable() {
            public void run() {
                if (ctx[0] == null) {
                    Position[] range = findTooltipRange(evt.getOrigin(), evt.getMatches());
                    if (range == null) {
                        ctx[0] = null;
                        return;
                    }
                    ctx[0] = BraceContext.create(range[0], range[1]);
                }
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
            braceContext = ctx[0];
            if (ctx[0] == null) {
                return;
            }
            if (!isEditorValid()) {
                return;
            }
            origin = evt.getOrigin();
            matches = evt.getMatches();
            repaint();
            showTooltip();
           } 
        });
    }

    @Override
    public void matchCleared(MatchEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
            braceContext = null;
            origin = null;
            repaint();
           } 
        });
    }
    
    /**
     * Computes points where the outline starts, ends and possibly when the
     * outline marks a match, but continues. The matcher can return several "matches"
     * for an origin, so the outline COULD include marks for all of them.
     * <p/>
     * The return value is interpreted as Y-coordinates.
     * 
     * @param origin values from Matcher
     * @param matches values from Matcher
     * @return array of Y coordinates of outline start/end/branching.
     */
    private int[] findLinePoints(Position[] origin, Position[] matches) throws BadLocationException {
        boolean lineDown = false;
        if (editorPane != null) {
            Object o = editorPane.getClientProperty(MATCHED_BRACES);
            if (o instanceof Position[]) {
                origin = (Position[])o;
                matches = null;
                lineDown = true;
            }
        }
        if (baseUI == null || origin == null || (matches == null && !lineDown)) {
            return null;
        }
        int minOffset = origin[0].getOffset();
        int maxOffset = origin[1].getOffset();

        int maxY;
        int minY;

        if (lineDown) {
            maxY = getSize().height;
        } else {
            for (int i = 0; i < matches.length; i += 2) {
                minOffset = Math.min(minOffset, matches[i].getOffset());
                maxOffset = Math.max(maxOffset, matches[i + 1].getOffset());
            }
            maxY = baseUI.getYFromPos(maxOffset);
        }

        minY = baseUI.getYFromPos(minOffset);

        // do not paint ranges for a single-line brace
        if (minY == maxY) {
            return null;
        }

        int height = baseUI.getEditorUI().getLineHeight();

        minY += lineWidth;
        maxY += (height - lineWidth);
        return new int[] { minY, maxY };
    }
    
    private void hideToolTip(boolean autoHidden) {
        if (isMatcherTooltipVisible()) {
            ToolTipSupport tts = baseUI.getEditorUI().getToolTipSupport();
            tts.setToolTipVisible(false);
            this.autoHidden = autoHidden;
//            this.tooltipVisible = false;
        }
    }
    
    private static Position[] findTooltipRange(Position[] origin, Position[] matches) {
        if (origin == null || matches == null) {
            return null;
        }
        int start = Integer.MAX_VALUE;
        int end = -1;
        
        Position sp = null;
        Position ep = null;
        
        // See issue #219683: in if-then-elif-else constructs, only the 2 initial pairs
        // (start and end of the initial tag/construct) are interesting. For finer control,
        // a language SPI has to be created.
        for (int i = 0; i < Math.min(matches.length, 4); i += 2) {
            Position s = matches[i];
            Position e = matches[i+1];
            
            int s2 = s.getOffset();
            if (s2 < start) {
                sp = s;
                start = s2;
            }
            int e2 = e.getOffset();
            if (e2 > end) {
                ep = e;
                end = e2;
            }
        }
        
        return new Position[] { sp, ep };
    }
    
    private boolean isEditorValid() {
        if (editor.isVisible() && Utilities.getEditorUI(editor) != null) {
            // do not operate on editors, which do not have focus
            return editor.hasFocus();
        } else {
            return false;
        }
    }
    
    public JComponent createToolTipView(int start, int end, int[] suppressRanges) {
        final JEditorPane tooltipPane = new JEditorPane();
        EditorKit kit = editorPane.getEditorKit();
        Document doc = editor.getDocument();
        if (kit == null || !(doc instanceof NbDocument.CustomEditor)) {
            return null;
        }
        CustomEditor ed = (NbDocument.CustomEditor)doc;
        Element lineRootElement = doc.getDefaultRootElement();

        // Set the same kit and document
        
        tooltipPane.setEditable(false);
        tooltipPane.setFocusable(false);
        tooltipPane.putClientProperty("nbeditorui.vScrollPolicy", JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        tooltipPane.putClientProperty("nbeditorui.hScrollPolicy", JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tooltipPane.putClientProperty("nbeditorui.selectSidebarLocations", "West");
        try {
            // Start-offset of the fold => line start => position
            int lineIndex = lineRootElement.getElementIndex(start);
            Position pos = doc.createPosition(
                    lineRootElement.getElement(lineIndex).getStartOffset());
            // DocumentView.START_POSITION_PROPERTY
            tooltipPane.putClientProperty("document-view-start-position", pos);
            // End-offset of the fold => line end => position
            lineIndex = lineRootElement.getElementIndex(end);
            pos = doc.createPosition(lineRootElement.getElement(lineIndex).getEndOffset());
            // DocumentView.END_POSITION_PROPERTY
            tooltipPane.putClientProperty("document-view-end-position", pos);
            tooltipPane.putClientProperty("document-view-accurate-span", true);

            // setEditorKit must come after doc-view-accurate-span
            tooltipPane.setEditorKit(kit);
            tooltipPane.setDocument(doc);
            EditorUI editorUI = Utilities.getEditorUI(tooltipPane);
            if (editorUI == null) {
                // see #232827; the NB editor kit is either not yet installed, or has been just uninstalled
                return null;
            }
            if (braceContext != null) {
                tooltipPane.putClientProperty(MATCHED_BRACES, origin);
            }
            if (suppressRanges != null) {
                tooltipPane.putClientProperty("nbeditorui.braces.suppressLines", suppressRanges);
            }

            tooltipPane.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e) {
                    hideToolTip(false);
                }

            });

            JComponent c = (JComponent)ed.createEditor(tooltipPane);
                /*
            c.putClientProperty("tooltip-type", "fold-preview"); // Checked in NbToolTip
            */
            Color foreColor = tooltipPane.getForeground();
            c.setBorder(new LineBorder(foreColor));
            c.setOpaque(true);

            //JComponent c2 = new BraceToolTip(editorPane, tooltipPane);
            tooltipPane.addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    // setting editorKit to null frees some resources allocated for sidebars etc.
                    if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) > 0 && !tooltipPane.isDisplayable()) {
                        tooltipPane.setEditorKit(null);
                        tooltipPane.removeHierarchyListener(this);
                    }
                }
                
            });
            return new BraceToolTip(c, tooltipPane);
        } catch (BadLocationException e) {
            // => return null
        }
        return null;
    }
    
    private boolean isMatcherTooltipVisible() {
        if (baseUI == null) {
            return false;
        }
        ToolTipSupport tts = baseUI.getEditorUI().getToolTipSupport();
        if (!(tts.isEnabled() && tts.isToolTipVisible())) {
            return false;
        }
        return tts.getToolTip() instanceof BraceToolTip;
    }
    
    /**
     * If automatically hidden because of visibility in of the anchor point in
     * the viewport. Allows to determine whether the tooltip should be shown
     * again if the anchor scrolls outside viewport
     */
    private boolean autoHidden;
    
    /**
     * Y view coordinate of the highlight range start; updated on tooltip show,
     * Integer.MAX when the range is empty.
     */
    private int tooltipYAnchor;
    
    private void showTooltip() {
        if (!showToolTip) {
            return;
        }
        if (braceContext == null) {
            autoHidden = false;
            tooltipYAnchor = Integer.MAX_VALUE;
            return;
        }
        int yFrom = braceContext.getStart().getOffset();
        int yTo = braceContext.getEnd().getOffset();
        int[] suppress = null;
        // show only iff the 1st line is out of the screen view:
        int contentHeight;
        Rectangle visible = getVisibleRect();
        
        BaseDocument bdoc = baseUI.getEditorUI().getDocument();
        if (bdoc == null) {
            return;
        }
        try {
            int yPos = baseUI.getYFromPos(yFrom);
            tooltipYAnchor = yPos;
            if (yPos >= visible.y) {
                autoHidden = true;
                return;
            }
            int yPos2 = baseUI.getYFromPos(yTo);
            contentHeight = yPos2 - yPos + lineHeight;
            if (braceContext.getRelated() != null) {
                BraceContext rel = braceContext.getRelated();
                int y1 = baseUI.getYFromPos(rel.getStart().getOffset());
                int y2 = baseUI.getYFromPos(rel.getEnd().getOffset());

                // only support preceding related segments, for now
                if (y2 < yPos) {
                    // the related content will be displayed
                    contentHeight += y2 - y1 + lineHeight;
                    // and finally the suppression line:
                    contentHeight += lineHeight;
                    
                    int startAfterRelated = Utilities.getRowStart(bdoc, rel.getEnd().getOffset(), 1);
                    int startAtContext = LineDocumentUtils.getLineStartOffset(bdoc, yFrom);
                    // measure the indent so the view can align the ellipsis 
                    int indent = Utilities.getRowIndent(bdoc, startAfterRelated);
                    // suppress the ellipsis if just a single line should be cut 
                    int nextLine = Utilities.getRowStart(bdoc, startAfterRelated, 1);
                    if (nextLine < startAtContext) {
                        suppress = new int[] {
                            startAfterRelated, startAtContext, indent
                        };
                    }
                    yFrom = rel.getStart().getOffset();
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        
        JComponent tooltip = createToolTipView(yFrom, yTo, suppress);
        if (tooltip == null) {
            return;
        }
        
        int x = 1;
        int y = contentHeight + 5;
        
        if (tooltip.getBorder() != null) {
            Insets in = tooltip.getBorder().getBorderInsets(tooltip);
            x += in.left;
            y += in.bottom;
        }
        //y += visible.y;
        
        ToolTipSupport tts = baseUI.getEditorUI().getToolTipSupport();
        tts.setToolTipVisible(true, false);
        tts.setToolTip(tooltip, 
                PopupManager.ScrollBarBounds, 
                new Point(-x, -y),
                0, 0, 0);
        tts.setToolTipVisible(true, false);
//        this.tooltipVisible = true;
    }
}
